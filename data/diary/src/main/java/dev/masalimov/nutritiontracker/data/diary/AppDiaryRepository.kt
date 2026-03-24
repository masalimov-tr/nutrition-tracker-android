package dev.masalimov.nutritiontracker.data.diary

import dev.masalimov.nutritiontracker.core.database.diary.DiaryDao
import dev.masalimov.nutritiontracker.domain.DiaryDate
import dev.masalimov.nutritiontracker.domain.Food
import dev.masalimov.nutritiontracker.domain.FoodId
import dev.masalimov.nutritiontracker.domain.diary.DiaryRepository
import javax.inject.Inject

fun DiaryDate.toEpochDay(): Int {
    return date.toEpochDays()
}

internal class AppDiaryRepository @Inject constructor(
    private val diaryDao: DiaryDao,
) : DiaryRepository {

    override suspend fun getFoodByDate(date: DiaryDate): List<Food> {
        val foodList = diaryDao.getEntriesForDate(date.toEpochDay())
        return foodList.map {
            Food(
                id = FoodId(it.foodEntity.uid),
                name = it.foodEntity.name,
                caloriesPer100g = it.foodEntity.caloriesPer100g,
                proteinPer100g = it.foodEntity.proteinPer100g,
                fatPer100g = it.foodEntity.fatPer100g,
                carbsPer100g = it.foodEntity.carbsPer100g,
            )
        }
    }
}