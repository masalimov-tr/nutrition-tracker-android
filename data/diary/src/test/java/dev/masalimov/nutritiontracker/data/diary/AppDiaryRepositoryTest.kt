@file:OptIn(ExperimentalCoroutinesApi::class)

package dev.masalimov.nutritiontracker.data.diary

import com.google.common.truth.Truth.assertThat
import dev.masalimov.nutritiontracker.core.database.diary.DiaryDao
import dev.masalimov.nutritiontracker.core.database.diary.DiaryEntity
import dev.masalimov.nutritiontracker.core.database.diary.DiaryFoodPortion
import dev.masalimov.nutritiontracker.core.database.diary.DiaryFoodPortionCrossRef
import dev.masalimov.nutritiontracker.core.database.diary.DiaryWithFoodPortions
import dev.masalimov.nutritiontracker.core.database.food.exampleFoodEntity
import dev.masalimov.nutritiontracker.domain.GoalCalories
import dev.masalimov.nutritiontracker.domain.diary.model.DiaryDate
import dev.masalimov.nutritiontracker.domain.diary.model.DiaryEntryForDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test

class AppDiaryRepositoryTest {

    @Test
    fun addFoodToDiary_insertsNewDiaryAndCrossRef_whenNoEntryForDate() = runBlocking {
        val fakeDao = FakeDiaryDao()
        val goalCalories = GoalCalories()
        val repository = AppDiaryRepository(fakeDao, goalCalories)

        val epochDay = 12345
        val date = DiaryDate.of(epochDay)
        val foodId = 99L
        val quantity = 250.0 // repository currently uses 100.0 regardless

        repository.addFoodToDiary(foodId = foodId, date = date, quantityGrams = quantity)

        // Verify diary inserted once with expected fields
        assertThat(fakeDao.insertedDiaryEntities.value).hasSize(1)
        val insertedDiary = fakeDao.insertedDiaryEntities.value.first()
        assertThat(insertedDiary.diaryEntry.dateEpochDay).isEqualTo(epochDay)
        assertThat(insertedDiary.diaryEntry.goalCaloriesPerDay).isEqualTo(goalCalories.caloriesPerDay)

        // Verify cross-ref inserted with the new diary id and passed quantity
        assertThat(fakeDao.insertedCrossRefs.value).hasSize(1)
        val crossRef = fakeDao.insertedCrossRefs.value.first()
        assertThat(crossRef.diaryEntryId).isEqualTo(insertedDiary.diaryEntry.uid)
        assertThat(crossRef.foodId).isEqualTo(foodId)
        assertThat(crossRef.quantityGrams).isEqualTo(quantity)
    }

    @Test
    fun addFoodToDiary_usesExistingDiaryIdAndDoesNotInsertDiary_whenEntryExistsYet() = runBlocking {
        val fakeDao = FakeDiaryDao()
        val goalCalories = GoalCalories()
        val repository = AppDiaryRepository(fakeDao, goalCalories)

        val epochDay = 22222
        val date = DiaryDate.of(epochDay)
        val existingDiary =
            DiaryEntity(uid = 10L, dateEpochDay = epochDay, goalCaloriesPerDay = 1111)
        fakeDao.presetDiary(existingDiary) // insertedDiaryEntities.size 1
        assertThat(fakeDao.insertedDiaryEntities.value).hasSize(1)

        val foodId = 7L
        val quantity = 50.0

        repository.addFoodToDiary(foodId = foodId, date = date, quantityGrams = quantity)

        // Should not insert a new diary entity
        assertThat(fakeDao.insertedDiaryEntities.value).hasSize(1)

        // Should insert exactly one cross-ref pointing to existing uid with passed quantity
        assertThat(fakeDao.insertedCrossRefs.value).hasSize(1)
        val crossRef = fakeDao.insertedCrossRefs.value.first()
        assertThat(crossRef.diaryEntryId).isEqualTo(existingDiary.uid)
        assertThat(crossRef.foodId).isEqualTo(foodId)
        assertThat(crossRef.quantityGrams).isEqualTo(quantity)
    }

    @Test
    fun addFoodToDiary_emitsOnyOneDiary_withNotEmptyFood_whenEntryDoesntExist() = runTest {
        val fakeDao = FakeDiaryDao()
        val goalCalories = GoalCalories()
        val repository = AppDiaryRepository(fakeDao, goalCalories)

        val date = DiaryDate.of(12345)
        val emittedDiaries = mutableListOf<DiaryEntryForDate?>()
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            repository.getDiaryByDateFlow(date).collect {
                emittedDiaries.add(it)
            }
        }

        // First emission should be null since no diary exists for the date
        assertThat(emittedDiaries).hasSize(1)
        assertThat(emittedDiaries.first()).isEqualTo(null)

        repository.addFoodToDiary(foodId = 1L, date = date, quantityGrams = 100.0)
        advanceUntilIdle()

        // Second emission should be created new diary
        assertThat(emittedDiaries).hasSize(2)
        assertThat(emittedDiaries[1]?.eatenFood).isNotEmpty()

    }

    private class FakeDiaryDao : DiaryDao {
        val insertedDiaryEntities = MutableStateFlow<List<DiaryWithFoodPortions>>(emptyList())
        val insertedCrossRefs = MutableStateFlow<List<DiaryFoodPortionCrossRef>>(emptyList())

        private var nextId: Long = 1L

        fun presetDiary(diary: DiaryEntity) {
            insertedDiaryEntities.value += DiaryWithFoodPortions(
                diary,
                emptyList()
            )
        }

        override suspend fun insert(diaryEntity: DiaryEntity): Long {
            throw UnsupportedOperationException("Not used by repository in tests; use addFoodToDiaryTx")
        }

        override suspend fun insertCrossRef(crossRef: DiaryFoodPortionCrossRef) {
            throw UnsupportedOperationException("Not used by repository in tests; use addFoodToDiaryTx")
        }

        override suspend fun addFoodToDiaryTx(
            epochDay: Int,
            goalCaloriesPerDay: Int,
            foodId: Long,
            quantityGrams: Double,
        ) {
            val existing = insertedDiaryEntities.value.firstOrNull { it.diaryEntry.dateEpochDay == epochDay }
            val newList = if (existing == null) {
                val newDiary = DiaryEntity(
                    uid = nextId++,
                    dateEpochDay = epochDay,
                    goalCaloriesPerDay = goalCaloriesPerDay,
                )
                val link = DiaryFoodPortionCrossRef(
                    diaryEntryId = newDiary.uid,
                    foodId = foodId,
                    quantityGrams = quantityGrams,
                )
                insertedCrossRefs.value += link
                insertedDiaryEntities.value + DiaryWithFoodPortions(
                    diaryEntry = newDiary,
                    items = listOf(
                        DiaryFoodPortion(
                            link = link,
                            food = exampleFoodEntity,
                        )
                    )
                )
            } else {
                val link = DiaryFoodPortionCrossRef(
                    diaryEntryId = existing.diaryEntry.uid,
                    foodId = foodId,
                    quantityGrams = quantityGrams,
                )
                insertedCrossRefs.value += link
                insertedDiaryEntities.value.map {
                    if (it.diaryEntry.uid == existing.diaryEntry.uid)
                        it.copy(items = it.items + DiaryFoodPortion(link = link, food = exampleFoodEntity))
                    else it
                }
            }
            // single atomic-like emission for the diary list
            insertedDiaryEntities.value = newList
        }

        override suspend fun getDiaryForDate(epochDay: Int): DiaryWithFoodPortions? {
            return insertedDiaryEntities.value
                .firstOrNull { it.diaryEntry.dateEpochDay == epochDay }
        }

        override fun getDiaryForDateFlow(epochDay: Int): Flow<DiaryWithFoodPortions?> {
            return insertedDiaryEntities
                .map { entities ->
                    entities.firstOrNull { it.diaryEntry.dateEpochDay == epochDay }
                }
        }

        override suspend fun deleteDiary(entryId: Long) {
            throw UnsupportedOperationException("Not used by repository in tests; use addFoodToDiaryTx")
        }
        
    }

}
