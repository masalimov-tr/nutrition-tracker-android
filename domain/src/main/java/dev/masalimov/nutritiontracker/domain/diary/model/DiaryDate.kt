package dev.masalimov.nutritiontracker.domain.diary.model

import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn

data class DiaryDate(
    val date: LocalDate,
) {
    
    fun plusDays(days: Int): DiaryDate =
        DiaryDate(date.plus(days, DateTimeUnit.DAY))

    fun nextDay(): DiaryDate = plusDays(1)

    fun previousDay(): DiaryDate = plusDays(-1)

    operator fun rangeTo(other: DiaryDate): Iterable<DiaryDate> {
        val forward = this.date <= other.date
        return generateSequence(this) { current ->
            if (current.date == other.date) null
            else if (forward) current.nextDay() else current.previousDay()
        }.asIterable()
    }

    companion object {
        fun today(timeZone: TimeZone = TimeZone.currentSystemDefault()): DiaryDate =
            DiaryDate(Clock.System.todayIn(timeZone))

        fun of(date: LocalDate): DiaryDate =
            DiaryDate(date)

        fun of(epochDay: Int): DiaryDate =
            DiaryDate(LocalDate.fromEpochDays(epochDay))
    }
}