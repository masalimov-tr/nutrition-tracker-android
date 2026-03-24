package dev.masalimov.nutritiontracker.data.diary

import dev.masalimov.nutritiontracker.core.database.food.FoodEntity
import dev.masalimov.nutritiontracker.domain.Food
import dev.masalimov.nutritiontracker.domain.FoodId

internal fun FoodEntity.toFood(): Food {
    return Food(
        id = FoodId(uid),
        name = name,
        caloriesPer100g = caloriesPer100g,
        proteinPer100g = proteinPer100g,
        fatPer100g = fatPer100g,
        carbsPer100g = carbsPer100g,
    )
}