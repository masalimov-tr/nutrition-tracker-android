package dev.masalimov.nutritiontracker.feature.food.list

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.masalimov.nutritiontracker.domain.FoodSearchException
import dev.masalimov.nutritiontracker.domain.food.Food
import dev.masalimov.nutritiontracker.domain.food.FoodRepository
import dev.masalimov.nutritiontracker.domain.food.usecase.GetFoodByQueryUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
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


sealed class DeletionState {
    data object Idle : DeletionState()
    data object InProgress : DeletionState()
    data object Success : DeletionState()
    data class Error(val message: String) : DeletionState()
}

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class FoodViewModel @Inject constructor(
    private val getFoodByQueryUseCase: GetFoodByQueryUseCase,
    private val foodRepository: FoodRepository,
) : ViewModel() {

    private val _foodQuery: MutableStateFlow<String> = MutableStateFlow("")
    private val _refreshTrigger: MutableStateFlow<Int> = MutableStateFlow(0)

    private val _deletionState = MutableStateFlow<DeletionState>(DeletionState.Idle)
    val deletionState: StateFlow<DeletionState> = _deletionState.asStateFlow()

    val uiState: StateFlow<FoodListUiState> = combine(_foodQuery, _refreshTrigger) { query, _ -> query }
        .debounce { if (it.isEmpty()) 0 else 300 }
        .map { it.trim() }
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
        viewModelScope.launch {
            _deletionState.value = DeletionState.InProgress
            try {
                foodRepository.deleteFood(foodId)
                _deletionState.value = DeletionState.Success
                _refreshTrigger.value++
            } catch (e: Throwable) {
                _deletionState.value = DeletionState.Error(e.message ?: "Failed to delete food")
            }
        }
    }

    fun onDeletionSnackbarDismissed() {
        _deletionState.value = DeletionState.Idle
    }
}

private fun Food.toUiModel() = FoodUiModel(
    id = this.id.id,
    name = this.name,
    caloriesPer100g = this.caloriesPer100g
)