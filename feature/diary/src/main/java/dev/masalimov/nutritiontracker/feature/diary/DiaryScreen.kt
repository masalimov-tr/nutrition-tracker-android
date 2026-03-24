package dev.masalimov.nutritiontracker.feature.diary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
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

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            item(key = "date_header") {
                DateHeader(uiState.date)
            }

            item(key = "calories_card") {
                val consumed = uiState.eatenFoodList.sumOf { it.caloriesEaten }
                CaloriesCard(
                    caloriesPerDay = uiState.goalCaloriesPerDay,
                    consumedCalories = consumed
                )
            }

            item(key = "eaten_food_section") {
                EatenFood(uiState.eatenFoodList, uiState.isLoading)
            }

            item(key = "suggested_food_section") {
                SuggestedFood(uiState.suggestedFoodList)
            }
        }
    }
}

@Composable
private fun DateHeader(
    date: DateUiModel? = null,
) {
    val androidLocale = LocalConfiguration.current.locales[0]
    val formatter = remember(androidLocale) {
        DateTimeFormatter.ofPattern("EEEE, dd MMMM").withLocale(androidLocale)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
        ) {
            Text(
                text = date?.date?.toJavaLocalDate()?.format(formatter) ?: "Loading…",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = "Today's nutrition",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun CaloriesCard(
    caloriesPerDay: Int? = null,
    consumedCalories: Int? = null,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
        ) {
            Text(
                text = "Daily calories",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            if (caloriesPerDay != null && caloriesPerDay > 0) {
                val consumed = (consumedCalories ?: 0).coerceAtLeast(0)
                val progress = (consumed.toFloat() / caloriesPerDay.toFloat()).coerceIn(0f, 1f)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    Box(modifier = Modifier.size(72.dp)) {
                        // Track
                        CircularProgressIndicator(
                            progress = { 1f },
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.15f),
                            strokeWidth = 8.dp,
                            modifier = Modifier.fillMaxSize()
                        )
                        // Progress
                        CircularProgressIndicator(
                            progress = { progress },
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 8.dp,
                            modifier = Modifier.fillMaxSize()
                        )
                        // Center label
                        Column(
                            modifier = Modifier.matchParentSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${(progress * 100).toInt()}%",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = consumed.toString(),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = " / ${caloriesPerDay} kcal",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                                modifier = Modifier.padding(start = 6.dp, bottom = 2.dp)
                            )
                        }
                        val remaining = (caloriesPerDay - consumed).coerceAtLeast(0)
                        Text(
                            text = "Remaining: ${remaining} kcal",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            } else {
                Text(
                    text = "Loading…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun EatenFood(
    eatenFood: List<EatenFoodUiModel>,
    isLoading: Boolean,
) {
    Column {
        Text(
            text = "Today's meals",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        if (eatenFood.isNotEmpty()) {
            Text(
                text = "${eatenFood.size} items",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        if (isLoading) {
            Text(
                text = "Loading…",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else if (eatenFood.isEmpty()) {
            Text(
                text = "No meals added yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                eatenFood.forEach { food ->
                    DiaryListItem(food)
                }
            }
        }
    }
}

@Composable
private fun SuggestedFood(
    suggestedFood: List<SuggestedFoodUiModel>,
) {
    Column {
        Text(
            text = "Suggested meals",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        if (suggestedFood.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "No suggestions available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            }
            return
        }
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
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
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Text(
            text = food.name,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}


@Composable
private fun DiaryListItem(item: EatenFoodUiModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        shape = RoundedCornerShape(12.dp)
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
                text = "${calories} kcal/100g",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

