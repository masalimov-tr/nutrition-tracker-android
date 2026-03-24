package dev.masalimov.nutritiontracker.domain

import dev.masalimov.nutritiontracker.domain.diary.DiaryRepository
import dev.masalimov.nutritiontracker.domain.food.FoodRepository
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

data class DiaryForToday(
    val diaryEntitiesPerDay: List<Diary>,
    val suggestedFood: List<Food>,
    val caloriesPerDay: Int,
)

class GetDiaryForTodayUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository,
    private val foodRepository: FoodRepository,
) {
    suspend operator fun invoke(date: DiaryDate): DiaryForToday = coroutineScope {
        val diaryEntitiesPerDay = diaryRepository.getDiaryEntriesByDate(date)
        val suggestedFood = foodRepository.getSuggestedFood(diaryEntitiesPerDay.map { it.eatenFood.food })
        val caloriesPerDay = GoalCalories.caloriesPerDay
        DiaryForToday(diaryEntitiesPerDay, suggestedFood, caloriesPerDay)
    }
}