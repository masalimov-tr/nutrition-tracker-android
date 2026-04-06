package dev.masalimov.nutritiontracker.feature.food.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
    onEditClick: (Long) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    FoodListContent(
        modifier = modifier,
        uiState = uiState,
        onItemClick = onItemClick,
        onEditClick = onEditClick,
        onItemDeleteClick = viewModel::onItemDeleteClick,
        onQueryChanged = viewModel::onQueryChanged,
    )
}

@Composable
private fun FoodListContent(
    modifier: Modifier = Modifier,
    uiState: FoodListUiState,
    onItemClick: (Long) -> Unit = {},
    onEditClick: (Long) -> Unit = {},
    onItemDeleteClick: (Long) -> Unit = {},
    onQueryChanged: (String) -> Unit = {},
) {
    var query by rememberSaveable { mutableStateOf("") }

    Scaffold(
        topBar = {
            AppSearchBar(
                query = query,
                onQueryChange = {
                    query = it
                    onQueryChanged(it)
                },
                isLoading = uiState is FoodListUiState.Loading,
                errorMessage = (uiState as? FoodListUiState.Error)?.errorMessage,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 24.dp)
            )
        }
    ) { paddingValues ->

        Surface(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background,
        ) {
            LazyColumn(
                modifier = modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
            ) {
                if (uiState is FoodListUiState.Initial || (uiState is FoodListUiState.Loading && uiState.foodList.isEmpty())) {
                    return@LazyColumn
                }
                fun foodListSectionOrEmpty(
                    sectionTitle: String,
                    emptyText: String,
                    foodList: List<FoodUiModel>,
                ) {
                    if (uiState is FoodListUiState.Success && foodList.isEmpty()) {
                        item {
                            EmptyState(
                                text = emptyText,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp)
                            )
                        }
                    }
                    if (uiState is FoodListUiState.Success && foodList.isNotEmpty()) {
                        item {
                            SectionHeader(sectionTitle)
                        }
                        itemsIndexed(
                            foodList,
                            key = { _, item -> item.id + item.name.hashCode() },
                            contentType = { _, _ -> sectionTitle }
                        ) { index, item ->
                            FoodListItem(
                                modifier = Modifier.fillMaxWidth(),
                                item = item,
                                onClick = { onItemClick(item.id) },
                                onEditClick = { onEditClick(item.id) },
                                onDeleteClick = { onItemDeleteClick(item.id) },
                            )
                            if (index != foodList.lastIndex) {
                                HorizontalDivider(
                                    thickness = 1.dp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                        alpha = 0.2f
                                    )
                                )
                            }
                        }
                    }
                }
                foodListSectionOrEmpty(
                    sectionTitle = "Saved foods",
                    emptyText = "No saved foods",
                    foodList = (uiState as? FoodListUiState.Success)?.savedFood ?: emptyList()
                )
                if (uiState is FoodListUiState.Success && uiState.searchedFood.isNotEmpty()) {
                    item {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 16.dp),
                            thickness = 2.dp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                alpha = 0.6f
                            )
                        )
                    }
                }
                foodListSectionOrEmpty(
                    sectionTitle = "Searched foods",
                    emptyText = "No searched foods",
                    foodList = (uiState as? FoodListUiState.Success)?.searchedFood ?: emptyList()
                )
            }
        }
    }
}


@Composable
private fun SectionHeader(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        modifier = modifier.padding(vertical = 8.dp),
        text = text,
        style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold
        ),
        color = MaterialTheme.colorScheme.onPrimaryContainer,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FoodListItem(
    modifier: Modifier = Modifier,
    item: FoodUiModel,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit = {},
    showDialog: Boolean = false,
) {
    var dialog by rememberSaveable { mutableStateOf(showDialog) }
    if (dialog) {
        FoodListItemDialog(
            onDismissRequest = { dialog = false },
            onEditClick = {
                dialog = false
                onEditClick()
            },
            onDeleteClick = {
                dialog = false
                onDeleteClick()
            },
        )
    }
    AppCard(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        borderStroke = null,
        cornerRadius = 0.dp,
        onClick = onClick,
        onLongClick = {
            dialog = true
        },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodListItemDialog(
    onDismissRequest: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
    ) {
        Surface(
            modifier = Modifier,
            shape = MaterialTheme.shapes.large,
            tonalElevation = AlertDialogDefaults.TonalElevation,
        ) {
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
                Text(
                    text = "What would you like to do?",
                    style = MaterialTheme.typography.titleMedium.copy(),
                    modifier = Modifier.align(Alignment.Start),
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.align(Alignment.End),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TextButton(
                        onClick = onEditClick,
                    ) {
                        Text("Edit")
                    }
                    TextButton(
                        onClick = onDeleteClick,
                    ) {
                        Text(
                            "Delete",
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(modifier = modifier, text = text)
}


private val previewItems1 = listOf(
    FoodUiModel(1, "Apple", 52.0),
    FoodUiModel(2, "Banana", 96.0),
    FoodUiModel(3, "Orange", 47.0),
    FoodUiModel(4, "Apple", 52.0),
    FoodUiModel(5, "Banana", 96.0),
    FoodUiModel(6, "Orange", 47.0),
    FoodUiModel(7, "Banana", 96.0),
    FoodUiModel(8, "Orange", 47.0),
)

private val previewItems2 = listOf(
    FoodUiModel(10, "Apple", 52.0),
    FoodUiModel(20, "Banana", 96.0),
    FoodUiModel(30, "Orange", 47.0),
    FoodUiModel(40, "Apple", 52.0),
    FoodUiModel(50, "Banana", 96.0),
    FoodUiModel(60, "Orange", 47.0),
    FoodUiModel(70, "Banana", 96.0),
    FoodUiModel(80, "Orange", 47.0),
)

@Preview(showBackground = true)
@Composable
private fun FoodListScreenPreview() {
    NutritionTrackerTheme {
        FoodListContent(uiState = FoodListUiState.Success(previewItems1, previewItems2))
    }
}

@Preview
@Composable
private fun NoSavedFood() {
    NutritionTrackerTheme {
        FoodListContent(
            uiState = FoodListUiState.Success(emptyList(), previewItems2)
        )
    }
}

@Preview
@Composable
private fun NoSearchedFood() {
    NutritionTrackerTheme {
        FoodListContent(
            uiState = FoodListUiState.Success(previewItems1, emptyList())
        )
    }
}

@Preview
@Composable
private fun Initial() {
    NutritionTrackerTheme {
        FoodListContent(
            uiState = FoodListUiState.Initial
        )
    }
}

@Preview
@Composable
private fun Loading() {
    NutritionTrackerTheme {
        FoodListContent(
            uiState = FoodListUiState.Loading(emptyList())
        )
    }
}

@Preview
@Composable
private fun LoadingWithItems() {
    NutritionTrackerTheme {
        FoodListContent(
            uiState = FoodListUiState.Loading(previewItems1)
        )
    }
}

@Preview
@Composable
private fun ErrorWithItems() {
    NutritionTrackerTheme {
        FoodListContent(
            uiState = FoodListUiState.Error(previewItems1, "Something went wrong")
        )
    }
}

@Preview
@Composable
private fun Dialog() {
    NutritionTrackerTheme {
        FoodListItem(
            item = FoodUiModel(1, "Apple", 52.0),
            onClick = {},
            onEditClick = {},
            showDialog = true,
        )
    }
}
