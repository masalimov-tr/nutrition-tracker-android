package dev.masalimov.nutritiontracker.feature.diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.masalimov.nutritiontracker.domain.GoalCalories
import dev.masalimov.nutritiontracker.domain.diary.CalorieConsumptionStatus
import dev.masalimov.nutritiontracker.domain.diary.model.DiaryDate
import dev.masalimov.nutritiontracker.domain.diary.usecase.GetCaloriesConsumptionPerDateUseCase
import dev.masalimov.nutritiontracker.domain.diary.usecase.GetDiaryStreamForDateUseCase
import dev.masalimov.nutritiontracker.domain.food.Food
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject


@HiltViewModel
class DiaryViewModel @Inject constructor(
    private val getCaloriesConsumptionPerDateUseCase: GetCaloriesConsumptionPerDateUseCase,
    private val getDiaryStreamForDateUseCase: GetDiaryStreamForDateUseCase,
    private val goalCalories: GoalCalories,
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(DiaryDate.today())

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<DiaryUiState> = _selectedDate
        .distinctUntilChanged { old, new -> old.date == new.date }
        .flatMapLatest { date: DiaryDate ->
            getDiaryStreamForDateUseCase(date).combine(getCaloriesConsumptionPerDateUseCase())
            { diaryInfoForDate, caloriesConsumptionPerDate ->
                DiaryUiState(
                    dateList = uiState.value.dateList.map { currentDate ->
                        currentDate.copy(
                            calorieConsumptionStatus = caloriesConsumptionPerDate.getOrDefault(
                                key = currentDate.date,
                                defaultValue = CalorieConsumptionStatus.Unknown
                            ),
                        )
                    },
                    caloriesEatenTotal = diaryInfoForDate.diary?.caloriesEaten,
                    eatenFoodList = diaryInfoForDate.diary?.eatenFood?.map { it.toEatenFoodUiModel() } ?: emptyList(),
                    suggestedFoodList = diaryInfoForDate.suggestedFood.map(Food::toSuggestedFoodUiModel),
                    goalCaloriesPerDay = diaryInfoForDate.diary?.goalCaloriesPerDay ?: goalCalories.caloriesPerDay,
                    isLoading = false,
                )
            }
                .onStart {
                    emit(
                        DiaryUiState(
                            isLoading = true,
                            dateList = calendar.map { dateUiModel ->
                                dateUiModel.copy(isSelected = dateUiModel.date == date)
                            },
                        )
                    )
                }
                .flowOn(Dispatchers.Default)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DiaryUiState.loading())

    fun onSelectDate(date: DiaryDate) {
        _selectedDate.value = date
    }

}

