package dev.masalimov.nutritiontracker.domain.food

import dev.masalimov.nutritiontracker.domain.Food
import dev.masalimov.nutritiontracker.domain.FoodId
import kotlinx.coroutines.flow.Flow

interface FoodRepository {

    suspend fun getAllFood(): List<Food>

    fun getAllFoodStream(): Flow<List<Food>>

    suspend fun getFoodById(foodId: FoodId): Food

    suspend fun getSuggestedFood(eatenFood: List<Food>): List<Food>
}