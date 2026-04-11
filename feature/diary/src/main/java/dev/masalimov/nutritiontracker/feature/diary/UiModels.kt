package dev.masalimov.nutritiontracker.feature.diary

import dev.masalimov.nutritiontracker.domain.diary.CalorieConsumptionStatus
import dev.masalimov.nutritiontracker.domain.diary.model.DiaryDate


private val today = DiaryDate.today()
internal val diaryDateCalendar = (-7..7).map {
    today.plusDays(it)
}
internal val uiDateList = diaryDateCalendar.map {
    DateUiModel(it)
}


data class DiaryUiState(
    val isLoading: Boolean,
    val dateList: List<DateUiModel>,
    val eatenFoodList: List<EatenFoodUiModel> = emptyList(),
    val suggestedFoodList: List<SuggestedFoodUiModel> = emptyList(),
    val caloriesEatenTotal: Int? = null,
    val goalCaloriesPerDay: Int? = null,
    val error: String? = null,
) {
    companion object {
        fun loading(dateList: List<DateUiModel>) = DiaryUiState(isLoading = true, dateList = dateList)
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