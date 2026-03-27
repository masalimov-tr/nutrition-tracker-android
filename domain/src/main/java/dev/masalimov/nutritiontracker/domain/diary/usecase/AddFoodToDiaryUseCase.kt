package dev.masalimov.nutritiontracker.domain.diary.usecase

import dev.masalimov.nutritiontracker.domain.diary.DiaryRepository
import dev.masalimov.nutritiontracker.domain.diary.model.DiaryDate
import javax.inject.Inject

class AddFoodToDiaryUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository,
) {

    suspend operator fun invoke(foodId: Long, date: DiaryDate, quantityGrams: Double) {
        diaryRepository.addFoodToDiary(foodId = foodId, date = date, quantityGrams = quantityGrams)
    }
}