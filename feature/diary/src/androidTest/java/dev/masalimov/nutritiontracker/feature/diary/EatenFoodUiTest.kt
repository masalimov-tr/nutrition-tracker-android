package dev.masalimov.nutritiontracker.feature.diary

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.masalimov.nutritiontracker.core.ui.NutritionTrackerTheme
import dev.masalimov.nutritiontracker.feature.diary.components.EatenFood
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EatenFoodUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun showsTodaysMealsTitle_always() {
        composeTestRule.setContent {
            NutritionTrackerTheme {
                EatenFood(DiaryInfoUiState.Loading)
            }
        }
        composeTestRule.onNodeWithText("Today's meals").assertIsDisplayed()
    }

    @Test
    fun showsNoMealsText_whenEmptyList() {
        composeTestRule.setContent {
            NutritionTrackerTheme {
                EatenFood(
                    DiaryInfoUiState.DiaryInfo(
                        eatenFoodList = emptyList(),
                        suggestedFoodList = emptyList(),
                        caloriesEatenTotal = 0,
                        goalCaloriesPerDay = 2000,
                    )
                )
            }
        }
        composeTestRule.onNodeWithText("No meals added yet").assertIsDisplayed()
    }

    @Test
    fun showsFoodItemName_whenDiaryInfoLoaded() {
        composeTestRule.setContent {
            NutritionTrackerTheme {
                EatenFood(
                    DiaryInfoUiState.DiaryInfo(
                        eatenFoodList = listOf(
                            EatenFoodUiModel(
                                name = "Chicken breast",
                                quantityGram = 150.0,
                                caloriesEaten = 240,
                                caloriesPer100g = 160,
                            )
                        ),
                        suggestedFoodList = emptyList(),
                        caloriesEatenTotal = 240,
                        goalCaloriesPerDay = 2000,
                    )
                )
            }
        }
        composeTestRule.onNodeWithText("Chicken breast").assertIsDisplayed()
    }

    @Test
    fun showsFoodCaloriesPerHundredGrams_whenDiaryInfoLoaded() {
        composeTestRule.setContent {
            NutritionTrackerTheme {
                EatenFood(
                    DiaryInfoUiState.DiaryInfo(
                        eatenFoodList = listOf(
                            EatenFoodUiModel(
                                name = "Chicken breast",
                                quantityGram = 150.0,
                                caloriesEaten = 240,
                                caloriesPer100g = 160,
                            )
                        ),
                        suggestedFoodList = emptyList(),
                        caloriesEatenTotal = 240,
                        goalCaloriesPerDay = 2000,
                    )
                )
            }
        }
        composeTestRule.onNodeWithText("160 kcal / 100g").assertIsDisplayed()
    }

    @Test
    fun showsFoodQuantityAndCaloriesEaten_whenDiaryInfoLoaded() {
        composeTestRule.setContent {
            NutritionTrackerTheme {
                EatenFood(
                    DiaryInfoUiState.DiaryInfo(
                        eatenFoodList = listOf(
                            EatenFoodUiModel(
                                name = "Chicken breast",
                                quantityGram = 150.0,
                                caloriesEaten = 240,
                                caloriesPer100g = 160,
                            )
                        ),
                        suggestedFoodList = emptyList(),
                        caloriesEatenTotal = 240,
                        goalCaloriesPerDay = 2000,
                    )
                )
            }
        }
        composeTestRule.onNodeWithText("240 kkal").assertIsDisplayed()
        composeTestRule.onNodeWithText("150 g").assertIsDisplayed()
    }

    @Test
    fun showsMultipleFoodItems_whenDiaryInfoHasMultipleEntries() {
        composeTestRule.setContent {
            NutritionTrackerTheme {
                EatenFood(
                    DiaryInfoUiState.DiaryInfo(
                        eatenFoodList = listOf(
                            EatenFoodUiModel("Apple", 100.0, 52, 52),
                            EatenFoodUiModel("Greek yogurt", 200.0, 118, 59),
                            EatenFoodUiModel("Oatmeal", 80.0, 54, 68),
                        ),
                        suggestedFoodList = emptyList(),
                        caloriesEatenTotal = 224,
                        goalCaloriesPerDay = 2000,
                    )
                )
            }
        }
        composeTestRule.onNodeWithText("Apple").assertIsDisplayed()
        composeTestRule.onNodeWithText("Greek yogurt").assertIsDisplayed()
        composeTestRule.onNodeWithText("Oatmeal").assertIsDisplayed()
    }

    @Test
    fun doesNotShowNoMealsText_whenFoodItemsPresent() {
        composeTestRule.setContent {
            NutritionTrackerTheme {
                EatenFood(
                    DiaryInfoUiState.DiaryInfo(
                        eatenFoodList = listOf(
                            EatenFoodUiModel("Apple", 100.0, 52, 52)
                        ),
                        suggestedFoodList = emptyList(),
                        caloriesEatenTotal = 52,
                        goalCaloriesPerDay = 2000,
                    )
                )
            }
        }
        composeTestRule.onNodeWithText("No meals added yet").assertDoesNotExist()
    }

    @Test
    fun showsTodaysMealsTitle_whenError() {
        composeTestRule.setContent {
            NutritionTrackerTheme {
                EatenFood(DiaryInfoUiState.Error("Something went wrong"))
            }
        }
        composeTestRule.onNodeWithText("Today's meals").assertIsDisplayed()
    }
}
