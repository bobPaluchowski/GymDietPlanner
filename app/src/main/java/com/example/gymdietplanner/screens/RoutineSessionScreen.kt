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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.gymdietplanner.data.RoutineEntity
import com.example.gymdietplanner.data.Exercise
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import android.os.Build
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun RoutineSessionScreen(
    routine: RoutineEntity?,
    library: List<Exercise>,
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
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        val isDone = completedExerciseIndices.contains(page)
                        
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                                .then(if (isDone) Modifier.blur(12.dp) else Modifier),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Large Exercise Image
                            val fullExercise = library.find { it.exerciseId == exercise.exerciseId }
                            val firstImage = fullExercise?.imageUrls?.firstOrNull()
                            
                            val context = LocalContext.current
                            val imageRequest: ImageRequest = remember(firstImage) {
                                ImageRequest.Builder(context)
                                    .data(firstImage)
                                    .crossfade(true)
                                    .build()
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp),
                                shape = MaterialTheme.shapes.medium,
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                AsyncImage(
                                    model = imageRequest,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = fullExercise?.name ?: exercise.exerciseName,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                textAlign = TextAlign.Center
                            )
                            
                            Text(
                                text = (fullExercise?.equipments?.joinToString(", ") ?: "Target: ${fullExercise?.targetMuscles?.joinToString(", ") ?: "" }").uppercase(),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Stats Summary Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("SETS", style = MaterialTheme.typography.labelSmall)
                                    Text("${exercise.sets.size}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("WEIGHT ($unitSuffix)", style = MaterialTheme.typography.labelSmall)
                                    Text(exercise.sets.firstOrNull()?.weight?.ifBlank { "-" } ?: "-", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("REPS", style = MaterialTheme.typography.labelSmall)
                                    Text(exercise.sets.firstOrNull()?.reps?.ifBlank { "-" } ?: "-", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Scrollable Instructions & Sets
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                if (fullExercise != null && fullExercise.instructions.isNotEmpty()) {
                                    Text(
                                        "Instructions",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                    fullExercise.instructions.forEachIndexed { index, step ->
                                        Text(
                                            text = "${index + 1}. $step",
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.padding(bottom = 4.dp),
                                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                }

                                Text(
                                    "Target Sets",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                
                                exercise.sets.forEachIndexed { index, set ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .background(
                                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                                MaterialTheme.shapes.small
                                            )
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Set ${index + 1}", fontWeight = FontWeight.Medium)
                                        Text("${set.weight.ifBlank { "0" }} $unitSuffix x ${set.reps.ifBlank { "0" }}")
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    if (isDone) completedExerciseIndices.remove(page)
                                    else completedExerciseIndices.add(page)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp),
                                colors = if (isDone) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                                         else ButtonDefaults.buttonColors()
                            ) {
                                Text(if (isDone) "UNDO DONE" else "MARK DONE")
                            }
                        }

                        // Completion Overlay
                        if (completedExerciseIndices.contains(page)) {
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
