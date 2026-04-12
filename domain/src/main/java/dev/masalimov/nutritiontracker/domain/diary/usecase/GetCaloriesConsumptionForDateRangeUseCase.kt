package dev.masalimov.nutritiontracker.domain.diary.usecase

import dev.masalimov.nutritiontracker.domain.diary.CalorieConsumptionStatus
import dev.masalimov.nutritiontracker.domain.diary.model.DiaryDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetCaloriesConsumptionForDateRangeUseCase @Inject constructor(
    private val getCaloriesConsumptionForDateUseCase: GetCaloriesConsumptionForDateUseCase,
) {
    /**
     * Retrieves a stream of calorie consumption statuses for a date range.
     *
     * This function takes a range of dates and calculates the calorie consumption status for each date
     * by invoking the `getCaloriesConsumptionForDateUseCase`. The results are combined into a map where each
     * key is a date and its corresponding value is the calorie consumption status for that date.
     *
     * @param startDate The start date of the range for which to calculate the calorie consumption statuses.
     * @param endDate The end date of the range for which to calculate the calorie consumption statuses.
     * @return A [Flow] emitting a map where the key is a [DiaryDate] and the value is the corresponding
     * [CalorieConsumptionStatus] for that date.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(
        startDate: DiaryDate,
        endDate: DiaryDate
    ): Flow<Map<DiaryDate, CalorieConsumptionStatus>> {
        val dates = (startDate..endDate).toList()

        val flows = dates.map { date ->
            getCaloriesConsumptionForDateUseCase(date).map { status ->
                date to status
            }
        }

        return combine(flows) {
            it.toMap()
        }
    }
}