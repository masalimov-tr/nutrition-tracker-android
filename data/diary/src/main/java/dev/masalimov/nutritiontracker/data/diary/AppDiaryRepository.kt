package dev.masalimov.nutritiontracker.data.diary

import dev.masalimov.nutritiontracker.core.database.diary.DiaryDao
import dev.masalimov.nutritiontracker.domain.GoalCalories
import dev.masalimov.nutritiontracker.domain.diary.DiaryRepository
import dev.masalimov.nutritiontracker.domain.diary.model.DiaryDate
import dev.masalimov.nutritiontracker.domain.diary.model.DiaryEntryForDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject


internal class AppDiaryRepository @Inject constructor(
    private val diaryDao: DiaryDao,
    private val goalCalories: GoalCalories,
) : DiaryRepository {

    override fun getDiaryByDateFlow(date: DiaryDate): Flow<DiaryEntryForDate?> {
        return diaryDao.getDiaryForDateFlow(date.toEpochDay()).map { dbModel ->
            dbModel?.toDiary(date)
        }

    }

    override suspend fun addFoodToDiary(foodId: Long, date: DiaryDate, quantityGrams: Double) {
        diaryDao.addFoodToDiaryTx(
            epochDay = date.toEpochDay(),
            goalCaloriesPerDay = goalCalories.caloriesPerDay,
            foodId = foodId,
            quantityGrams = quantityGrams,
        )
    }
}