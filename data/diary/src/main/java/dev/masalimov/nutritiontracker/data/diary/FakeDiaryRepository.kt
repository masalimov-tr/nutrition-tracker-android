package dev.masalimov.nutritiontracker.data.diary

import dev.masalimov.nutritiontracker.domain.DiaryDate
import dev.masalimov.nutritiontracker.domain.FoodId
import dev.masalimov.nutritiontracker.domain.diary.DiaryRepository
import javax.inject.Inject

internal class FakeDiaryRepository @Inject constructor(): DiaryRepository {
    private val foodYesterday = (1L..2L).map { FoodId(it) }
    private val foodToday = (3L..4L).map { FoodId(it) }
    private val foodTomorrow = (5L..6L).map { FoodId(it) }

    private val dates = mutableMapOf<DiaryDate, List<FoodId>>().apply {
        put(DiaryDate.today(), foodToday)
        put(DiaryDate.today().nextDay(), foodTomorrow)
        put(DiaryDate.today().previousDay(), foodYesterday)
    }

    override suspend fun getFoodByDate(date: DiaryDate): List<FoodId> {
        val foodIds = dates[date] ?: emptyList()
        return foodIds
    }
}
