package dev.masalimov.nutritiontracker.feature.food.list

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.masalimov.nutritiontracker.domain.FoodSearchException
import dev.masalimov.nutritiontracker.domain.food.Food
import dev.masalimov.nutritiontracker.domain.food.usecase.GetFoodByQueryUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import javax.inject.Inject

@Immutable
data class FoodUiModel(
    val id: Long,
    val name: String,
    val caloriesPer100g: Double,
)

sealed class FoodListUiState(
    open val foodList: List<FoodUiModel> = emptyList()
) {
    data object Initial : FoodListUiState()
    data class Loading(private val list: List<FoodUiModel>) : FoodListUiState(list)
    data class Success(val savedFood: List<FoodUiModel>, val searchedFood: List<FoodUiModel>) :
        FoodListUiState(savedFood + searchedFood)

    data class Error(private val list: List<FoodUiModel>, val errorMessage: String?) : FoodListUiState(list)
}


@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class FoodViewModel @Inject constructor(
    private val getFoodByQueryUseCase: GetFoodByQueryUseCase,
) : ViewModel() {

    private val _foodQuery: MutableStateFlow<String> = MutableStateFlow("")

    val uiState: StateFlow<FoodListUiState> = _foodQuery
        .debounce { if (it.isEmpty()) 0 else 300 }
        .map { it.trim() }
        .distinctUntilChanged()
        .transformLatest { query ->
            emit(FoodListUiState.Loading(uiState.value.foodList))

            if (query.isNotEmpty())
                delay(300) // debounce

            try {
                val (savedFood, searchedFood) = getFoodByQueryUseCase(query)
                emit(
                    FoodListUiState.Success(
                        savedFood = savedFood.map(Food::toUiModel),
                        searchedFood = searchedFood.map(Food::toUiModel),
                    )
                )
            } catch (e: Throwable) {
                val errorMessage = if (e is FoodSearchException) e.errorMessage else null
                emit(FoodListUiState.Error(uiState.value.foodList, errorMessage))
            }

        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = FoodListUiState.Initial
        )

    fun onQueryChanged(query: String) {
        _foodQuery.value = query
    }
    fun onItemDeleteClick(foodId: Long) {
        // TODO: delete food
    }
}

private fun Food.toUiModel() = FoodUiModel(
    id = this.id.id,
    name = this.name,
    caloriesPer100g = this.caloriesPer100g
)