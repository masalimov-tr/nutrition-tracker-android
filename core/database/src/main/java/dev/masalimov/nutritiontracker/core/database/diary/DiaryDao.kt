package dev.masalimov.nutritiontracker.core.database.diary


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(diaryEntity: DiaryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrossRef(crossRef: DiaryFoodPortionCrossRef)

    @Transaction
    @Query("SELECT * FROM DiaryEntity WHERE dateEpochDay = :epochDay LIMIT 1")
    suspend fun getDiaryForDate(epochDay: Int): DiaryWithFoodPortions?

    @Transaction
    @Query("SELECT * FROM DiaryEntity WHERE dateEpochDay = :epochDay LIMIT 1")
    fun getDiaryForDateFlow(epochDay: Int): Flow<DiaryWithFoodPortions?>

    @Query("DELETE FROM DiaryEntity WHERE uid = :entryId")
    suspend fun deleteDiary(entryId: Long)

    // Wrap add operation into a single DB transaction
    @Transaction
    suspend fun addFoodToDiaryTx(
        epochDay: Int,
        goalCaloriesPerDay: Int,
        foodId: Long,
        quantityGrams: Double,
    ) {
        val existing = getDiaryForDate(epochDay)
        val diaryEntryId = existing?.diaryEntry?.uid ?: insert(
            DiaryEntity(
                dateEpochDay = epochDay,
                goalCaloriesPerDay = goalCaloriesPerDay,
            )
        )
        insertCrossRef(
            DiaryFoodPortionCrossRef(
                diaryEntryId = diaryEntryId,
                foodId = foodId,
                quantityGrams = quantityGrams,
            )
        )
    }
}