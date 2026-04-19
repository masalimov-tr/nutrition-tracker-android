package dev.masalimov.nutritiontracker.core.database.food

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class FoodEntity(
    @PrimaryKey(autoGenerate = true) val uid: Long = 0L,

    val name: String,
    val caloriesPer100g: Double,
    val proteinPer100g: Double,
    val fatPer100g: Double,
    val carbsPer100g: Double,
    // Soft-delete flag: false means removed from the saved list but kept for diary history
    val isSaved: Boolean = true,
)

val exampleFoodEntity = FoodEntity(
    uid = 1L,
    name = "Apple",
    caloriesPer100g = 52.0,
    proteinPer100g = 0.3,
    fatPer100g = 0.2,
    carbsPer100g = 14.0,
    isSaved = true
)