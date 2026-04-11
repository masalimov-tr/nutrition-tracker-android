package dev.masalimov.nutritiontracker.feature.diary

import dev.masalimov.nutritiontracker.domain.diary.CalorieConsumptionStatus
import dev.masalimov.nutritiontracker.domain.diary.model.DiaryDate


private val today = DiaryDate.today()
internal val calendar: List<DateUiModel> = (-7..7).map {
    DateUiModel(today.plusDays(it))
}

data class DiaryUiState(
    val isLoading: Boolean,
    val dateList: List<DateUiModel> = calendar,
    val eatenFoodList: List<EatenFoodUiModel> = emptyList(),
    val suggestedFoodList: List<SuggestedFoodUiModel> = emptyList(),
    val caloriesEatenTotal: Int? = null,
    val goalCaloriesPerDay: Int? = null,
    val error: String? = null,
) {
    companion object {
        fun loading() = DiaryUiState(isLoading = true)
    }
}

data class DateUiModel(
    val date: DiaryDate,
    val isSelected: Boolean = false,
    val calorieConsumptionStatus: CalorieConsumptionStatus = CalorieConsumptionStatus.Unknown,
)

data class EatenFoodUiModel(
    val name: String,
    val quantityGram: Double,
    val caloriesEaten: Int,
    val caloriesPer100g: Int,
)

data class SuggestedFoodUiModel(
    val name: String,
    val caloriesPer100g: Double? = null,
)

sealed interface AddFoodUiState {
    object Idle : AddFoodUiState
    object Loading : AddFoodUiState
    object Success : AddFoodUiState
    object Error : AddFoodUiState
}