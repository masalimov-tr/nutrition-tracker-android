package dev.masalimov.nutritiontracker.core.database.diary

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import dev.masalimov.nutritiontracker.core.database.food.FoodEntity

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = FoodEntity::class,
            parentColumns = ["uid"],
            childColumns = ["foodId"],
        )
    ],
    indices = [
        Index(value = ["foodId"]) // still useful for joins while not null
    ]
)
class DiaryEntity(
    @PrimaryKey(autoGenerate = true) val uid: Long = 0L,

    val foodId: Long,

    // how many grams of the food were consumed
    val quantityGrams: Double,

    // when the food was consumed
    val dateEpochDay: Int,

    // SNAPSHOT of the food at log time
    val foodNameAtLog: String,
    val caloriesPer100gAtLog: Double,
    val proteinPer100gAtLog: Double,
    val fatPer100gAtLog: Double,
    val carbsPer100gAtLog: Double,
)