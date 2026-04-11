package dev.masalimov.nutritiontracker.domain.diary.usecase

import dev.masalimov.nutritiontracker.domain.diary.CalorieConsumptionStatus
import dev.masalimov.nutritiontracker.domain.diary.model.DiaryDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetCaloryConsumptionForDateRangeUseCase @Inject constructor(
    private val getCaloriesConsumptionForDateUseCase: GetCaloriesConsumptionForDateUseCase,
) {
    /**
     * Retrieves a flow that emits pairs of dates and their associated calorie consumption statuses
     * over a specified date range.
     *
     * This method generates all dates within the range from `startDate` to `endDate` and, for each date,
     * queries the calorie consumption status. The result is a flow of pairs where each pair contains a date
     * and its corresponding calorie consumption status, which can either be `Unknown`, `Over`, or `NotOver`.
     *
     * @param startDate The starting date of the range (inclusive).
     * @param endDate The ending date of the range (inclusive).
     * @return A [Flow] emitting pairs of [DiaryDate] and [CalorieConsumptionStatus] for each date in the range.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(startDate: DiaryDate, endDate: DiaryDate): Flow<Pair<DiaryDate,CalorieConsumptionStatus>> {
      return (startDate..endDate)
          .asFlow()
          .flatMapMerge { date ->
              getCaloriesConsumptionForDateUseCase(date).map {
                  date to it
              }
          }
    }
}