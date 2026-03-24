package dev.masalimov.nutritiontracker.feature.diary

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.toJavaLocalDate
import kotlinx.serialization.Serializable
import java.time.format.DateTimeFormatter


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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        CurrentDate(uiState.date)
        EatenFood(uiState.foodList, uiState.isLoading)
        SuggestedFood(uiState.suggestedFoodList)
    }
}

@Composable
private fun CurrentDate(
    date: DateUiModel? = null,
) {
    val androidLocale = LocalConfiguration.current.locales[0]
    val formatter = remember(androidLocale) {
        DateTimeFormatter.ofPattern("EEEE, dd MMMM").withLocale(androidLocale)
    }
    if (date == null) {
        Text(text = "Loading date...")
        return
    }
    Text(
        text = date.date.toJavaLocalDate().format(formatter) ?: "",
        style = MaterialTheme.typography.titleLarge
    )
}

@Composable
private fun EatenFood(
    eatenFood: List<FoodUiModel>,
    isLoading: Boolean,
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "eaten_header") {
            Text(text = "Eaten food", style = MaterialTheme.typography.titleMedium)
        }
        if (isLoading) {
            item(key = "eaten_loading") {
                Text(text = "Loading eaten food...")
            }
        }
        items(
            items = eatenFood,
            key = { it.id }
        ) { food ->
            DiaryListItem(food)
        }
    }
}

@Composable
private fun SuggestedFood(
    suggestedFood: List<FoodUiModel>,
) {
    Column {
        Text(text = "Suggested food", style = MaterialTheme.typography.titleMedium)
        if (suggestedFood.isEmpty()) {
            Text(text = "No suggested food")
            return
        }
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            suggestedFood.forEach { food ->
                item(key = food.id) {
                    Box(
                        modifier = Modifier
                            .border(
                                BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                shape = RoundedCornerShape(50)
                            )
                            .padding(
                                horizontal = 12.dp, vertical = 6.dp,
                            )
                    ) {
                        Text(
                            text = food.name,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun DiaryListItem(item: FoodUiModel) {
    Text(text = item.name)
}

