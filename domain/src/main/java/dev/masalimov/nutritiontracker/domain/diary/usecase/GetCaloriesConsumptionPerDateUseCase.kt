package dev.masalimov.nutritiontracker.domain.diary.usecase

import dev.masalimov.nutritiontracker.domain.diary.CalorieConsumptionStatus
import dev.masalimov.nutritiontracker.domain.diary.DiaryRepository
import dev.masalimov.nutritiontracker.domain.diary.model.DiaryDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetCaloriesConsumptionPerDateUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository,
) {
    operator fun invoke(): Flow<Map<DiaryDate, CalorieConsumptionStatus>> {
        return diaryRepository.getAllDiaryEntriesFlow().map {
            it.associateBy { diary -> diary.date }.map {
                it.key to getConsumptionStatus(it.value.goalCaloriesPerDay, it.value.caloriesEaten)
            }.toMap()
        }
    }

    private fun getConsumptionStatus(
        goalCaloriesPerDay: Int,
        caloriesEaten: Int
    ): CalorieConsumptionStatus {
        return if (caloriesEaten > goalCaloriesPerDay) {
            CalorieConsumptionStatus.Over
        } else {
            CalorieConsumptionStatus.NotOver
        }
    }

}