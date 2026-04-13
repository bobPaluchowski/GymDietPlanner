package com.example.gymdietplanner.data

data class Exercise(
    val exerciseId: String,
    val name: String,
    val targetMuscles: List<String> = emptyList(),
    val secondaryMuscles: List<String> = emptyList(),
    val muscles: List<String> = emptyList(),
    val equipments: List<String> = emptyList(),
    val imageUrls: List<String> = emptyList(),
    val videoUrls: List<String> = emptyList(),
    val instructions: List<String> = emptyList()
)

data class MuscleGroup(
    val name: String,
    val exercises: List<Exercise>
)
