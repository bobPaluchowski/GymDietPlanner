package com.example.gymdietplanner.data

data class Exercise(
    val name: String,
    val equipment: String,
    val category: String = "",
    val iconName: String? = null
)

data class MuscleGroup(val name: String, val exercises: List<Exercise>)
