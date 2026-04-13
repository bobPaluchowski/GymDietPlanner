package com.example.gymdietplanner

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Filled.Home)
    object Routines : Screen("routines", "Routines", Icons.Filled.Assignment)
    object CreateRoutine : Screen("create_routine", "Create Routine", Icons.Filled.Add)
    object EditRoutine : Screen("edit_routine/{routineId}", "Edit Routine", Icons.Filled.Add) {
        fun createRoute(routineId: Int) = "edit_routine/$routineId"
    }
    object RoutineSession : Screen("routine_session/{routineId}", "Session", Icons.Filled.PlayArrow) {
        fun createRoute(routineId: Int) = "routine_session/$routineId"
    }
    object Workouts : Screen("workouts", "Workouts", Icons.Filled.FitnessCenter)
    object Meals : Screen("meals", "Meals", Icons.Filled.Restaurant)
    object CreateMeal : Screen("create_meal", "New Meal", Icons.Filled.Add)
    object EditMeal : Screen("edit_meal/{mealId}", "Edit Meal", Icons.Filled.Add) {
        fun createRoute(mealId: Int) = "edit_meal/$mealId"
    }
    object Weight : Screen("weight", "Weight", Icons.Filled.MonitorWeight)
    object Settings : Screen("settings", "Settings", Icons.Filled.Settings)
}
