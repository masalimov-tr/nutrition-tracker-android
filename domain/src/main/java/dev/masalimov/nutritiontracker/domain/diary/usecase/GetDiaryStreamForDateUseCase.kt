package dev.masalimov.nutritiontracker.domain.diary.usecase

import dev.masalimov.nutritiontracker.domain.diary.DiaryRepository
import dev.masalimov.nutritiontracker.domain.diary.model.CompleteDiaryInformationForDate
import dev.masalimov.nutritiontracker.domain.diary.model.DiaryDate
import dev.masalimov.nutritiontracker.domain.diary.model.DiaryEntryForDate
import dev.masalimov.nutritiontracker.domain.food.FoodRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transform
import javax.inject.Inject


/**
 * Use case for retrieving a real-time stream of diary information for a specific date.
 *
 * This class fetches a diary entry for the provided date from the [DiaryRepository]. If no diary entry exists for the
 * specified date, it emits an empty [CompleteDiaryInformationForDate]. Otherwise, it computes suggested food based on the diary's
 * eaten food using the [FoodRepository] and emits the resulting [CompleteDiaryInformationForDate].
 *
 * @property diaryRepository Repository for accessing diary data.
 * @property foodRepository Repository for accessing food data and suggestions.
 */
class GetDiaryStreamForDateUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository,
    private val foodRepository: FoodRepository,
) {
    /**
     * Invokes the use case for a given date and retrieves a stream of [CompleteDiaryInformationForDate].
     *
     * @param date The date for which the diary information is requested.
     * @return A [Flow] emitting [CompleteDiaryInformationForDate]. Emits [CompleteDiaryInformationForDate.EMPTY] if no diary entry exists for the date.
     * Otherwise, emits detailed information including the diary and suggested food.
     */
    operator fun invoke(date: DiaryDate): Flow<CompleteDiaryInformationForDate> {
        return diaryRepository.getDiaryByDateFlow(date)
            .transform { diary: DiaryEntryForDate? ->
                delay(1000)
                if (diary == null) {
                    emit(CompleteDiaryInformationForDate.EMPTY)
                    return@transform
                }

                val suggestedFood =
                    foodRepository.getSuggestedFood(diary.eatenFood.map { it.food })
                emit(
                    CompleteDiaryInformationForDate(
                        diary,
                        suggestedFood,
                    )
                )
            }
    }

}