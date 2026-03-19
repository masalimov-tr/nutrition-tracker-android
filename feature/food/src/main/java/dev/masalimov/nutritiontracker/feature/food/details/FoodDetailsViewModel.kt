package dev.masalimov.nutritiontracker.feature.food.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.masalimov.nutritiontracker.data.food.FoodRepository
import dev.masalimov.nutritiontracker.feature.food.list.FoodUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = FoodDetailsViewModel.Factory::class)
class FoodDetailsViewModel @AssistedInject constructor(
    @Assisted private val foodId: Long,
    private val foodRepository: FoodRepository,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(foodId: Long): FoodDetailsViewModel
    }

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
}