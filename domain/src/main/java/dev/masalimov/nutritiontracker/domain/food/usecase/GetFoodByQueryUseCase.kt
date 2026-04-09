package dev.masalimov.nutritiontracker.domain.food.usecase

import dev.masalimov.nutritiontracker.domain.food.Food
import dev.masalimov.nutritiontracker.domain.food.FoodRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class SavedAndSearchResults(
    val savedFood: List<Food>,
    val searchFood: List<Food>,
)

class GetFoodByQueryUseCase @Inject constructor(
    private val foodRepository: FoodRepository,
) {

    suspend operator fun invoke(query: String): SavedAndSearchResults = coroutineScope {
        if (query.isBlank()) {
            val savedFood = foodRepository.getAllFood()
            return@coroutineScope SavedAndSearchResults(savedFood, emptyList())
        }

        val savedDeferred = async { foodRepository.getAllFood() }
        val searchedDeferred = async { foodRepository.searchFood(query) }
        val savedFood = savedDeferred.await()
        val searchedFood = searchedDeferred.await()

        val filteredSavedFood = withContext(Dispatchers.Default) {
            savedFood.filter { it.name.contains(query, ignoreCase = true) }
        }

        SavedAndSearchResults(
            filteredSavedFood,
            searchedFood,
        )
    }
}
