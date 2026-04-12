package com.example.gymdietplanner.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.gymdietplanner.data.RoutineEntity

@Composable
fun RoutinesScreen(
    routines: List<RoutineEntity>,
    onCreateRoutineClick: () -> Unit,
    onRoutineClick: (Int) -> Unit,
    onEditRoutineClick: (Int) -> Unit,
    onDeleteRoutineClick: (RoutineEntity) -> Unit
) {
    val listState = rememberLazyListState()
    var isFabExpanded by remember { mutableStateOf(true) }
    var routineToDelete by remember { mutableStateOf<RoutineEntity?>(null) }
    
    LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
        isFabExpanded = listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
    }

    if (routineToDelete != null) {
        AlertDialog(
            onDismissRequest = { routineToDelete = null },
            title = { Text("Delete routine?") },
            text = { Text("Are you sure you want to delete this routine? This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteRoutineClick(routineToDelete!!)
                        routineToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { routineToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        floatingActionButton = {
            if (routines.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    text = { Text("Add Routine") },
                    icon = { Icon(Icons.Filled.Add, contentDescription = "Add Routine") },
                    onClick = onCreateRoutineClick,
                    expanded = isFabExpanded,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
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
                    text = "Your Routines",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (routines.isEmpty()) {
                EmptyRoutinesState(onCreateRoutineClick)
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(routines) { routine ->
                        RoutineCard(
                            routine = routine,
                            onRoutineClick = onRoutineClick,
                            onEditRoutineClick = onEditRoutineClick,
                            onDeleteClick = { routineToDelete = routine }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RoutineCard(
    routine: RoutineEntity,
    onRoutineClick: (Int) -> Unit,
    onEditRoutineClick: (Int) -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onRoutineClick(routine.id) },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val dayMap = mapOf(
                "M" to "Monday",
                "T" to "Tuesday",
                "W" to "Wednesday",
                "Th" to "Thursday",
                "F" to "Friday",
                "S" to "Saturday",
                "Su" to "Sunday"
            )
            val fullDays = routine.days.map { dayMap[it] ?: it }.joinToString(", ")
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = routine.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Days: ${fullDays.takeIf { it.isNotBlank() } ?: "None"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Workouts: ${routine.exercises.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
            Row {
                IconButton(onClick = { onEditRoutineClick(routine.id) }) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit Routine",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete Routine",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyRoutinesState(onCreateRoutineClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(120.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Filled.FitnessCenter,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            "No Routines Saved",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            "Build your strength by planning your first workout routine.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onCreateRoutineClick,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Create First Routine", style = MaterialTheme.typography.titleMedium)
        }
    }
}
