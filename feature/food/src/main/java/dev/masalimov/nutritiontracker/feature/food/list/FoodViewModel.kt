package dev.masalimov.nutritiontracker.feature.food.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.masalimov.nutritiontracker.domain.food.Food
import dev.masalimov.nutritiontracker.domain.food.FoodRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject


data class FoodUiModel(
    val id: Long,
    val name: String,
    val caloriesPer100g: Double,
)

data class FoodListUiState(
    val foodList: List<FoodUiModel> = emptyList(),
    val isLoading: Boolean = false,
)

@OptIn(FlowPreview::class)
@HiltViewModel
class FoodViewModel @Inject constructor(
    private val foodRepository: FoodRepository,
) : ViewModel() {

    private val _foodQuery: MutableStateFlow<String> = MutableStateFlow("")

    val uiState: StateFlow<FoodListUiState> = _foodQuery
        .distinctUntilChanged { old, new -> old == new }
        .map { it.trim() }
        .debounce { 300 }
        .map {
            if (it.isEmpty()) {
                emptyList()
            } else {
                foodRepository.searchFood(it).map {
                    it.toUiModel()
                }
            }
        }
        .map { it ->
            uiState.value.copy(foodList = it)
        }
        .onStart {
            emit(uiState.value.copy(isLoading = true))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = FoodListUiState(emptyList(), isLoading = true)
        )

    fun onQueryChanged(query: String) {
        _foodQuery.value = query
    }


//    val uiState: StateFlow<List<FoodUiModel>> =
//        combine(foodRepository.getAllFoodStream(), _uiHandle) { foods, _ ->
//            foods.map {
//                FoodUiModel(id = it.id.id, name = it.name, caloriesPer100g = it.caloriesPer100g)
//            }
//        }.stateIn(
//            scope = viewModelScope,
//            started = SharingStarted.WhileSubscribed(5000),
//            initialValue = emptyList()
//        )
}

private fun Food.toUiModel() = FoodUiModel(
    id = this.id.id,
    name = this.name,
    caloriesPer100g = this.caloriesPer100g
)