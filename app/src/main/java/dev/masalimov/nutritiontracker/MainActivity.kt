package dev.masalimov.nutritiontracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import dev.masalimov.nutritiontracker.core.navigation.NavigationRoutes
import dev.masalimov.nutritiontracker.feature.food.details.FoodDetailScreen
import dev.masalimov.nutritiontracker.feature.food.details.FoodDetailsViewModel
import dev.masalimov.nutritiontracker.feature.food.details.foodDetailsScreen
import dev.masalimov.nutritiontracker.feature.food.details.navigateToFoodDetail
import dev.masalimov.nutritiontracker.feature.food.list.FoodListScreen
import dev.masalimov.nutritiontracker.feature.food.list.FoodViewModel
import dev.masalimov.nutritiontracker.feature.food.list.foodListScreen
import dev.masalimov.nutritiontracker.ui.theme.NutritionTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NutritionTrackerTheme {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = NavigationRoutes.FoodList,
                        modifier = Modifier
                            .fillMaxSize()
                            .then(Modifier.padding(innerPadding))
                    ) {
                        foodListScreen(onItemClick = {
                            navController.navigateToFoodDetail(it)
                        })
                        foodDetailsScreen()
                    }
                }
            }
        }
    }
}