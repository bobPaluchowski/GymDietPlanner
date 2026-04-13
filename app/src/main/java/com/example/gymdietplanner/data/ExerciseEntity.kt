package com.example.gymdietplanner.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey val exerciseId: String,
    val name: String,
    val targetMuscles: List<String> = emptyList(),
    val secondaryMuscles: List<String> = emptyList(),
    val muscles: List<String> = emptyList(),
    val equipments: List<String> = emptyList(),
    val imageUrls: List<String> = emptyList(),
    val videoUrls: List<String> = emptyList(),
    val instructions: List<String> = emptyList(),
    val isCustom: Boolean = true
)
