package dev.masalimov.nutritiontracker.domain.diary.model

import javax.inject.Inject

private const val DAYS_RANGE_FROM_TODAY = 1

class DiaryDateCalendar @Inject constructor(
) {
    val startingDate: DiaryDate = DiaryDate.today()
    val dates = (-DAYS_RANGE_FROM_TODAY..DAYS_RANGE_FROM_TODAY).map {
        startingDate.plusDays(it)
    }
}