package dev.masalimov.nutritiontracker.core.database.diary

import androidx.room.Embedded
import androidx.room.Relation
import dev.masalimov.nutritiontracker.core.database.food.FoodEntity

// Link + Food pair for one item in a diary entry
data class DiaryFoodLink(
    @Embedded val link: DiaryEntryFoodCrossRef, // contains quantityGrams
    @Relation(
        parentColumn = "foodId",
        entityColumn = "uid"
    )
    val food: FoodEntity
)

// Wrapper for an entry containing multiple link+food items
data class DiaryEntryWithFoods(
    @Embedded val diaryEntry: DiaryEntity,
    @Relation(
        parentColumn = "uid",
        entity = DiaryEntryFoodCrossRef::class,
        entityColumn = "diaryEntryId"
    )
    val items: List<DiaryFoodLink>
)