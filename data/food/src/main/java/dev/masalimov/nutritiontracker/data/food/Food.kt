package dev.masalimov.nutritiontracker.data.food

data class Food(
    val id: Long,
    val name: String,
    val caloriesPer100g: Double,
    val proteinPer100g: Double,
    val fatPer100g: Double,
    val carbsPer100g: Double,
)