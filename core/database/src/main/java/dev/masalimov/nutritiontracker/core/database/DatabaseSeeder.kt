package dev.masalimov.nutritiontracker.core.database

import androidx.room.withTransaction
import dev.masalimov.nutritiontracker.core.database.diary.DiaryDao
import dev.masalimov.nutritiontracker.core.database.diary.DiaryEntity
import dev.masalimov.nutritiontracker.core.database.diary.DiaryFoodPortionCrossRef
import dev.masalimov.nutritiontracker.core.database.food.FoodDao
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
internal class DatabaseSeeder @Inject constructor(
    private val db: NutritionAppDatabase,
    private val foodDao: FoodDao,
    private val diaryDao: DiaryDao,
) {


    internal suspend fun seedIfEmpty() = withContext(Dispatchers.IO) {
        db.withTransaction {
            if (foodDao.countAll() > 0) return@withTransaction
            val foods = PrepopulationData.prepopulatedFoods
            val insertedFoodIds = foodDao.insertAll(foods)

            val today = today()
            val yesterday = today.minus(1, DateTimeUnit.DAY)
            val tomorrow = today.plus(1, DateTimeUnit.DAY)

            fun diary(date: LocalDate, goalCalories: Int) = DiaryEntity(
                dateEpochDay = date.toEpochDays(),
                goalCaloriesPerDay = goalCalories,
            )

            // Example quantities for demo
            val defaultQty = 150.0

            // Seed yesterday with foods[0..3]
            run {
                val diaryId = diaryDao.insert(diary(yesterday, 2000))
                for (i in 0..3) {
                    diaryDao.insertCrossRef(
                        DiaryFoodPortionCrossRef(
                            diaryEntryId = diaryId,
                            foodId = insertedFoodIds[i],
                            quantityGrams = defaultQty,
                        )
                    )
                }
            }

            // Seed today with foods[2..5]
            run {
                val diaryId = diaryDao.insert(diary(today, 1500))
                for (i in 2..5) {
                    diaryDao.insertCrossRef(
                        DiaryFoodPortionCrossRef(
                            diaryEntryId = diaryId,
                            foodId = insertedFoodIds[i],
                            quantityGrams = defaultQty,
                        )
                    )
                }
            }

            // Seed tomorrow with foods[6..7]
            run {
                val diaryId = diaryDao.insert(diary(tomorrow, 1700))
                for (i in 6..7) {
                    diaryDao.insertCrossRef(
                        DiaryFoodPortionCrossRef(
                            diaryEntryId = diaryId,
                            foodId = insertedFoodIds[i],
                            quantityGrams = defaultQty,
                        )
                    )
                }
            }
        }
    }
}

private fun today() = Clock.System.todayIn(TimeZone.currentSystemDefault())
