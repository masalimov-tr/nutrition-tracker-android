package dev.masalimov.nutritiontracker.domain.food.usecase

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetFoodByQueryUseCase @Inject constructor(
    private val getSavedAndSearchFoodUseCase: GetSavedAndSearchFoodUseCase,
) {

    suspend operator fun invoke(query: String): SavedAndSearchResults {
        val (savedFood, searchedFood) = getSavedAndSearchFoodUseCase(query)
        if (query.isBlank()) {
            return SavedAndSearchResults(savedFood, searchedFood)
        }

        val filteredSavedFood = withContext(Dispatchers.Default) {
            savedFood.filter {
                it.name.contains(query, ignoreCase = true)
            }
        }

        return SavedAndSearchResults(
            filteredSavedFood,
            searchedFood,
        )
    }
}
