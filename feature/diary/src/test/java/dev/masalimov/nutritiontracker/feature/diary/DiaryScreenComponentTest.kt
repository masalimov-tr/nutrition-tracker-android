package dev.masalimov.nutritiontracker.feature.diary

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.masalimov.nutritiontracker.core.common.AppDispatchers
import dev.masalimov.nutritiontracker.core.ui.NutritionTrackerTheme
import dev.masalimov.nutritiontracker.domain.GoalCalories
import dev.masalimov.nutritiontracker.domain.diary.model.DiaryDate
import dev.masalimov.nutritiontracker.domain.diary.model.DiaryDateCalendar
import dev.masalimov.nutritiontracker.domain.diary.usecase.AddFoodToDiaryUseCase
import dev.masalimov.nutritiontracker.domain.diary.usecase.GetCaloriesConsumptionForDateRangeUseCase
import dev.masalimov.nutritiontracker.domain.diary.usecase.GetCaloriesConsumptionForDateUseCase
import dev.masalimov.nutritiontracker.domain.diary.usecase.GetDiaryStreamForDateUseCase
import dev.masalimov.nutritiontracker.domain.food.Food
import dev.masalimov.nutritiontracker.domain.food.FoodId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * Component-level tests for DiaryScreen.
 *
 * Tests exercise the full DiaryScreen composable (Calendar + CaloriesCard + EatenFood +
 * SuggestedFood sections) using real use cases wired to in-memory repositories.
 * These replace the previous per-component tests (CalendarUiTest, CaloriesCardUiTest,
 * EatenFoodUiTest, SuggestedFoodUiTest) which tested components in isolation with
 * hard-coded state — important behaviours are now verified end-to-end through the screen.
 *
 * Calorie math reference (in-memory repo sets goalCaloriesPerDay = 2000):
 *   EatenFood.calories = (quantityGram / 100 * caloriesPer100g).toInt()
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class DiaryScreenComponentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var foodRepo: InMemoryFoodRepository
    private lateinit var diaryRepo: InMemoryDiaryRepository
    private lateinit var viewModel: DiaryViewModel

    @Before
    fun setUp() {
        foodRepo = InMemoryFoodRepository()
        diaryRepo = InMemoryDiaryRepository(foodRepo)
        viewModel = buildViewModel()
    }

    // ── Section titles — visible immediately in the loading state ─────────────

    @Test
    fun showsComponentTitles_always() {
        composeTestRule.setContent {
            NutritionTrackerTheme { DiaryScreen(viewModel = viewModel) }
        }
        composeTestRule.onNodeWithText("Daily calories").assertIsDisplayed()
        composeTestRule.onNodeWithText("Today's meals").assertIsDisplayed()
        composeTestRule.onNodeWithText("Suggested meals").assertIsDisplayed()
    }

    // ── Calendar ──────────────────────────────────────────────────────────────

    @Test
    fun showsTodaysDayNumber_inCalendar() {
        val today = DiaryDate.today()
        composeTestRule.setContent {
            NutritionTrackerTheme { DiaryScreen(viewModel = viewModel) }
        }
        composeTestRule.waitUntil(5_000) {
            composeTestRule.onAllNodesWithText(today.date.dayOfMonth.toString())
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText(today.date.dayOfMonth.toString()).assertIsDisplayed()
    }

    // ── Empty diary state ─────────────────────────────────────────────────────

    @Test
    fun showsNoMealsText_whenNothingLogged() {
        composeTestRule.setContent {
            NutritionTrackerTheme { DiaryScreen(viewModel = viewModel) }
        }
        composeTestRule.waitUntil(5_000) {
            composeTestRule.onAllNodesWithText("No meals added yet")
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("No meals added yet").assertIsDisplayed()
    }

    // ── Calories card ─────────────────────────────────────────────────────────
    // Pasta: 200 kcal / 100g. In-memory repo sets goalCaloriesPerDay = 2000.

    @Test
    fun showsConsumedAndGoalCalories_whenFoodLogged() {
        // 100g × 200 kcal/100g = 200 kcal consumed
        val pasta = Food(FoodId(1), "Pasta", 200.0, 7.0, 1.5, 40.0)
        foodRepo.addFood(pasta)
        runBlocking { diaryRepo.addFoodToDiary(pasta.id.id, DiaryDate.today(), 100.0) }

        composeTestRule.setContent {
            NutritionTrackerTheme { DiaryScreen(viewModel = viewModel) }
        }
        composeTestRule.waitUntil(5_000) {
            composeTestRule.onAllNodesWithText("Pasta").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("200").assertIsDisplayed()
        composeTestRule.onNodeWithText(" / 2000 kcal").assertIsDisplayed()
    }

    @Test
    fun showsRemainingCalories_whenUnderGoal() {
        // 100g × 200 kcal/100g = 200 consumed; 2000 − 200 = 1800 remaining
        val pasta = Food(FoodId(1), "Pasta", 200.0, 7.0, 1.5, 40.0)
        foodRepo.addFood(pasta)
        runBlocking { diaryRepo.addFoodToDiary(pasta.id.id, DiaryDate.today(), 100.0) }

        composeTestRule.setContent {
            NutritionTrackerTheme { DiaryScreen(viewModel = viewModel) }
        }
        composeTestRule.waitUntil(5_000) {
            composeTestRule.onAllNodesWithText("Pasta").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Remaining: 1800 kcal").assertIsDisplayed()
    }

    @Test
    fun showsOverCalories_whenExceedingGoal() {
        // 600g × 400 kcal/100g = 2400 consumed; 2400 − 2000 = 400 over
        val fatFood = Food(FoodId(1), "Fat food", 400.0, 1.0, 40.0, 5.0)
        foodRepo.addFood(fatFood)
        runBlocking { diaryRepo.addFoodToDiary(fatFood.id.id, DiaryDate.today(), 600.0) }

        composeTestRule.setContent {
            NutritionTrackerTheme { DiaryScreen(viewModel = viewModel) }
        }
        composeTestRule.waitUntil(5_000) {
            composeTestRule.onAllNodesWithText("Fat food").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Over: 400 kcal").assertIsDisplayed()
    }

    // ── Eaten food section ────────────────────────────────────────────────────

    @Test
    fun showsFoodItemDetails_whenFoodLogged() {
        // 150g × 160 kcal/100g = (150/100 * 160).toInt() = 240 kcal
        val chicken = Food(FoodId(1), "Chicken breast", 160.0, 30.0, 3.0, 0.0)
        foodRepo.addFood(chicken)
        runBlocking { diaryRepo.addFoodToDiary(chicken.id.id, DiaryDate.today(), 150.0) }

        composeTestRule.setContent {
            NutritionTrackerTheme { DiaryScreen(viewModel = viewModel) }
        }
        composeTestRule.waitUntil(5_000) {
            composeTestRule.onAllNodesWithText("Chicken breast").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Chicken breast").assertIsDisplayed()
        composeTestRule.onNodeWithText("160 kcal / 100g").assertIsDisplayed()
        composeTestRule.onNodeWithText("150 g").assertIsDisplayed()
        composeTestRule.onNodeWithText("240 kkal").assertIsDisplayed()
    }

    @Test
    fun showsMultipleFoodItems_whenSeveralFoodsLogged() {
        val apple = Food(FoodId(1), "Apple", 52.0, 0.3, 0.2, 14.0)
        val yogurt = Food(FoodId(2), "Greek yogurt", 59.0, 10.0, 0.4, 3.6)
        foodRepo.addFood(apple)
        foodRepo.addFood(yogurt)
        runBlocking {
            diaryRepo.addFoodToDiary(apple.id.id, DiaryDate.today(), 100.0)
            diaryRepo.addFoodToDiary(yogurt.id.id, DiaryDate.today(), 200.0)
        }

        composeTestRule.setContent {
            NutritionTrackerTheme { DiaryScreen(viewModel = viewModel) }
        }
        composeTestRule.waitUntil(5_000) {
            composeTestRule.onAllNodesWithText("Apple").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Apple").assertIsDisplayed()
        composeTestRule.onNodeWithText("Greek yogurt").assertIsDisplayed()
    }

    // ── Date switching ────────────────────────────────────────────────────────

    @Test
    fun switchingCalendarDate_updatesEatenFoodSection() {
        val today = DiaryDate.today()
        val tomorrow = today.nextDay()
        val apple = Food(FoodId(1), "Apple", 52.0, 0.3, 0.2, 14.0)
        foodRepo.addFood(apple)
        runBlocking { diaryRepo.addFoodToDiary(apple.id.id, today, 100.0) }

        composeTestRule.setContent {
            NutritionTrackerTheme { DiaryScreen(viewModel = viewModel) }
        }
        composeTestRule.waitUntil(5_000) {
            composeTestRule.onAllNodesWithText("Apple").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Apple").assertIsDisplayed()

        composeTestRule.onNodeWithText(tomorrow.date.dayOfMonth.toString()).performClick()

        composeTestRule.waitUntil(5_000) {
            composeTestRule.onAllNodesWithText("No meals added yet")
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("No meals added yet").assertIsDisplayed()
        composeTestRule.onNodeWithText("Apple").assertDoesNotExist()
    }

    private fun buildViewModel() = DiaryViewModel(
        getCaloriesConsumptionForDateRangeUseCase = GetCaloriesConsumptionForDateRangeUseCase(
            getCaloriesConsumptionForDateUseCase = GetCaloriesConsumptionForDateUseCase(
                diaryRepository = diaryRepo,
            )
        ),
        getDiaryStreamForDateUseCase = GetDiaryStreamForDateUseCase(
            diaryRepository = diaryRepo,
            foodRepository = foodRepo,
        ),
        addFoodToDiaryUseCase = AddFoodToDiaryUseCase(
            diaryRepository = diaryRepo,
        ),
        goalCalories = GoalCalories(),
        diaryDateCalendar = DiaryDateCalendar(),
        appDispatcher = object : AppDispatchers {
            override val ioDispatcher = Dispatchers.IO
            override val mainDispatcher = Dispatchers.Main
            override val defaultDispatcher = Dispatchers.Default
        },
    )
}
