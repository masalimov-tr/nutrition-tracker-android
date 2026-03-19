package dev.masalimov.nutritiontracker.data.food

internal class FakeFoodRepository : FoodRepository {

    private val listOfFoods = listOf(
        Food(
            id = 1,
            name = "Apple",
            caloriesPer100g = 52.0,
            proteinPer100g = 0.3,
            fatPer100g = 0.2,
            carbsPer100g = 14.0,
        ),
        Food(
            id = 2,
            name = "Banana",
            caloriesPer100g = 96.0,
            proteinPer100g = 1.3,
            fatPer100g = 0.3,
            carbsPer100g = 27.0,
        ),
        Food(
            id = 3,
            name = "Chicken Breast (roasted)",
            caloriesPer100g = 165.0,
            proteinPer100g = 31.0,
            fatPer100g = 3.6,
            carbsPer100g = 0.0,
        ),
        Food(
            id = 4,
            name = "White Rice (cooked)",
            caloriesPer100g = 130.0,
            proteinPer100g = 2.4,
            fatPer100g = 0.3,
            carbsPer100g = 28.0,
        ),
        Food(
            id = 5,
            name = "Whole Milk",
            caloriesPer100g = 61.0,
            proteinPer100g = 3.2,
            fatPer100g = 3.3,
            carbsPer100g = 4.8,
        ),
        Food(
            id = 6,
            name = "Almonds",
            caloriesPer100g = 579.0,
            proteinPer100g = 21.2,
            fatPer100g = 49.9,
            carbsPer100g = 21.6,
        ),
        Food(
            id = 7,
            name = "Broccoli (raw)",
            caloriesPer100g = 34.0,
            proteinPer100g = 2.8,
            fatPer100g = 0.4,
            carbsPer100g = 6.6,
        ),
        Food(
            id = 8,
            name = "Olive Oil",
            caloriesPer100g = 884.0,
            proteinPer100g = 0.0,
            fatPer100g = 100.0,
            carbsPer100g = 0.0,
        ),
        Food(
            id = 9,
            name = "Egg (boiled)",
            caloriesPer100g = 155.0,
            proteinPer100g = 12.6,
            fatPer100g = 10.6,
            carbsPer100g = 1.1,
        ),
        Food(
            id = 10,
            name = "Salmon (grilled)",
            caloriesPer100g = 208.0,
            proteinPer100g = 20.4,
            fatPer100g = 13.4,
            carbsPer100g = 0.0,
        ),
    )

    override suspend fun getAllFood(): List<Food> = listOfFoods
}