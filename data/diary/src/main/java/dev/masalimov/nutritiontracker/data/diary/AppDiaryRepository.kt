package dev.masalimov.nutritiontracker.data.diary

import dev.masalimov.nutritiontracker.core.database.diary.DiaryDao
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
) : DiaryRepository {

    override suspend fun getDiaryEntriesByDate(date: DiaryDate): List<Diary> {
        return diaryDao.getEntriesForDate(date.toEpochDay()).map {
            it.toDiary()
        }
    }

    override fun getDiaryEntriesByDateFlow(date: DiaryDate): Flow<List<Diary>> {
        return diaryDao.getEntriesForDateFlow(date.toEpochDay()).map {
            it.map { it.toDiary() }
        }
    }
}