package dev.masalimov.nutritiontracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.navigation.ModalBottomSheetLayout
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.navigation.bottomSheet
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.navigation.rememberBottomSheetNavigator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.masalimov.nutritiontracker.core.ui.NutritionTrackerTheme
import dev.masalimov.nutritiontracker.feature.diary.DiaryScreen
import dev.masalimov.nutritiontracker.feature.diary.DiaryScreenRoute
import dev.masalimov.nutritiontracker.feature.diary.DiaryViewModel
import dev.masalimov.nutritiontracker.feature.food.details.FoodDetailRoute
import dev.masalimov.nutritiontracker.feature.food.details.FoodDetailScreen
import dev.masalimov.nutritiontracker.feature.food.details.FoodDetailsViewModel
import dev.masalimov.nutritiontracker.feature.food.list.FoodListRoute
import dev.masalimov.nutritiontracker.feature.food.list.FoodListScreen
import dev.masalimov.nutritiontracker.feature.food.list.FoodViewModel

@AndroidEntryPoint
@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NutritionTrackerTheme {
                val bottomSheetNavigator = rememberBottomSheetNavigator()
                val navController = rememberNavController(bottomSheetNavigator)

                ModalBottomSheetLayout(
                    bottomSheetNavigator = bottomSheetNavigator,
                    sheetShape = MaterialTheme.shapes.large,
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = DiaryScreenRoute,
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        val foodIdToAdd =
                            navController.currentBackStackEntry?.savedStateHandle?.get<Long>(
                                FOOD_ID_TO_ADD_NAVIGATION_ARG_KEY
                            )
                        diaryScreen(
                            foodIdToAdd = foodIdToAdd,
                            onLogFoodClick = {
                                navController.navigateToFoodList()
                            })
                        foodListScreen(
                            onItemClick = { foodId ->
                                navController.setNavigationArguments(
                                    foodId,
                                    FOOD_ID_TO_ADD_NAVIGATION_ARG_KEY
                                )
                            })
                        foodDetailsScreen()
                    }
                }
            }
        }
    }
}

const val FOOD_ID_TO_ADD_NAVIGATION_ARG_KEY = "foodIdToAdd"
fun NavGraphBuilder.diaryScreen(
    foodIdToAdd: Long? = null,
    onLogFoodClick: () -> Unit = {},
) {
    composable<DiaryScreenRoute> {
        val viewModel: DiaryViewModel = hiltViewModel()
        LaunchedEffect(foodIdToAdd) {
            if (foodIdToAdd != null) {
                viewModel.addFoodToDiary(foodIdToAdd)
            }
        }
        DiaryScreen(viewModel, onLogFoodClick)
    }
}

fun NavController.navigateToFoodList() {
    navigate(FoodListRoute)
}

fun NavGraphBuilder.foodListScreen(
    onItemClick: (Long) -> Unit = {},
) {
    bottomSheet<FoodListRoute> {
        val foodViewModel: FoodViewModel = hiltViewModel()
        FoodListScreen(
            modifier = Modifier.fillMaxSize(),
            viewModel = foodViewModel,
            onItemClick = onItemClick,
        )
    }
}

fun <T> NavController.setNavigationArguments(args: T, key: String) {
    previousBackStackEntry?.savedStateHandle?.set(key, args)
    popBackStack()
}

fun NavController.navigateToFoodDetail(foodId: Long) {
    navigate(FoodDetailRoute(foodId))
}

fun NavGraphBuilder.foodDetailsScreen() {
    composable<FoodDetailRoute> {
        val viewModel: FoodDetailsViewModel = hiltViewModel()
        FoodDetailScreen(viewModel)
    }
}
