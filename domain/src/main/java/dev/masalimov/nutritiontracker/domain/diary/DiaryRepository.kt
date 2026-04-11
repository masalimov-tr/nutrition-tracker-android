package dev.masalimov.nutritiontracker.domain.diary

import dev.masalimov.nutritiontracker.domain.diary.model.DiaryDate
import dev.masalimov.nutritiontracker.domain.diary.model.DiaryEntryForDate
import kotlinx.coroutines.flow.Flow

interface DiaryRepository {
    fun getDiaryByDateFlow(date: DiaryDate) : Flow<DiaryEntryForDate?>

    fun getAllDiaryEntriesFlow(): Flow<List<DiaryEntryForDate>>

    suspend fun addFoodToDiary(foodId: Long, date: DiaryDate, quantityGrams: Double)
}