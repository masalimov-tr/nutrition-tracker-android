package dev.masalimov.nutritiontracker.domain.diary

import dev.masalimov.nutritiontracker.domain.DiaryDate
import dev.masalimov.nutritiontracker.domain.Food

interface DiaryRepository {
    suspend fun getFoodByDate(date: DiaryDate = DiaryDate.Companion.today()): List<Food>
}