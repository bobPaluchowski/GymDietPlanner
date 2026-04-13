package com.example.gymdietplanner.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun getExerciseIcon(iconName: String?): ImageVector {
    return when (iconName) {
        "VerticalAlignTop" -> Icons.Default.VerticalAlignTop
        "VerticalAlignBottom" -> Icons.Default.VerticalAlignBottom
        "OpenInFull" -> Icons.Default.OpenInFull
        "CompareArrows" -> Icons.Default.CompareArrows
        "KeyboardDoubleArrowDown" -> Icons.Default.KeyboardDoubleArrowDown
        "KeyboardDoubleArrowUp" -> Icons.Default.KeyboardDoubleArrowUp
        "HorizontalRule" -> Icons.Default.HorizontalRule
        "ArrowDownward" -> Icons.Default.ArrowDownward
        "Compress" -> Icons.Default.Compress
        "PanTool" -> Icons.Default.PanTool
        "UnfoldMore" -> Icons.Default.UnfoldMore
        "DirectionsRun" -> Icons.Default.DirectionsRun
        "DirectionsWalk" -> Icons.Default.DirectionsWalk
        "Chair" -> Icons.Default.Chair
        "AirlineSeatFlat" -> Icons.Default.AirlineSeatFlat
        "DoubleArrow" -> Icons.Default.DoubleArrow
        "Height" -> Icons.Default.Height
        "History" -> Icons.Default.History
        "Download" -> Icons.Default.Download
        "Loop" -> Icons.Default.Loop
        "SportsGymnastics" -> Icons.Default.SportsGymnastics
        "AccessibilityNew" -> Icons.Default.AccessibilityNew
        "Straighten" -> Icons.Default.Straighten
        else -> Icons.Default.FitnessCenter
    }
}
