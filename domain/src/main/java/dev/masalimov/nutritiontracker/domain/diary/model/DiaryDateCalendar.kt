package dev.masalimov.nutritiontracker.domain.diary.model

import javax.inject.Inject

class DiaryDateCalendar @Inject constructor() {
    private val today = DiaryDate.today()
    val dates = (-7..7).map {
        today.plusDays(it)
    }
    val startingDate = today
}