package dev.masalimov.nutritiontracker.feature.diary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.masalimov.nutritiontracker.core.ui.NutritionTrackerTheme
import dev.masalimov.nutritiontracker.domain.diary.model.DiaryDate
import dev.masalimov.nutritiontracker.feature.diary.components.CaloriesCard
import dev.masalimov.nutritiontracker.feature.diary.components.DateHeader
import dev.masalimov.nutritiontracker.feature.diary.components.DateList
import dev.masalimov.nutritiontracker.feature.diary.components.EatenFood
import dev.masalimov.nutritiontracker.feature.diary.components.SuggestedFood
import kotlinx.serialization.Serializable


@Serializable
object DiaryScreenRoute

@Composable
fun DiaryScreen(
    viewModel: DiaryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DiaryContent(uiState, viewModel::onSelectDate)
}

@Composable
private fun DiaryContent(
    uiState: DiaryUiState,
    onDayClick: (DiaryDate) -> Unit = {},
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
                DateHeader(
                    Modifier.fillMaxWidth().padding(top = 32.dp),
                    uiState.dateList.find { it.isSelected },
                    )
            }

            item(key = "date_list") {
                DateList(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    uiState.dateList,
                    onDayClick = onDayClick
                )
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


@Preview(showBackground = true, name = "Diary Screen")
@Composable
private fun DiaryScreenPreview() {
    NutritionTrackerTheme {
        DiaryContent(
            uiState = DiaryUiState(
                isLoading = false,
                dateList = calendar,
                eatenFoodList = listOf(
                    EatenFoodUiModel(
                        name = "Chicken breast",
                        quantityGram = 150.0,
                        caloriesEaten = 240,
                        caloriesPer100g = 160
                    ),
                    EatenFoodUiModel(
                        name = "Greek yogurt",
                        quantityGram = 200.0,
                        caloriesEaten = 120,
                        caloriesPer100g = 60
                    ),
                    EatenFoodUiModel(
                        name = "Avocado toast",
                        quantityGram = 120.0,
                        caloriesEaten = 280,
                        caloriesPer100g = 233
                    )
                ),
                suggestedFoodList = listOf(
                    SuggestedFoodUiModel(name = "Apple", caloriesPer100g = 52.0),
                    SuggestedFoodUiModel(name = "Cottage cheese", caloriesPer100g = 98.0),
                    SuggestedFoodUiModel(name = "Oatmeal", caloriesPer100g = 68.0),
                ),
                caloriesEatenTotal = 640,
                goalCaloriesPerDay = 2000,
            )
        )
    }
}
