package dev.masalimov.nutritiontracker.feature.diary.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.masalimov.nutritiontracker.core.ui.NutritionTrackerTheme
import dev.masalimov.nutritiontracker.core.ui.components.ShimmerBar
import dev.masalimov.nutritiontracker.core.ui.components.ShimmerCircle

@Composable
internal fun CaloriesCard(
    caloriesPerDay: Int? = null,
    consumedCalories: Int? = null,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                RoundedCornerShape(32.dp)
            ),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
        ) {
            Text(
                text = "Daily calories",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            if (caloriesPerDay != null && consumedCalories != null) {
                CaloriesCardContent(caloriesPerDay, consumedCalories)
            } else {
                val shimmerColor = MaterialTheme.colorScheme.onSurfaceVariant

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ShimmerCircle(
                        modifier = Modifier.size(72.dp),
                        shimmerColor = shimmerColor
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        ShimmerBar(
                            modifier = Modifier
                                .height(20.dp)
                                .fillMaxWidth(0.4f)
                                .clip(RoundedCornerShape(6.dp)),
                            shimmerColor = shimmerColor
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        ShimmerBar(
                            modifier = Modifier
                                .height(20.dp)
                                .fillMaxWidth(0.6f)
                                .clip(RoundedCornerShape(6.dp)),
                            shimmerColor = shimmerColor
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun CaloriesCardContent(
    caloriesPerDay: Int,
    consumedCalories: Int,
) {
    val progress = if (caloriesPerDay > 0) {
        consumedCalories.toFloat() / caloriesPerDay.toFloat()
    } else 0f
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
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
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    modifier = Modifier.alignByBaseline(),
                    text = " / $caloriesPerDay kcal",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            val remaining = (caloriesPerDay - consumedCalories)
            if (remaining >= 0)
                Text(
                    text = "Remaining: $remaining kcal",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            else
                Text(
                    text = "Over: ${-remaining} kcal",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
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
            progress = { progress.coerceIn(0f, 1f) },
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 8.dp,
            modifier = Modifier.fillMaxSize()
        )

        // Overflow
        run {
            val overflow = (progress - 1f).coerceIn(0f, 1f)
            if (overflow > 0f)
                CircularProgressIndicator(
                    progress = { overflow },
                    color = MaterialTheme.colorScheme.error,
                    strokeWidth = 8.dp,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                )
        }
        // Center label
        Column(
            modifier = Modifier.matchParentSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.labelLarge,
                color = if (progress <= 1f)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.error
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
internal fun CaloriesCard1Preview() {
    NutritionTrackerTheme {
        CaloriesCard(
            caloriesPerDay = 2500,
            consumedCalories = 1850
        )
    }
}

@Preview(showBackground = true)
@Composable
internal fun CaloriesCard2Preview() {
    NutritionTrackerTheme {
        CaloriesCard(
            caloriesPerDay = 2500,
            consumedCalories = 2500
        )
    }
}

@Preview(showBackground = true)
@Composable
internal fun CaloriesCard3Preview() {
    NutritionTrackerTheme {
        CaloriesCard(
            caloriesPerDay = 2500,
            consumedCalories = 2800
        )
    }
}

@Preview(showBackground = true)
@Composable
internal fun CaloriesCardLoadingPreview() {
    NutritionTrackerTheme {
        CaloriesCard()
    }
}

@Preview(showBackground = true)
@Composable
internal fun CaloriesProgressOverflowPreview() {
    NutritionTrackerTheme {
        CaloriesProgress(
            progress = 1.2f
        )
    }
}

@Preview(showBackground = true)
@Composable
internal fun CaloriesProgressPreview() {
    NutritionTrackerTheme {
        CaloriesProgress(
            progress = 0.5f
        )
    }
}

