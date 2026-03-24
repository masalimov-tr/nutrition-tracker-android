package dev.masalimov.nutritiontracker.feature.food.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.masalimov.nutritiontracker.domain.food.FoodRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject


data class FoodUiModel(
    val id: Long,
    val name: String,
    val caloriesPer100g: Double,
)

@HiltViewModel
class FoodViewModel @Inject constructor(
    private val foodRepository: FoodRepository,
) : ViewModel() {

    val _uiHandle: MutableStateFlow<Int> = MutableStateFlow(0)

    val uiState: StateFlow<List<FoodUiModel>> =
        combine(foodRepository.getAllFoodStream(), _uiHandle) { foods, _ ->
            foods.map {
                FoodUiModel(id = it.id.id, name = it.name, caloriesPer100g = it.caloriesPer100g)
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}