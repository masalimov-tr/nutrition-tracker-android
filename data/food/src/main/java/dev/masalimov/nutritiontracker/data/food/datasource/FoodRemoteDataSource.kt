package dev.masalimov.nutritiontracker.data.food.datasource

import dev.masalimov.nutritiontracker.core.common.AppDispatchers
import dev.masalimov.nutritiontracker.data.food.FoodApi
import dev.masalimov.nutritiontracker.data.food.FoodApiModel
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class FoodRemoteDataSource @Inject constructor(
    private val foodApi: FoodApi,
    private val appDispatchers: AppDispatchers,
) {
    suspend fun searchFood(query: String): List<FoodApiModel> {
        return withContext(appDispatchers.ioDispatcher) {
            foodApi.searchFood(query)
        }
    }
}