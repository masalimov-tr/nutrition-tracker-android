package dev.masalimov.nutritiontracker.feature.diary

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import dev.masalimov.nutritiontracker.core.common.AppDispatchers
import dev.masalimov.nutritiontracker.domain.GoalCalories
import dev.masalimov.nutritiontracker.domain.diary.CalorieConsumptionStatus
import dev.masalimov.nutritiontracker.domain.diary.model.CompleteDiaryInformationForDate
import dev.masalimov.nutritiontracker.domain.diary.model.DiaryDate
import dev.masalimov.nutritiontracker.domain.diary.model.DiaryDateCalendar
import dev.masalimov.nutritiontracker.domain.diary.model.DiaryEntryForDate
import dev.masalimov.nutritiontracker.domain.diary.model.DiaryId
import dev.masalimov.nutritiontracker.domain.diary.model.exampleEatenFood
import dev.masalimov.nutritiontracker.domain.diary.usecase.AddFoodToDiaryUseCase
import dev.masalimov.nutritiontracker.domain.diary.usecase.GetCaloriesConsumptionForDateRangeUseCase
import dev.masalimov.nutritiontracker.domain.diary.usecase.GetDiaryStreamForDateUseCase
import dev.masalimov.nutritiontracker.domain.food.FoodId
import dev.masalimov.nutritiontracker.domain.food.exampleFood
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DiaryViewModelTests {

    @RelaxedMockK
    lateinit var getCaloriesConsumptionForDateRangeUseCase: GetCaloriesConsumptionForDateRangeUseCase

    @RelaxedMockK
    lateinit var getDiaryStreamForDateUseCase: GetDiaryStreamForDateUseCase

    @RelaxedMockK
    lateinit var addFoodToDiaryUseCase: AddFoodToDiaryUseCase

    @RelaxedMockK
    lateinit var diaryDateCalendar: DiaryDateCalendar

    @RelaxedMockK
    lateinit var goalCalories: GoalCalories

    private lateinit var viewModel: DiaryViewModel

    private val testDispatcher: TestDispatcher = StandardTestDispatcher()

    private fun testAppDispatchers(): AppDispatchers = object : AppDispatchers {
        override val ioDispatcher = testDispatcher
        override val mainDispatcher = testDispatcher
        override val defaultDispatcher = testDispatcher
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        MockKAnnotations.init(this)
        initViewModel()
    }

    private fun initViewModel(diaryDateCalendar: DiaryDateCalendar = DiaryDateCalendar()) {
        viewModel = DiaryViewModel(
            getCaloriesConsumptionForDateRangeUseCase,
            getDiaryStreamForDateUseCase,
            addFoodToDiaryUseCase,
            goalCalories = goalCalories,
            diaryDateCalendar = diaryDateCalendar,
            appDispatcher = testAppDispatchers(),
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @MockK
    @Test
    fun caloriesConsumptionStatus_emitsEmptyMap_thenMapWithStatuses() = runTest {
        val expectedMap = mapOf(
            DiaryDate.today() to CalorieConsumptionStatus.Over,
        )
        every { getCaloriesConsumptionForDateRangeUseCase(any(), any()) } returns flowOf(
            expectedMap
        )
        initViewModel()
        viewModel.caloriesConsumptionStatusFlow.test {
            val first = awaitItem()
            assertThat(first).isEmpty()

            val second = awaitItem()
            assertThat(second).isEqualTo(expectedMap)
        }
    }

    @Test
    fun calendarState_emitsLoading_thenCalendarWithSelectedDate_thenCalendarWithStatuses() =
        runTest {
            val date1 = DiaryDate.today()
            val date2 = date1.plusDays(1)
            val date3 = date2.plusDays(1)
            val expectedMap = mapOf(
                date1 to CalorieConsumptionStatus.Over,
                date2 to CalorieConsumptionStatus.NotOver,
            )
            every { getCaloriesConsumptionForDateRangeUseCase(any(), any()) } returns flowOf(
                expectedMap
            )

            every { diaryDateCalendar.dates } returns listOf(date1, date2, date3)

            initViewModel(diaryDateCalendar)
            viewModel.calendarUiState.test {
                val initialLoadingState = awaitItem()
                assertThat(initialLoadingState).isInstanceOf(CalendarUiState.Loading::class.java)

                val expectedDates = listOf(date1, date2, date3)

                val initialCalendarState = awaitItem()
                assertThat(initialCalendarState).isInstanceOf(CalendarUiState.Calendar::class.java)
                initialCalendarState as CalendarUiState.Calendar

                assertThat(initialCalendarState.uiModels.map { it.date })
                    .isEqualTo(expectedDates)

                assertThat(initialCalendarState.uiModels.map { it.calorieConsumptionStatus })
                    .isEqualTo(
                        listOf(
                            CalorieConsumptionStatus.Unknown,
                            CalorieConsumptionStatus.Unknown,
                            CalorieConsumptionStatus.Unknown,
                        )
                    )

                val updatedCalendarState = awaitItem() as CalendarUiState.Calendar
                assertThat(updatedCalendarState.uiModels.map { it.date })
                    .isEqualTo(expectedDates)

                assertThat(updatedCalendarState.uiModels.map { it.calorieConsumptionStatus })
                    .containsExactlyElementsIn(
                        listOf(
                            CalorieConsumptionStatus.Over,
                            CalorieConsumptionStatus.NotOver,
                            CalorieConsumptionStatus.Unknown,
                        )
                    )
                    .inOrder()
            }
        }

    @Test
    fun diaryInfoState_emitsLoading_thenDiaryInfo() = runTest {
        val expectedDate = DiaryDate.today().plusDays(1)
        val eatenFood = exampleEatenFood
        val suggestedFood = exampleFood.copy(id = FoodId(2))

        val diaryEntry = DiaryEntryForDate(
            DiaryId(1),
            date = expectedDate,
            eatenFood = listOf(eatenFood),
            goalCaloriesPerDay = 1000,
        )

        every { getDiaryStreamForDateUseCase.invoke(any()) } returns flowOf(
            CompleteDiaryInformationForDate(
                diaryEntryForDate = diaryEntry,
                suggestedFood = listOf(suggestedFood),
            )
        )
        initViewModel()
        viewModel.diaryInfoUiState.test {
            val initialLoadingState = awaitItem()
            assertThat(initialLoadingState).isInstanceOf(DiaryInfoUiState.Loading::class.java)

            val expectedDiaryInfoState = awaitItem()
            assertThat(expectedDiaryInfoState).isInstanceOf(DiaryInfoUiState.DiaryInfo::class.java)

            expectedDiaryInfoState as DiaryInfoUiState.DiaryInfo
            assertThat(expectedDiaryInfoState.eatenFoodList)
                .isEqualTo(listOf(eatenFood.toEatenFoodUiModel()))
            assertThat(expectedDiaryInfoState.suggestedFoodList)
                .isEqualTo(listOf(suggestedFood.toSuggestedFoodUiModel()))
            assertThat(expectedDiaryInfoState.caloriesEatenTotal)
                .isEqualTo(eatenFood.calories)
            assertThat(expectedDiaryInfoState.goalCaloriesPerDay).isEqualTo(1000)
        }
    }
    @Test
    fun diaryInfoState_emitsEmptyState_whenGetDiaryStreamForDateEmpty() = runTest {
        every { getDiaryStreamForDateUseCase.invoke(any()) } returns flowOf(
            CompleteDiaryInformationForDate(
                diaryEntryForDate = null,
                suggestedFood = emptyList()
            )
        )
        val expectedDefaultGoalCaloriesPerDay = 500
        every { goalCalories.caloriesPerDay } returns expectedDefaultGoalCaloriesPerDay

        initViewModel()
        viewModel.diaryInfoUiState.test {
            awaitItem() // loading
            val emptyState = awaitItem()
            assertThat(emptyState).isEqualTo(DiaryInfoUiState.DiaryInfo.Empty.copy(
                goalCaloriesPerDay = expectedDefaultGoalCaloriesPerDay
            ))
        }
    }

    @Test
    fun diaryInfoState_emitsError_whenGetDiaryStreamForDateFails() = runTest {
        every { getDiaryStreamForDateUseCase.invoke(any()) } returns flow {
            throw RuntimeException("Test exception")
        }
        initViewModel()
        viewModel.diaryInfoUiState.test {
            val initialLoadingState = awaitItem()
            assertThat(initialLoadingState).isInstanceOf(DiaryInfoUiState.Loading::class.java)

            val errorState = awaitItem()
            assertThat(errorState).isInstanceOf(DiaryInfoUiState.Error::class.java)
            errorState as DiaryInfoUiState.Error
            // Shouldn't expose inner exception message to the user
            assertThat(errorState.errorMessage).isNotEqualTo("Test exception")
        }
    }
}