package dev.masalimov.nutritiontracker.domain.diary

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import dev.masalimov.nutritiontracker.domain.diary.model.DiaryDate
import dev.masalimov.nutritiontracker.domain.diary.model.DiaryEntryForDate
import dev.masalimov.nutritiontracker.domain.diary.model.DiaryId
import dev.masalimov.nutritiontracker.domain.diary.model.EatenFood
import dev.masalimov.nutritiontracker.domain.diary.usecase.GetCaloriesConsumptionForDateRangeUseCase
import dev.masalimov.nutritiontracker.domain.diary.usecase.GetCaloriesConsumptionForDateUseCase
import dev.masalimov.nutritiontracker.domain.diary.usecase.GetDiaryStreamForDateUseCase
import dev.masalimov.nutritiontracker.domain.food.Food
import dev.masalimov.nutritiontracker.domain.food.FoodId
import dev.masalimov.nutritiontracker.domain.food.FoodRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Integration test that verifies the reactive behavior of use cases and flows
 * when data is updated.
 *
 * This test uses in-memory repositories to demonstrate:
 * 1. How getDiaryStreamForDateUseCase emits when diary data changes
 * 2. How caloriesConsumptionStatusFlow updates when calories change
 * 3. The complete reactive chain from data change to state emission
 */
class DiaryUseCasesReactivityIntegrationTest {

    private lateinit var getDiaryStreamForDateUseCase: GetDiaryStreamForDateUseCase
    private lateinit var getCaloriesConsumptionForDateRangeUseCase: GetCaloriesConsumptionForDateRangeUseCase

    private lateinit var inMemoryDiaryRepository: InMemoryDiaryRepository
    private lateinit var inMemoryFoodRepository: InMemoryFoodRepository

    @Before
    fun setUp() {
        // Initialize in-memory repositories
        inMemoryDiaryRepository = InMemoryDiaryRepository()
        inMemoryFoodRepository = InMemoryFoodRepository()

        // Initialize use cases with in-memory repositories
        getDiaryStreamForDateUseCase = GetDiaryStreamForDateUseCase(
            diaryRepository = inMemoryDiaryRepository,
            foodRepository = inMemoryFoodRepository,
        )

        val getCaloriesConsumptionForDateUseCase = GetCaloriesConsumptionForDateUseCase(
            diaryRepository = inMemoryDiaryRepository,
        )

        getCaloriesConsumptionForDateRangeUseCase = GetCaloriesConsumptionForDateRangeUseCase(
            getCaloriesConsumptionForDateUseCase = getCaloriesConsumptionForDateUseCase,
        )
    }

    @Test
    fun getDiaryStreamForDateUseCase_emitsUpdatedData_whenDiaryIsUpdated() = runTest {
        val selectedDate = DiaryDate.today()
        val apple = Food(
            id = FoodId(1),
            name = "Apple",
            caloriesPer100g = 52.0,
            proteinPer100g = 0.3,
            fatPer100g = 0.2,
            carbsPer100g = 14.0,
        )

        // Add food to repository
        inMemoryFoodRepository.addFood(apple)

        // Collect emissions from the use case
        getDiaryStreamForDateUseCase(selectedDate).test {
            // First emission: empty diary (no entry yet)
            val firstEmission = awaitItem()
            assertThat(firstEmission.diaryEntryForDate).isNull()

            // Simulate adding food to diary
            inMemoryDiaryRepository.addFoodToDiarySync(
                foodId = 1,
                date = selectedDate,
                quantityGrams = 150.0
            )

            // Second emission: diary with food
            val secondEmission = awaitItem()
            assertThat(secondEmission.diaryEntryForDate).isNotNull()
            secondEmission.diaryEntryForDate?.let { diary ->
                assertThat(diary.eatenFood).hasSize(1)
                assertThat(diary.eatenFood[0].food.name).isEqualTo("Apple")
                assertThat(diary.eatenFood[0].quantityGram).isEqualTo(150.0)
                // Apple: 150g * 52 cal/100g = 78 calories
                assertThat(diary.eatenFood[0].calories).isEqualTo(78)
                assertThat(diary.caloriesEaten).isEqualTo(78)
            }
        }
    }

    @Test
    fun getDiaryStreamForDateUseCase_emitsMultipleUpdates_whenMultipleFoodsAreAdded() = runTest {
        val selectedDate = DiaryDate.today()
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

        getDiaryStreamForDateUseCase(selectedDate).test {
            // Initial empty state
            awaitItem()

            // Add first food (Apple)
            inMemoryDiaryRepository.addFoodToDiarySync(1, selectedDate, 150.0)
            val emission1 = awaitItem()
            assertThat(emission1.diaryEntryForDate?.eatenFood).hasSize(1)
            assertThat(emission1.diaryEntryForDate?.caloriesEaten).isEqualTo(78)

            // Add second food (Chicken)
            inMemoryDiaryRepository.addFoodToDiarySync(2, selectedDate, 200.0)
            val emission2 = awaitItem()
            assertThat(emission2.diaryEntryForDate?.eatenFood).hasSize(2)
            // Apple (78) + Chicken (330: 200*165/100)
            assertThat(emission2.diaryEntryForDate?.caloriesEaten).isEqualTo(408)
        }
    }

    @Test
    fun caloriesConsumptionStatusFlow_emitsUpdatedStatus_whenCaloriesChange() = runTest {
        val selectedDate = DiaryDate.today()
        val chicken = Food(
            id = FoodId(2),
            name = "Chicken Breast",
            caloriesPer100g = 165.0,
            proteinPer100g = 31.0,
            fatPer100g = 3.6,
            carbsPer100g = 0.0,
        )

        inMemoryFoodRepository.addFood(chicken)

        // Set a low goal (100 cal) so adding chicken (330 cal) will exceed it
        inMemoryDiaryRepository.setGoalCaloriesPerDay(100)

        getCaloriesConsumptionForDateRangeUseCase(selectedDate, selectedDate).test {
            // First emission: initial status (Unknown - no diary entry yet)
            var emission = awaitItem()
            assertThat(emission[selectedDate]).isEqualTo(CalorieConsumptionStatus.Unknown)

            // Add food that exceeds goal
            inMemoryDiaryRepository.addFoodToDiarySync(2, selectedDate, 200.0) // 330 calories

            // Second emission: status should be Over (330 > 100)
            emission = awaitItem()
            assertThat(emission[selectedDate]).isEqualTo(CalorieConsumptionStatus.Over)
        }
    }

    @Test
    fun caloriesConsumptionStatusFlow_emitsUpdatedStatusForMultipleDates() = runTest {
        val selectedDate1 = DiaryDate.today()
        val selectedDate2 = selectedDate1.plusDays(2)
        val chicken = Food(
            id = FoodId(2),
            name = "Chicken Breast",
            caloriesPer100g = 165.0,
            proteinPer100g = 31.0,
            fatPer100g = 3.6,
            carbsPer100g = 0.0,
        )

        inMemoryFoodRepository.addFood(chicken)
        inMemoryDiaryRepository.setGoalCaloriesPerDay(200)

        getCaloriesConsumptionForDateRangeUseCase(selectedDate1, selectedDate2).test {
            // First emission: initial status (Unknown - no diary entry yet)
            val emission1 = awaitItem()
            assertThat(emission1[selectedDate1]).isEqualTo(CalorieConsumptionStatus.Unknown)
            assertThat(emission1[selectedDate2]).isEqualTo(CalorieConsumptionStatus.Unknown)

            // Add food that exceeds goal
            inMemoryDiaryRepository.addFoodToDiarySync(2, selectedDate1, 200.0) // 330 calories

            // Second emission: status should be Over (330 > 100) for selectedDate1, and still Unknown for selectedDate2
            val emission2 = awaitItem()
            assertThat(emission2[selectedDate1]).isEqualTo(CalorieConsumptionStatus.Over)
            assertThat(emission2[selectedDate2]).isEqualTo(CalorieConsumptionStatus.Unknown)

            // Add food not exceeding goal to selectedDate2
            inMemoryDiaryRepository.addFoodToDiarySync(2, selectedDate2, 10.0)

            val emission3 = awaitItem()
            assertThat(emission3[selectedDate1]).isEqualTo(CalorieConsumptionStatus.Over)
            assertThat(emission3[selectedDate2]).isEqualTo(CalorieConsumptionStatus.NotOver) // 16.5 calories, below 200 goal
        }

    }

    @Test
    fun getDiaryStreamForDateUseCase_switchesBetweenDates_correctly() = runTest {
        val date1 = DiaryDate.today()
        val date2 = date1.plusDays(1)
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

        // Add food to date1
        inMemoryDiaryRepository.addFoodToDiarySync(1, date1, 150.0)
        // Add different food to date2
        inMemoryDiaryRepository.addFoodToDiarySync(2, date2, 200.0)

        // Test date1
        getDiaryStreamForDateUseCase(date1).test {
            val emission = awaitItem()
            assertThat(emission.diaryEntryForDate?.eatenFood).hasSize(1)
            assertThat(emission.diaryEntryForDate?.eatenFood?.get(0)?.food?.name)
                .isEqualTo("Apple")
        }

        // Test date2
        getDiaryStreamForDateUseCase(date2).test {
            val emission = awaitItem()
            assertThat(emission.diaryEntryForDate?.eatenFood).hasSize(1)
            assertThat(emission.diaryEntryForDate?.eatenFood?.get(0)?.food?.name)
                .isEqualTo("Chicken Breast")
        }
    }

    /**
     * In-memory implementation of DiaryRepository for testing reactive behavior
     */
    internal inner class InMemoryDiaryRepository : DiaryRepository {
        private val diaryMap: MutableMap<Long, DiaryEntryForDate> = mutableMapOf()
        private val diaryFlows: MutableMap<Long, MutableStateFlow<DiaryEntryForDate?>> =
            mutableMapOf()

        private var goalCaloriesPerDay: Int = 2000

        fun setGoalCaloriesPerDay(goal: Int) {
            goalCaloriesPerDay = goal
        }

        fun addFoodToDiarySync(foodId: Long, date: DiaryDate, quantityGrams: Double) {
            val epochDay = date.date.toEpochDays().toLong()
            val currentDiary = diaryMap[epochDay]
            
            // Fetch the real food from the repository
            val food = try {
                inMemoryFoodRepository.getFoodByIdSync(FoodId(foodId))
            } catch (_: Exception) {
                // Fallback if food not found
                Food(
                    id = FoodId(foodId),
                    name = "Food $foodId",
                    caloriesPer100g = 100.0,
                    proteinPer100g = 0.0,
                    fatPer100g = 0.0,
                    carbsPer100g = 0.0,
                )
            }

            if (currentDiary == null) {
                // Create new diary entry
                val newDiary = DiaryEntryForDate(
                    id = DiaryId(epochDay),
                    date = date,
                    eatenFood = listOf(
                        EatenFood(
                            quantityGram = quantityGrams,
                            food = food
                        )
                    ),
                    goalCaloriesPerDay = goalCaloriesPerDay,
                )
                diaryMap[epochDay] = newDiary
            } else {
                // Add food to existing diary
                val updatedDiary = currentDiary.copy(
                    eatenFood = currentDiary.eatenFood + EatenFood(
                        quantityGram = quantityGrams,
                        food = food
                    )
                )
                diaryMap[epochDay] = updatedDiary
            }

            // Emit the updated diary through the flow
            val flow = diaryFlows.getOrPut(epochDay) {
                MutableStateFlow(null)
            }
            flow.value = diaryMap[epochDay]
        }

        override fun getDiaryByDateFlow(date: DiaryDate): Flow<DiaryEntryForDate?> {
            val epochDay = date.date.toEpochDays().toLong()
            return diaryFlows.getOrPut(epochDay) {
                MutableStateFlow(diaryMap[epochDay])
            }.asStateFlow()
        }

        override fun getAllDiaryEntriesFlow(): Flow<List<DiaryEntryForDate>> {
            return MutableStateFlow(diaryMap.values.toList()).asStateFlow()
        }

        override suspend fun addFoodToDiary(
            foodId: Long,
            date: DiaryDate,
            quantityGrams: Double
        ) {
            addFoodToDiarySync(foodId, date, quantityGrams)
        }
    }

    /**
     * In-memory implementation of FoodRepository for testing
     */
    internal class InMemoryFoodRepository : FoodRepository {
        private val foodMap: MutableMap<Long, Food> = mutableMapOf()

        fun addFood(food: Food) {
            foodMap[food.id.id] = food
        }

        @Suppress("unused")
        fun getFoodByIdSync(foodId: FoodId): Food {
            return foodMap[foodId.id] ?: throw IllegalArgumentException("Food not found: ${foodId.id}")
        }

        override suspend fun getAllFood(): List<Food> = foodMap.values.toList()

        override fun getAllFoodStream(): Flow<List<Food>> {
            return MutableStateFlow(foodMap.values.toList()).asStateFlow()
        }

        override suspend fun getFoodById(foodId: FoodId): Food {
            return foodMap[foodId.id] ?: throw IllegalArgumentException("Food not found")
        }

        override suspend fun getSuggestedFood(eatenFood: List<Food>): List<Food> {
            // Return first 5 foods not in eaten list
            return foodMap.values
                .filter { it !in eatenFood }
                .take(5)
        }

        override suspend fun searchFood(query: String): List<Food> = emptyList()

        override suspend fun deleteFood(foodId: Long) {
            foodMap.remove(foodId)
        }
    }
}










