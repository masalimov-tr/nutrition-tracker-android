package dev.masalimov.nutritiontracker.feature.diary

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.masalimov.nutritiontracker.core.ui.NutritionTrackerTheme
import dev.masalimov.nutritiontracker.feature.diary.components.SuggestedFood
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SuggestedFoodUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun showsSuggestedMealsTitle_always() {
        composeTestRule.setContent {
            NutritionTrackerTheme {
                SuggestedFood(DiaryInfoUiState.Loading)
            }
        }
        composeTestRule.onNodeWithText("Suggested meals").assertIsDisplayed()
    }

    @Test
    fun showsLoadingChips_whenLoading() {
        composeTestRule.setContent {
            NutritionTrackerTheme {
                SuggestedFood(DiaryInfoUiState.Loading)
            }
        }
        val loadingChips = composeTestRule.onAllNodesWithText("Loading...")
        loadingChips[0].assertIsDisplayed()
    }

    @Test
    fun showsNoSuggestionsText_whenEmptyList() {
        composeTestRule.setContent {
            NutritionTrackerTheme {
                SuggestedFood(
                    DiaryInfoUiState.DiaryInfo(
                        eatenFoodList = emptyList(),
                        suggestedFoodList = emptyList(),
                        caloriesEatenTotal = 0,
                        goalCaloriesPerDay = 2000,
                    )
                )
            }
        }
        composeTestRule.onNodeWithText("No suggestions available").assertIsDisplayed()
    }

    @Test
    fun showsFoodChip_whenSuggestedFoodAvailable() {
        composeTestRule.setContent {
            NutritionTrackerTheme {
                SuggestedFood(
                    DiaryInfoUiState.DiaryInfo(
                        eatenFoodList = emptyList(),
                        suggestedFoodList = listOf(
                            SuggestedFoodUiModel(name = "Apple", caloriesPer100g = 52.0)
                        ),
                        caloriesEatenTotal = 0,
                        goalCaloriesPerDay = 2000,
                    )
                )
            }
        }
        composeTestRule.onNodeWithText("Apple").assertIsDisplayed()
    }

    @Test
    fun showsAllFoodChips_whenMultipleSuggestions() {
        composeTestRule.setContent {
            NutritionTrackerTheme {
                SuggestedFood(
                    DiaryInfoUiState.DiaryInfo(
                        eatenFoodList = emptyList(),
                        suggestedFoodList = listOf(
                            SuggestedFoodUiModel(name = "Apple", caloriesPer100g = 52.0),
                            SuggestedFoodUiModel(name = "Banana", caloriesPer100g = 89.0),
                            SuggestedFoodUiModel(name = "Cottage cheese", caloriesPer100g = 98.0),
                        ),
                        caloriesEatenTotal = 0,
                        goalCaloriesPerDay = 2000,
                    )
                )
            }
        }
        composeTestRule.onNodeWithText("Apple").assertIsDisplayed()
        composeTestRule.onNodeWithText("Banana").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cottage cheese").assertIsDisplayed()
    }

    @Test
    fun doesNotShowNoSuggestionsText_whenSuggestionsPresent() {
        composeTestRule.setContent {
            NutritionTrackerTheme {
                SuggestedFood(
                    DiaryInfoUiState.DiaryInfo(
                        eatenFoodList = emptyList(),
                        suggestedFoodList = listOf(
                            SuggestedFoodUiModel(name = "Apple", caloriesPer100g = 52.0)
                        ),
                        caloriesEatenTotal = 0,
                        goalCaloriesPerDay = 2000,
                    )
                )
            }
        }
        composeTestRule.onNodeWithText("No suggestions available").assertDoesNotExist()
    }

    @Test
    fun showsLoadingChips_whenError() {
        composeTestRule.setContent {
            NutritionTrackerTheme {
                SuggestedFood(DiaryInfoUiState.Error("Something went wrong"))
            }
        }
        val loadingChips = composeTestRule.onAllNodesWithText("Loading...")
        loadingChips[0].assertIsDisplayed()
    }
}
