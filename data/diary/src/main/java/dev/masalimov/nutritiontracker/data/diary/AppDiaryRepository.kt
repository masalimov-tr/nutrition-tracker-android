package dev.masalimov.nutritiontracker.data.diary

import dev.masalimov.nutritiontracker.core.database.diary.DiaryDao
import dev.masalimov.nutritiontracker.core.database.diary.DiaryEntity
import dev.masalimov.nutritiontracker.core.database.diary.DiaryEntryFoodCrossRef
import dev.masalimov.nutritiontracker.domain.GoalCalories
import dev.masalimov.nutritiontracker.domain.diary.DiaryRepository
import dev.masalimov.nutritiontracker.domain.diary.model.Diary
import dev.masalimov.nutritiontracker.domain.diary.model.DiaryDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

fun DiaryDate.toEpochDay(): Int {
    return date.toEpochDays()
}

internal class AppDiaryRepository @Inject constructor(
    private val diaryDao: DiaryDao,
    private val goalCalories: GoalCalories,
) : DiaryRepository {

    override fun getDiaryByDateFlow(date: DiaryDate): Flow<Diary?> {
        return diaryDao.getDiaryForDateFlow(date.toEpochDay()).map { dbModel ->
            dbModel?.toDiary(date)
        }

    }

    override fun getAllDiaryEntriesFlow(): Flow<List<Diary>> {
        return diaryDao.getAllDiaryEntriesFlow().map {
            it.map { it.toDiary(DiaryDate.of(it.diaryEntry.dateEpochDay)) }
        }
    }

    override suspend fun addFoodToDiary(foodId: Long, date: DiaryDate, quantityGrams: Double) {
//        delay(2000)
        val result = diaryDao.getDiaryForDate(date.toEpochDay())
        val diaryEntryId = result?.diaryEntry?.uid ?: run {
            diaryDao.insert(
                DiaryEntity(
                    dateEpochDay = date.toEpochDay(),
                    goalCaloriesPerDay = goalCalories.caloriesPerDay,
                )
            )
        }
        diaryDao.insertCrossRef(
            DiaryEntryFoodCrossRef(
                diaryEntryId = diaryEntryId,
                foodId = foodId,
                quantityGrams = 100.0,
            )
        )

    }
}