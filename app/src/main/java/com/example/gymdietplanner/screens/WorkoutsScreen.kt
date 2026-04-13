package com.example.gymdietplanner.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import com.example.gymdietplanner.data.Exercise
import com.example.gymdietplanner.data.ExerciseEntity
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import android.os.Build
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutsScreen(
    exercises: List<Exercise>,
    isLoading: Boolean,
    onSearch: (String) -> Unit,
    onSaveExercise: (String, String, String) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Workout")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
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
                if (isLoading) {
                    Spacer(modifier = Modifier.width(16.dp))
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                }
            }

            ExerciseLibraryList(
                exercises = exercises,
                onSearch = onSearch,
                onExerciseClick = { /* TODO in Workouts tab */ }
            )
        }

        if (showAddDialog) {
            AddExerciseDialog(
                onDismiss = { showAddDialog = false },
                onSave = { name, equipment, category ->
                    onSaveExercise(name, equipment, category)
                    showAddDialog = false
                },
                existingCategories = listOf("Chest", "Back", "Legs", "Shoulders", "Arms", "Core")
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExerciseDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit,
    existingCategories: List<String>
) {
    var name by remember { mutableStateOf("") }
    var equipment by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Custom Workout") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Workout Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = equipment,
                    onValueChange = { equipment = it },
                    label = { Text("Equipment (e.g. Barbell, Machine)") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Category Selection
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category (Muscle Group)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        readOnly = false // Allow typing new ones too
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        existingCategories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank() && category.isNotBlank()) onSave(name, equipment, category) },
                enabled = name.isNotBlank() && category.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ExerciseLibraryList(
    exercises: List<Exercise>,
    onSearch: (String) -> Unit,
    onExerciseClick: (Exercise) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(searchQuery) {
        delay(500)
        onSearch(searchQuery)
    }

    val groupedExercises = remember(exercises) {
        exercises.groupBy { it.targetMuscles.firstOrNull() ?: "Other" }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                placeholder = { Text("Search exercises...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )
        }

        if (groupedExercises.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No exercises found",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        groupedExercises.forEach { (category, categoryExercises) ->
            item {
                Text(
                    text = category,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }

            items(categoryExercises) { exercise ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onExerciseClick(exercise) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val firstImage = exercise.imageUrls.firstOrNull()
                        val context = LocalContext.current
                        val imageRequest: ImageRequest = remember(firstImage) {
                            ImageRequest.Builder(context)
                                .data(firstImage)
                                .crossfade(true)
                                .build()
                        }

                        AsyncImage(
                            model = imageRequest,
                            contentDescription = null,
                            modifier = Modifier
                                .size(50.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.shapes.small)
                                .padding(2.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column {
                            Text(
                                text = exercise.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = exercise.equipments.joinToString(", "),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MultiSelectExerciseList(
    exercises: List<Exercise>,
    onSearch: (String) -> Unit,
    onExercisesSelected: (List<Exercise>) -> Unit
) {
    val selectedItems = remember { mutableStateListOf<Exercise>() }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(searchQuery) {
        delay(500)
        onSearch(searchQuery)
    }

    val groupedExercises = remember(exercises) {
        exercises.groupBy { it.targetMuscles.firstOrNull() ?: "Other" }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    placeholder = { Text("Search exercises...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )
            }

            if (groupedExercises.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No exercises found",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            groupedExercises.forEach { (category, categoryExercises) ->
                item {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp, start = 8.dp)
                    )
                }

                items(categoryExercises) { exercise ->
                    val isSelected = selectedItems.any { it.exerciseId == exercise.exerciseId }
                    val cardBackgroundColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isSelected) {
                                    selectedItems.removeAll { it.exerciseId == exercise.exerciseId }
                                } else {
                                    selectedItems.add(exercise)
                                }
                            }
                            .background(cardBackgroundColor)
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val context = LocalContext.current
                        if (isSelected) {
                             Surface(
                                modifier = Modifier.size(50.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        } else {
                            val firstImage = exercise.imageUrls.firstOrNull()
                            val imageRequest: ImageRequest = remember(firstImage) {
                                ImageRequest.Builder(context)
                                    .data(firstImage)
                                    .crossfade(true)
                                    .build()
                            }
                            AsyncImage(
                                model = imageRequest,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(50.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.small)
                                    .padding(2.dp)
                            )
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
                                text = exercise.equipments.joinToString(", "),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(80.dp))
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
