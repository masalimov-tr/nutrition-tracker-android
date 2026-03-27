package dev.masalimov.nutritiontracker.domain.diary

import dev.masalimov.nutritiontracker.domain.diary.model.Diary
import dev.masalimov.nutritiontracker.domain.diary.model.DiaryDate
import kotlinx.coroutines.flow.Flow

interface DiaryRepository {
    fun getDiaryByDateFlow(date: DiaryDate) : Flow<Diary?>

    fun getAllDiaryEntriesFlow(): Flow<List<Diary>>

    suspend fun addFoodToDiary(foodId: Long, date: DiaryDate, quantityGrams: Double)
}