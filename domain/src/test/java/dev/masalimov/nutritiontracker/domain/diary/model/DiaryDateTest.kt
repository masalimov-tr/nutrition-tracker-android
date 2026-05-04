package dev.masalimov.nutritiontracker.domain.diary.model

import com.google.common.truth.Truth.assertThat
import kotlinx.datetime.LocalDate
import org.junit.Test

class DiaryDateTest {

    private val june15 = DiaryDate.of(LocalDate(2024, 6, 15))
    private val june16 = DiaryDate.of(LocalDate(2024, 6, 16))
    private val june17 = DiaryDate.of(LocalDate(2024, 6, 17))

    @Test
    fun `plusDays adds the correct number of days`() {
        assertThat(june15.plusDays(2)).isEqualTo(june17)
    }

    @Test
    fun `plusDays with negative value goes back in time`() {
        assertThat(june17.plusDays(-2)).isEqualTo(june15)
    }

    @Test
    fun `plusDays with zero returns the same date`() {
        assertThat(june15.plusDays(0)).isEqualTo(june15)
    }

    @Test
    fun `nextDay returns the following calendar day`() {
        assertThat(june15.nextDay()).isEqualTo(june16)
    }

    @Test
    fun `previousDay returns the preceding calendar day`() {
        assertThat(june16.previousDay()).isEqualTo(june15)
    }

    @Test
    fun `rangeTo produces an inclusive forward sequence`() {
        val range = (june15..june17).toList()
        assertThat(range).containsExactly(june15, june16, june17).inOrder()
    }

    @Test
    fun `rangeTo produces an inclusive reverse sequence`() {
        val range = (june17..june15).toList()
        assertThat(range).containsExactly(june17, june16, june15).inOrder()
    }

    @Test
    fun `rangeTo with equal start and end returns a single element`() {
        val range = (june15..june15).toList()
        assertThat(range).containsExactly(june15)
    }

    @Test
    fun `nextDay followed by previousDay returns the original date`() {
        assertThat(june15.nextDay().previousDay()).isEqualTo(june15)
    }
}
