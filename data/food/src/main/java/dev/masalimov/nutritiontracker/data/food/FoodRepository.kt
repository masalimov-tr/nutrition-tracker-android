package dev.masalimov.nutritiontracker.data.food

interface FoodRepository {

    suspend fun getAllFood(): List<Food>
    suspend fun getFoodById(foodId: Long): Food
}