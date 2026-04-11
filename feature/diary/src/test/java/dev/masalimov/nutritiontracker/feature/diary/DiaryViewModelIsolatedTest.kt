package dev.masalimov.nutritiontracker.feature.diary

import dev.masalimov.nutritiontracker.domain.GoalCalories
import dev.masalimov.nutritiontracker.domain.diary.CalorieConsumptionStatus
import dev.masalimov.nutritiontracker.domain.diary.model.CompleteDiaryInformationForDate
import dev.masalimov.nutritiontracker.domain.diary.model.DiaryDate
import dev.masalimov.nutritiontracker.domain.diary.usecase.AddFoodToDiaryUseCase
import dev.masalimov.nutritiontracker.domain.diary.usecase.GetCaloriesConsumptionPerDateUseCase
import dev.masalimov.nutritiontracker.domain.diary.usecase.GetDiaryStreamForDateUseCase
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
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
    fun `should return empty list when no diary data is available for the given date`() = runTest {
        // Given
        val today = DiaryDate.today()
        every { getDiaryStreamForDateUseCase.invoke(today) } returns flowOf(
            CompleteDiaryInformationForDate.EMPTY
        )
        every { getCaloriesConsumptionPerDateUseCase.invoke(today) } returns flowOf(
            CalorieConsumptionStatus.NotOver
        )

        backgroundScope.launch(
            UnconfinedTestDispatcher(testScheduler)
        ) { diaryViewModel.uiState.collect {} }
        // When + then
        advanceUntilIdle()
        verify(exactly = 1) { getDiaryStreamForDateUseCase.invoke(today) }
        verify(exactly = 1) { getCaloriesConsumptionPerDateUseCase.invoke(today) }

        // When + then
        val tomorrow = DiaryDate.today().plusDays(1)
        diaryViewModel.onSelectDate(tomorrow)
        advanceUntilIdle()
        verify(exactly = 1) { getDiaryStreamForDateUseCase.invoke(tomorrow) }
        verify(exactly = 1) { getCaloriesConsumptionPerDateUseCase.invoke(tomorrow) }

        // When
        diaryViewModel.onSelectDate(tomorrow)
        advanceUntilIdle()

        // Then
        verify(exactly = 1) { getDiaryStreamForDateUseCase.invoke(tomorrow) }
        verify(exactly = 1) { getCaloriesConsumptionPerDateUseCase.invoke(tomorrow) }
    }
}
