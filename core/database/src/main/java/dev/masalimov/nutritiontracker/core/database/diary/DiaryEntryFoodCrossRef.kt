package dev.masalimov.nutritiontracker.core.database.diary

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import dev.masalimov.nutritiontracker.core.database.food.FoodEntity

@Entity(
    primaryKeys = ["diaryEntryId", "foodId"],
    foreignKeys = [
        ForeignKey(
            entity = DiaryEntity::class,
            parentColumns = ["uid"],
            childColumns = ["diaryEntryId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = FoodEntity::class,
            parentColumns = ["uid"],
            childColumns = ["foodId"],
            onDelete = ForeignKey.NO_ACTION
        )
    ],
    indices = [
        Index(value = ["diaryEntryId"]),
        Index(value = ["foodId"]) // for joins
    ],
)
data class DiaryEntryFoodCrossRef(
    val diaryEntryId: Long,
    val foodId: Long,

    val quantityGrams: Double,
)