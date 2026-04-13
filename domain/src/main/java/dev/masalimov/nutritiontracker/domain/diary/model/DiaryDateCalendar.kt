package dev.masalimov.nutritiontracker.domain.diary.model

import javax.inject.Inject

private const val DAYS_RANGE_FROM_TODAY = 1
class DiaryDateCalendar @Inject constructor() {
    private val today = DiaryDate.today()
    val dates = (DAYS_RANGE_FROM_TODAY.unaryMinus()..DAYS_RANGE_FROM_TODAY).map {
        today.plusDays(it)
    }
    val startingDate = today
}