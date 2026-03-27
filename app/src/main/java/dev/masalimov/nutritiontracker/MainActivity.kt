package dev.masalimov.nutritiontracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
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
import dev.masalimov.nutritiontracker.feature.diary.rememberDiaryBottomSheetState
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
                val navController = rememberNavController()
                val diaryBottomSheetState = rememberDiaryBottomSheetState()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = DiaryScreenRoute,
                        modifier = Modifier
                            .fillMaxSize()
                            .then(Modifier.padding(innerPadding))
                    ) {
                        diaryScreen(onLogFoodClick = {
                            diaryBottomSheetState.open()
                        })
                        foodListScreen(onItemClick = {
                            navController.navigateToFoodDetail(it)
                        })
                        foodDetailsScreen()
                    }
                }

                if (diaryBottomSheetState.isExtended) {
                    ModalBottomSheet(
                        onDismissRequest = { diaryBottomSheetState.close() },
                        sheetState = diaryBottomSheetState.sheetState,
                        containerColor = MaterialTheme.colorScheme.background,
                    ) {
                        val foodVm: FoodViewModel = hiltViewModel()
                        FoodListScreen(
                            viewModel = foodVm,
                            onItemClick = { id ->
                                diaryBottomSheetState.close()
                                navController.navigateToFoodDetail(id)
                            }
                        )
                    }
                }
            }
        }
    }
}

fun NavGraphBuilder.diaryScreen(
    onLogFoodClick: () -> Unit = {},
) {
    composable<DiaryScreenRoute> {
        val viewModel: DiaryViewModel = hiltViewModel()
        DiaryScreen(viewModel, onLogFoodClick)
    }
}

fun NavGraphBuilder.foodListScreen(
    onItemClick: (Long) -> Unit = {},
) {
    composable<FoodListRoute> {
        val foodViewModel: FoodViewModel = hiltViewModel()
        FoodListScreen(
            modifier = Modifier.fillMaxSize(),
            viewModel = foodViewModel,
            onItemClick = onItemClick,
        )
    }
}
fun NavController.navigateToFoodList() {
    navigate(FoodListRoute)
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
