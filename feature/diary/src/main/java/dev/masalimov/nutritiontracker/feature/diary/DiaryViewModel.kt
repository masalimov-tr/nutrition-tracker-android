package dev.masalimov.nutritiontracker.feature.diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.masalimov.nutritiontracker.core.common.AppDispatchers
import dev.masalimov.nutritiontracker.core.ui.logD
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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DiaryViewModel @Inject constructor(
    getCaloriesConsumptionForDateRangeUseCase: GetCaloriesConsumptionForDateRangeUseCase,
    private val getDiaryStreamForDateUseCase: GetDiaryStreamForDateUseCase,
    private val addFoodToDiaryUseCase: AddFoodToDiaryUseCase,
    private val goalCalories: GoalCalories,
    diaryDateCalendar: DiaryDateCalendar,
    appDispatcher: AppDispatchers,
) : ViewModel() {

    private val calendarDates: List<DiaryDate> = diaryDateCalendar.dates

    val caloriesConsumptionStatusFlow = getCaloriesConsumptionForDateRangeUseCase(
        calendarDates.first(),
        calendarDates.last()
    )
        .onStart {
            logD("caloriesConsumptionStatusFlow onStart")
        }
        .onEach {
            logD("caloriesConsumptionStatusFlow on each: $it")
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    private val _selectedDate = MutableStateFlow(diaryDateCalendar.startingDate)

    val calendarUiState: StateFlow<CalendarUiState> = _selectedDate
        .combine(caloriesConsumptionStatusFlow) { selectedDate, statuses ->
            logD("calendarUiState combine: selectedDate = $selectedDate, statuses = $statuses")
            CalendarUiState.Calendar(
                buildCalendar(
                    calendarDates,
                    selectedDate,
                    statuses = statuses
                )
            ) as CalendarUiState
        }
        .onStart {
            logD("calendarUiState onStart")
            emit(CalendarUiState.Loading)
        }
        .onEach {
            logD("calendarUiState on each: $it")
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            CalendarUiState.Loading
        )

    val diaryInfoUiState: StateFlow<DiaryInfoUiState> = _selectedDate
        .flatMapLatest { date ->
            getDiaryStreamForDateUseCase(date)
                .map { diaryInfoForDate ->
                    DiaryInfoUiState.DiaryInfo(
                        eatenFoodList = diaryInfoForDate.diaryEntryForDate?.eatenFood?.map { it.toEatenFoodUiModel() }
                            ?: emptyList(),
                        suggestedFoodList = diaryInfoForDate.suggestedFood.map(Food::toSuggestedFoodUiModel),
                        caloriesEatenTotal = diaryInfoForDate.diaryEntryForDate?.caloriesEaten
                            ?: 0,
                        goalCaloriesPerDay = diaryInfoForDate.diaryEntryForDate?.goalCaloriesPerDay
                            ?: goalCalories.caloriesPerDay,
                    ) as DiaryInfoUiState
                }
                .flowOn(appDispatcher.defaultDispatcher)
                .onStart { emit(DiaryInfoUiState.Loading) }
                .catch { e -> emit(DiaryInfoUiState.Error("Error has occurred: ${e.message}")) }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            DiaryInfoUiState.Loading
        )

    private fun buildCalendar(
        dates: List<DiaryDate>,
        selectedDate: DiaryDate?,
        statuses: Map<DiaryDate, CalorieConsumptionStatus> = emptyMap()
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

sealed interface CalendarUiState {
    data object Loading : CalendarUiState
    data class Calendar(val uiModels: List<DateUiModel>) : CalendarUiState
}

sealed interface DiaryInfoUiState {
    data object Loading : DiaryInfoUiState
    data class DiaryInfo(
        val eatenFoodList: List<EatenFoodUiModel>,
        val suggestedFoodList: List<SuggestedFoodUiModel>,
        val caloriesEatenTotal: Int,
        val goalCaloriesPerDay: Int,
    ) : DiaryInfoUiState {
        companion object {
            val Empty = DiaryInfo(
                eatenFoodList = emptyList(),
                suggestedFoodList = emptyList(),
                caloriesEatenTotal = 0,
                goalCaloriesPerDay = 0,
            )
        }
    }

    data class Error(val errorMessage: String) : DiaryInfoUiState
}