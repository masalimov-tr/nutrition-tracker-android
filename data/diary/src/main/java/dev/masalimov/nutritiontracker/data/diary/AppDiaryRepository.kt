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
}