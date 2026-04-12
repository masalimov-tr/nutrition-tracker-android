package dev.masalimov.nutritiontracker.feature.diary

import app.cash.turbine.test
import dev.masalimov.nutritiontracker.domain.GoalCalories
import dev.masalimov.nutritiontracker.domain.diary.CalorieConsumptionStatus
import dev.masalimov.nutritiontracker.domain.diary.model.CompleteDiaryInformationForDate
import dev.masalimov.nutritiontracker.domain.diary.model.DiaryDate
import dev.masalimov.nutritiontracker.domain.diary.model.DiaryDateCalendar
import dev.masalimov.nutritiontracker.domain.diary.usecase.AddFoodToDiaryUseCase
import dev.masalimov.nutritiontracker.domain.diary.usecase.GetCaloriesConsumptionForDateRangeUseCase
import dev.masalimov.nutritiontracker.domain.diary.usecase.GetDiaryStreamForDateUseCase
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
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

    @RelaxedMockK
    lateinit var goalCalories: GoalCalories

    @MockK
    lateinit var diaryDateCalendar: DiaryDateCalendar

    lateinit var diaryViewModel: DiaryViewModel

    private val startingDate = DiaryDate.today()
    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { getDiaryStreamForDateUseCase.invoke(any()) } returns flowOf(
            CompleteDiaryInformationForDate.EMPTY
        )
        every { getCaloriesConsumptionForDateRangeUseCase.invoke(any(), any()) } returns flowOf(emptyList())
        every { diaryDateCalendar.dates } returns listOf(startingDate)
        every { diaryDateCalendar.startingDate } returns startingDate

        initializeViewModel()
    }

    private fun initializeViewModel() {
        diaryViewModel = DiaryViewModel(
            getCaloriesConsumptionForDateRangeUseCase,
            getDiaryStreamForDateUseCase,
            addFoodToDiaryUseCase,
            goalCalories,
            diaryDateCalendar = diaryDateCalendar,
            defaultDispatcher = mainDispatcherRule.testDispatcher,
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
            CompleteDiaryInformationForDate.EMPTY
        )
        every { getDiaryStreamForDateUseCase.invoke(dateError) } returns flow {
            throw IllegalStateException("fail diary")
        }

        diaryViewModel.uiState.test {
            // loading -> success
            assertTrue(awaitItem().isLoading) // initial
            assertTrue(awaitItem().isLoading) // loading
            assertFalse(awaitItem().isLoading) // success


            // loading -> error
            diaryViewModel.onSelectDate(dateError)
            assertTrue(awaitItem().isLoading) // loading
            assertTrue(awaitItem().error != null) // error

            // reselect -> loading -> success
            diaryViewModel.onSelectDate(dateSuccess)
            assertTrue(awaitItem().isLoading) // loading
            assertTrue(awaitItem().error == null) // success
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
            listOf(
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
        val statusFlow = MutableStateFlow<List<Pair<DiaryDate, CalorieConsumptionStatus>>>(listOf(date to CalorieConsumptionStatus.NotOver))

        every { getCaloriesConsumptionForDateRangeUseCase.invoke(any(), any()) } returns statusFlow
        coEvery { addFoodToDiaryUseCase.invoke(any(), date, any()) } coAnswers {
            statusFlow.value = listOf(date to CalorieConsumptionStatus.Over)
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
