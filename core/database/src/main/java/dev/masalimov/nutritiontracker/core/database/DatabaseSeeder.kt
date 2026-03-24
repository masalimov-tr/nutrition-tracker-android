package dev.masalimov.nutritiontracker.core.database

import androidx.room.withTransaction
import dev.masalimov.nutritiontracker.core.database.diary.DiaryDao
import dev.masalimov.nutritiontracker.core.database.diary.DiaryEntity
import dev.masalimov.nutritiontracker.core.database.food.FoodDao
import dev.masalimov.nutritiontracker.core.database.food.FoodEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class DatabaseSeeder @Inject constructor(
    private val db: NutritionAppDatabase,
    private val foodDao: FoodDao,
    private val diaryDao: DiaryDao,
) {


    suspend fun seedIfEmpty() = withContext(Dispatchers.IO) {
        db.withTransaction {
            if (foodDao.countAll() > 0) return@withTransaction
            val foods = PrepopulationData.prepopulatedFoods
            val insertedFoodIds = foodDao.insertAll(foods)

            val today = today()
            val yesterday = today().minus(1, DateTimeUnit.DAY)
            val tomorrow = today().plus(1, DateTimeUnit.DAY)

            for (i in 0..3) {
                diaryDao.insert(
                    foods[i].toDiaryEntity(insertedFoodIds[i], yesterday)
                )
            }
            for (i in 2..5) {
                diaryDao.insert(
                    foods[i].toDiaryEntity(insertedFoodIds[i], today)
                )
            }
            for (i in 6..7) {
                diaryDao.insert(
                    foods[i].toDiaryEntity(insertedFoodIds[i], tomorrow)
                )
            }
        }
    }
}

private fun today() = Clock.System.todayIn(TimeZone.currentSystemDefault())

private fun FoodEntity.toDiaryEntity(foodId: Long, date: LocalDate): DiaryEntity {
    return DiaryEntity(
        foodId = foodId,
        quantityGrams = 150.0,
        dateEpochDay = date.toEpochDays(),
        foodNameAtLog = name,
        caloriesPer100gAtLog = caloriesPer100g,
        proteinPer100gAtLog = proteinPer100g,
        fatPer100gAtLog = fatPer100g,
        carbsPer100gAtLog = carbsPer100g,
    )
}
