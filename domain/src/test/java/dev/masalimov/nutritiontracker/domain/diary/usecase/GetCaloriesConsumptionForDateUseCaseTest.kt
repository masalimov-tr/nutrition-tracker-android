package dev.masalimov.nutritiontracker.domain.diary.usecase

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import dev.masalimov.nutritiontracker.domain.diary.CalorieConsumptionStatus
import dev.masalimov.nutritiontracker.domain.diary.DiaryRepository
import dev.masalimov.nutritiontracker.domain.diary.model.DiaryDate
import dev.masalimov.nutritiontracker.domain.diary.model.DiaryEntryForDate
import dev.masalimov.nutritiontracker.domain.diary.model.DiaryId
import dev.masalimov.nutritiontracker.domain.diary.model.EatenFood
import dev.masalimov.nutritiontracker.domain.food.Food
import dev.masalimov.nutritiontracker.domain.food.FoodId
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class GetCaloriesConsumptionForDateUseCaseTest {

    @MockK
    private lateinit var diaryRepository: DiaryRepository

    private lateinit var useCase: GetCaloriesConsumptionForDateUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = GetCaloriesConsumptionForDateUseCase(diaryRepository)
    }

    @Test
    fun `returns Unknown when no diary entry exists for the date`() = runTest {
        val date = DiaryDate.today()
        every { diaryRepository.getDiaryByDateFlow(date) } returns flowOf(null)

        useCase(date).test {
            assertThat(awaitItem()).isEqualTo(CalorieConsumptionStatus.Unknown)
            awaitComplete()
        }
    }

    @Test
    fun `returns Over when calories consumed exceed goal`() = runTest {
        val date = DiaryDate.today()
        // 2000 kcal eaten, goal 1500 → Over
        every { diaryRepository.getDiaryByDateFlow(date) } returns flowOf(
            entryWith(caloriesEaten = 2000, goal = 1500)
        )

        useCase(date).test {
            assertThat(awaitItem()).isEqualTo(CalorieConsumptionStatus.Over)
            awaitComplete()
        }
    }

    @Test
    fun `returns NotOver when calories consumed are under goal`() = runTest {
        val date = DiaryDate.today()
        // 500 kcal eaten, goal 1500 → NotOver
        every { diaryRepository.getDiaryByDateFlow(date) } returns flowOf(
            entryWith(caloriesEaten = 500, goal = 1500)
        )

        useCase(date).test {
            assertThat(awaitItem()).isEqualTo(CalorieConsumptionStatus.NotOver)
            awaitComplete()
        }
    }

    @Test
    fun `returns NotOver when calories consumed exactly equal goal`() = runTest {
        val date = DiaryDate.today()
        // Condition is strictly greater-than, so equal is NotOver
        every { diaryRepository.getDiaryByDateFlow(date) } returns flowOf(
            entryWith(caloriesEaten = 1500, goal = 1500)
        )

        useCase(date).test {
            assertThat(awaitItem()).isEqualTo(CalorieConsumptionStatus.NotOver)
            awaitComplete()
        }
    }

    @Test
    fun `emits a new status when the diary entry updates`() = runTest {
        val date = DiaryDate.today()
        // Flow emits two diary entries in succession
        every { diaryRepository.getDiaryByDateFlow(date) } returns flowOf(
            entryWith(caloriesEaten = 500, goal = 1500),
            entryWith(caloriesEaten = 2000, goal = 1500),
        )

        useCase(date).test {
            assertThat(awaitItem()).isEqualTo(CalorieConsumptionStatus.NotOver)
            assertThat(awaitItem()).isEqualTo(CalorieConsumptionStatus.Over)
            awaitComplete()
        }
    }

    /**
     * Creates a DiaryEntryForDate with a single food item that contributes exactly
     * [caloriesEaten] total calories. Uses caloriesPer100g == caloriesEaten so that
     * 100 g × (caloriesEaten / 100) = caloriesEaten (integer, no rounding loss).
     */
    private fun entryWith(caloriesEaten: Int, goal: Int): DiaryEntryForDate {
        val food = Food(FoodId(1), "Test food", caloriesPer100g = caloriesEaten.toDouble(), 0.0, 0.0, 0.0)
        return DiaryEntryForDate(
            id = DiaryId(1),
            date = DiaryDate.today(),
            eatenFood = listOf(EatenFood(quantityGram = 100.0, food = food)),
            goalCaloriesPerDay = goal,
        )
    }
}
