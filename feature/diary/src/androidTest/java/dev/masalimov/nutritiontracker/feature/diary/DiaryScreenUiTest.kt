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
import dev.masalimov.nutritiontracker.domain.diary.DiaryRepository
import dev.masalimov.nutritiontracker.domain.diary.model.DiaryDate
import dev.masalimov.nutritiontracker.domain.diary.model.DiaryDateCalendar
import dev.masalimov.nutritiontracker.domain.diary.model.DiaryEntryForDate
import dev.masalimov.nutritiontracker.domain.diary.model.DiaryId
import dev.masalimov.nutritiontracker.domain.diary.model.EatenFood
import dev.masalimov.nutritiontracker.domain.diary.usecase.AddFoodToDiaryUseCase
import dev.masalimov.nutritiontracker.domain.diary.usecase.GetCaloriesConsumptionForDateRangeUseCase
import dev.masalimov.nutritiontracker.domain.diary.usecase.GetCaloriesConsumptionForDateUseCase
import dev.masalimov.nutritiontracker.domain.diary.usecase.GetDiaryStreamForDateUseCase
import dev.masalimov.nutritiontracker.domain.food.Food
import dev.masalimov.nutritiontracker.domain.food.FoodId
import dev.masalimov.nutritiontracker.domain.food.FoodRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DiaryScreenUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var inMemoryFoodRepository: InMemoryFoodRepository
    private lateinit var inMemoryDiaryRepository: InMemoryDiaryRepository
    private lateinit var viewModel: DiaryViewModel

    @Before
    fun setUp() {
        inMemoryFoodRepository = InMemoryFoodRepository()
        inMemoryDiaryRepository = InMemoryDiaryRepository(inMemoryFoodRepository)
        viewModel = buildViewModel()
    }

    @Test
    fun switchingCalendarDate_updatesEatenFoodSection() {
        val today = DiaryDate.today()
        val tomorrow = today.nextDay()
        val apple = Food(FoodId(1), "Apple", 52.0, 0.3, 0.2, 14.0)

        inMemoryFoodRepository.addFood(apple)
        runBlocking { inMemoryDiaryRepository.addFoodToDiary(apple.id.id, today, 100.0) }

        composeTestRule.setContent {
            NutritionTrackerTheme { DiaryScreen(viewModel = viewModel) }
        }

        // Wait for the async flow to deliver DiaryInfo with Apple
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText("Apple").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Apple").assertIsDisplayed()

        // Tap tomorrow's date in the calendar — real ViewModel.onSelectDate() is invoked
        composeTestRule.onNodeWithText(tomorrow.date.dayOfMonth.toString()).performClick()

        // Wait for the flow to switch to tomorrow's (empty) diary
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText("No meals added yet").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("No meals added yet").assertIsDisplayed()
        composeTestRule.onNodeWithText("Apple").assertDoesNotExist()
    }

    private fun buildViewModel() = DiaryViewModel(
        getCaloriesConsumptionForDateRangeUseCase = GetCaloriesConsumptionForDateRangeUseCase(
            getCaloriesConsumptionForDateUseCase = GetCaloriesConsumptionForDateUseCase(
                diaryRepository = inMemoryDiaryRepository,
            )
        ),
        getDiaryStreamForDateUseCase = GetDiaryStreamForDateUseCase(
            diaryRepository = inMemoryDiaryRepository,
            foodRepository = inMemoryFoodRepository,
        ),
        addFoodToDiaryUseCase = AddFoodToDiaryUseCase(
            diaryRepository = inMemoryDiaryRepository,
        ),
        goalCalories = GoalCalories(),
        diaryDateCalendar = DiaryDateCalendar(),
        appDispatcher = object : AppDispatchers {
            override val ioDispatcher = Dispatchers.IO
            override val mainDispatcher = Dispatchers.Main
            override val defaultDispatcher = Dispatchers.Default
        },
    )

    private class InMemoryFoodRepository : FoodRepository {
        private val foods: MutableMap<Long, Food> = mutableMapOf()

        fun addFood(food: Food) {
            foods[food.id.id] = food
        }

        fun getSync(foodId: FoodId): Food =
            foods[foodId.id] ?: error("Food not found: ${foodId.id}")

        override suspend fun getAllFood(): List<Food> = foods.values.toList()
        override fun getAllFoodStream(): Flow<List<Food>> =
            MutableStateFlow(foods.values.toList()).asStateFlow()

        override suspend fun getFoodById(foodId: FoodId): Food = getSync(foodId)
        override suspend fun getSuggestedFood(eatenFood: List<Food>): List<Food> =
            foods.values.filter { it !in eatenFood }.take(5)

        override suspend fun searchFood(query: String): List<Food> = emptyList()
        override suspend fun deleteFood(foodId: Long) { foods.remove(foodId) }
    }

    private class InMemoryDiaryRepository(
        private val foodRepository: InMemoryFoodRepository,
    ) : DiaryRepository {
        private val entries: MutableMap<Long, DiaryEntryForDate> = mutableMapOf()
        private val flows: MutableMap<Long, MutableStateFlow<DiaryEntryForDate?>> = mutableMapOf()

        override fun getDiaryByDateFlow(date: DiaryDate): Flow<DiaryEntryForDate?> {
            val key = epochKey(date)
            return flows.getOrPut(key) { MutableStateFlow(entries[key]) }.asStateFlow()
        }

        override suspend fun addFoodToDiary(foodId: Long, date: DiaryDate, quantityGrams: Double) {
            val key = epochKey(date)
            val food = foodRepository.getSync(FoodId(foodId))
            val current = entries[key]
            val updated = current?.copy(
                eatenFood = current.eatenFood + EatenFood(quantityGram = quantityGrams, food = food)
            ) ?: DiaryEntryForDate(
                id = DiaryId(key),
                date = date,
                eatenFood = listOf(EatenFood(quantityGram = quantityGrams, food = food)),
                goalCaloriesPerDay = 2000,
            )
            entries[key] = updated
            flows.getOrPut(key) { MutableStateFlow(null) }.value = updated
        }

        private fun epochKey(date: DiaryDate): Long = date.date.toEpochDays().toLong()
    }
}
