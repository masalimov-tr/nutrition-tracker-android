package dev.masalimov.nutritiontracker.domain.food

import dev.masalimov.nutritiontracker.domain.Food
import dev.masalimov.nutritiontracker.domain.FoodId

interface FoodRepository {

    suspend fun getAllFood(): List<Food>
    suspend fun getFoodById(foodId: FoodId): Food
}