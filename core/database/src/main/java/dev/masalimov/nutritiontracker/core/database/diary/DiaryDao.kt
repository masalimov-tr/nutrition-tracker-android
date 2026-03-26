package dev.masalimov.nutritiontracker.core.database.diary

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(diaryEntity: DiaryEntity): Long

    @Transaction
    @Query("SELECT * FROM DiaryEntity WHERE dateEpochDay = :epochDay")
    suspend fun getEntriesForDate(epochDay: Int): List<DiaryEntryWithFood>

    @Transaction
    @Query("SELECT * FROM DiaryEntity WHERE dateEpochDay = :epochDay")
    fun getEntriesForDateFlow(epochDay: Int): Flow<List<DiaryEntryWithFood>>

    @Query("DELETE FROM DiaryEntity WHERE uid = :entryId")
    suspend fun deleteEntry(entryId: Long)
}