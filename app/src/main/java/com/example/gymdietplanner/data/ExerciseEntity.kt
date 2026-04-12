package com.example.gymdietplanner.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val equipment: String,
    val category: String,
    val isCustom: Boolean = false
)
