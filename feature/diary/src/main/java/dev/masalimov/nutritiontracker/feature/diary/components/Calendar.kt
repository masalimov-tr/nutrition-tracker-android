package dev.masalimov.nutritiontracker.feature.diary.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.masalimov.nutritiontracker.core.ui.NutritionTrackerTheme
import dev.masalimov.nutritiontracker.domain.diary.CalorieConsumptionStatus
import dev.masalimov.nutritiontracker.domain.diary.model.DiaryDate
import dev.masalimov.nutritiontracker.feature.diary.CalendarUiState
import dev.masalimov.nutritiontracker.feature.diary.DateUiModel
import kotlinx.datetime.toJavaLocalDate
import java.time.format.DateTimeFormatter


@Composable
fun Calendar(
    modifier: Modifier = Modifier,
    calendarUiState: CalendarUiState,
    onDayClick: (DiaryDate) -> Unit = {},
) {
    if (calendarUiState is CalendarUiState.Loading) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(14) {
                item {
                    DayButton(
                        date = DiaryDate.EMPTY,
                        selected = false,
                        calorieConsumptionStatus = CalorieConsumptionStatus.Unknown,
                    )
                }
            }
        }
        return
    }
    calendarUiState as CalendarUiState.Calendar

    Box(modifier = modifier) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = calendarUiState.uiModels,
                key = { it.date.date.toEpochDays() }
            ) {
                DayButton(
                    date = it.date,
                    selected = it.isSelected,
                    calorieConsumptionStatus = it.calorieConsumptionStatus,
                    onClick = onDayClick,
                )
            }
        }
        EdgeFade(FadeSide.Left)
        EdgeFade(FadeSide.Right)
    }
}

private sealed interface FadeSide {
    data object Left : FadeSide
    data object Right : FadeSide
}

@Composable
private fun BoxScope.EdgeFade(
    fadeSide: FadeSide
) {
    val alignment = when (fadeSide) {
        FadeSide.Left -> Alignment.CenterStart
        FadeSide.Right -> Alignment.CenterEnd
    }
    val colorsForLeftFade = listOf(
        MaterialTheme.colorScheme.background,
        MaterialTheme.colorScheme.background.copy(alpha = 0f),
    )
    val colorsForRightFade = colorsForLeftFade.toList().reversed()
    Box(Modifier.matchParentSize()) { // now we surely have the parent’s measured height
        Box(
            modifier = Modifier
                .align(alignment)
                .width(40.dp)
                .fillMaxHeight()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = when (fadeSide) {
                            FadeSide.Left -> colorsForLeftFade
                            FadeSide.Right -> colorsForRightFade
                        }
                    ),
                    shape = RoundedCornerShape(0.dp)
                )
        )
    }
}

@Composable
private fun DayButton(
    date: DiaryDate,
    selected: Boolean,
    calorieConsumptionStatus: CalorieConsumptionStatus = CalorieConsumptionStatus.Unknown,
    onClick: (DiaryDate) -> Unit = {},
) {
    val androidLocale = LocalConfiguration.current.locales[0]
    val dayFormatter = remember(androidLocale) {
        DateTimeFormatter.ofPattern("d").withLocale(androidLocale)
    }
    val monthFormatter = remember(androidLocale) {
        DateTimeFormatter.ofPattern("MMM").withLocale(androidLocale)
    }

    val surfaceColor = if (selected)
        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
    else
        MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (selected)
        MaterialTheme.colorScheme.onPrimary
    else
        MaterialTheme.colorScheme.onSurfaceVariant

    val shape = RoundedCornerShape(30.dp)

    Box(
        modifier = Modifier
            .clip(shape)
            .background(
                color = surfaceColor,
                shape = shape
            )
            .clickable(onClick = { onClick(date) })
            .padding(horizontal = 8.dp, vertical = 16.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val (date,month) = if (date == DiaryDate.EMPTY)
                "." to "xxx"
            else
                date.date.toJavaLocalDate().dayOfMonth.toString() to date.date.toJavaLocalDate().format(monthFormatter)

            Text(
                text = date,
                style = MaterialTheme.typography.labelMedium,
                color = textColor,
            )
            Text(
                text = month,
                style = MaterialTheme.typography.labelSmall,
                color = textColor,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .background(
                        shape = CircleShape,
                        color = when (calorieConsumptionStatus) {
                            CalorieConsumptionStatus.Over -> MaterialTheme.colorScheme.error
                            CalorieConsumptionStatus.NotOver -> MaterialTheme.colorScheme.primary
                            CalorieConsumptionStatus.Unknown -> MaterialTheme.colorScheme.outline
                        }
                    )
                    .size(6.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadingCalendar() {
    NutritionTrackerTheme {
        Calendar(
            modifier = Modifier.fillMaxWidth(),
            CalendarUiState.Loading,
        )
    }
}

internal val calendarPreview = CalendarUiState.Calendar(
    uiModels = listOf(
        DateUiModel(DiaryDate.today().plusDays(-1), false),
        DateUiModel(DiaryDate.today(), true),
        DateUiModel(DiaryDate.today().plusDays(1), false),
        DateUiModel(DiaryDate.today().plusDays(2), false),
        DateUiModel(DiaryDate.today().plusDays(3), false),
        DateUiModel(DiaryDate.today().plusDays(4), false),
        DateUiModel(DiaryDate.today().plusDays(5), false),
        DateUiModel(DiaryDate.today().plusDays(6), false),
        DateUiModel(DiaryDate.today().plusDays(7), false),
        DateUiModel(DiaryDate.today().plusDays(8), false),
        DateUiModel(DiaryDate.today().plusDays(9), false),
        DateUiModel(DiaryDate.today().plusDays(10), false),
    ),
)

@Preview(showBackground = true)
@Composable
private fun LoadedCalendar() {
    NutritionTrackerTheme {
        Calendar(
            modifier = Modifier.fillMaxWidth(),
            calendarPreview,
        )
    }
}

