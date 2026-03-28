package dev.masalimov.nutritiontracker.feature.food.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.masalimov.nutritiontracker.domain.FoodSearchException
import dev.masalimov.nutritiontracker.domain.food.Food
import dev.masalimov.nutritiontracker.domain.food.FoodRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import javax.inject.Inject


data class FoodUiModel(
    val id: Long,
    val name: String,
    val caloriesPer100g: Double,
)

sealed class FoodListAsync(
    open val foodList: List<FoodUiModel> = emptyList()
) {
    data object Initial : FoodListAsync()
    data class Loading(val list: List<FoodUiModel>) : FoodListAsync(list)

    data class Success(val list: List<FoodUiModel>) : FoodListAsync(list)
}

data class FoodListUiState(
    val foodListAsync: FoodListAsync = FoodListAsync.Initial,
    val errorMessage: String? = null,
)

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class FoodViewModel @Inject constructor(
    private val foodRepository: FoodRepository,
) : ViewModel() {

    private val _foodQuery: MutableStateFlow<String> = MutableStateFlow("")

    val uiState: StateFlow<FoodListUiState> = _foodQuery
        .map { it.trim() }
        .distinctUntilChanged()
        .transformLatest { query ->
            if (query.isEmpty()) {
                emit(FoodListUiState(FoodListAsync.Initial))
                return@transformLatest
            }

            emit(FoodListUiState(FoodListAsync.Loading(
                uiState.value.foodListAsync.foodList
            )))

            if (query.isNotEmpty())
                delay(300) // debounce

            try {
                val foundFood = foodRepository.searchFood(query).map { it.toUiModel() }
                emit(FoodListUiState(FoodListAsync.Success(foundFood)))
            } catch (e: Throwable) {
                val errorMessage = if (e is FoodSearchException) e.errorMessage else null
                emit(FoodListUiState(FoodListAsync.Success(
                    uiState.value.foodListAsync.foodList
                ), errorMessage))
            }

        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = FoodListUiState(FoodListAsync.Initial)
        )

    fun onQueryChanged(query: String) {
        _foodQuery.value = query
    }
}

private fun Food.toUiModel() = FoodUiModel(
    id = this.id.id,
    name = this.name,
    caloriesPer100g = this.caloriesPer100g
)