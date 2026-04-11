package dev.masalimov.nutritiontracker.domain.diary.usecase

import dev.masalimov.nutritiontracker.domain.diary.CalorieConsumptionStatus
import dev.masalimov.nutritiontracker.domain.diary.DiaryRepository
import dev.masalimov.nutritiontracker.domain.diary.model.DiaryDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * A use case to determine the calorie consumption status for a specific diary date.
 *
 * This class retrieves diary entries from the repository and evaluates whether the calorie goal for a given date
 * has been met, exceeded, or is unknown. It processes data reactively using Kotlin Flows.
 *
 * @property diaryRepository The repository that stores diary entries with calorie intake information.
 */
class GetCaloriesConsumptionPerDateUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository,
) {

    /**
     * Retrieves the calorie consumption status for a specific date as a Flow.
     *
     * The function accesses all diary entries as a flow, searches for the entry matching the
     * provided date, and calculates its consumption status based on the calorie goal and actual value.
     * If no record exists for the given date, the status is marked as `Unknown`.
     *
     * @param date The target date for which calorie consumption status is calculated.
     * @return A Flow emitting the `CalorieConsumptionStatus` for the given date.
     */
    operator fun invoke(date: DiaryDate): Flow<CalorieConsumptionStatus> {
        return diaryRepository.getAllDiaryEntriesFlow().map { diaryEntries ->
            val diaryEntry = diaryEntries.find { it.date == date }
            diaryEntry?.let {
                getConsumptionStatus(it.goalCaloriesPerDay, it.caloriesEaten)
            } ?: CalorieConsumptionStatus.Unknown
        }
    }
}

/**
 * Computes the calorie consumption status by comparing the set goal and consumed calories.
 *
 * @param goalCaloriesPerDay The daily calorie goal to be achieved.
 * @param caloriesEaten The actual calories consumed during the day.
 * @return `CalorieConsumptionStatus.Over` if the calories consumed exceed the goal,
 *         `CalorieConsumptionStatus.NotOver` if within the goal, or `Unknown` in other cases.
 */
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
