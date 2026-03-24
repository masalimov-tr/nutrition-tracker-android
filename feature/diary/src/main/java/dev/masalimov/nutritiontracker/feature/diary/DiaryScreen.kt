package dev.masalimov.nutritiontracker.feature.diary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.serialization.Serializable


@Serializable
object DiaryScreenRoute

@Composable
fun DiaryScreen(
    viewModel: DiaryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.diaryUiState.collectAsStateWithLifecycle()
    DiaryContent(uiState)
}

@Composable
private fun DiaryContent(uiState: DiaryUiState) {
    Box {
        if (uiState.isLoading) {
            CircularProgressIndicator()
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            uiState.foodList.forEach { item ->
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

