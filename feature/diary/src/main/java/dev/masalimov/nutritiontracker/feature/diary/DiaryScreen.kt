package dev.masalimov.nutritiontracker.feature.diary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.masalimov.nutritiontracker.feature.diary.components.CaloriesCard
import dev.masalimov.nutritiontracker.feature.diary.components.DateHeader
import dev.masalimov.nutritiontracker.feature.diary.components.EatenFood
import dev.masalimov.nutritiontracker.feature.diary.components.SuggestedFood
import kotlinx.serialization.Serializable


@Serializable
object DiaryScreenRoute

@Composable
fun DiaryScreen(
    viewModel: DiaryViewModel = hiltViewModel(),
) {
    LaunchedEffect(Unit) {
        viewModel.onEvent(DiaryViewModel.DiaryEvent.LoadToday)
    }
    val uiState by viewModel.diaryUiState.collectAsStateWithLifecycle()

    DiaryContent(uiState)
}

@Composable
private fun DiaryContent(
    uiState: DiaryUiState,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            item(key = "date_header") {
                DateHeader(uiState.date)
            }

            item(key = "calories_card") {
                CaloriesCard(
                    caloriesPerDay = uiState.goalCaloriesPerDay,
                    consumedCalories = uiState.caloriesEatenTotal
                )
            }

            item(key = "eaten_food_section") {
                EatenFood(uiState.eatenFoodList, uiState.isLoading)
            }

            item(key = "suggested_food_section") {
                SuggestedFood(uiState.suggestedFoodList)
            }
        }
    }
}
