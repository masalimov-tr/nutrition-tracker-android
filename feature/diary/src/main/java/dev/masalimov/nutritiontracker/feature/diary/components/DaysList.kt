package dev.masalimov.nutritiontracker.feature.diary.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.masalimov.nutritiontracker.core.ui.NutritionTrackerTheme
import dev.masalimov.nutritiontracker.domain.diary.model.DiaryDate
import dev.masalimov.nutritiontracker.feature.diary.DateUiModel
import kotlinx.datetime.toJavaLocalDate
import java.time.format.DateTimeFormatter


@Composable
fun DateList(
    modifier: Modifier = Modifier,
    dateList: List<DateUiModel> = emptyList(),
    onDayClick: (DiaryDate) -> Unit = {},
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = dateList,
            key = { it.date.date.toEpochDays() }
        ) {
            DayButton(
                date = it.date,
                selected = it.isSelected,
                onClick = { onDayClick(it.date) }
            )
        }
    }
}

@Composable
fun DayButton(
    date: DiaryDate,
    selected: Boolean,
    caloriesOver: Boolean = false,
    onClick: () -> Unit = {},
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

    Box(
        modifier = Modifier
            .background(
                color = surfaceColor,
                shape = RoundedCornerShape(30.dp)
            )
            .padding(horizontal = 8.dp, vertical = 16.dp)
            .clickable(onClick = onClick),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = date.date.toJavaLocalDate().format(dayFormatter),
                style = MaterialTheme.typography.labelMedium,
                color = textColor,
            )
            Text(
                text = date.date.toJavaLocalDate().format(monthFormatter),
                style = MaterialTheme.typography.labelSmall,
                color = textColor,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .background(
                        shape = CircleShape,
                        color = if (!selected && caloriesOver)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.primary
                    )
                    .size(6.dp)
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun DayButtonPreview() {
    NutritionTrackerTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DayButton(DiaryDate.today().previousDay(), selected = false, caloriesOver = true)
            DayButton(DiaryDate.today(), selected = false, caloriesOver = false)
            DayButton(DiaryDate.today().plusDays(1), false)
            DayButton(DiaryDate.today().plusDays(2), true)
            DayButton(DiaryDate.today().plusDays(3), false)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DateListPreview() {
    NutritionTrackerTheme {
        DateList()
    }
}