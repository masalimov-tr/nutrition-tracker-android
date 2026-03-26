package dev.masalimov.nutritiontracker.domain.diary.usecase

import dev.masalimov.nutritiontracker.domain.diary.DiaryRepository
import dev.masalimov.nutritiontracker.domain.diary.model.Diary
import dev.masalimov.nutritiontracker.domain.diary.model.DiaryDate
import dev.masalimov.nutritiontracker.domain.diary.model.DiaryInfoForDate
import dev.masalimov.nutritiontracker.domain.food.FoodRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transform
import javax.inject.Inject

class GetDiaryStreamForDateUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository,
    private val foodRepository: FoodRepository,
) {
    operator fun invoke(date: DiaryDate): Flow<DiaryInfoForDate> {
        return diaryRepository.getDiaryByDateFlow(date)
            .transform { diary: Diary? ->
                if (diary == null) {
                    emit(DiaryInfoForDate.EMPTY)
                    return@transform
                }

                val suggestedFood =
                    foodRepository.getSuggestedFood(diary.eatenFood.map { it.food })
                emit(
                    DiaryInfoForDate(
                        diary,
                        suggestedFood,
                    )
                )
            }
    }

}