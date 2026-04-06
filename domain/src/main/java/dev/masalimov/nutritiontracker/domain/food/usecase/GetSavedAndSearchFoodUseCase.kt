package dev.masalimov.nutritiontracker.domain.food.usecase

import dev.masalimov.nutritiontracker.domain.food.Food
import dev.masalimov.nutritiontracker.domain.food.FoodRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

data class SavedAndSearchResults(
    val savedFood: List<Food>,
    val searchFood: List<Food>,
)

class GetSavedAndSearchFoodUseCase @Inject constructor(
    private val foodRepository: FoodRepository,
) {
    suspend operator fun invoke(query: String): SavedAndSearchResults = coroutineScope {
        val savedFood = async { foodRepository.getAllFood() }
        val searchFood = async { foodRepository.searchFood(query) }
        SavedAndSearchResults(savedFood.await(), searchFood.await())
    }
}