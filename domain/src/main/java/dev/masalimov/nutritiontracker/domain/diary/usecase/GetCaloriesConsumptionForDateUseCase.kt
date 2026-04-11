package dev.masalimov.nutritiontracker.domain.diary.usecase

import dev.masalimov.nutritiontracker.domain.diary.CalorieConsumptionStatus
import dev.masalimov.nutritiontracker.domain.diary.DiaryRepository
import dev.masalimov.nutritiontracker.domain.diary.model.DiaryDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetCaloriesConsumptionForDateUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository,
) {

    /**
     * Retrieves a stream of calorie consumption status for a specific date.
     *
     * This function queries the diary repository for diary data associated with the given date,
     * and calculates the calorie consumption status using the daily calorie goal and the actual
     * calories consumed. If no diary entry exists for the provided date, the status is set to `Unknown`.
     *
     * @param date The date for which the calorie consumption status is to be retrieved.
     * @return A [Flow] emitting the [CalorieConsumptionStatus] for the specified date. The status can be one of `Over`,
     * `NotOver`, or `Unknown` depending on the retrieved data.
     */
    operator fun invoke(date: DiaryDate): Flow<CalorieConsumptionStatus> {
        return diaryRepository.getDiaryByDateFlow(date).map { diaryEntry ->
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
