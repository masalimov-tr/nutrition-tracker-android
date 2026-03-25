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


data class DateUiModel(
    val date: LocalDate,
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

data class DiaryUiState(
    val isLoading: Boolean,
    val date: DateUiModel? = null,
    val eatenFoodList: List<EatenFoodUiModel> = emptyList(),
    val suggestedFoodList: List<SuggestedFoodUiModel> = emptyList(),
    val caloriesEatenTotal: Int? = null,
    val goalCaloriesPerDay: Int? = null,
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
            val (eatenFoodList, suggestedFoodList, caloriesEaten) = withContext(Dispatchers.Default) {
                Triple(
                    diary.diaryEntitiesPerDay.map { it.eatenFood.toEatenFoodUiModel() },
                    diary.suggestedFood.map(Food::toSuggestedFoodUiModel),
                    diary.diaryEntitiesPerDay.sumOf { it.eatenFood.calories })
            }
            _diaryUiState.update {
                it.copy(
                    isLoading = false,
                    caloriesEatenTotal = caloriesEaten,
                    eatenFoodList = eatenFoodList,
                    suggestedFoodList = suggestedFoodList,
                    goalCaloriesPerDay = diary.caloriesPerDay,
                )
            }
        }
    }

    sealed interface DiaryEvent {
        data object LoadToday : DiaryEvent
        data class LoadDate(val date: DiaryDate) : DiaryEvent
    }
}

