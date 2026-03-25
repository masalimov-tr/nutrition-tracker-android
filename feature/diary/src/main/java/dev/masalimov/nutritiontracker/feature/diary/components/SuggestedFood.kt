package dev.masalimov.nutritiontracker.feature.diary.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.unit.dp
import dev.masalimov.nutritiontracker.core.ui.components.PillChip
import dev.masalimov.nutritiontracker.feature.diary.SuggestedFoodUiModel

@Composable
internal fun SuggestedFood(
    suggestedFood: List<SuggestedFoodUiModel>,
) {
    Column {
        Text(
            text = "Suggested meals",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.Companion.padding(bottom = 12.dp)
        )

        if (suggestedFood.isEmpty()) {
            Card(
                modifier = Modifier.Companion.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "No suggestions available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.Companion.padding(16.dp)
                )
            }
            return
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.Companion.fillMaxWidth()
        ) {
            items(
                items = suggestedFood,
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
