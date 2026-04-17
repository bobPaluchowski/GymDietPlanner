package com.example.gymdietplanner.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.activity.compose.BackHandler
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gymdietplanner.data.Exercise
import com.example.gymdietplanner.data.RoutineEntity
import com.example.gymdietplanner.data.RoutineExercise
import com.example.gymdietplanner.data.WorkoutSet
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import com.example.gymdietplanner.data.ExerciseEntity
import kotlinx.coroutines.launch
import com.example.gymdietplanner.utils.getExerciseIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRoutineScreen(
    onNavigateBack: () -> Unit,
    onSaveRoutine: (RoutineEntity) -> Unit,
    routine: RoutineEntity? = null,
    isMetric: Boolean,
    exercises: List<ExerciseEntity>
) {
    val unitSuffix = if (isMetric) "kg" else "lbs"
    var routineName by rememberSaveable { mutableStateOf(routine?.name ?: "") }
    var repeatWeekly by rememberSaveable { mutableStateOf(routine?.repeatWeekly ?: false) }
    
    val daysOfWeek = listOf("M", "T", "W", "Th", "F", "S", "Su")
    val selectedDays = remember { mutableStateListOf<String>().apply { addAll(routine?.days ?: emptyList()) } }
    val selectedExercises = remember { mutableStateListOf<RoutineExercise>().apply { addAll(routine?.exercises ?: emptyList()) } }
    
    var showSelectionScreen by remember { mutableStateOf(false) }
    
    // Proper back handling: If selection screen is open, back button closes it first.
    BackHandler(enabled = showSelectionScreen) {
        showSelectionScreen = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (routine == null) "Create Routine" else "Edit Routine") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = routineName,
                onValueChange = { routineName = it },
                label = { Text("Routine Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Workouts",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                TextButton(onClick = { showSelectionScreen = true }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Workout")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Workout")
                }
            }

            // Display added exercises
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(selectedExercises.toList()) { routineExercise ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        modifier = Modifier.size(32.dp),
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = getExerciseIcon(routineExercise.exercise.iconName),
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp),
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(text = routineExercise.exercise.name, fontWeight = FontWeight.Bold)
                                        Text(text = routineExercise.exercise.equipment, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                                IconButton(onClick = { selectedExercises.remove(routineExercise) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Remove")
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            routineExercise.sets.forEachIndexed { index, set ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Set ${index + 1}", modifier = Modifier.weight(1f))
                                    
                                    OutlinedTextField(
                                        value = set.weight,
                                        onValueChange = { newWeight -> 
                                            val updatedSet = set.copy(weight = newWeight)
                                            val newSets = routineExercise.sets.toMutableList()
                                            newSets[index] = updatedSet
                                            val newRoutineExercise = routineExercise.copy(sets = newSets)
                                            val itemIndex = selectedExercises.indexOf(routineExercise)
                                            if(itemIndex != -1) selectedExercises[itemIndex] = newRoutineExercise
                                        },
                                        label = { Text(unitSuffix) },
                                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                                        singleLine = true
                                    )
                                    
                                    OutlinedTextField(
                                        value = set.reps,
                                        onValueChange = { newReps -> 
                                            val updatedSet = set.copy(reps = newReps)
                                            val newSets = routineExercise.sets.toMutableList()
                                            newSets[index] = updatedSet
                                            val newRoutineExercise = routineExercise.copy(sets = newSets)
                                            val itemIndex = selectedExercises.indexOf(routineExercise)
                                            if(itemIndex != -1) selectedExercises[itemIndex] = newRoutineExercise
                                        },
                                        label = { Text("reps") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                }
                            }
                            
                            TextButton(
                                onClick = {
                                    val newSets = routineExercise.sets.toMutableList().apply { add(WorkoutSet()) }
                                    val newRoutineExercise = routineExercise.copy(sets = newSets)
                                    val itemIndex = selectedExercises.indexOf(routineExercise)
                                    if(itemIndex != -1) selectedExercises[itemIndex] = newRoutineExercise
                                },
                                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp)
                            ) {
                                Text("+ Add Set")
                            }
                        }
                    }
                }
            }

            Text(
                text = "Assign Days",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                daysOfWeek.forEach { day ->
                    val isSelected = selectedDays.contains(day)
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        onClick = {
                            if (isSelected) selectedDays.remove(day) else selectedDays.add(day)
                        }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = day,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Repeat Weekly",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Switch(
                    checked = repeatWeekly,
                    onCheckedChange = { repeatWeekly = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val newRoutine = RoutineEntity(
                        id = routine?.id ?: 0,
                        name = routineName.takeIf { it.isNotBlank() } ?: "Unnamed Routine",
                        days = selectedDays.toList(),
                        repeatWeekly = repeatWeekly,
                        exercises = selectedExercises.toList()
                    )
                    onSaveRoutine(newRoutine)
                    onNavigateBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Save Routine", style = MaterialTheme.typography.titleMedium)
            }
        }
    }

    // Full-screen selection overlay
        AnimatedVisibility(
            visible = showSelectionScreen,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Select Workouts") },
                        navigationIcon = {
                            IconButton(onClick = { showSelectionScreen = false }) {
                                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            titleContentColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            ) { padding ->
                Box(modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
                ) {
                    MultiSelectExerciseList(
                        exercises = exercises,
                        onExercisesSelected = { selected ->
                            val mappedExercises = selected.map { RoutineExercise(exercise = it) }
                            selectedExercises.addAll(mappedExercises)
                            showSelectionScreen = false
                        }
                    )
                }
            }
        }
    }
}
