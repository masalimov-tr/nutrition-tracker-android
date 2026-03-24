package dev.masalimov.nutritiontracker.domain.diary

import dev.masalimov.nutritiontracker.domain.Diary
import dev.masalimov.nutritiontracker.domain.DiaryDate

interface DiaryRepository {
    suspend fun getDiaryEntriesByDate(date: DiaryDate = DiaryDate.Companion.today()): List<Diary>
}