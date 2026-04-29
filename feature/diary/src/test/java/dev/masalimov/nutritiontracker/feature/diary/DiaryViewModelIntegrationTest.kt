package dev.masalimov.nutritiontracker.feature.diary

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import dev.masalimov.nutritiontracker.core.common.AppDispatchers
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Integration test verifying that DiaryViewModel reacts to database changes
 * triggered by addFoodToDiary. Uses real use cases wired to in-memory
 * repositories instead of mocks so the full reactive chain is exercised.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DiaryViewModelIntegrationTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var inMemoryFoodRepository: InMemoryFoodRepository
    private lateinit var inMemoryDiaryRepository: InMemoryDiaryRepository
    private lateinit var viewModel: DiaryViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        inMemoryFoodRepository = InMemoryFoodRepository()
        inMemoryDiaryRepository = InMemoryDiaryRepository(inMemoryFoodRepository)
        viewModel = buildViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun diaryInfoUiState_updatesWithAddedFood_afterAddFoodToDiary() = runTest {
        val apple = Food(
            id = FoodId(1),
            name = "Apple",
            caloriesPer100g = 52.0,
            proteinPer100g = 0.3,
            fatPer100g = 0.2,
            carbsPer100g = 14.0,
        )
        inMemoryFoodRepository.addFood(apple)

        viewModel.diaryInfoUiState.test {
            assertThat(awaitItem()).isInstanceOf(DiaryInfoUiState.Loading::class.java)
            advanceUntilIdle()

            val emptyState = awaitItem() as DiaryInfoUiState.DiaryInfo
            assertThat(emptyState.eatenFoodList).isEmpty()
            assertThat(emptyState.caloriesEatenTotal).isEqualTo(0)

            viewModel.addFoodToDiary(foodIdToAdd = apple.id.id, quantityGrams = 150.0)
            advanceUntilIdle()

            val updatedState = awaitItem() as DiaryInfoUiState.DiaryInfo
            assertThat(updatedState.eatenFoodList).hasSize(1)
            assertThat(updatedState.eatenFoodList[0].name).isEqualTo("Apple")
            assertThat(updatedState.eatenFoodList[0].quantityGram).isEqualTo(150.0)
            // 150g * 52 cal/100g = 78 calories
            assertThat(updatedState.eatenFoodList[0].caloriesEaten).isEqualTo(78)
            assertThat(updatedState.caloriesEatenTotal).isEqualTo(78)
        }
    }

    @Test
    fun diaryInfoUiState_accumulatesCalories_asEachFoodIsAdded() = runTest {
        val apple = Food(
            id = FoodId(1),
            name = "Apple",
            caloriesPer100g = 52.0,
            proteinPer100g = 0.3,
            fatPer100g = 0.2,
            carbsPer100g = 14.0,
        )
        val chicken = Food(
            id = FoodId(2),
            name = "Chicken Breast",
            caloriesPer100g = 165.0,
            proteinPer100g = 31.0,
            fatPer100g = 3.6,
            carbsPer100g = 0.0,
        )
        inMemoryFoodRepository.addFood(apple)
        inMemoryFoodRepository.addFood(chicken)

        viewModel.diaryInfoUiState.test {
            assertThat(awaitItem()).isInstanceOf(DiaryInfoUiState.Loading::class.java)
            advanceUntilIdle()
            awaitItem() // empty DiaryInfo

            viewModel.addFoodToDiary(foodIdToAdd = apple.id.id, quantityGrams = 100.0)
            advanceUntilIdle()

            val afterApple = awaitItem() as DiaryInfoUiState.DiaryInfo
            assertThat(afterApple.eatenFoodList).hasSize(1)
            // 100g * 52/100 = 52 calories
            assertThat(afterApple.caloriesEatenTotal).isEqualTo(52)

            viewModel.addFoodToDiary(foodIdToAdd = chicken.id.id, quantityGrams = 200.0)
            advanceUntilIdle()

            val afterChicken = awaitItem() as DiaryInfoUiState.DiaryInfo
            assertThat(afterChicken.eatenFoodList).hasSize(2)
            // Apple (52) + Chicken 200g * 165/100 = 330 → total 382
            assertThat(afterChicken.caloriesEatenTotal).isEqualTo(382)
        }
    }

    @Test
    fun diaryInfoUiState_showsEmptyState_forDateWithNoFood_afterSwitchingFromFilledDate() =
        runTest {
            val apple = Food(
                id = FoodId(1),
                name = "Apple",
                caloriesPer100g = 52.0,
                proteinPer100g = 0.3,
                fatPer100g = 0.2,
                carbsPer100g = 14.0,
            )
            inMemoryFoodRepository.addFood(apple)

            val today = DiaryDate.today()
            val tomorrow = today.plusDays(1)

            viewModel.diaryInfoUiState.test {
                assertThat(awaitItem()).isInstanceOf(DiaryInfoUiState.Loading::class.java)
                advanceUntilIdle()
                awaitItem() // empty DiaryInfo for today

                viewModel.addFoodToDiary(foodIdToAdd = apple.id.id, quantityGrams = 100.0)
                advanceUntilIdle()

                val todayWithFood = awaitItem() as DiaryInfoUiState.DiaryInfo
                assertThat(todayWithFood.eatenFoodList).hasSize(1)

                // Switch to tomorrow — flatMapLatest restarts the inner flow
                viewModel.onSelectDate(tomorrow)
                advanceUntilIdle()

                assertThat(awaitItem()).isInstanceOf(DiaryInfoUiState.Loading::class.java)

                val tomorrowEmpty = awaitItem() as DiaryInfoUiState.DiaryInfo
                assertThat(tomorrowEmpty.eatenFoodList).isEmpty()
                assertThat(tomorrowEmpty.caloriesEatenTotal).isEqualTo(0)
            }
        }

    // region helpers

    private fun buildViewModel(): DiaryViewModel {
        val getCaloriesConsumptionForDateUseCase = GetCaloriesConsumptionForDateUseCase(
            diaryRepository = inMemoryDiaryRepository,
        )
        return DiaryViewModel(
            getCaloriesConsumptionForDateRangeUseCase = GetCaloriesConsumptionForDateRangeUseCase(
                getCaloriesConsumptionForDateUseCase = getCaloriesConsumptionForDateUseCase,
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
                override val ioDispatcher = testDispatcher
                override val mainDispatcher = testDispatcher
                override val defaultDispatcher = testDispatcher
            },
        )
    }

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
            )
                ?: DiaryEntryForDate(
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

    // endregion
}
