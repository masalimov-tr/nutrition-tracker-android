package dev.masalimov.nutritiontracker.domain.diary.model

import dev.masalimov.nutritiontracker.domain.food.Food
import dev.masalimov.nutritiontracker.domain.food.exampleFood


@JvmInline
value class DiaryId(val id: Long)

data class EatenFood(
    val quantityGram: Double,
    val food: Food
) {
    val calories: Int
        get() = (quantityGram / 100 * food.caloriesPer100g).toInt()
}

val exampleEatenFood = EatenFood(
    quantityGram = 150.0,
    food = exampleFood
)

data class DiaryEntryForDate(
    val id: DiaryId,
    val date: DiaryDate,
    val eatenFood: List<EatenFood>,
    val goalCaloriesPerDay: Int,
) {
    val caloriesEaten: Int
        get() = eatenFood.sumOf { it.calories }
}