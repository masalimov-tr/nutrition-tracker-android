package dev.masalimov.nutritiontracker.feature.diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.masalimov.nutritiontracker.domain.diary.model.DiaryDate
import dev.masalimov.nutritiontracker.domain.diary.usecase.GetDiaryStreamForDateUseCase
import dev.masalimov.nutritiontracker.domain.food.Food
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject


@HiltViewModel
class DiaryViewModel @Inject constructor(
    getDiaryStreamForDateUseCase: GetDiaryStreamForDateUseCase,
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(DiaryDate.today())

    val uiState = _selectedDate.combine(
        getDiaryStreamForDateUseCase(_selectedDate.value)
    ) { selectedDate, diary ->
        DiaryUiState(
            caloriesEatenTotal = diary.diaryEntitiesPerDay.sumOf { it.eatenFood.calories },
            eatenFoodList = diary.diaryEntitiesPerDay.map { it.eatenFood.toEatenFoodUiModel() },
            suggestedFoodList = diary.suggestedFood.map(Food::toSuggestedFoodUiModel),
            goalCaloriesPerDay = diary.goalCaloriesPerDay,
            isLoading = false,
        )
    }.flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DiaryUiState.loading())

    fun onSelectDate(date: DiaryDate) {
        _selectedDate.value = date
    }

}

