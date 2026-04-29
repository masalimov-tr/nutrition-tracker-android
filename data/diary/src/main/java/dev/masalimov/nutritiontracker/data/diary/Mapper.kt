package dev.masalimov.nutritiontracker.data.diary

import dev.masalimov.nutritiontracker.core.database.diary.DiaryWithFoodPortions
import dev.masalimov.nutritiontracker.core.database.food.FoodEntity
import dev.masalimov.nutritiontracker.domain.diary.model.DiaryDate
import dev.masalimov.nutritiontracker.domain.diary.model.DiaryEntryForDate
import dev.masalimov.nutritiontracker.domain.diary.model.DiaryId
import dev.masalimov.nutritiontracker.domain.diary.model.EatenFood
import dev.masalimov.nutritiontracker.domain.food.Food
import dev.masalimov.nutritiontracker.domain.food.FoodId

internal fun DiaryWithFoodPortions.toDiary(date: DiaryDate): DiaryEntryForDate {
    return DiaryEntryForDate(
        id = DiaryId(this.diaryEntry.uid),
        date = date,
        eatenFood =  items.map {
            EatenFood(
                it.link.quantityGrams,
                it.food.toFood()
            )
        },
        goalCaloriesPerDay = diaryEntry.goalCaloriesPerDay
    )
}

private fun FoodEntity.toFood(): Food {
    return Food(
        id = FoodId(uid),
        name = name,
        caloriesPer100g = caloriesPer100g,
        proteinPer100g = proteinPer100g,
        fatPer100g = fatPer100g,
        carbsPer100g = carbsPer100g,
    )
}

internal fun DiaryDate.toEpochDay(): Int {
    return date.toEpochDays()
}
