package dev.masalimov.nutritiontracker.data.food

import dev.masalimov.nutritiontracker.core.common.AppDispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FoodRemoteDataSource @Inject constructor(
    private val foodApi: FoodApi,
    private val appDispatchers: AppDispatchers,
) {
    suspend fun searchFood(query: String): List<FoodApiModel> {
        return withContext(appDispatchers.ioDispatcher) {
            foodApi.searchFood(query)
        }
    }
}