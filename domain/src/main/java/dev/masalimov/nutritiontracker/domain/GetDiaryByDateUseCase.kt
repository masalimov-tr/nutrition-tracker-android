package dev.masalimov.nutritiontracker.domain

import dev.masalimov.nutritiontracker.domain.diary.DiaryRepository
import dev.masalimov.nutritiontracker.domain.food.FoodRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class GetDiaryByDateUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository,
    private val foodRepository: FoodRepository,
) {
    suspend operator fun invoke(date: DiaryDate): List<Food> = coroutineScope {
        val foodIds = diaryRepository.getFoodByDate(date)
        foodIds.map { foodId ->
            async { foodRepository.getFoodById(foodId) }
        }.map { it.await() }
    }
}