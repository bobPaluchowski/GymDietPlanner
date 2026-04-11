package com.example.gymdietplanner.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

import java.util.UUID

data class WorkoutSet(
    val id: String = UUID.randomUUID().toString(),
    var weight: String = "",
    var reps: String = "",
    var isCompleted: Boolean = false
)

data class RoutineExercise(
    val id: String = UUID.randomUUID().toString(),
    val exercise: Exercise,
    val sets: List<WorkoutSet> = listOf(WorkoutSet()) // Initialize with 1 empty set
)

@Entity(tableName = "routines")
data class RoutineEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val days: List<String>,
    val repeatWeekly: Boolean,
    val exercises: List<RoutineExercise>
)

class RoutineTypeConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return gson.toJson(value ?: emptyList<String>())
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }

    @TypeConverter
    fun fromRoutineExerciseList(value: List<RoutineExercise>?): String {
        return gson.toJson(value ?: emptyList<RoutineExercise>())
    }

    @TypeConverter
    fun toRoutineExerciseList(value: String): List<RoutineExercise> {
        return try {
            val listType = object : TypeToken<List<RoutineExercise>>() {}.type
            val list = gson.fromJson<List<RoutineExercise>>(value, listType) ?: emptyList()
            
            // Check if any exercise field is null (sign of legacy Exercise format)
            if (list.any { it.exercise == null }) {
                // Try parsing as legacy List<Exercise>
                val legacyType = object : TypeToken<List<com.example.gymdietplanner.data.Exercise>>() {}.type
                val legacyList = gson.fromJson<List<com.example.gymdietplanner.data.Exercise>>(value, legacyType) ?: emptyList()
                legacyList.map { RoutineExercise(exercise = it) }
            } else {
                list
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
