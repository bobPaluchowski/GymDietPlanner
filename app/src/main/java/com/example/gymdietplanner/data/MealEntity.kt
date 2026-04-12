package com.example.gymdietplanner.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meals")
data class MealEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val time: String,
    val days: List<String> = emptyList(),
    val ingredients: List<String>,
    val instructions: String
)
