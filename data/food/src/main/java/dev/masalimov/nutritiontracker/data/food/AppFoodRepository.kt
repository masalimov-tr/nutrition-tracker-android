package dev.masalimov.nutritiontracker.data.food

import dev.masalimov.nutritiontracker.core.database.food.FoodDao
import dev.masalimov.nutritiontracker.core.database.food.FoodEntity
import dev.masalimov.nutritiontracker.data.food.datasource.FoodRemoteDataSource
import dev.masalimov.nutritiontracker.domain.food.Food
import dev.masalimov.nutritiontracker.domain.food.FoodId
import dev.masalimov.nutritiontracker.domain.food.FoodRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class AppFoodRepository @Inject constructor(
    private val foodDao: FoodDao,
    private val foodRemoteDataSource: FoodRemoteDataSource,
) : FoodRepository {

    override suspend fun getAllFood(): List<Food> {
        return foodDao.getAll().map(FoodEntity::toFood)
    }

    override fun getAllFoodStream(): Flow<List<Food>> {
        return foodDao.getAllStream().map { it.map(FoodEntity::toFood) }
    }

    override suspend fun getFoodById(foodId: FoodId): Food {
        return foodDao.getById(foodId.id).toFood()
    }

    override suspend fun getSuggestedFood(eatenFood: List<Food>): List<Food> {
        val allFoods = foodDao.getAll()
        if (allFoods.isEmpty()) return emptyList()
        return allFoods.take(0).map(FoodEntity::toFood)
    }

    override suspend fun searchFood(query: String): List<Food> {
        return foodRemoteDataSource.searchFood(query).map(FoodApiModel::toFood)
    }

    override suspend fun deleteFood(foodId: Long) {
        delay(1000)
        foodDao.deleteById(foodId)
    }
}
