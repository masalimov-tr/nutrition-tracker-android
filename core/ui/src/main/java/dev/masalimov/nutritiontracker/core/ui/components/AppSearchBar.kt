package dev.masalimov.nutritiontracker.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.masalimov.nutritiontracker.core.ui.NutritionTrackerTheme


@Composable
fun AppSearchBar(
    modifier: Modifier = Modifier,
    query: String = "",
    onQueryChange: (String) -> Unit = {},
    placeholder: String = "Search",
    onClear: () -> Unit = { onQueryChange("") },
    isLoading: Boolean = false,
    errorMessage: String? = null,
) {
    val colors = TextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        disabledContainerColor = MaterialTheme.colorScheme.outline,
        errorContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        cursorColor = MaterialTheme.colorScheme.primary,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        disabledIndicatorColor = Color.Transparent,
        errorIndicatorColor = Color.Transparent,
        focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
        unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
    )

    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(24.dp),
        colors = colors,
        supportingText = {
            if (errorMessage != null)
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodyMedium
                )
        },
        isError = errorMessage != null,
        placeholder = {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                ),
            )
        },
        leadingIcon = {
            Icon(
                Icons.Default.Search,
                contentDescription = "Search Icon",
            )
        },
        trailingIcon = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                if (isLoading)
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 3.dp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                if (query.isNotEmpty()) {
                    IconButton(onClick = onClear) {
                        Icon(
                            Icons.Default.Close,
                            modifier = Modifier.size(20.dp),
                            contentDescription = "Clear Icon",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        textStyle = MaterialTheme.typography.bodyMedium
    )
}


@Preview
@Composable
private fun ThemedLoading() {
    NutritionTrackerTheme {
        AppSearchBar(query = "Apple qwe", isLoading = true)
    }
}


@Preview
@Composable
private fun ThemedWithError() {
    NutritionTrackerTheme {
        AppSearchBar(query = "Apple", errorMessage = "Something went wrong")
    }
}
@Preview
@Composable
private fun ThemedWithQuery() {
    NutritionTrackerTheme {
        AppSearchBar(query = "Apple")
    }
}

@Preview
@Composable
private fun ThemedPlaceholder() {
    NutritionTrackerTheme {
        AppSearchBar()
    }
}

@Preview
@Composable
private fun Basic() {
    AppSearchBar()
}
