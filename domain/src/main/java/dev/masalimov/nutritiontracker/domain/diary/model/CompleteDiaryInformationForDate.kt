package dev.masalimov.nutritiontracker.domain.diary.model

import dev.masalimov.nutritiontracker.domain.food.Food

data class CompleteDiaryInformationForDate(
    val diaryEntryForDate: DiaryEntryForDate?,
    val suggestedFood: List<Food>,
) {
    companion object Companion {
        val EMPTY = CompleteDiaryInformationForDate(null, emptyList())
    }
}