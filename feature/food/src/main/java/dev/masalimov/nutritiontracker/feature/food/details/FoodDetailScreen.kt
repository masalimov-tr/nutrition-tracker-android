package dev.masalimov.nutritiontracker.feature.food.details

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.masalimov.nutritiontracker.feature.food.list.FoodUiModel

@Composable
fun FoodDetailScreen(
    viewModel: FoodDetailsViewModel,
) {
    val food by viewModel.uiState.collectAsStateWithLifecycle()
    if (food == null) {
        CircularProgressIndicator()
    } else
        FoodDetailContent(food as FoodUiModel)
}

@Composable
private fun FoodDetailContent(
    food: FoodUiModel,
) {
    Box(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = food.name,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(16.dp)
        )
    }
}