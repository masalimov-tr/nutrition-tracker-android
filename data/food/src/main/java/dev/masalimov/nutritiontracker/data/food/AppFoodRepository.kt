package dev.masalimov.nutritiontracker.data.food

import dev.masalimov.nutritiontracker.core.database.food.FoodDao
import dev.masalimov.nutritiontracker.core.database.food.FoodEntity
import dev.masalimov.nutritiontracker.domain.food.Food
import dev.masalimov.nutritiontracker.domain.food.FoodId
import dev.masalimov.nutritiontracker.domain.food.FoodRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class AppFoodRepository @Inject constructor(
    private val foodDao: FoodDao,
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
        return allFoods.take(5).map(FoodEntity::toFood)
    }
}

fun FoodEntity.toFood(): Food {
    return Food(
        id = FoodId(uid),
        name = name,
        caloriesPer100g = caloriesPer100g,
        proteinPer100g = proteinPer100g,
        fatPer100g = fatPer100g,
        carbsPer100g = carbsPer100g,
    )
}
