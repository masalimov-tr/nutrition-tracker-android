package dev.masalimov.nutritiontracker.feature.diary.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.masalimov.nutritiontracker.core.ui.NutritionTrackerTheme
import dev.masalimov.nutritiontracker.core.ui.components.AppCard
import dev.masalimov.nutritiontracker.core.ui.components.ShimmerBar
import dev.masalimov.nutritiontracker.feature.diary.DiaryInfoUiState
import dev.masalimov.nutritiontracker.feature.diary.EatenFoodUiModel

@Composable
internal fun EatenFood(
    diaryInfoUiState: DiaryInfoUiState,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
            Text(
                text = "Today's meals",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(bottom = 16.dp)
            )

        if (diaryInfoUiState is DiaryInfoUiState.Loading) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    repeat(4) {
                        DiaryListItemPlaceholder()
                    }
            }
            return
        }


        diaryInfoUiState as DiaryInfoUiState.DiaryInfo
        if (diaryInfoUiState.eatenFoodList.isEmpty()) {
                Text(
                    text = "No meals added yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

        for (item in diaryInfoUiState.eatenFoodList) {
            DiaryListItem(item)
        }
    }

}


@Composable
private fun DiaryListItem(item: EatenFoodUiModel) {
    DiaryItemCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Spacer(modifier = Modifier.height(4.dp))
                val calories = item.caloriesPer100g
                Text(
                    text = "$calories kcal / 100g",
                    style = MaterialTheme.typography.labelMedium,
                )
            }
            Column(
                horizontalAlignment = Alignment.End,
            ) {
                Text(
                    text = "${item.caloriesEaten} kkal",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${item.quantityGram.toInt()} g",
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}

@Composable
private fun DiaryListItemPlaceholder() {
    val shimmerColor = MaterialTheme.colorScheme.onSurfaceVariant
    DiaryItemCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                ShimmerBar(
                    modifier = Modifier
                        .height(20.dp)
                        .fillMaxWidth(0.6f)
                        .clip(RoundedCornerShape(6.dp)),
                    shimmerColor = shimmerColor
                )
                Spacer(modifier = Modifier.height(8.dp))
                ShimmerBar(
                    modifier = Modifier
                        .height(14.dp)
                        .fillMaxWidth(0.4f)
                        .clip(RoundedCornerShape(6.dp)),
                    shimmerColor = shimmerColor
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                horizontalAlignment = Alignment.End,
            ) {
                ShimmerBar(
                    modifier = Modifier
                        .height(16.dp)
                        .width(50.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    shimmerColor = shimmerColor
                )
                Spacer(modifier = Modifier.height(8.dp))

                ShimmerBar(
                    modifier = Modifier
                        .height(16.dp)
                        .width(110.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    shimmerColor = shimmerColor
                )
            }
        }
    }
}

@Composable
private fun DiaryItemCard(
    content: @Composable () -> Unit,
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        cornerRadius = 12.dp,
        borderStroke = null,
    ) {
        content()
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadedPreview() {
    NutritionTrackerTheme {
        EatenFood(
            DiaryInfoUiState.DiaryInfo(
                listOf(
                    EatenFoodUiModel(
                        name = "Apple",
                        quantityGram = 150.0,
                        caloriesEaten = 78,
                        caloriesPer100g = 52,
                    ),
                    EatenFoodUiModel(
                        name = "Grilled Chicken Breast",
                        quantityGram = 200.0,
                        caloriesEaten = 330,
                        caloriesPer100g = 165,
                    ),
                    EatenFoodUiModel(
                        name = "Greek Yogurt",
                        quantityGram = 100.0,
                        caloriesEaten = 59,
                        caloriesPer100g = 59,
                    ),
                    EatenFoodUiModel(
                        name = "Lorem ipsum dolor sit amet lorem ipsum dolor sit amet",
                        quantityGram = 100.0,
                        caloriesEaten = 59,
                        caloriesPer100g = 59,
                    ),
                ),
                emptyList(),
                0, 0,
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadingPreview() {
    NutritionTrackerTheme {
        EatenFood(
            DiaryInfoUiState.Loading
        )
    }
}
