package dev.masalimov.nutritiontracker.data.food

import dev.masalimov.nutritiontracker.core.database.food.FoodEntity
import dev.masalimov.nutritiontracker.domain.food.Food
import dev.masalimov.nutritiontracker.domain.food.FoodId

internal fun FoodApiModel.toFood(): Food {
    return Food(
        id = FoodId(id),
        name = name,
        caloriesPer100g = caloriesPer100g,
        proteinPer100g = 0.0,
        fatPer100g = 0.0,
        carbsPer100g = 0.0,
    )

}

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
