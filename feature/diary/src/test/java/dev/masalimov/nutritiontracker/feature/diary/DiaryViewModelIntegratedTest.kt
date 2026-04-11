package dev.masalimov.nutritiontracker.feature.diary

import dev.masalimov.nutritiontracker.domain.GoalCalories
import dev.masalimov.nutritiontracker.domain.diary.DiaryRepository
import dev.masalimov.nutritiontracker.domain.diary.model.DiaryDate
import dev.masalimov.nutritiontracker.domain.diary.model.DiaryEntryForDate
import dev.masalimov.nutritiontracker.domain.diary.model.DiaryId
import dev.masalimov.nutritiontracker.domain.diary.model.EatenFood
import dev.masalimov.nutritiontracker.domain.diary.usecase.AddFoodToDiaryUseCase
import dev.masalimov.nutritiontracker.domain.diary.usecase.GetCaloriesConsumptionPerDateUseCase
import dev.masalimov.nutritiontracker.domain.diary.usecase.GetDiaryStreamForDateUseCase
import dev.masalimov.nutritiontracker.domain.food.Food
import dev.masalimov.nutritiontracker.domain.food.FoodId
import dev.masalimov.nutritiontracker.domain.food.FoodRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DiaryViewModelIntegratedTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun createViewModel(
        diaryRepository: DiaryRepository,
        foodRepository: FoodRepository,
        goalCaloriesPerDay: Int = 1500,
    ): DiaryViewModel {
        val getCaloriesConsumptionPerDate = GetCaloriesConsumptionPerDateUseCase(diaryRepository)
        val getDiaryStreamForDate = GetDiaryStreamForDateUseCase(diaryRepository, foodRepository)
        val addFoodToDiary = AddFoodToDiaryUseCase(diaryRepository)
        val goalCalories = GoalCalories()
        return DiaryViewModel(
            getCaloriesConsumptionPerDate,
            getDiaryStreamForDate,
            addFoodToDiary,
            goalCalories,
        )
    }

    @Test
    fun uiState_emitsLoadedData_forTodayDiary() = runTest {
        // Arrange
        val today = DiaryDate.today()
        val diary = getFakeDiary(today)

        val fakeDiaryRepo = FakeDiaryRepository()
        fakeDiaryRepo.setDiary(today, diary)
        val fakeFoodRepo = FakeFoodRepository(suggested = listOf(
            Food(FoodId(2), "Banana", 96.0, 1.3, 0.3, 27.0)
        ))

        val vm = createViewModel(fakeDiaryRepo, fakeFoodRepo)

        // Act: wait until the first non-loading state is emitted
        val state = vm.uiState.first { !it.isLoading }

        // Assert
        assertEquals(false, state.isLoading)
        assertEquals(100, state.caloriesEatenTotal)
        assertEquals(1800, state.goalCaloriesPerDay)
        assertEquals(1, state.eatenFoodList.size)
        assertEquals("Apple", state.eatenFoodList.first().name)
        assertEquals(1, state.suggestedFoodList.size)
        assertTrue(state.dateList.any { it.date == today && it.isSelected })
    }

    private fun getFakeDiary(today: DiaryDate): DiaryEntryForDate {
        val apple = Food(
            FoodId(1),
            name = "Apple",
            caloriesPer100g = 50.0,
            proteinPer100g = 0.3,
            fatPer100g = 0.2,
            carbsPer100g = 25.0
        )
        val eaten = listOf(EatenFood(quantityGram = 200.0, food = apple)) // 200 kcal
        val diary = DiaryEntryForDate(DiaryId(1), date = today, eatenFood = eaten, goalCaloriesPerDay = 1800)
        return diary
    }

    @Test
    fun addFoodToDiary_emitsSuccessAndError_andResetToIdle() = runTest {
        // Arrange
        val today = DiaryDate.today()
        val fakeDiaryRepo = FakeDiaryRepository()
        val fakeFoodRepo = FakeFoodRepository()
        val vm = createViewModel(fakeDiaryRepo, fakeFoodRepo)

        // Act + Assert: success path
        vm.addFoodToDiary(foodIdToAdd = 42L, quantityGrams = 150.0)
        advanceUntilIdle()
        assertEquals(AddFoodUiState.Success, vm.addFoodUiState.value)
        // Verify repository call captured the date
        assertTrue(fakeDiaryRepo.addCalls.any { it.second == today })

        // Error path
        fakeDiaryRepo.failNextAdd()
        vm.addFoodToDiary(foodIdToAdd = 43L, quantityGrams = 100.0)
        advanceUntilIdle()
        assertEquals(AddFoodUiState.Error, vm.addFoodUiState.value)

        // Reset
        vm.addFoodErrorShowed()
        assertEquals(AddFoodUiState.Idle, vm.addFoodUiState.value)
    }

    @Test
    fun uiState_emitsLoadingState_whenDateSelected() = runTest {
        // Arrange
        val (today, vm) = getVm()

        // Act: wait for first uiState change
        vm.onSelectDate(today)
        vm.uiState.first()

        // Assert
        assertEquals(true, vm.uiState.value.isLoading)
    }

    @Test
    fun onSelectDate_emitsSuccessAndError() = runTest { 
        // Arrange
        // Arrange
        val today = DiaryDate.today()
        val tomorrow = DiaryDate.today().plusDays(1)
        val fakeDiaryRepo = FakeDiaryRepository()
        val vm = createViewModel(fakeDiaryRepo, FakeFoodRepository())
        fakeDiaryRepo.setDiary(today, null)
        fakeDiaryRepo.setDiary(tomorrow, getFakeDiary(tomorrow))

        // Act: select next day
        vm.onSelectDate(tomorrow)
        // Act: wait until the first non-loading state is emitted
        val state = vm.uiState.first { !it.isLoading }

        // Assert
        assertTrue(state.isLoading.not())
        assertTrue(state.dateList.first { it.date == today }.isSelected.not())
        assertTrue(state.dateList.first { it.date == tomorrow }.isSelected)
        assertTrue(state.eatenFoodList.size == 1)
        assertTrue(state.eatenFoodList.first().name == "Apple")

    }

    private fun getVm(): Pair<DiaryDate, DiaryViewModel> {
        // Arrange
        val today = DiaryDate.today()
        val fakeDiaryRepo = FakeDiaryRepository()
        val fakeFoodRepo = FakeFoodRepository()
        val vm = createViewModel(fakeDiaryRepo, fakeFoodRepo)
        return Pair(today, vm)
    }


    private class FakeDiaryRepository : DiaryRepository {
        private val byDate = mutableMapOf<DiaryDate, MutableStateFlow<DiaryEntryForDate?>>()
        private val all = MutableStateFlow<List<DiaryEntryForDate>>(emptyList())

        val addCalls = mutableListOf<Triple<Long, DiaryDate, Double>>()
        private var shouldFailNextAdd = false

        fun setDiary(date: DiaryDate, diary: DiaryEntryForDate?) {
            val flow = byDate.getOrPut(date) { MutableStateFlow(null) }
            flow.value = diary
            all.update { byDate.values.mapNotNull { it.value } }
        }

        fun failNextAdd() {
            shouldFailNextAdd = true
        }

        override fun getDiaryByDateFlow(date: DiaryDate): Flow<DiaryEntryForDate?> {
            return byDate.getOrPut(date) { MutableStateFlow(null) }
        }

        override fun getAllDiaryEntriesFlow(): Flow<List<DiaryEntryForDate>> = all

        override suspend fun addFoodToDiary(foodId: Long, date: DiaryDate, quantityGrams: Double) {
            addCalls += Triple(foodId, date, quantityGrams)
            if (shouldFailNextAdd) {
                shouldFailNextAdd = false
                throw IllegalStateException("Failed to add food")
            }
        }
    }

    private class FakeFoodRepository(
        private val saved: List<Food> = emptyList(),
        private val suggested: List<Food> = emptyList(),
    ) : FoodRepository {
        override suspend fun getAllFood(): List<Food> = saved
        override fun getAllFoodStream(): Flow<List<Food>> = MutableStateFlow(saved)
        override suspend fun getFoodById(foodId: FoodId): Food = saved.first { it.id == foodId }
        override suspend fun getSuggestedFood(eatenFood: List<Food>): List<Food> = suggested
        override suspend fun searchFood(query: String): List<Food> = emptyList()
        override suspend fun deleteFood(foodId: Long) { /* no-op */ }
    }
}
