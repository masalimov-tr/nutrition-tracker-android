package dev.masalimov.nutritiontracker.feature.diary.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.masalimov.nutritiontracker.feature.diary.DateUiModel
import kotlinx.datetime.toJavaLocalDate
import java.time.format.DateTimeFormatter

@Composable
internal fun DateHeader(
    date: DateUiModel? = null,
) {
    val androidLocale = LocalConfiguration.current.locales[0]
    val formatter = remember(androidLocale) {
        DateTimeFormatter.ofPattern("EEEE, dd MMMM").withLocale(androidLocale)
    }

    if (date?.date != null) {
        Text(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
            text = date.date.toJavaLocalDate().format(formatter),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }

}