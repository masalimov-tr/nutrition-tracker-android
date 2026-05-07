package dev.masalimov.nutritiontracker.feature.diary.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.masalimov.nutritiontracker.core.ui.NutritionTrackerTheme
import dev.masalimov.nutritiontracker.core.ui.components.PillChip
import dev.masalimov.nutritiontracker.feature.diary.DiaryInfoUiState
import dev.masalimov.nutritiontracker.feature.diary.DiaryTestTags
import dev.masalimov.nutritiontracker.feature.diary.SuggestedFoodUiModel

@Composable
internal fun SuggestedFood(
    diaryInfoUiState: DiaryInfoUiState,
) {
    Column(
        modifier = Modifier.testTag(DiaryTestTags.SuggestedFoodSection)
    ) {
        Text(
            text = "Suggested meals",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        if (diaryInfoUiState is DiaryInfoUiState.Loading || diaryInfoUiState is DiaryInfoUiState.Error) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                repeat(3) {
                    PillChip(text = "Loading...")
                }
            }
            return
        }

        diaryInfoUiState as DiaryInfoUiState.DiaryInfo

        if (diaryInfoUiState.suggestedFoodList.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "No suggestions available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            }
            return
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(
                items = diaryInfoUiState.suggestedFoodList,
                key = { it.name + it.caloriesPer100g }
            ) { food ->
                SuggestedFoodChip(food)
            }
        }
    }
}


@Composable
private fun SuggestedFoodChip(food: SuggestedFoodUiModel) {
    PillChip(text = food.name)
}

@Preview(showBackground = true)
@Composable
fun Loading() {
    NutritionTrackerTheme {
        SuggestedFood(
            diaryInfoUiState = DiaryInfoUiState.Loading
        )
    }
}

@Preview(showBackground = true)
@Composable
fun Loaded() {
    NutritionTrackerTheme {
        SuggestedFood(
            diaryInfoUiState = DiaryInfoUiState.DiaryInfo(
                emptyList(),
                listOf(
                    SuggestedFoodUiModel(
                        name = "Apple",
                        caloriesPer100g = 52.0
                    ),
                    SuggestedFoodUiModel(
                        name = "Banana",
                        caloriesPer100g = 100.0
                    ),
                ),
                0, 0,
            )
        )
    }
}
