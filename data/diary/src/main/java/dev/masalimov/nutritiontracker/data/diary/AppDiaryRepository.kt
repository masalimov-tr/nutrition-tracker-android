package dev.masalimov.nutritiontracker.data.diary

import dev.masalimov.nutritiontracker.core.database.diary.DiaryDao
import dev.masalimov.nutritiontracker.domain.Diary
import dev.masalimov.nutritiontracker.domain.DiaryDate
import dev.masalimov.nutritiontracker.domain.diary.DiaryRepository
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
}