package dev.masalimov.nutritiontracker.core.database.food

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {

    @Query("SELECT COUNT(*) FROM FoodEntity")
    suspend fun countAll(): Long
    @Insert
    suspend fun insert(foodEntity: FoodEntity): Long

    @Insert
    suspend fun insertAll(foods: List<FoodEntity>): List<Long>
    @Query("SELECT * FROM FoodEntity")
    suspend fun getAll(): List<FoodEntity>

    @Query("SELECT * FROM FoodEntity WHERE uid = :id")
    suspend fun getById(id: Long): FoodEntity

    @Query("SELECT * FROM FoodEntity")
    fun getAllStream(): Flow<List<FoodEntity>>

    @Query("DELETE FROM FoodEntity WHERE uid = :foodId")
    suspend fun deleteById(foodId: Long)
}