package dev.masalimov.nutritiontracker.feature.diary

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.masalimov.nutritiontracker.core.ui.NutritionTrackerTheme
import dev.masalimov.nutritiontracker.core.ui.logD
import dev.masalimov.nutritiontracker.domain.diary.model.DiaryDate
import dev.masalimov.nutritiontracker.feature.diary.components.Calendar
import dev.masalimov.nutritiontracker.feature.diary.components.CaloriesCard
import dev.masalimov.nutritiontracker.feature.diary.components.DateHeader
import dev.masalimov.nutritiontracker.feature.diary.components.EatenFood
import dev.masalimov.nutritiontracker.feature.diary.components.SuggestedFood
import dev.masalimov.nutritiontracker.feature.diary.components.calendarPreview
import kotlinx.serialization.Serializable


@Serializable
object DiaryScreenRoute

@Composable
fun DiaryScreen(
    viewModel: DiaryViewModel = hiltViewModel(),
    onLogFoodClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
) {
    val context = LocalContext.current

    val calendarUiState by viewModel.calendarUiState.collectAsStateWithLifecycle()
    val diaryInfoUiState by viewModel.diaryInfoUiState.collectAsStateWithLifecycle()

    logD("UI: calendarUiState: $calendarUiState")
    logD("UI: diaryInfoUiState: $diaryInfoUiState")
    when (calendarUiState) {
        is CalendarUiState.Calendar -> logD("Calendar loaded: ${(calendarUiState as CalendarUiState.Calendar).uiModels.size}")
        CalendarUiState.Loading -> logD("Calendar loading")
    }
    when (diaryInfoUiState) {
        is DiaryInfoUiState.DiaryInfo -> logD("Diary info loaded: ${(diaryInfoUiState as DiaryInfoUiState.DiaryInfo).eatenFoodList.size}")
        DiaryInfoUiState.Loading -> logD("Diary info loading")
        is DiaryInfoUiState.Error -> logD("Diary info error")
    }
    logD("-----")

    if (diaryInfoUiState is DiaryInfoUiState.Error) {
        LaunchedEffect(Unit) {
            Toast.makeText(
                context, (diaryInfoUiState as DiaryInfoUiState.Error).errorMessage, Toast.LENGTH_LONG
            ).show()
        }
    }



    val addFoodUiState by viewModel.addFoodUiState.collectAsStateWithLifecycle()
    if (addFoodUiState is AddFoodUiState.Error) {
        LaunchedEffect(Unit) {
            Toast.makeText(
                context, "Failed to add food", Toast.LENGTH_LONG
            ).show()
            viewModel.addFoodErrorShowed()
        }
    }
    DiaryContent(
        calendarUiState,
        diaryInfoUiState,
        addFoodUiState,
        onDayClick = viewModel::onSelectDate,
        onLogFoodClick = onLogFoodClick,
        onSettingsClick = onSettingsClick,
    )
}

@Composable
private fun DiaryContent(
    calendarUiState: CalendarUiState,
    diaryInfoUiState: DiaryInfoUiState,
    addFoodUiState: AddFoodUiState,
    onDayClick: (DiaryDate) -> Unit = {},
    onLogFoodClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
) {
    Scaffold(
        floatingActionButton = {
            if (addFoodUiState !is AddFoodUiState.Loading)
                ExtendedFloatingActionButton(
                    onClick = onLogFoodClick
                ) {
                    Text("Log food")
                }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background,
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag(DIARY_CONTENT_LIST_TEST_TAG)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                item(key = "date_header") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        DateHeader(
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
                            calendarUiState,
                        )
                        IconButton(
                            modifier = Modifier.padding(top = 16.dp),
                            colors = androidx.compose.material3.IconButtonDefaults.iconButtonColors(
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            onClick = onSettingsClick
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings screen"
                            )
                        }
                    }
                }

                item(key = "date_list") {
                    Calendar(
                        modifier = Modifier.fillMaxWidth(),
                        calendarUiState = calendarUiState,
                        onDayClick = onDayClick
                    )
                }

                item(key = "calories_card") {
                    CaloriesCard(
                        diaryInfoUiState,
                    )
                }

                item(key = "eaten_food_section") {
                    EatenFood(diaryInfoUiState)
                }

                item(key = "suggested_food_section") {
                    SuggestedFood(diaryInfoUiState)
                }
            }
        }
        if (addFoodUiState is AddFoodUiState.Loading) {
            AddFoodLoading()
        }
    }
}

@Composable
private fun AddFoodLoading() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.8f)
            )
            .clickable(enabled = false) {},
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text("Adding food...", style = MaterialTheme.typography.headlineMedium)
            CircularProgressIndicator(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .size(24.dp)
            )
        }
    }
}

internal const val DIARY_CONTENT_LIST_TEST_TAG = "DiaryContentList"

private val diaryInfoUiStatePreview = DiaryInfoUiState.DiaryInfo(
    eatenFoodList = listOf(
        EatenFoodUiModel(
            name = "Chicken breast",
            quantityGram = 150.0,
            caloriesEaten = 240,
            caloriesPer100g = 160
        ),
        EatenFoodUiModel(
            name = "Greek yogurt",
            quantityGram = 200.0,
            caloriesEaten = 120,
            caloriesPer100g = 60
        ),
        EatenFoodUiModel(
            name = "Avocado toast",
            quantityGram = 120.0,
            caloriesEaten = 280,
            caloriesPer100g = 233
        )
    ),
    suggestedFoodList = listOf(
        SuggestedFoodUiModel(name = "Apple", caloriesPer100g = 52.0),
        SuggestedFoodUiModel(name = "Cottage cheese", caloriesPer100g = 98.0),
        SuggestedFoodUiModel(name = "Oatmeal", caloriesPer100g = 68.0),
    ),
    caloriesEatenTotal = 640,
    goalCaloriesPerDay = 2000,
)


@Preview(showBackground = true)
@Composable
private fun LoadingCalendar_LoadingInfo() {
    NutritionTrackerTheme {
        DiaryContent(
            addFoodUiState = AddFoodUiState.Idle,
            calendarUiState = CalendarUiState.Loading,
            diaryInfoUiState = DiaryInfoUiState.Loading,
        )
    }
}


@Preview(showBackground = true)
@Composable
private fun ReadyCalendar_LoadingInfo() {
    NutritionTrackerTheme {
        DiaryContent(
            addFoodUiState = AddFoodUiState.Idle,
            calendarUiState = calendarPreview,
            diaryInfoUiState = DiaryInfoUiState.Loading,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ReadyCalendar_ReadyInfo() {
    NutritionTrackerTheme {
        DiaryContent(
            addFoodUiState = AddFoodUiState.Idle,
            calendarUiState = calendarPreview,
            diaryInfoUiState = diaryInfoUiStatePreview,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadingCalendar_ReadyInfo() {
    NutritionTrackerTheme {
        DiaryContent(
            addFoodUiState = AddFoodUiState.Idle,
            calendarUiState = CalendarUiState.Loading,
            diaryInfoUiState = diaryInfoUiStatePreview,
        )
    }
}


@Preview(showBackground = true)
@Composable
private fun ReadyCalendar_ReadyInfo_AddFood() {
    NutritionTrackerTheme {
        DiaryContent(
            addFoodUiState = AddFoodUiState.Loading,
            calendarUiState = calendarPreview,
            diaryInfoUiState = diaryInfoUiStatePreview,
        )
    }
}
