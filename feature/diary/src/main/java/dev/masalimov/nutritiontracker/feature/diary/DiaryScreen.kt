package dev.masalimov.nutritiontracker.feature.diary

import android.R.attr.text
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.masalimov.nutritiontracker.domain.DiaryDate
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import androidx.compose.ui.platform.LocalConfiguration
import kotlinx.datetime.format
import kotlinx.datetime.toJavaLocalDate
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

    Box {
        CurrentDate(uiState.date)
        LoadingContent(uiState.isLoading)
        EatenFood(uiState.foodList)
        SuggestedFood(uiState.suggestedFoodList)
    }
}

@Composable
private fun LoadingContent(
    isLoading: Boolean,
) {
    if (isLoading) {
        CircularProgressIndicator()
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
    Text(text = date.date.toJavaLocalDate().format(formatter) ?: "")
}

@Composable
private fun EatenFood(
    eatenFood: List<FoodUiModel>,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Text(text = "Eaten food")
        }
        eatenFood.forEach { item ->
            item(key = item.id) {
                DiaryListItem(item)
            }
        }
    }
}

@Composable
private fun SuggestedFood(
    suggestedFood: List<FoodUiModel>,
) {
    Column {
        Text(text = "Suggested food")
        LazyRow(
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            suggestedFood.forEach { item ->
                item(key = item.id) {
                    DiaryListItem(item)
                }
            }
        }
    }
}


@Composable
private fun DiaryListItem(item: FoodUiModel) {
    Text(text = item.name)
}

