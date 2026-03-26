package dev.masalimov.nutritiontracker.domain.diary.usecase

import dev.masalimov.nutritiontracker.domain.GoalCalories
import dev.masalimov.nutritiontracker.domain.diary.DiaryRepository
import dev.masalimov.nutritiontracker.domain.diary.model.Diary
import dev.masalimov.nutritiontracker.domain.diary.model.DiaryDate
import dev.masalimov.nutritiontracker.domain.diary.model.DiaryEntriesForDate
import dev.masalimov.nutritiontracker.domain.food.FoodRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transform
import javax.inject.Inject

class GetDiaryStreamForDateUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository,
    private val foodRepository: FoodRepository,
    private val goalCalories: GoalCalories,
) {
    operator fun invoke(date: DiaryDate): Flow<DiaryEntriesForDate> {
        return diaryRepository.getDiaryEntriesByDateFlow(date)
            .transform { diaryEntries: List<Diary> ->
                val suggestedFood =
                    foodRepository.getSuggestedFood(diaryEntries.map { it.eatenFood.food })
                emit(
                    DiaryEntriesForDate(
                        date,
                        diaryEntries,
                        suggestedFood,
                        goalCalories.caloriesPerDay
                    )
                )
            }
    }

}