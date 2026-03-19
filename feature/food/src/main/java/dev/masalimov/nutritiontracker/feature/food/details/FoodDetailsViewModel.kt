package dev.masalimov.nutritiontracker.feature.food.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.masalimov.nutritiontracker.data.food.DataModule
import dev.masalimov.nutritiontracker.feature.food.FoodFeatureModule
import dev.masalimov.nutritiontracker.feature.food.list.FoodUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FoodDetailsViewModel(
    foodId: Long,
) : ViewModel() {
    val foodRepository = DataModule.getFoodRepository()

    private val _uiState: MutableStateFlow<FoodUiModel?> = MutableStateFlow(null)
    val uiState: StateFlow<FoodUiModel?> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val food = foodRepository.getFoodById(foodId)
            _uiState.value = FoodUiModel(
                id = food.id,
                name = food.name,
                caloriesPer100g = food.caloriesPer100g
            )
        }
    }

    companion object {
        fun Factory(foodId: Long): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return FoodDetailsViewModel(foodId) as T
            }

        }
    }
}