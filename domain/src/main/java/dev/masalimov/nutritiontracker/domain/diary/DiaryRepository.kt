package dev.masalimov.nutritiontracker.domain.diary

import dev.masalimov.nutritiontracker.domain.diary.model.Diary
import dev.masalimov.nutritiontracker.domain.diary.model.DiaryDate
import kotlinx.coroutines.flow.Flow

interface DiaryRepository {
    suspend fun getDiaryEntriesByDate(date: DiaryDate): List<Diary>

    fun getDiaryEntriesByDateFlow(date: DiaryDate) : Flow<List<Diary>>
}