package dev.masalimov.nutritiontracker.feature.diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.masalimov.nutritiontracker.domain.DiaryDate
import dev.masalimov.nutritiontracker.domain.Food
import dev.masalimov.nutritiontracker.domain.GetDiaryForTodayUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import javax.inject.Inject

data class FoodUiModel(
    val id: Long,
    val name: String,
    val caloriesPer100g: Double? = null,
)

data class DateUiModel(
    val date: LocalDate,
)

data class DiaryUiState(
    val isLoading: Boolean,
    val date: DateUiModel? = null,
    val foodList: List<FoodUiModel> = emptyList(),
    val suggestedFoodList: List<FoodUiModel> = emptyList(),
    val caloriesPerDay: Int? = null,
) {
    companion object {
        fun loading() = DiaryUiState(isLoading = true)
    }
}


@HiltViewModel
class DiaryViewModel @Inject constructor(
    private val getDiaryByDateUseCase: GetDiaryForTodayUseCase,
) : ViewModel() {

    private val _diaryUiState: MutableStateFlow<DiaryUiState> =
        MutableStateFlow(DiaryUiState.loading())
    val diaryUiState: StateFlow<DiaryUiState> = _diaryUiState.asStateFlow()

    fun onEvent(diaryEvent: DiaryEvent) {
        when (diaryEvent) {
            is DiaryEvent.LoadToday -> loadDiary(DiaryDate.today())
            is DiaryEvent.LoadDate -> loadDiary(diaryEvent.date)
        }
    }

    fun loadDiary(date: DiaryDate) {
        viewModelScope.launch {
            _diaryUiState.update {
                it.copy(
                    isLoading = true,
                    date = DateUiModel(date.date),
                )
            }
            val diary = getDiaryByDateUseCase(date)
            val (foodList, suggestedFoodList) = withContext(Dispatchers.Default) {
                val eatenFood = diary.eatenFood.map { it.toFoodUiModel() }
                val suggestedFood = diary.suggestedFood.map { it.toFoodUiModel() }
                eatenFood to suggestedFood
            }
            _diaryUiState.update {
                it.copy(
                    isLoading = false,
                    foodList = foodList,
                    suggestedFoodList = suggestedFoodList,
                    caloriesPerDay = diary.caloriesPerDay,
                )
            }
        }
    }

    sealed interface DiaryEvent {
        data object LoadToday : DiaryEvent
        data class LoadDate(val date: DiaryDate) : DiaryEvent
    }
}

private fun Food.toFoodUiModel() = FoodUiModel(
    id = id.id,
    name = name,
    caloriesPer100g = caloriesPer100g,
)
