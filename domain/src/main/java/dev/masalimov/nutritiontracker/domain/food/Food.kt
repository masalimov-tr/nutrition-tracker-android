package dev.masalimov.nutritiontracker.domain.food

data class Food(
    val id: FoodId,
    val name: String,
    val caloriesPer100g: Double,
    val proteinPer100g: Double,
    val fatPer100g: Double,
    val carbsPer100g: Double,
)