package dev.masalimov.nutritiontracker.domain.diary.model

import dev.masalimov.nutritiontracker.domain.food.Food

data class DiaryEntriesForDate(
    val date: DiaryDate,
    val diaryEntitiesPerDay: List<Diary>,
    val suggestedFood: List<Food>,
    val goalCaloriesPerDay: Int,
)