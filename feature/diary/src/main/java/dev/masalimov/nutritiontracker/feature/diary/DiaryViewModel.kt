package dev.masalimov.nutritiontracker.feature.diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.masalimov.nutritiontracker.domain.GoalCalories
import dev.masalimov.nutritiontracker.domain.diary.CalorieConsumptionStatus
import dev.masalimov.nutritiontracker.domain.diary.model.DiaryDate
import dev.masalimov.nutritiontracker.domain.diary.model.DiaryDateCalendar
import dev.masalimov.nutritiontracker.domain.diary.usecase.AddFoodToDiaryUseCase
import dev.masalimov.nutritiontracker.domain.diary.usecase.GetCaloriesConsumptionForDateRangeUseCase
import dev.masalimov.nutritiontracker.domain.diary.usecase.GetDiaryStreamForDateUseCase
import dev.masalimov.nutritiontracker.domain.food.Food
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DiaryViewModel @Inject constructor(
    private val getCaloriesConsumptionForDateRangeUseCase: GetCaloriesConsumptionForDateRangeUseCase,
    private val getDiaryStreamForDateUseCase: GetDiaryStreamForDateUseCase,
    private val addFoodToDiaryUseCase: AddFoodToDiaryUseCase,
    private val goalCalories: GoalCalories,
    private val diaryDateCalendar: DiaryDateCalendar,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(diaryDateCalendar.startingDate)
    private val _addFoodUiState: MutableStateFlow<AddFoodUiState> =
        MutableStateFlow(AddFoodUiState.Idle)
    val addFoodUiState: StateFlow<AddFoodUiState> = _addFoodUiState.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<DiaryUiState> = _selectedDate
        .distinctUntilChanged { prev, next -> prev.date == next.date }
        .flatMapLatest { selectedDate: DiaryDate ->
            getDiaryStreamForDateUseCase(selectedDate)
                .combine(getCaloriesConsumptionForDateRangeUseCase(diaryDateCalendar.dates.first(), diaryDateCalendar.dates.last())) { diaryInfoForDate, caloriesConsumptionsList ->
                    diaryInfoForDate to caloriesConsumptionsList
                }
                .map { (diaryInfoForDate, caloriesConsumptionsList) ->
                    DiaryUiState(
                        dateList = uiState.value.dateList.map { dateUiModel ->
                            val status = caloriesConsumptionsList.find { it.first == dateUiModel.date }?.second
                            dateUiModel.copy(
                                calorieConsumptionStatus = status ?: CalorieConsumptionStatus.Unknown,
                            )
                        },
                        caloriesEatenTotal = diaryInfoForDate.diaryEntryForDate?.caloriesEaten ?: 0,
                        eatenFoodList = diaryInfoForDate.diaryEntryForDate?.eatenFood?.map { it.toEatenFoodUiModel() }
                            ?: emptyList(),
                        suggestedFoodList = diaryInfoForDate.suggestedFood.map(Food::toSuggestedFoodUiModel),
                        goalCaloriesPerDay = diaryInfoForDate.diaryEntryForDate?.goalCaloriesPerDay
                            ?: goalCalories.caloriesPerDay,
                        isLoading = false,
                    )
                }
                .onStart {
                    val loadingState = DiaryUiState(
                        isLoading = true,
                        dateList = uiState.value.dateList.map { dateUiModel ->
                            dateUiModel.copy(isSelected = dateUiModel.date == selectedDate)
                        },
                    )
                    emit(loadingState)
                }
                .catch { e ->
                    emit(
                        uiState.value.copy(
                            isLoading = false,
                            error = e.message ?: "Failed to load diary data",
                        )
                    )
                }
                .flowOn(defaultDispatcher)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DiaryUiState.loading(
            diaryDateCalendar.dates.map { DateUiModel(it) }
        ))

    fun onSelectDate(date: DiaryDate) {
        _selectedDate.value = date
    }

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
