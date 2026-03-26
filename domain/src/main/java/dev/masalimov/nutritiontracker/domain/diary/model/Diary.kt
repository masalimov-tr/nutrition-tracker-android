package dev.masalimov.nutritiontracker.domain.diary.model

import dev.masalimov.nutritiontracker.domain.food.Food


@JvmInline
value class DiaryId(val id: Long)

data class EatenFood(
    val quantityGram: Double,
    val food: Food
) {
    val calories: Int
        get() = (quantityGram / 100 * food.caloriesPer100g).toInt()
}

data class Diary(
    val id: DiaryId,
    val date: DiaryDate,
    val eatenFood: List<EatenFood>,
    val goalCaloriesPerDay: Int,
) {
    val caloriesEaten: Int
        get() = eatenFood.sumOf { it.calories }
}