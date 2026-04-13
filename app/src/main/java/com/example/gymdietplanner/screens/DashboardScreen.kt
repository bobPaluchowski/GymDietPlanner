package com.example.gymdietplanner.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.gymdietplanner.data.MealEntity
import com.example.gymdietplanner.data.RoutineEntity
import com.example.gymdietplanner.data.WeightEntity
import java.time.DayOfWeek
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    routines: List<RoutineEntity>,
    meals: List<MealEntity>,
    weights: List<WeightEntity>,
    isMetric: Boolean,
    onNavigateToSettings: () -> Unit,
    onRoutineClick: (Int) -> Unit
) {
    val today = LocalDate.now()
    val dayMapping = mapOf(
        DayOfWeek.MONDAY to "M",
        DayOfWeek.TUESDAY to "T",
        DayOfWeek.WEDNESDAY to "W",
        DayOfWeek.THURSDAY to "Th",
        DayOfWeek.FRIDAY to "F",
        DayOfWeek.SATURDAY to "S",
        DayOfWeek.SUNDAY to "Su"
    )
    val todayAbbr = dayMapping[today.dayOfWeek] ?: ""

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 24.dp, start = 16.dp, end = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header with Settings
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 48.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Dashboard",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = onNavigateToSettings) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                         tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Weekly Calendar
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Training Schedule",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            val days = listOf("M", "T", "W", "Th", "F", "S", "Su")
            val scheduledDays = routines.flatMap { it.days }.toSet()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                days.forEach { day ->
                    val isToday = day == todayAbbr
                    val hasRoutine = scheduledDays.contains(day)
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            shape = CircleShape,
                            color = when {
                                isToday -> MaterialTheme.colorScheme.primary
                                hasRoutine -> MaterialTheme.colorScheme.primaryContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = day,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                                    color = when {
                                        isToday -> MaterialTheme.colorScheme.onPrimary
                                        hasRoutine -> MaterialTheme.colorScheme.onPrimaryContainer
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Today's Focus
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = "Today's Focus",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Routine Card
            val todaysRoutines = routines.filter { it.days.contains(todayAbbr) }
            DashboardSummaryCard(
                title = "Routine",
                content = if (todaysRoutines.isEmpty()) "Rest Day" else todaysRoutines.joinToString(", ") { it.name },
                icon = Icons.Default.FitnessCenter,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                onClick = todaysRoutines.firstOrNull()?.let { routine ->
                    { onRoutineClick(routine.id) }
                }
            )

            // Meals Card
            val todaysMeals = meals.filter { it.days.contains(todayAbbr) }
            DashboardSummaryCard(
                title = "Nutrition",
                content = if (todaysMeals.isEmpty()) "No meals planned" else "${todaysMeals.size} Meals Planned",
                icon = Icons.Default.Restaurant,
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            )

            // Weight Card
            val latestWeight = weights.firstOrNull()
            val unitSuffix = if (isMetric) "kg" else "lbs"
            DashboardSummaryCard(
                title = "Latest Weight",
                content = latestWeight?.let { "${it.weight} $unitSuffix - ${it.date}" } ?: "Not logged yet",
                icon = Icons.Default.Scale,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun DashboardSummaryCard(
    title: String,
    content: String,
    icon: ImageVector,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = MaterialTheme.shapes.large
    ) {
        Surface(
            color = androidx.compose.ui.graphics.Color.Transparent,
            modifier = Modifier
                .fillMaxWidth()
                .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
        ) {
            Row(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = contentColor.copy(alpha = 0.15f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = contentColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelLarge,
                        color = contentColor.copy(alpha = 0.7f)
                    )
                    Text(
                        text = content,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                }

                if (onClick != null) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Navigate",
                        tint = contentColor.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}
