package com.example.gymdietplanner.data

data class Exercise(val name: String, val equipment: String)

data class MuscleGroup(val name: String, val exercises: List<Exercise>)
