package com.example.gymdietplanner.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.gymdietplanner.data.RoutineEntity

@Composable
fun RoutineSessionScreen(
    routine: RoutineEntity?,
    onNavigateBack: () -> Unit,
    isMetric: Boolean
) {
    val unitSuffix = if (isMetric) "kg" else "lbs"
    if (routine == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Routine not found")
            Button(onClick = onNavigateBack, modifier = Modifier.padding(top = 16.dp)) {
                Text("Go Back")
            }
        }
        return
    }

    val exercises = routine.exercises
    if (exercises.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No exercises in this routine!")
            Button(onClick = onNavigateBack, modifier = Modifier.padding(top = 16.dp)) {
                Text("Go Back")
            }
        }
        return
    }

    val pagerState = rememberPagerState(pageCount = { exercises.size })
    val completedExerciseIndices = remember { mutableStateListOf<Int>() }

    // No App Bar or Bottom Navigation to make it fully immersive
    Box(modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .systemBarsPadding()
    ) {
        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 100.dp), // Centers the current card area
            pageSpacing = 24.dp,
            beyondViewportPageCount = 1 // Forces rendering of neighbors
        ) { page ->
            val exercise = exercises[page]

            // Center the card in the page to ensure previous/next peek-a-boo is symmetrical
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.8f) // Card takes up 80% of viewport, leaving 10% on top and bottom for peeking
                        .padding(horizontal = 32.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val isDone = completedExerciseIndices.contains(page)
                        
                        // Scrollable content
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                                .padding(bottom = 70.dp) // Leave room for pinned button
                                .then(if (isDone) Modifier.blur(12.dp) else Modifier),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Exercise Image with forced white background
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .padding(bottom = 16.dp)
                                    .clip(MaterialTheme.shapes.medium)
                                    .background(Color.White)
                            ) {
                                if (exercise.exercise.iconName != null) {
                                    AsyncImage(
                                        model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                                            .data("file:///android_asset/exercises/${exercise.exercise.iconName}")
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = exercise.exercise.name,
                                        contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                                        modifier = Modifier.fillMaxSize().padding(12.dp)
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(MaterialTheme.colorScheme.surfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.FitnessCenter,
                                            contentDescription = null,
                                            modifier = Modifier.size(80.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }

                            Text(
                                text = exercise.exercise.name,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                textAlign = TextAlign.Center
                            )
                            
                            Text(
                                text = exercise.exercise.equipment,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Stats Summary Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("SETS", style = MaterialTheme.typography.labelSmall)
                                    Text(
                                        "${exercise.sets.size}",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("WEIGHT ($unitSuffix)", style = MaterialTheme.typography.labelSmall)
                                    Text(
                                        exercise.sets.firstOrNull()?.weight?.ifBlank { "-" } ?: "-",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("REPS", style = MaterialTheme.typography.labelSmall)
                                    Text(
                                        exercise.sets.firstOrNull()?.reps?.ifBlank { "-" } ?: "-",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Detailed Sets View
                            Text(
                                "Target Sets",
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.align(Alignment.Start).padding(bottom = 8.dp)
                            )
                            
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(exercise.sets.size) { index ->
                                    val set = exercise.sets[index]
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                                MaterialTheme.shapes.small
                                            )
                                            .padding(8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Set ${index + 1}", fontWeight = FontWeight.Medium)
                                        Text("${set.weight.ifBlank { "0" }} $unitSuffix x ${set.reps.ifBlank { "0" }}")
                                    }
                                }
                            }
                        }

                        // Pinned "MARK DONE" button
                        Button(
                            onClick = {
                                if (isDone) completedExerciseIndices.remove(page)
                                else completedExerciseIndices.add(page)
                            },
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .padding(16.dp),
                            colors = if (isDone) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                                     else ButtonDefaults.buttonColors()
                        ) {
                            Text(if (isDone) "UNDO DONE" else "MARK DONE")
                        }

                        // Completion Overlay (Inside the Box to cover everything)
                        if (isDone) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                                    .clickable { completedExerciseIndices.remove(page) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = "Completed",
                                    modifier = Modifier.size(120.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
