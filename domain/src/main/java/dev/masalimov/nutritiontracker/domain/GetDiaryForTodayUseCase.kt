package dev.masalimov.nutritiontracker.domain

import dev.masalimov.nutritiontracker.domain.diary.DiaryRepository
import dev.masalimov.nutritiontracker.domain.food.FoodRepository
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

data class DiaryForToday(
    val eatenFood: List<Food>,
    val suggestedFood: List<Food>,
    val caloriesPerDay: Int,
)

class GetDiaryForTodayUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository,
    private val foodRepository: FoodRepository,
) {
    suspend operator fun invoke(date: DiaryDate): DiaryForToday = coroutineScope {
        val eatenFood = diaryRepository.getFoodByDate(date)
        val suggestedFood = foodRepository.getSuggestedFood(eatenFood)
        val caloriesPerDay = GoalCalories.caloriesPerDay
        DiaryForToday(eatenFood, suggestedFood, caloriesPerDay)
    }
}