package com.example.gymdietplanner.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weights")
data class WeightEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val weight: String,
    val date: String
)
