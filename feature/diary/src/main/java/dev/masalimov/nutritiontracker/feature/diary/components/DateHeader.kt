package dev.masalimov.nutritiontracker.feature.diary.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.masalimov.nutritiontracker.core.ui.NutritionTrackerTheme
import dev.masalimov.nutritiontracker.core.ui.components.ShimmerBar
import dev.masalimov.nutritiontracker.domain.diary.model.DiaryDate
import dev.masalimov.nutritiontracker.feature.diary.CalendarUiState
import dev.masalimov.nutritiontracker.feature.diary.DateUiModel
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import java.time.format.DateTimeFormatter

@Composable
internal fun DateHeader(
    modifier: Modifier = Modifier,
    calendarUiState: CalendarUiState,
) {
    val androidLocale = LocalConfiguration.current.locales[0]
    val formatterLineOne = remember(androidLocale) {
        DateTimeFormatter.ofPattern("EEEE, dd").withLocale(androidLocale)
    }
    val formatterLineTwo = remember(androidLocale) {
        DateTimeFormatter.ofPattern("MMMM").withLocale(androidLocale)
    }

    when (calendarUiState) {
        is CalendarUiState.Loading -> {
            val shimmerColor = MaterialTheme.colorScheme.onSurfaceVariant
            Column {
                ShimmerBar(
                    modifier = Modifier
                        .height(20.dp)
                        .fillMaxWidth(0.6f)
                        .clip(RoundedCornerShape(6.dp)),
                    shimmerColor = shimmerColor
                )
                Spacer(modifier = Modifier.height(8.dp))
                ShimmerBar(
                    modifier = Modifier
                        .height(14.dp)
                        .fillMaxWidth(0.4f)
                        .clip(RoundedCornerShape(6.dp)),
                    shimmerColor = shimmerColor
                )
            }
        }

        is CalendarUiState.Calendar -> {
            val date = calendarUiState.uiModels.first { it.isSelected }
            Text(
                modifier = modifier,
                text = date.date.date.toJavaLocalDate().format(formatterLineOne) + "\n" +
                        date.date.date.toJavaLocalDate().format(formatterLineTwo),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadingPreview() {
    NutritionTrackerTheme {
        DateHeader(calendarUiState = CalendarUiState.Loading)
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadedPreview() {
    NutritionTrackerTheme {
        DateHeader(
            calendarUiState = CalendarUiState.Calendar(
                uiModels = listOf(
                    DateUiModel(
                        date = DiaryDate.of(LocalDate(2024, 6, 1)),
                        isSelected = true,
                    ),
                    DateUiModel(
                        date = DiaryDate.of(LocalDate(2024, 6, 1)),
                        isSelected = false,
                    ),
                )
            )
        )
    }
}