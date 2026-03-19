package dev.masalimov.nutritiontracker.feature.food.details

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import dev.masalimov.nutritiontracker.core.navigation.NavigationRoutes
import dev.masalimov.nutritiontracker.feature.food.list.FoodUiModel

fun NavController.navigateToFoodDetail(foodId: Long) {
    navigate(NavigationRoutes.FoodDetail(foodId))
}

fun NavGraphBuilder.foodDetailsScreen() {
    composable<NavigationRoutes.FoodDetail> { backStackEntry ->
        val route = backStackEntry.toRoute<NavigationRoutes.FoodDetail>()
        val foodId = route.foodId
        val viewModel: FoodDetailsViewModel = viewModel(factory =
            FoodDetailsViewModel.Factory(foodId)
        )
        FoodDetailScreen(viewModel)
    }
}
@Composable
fun FoodDetailScreen(
    viewModel: FoodDetailsViewModel,
) {
    val food by viewModel.uiState.collectAsStateWithLifecycle()
    if (food == null) {
        CircularProgressIndicator()
    } else
        FoodDetailContent(food as FoodUiModel)
}

@Composable
private fun FoodDetailContent(
    food: FoodUiModel,
) {
    Box(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = food.name,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(16.dp)
        )
    }
}