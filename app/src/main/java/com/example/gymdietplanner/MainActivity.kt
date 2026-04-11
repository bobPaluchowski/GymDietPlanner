package com.example.gymdietplanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.gymdietplanner.screens.DashboardScreen
import com.example.gymdietplanner.screens.RoutinesScreen
import com.example.gymdietplanner.screens.WorkoutsScreen
import com.example.gymdietplanner.screens.MealsScreen
import com.example.gymdietplanner.screens.WeightScreen
import com.example.gymdietplanner.ui.theme.GymDietPlannerTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import com.example.gymdietplanner.screens.CreateRoutineScreen
import com.example.gymdietplanner.screens.CreateMealScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GymDietPlannerTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: MainViewModel = viewModel()) {
    val navController = rememberNavController()

    val screens = listOf(
        Screen.Dashboard,
        Screen.Routines,
        Screen.Workouts,
        Screen.Meals,
        Screen.Weight
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Immersive filtering: Only show Bottom Navigation if the route matches the core tabs
    val showBottomBar = screens.any { it.route == currentDestination?.route } || currentDestination?.route == null

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    screens.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        // When standard screens are mapped we assign the padding, however deep full-screen elements can map padding manually or consume it
        val paddingModifier = if (showBottomBar) Modifier.padding(innerPadding) else Modifier

        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = paddingModifier
        ) {
            composable(Screen.Dashboard.route) { 
                val routines by viewModel.routines.collectAsState()
                val meals by viewModel.meals.collectAsState()
                val weights by viewModel.weights.collectAsState()
                val isMetric by viewModel.isMetric.collectAsState()
                DashboardScreen(routines, meals, weights, isMetric, onToggleUnit = { viewModel.toggleUnitSystem() }) 
            }
            composable(Screen.Routines.route) {
                val routines by viewModel.routines.collectAsState()
                val isMetric by viewModel.isMetric.collectAsState()
                RoutinesScreen(
                    routines = routines,
                    onCreateRoutineClick = { navController.navigate(Screen.CreateRoutine.route) },
                    onRoutineClick = { routineId -> navController.navigate(Screen.RoutineSession.createRoute(routineId)) },
                    onEditRoutineClick = { routineId -> navController.navigate(Screen.EditRoutine.createRoute(routineId)) },
                    onDeleteRoutineClick = { routine -> viewModel.deleteRoutine(routine.id) }
                )
            }
            composable(Screen.CreateRoutine.route) {
                val isMetric by viewModel.isMetric.collectAsState()
                CreateRoutineScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onSaveRoutine = { routine -> viewModel.saveRoutine(routine) },
                    isMetric = isMetric
                )
            }
            composable(Screen.EditRoutine.route) { backStackEntry ->
                val routineId = backStackEntry.arguments?.getString("routineId")?.toIntOrNull()
                val routines by viewModel.routines.collectAsState()
                val routine = routines.find { it.id == routineId }
                val isMetric by viewModel.isMetric.collectAsState()
                
                CreateRoutineScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onSaveRoutine = { updatedRoutine -> viewModel.saveRoutine(updatedRoutine) },
                    routine = routine,
                    isMetric = isMetric
                )
            }
            composable(Screen.RoutineSession.route) { backStackEntry ->
                val routineId = backStackEntry.arguments?.getString("routineId")?.toIntOrNull()
                val routines by viewModel.routines.collectAsState()
                val routine = routines.find { it.id == routineId }
                val isMetric by viewModel.isMetric.collectAsState()
                
                com.example.gymdietplanner.screens.RoutineSessionScreen(
                    routine = routine,
                    onNavigateBack = { navController.popBackStack() },
                    isMetric = isMetric
                )
            }
            composable(Screen.Workouts.route) { WorkoutsScreen() }
            composable(Screen.Meals.route) {
                val meals by viewModel.meals.collectAsState()
                MealsScreen(
                    meals = meals,
                    onCreateMealClick = { navController.navigate(Screen.CreateMeal.route) },
                    onDeleteMealClick = { meal -> viewModel.deleteMeal(meal.id) }
                )
            }
            composable(Screen.CreateMeal.route) {
                CreateMealScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onSaveMeal = { meal -> viewModel.saveMeal(meal) }
                )
            }
            composable(Screen.Weight.route) {
                val weights by viewModel.weights.collectAsState()
                val isMetric by viewModel.isMetric.collectAsState()
                WeightScreen(
                    weights = weights,
                    isMetric = isMetric,
                    onSaveWeight = { viewModel.saveWeight(it) },
                    onDeleteWeight = { viewModel.deleteWeight(it.id) }
                )
            }
        }
    }
}