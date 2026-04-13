package dev.masalimov.nutritiontracker.feature.diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.masalimov.nutritiontracker.core.common.AppDispatchers
import dev.masalimov.nutritiontracker.domain.GoalCalories
import dev.masalimov.nutritiontracker.domain.diary.CalorieConsumptionStatus
import dev.masalimov.nutritiontracker.domain.diary.model.DiaryDate
import dev.masalimov.nutritiontracker.domain.diary.model.DiaryDateCalendar
import dev.masalimov.nutritiontracker.domain.diary.usecase.AddFoodToDiaryUseCase
import dev.masalimov.nutritiontracker.domain.diary.usecase.GetCaloriesConsumptionForDateRangeUseCase
import dev.masalimov.nutritiontracker.domain.diary.usecase.GetDiaryStreamForDateUseCase
import dev.masalimov.nutritiontracker.domain.food.Food
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DiaryViewModel @Inject constructor(
    private val getCaloriesConsumptionForDateRangeUseCase: GetCaloriesConsumptionForDateRangeUseCase,
    private val getDiaryStreamForDateUseCase: GetDiaryStreamForDateUseCase,
    private val addFoodToDiaryUseCase: AddFoodToDiaryUseCase,
    private val goalCalories: GoalCalories,
    diaryDateCalendar: DiaryDateCalendar,
    appDispatcher: AppDispatchers,
) : ViewModel() {

    private val calendarDates: List<DiaryDate> = diaryDateCalendar.dates

    private val caloriesConsumptionStatusFlow = getCaloriesConsumptionForDateRangeUseCase(
        diaryDateCalendar.startingDate.plusDays(-1),
        diaryDateCalendar.startingDate.plusDays(1),
    )
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyMap()
    )

    private val _selectedDate = MutableStateFlow(diaryDateCalendar.startingDate)

    val uiState: StateFlow<DiaryUiState> = _selectedDate.flatMapLatest { selectedDate ->
        getDiaryStreamForDateUseCase(selectedDate)
            .combine(caloriesConsumptionStatusFlow) { diaryInfoForDate, caloriesConsumptionStatusMap ->
                DiaryUiState(
                    isLoading = false,
                    caloriesEatenTotal = diaryInfoForDate.diaryEntryForDate?.caloriesEaten ?: 0,
                    eatenFoodList = diaryInfoForDate.diaryEntryForDate?.eatenFood?.map { it.toEatenFoodUiModel() }
                        ?: emptyList(),
                    suggestedFoodList = diaryInfoForDate.suggestedFood.map(Food::toSuggestedFoodUiModel),
                    goalCaloriesPerDay = diaryInfoForDate.diaryEntryForDate?.goalCaloriesPerDay
                        ?: goalCalories.caloriesPerDay,
                    calendar = buildCalendar(calendarDates, selectedDate, caloriesConsumptionStatusMap)
                )
            }
            .onEach {
                it
            }
            .onStart {
                emit(DiaryUiState.loading(calendar = buildCalendar(calendarDates, selectedDate)))
            }
    }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            DiaryUiState.loading(calendar = buildCalendar(calendarDates, _selectedDate.value))
        )

    private fun buildCalendar(
        dates: List<DiaryDate>,
        selectedDate: DiaryDate?,
        statuses: Map<DiaryDate, CalorieConsumptionStatus> = caloriesConsumptionStatusFlow.value
    ): List<DateUiModel> = dates.map { date ->
        DateUiModel(
            date = date,
            isSelected = selectedDate?.let { it == date } ?: false,
            calorieConsumptionStatus = statuses[date] ?: CalorieConsumptionStatus.Unknown
        )
    }

    fun onSelectDate(date: DiaryDate) {
        _selectedDate.value = date
    }

    private val _addFoodUiState: MutableStateFlow<AddFoodUiState> =
        MutableStateFlow(AddFoodUiState.Idle)
    val addFoodUiState: StateFlow<AddFoodUiState> = _addFoodUiState.asStateFlow()


    fun addFoodToDiary(foodIdToAdd: Long, quantityGrams: Double = 100.0) {
        viewModelScope.launch {
            _addFoodUiState.value = AddFoodUiState.Loading
            try {
                addFoodToDiaryUseCase(foodIdToAdd, _selectedDate.value, quantityGrams)
                _addFoodUiState.value = AddFoodUiState.Success
            } catch (e: Exception) {
                _addFoodUiState.value = AddFoodUiState.Error
                return@launch
            }
        }
    }

    fun addFoodErrorShowed() {
        _addFoodUiState.value = AddFoodUiState.Idle
    }

}
