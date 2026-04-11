// GetCaloryConsumptionForDateRangeUseCaseTest.kt
package dev.masalimov.nutritiontracker.domain.diary.usecase

import dev.masalimov.nutritiontracker.domain.diary.CalorieConsumptionStatus
import dev.masalimov.nutritiontracker.domain.diary.model.DiaryDate
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Before
import org.junit.Test

class GetCaloryConsumptionForDateRangeUseCaseTest {

    @MockK
    private lateinit var getCaloriesConsumptionForDateUseCase: GetCaloriesConsumptionForDateUseCase
    private lateinit var sut: GetCaloriesConsumptionForDateRangeUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        sut = GetCaloriesConsumptionForDateRangeUseCase(getCaloriesConsumptionForDateUseCase)
    }

    @Test
    fun `return calories consumption status for each date`() = runTest {
        val date1 = DiaryDate.of(LocalDate(2024, 6, 1))
        val date2 = DiaryDate.of(LocalDate(2024, 6, 2))
        val date3 = DiaryDate.of(LocalDate(2024, 6, 3))

        every { getCaloriesConsumptionForDateUseCase.invoke(date1) } returns flowOf(
            CalorieConsumptionStatus.Over
        )
        every { getCaloriesConsumptionForDateUseCase.invoke(date2) } returns flowOf(
            CalorieConsumptionStatus.NotOver
        )
        every { getCaloriesConsumptionForDateUseCase.invoke(date3) } returns flowOf(
            CalorieConsumptionStatus.Unknown
        )
        val result = sut(date1, date3).toList()
        val expected = listOf(
            Pair(date1,CalorieConsumptionStatus.Over),
            Pair(date2,CalorieConsumptionStatus.NotOver),
            Pair(date3,CalorieConsumptionStatus.Unknown),
        )
        assertEquals(true, result.containsAll(expected))

        // Reverse order
        val result2 = sut(date3, date1).toList()
        assertEquals(true, result2.containsAll(expected))

        // One element range
        val resultOneElement = sut(date1, date1).toList()
        assertEquals(1, resultOneElement.size)
        assertEquals(true, resultOneElement[0] == Pair(date1,CalorieConsumptionStatus.Over))
    }
}