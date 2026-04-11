package com.example.gymdietplanner

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymdietplanner.data.AppDatabase
import com.example.gymdietplanner.data.RoutineEntity
import com.example.gymdietplanner.data.MealEntity
import com.example.gymdietplanner.data.WeightEntity
import com.example.gymdietplanner.data.PreferencesManager
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
    private val preferencesManager = PreferencesManager(application)

    private val _isMetric = MutableStateFlow(preferencesManager.isMetric())
    val isMetric: StateFlow<Boolean> = _isMetric.asStateFlow()

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

    fun saveRoutine(routine: RoutineEntity) {
        viewModelScope.launch {
            routineDao.insertRoutine(routine)
        }
    }

    fun deleteRoutine(routineId: Int) {
        viewModelScope.launch {
            routineDao.deleteRoutine(routineId)
        }
    }

    fun saveMeal(meal: MealEntity) {
        viewModelScope.launch {
            mealDao.insertMeal(meal)
        }
    }

    fun deleteMeal(mealId: Int) {
        viewModelScope.launch {
            mealDao.deleteMeal(mealId)
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

    fun toggleUnitSystem() {
        val newValue = !_isMetric.value
        preferencesManager.setMetric(newValue)
        _isMetric.value = newValue
    }
}
