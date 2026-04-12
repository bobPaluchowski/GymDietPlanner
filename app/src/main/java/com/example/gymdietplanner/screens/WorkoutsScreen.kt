package com.example.gymdietplanner.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check

import com.example.gymdietplanner.data.Exercise
import com.example.gymdietplanner.data.rawExercises

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 24.dp, start = 16.dp, end = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .defaultMinSize(minHeight = 48.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Exercise Library",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        ExerciseLibraryList(onExerciseClick = { /* TODO in Workouts tab */ })
    }
}

@Composable
fun ExerciseLibraryList(onExerciseClick: (Exercise) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        rawExercises.forEach { group ->
            item {
                Text(
                    text = group.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }

            items(group.exercises) { exercise ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onExerciseClick(exercise) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = exercise.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = exercise.equipment,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun MultiSelectExerciseList(onExercisesSelected: (List<Exercise>) -> Unit) {
    val selectedItems = remember { mutableStateListOf<Exercise>() }
    
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            rawExercises.forEach { group ->
                item {
                    Text(
                        text = group.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp, start = 8.dp)
                    )
                }

                items(group.exercises) { exercise ->
                    val isSelected = selectedItems.contains(exercise)
                    val cardBackgroundColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isSelected) selectedItems.remove(exercise) else selectedItems.add(exercise)
                            }
                            .background(cardBackgroundColor)
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = CircleShape,
                            color = if (isSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                } else {
                                    Text(
                                        text = exercise.name.take(1).uppercase(),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column {
                            Text(
                                text = exercise.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = exercise.equipment,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(80.dp)) // Extra padding for the FAB
            }
        }
        
        if (selectedItems.isNotEmpty()) {
            FloatingActionButton(
                onClick = { onExercisesSelected(selectedItems.toList()) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Accept list"
                )
            }
        }
    }
}
