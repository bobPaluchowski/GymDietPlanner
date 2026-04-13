package com.example.gymdietplanner

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymdietplanner.data.AppDatabase
import com.example.gymdietplanner.data.RoutineEntity
import com.example.gymdietplanner.data.MealEntity
import com.example.gymdietplanner.data.WeightEntity
import com.example.gymdietplanner.data.ExerciseEntity
import com.example.gymdietplanner.data.rawExercises
import com.example.gymdietplanner.data.rawExercises
import com.example.gymdietplanner.data.PreferencesManager
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

    val exercises: StateFlow<List<ExerciseEntity>> = exerciseDao.getAllExercises()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Initialize notification channels as soon as the app starts
        NotificationHelper(application)
        reminderManager.scheduleAll()
        prepopulateExercises()
    }

    private fun prepopulateExercises() {
        viewModelScope.launch {
            if (exerciseDao.getExerciseCount() == 0) {
                val defaultExercises = mutableListOf<ExerciseEntity>()
                rawExercises.forEach { muscleGroup ->
                    muscleGroup.exercises.forEach { ex ->
                        defaultExercises.add(
                            ExerciseEntity(
                                name = ex.name,
                                equipment = ex.equipment,
                                category = muscleGroup.name,
                                isCustom = false
                            )
                        )
                    }
                }
                exerciseDao.insertExercises(defaultExercises)
            }
        }
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
                    name = name,
                    equipment = equipment,
                    category = category,
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
