package dev.masalimov.nutritiontracker.core.database.food

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class FoodEntity(
    @PrimaryKey(autoGenerate = true) val uid: Long = 0L,

    val name: String,
    val caloriesPer100g: Double,
    val proteinPer100g: Double,
    val fatPer100g: Double,
    val carbsPer100g: Double,
)