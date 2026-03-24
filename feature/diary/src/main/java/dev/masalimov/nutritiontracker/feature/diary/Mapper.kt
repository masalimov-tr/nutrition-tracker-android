package dev.masalimov.nutritiontracker.feature.diary

import dev.masalimov.nutritiontracker.domain.EatenFood
import dev.masalimov.nutritiontracker.domain.Food

internal fun EatenFood.toEatenFoodUiModel() = EatenFoodUiModel(
    name = this.food.name,
    quantityGram = this.quantityGram,
    caloriesEaten = this.calories,
    caloriesPer100g = this.food.caloriesPer100g.toInt(),
)

internal fun Food.toSuggestedFoodUiModel() = SuggestedFoodUiModel(
    name = this.name,
    caloriesPer100g = this.caloriesPer100g,
)
