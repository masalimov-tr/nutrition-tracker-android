package dev.masalimov.nutritiontracker.feature.diary.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.masalimov.nutritiontracker.core.ui.components.AppSurfaceCard

@Composable
internal fun CaloriesCard(
    caloriesPerDay: Int? = null,
    consumedCalories: Int? = null,
) {
    AppSurfaceCard(
        modifier = Modifier.Companion.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.Companion.padding(20.dp),
        ) {
            Text(
                text = "Daily calories",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Companion.SemiBold),
            )
            if (caloriesPerDay != null && consumedCalories != null) {
                CaloriesCardContent(caloriesPerDay, consumedCalories)
            } else {
                Text(
                    text = "Loading...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.Companion.padding(top = 8.dp)
                )
            }
        }
    }
}


@Composable
private fun CaloriesCardContent(
    caloriesPerDay: Int,
    consumedCalories: Int,
) {
    val progress = (consumedCalories.toFloat() / caloriesPerDay.toFloat()).coerceIn(0f, 1f)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(top = 12.dp)
    ) {
        CaloriesProgress(progress = progress)
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    modifier = Modifier.alignByBaseline(),
                    text = "$consumedCalories",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    modifier = Modifier.alignByBaseline(),
                    text = " / $caloriesPerDay kcal",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Thin,
                )
            }
            val remaining = (caloriesPerDay - consumedCalories).coerceAtLeast(0)
            Text(
                text = "Remaining: $remaining kcal",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun CaloriesProgress(
    progress: Float,
) {
    Box(modifier = Modifier.size(72.dp)) {
        // Track
        CircularProgressIndicator(
            progress = { 1f },
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f),
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
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
