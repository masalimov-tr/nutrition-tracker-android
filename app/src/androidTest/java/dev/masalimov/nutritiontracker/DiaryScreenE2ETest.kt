package dev.masalimov.nutritiontracker

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dev.masalimov.nutritiontracker.core.database.NutritionAppDatabase
import dev.masalimov.nutritiontracker.core.database.food.FoodEntity
import dev.masalimov.nutritiontracker.domain.diary.DiaryRepository
import dev.masalimov.nutritiontracker.domain.diary.model.DiaryDate
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class DiaryScreenE2ETest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Inject lateinit var database: NutritionAppDatabase
    @Inject lateinit var diaryRepository: DiaryRepository

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun switchingCalendarDate_updatesEatenFoodSection() {
        val today = DiaryDate.today()
        val tomorrow = today.nextDay()
        val foodToEat = "Donut"
        // Seed the in-memory database through the same objects the ViewModel uses
        val foodId = runBlocking {
            database.foodDao().insert(
                FoodEntity(
                    name = foodToEat,
                    caloriesPer100g = 52.0,
                    proteinPer100g = 0.3,
                    fatPer100g = 0.2,
                    carbsPer100g = 14.0,
                )
            )
        }
        runBlocking { diaryRepository.addFoodToDiary(foodId, today, 100.0) }

        // The ViewModel is already running — the flow update propagates reactively to the UI
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText(foodToEat).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText(foodToEat).assertIsDisplayed()

        // Tap tomorrow's date in the calendar
        composeTestRule.onNodeWithText(tomorrow.date.dayOfMonth.toString()).performClick()

        // Real ViewModel.onSelectDate() fires → getDiaryStreamForDateUseCase queries the DB for
        // tomorrow → no entries found → DiaryInfoUiState.DiaryInfo with empty eatenFoodList
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText("No meals added yet").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("No meals added yet").assertIsDisplayed()
        composeTestRule.onNodeWithText(foodToEat).assertDoesNotExist()
    }
}
