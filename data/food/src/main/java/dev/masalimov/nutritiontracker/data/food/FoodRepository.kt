package dev.masalimov.nutritiontracker.data.food

interface FoodRepository {

    suspend fun getAllFood(): List<Food>
}