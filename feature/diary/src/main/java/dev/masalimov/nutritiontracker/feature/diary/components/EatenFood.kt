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
import androidx.compose.ui.unit.dp
import dev.masalimov.nutritiontracker.core.ui.ShimmerBar
import dev.masalimov.nutritiontracker.core.ui.components.AppSurfaceCard
import dev.masalimov.nutritiontracker.feature.diary.EatenFoodUiModel

@Composable
internal fun EatenFood(
    eatenFood: List<EatenFoodUiModel>,
    isLoading: Boolean,
) {
    Column {
        Text(
            text = "Today's meals",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.Companion.padding(bottom = 8.dp)
        )
        if (isLoading) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                repeat(2) {
                    DiaryListItemPlaceholder()
                }
            }
            return
        }
        if (eatenFood.isEmpty()) {
            Text(
                text = "No meals added yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.Companion.padding(vertical = 8.dp)
            )
            return
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            eatenFood.forEach { food ->
                DiaryListItem(food)
            }
        }
    }
}



@Composable
private fun DiaryListItem(item: EatenFoodUiModel) {
    AppSurfaceCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
            )
            val calories = item.caloriesPer100g
            Text(
                text = "$calories kcal/100g",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}



@Composable
private fun DiaryListItemPlaceholder() {
    AppSurfaceCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
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
                        .clip(RoundedCornerShape(6.dp))
                )
                Spacer(modifier = Modifier.height(8.dp))
                ShimmerBar(
                    modifier = Modifier
                        .height(14.dp)
                        .fillMaxWidth(0.4f)
                        .clip(RoundedCornerShape(6.dp))
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            ShimmerBar(
                modifier = Modifier
                    .height(16.dp)
                    .width(110.dp)
                    .clip(RoundedCornerShape(6.dp))
            )
        }
    }
}
