package com.example.gymdietplanner
import java.util.UUID

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymdietplanner.data.AppDatabase
import com.example.gymdietplanner.data.RoutineEntity
import com.example.gymdietplanner.data.MealEntity
import com.example.gymdietplanner.data.WeightEntity
import android.util.Log
import com.example.gymdietplanner.data.Exercise
import com.example.gymdietplanner.data.ExerciseEntity
import com.example.gymdietplanner.data.PreferencesManager
import com.example.gymdietplanner.api.RetrofitClient
import com.example.gymdietplanner.api.ExerciseResponse
import com.example.gymdietplanner.notifications.NotificationHelper
import com.example.gymdietplanner.notifications.ReminderManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val routineDao = AppDatabase.getDatabase(application).routineDao()
    private val mealDao = AppDatabase.getDatabase(application).mealDao()
    private val weightDao = AppDatabase.getDatabase(application).weightDao()
    private val exerciseDao = AppDatabase.getDatabase(application).exerciseDao()
    private val preferencesManager = PreferencesManager(application)
    private val reminderManager = ReminderManager(application)

    private val _isMetric = MutableStateFlow(preferencesManager.isMetric())
    val isMetric: StateFlow<Boolean> = _isMetric.asStateFlow()

    private val _isWorkoutNotificationsEnabled = MutableStateFlow(preferencesManager.isWorkoutNotificationsEnabled())
    val isWorkoutNotificationsEnabled: StateFlow<Boolean> = _isWorkoutNotificationsEnabled.asStateFlow()

    private val _isMealNotificationsEnabled = MutableStateFlow(preferencesManager.isMealNotificationsEnabled())
    val isMealNotificationsEnabled: StateFlow<Boolean> = _isMealNotificationsEnabled.asStateFlow()

    val routines: StateFlow<List<RoutineEntity>> = routineDao.getAllRoutines()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val meals: StateFlow<List<MealEntity>> = mealDao.getAllMeals()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val weights: StateFlow<List<WeightEntity>> = weightDao.getAllWeights()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _apiLibrary = MutableStateFlow<List<Exercise>>(emptyList())
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _customExercises = exerciseDao.getAllExercises()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val exercises: StateFlow<List<Exercise>> = MutableStateFlow<List<Exercise>>(emptyList()) // Placeholder, will update in init
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Actual combined StateFlow logic will be handled better or I'll just keep them separate for UI
    val apiLibrary = _apiLibrary.asStateFlow()

    init {
        NotificationHelper(application)
        reminderManager.scheduleAll()
        fetchLibrary()
    }

    private fun fetchLibrary() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                Log.d("GymApp", "Fetching exercises from AscendAPI...")
                val response = RetrofitClient.instance.getExercises()
                Log.d("GymApp", "Response code: ${response.code()}")
                if (response.isSuccessful) {
                    val body = response.body()
                    Log.d("GymApp", "Response body success=${body?.success}, count=${body?.data?.size}")
                    val exercises = body?.data?.map { it.toDomain() } ?: emptyList()
                    Log.d("GymApp", "Parsed ${exercises.size} exercises")
                    _apiLibrary.value = exercises
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("GymApp", "API error ${response.code()}: $errorBody")
                }
            } catch (e: Exception) {
                Log.e("GymApp", "Exception fetching exercises: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchLibrary(query: String) {
        if (query.isBlank()) {
            fetchLibrary()
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            try {
                Log.d("GymApp", "Searching exercises for: '$query'")
                val response = RetrofitClient.instance.searchExercises(query)
                Log.d("GymApp", "Search response code: ${response.code()}")
                if (response.isSuccessful) {
                    val body = response.body()
                    val exercises = body?.data?.map { it.toDomain() } ?: emptyList()
                    Log.d("GymApp", "Search returned ${exercises.size} exercises")
                    _apiLibrary.value = exercises
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("GymApp", "Search API error ${response.code()}: $errorBody")
                }
            } catch (e: Exception) {
                Log.e("GymApp", "Exception searching exercises: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun ExerciseResponse.toDomain(): Exercise {
        return Exercise(
            exerciseId = this.exerciseId ?: "",
            name = this.name ?: "Unknown",
            targetMuscles = this.targetMuscles ?: this.bodyParts ?: emptyList(),
            secondaryMuscles = this.secondaryMuscles ?: emptyList(),
            muscles = emptyList(),
            equipments = this.equipments ?: emptyList(),
            imageUrls = listOfNotNull(this.imageUrl),  // wrap single URL into list
            videoUrls = this.videoUrls ?: emptyList(),
            instructions = this.instructions ?: emptyList()
        )
    }

    fun saveRoutine(routine: RoutineEntity) {
        viewModelScope.launch {
            routineDao.insertRoutine(routine)
            reminderManager.scheduleAll()
        }
    }

    fun deleteRoutine(routineId: Int) {
        viewModelScope.launch {
            routineDao.deleteRoutine(routineId)
            reminderManager.scheduleAll()
        }
    }

    fun saveMeal(meal: MealEntity) {
        viewModelScope.launch {
            mealDao.insertMeal(meal)
            reminderManager.scheduleAll()
        }
    }

    fun deleteMeal(mealId: Int) {
        viewModelScope.launch {
            mealDao.deleteMeal(mealId)
            reminderManager.scheduleAll()
        }
    }

    fun saveWeight(weight: WeightEntity) {
        viewModelScope.launch {
            weightDao.insertWeight(weight)
        }
    }

    fun deleteWeight(weightId: Int) {
        viewModelScope.launch {
            weightDao.deleteWeight(weightId)
        }
    }

    fun saveExercise(name: String, equipment: String, category: String) {
        viewModelScope.launch {
            exerciseDao.insertExercise(
                ExerciseEntity(
                    exerciseId = "custom_${UUID.randomUUID()}",
                    name = name,
                    equipments = listOf(equipment),
                    targetMuscles = listOf(category),
                    isCustom = true
                )
            )
        }
    }

    fun toggleUnitSystem() {
        val newValue = !_isMetric.value
        preferencesManager.setMetric(newValue)
        _isMetric.value = newValue
    }

    fun toggleWorkoutNotifications() {
        val newValue = !_isWorkoutNotificationsEnabled.value
        preferencesManager.setWorkoutNotificationsEnabled(newValue)
        _isWorkoutNotificationsEnabled.value = newValue
        reminderManager.scheduleAll()
    }

    fun toggleMealNotifications() {
        val newValue = !_isMealNotificationsEnabled.value
        preferencesManager.setMealNotificationsEnabled(newValue)
        _isMealNotificationsEnabled.value = newValue
        reminderManager.scheduleAll()
    }
}
