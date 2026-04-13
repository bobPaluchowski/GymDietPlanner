package com.example.gymdietplanner.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercises ORDER BY name ASC")
    fun getAllExercises(): Flow<List<ExerciseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: ExerciseEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertExercises(exercises: List<ExerciseEntity>)

    @Query("DELETE FROM exercises WHERE exerciseId = :exerciseId")
    suspend fun deleteExercise(exerciseId: String)
    
    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun getExerciseCount(): Int
}
