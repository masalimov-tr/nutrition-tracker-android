package dev.masalimov.nutritiontracker.core.database.diary

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import dev.masalimov.nutritiontracker.core.database.NutritionAppDatabase
import dev.masalimov.nutritiontracker.core.database.food.FoodDao
import dev.masalimov.nutritiontracker.core.database.food.FoodEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
class DiaryDaoTest {

    private lateinit var db: NutritionAppDatabase
    private lateinit var dao: DiaryDao
    private lateinit var foodDao: FoodDao

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, NutritionAppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.diaryDao()
        foodDao = db.foodDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    // region getDiaryForDate

    @Test
    fun getDiaryForDate_returnsNull_whenNoDiaryExists() = runTest {
        val result = dao.getDiaryForDate(epochDay = 1000)
        assertThat(result).isNull()
    }

    // endregion

    // region addFoodToDiaryTx

    @Test
    fun addFoodToDiaryTx_createsNewDiaryAndCrossRef_whenNoneExists() = runTest {
        val foodId = insertFood("Apple")

        dao.addFoodToDiaryTx(
            epochDay = 1000,
            goalCaloriesPerDay = 2000,
            foodId = foodId,
            quantityGrams = 150.0,
        )

        val entry = dao.getDiaryForDate(epochDay = 1000)
        assertThat(entry).isNotNull()
        assertThat(entry!!.diaryEntry.dateEpochDay).isEqualTo(1000)
        assertThat(entry.diaryEntry.goalCaloriesPerDay).isEqualTo(2000)
        assertThat(entry.items).hasSize(1)
        assertThat(entry.items[0].link.foodId).isEqualTo(foodId)
        assertThat(entry.items[0].link.quantityGrams).isEqualTo(150.0)
    }

    @Test
    fun addFoodToDiaryTx_reusesExistingDiaryEntry_whenOneAlreadyExistsForDate() = runTest {
        val foodId1 = insertFood("Apple")
        val foodId2 = insertFood("Banana")

        dao.addFoodToDiaryTx(epochDay = 1000, goalCaloriesPerDay = 2000, foodId = foodId1, quantityGrams = 100.0)
        val diaryIdAfterFirst = dao.getDiaryForDate(1000)!!.diaryEntry.uid

        dao.addFoodToDiaryTx(epochDay = 1000, goalCaloriesPerDay = 2000, foodId = foodId2, quantityGrams = 200.0)
        val entryAfterSecond = dao.getDiaryForDate(1000)!!

        assertThat(entryAfterSecond.diaryEntry.uid).isEqualTo(diaryIdAfterFirst)
        assertThat(entryAfterSecond.items).hasSize(2)
    }

    @Test
    fun addFoodToDiaryTx_storesQuantityGrams_correctly() = runTest {
        val foodId = insertFood("Chicken")
        dao.addFoodToDiaryTx(epochDay = 2000, goalCaloriesPerDay = 1500, foodId = foodId, quantityGrams = 250.0)

        val crossRefQuantity = dao.getDiaryForDate(2000)!!.items[0].link.quantityGrams
        assertThat(crossRefQuantity).isEqualTo(250.0)
    }

    @Test
    fun addFoodToDiaryTx_isDateIsolated_differentDatesDoNotInterfere() = runTest {
        val appleId = insertFood("Apple")
        val chickenId = insertFood("Chicken")

        dao.addFoodToDiaryTx(epochDay = 100, goalCaloriesPerDay = 2000, foodId = appleId, quantityGrams = 100.0)
        dao.addFoodToDiaryTx(epochDay = 200, goalCaloriesPerDay = 2000, foodId = chickenId, quantityGrams = 200.0)

        val day100 = dao.getDiaryForDate(100)!!
        val day200 = dao.getDiaryForDate(200)!!

        assertThat(day100.items).hasSize(1)
        assertThat(day100.items[0].food.name).isEqualTo("Apple")
        assertThat(day200.items).hasSize(1)
        assertThat(day200.items[0].food.name).isEqualTo("Chicken")
    }

    // endregion

    // region deleteDiary

    @Test
    fun deleteDiary_removesEntry_andCascadesDeleteToCrossRefRows() = runTest {
        val foodId = insertFood("Apple")
        dao.addFoodToDiaryTx(epochDay = 1000, goalCaloriesPerDay = 2000, foodId = foodId, quantityGrams = 100.0)
        val diaryId = dao.getDiaryForDate(1000)!!.diaryEntry.uid

        dao.deleteDiary(diaryId)

        // Entry is gone
        assertThat(dao.getDiaryForDate(1000)).isNull()

        // Re-adding food creates a fresh diary with no leftover cross-refs from deleted entry
        dao.addFoodToDiaryTx(epochDay = 1000, goalCaloriesPerDay = 2000, foodId = foodId, quantityGrams = 50.0)
        val newEntry = dao.getDiaryForDate(1000)!!
        assertThat(newEntry.items).hasSize(1)
        assertThat(newEntry.items[0].link.quantityGrams).isEqualTo(50.0)
    }

    // endregion

    // region getDiaryForDateFlow

    @Test
    fun getDiaryForDateFlow_emitsNull_thenUpdatedEntry_whenFoodAdded() = runTest {
        val foodId = insertFood("Apple")

        dao.getDiaryForDateFlow(epochDay = 1000).test {
            assertThat(awaitItem()).isNull()

            dao.addFoodToDiaryTx(epochDay = 1000, goalCaloriesPerDay = 2000, foodId = foodId, quantityGrams = 150.0)

            val updated = awaitItem()
            assertThat(updated).isNotNull()
            assertThat(updated!!.items).hasSize(1)
            assertThat(updated.items[0].food.name).isEqualTo("Apple")
            assertThat(updated.items[0].link.quantityGrams).isEqualTo(150.0)
        }
    }

    @Test
    fun getDiaryForDateFlow_doesNotEmitForOtherDate_whenFoodAddedToDifferentDate() = runTest {
        val foodId = insertFood("Apple")

        dao.getDiaryForDateFlow(epochDay = 999).test {
            assertThat(awaitItem()).isNull()

            // Add food to a different date (1000, not 999)
            dao.addFoodToDiaryTx(epochDay = 1000, goalCaloriesPerDay = 2000, foodId = foodId, quantityGrams = 100.0)

            expectNoEvents()
        }
    }

    // endregion

    private suspend fun insertFood(name: String): Long {
        return foodDao.insert(
            FoodEntity(
                name = name,
                caloriesPer100g = 100.0,
                proteinPer100g = 10.0,
                fatPer100g = 5.0,
                carbsPer100g = 20.0,
            )
        )
    }
}
