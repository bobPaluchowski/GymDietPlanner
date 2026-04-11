package com.example.gymdietplanner.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightDao {
    @Query("SELECT * FROM weights ORDER BY id DESC")
    fun getAllWeights(): Flow<List<WeightEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeight(weight: WeightEntity)

    @Query("DELETE FROM weights WHERE id = :weightId")
    suspend fun deleteWeight(weightId: Int)
}
