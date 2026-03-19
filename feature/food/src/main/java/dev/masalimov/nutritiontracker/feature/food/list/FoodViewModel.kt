package dev.masalimov.nutritiontracker.feature.food.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.masalimov.nutritiontracker.data.food.DataModule
import dev.masalimov.nutritiontracker.feature.food.FoodFeatureModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


data class FoodUiModel(
    val id: Long,
    val name: String,
    val caloriesPer100g: Double,
)

class FoodViewModel(
) : ViewModel() {
    val foodRepository = DataModule.getFoodRepository()

    private val _uiState: MutableStateFlow<List<FoodUiModel>> = MutableStateFlow(emptyList())
    val uiState: StateFlow<List<FoodUiModel>> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val foods = foodRepository.getAllFood()
            val uiModels = withContext(Dispatchers.Default) {
                foods.map {
                    FoodUiModel(id = it.id, name = it.name, caloriesPer100g = it.caloriesPer100g)
                }
            }
            _uiState.update {
                uiModels
            }
        }
    }
}