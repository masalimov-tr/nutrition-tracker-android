package dev.masalimov.nutritiontracker.core.database.diary

import androidx.room.Embedded
import androidx.room.Relation
import dev.masalimov.nutritiontracker.core.database.food.FoodEntity

data class DiaryEntryWithFood(
    @Embedded val diaryEntry: DiaryEntity,
    @Relation(
        parentColumn = "foodId",
        entityColumn = "uid"
    )
    val foodEntity: FoodEntity
)