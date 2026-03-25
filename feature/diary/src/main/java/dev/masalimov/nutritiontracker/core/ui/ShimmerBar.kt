package dev.masalimov.nutritiontracker.core.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

private const val SHIMMER_ALPHA_HIGH = 0.6f
private const val SHIMMER_ALPHA_LOW = 0.3f
private const val SHIMMER_TRANSLATE_TARGET = 1000f
private const val SHIMMER_ANIMATION_DURATION_MS = 1500
private const val SHIMMER_GRADIENT_OFFSET_DELTA = 200f
private const val SHIMMER_TRANSITION_LABEL = "shimmer"
private const val SHIMMER_TRANSLATE_LABEL = "shimmerTranslate"

@Composable
fun ShimmerBar(
    modifier: Modifier,
    shimmerColors: List<Color> = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = SHIMMER_ALPHA_HIGH),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = SHIMMER_ALPHA_LOW),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = SHIMMER_ALPHA_HIGH)
    ),
) {
    val brush = rememberShimmerBrush(shimmerColors)
    Box(
        modifier = modifier
            .background(brush = brush)
    )
}

@Composable
private fun rememberShimmerBrush(
    shimmerColors: List<Color> 
): Brush {
    val transition = rememberInfiniteTransition(label = SHIMMER_TRANSITION_LABEL)
    val translate by transition.animateFloat(
        initialValue = 0f,
        targetValue = SHIMMER_TRANSLATE_TARGET,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = SHIMMER_ANIMATION_DURATION_MS, easing = LinearEasing)
        ),
        label = SHIMMER_TRANSLATE_LABEL
    )

    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(x = translate - SHIMMER_GRADIENT_OFFSET_DELTA, y = 0f),
        end = Offset(x = translate, y = 0f)
    )
}
