package dev.masalimov.nutritiontracker.feature.diary

import app.cash.turbine.test
import dev.masalimov.nutritiontracker.domain.GoalCalories
import dev.masalimov.nutritiontracker.domain.diary.CalorieConsumptionStatus
import dev.masalimov.nutritiontracker.domain.diary.model.CompleteDiaryInformationForDate
import dev.masalimov.nutritiontracker.domain.diary.model.DiaryDate
import dev.masalimov.nutritiontracker.domain.diary.usecase.AddFoodToDiaryUseCase
import dev.masalimov.nutritiontracker.domain.diary.usecase.GetCaloriesConsumptionForDateUseCase
import dev.masalimov.nutritiontracker.domain.diary.usecase.GetDiaryStreamForDateUseCase
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DiaryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @RelaxedMockK
    lateinit var getDiaryStreamForDateUseCase: GetDiaryStreamForDateUseCase

    @RelaxedMockK
    lateinit var getCaloriesConsumptionPerDateUseCase: GetCaloriesConsumptionForDateUseCase

    @MockK
    lateinit var addFoodToDiaryUseCase: AddFoodToDiaryUseCase

    @RelaxedMockK
    lateinit var goalCalories: GoalCalories

    lateinit var diaryViewModel: DiaryViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { getDiaryStreamForDateUseCase.invoke(any()) } returns flowOf(
            CompleteDiaryInformationForDate.EMPTY
        )
        every { getCaloriesConsumptionPerDateUseCase.invoke(any()) } returns flowOf(
            CalorieConsumptionStatus.Unknown
        )
        diaryViewModel = DiaryViewModel(
            getCaloriesConsumptionPerDateUseCase,
            getDiaryStreamForDateUseCase,
            addFoodToDiaryUseCase,
            goalCalories,
            mainDispatcherRule.testDispatcher,
        )
    }

    @Test
    fun `should load diary for today on start`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            diaryViewModel.uiState.collect {}
        }
        advanceUntilIdle()
        verify(exactly = 1) { getDiaryStreamForDateUseCase.invoke(DiaryDate.today()) }
        verify(exactly = 1) { getCaloriesConsumptionPerDateUseCase.invoke(DiaryDate.today()) }
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
        verify(exactly = 1) { getCaloriesConsumptionPerDateUseCase.invoke(selectedDate) }
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
        verify(exactly = 1) { getCaloriesConsumptionPerDateUseCase.invoke(selectedDate) }
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
        every { getCaloriesConsumptionPerDateUseCase.invoke(dateSuccess) } returns flowOf(
            CalorieConsumptionStatus.Unknown
        )
        every { getDiaryStreamForDateUseCase.invoke(dateError) } returns flow {
            throw IllegalStateException("fail diary")
        }
        every { getCaloriesConsumptionPerDateUseCase.invoke(dateError) } returns flow {
            throw IllegalStateException("fail diary")
        }
        diaryViewModel.uiState.test {
            val state1Loading = awaitItem()
            assertEquals(true, state1Loading.isLoading)
            assertEquals(null, state1Loading.error)

            val state1Success = awaitItem()
            assertEquals(false, state1Success.isLoading)
            assertEquals(null, state1Success.error)

            diaryViewModel.onSelectDate(dateError)

            val state2Loading = awaitItem()
            assertEquals(true, state2Loading.isLoading)
            assertEquals(null, state2Loading.error)

            val state2Error = awaitItem()
            assertEquals(false, state2Error.isLoading)
            assertEquals(true, state2Error.error != null)

            diaryViewModel.onSelectDate(dateSuccess)

            val state3Loading = awaitItem()
            assertEquals(true, state3Loading.isLoading)
            assertEquals(null, state3Loading.error)

            val state3Success = awaitItem()
            assertEquals(false, state3Success.isLoading)
            assertEquals(null, state3Success.error)
        }
    }

    @Test
    fun `should mark today date as selected on start`() = runTest {
        diaryViewModel.uiState.test {
            val loadingState = awaitItem()
            val successState = awaitItem()
            assertEquals(
                true,
                successState.dateList.find { it.date == DiaryDate.today() }?.isSelected
            )
        }
    }

    @Test
    fun `should mark new date as selected`() = runTest {
        diaryViewModel.uiState.test {
            val loadingState = awaitItem()
            val successState = awaitItem()
            diaryViewModel.onSelectDate(DiaryDate.today().plusDays(1))
            val loadingState2 = awaitItem()
            val selectedState = awaitItem()
            assertEquals(
                true,
                selectedState.dateList.find { it.date == DiaryDate.today().plusDays(1) }?.isSelected
            )
        }
    }

    @Test
    fun `should show calories consumption state for selected date in date list`() = runTest {
        val expectedStatus = CalorieConsumptionStatus.Over

        every { getCaloriesConsumptionPerDateUseCase.invoke(DiaryDate.today()) } returns flowOf(
            expectedStatus
        )

        diaryViewModel.uiState.test {
            awaitItem()
            val loadedState = awaitItem()
            val selectedDate = loadedState.dateList.find { it.isSelected }

            assertEquals(expectedStatus, selectedDate?.calorieConsumptionStatus)
        }
    }
}
