package dev.masalimov.nutritiontracker.feature.diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.masalimov.nutritiontracker.domain.DiaryDate
import dev.masalimov.nutritiontracker.domain.GetDiaryByDateUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class FoodUiModel(
    val id: Long,
    val name: String,
)

data class DiaryUiState(
    val date: DiaryDate,
    val foodList: List<FoodUiModel>,
    val isLoading: Boolean = false,
)


@HiltViewModel
class DiaryViewModel @Inject constructor(
    private val getDiaryByDateUseCase: GetDiaryByDateUseCase,
) : ViewModel() {

    private val _diaryUiState: MutableStateFlow<DiaryUiState> =
        MutableStateFlow(DiaryUiState(DiaryDate.today(), emptyList()))
    val diaryUiState: StateFlow<DiaryUiState> = _diaryUiState.asStateFlow()

    init {
        viewModelScope.launch {
            _diaryUiState.update {
                it.copy(isLoading = true)
            }
            val diary = getDiaryByDateUseCase(DiaryDate.today())
            withContext(Dispatchers.Default) {
                val uiModels = diary.map {
                    FoodUiModel(
                        id = it.id.id,
                        name = it.name,
                    )
                }
                _diaryUiState.update {
                    it.copy(foodList = uiModels, isLoading = false)
                }
            }

        }
    }
}