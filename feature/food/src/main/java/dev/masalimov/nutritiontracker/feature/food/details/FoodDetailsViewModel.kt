package dev.masalimov.nutritiontracker.feature.food.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.masalimov.nutritiontracker.domain.food.FoodId
import dev.masalimov.nutritiontracker.domain.food.FoodRepository
import dev.masalimov.nutritiontracker.feature.food.list.FoodUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FoodDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val foodRepository: FoodRepository,
) : ViewModel() {
    private val foodId: Long = savedStateHandle.toRoute<FoodDetailRoute>().foodId

    private val _uiState: MutableStateFlow<FoodUiModel?> = MutableStateFlow(null)
    val uiState: StateFlow<FoodUiModel?> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val food = foodRepository.getFoodById(FoodId(foodId))
            _uiState.value = FoodUiModel(
                id = food.id.id,
                name = food.name,
                caloriesPer100g = food.caloriesPer100g
            )
        }
    }
}