package dev.masalimov.nutritiontracker.feature.diary

import app.cash.turbine.test
import dev.masalimov.nutritiontracker.core.common.AppDispatchers
import dev.masalimov.nutritiontracker.domain.GoalCalories
import dev.masalimov.nutritiontracker.domain.diary.CalorieConsumptionStatus
import dev.masalimov.nutritiontracker.domain.diary.model.CompleteDiaryInformationForDate
import dev.masalimov.nutritiontracker.domain.diary.model.DiaryDate
import dev.masalimov.nutritiontracker.domain.diary.model.DiaryDateCalendar
import dev.masalimov.nutritiontracker.domain.diary.model.DiaryEntryForDate
import dev.masalimov.nutritiontracker.domain.diary.model.DiaryId
import dev.masalimov.nutritiontracker.domain.diary.model.EatenFood
import dev.masalimov.nutritiontracker.domain.diary.usecase.AddFoodToDiaryUseCase
import dev.masalimov.nutritiontracker.domain.diary.usecase.GetCaloriesConsumptionForDateRangeUseCase
import dev.masalimov.nutritiontracker.domain.diary.usecase.GetDiaryStreamForDateUseCase
import dev.masalimov.nutritiontracker.domain.food.exampleFood
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DiaryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @MockK
    lateinit var getDiaryStreamForDateUseCase: GetDiaryStreamForDateUseCase

    @MockK
    lateinit var getCaloriesConsumptionForDateRangeUseCase: GetCaloriesConsumptionForDateRangeUseCase

    @MockK
    lateinit var addFoodToDiaryUseCase: AddFoodToDiaryUseCase

    @MockK
    lateinit var goalCalories: GoalCalories

    @MockK
    lateinit var diaryDateCalendar: DiaryDateCalendar

    lateinit var diaryViewModel: DiaryViewModel

    private val startingDate = DiaryDate.today()

    private fun testDiaryEntryForDate(date: DiaryDate) = DiaryEntryForDate(
        id = DiaryId(1),
        date = date,
        eatenFood = listOf(
            EatenFood(100.0, exampleFood),
        ),
        goalCaloriesPerDay = goalCalories.caloriesPerDay,
    )
    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { getDiaryStreamForDateUseCase.invoke(any()) } returns flowOf(
            CompleteDiaryInformationForDate.EMPTY
        )
        every { getCaloriesConsumptionForDateRangeUseCase.invoke(any(), any()) } returns flowOf(emptyMap())
        every { diaryDateCalendar.dates } returns listOf(startingDate)
        every { diaryDateCalendar.startingDate } returns startingDate
        every { goalCalories.caloriesPerDay } returns 2000

        initializeViewModel()
    }

    val testDispatcher = object : AppDispatchers {
        override val ioDispatcher: CoroutineDispatcher
            get() = mainDispatcherRule.testDispatcher
        override val mainDispatcher: CoroutineDispatcher
            get() = mainDispatcherRule.testDispatcher
        override val defaultDispatcher: CoroutineDispatcher
            get() = mainDispatcherRule.testDispatcher
    }
    private fun initializeViewModel() {
        diaryViewModel = DiaryViewModel(
            getCaloriesConsumptionForDateRangeUseCase,
            getDiaryStreamForDateUseCase,
            addFoodToDiaryUseCase,
            goalCalories,
            diaryDateCalendar = diaryDateCalendar,
            appDispatcher = testDispatcher,
        )
    }

    @Test
    fun `should load diary for today on start`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            diaryViewModel.uiState.collect {}
        }
        advanceUntilIdle()
        verify(exactly = 1) { getDiaryStreamForDateUseCase.invoke(DiaryDate.today()) }
        verify(exactly = 1) { getCaloriesConsumptionForDateRangeUseCase(any(), any()) }
    }

    @Test
    fun `should load diary for the selected date`() = runTest {
        // Given
        val selectedDate = DiaryDate.today().plusDays(2)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            diaryViewModel.uiState.collect {}
        }

        // When
        diaryViewModel.onSelectDate(selectedDate)
        advanceUntilIdle()

        // Then
        verify(exactly = 1) { getDiaryStreamForDateUseCase.invoke(selectedDate) }
        verify(exactly = 1) { getCaloriesConsumptionForDateRangeUseCase(any(), any()) }
    }

    @Test
    fun `should not load diary for the same date`() = runTest {
        // Given
        val selectedDate = DiaryDate.today().plusDays(2)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            diaryViewModel.uiState.collect {}
        }

        // When
        diaryViewModel.onSelectDate(selectedDate)
        advanceUntilIdle()
        diaryViewModel.onSelectDate(selectedDate)
        advanceUntilIdle()

        // Then
        verify(exactly = 1) { getDiaryStreamForDateUseCase.invoke(selectedDate) }
        verify(exactly = 1) { getCaloriesConsumptionForDateRangeUseCase(any(), any()) }
    }

    @Test
    fun `should add food to the selected date`() = runTest {
        // Given
        val selectedDate = DiaryDate.today().plusDays(2)

        // When
        diaryViewModel.onSelectDate(selectedDate)
        diaryViewModel.addFoodToDiary(foodIdToAdd = 42L, quantityGrams = 150.0)
        advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { addFoodToDiaryUseCase.invoke(42L, selectedDate, 150.0) }
    }

    // Here we use backgroundScope to collect the addFoodUiState in order to verify the sequence of emitted states when adding food.
    @Test
    fun `should show loading-success-error states on adding food`() = runTest {
        // Given
        val foodIdFirst = 42L
        val foodIdSecond = 43L
        val quantityGrams = 150.0
        // Mock use case: first call succeeds, second call fails for the same selected date
        val today = DiaryDate.today()
        coEvery { addFoodToDiaryUseCase.invoke(foodIdFirst, today, quantityGrams) } returns Unit
        coEvery {
            addFoodToDiaryUseCase.invoke(
                foodIdSecond,
                today,
                quantityGrams
            )
        } throws IllegalStateException("fail")

        val emissions = mutableListOf<AddFoodUiState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            diaryViewModel.addFoodUiState.collect { emissions.add(it) }
        }

        // When: first add succeeds
        diaryViewModel.addFoodToDiary(foodIdFirst, quantityGrams)
        advanceUntilIdle()
        // Then: Idle -> Loading -> Success
        assertEquals(
            listOf(
                AddFoodUiState.Idle,
                AddFoodUiState.Loading,
                AddFoodUiState.Success
            ), emissions
        )

        emissions.clear()
        // When: second add fails
        diaryViewModel.addFoodToDiary(foodIdSecond, quantityGrams)
        advanceUntilIdle()
        // Then: ... -> Loading -> Error
        assertEquals(
            listOf(
                AddFoodUiState.Loading,
                AddFoodUiState.Error,
            ),
            emissions
        )

        // When: user acknowledged error
        diaryViewModel.addFoodErrorShowed()
        advanceUntilIdle()
        // Then: reset to Idle
        assertEquals(AddFoodUiState.Idle, diaryViewModel.addFoodUiState.value)
    }

    // Here we use turbine library
    @Test
    fun `should show loading-success-error-success on start`() = runTest {
        val dateSuccess = DiaryDate.today()
        val dateError = dateSuccess.plusDays(1)

        every { getDiaryStreamForDateUseCase.invoke(dateSuccess) } returns flowOf(
            CompleteDiaryInformationForDate(
                diaryEntryForDate = testDiaryEntryForDate(dateSuccess),
                emptyList(),
            )
        )
        every { getDiaryStreamForDateUseCase.invoke(dateError) } returns flow {
            throw IllegalStateException("fail diary")
        }

        every { diaryDateCalendar.startingDate } returns dateSuccess

        initializeViewModel()

        diaryViewModel.uiState.test {
            // loading -> success

            val initialState = awaitItem()
            assertTrue(initialState.isLoading) // initial
            assertTrue(initialState.eatenFoodList.isEmpty()) // initial
            assertTrue(initialState.dateList.isEmpty()) // initial

            val loadingState1 = awaitItem()
            assertTrue(loadingState1.isLoading) // loading
            assertTrue(loadingState1.eatenFoodList.isEmpty()) // loading
            assertTrue(loadingState1.dateList.isNotEmpty()) // loading

            val successState1 = awaitItem()
            assertFalse(successState1.isLoading) // success
            assertTrue(successState1.error == null) // success
            assertTrue(successState1.eatenFoodList.isNotEmpty()) // success
            assertTrue(successState1.dateList.isNotEmpty()) // success


            // loading -> error
            diaryViewModel.onSelectDate(dateError)

            val loadingState2 = awaitItem()
            assertTrue(loadingState2.isLoading) // loading
            assertTrue(loadingState2.eatenFoodList.isEmpty()) // loading

            val stateError2 = awaitItem()
            assertTrue(stateError2.error != null) // error
            assertTrue(stateError2.eatenFoodList.isEmpty())

            // reselect -> loading -> success
            diaryViewModel.onSelectDate(dateSuccess)

            val loadingState3 = awaitItem()
            assertTrue(loadingState3.isLoading) // loading
            assertTrue(loadingState3.error == null) // loading
            assertTrue(loadingState3.eatenFoodList.isEmpty()) // loading

            val successState3 = awaitItem()
            assertFalse(successState3.isLoading) // success
            assertTrue(successState3.eatenFoodList.isNotEmpty()) // success
            assertTrue(successState3.error == null) // success
        }
    }

    @Test
    fun `should mark today date as selected on start`() = runTest {
        diaryViewModel.uiState.test {
            assertTrue(awaitItem().dateList.none { it.isSelected })
            assertEquals(true, awaitItem().dateList.find { it.date == DiaryDate.today() }?.isSelected)
        }
    }

    @Test
    fun `should mark new date as selected`() = runTest {
        val date1 = DiaryDate.today()
        val date2 = date1.plusDays(1)
        val date3 = date2.plusDays(1)
        every { diaryDateCalendar.dates } returns listOf(date1, date2, date3)
        initializeViewModel()

        diaryViewModel.uiState.test {
            awaitItem() // initial
            diaryViewModel.onSelectDate(date2)
            val state = awaitItem()
            assertTrue(state.isLoading)
            assertEquals(true, state.dateList.find { it.date == date2 }?.isSelected)
        }
    }

    @Test
    fun `should show calories consumption state for selected date in date list`() = runTest {
        val expectedDate1 = DiaryDate.today()
        val expectedDate2 = DiaryDate.today().plusDays(1)
        val expectedDate3 = DiaryDate.today().plusDays(2)

        every { diaryDateCalendar.dates } returns listOf(expectedDate1, expectedDate2, expectedDate3)
        initializeViewModel()

        every { getCaloriesConsumptionForDateRangeUseCase.invoke(any(), any()) } returns flowOf(
            mapOf(
                Pair(expectedDate1, CalorieConsumptionStatus.Over),
                Pair(expectedDate2, CalorieConsumptionStatus.NotOver),
                Pair(expectedDate3, CalorieConsumptionStatus.Unknown),
            )
        )

        diaryViewModel.uiState.test {
            awaitItem() // initial
            awaitItem() // loading
            assertEquals(listOf(
                CalorieConsumptionStatus.Over,
                CalorieConsumptionStatus.NotOver,
                CalorieConsumptionStatus.Unknown,
            ),
                awaitItem().dateList.map {
                    it.calorieConsumptionStatus
                }
            )
        }
    }

    @Test
    fun `should update calories consumption status after adding food`() = runTest {
        val date = DiaryDate.today()
        val statusFlow = MutableStateFlow<Map<DiaryDate, CalorieConsumptionStatus>>(mapOf(date to CalorieConsumptionStatus.NotOver))

        every { getCaloriesConsumptionForDateRangeUseCase.invoke(any(), any()) } returns statusFlow
        coEvery { addFoodToDiaryUseCase.invoke(any(), date, any()) } coAnswers {
            statusFlow.value = mapOf(date to CalorieConsumptionStatus.Over)
            Unit
        }
        every { diaryDateCalendar.dates } returns listOf(date)

        diaryViewModel.uiState.test {
            awaitItem() // initial
            awaitItem() // loading
            val before = awaitItem()
            assertEquals(CalorieConsumptionStatus.NotOver, before.dateList.single().calorieConsumptionStatus)

            diaryViewModel.addFoodToDiary(foodIdToAdd = 1L, quantityGrams = 100.0)

            val after = awaitItem()
            assertEquals(CalorieConsumptionStatus.Over, after.dateList.single().calorieConsumptionStatus)
        }
    }
}
