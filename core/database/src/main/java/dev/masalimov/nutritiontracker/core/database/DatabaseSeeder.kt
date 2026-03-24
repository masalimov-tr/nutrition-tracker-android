package dev.masalimov.nutritiontracker.core.database

import androidx.room.withTransaction
import dev.masalimov.nutritiontracker.core.database.food.FoodDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class DatabaseSeeder @Inject constructor(
    private val db: NutritionAppDatabase,
    private val foodDao: FoodDao,
) {


    suspend fun seedIfEmpty() = withContext(Dispatchers.IO) {
        db.withTransaction {
            if (foodDao.countAll() > 0) return@withTransaction
            foodDao.insertAll(PrepopulationData.prepopulatedFoods)
        }
    }
}