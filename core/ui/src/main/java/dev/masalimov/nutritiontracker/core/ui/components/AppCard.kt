package dev.masalimov.nutritiontracker.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    cornerRadius : Dp = 20.dp,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    borderStroke: BorderStroke? = BorderStroke(1.dp, contentColor.copy(alpha = 0.12f)),
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.combinedClickable(
            interactionSource = remember { MutableInteractionSource() },
            role = Role.Button,
            onClick = onClick,
            onLongClick = onLongClick,
        ),
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
        border = borderStroke,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        content()
    }
}
