package dev.masalimov.nutritiontracker.feature.food.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle


@Composable
fun FoodListScreen(
    modifier: Modifier = Modifier,
    viewModel: FoodViewModel,
    onItemClick: (FoodUiModel) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    FoodListContent(modifier, uiState, onItemClick)
}

@Composable
private fun FoodListContent(
    modifier: Modifier = Modifier,
    items: List<FoodUiModel>,
    onItemClick: (FoodUiModel) -> Unit = {},
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items.forEach { item ->
            item(key = item.name) {
                FoodListItem(
                    item = item,
                    onClick = { onItemClick(item) },
                    modifier = Modifier
                )
            }
        }
    }
}

@Composable
private fun FoodListItem(
    item: FoodUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.clickable(onClick = onClick).fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Text(
            text = item.name,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FoodListScreenPreview() {
    val previewItems = listOf(
        FoodUiModel(1,"Apple", 52.0),
        FoodUiModel(2,"Banana", 96.0),
        FoodUiModel(3, "Orange", 47.0)
    )
    FoodListContent(items = previewItems)
}