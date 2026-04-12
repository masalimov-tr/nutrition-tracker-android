package dev.masalimov.nutritiontracker.domain.food

data class Food(
    val id: FoodId,
    val name: String,
    val caloriesPer100g: Double,
    val proteinPer100g: Double,
    val fatPer100g: Double,
    val carbsPer100g: Double,
)

val exampleFood = Food(
    FoodId(1),
    name = "Apple",
    caloriesPer100g = 100.0,
    proteinPer100g = 10.0,
    fatPer100g = 0.5,
    carbsPer100g = 50.0,
)