package dev.masalimov.nutritiontracker.feature.food

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.masalimov.nutritiontracker.data.food.Food
import dev.masalimov.nutritiontracker.data.food.FoodRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class FoodUiModel(
    val name: String,
    val caloriesPer100g: Double,
) {
    val nameAndCalories = "$name ($caloriesPer100g calories per 100g)"
}
class FoodViewModel : ViewModel() {
    private val foodRepository: FoodRepository = object : FoodRepository {
        override suspend fun getAllFood(): List<Food> {
            return listOf(
                Food(
                    id = 1,
                    name = "Apple",
                    caloriesPer100g = 52.0,
                    proteinPer100g = 0.3,
                    fatPer100g = 0.2,
                    carbsPer100g = 14.0,
                ),
                Food(
                    id = 2,
                    name = "Banana",
                    caloriesPer100g = 96.0,
                    proteinPer100g = 1.3,
                    fatPer100g = 0.3,
                    carbsPer100g = 27.0,
                )
            )
        }

    }


    private val _uiState: MutableStateFlow<List<FoodUiModel>> = MutableStateFlow(emptyList())
    val uiState: StateFlow<List<FoodUiModel>> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val foods = foodRepository.getAllFood()
            val uiModels = withContext(Dispatchers.Default) {
                foods.map {
                    FoodUiModel(name = it.name, caloriesPer100g = it.caloriesPer100g)
                }
            }
            _uiState.update {
                uiModels
            }
        }
    }
}