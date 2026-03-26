package dev.masalimov.nutritiontracker.feature.diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.masalimov.nutritiontracker.domain.diary.model.DiaryDate
import dev.masalimov.nutritiontracker.domain.diary.usecase.GetDiaryStreamForDateUseCase
import dev.masalimov.nutritiontracker.domain.food.Food
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject


@HiltViewModel
class DiaryViewModel @Inject constructor(
    private val getDiaryStreamForDateUseCase: GetDiaryStreamForDateUseCase,
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(DiaryDate.today())

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<DiaryUiState> = _selectedDate
        .distinctUntilChanged { old, new -> old.date == new.date }
        .flatMapLatest { date: DiaryDate ->
            getDiaryStreamForDateUseCase(date)
                .map { diary ->
                    DiaryUiState(
                        dateList = uiState.value.dateList,
                        caloriesEatenTotal = diary.diaryEntitiesPerDay.sumOf { it.eatenFood.calories },
                        eatenFoodList = diary.diaryEntitiesPerDay.map { it.eatenFood.toEatenFoodUiModel() },
                        suggestedFoodList = diary.suggestedFood.map(Food::toSuggestedFoodUiModel),
                        goalCaloriesPerDay = diary.goalCaloriesPerDay,
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

