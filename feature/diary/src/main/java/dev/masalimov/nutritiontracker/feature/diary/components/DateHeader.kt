package dev.masalimov.nutritiontracker.feature.diary.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import dev.masalimov.nutritiontracker.core.ui.NutritionTrackerTheme
import dev.masalimov.nutritiontracker.domain.diary.model.DiaryDate
import dev.masalimov.nutritiontracker.feature.diary.DateUiModel
import kotlinx.datetime.toJavaLocalDate
import java.time.format.DateTimeFormatter

@Composable
internal fun DateHeader(
    modifier: Modifier = Modifier,
    date: DateUiModel? = null,
) {
    val androidLocale = LocalConfiguration.current.locales[0]
    val formatterLineOne = remember(androidLocale) {
        DateTimeFormatter.ofPattern("EEEE, dd").withLocale(androidLocale)
    }
    val formatterLineTwo = remember(androidLocale) {
        DateTimeFormatter.ofPattern("MMMM").withLocale(androidLocale)
    }

    if (date?.date != null) {
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

@Preview(showBackground = true)
@Composable
private fun Preview() {
    NutritionTrackerTheme {
        DateHeader(
            date = DateUiModel(
                date = DiaryDate.today(),
                isSelected = true,
            )
        )
    }
}