package dev.masalimov.nutritiontracker.feature.diary

import dev.masalimov.nutritiontracker.domain.GoalCalories
import dev.masalimov.nutritiontracker.domain.diary.model.DiaryDate
import dev.masalimov.nutritiontracker.domain.diary.usecase.AddFoodToDiaryUseCase
import dev.masalimov.nutritiontracker.domain.diary.usecase.GetCaloriesConsumptionPerDateUseCase
import dev.masalimov.nutritiontracker.domain.diary.usecase.GetDiaryStreamForDateUseCase
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DiaryViewModelIsolatedTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @RelaxedMockK
    lateinit var getDiaryStreamForDateUseCase: GetDiaryStreamForDateUseCase

    @RelaxedMockK
    lateinit var getCaloriesConsumptionPerDateUseCase: GetCaloriesConsumptionPerDateUseCase

    @MockK
    lateinit var addFoodToDiaryUseCase: AddFoodToDiaryUseCase

    @RelaxedMockK
    lateinit var goalCalories: GoalCalories

    lateinit var diaryViewModel: DiaryViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
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


}
