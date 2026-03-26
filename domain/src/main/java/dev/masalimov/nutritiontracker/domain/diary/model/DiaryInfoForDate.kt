package dev.masalimov.nutritiontracker.domain.diary.model

import dev.masalimov.nutritiontracker.domain.food.Food

data class DiaryInfoForDate(
    val diary: Diary?,
    val suggestedFood: List<Food>,
) {
    companion object {
        val EMPTY = DiaryInfoForDate(null, emptyList())
    }
}