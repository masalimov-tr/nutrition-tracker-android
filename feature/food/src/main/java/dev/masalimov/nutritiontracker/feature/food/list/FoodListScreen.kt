package dev.masalimov.nutritiontracker.feature.food.list

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.masalimov.nutritiontracker.core.ui.NutritionTrackerTheme
import dev.masalimov.nutritiontracker.core.ui.components.AppCard
import dev.masalimov.nutritiontracker.core.ui.components.AppSearchBar
import kotlinx.serialization.Serializable


@Serializable
object FoodListRoute

@Composable
fun FoodListScreen(
    modifier: Modifier = Modifier,
    viewModel: FoodViewModel,
    onItemClick: (Long) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    FoodListContent(modifier, uiState, onItemClick, viewModel::onQueryChanged)
}

@Composable
private fun FoodListContent(
    modifier: Modifier = Modifier,
    uiState: FoodListUiState,
    onItemClick: (Long) -> Unit = {},
    onQueryChanged: (String) -> Unit = {},
    query: String = "",
) {
    var query by rememberSaveable { mutableStateOf(query) }

    Surface(
        modifier = modifier
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        LazyColumn(
            modifier = modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
        ) {
            item {
                AppSearchBar(
                    query = query,
                    onQueryChange = {
                        query = it
                        onQueryChanged(it)
                    },
                    isLoading = uiState.isLoading,
                    errorMessage = uiState.errorMessage,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                )
            }
            if (query.isNotEmpty() && uiState.foodList.isEmpty())
                item {
                    EmptyListPlaceholder(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    )
                }
            else
                uiState.foodList.forEachIndexed { index, item ->
                    item(key = item.id) {
                        FoodListItem(
                            modifier = Modifier.fillMaxWidth(),
                            item = item,
                            onClick = { onItemClick(item.id) },
                        )
                        if (index != uiState.foodList.lastIndex) {
                            HorizontalDivider(
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                            )
                        }
                    }
                }
        }
    }
}

@Composable
private fun EmptyListPlaceholder(
    modifier: Modifier = Modifier
) {
    Text(modifier = modifier, text = "No food found")
}

@Composable
private fun FoodListItem(
    item: FoodUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        borderStroke = null,
        cornerRadius = 0.dp,
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Spacer(modifier = Modifier.height(4.dp))
            val calories = item.caloriesPer100g
            Text(
                text = "$calories kcal / 100g",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = MaterialTheme.typography.labelSmall.fontSize * 0.7f
                ),
            )
        }
    }
}

@Preview
@Composable
private fun Empty() {
    NutritionTrackerTheme {
        FoodListContent(uiState = FoodListUiState.empty(), query = "App")
    }
}

@Preview(showBackground = true)
@Composable
private fun FoodListScreenPreview() {
    val previewItems = listOf(
        FoodUiModel(1, "Apple", 52.0),
        FoodUiModel(2, "Banana", 96.0),
        FoodUiModel(3, "Orange", 47.0),
        FoodUiModel(4, "Apple", 52.0),
        FoodUiModel(5, "Banana", 96.0),
        FoodUiModel(6, "Orange", 47.0),
        FoodUiModel(7, "Banana", 96.0),
        FoodUiModel(8, "Orange", 47.0),
    )
    NutritionTrackerTheme {
        FoodListContent(uiState = FoodListUiState(previewItems, false))
    }
}