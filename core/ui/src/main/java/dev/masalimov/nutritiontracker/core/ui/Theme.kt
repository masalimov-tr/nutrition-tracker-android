package dev.masalimov.nutritiontracker.core.ui

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryGreen,
    onPrimary = OnPrimary,
    secondary = SecondarySand,
    background = Color(0xFF0F1220),
    onBackground = Color(0xFFE6E9F7),
    surface = Color(0xFF171B2B),
    onSurface = Color(0xFFE6E9F7),
    surfaceVariant = Color(0xFF222840),
    onSurfaceVariant = Color(0xFFB6C0E3),
    outline = Outline,
    primaryContainer = Color(0xFF2B3A88),
    onPrimaryContainer = Color(0xFFE1E6FF),
    secondaryContainer = Color(0xFF1F274F),
    onSecondaryContainer = Color(0xFFD4DAFF),
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = OnPrimary,
    secondary = SecondarySand,
    background = BackgroundCream,
    onBackground = OnSurface,
    surface = SurfaceIvory,
    onSurface = OnSurface,
    surfaceVariant = SecondaryContainer,
    onSurfaceVariant = OnSurfaceVariant,
    outline = Outline,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
)

@Composable
fun NutritionTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}