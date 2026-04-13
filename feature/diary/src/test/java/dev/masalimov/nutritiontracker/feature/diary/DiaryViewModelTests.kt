package dev.masalimov.nutritiontracker.feature.diary

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import dev.masalimov.nutritiontracker.core.common.AppDispatchers
import dev.masalimov.nutritiontracker.domain.GoalCalories
import dev.masalimov.nutritiontracker.domain.diary.CalorieConsumptionStatus
import dev.masalimov.nutritiontracker.domain.diary.model.DiaryDate
import dev.masalimov.nutritiontracker.domain.diary.model.DiaryDateCalendar
import dev.masalimov.nutritiontracker.domain.diary.usecase.AddFoodToDiaryUseCase
import dev.masalimov.nutritiontracker.domain.diary.usecase.GetCaloriesConsumptionForDateRangeUseCase
import dev.masalimov.nutritiontracker.domain.diary.usecase.GetDiaryStreamForDateUseCase
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
            goalCalories = GoalCalories(),
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
            assertTrue(first.isEmpty())

            val second = awaitItem()
            assertTrue(second == expectedMap)
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
}