package dev.masalimov.nutritiontracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.navigation.ModalBottomSheetLayout
import androidx.compose.material.navigation.bottomSheet
import androidx.compose.material.navigation.rememberBottomSheetNavigator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
            val systemDark = isSystemInDarkTheme()
            val darkTheme = rememberSaveable { mutableStateOf(systemDark) }
            NutritionTrackerTheme(
                darkTheme = darkTheme.value,
            ) {
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
                        diaryScreen(
                            onLogFoodClick = {
                                navController.navigateToFoodList()
                            },
                            onSettingsClick = {
                                navController.navigateToSettings()
                            },
                        )
                        foodListScreen(
                            onItemClick = { foodId ->
                                navController.setNavigationArguments(
                                    foodId,
                                    FOOD_ID_TO_ADD_NAVIGATION_ARG_KEY
                                )
                            },
                            onItemLongClick = { foodId ->
                                navController.navigateToFoodDetail(foodId)
                            },
                        )
                        foodDetailsScreen()
                        settingsScreen(
                            darkThemeEnabled = darkTheme,
                            onDarkThemeChange = {
                                darkTheme.value = it
                            })
                    }
                }
            }
        }
    }
}

const val FOOD_ID_TO_ADD_NAVIGATION_ARG_KEY = "foodIdToAdd"
fun NavGraphBuilder.diaryScreen(
    onLogFoodClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
) {
    composable<DiaryScreenRoute> { backStackEntry ->
        val viewModel: DiaryViewModel = hiltViewModel()

        val foodIdToAdd by backStackEntry
            .savedStateHandle
            .getStateFlow<Long?>(FOOD_ID_TO_ADD_NAVIGATION_ARG_KEY, null)
            .collectAsStateWithLifecycle()

        LaunchedEffect(foodIdToAdd) {
            if (foodIdToAdd != null) {
                viewModel.addFoodToDiary(foodIdToAdd as Long)
            }
        }
        DiaryScreen(viewModel, onLogFoodClick, onSettingsClick)
    }
}

fun NavController.navigateToFoodList() {
    navigate(FoodListRoute)
}


fun NavController.navigateToSettings() {
    navigate("settings")
}

fun NavGraphBuilder.settingsScreen(
    modifier: Modifier = Modifier,
    darkThemeEnabled: State<Boolean>,
    onDarkThemeChange: (Boolean) -> Unit = {},
) {
    composable("settings") {
        Scaffold { innerPadding ->
            Surface(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                color = MaterialTheme.colorScheme.background,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 48.dp, start = 24.dp, end = 24.dp)
                ) {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Dark mode",
                        )
                        Switch(
                            checked = darkThemeEnabled.value,
                            onCheckedChange = {
                                onDarkThemeChange(it)
                            },
                        )
                    }
                }
            }
        }
    }
}

fun NavGraphBuilder.foodListScreen(
    onItemClick: (Long) -> Unit = {},
    onItemLongClick: (Long) -> Unit = {},
) {
    bottomSheet<FoodListRoute> {
        val foodViewModel: FoodViewModel = hiltViewModel()
        FoodListScreen(
            modifier = Modifier.fillMaxSize(),
            viewModel = foodViewModel,
            onItemClick = onItemClick,
            onItemLongClick = onItemLongClick,
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
