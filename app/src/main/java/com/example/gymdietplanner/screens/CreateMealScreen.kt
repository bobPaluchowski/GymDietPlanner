package com.example.gymdietplanner.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gymdietplanner.data.MealEntity
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.material.icons.filled.Schedule

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateMealScreen(
    onNavigateBack: () -> Unit,
    onSaveMeal: (MealEntity) -> Unit
) {
    var mealName by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("12:00") }
    var showTimePicker by remember { mutableStateOf(false) }
    var currentIngredient by remember { mutableStateOf("") }
    val ingredients = remember { mutableStateListOf<String>() }
    var instructions by remember { mutableStateOf("") }

    val daysOfWeek = listOf("M", "T", "W", "Th", "F", "S", "Su")
    val selectedDays = remember { mutableStateListOf<String>() }

    Scaffold(
        modifier = Modifier.pointerInput(Unit) {
            var gestured = false
            detectHorizontalDragGestures(
                onDragStart = { gestured = false },
                onHorizontalDrag = { _, dragAmount ->
                    if (!gestured && dragAmount > 30) {
                        gestured = true
                        onNavigateBack()
                    }
                }
            )
        },
        topBar = {
            TopAppBar(
                title = { Text("Create New Meal", fontWeight = FontWeight.Bold) },
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
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Meal Identity
            item {
                OutlinedTextField(
                    value = mealName,
                    onValueChange = { mealName = it },
                    label = { Text("Meal Name") },
                    placeholder = { Text("e.g., Grilled Salmon & Asparagus") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    leadingIcon = { Icon(Icons.Filled.Restaurant, contentDescription = null) }
                )
            }

            item {
                OutlinedTextField(
                    value = time,
                    onValueChange = { /* Read only, changed via picker */ },
                    label = { Text("Meal Time") },
                    placeholder = { Text("Set meal time") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showTimePicker = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Pick Time") // Using Add as clock placeholder if not available
                        }
                    }
                )
            }

            // Ingredients Section
            item {
                Text(
                    "Ingredients",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = currentIngredient,
                        onValueChange = { currentIngredient = it },
                        label = { Text("Ingredient Name") },
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FloatingActionButton(
                        onClick = {
                            if (currentIngredient.isNotBlank()) {
                                ingredients.add(currentIngredient)
                                currentIngredient = ""
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Add Ingredient")
                    }
                }
            }

            // Ingredient List Cards
            items(ingredients) { ingredient ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                    ),
                    shape = MaterialTheme.shapes.small
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(ingredient, style = MaterialTheme.typography.bodyLarge)
                        IconButton(onClick = { ingredients.remove(ingredient) }) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "Remove",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Instructions
            item {
                OutlinedTextField(
                    value = instructions,
                    onValueChange = { instructions = it },
                    label = { Text("Instructions / Preparation") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 150.dp),
                    shape = MaterialTheme.shapes.medium,
                    singleLine = false
                )
            }

            // Assign Days Section
            item {
                Text(
                    text = "Assign Days",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
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
            }

            // Save Button
            item {
                Button(
                    onClick = {
                        if (mealName.isNotBlank()) {
                            onSaveMeal(
                                MealEntity(
                                    name = mealName,
                                    time = time,
                                    days = selectedDays.toList(),
                                    ingredients = ingredients.toList(),
                                    instructions = instructions
                                )
                            )
                            onNavigateBack()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Save Meal", style = MaterialTheme.typography.titleMedium)
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        if (showTimePicker) {
            val state = rememberTimePickerState(
                initialHour = time.split(":")[0].toIntOrNull() ?: 12,
                initialMinute = time.split(":")[1].toIntOrNull() ?: 0,
                is24Hour = true
            )
            AlertDialog(
                onDismissRequest = { showTimePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        time = String.format("%02d:%02d", state.hour, state.minute)
                        showTimePicker = false
                    }) { Text("Confirm") }
                },
                dismissButton = {
                    TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
                },
                text = {
                    TimePicker(state = state)
                }
            )
        }
    }
}
