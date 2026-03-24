package dev.masalimov.nutritiontracker.data.diary

import dev.masalimov.nutritiontracker.core.common.ApplicationScope
import dev.masalimov.nutritiontracker.core.database.diary.DiaryDao
import dev.masalimov.nutritiontracker.core.database.diary.DiaryEntity
import dev.masalimov.nutritiontracker.core.database.food.FoodDao
import dev.masalimov.nutritiontracker.core.database.food.FoodEntity
import dev.masalimov.nutritiontracker.domain.DiaryDate
import dev.masalimov.nutritiontracker.domain.Food
import dev.masalimov.nutritiontracker.domain.FoodId
import dev.masalimov.nutritiontracker.domain.diary.DiaryRepository
import dev.masalimov.nutritiontracker.domain.food.FoodRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

fun DiaryDate.toEpochDay(): Int {
    return date.toEpochDays()
}

internal class FakeDiaryRepository @Inject constructor(
    private val diaryDao: DiaryDao,
    private val foodDao: FoodDao,
    private val foodRepository: FoodRepository,
    @ApplicationScope private val appScope: CoroutineScope
) : DiaryRepository {

    init {
        appScope.launch {
            val foods = foodRepository.getAllFood().take(3)
            foods.forEach {
                foodDao.insert(it.toFoodEntity())
            }

            with(DiaryDate.today().previousDay()) {
                diaryDao.insert(foods[0].toDiaryEntity(this))
            }
            with(DiaryDate.today()) {
                diaryDao.insert(foods[0].toDiaryEntity(this))
                diaryDao.insert(foods[1].toDiaryEntity(this))
            }
            with(DiaryDate.today().nextDay()) {
                diaryDao.insert(foods[2].toDiaryEntity(this))
            }
        }
    }

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

private fun Food.toDiaryEntity(date: DiaryDate): DiaryEntity {
    return DiaryEntity(
        foodId = id.id,
        quantityGrams = 150.0,
        dateEpochDay = date.toEpochDay(),
        foodNameAtLog = name,
        caloriesPer100gAtLog = caloriesPer100g,
        proteinPer100gAtLog = proteinPer100g,
        fatPer100gAtLog = fatPer100g,
        carbsPer100gAtLog = carbsPer100g,
    )
}

private fun Food.toFoodEntity(): FoodEntity {
    return FoodEntity(
        name = name,
        caloriesPer100g = caloriesPer100g,
        proteinPer100g = proteinPer100g,
        fatPer100g = fatPer100g,
        carbsPer100g = carbsPer100g,
    )
}
