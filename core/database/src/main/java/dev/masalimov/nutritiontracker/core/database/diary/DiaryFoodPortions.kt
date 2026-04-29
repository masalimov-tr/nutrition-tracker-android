package dev.masalimov.nutritiontracker.core.database.diary

import androidx.room.Embedded
import androidx.room.Relation
import dev.masalimov.nutritiontracker.core.database.food.FoodEntity

// A food item with its quantity as it appears in a diary entry
data class DiaryFoodPortion(
    @Embedded val link: DiaryFoodPortionCrossRef,
    @Relation(
        parentColumn = "foodId",
        entityColumn = "uid"
    )
    val food: FoodEntity
)

// A complete diary day: the diary slot + all food portions logged for it
data class DiaryWithFoodPortions(
    @Embedded val diaryEntry: DiaryEntity,
    @Relation(
        parentColumn = "uid",
        entity = DiaryFoodPortionCrossRef::class,
        entityColumn = "diaryEntryId"
    )
    val items: List<DiaryFoodPortion>
)