package dev.masalimov.nutritiontracker.feature.diary

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.masalimov.nutritiontracker.core.ui.NutritionTrackerTheme
import dev.masalimov.nutritiontracker.feature.diary.components.CaloriesCard
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CaloriesCardUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun showsDailyCaloriesTitle_whenLoading() {
        composeTestRule.setContent {
            NutritionTrackerTheme {
                CaloriesCard(DiaryInfoUiState.Loading)
            }
        }
        composeTestRule.onNodeWithText("Daily calories").assertIsDisplayed()
    }

    @Test
    fun doesNotShowCalorieNumbers_whenLoading() {
        composeTestRule.setContent {
            NutritionTrackerTheme {
                CaloriesCard(DiaryInfoUiState.Loading)
            }
        }
        composeTestRule.onNodeWithText("Remaining", substring = true).assertDoesNotExist()
        composeTestRule.onNodeWithText("Over", substring = true).assertDoesNotExist()
    }

    @Test
    fun showsConsumedAndGoalCalories_whenDiaryInfoLoaded() {
        composeTestRule.setContent {
            NutritionTrackerTheme {
                CaloriesCard(
                    DiaryInfoUiState.DiaryInfo(
                        eatenFoodList = emptyList(),
                        suggestedFoodList = emptyList(),
                        caloriesEatenTotal = 750,
                        goalCaloriesPerDay = 2000,
                    )
                )
            }
        }
        composeTestRule.onNodeWithText("750").assertIsDisplayed()
        composeTestRule.onNodeWithText(" / 2000 kcal").assertIsDisplayed()
    }

    @Test
    fun showsRemainingCalories_whenUnderGoal() {
        composeTestRule.setContent {
            NutritionTrackerTheme {
                CaloriesCard(
                    DiaryInfoUiState.DiaryInfo(
                        eatenFoodList = emptyList(),
                        suggestedFoodList = emptyList(),
                        caloriesEatenTotal = 500,
                        goalCaloriesPerDay = 2000,
                    )
                )
            }
        }
        composeTestRule.onNodeWithText("Remaining: 1500 kcal").assertIsDisplayed()
    }

    @Test
    fun showsOverCalories_whenOverGoal() {
        composeTestRule.setContent {
            NutritionTrackerTheme {
                CaloriesCard(
                    DiaryInfoUiState.DiaryInfo(
                        eatenFoodList = emptyList(),
                        suggestedFoodList = emptyList(),
                        caloriesEatenTotal = 2300,
                        goalCaloriesPerDay = 2000,
                    )
                )
            }
        }
        composeTestRule.onNodeWithText("Over: 300 kcal").assertIsDisplayed()
    }

    @Test
    fun showsCorrectPercentage_whenExactlyAtGoal() {
        composeTestRule.setContent {
            NutritionTrackerTheme {
                CaloriesCard(
                    DiaryInfoUiState.DiaryInfo(
                        eatenFoodList = emptyList(),
                        suggestedFoodList = emptyList(),
                        caloriesEatenTotal = 2000,
                        goalCaloriesPerDay = 2000,
                    )
                )
            }
        }
        composeTestRule.onNodeWithText("100%").assertIsDisplayed()
    }

    @Test
    fun showsCorrectPercentage_whenHalfGoalReached() {
        composeTestRule.setContent {
            NutritionTrackerTheme {
                CaloriesCard(
                    DiaryInfoUiState.DiaryInfo(
                        eatenFoodList = emptyList(),
                        suggestedFoodList = emptyList(),
                        caloriesEatenTotal = 1000,
                        goalCaloriesPerDay = 2000,
                    )
                )
            }
        }
        composeTestRule.onNodeWithText("50%").assertIsDisplayed()
    }

    @Test
    fun showsZeroPercent_whenNoCaloriesEaten() {
        composeTestRule.setContent {
            NutritionTrackerTheme {
                CaloriesCard(
                    DiaryInfoUiState.DiaryInfo(
                        eatenFoodList = emptyList(),
                        suggestedFoodList = emptyList(),
                        caloriesEatenTotal = 0,
                        goalCaloriesPerDay = 2000,
                    )
                )
            }
        }
        composeTestRule.onNodeWithText("0%").assertIsDisplayed()
        composeTestRule.onNodeWithText("Remaining: 2000 kcal").assertIsDisplayed()
    }

    @Test
    fun showsShimmer_whenError() {
        composeTestRule.setContent {
            NutritionTrackerTheme {
                CaloriesCard(DiaryInfoUiState.Error("Something went wrong"))
            }
        }
        composeTestRule.onNodeWithText("Daily calories").assertIsDisplayed()
        composeTestRule.onNodeWithText("Remaining", substring = true).assertDoesNotExist()
    }
}
