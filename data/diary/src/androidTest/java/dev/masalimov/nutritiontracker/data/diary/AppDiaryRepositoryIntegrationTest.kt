package dev.masalimov.nutritiontracker.data.diary

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import dev.masalimov.nutritiontracker.core.database.NutritionAppDatabase
import dev.masalimov.nutritiontracker.core.database.food.FoodEntity
import dev.masalimov.nutritiontracker.domain.GoalCalories
import dev.masalimov.nutritiontracker.domain.diary.model.DiaryDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration test for AppDiaryRepository against a real in-memory Room database.
 * Verifies that the full chain — DAO queries + domain mapping — produces the correct
 * domain model after diary mutations.
 */
@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
class AppDiaryRepositoryIntegrationTest {

    private lateinit var db: NutritionAppDatabase
    private lateinit var repository: AppDiaryRepository

    private val goalCalories = GoalCalories()

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, NutritionAppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = AppDiaryRepository(db.diaryDao(), goalCalories)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun getDiaryByDateFlow_emitsNull_whenNoDiaryExists() = runTest {
        repository.getDiaryByDateFlow(DiaryDate.of(1000)).test {
            assertThat(awaitItem()).isNull()
        }
    }

    @Test
    fun getDiaryByDateFlow_emitsUpdatedDomainModel_afterAddFoodToDiary() = runTest {
        val date = DiaryDate.of(1000)
        val foodId = insertFood(name = "Apple", caloriesPer100g = 52.0)

        repository.getDiaryByDateFlow(date).test {
            assertThat(awaitItem()).isNull()

            repository.addFoodToDiary(foodId = foodId, date = date, quantityGrams = 150.0)

            val entry = awaitItem()
            assertThat(entry).isNotNull()
            assertThat(entry!!.eatenFood).hasSize(1)
            assertThat(entry.eatenFood[0].food.name).isEqualTo("Apple")
            assertThat(entry.eatenFood[0].quantityGram).isEqualTo(150.0)
            // 150g * 52 cal/100g = 78
            assertThat(entry.eatenFood[0].calories).isEqualTo(78)
            assertThat(entry.caloriesEaten).isEqualTo(78)
        }
    }

    @Test
    fun addFoodToDiary_usesGoalCaloriesFromGoalCalories_instance() = runTest {
        val date = DiaryDate.of(2000)
        val foodId = insertFood("Chicken")

        repository.addFoodToDiary(foodId = foodId, date = date, quantityGrams = 100.0)

        repository.getDiaryByDateFlow(date).test {
            val entry = awaitItem()
            assertThat(entry!!.goalCaloriesPerDay).isEqualTo(goalCalories.caloriesPerDay)
        }
    }

    @Test
    fun getDiaryByDateFlow_accumulates_whenMultipleFoodsAdded() = runTest {
        val date = DiaryDate.of(3000)
        val appleId = insertFood(name = "Apple", caloriesPer100g = 52.0)
        val chickenId = insertFood(name = "Chicken", caloriesPer100g = 165.0)

        repository.getDiaryByDateFlow(date).test {
            assertThat(awaitItem()).isNull()

            repository.addFoodToDiary(foodId = appleId, date = date, quantityGrams = 100.0)
            val afterApple = awaitItem()!!
            assertThat(afterApple.eatenFood).hasSize(1)
            // 100g * 52/100 = 52
            assertThat(afterApple.caloriesEaten).isEqualTo(52)

            repository.addFoodToDiary(foodId = chickenId, date = date, quantityGrams = 200.0)
            val afterChicken = awaitItem()!!
            assertThat(afterChicken.eatenFood).hasSize(2)
            // Apple (52) + Chicken 200g * 165/100 = 330 → total 382
            assertThat(afterChicken.caloriesEaten).isEqualTo(382)
        }
    }

    @Test
    fun getDiaryByDateFlow_isDateIsolated_flowForDateANotEmitted_whenFoodAddedToDateB() = runTest {
        val dateA = DiaryDate.of(100)
        val dateB = DiaryDate.of(200)
        val foodId = insertFood("Apple")

        repository.getDiaryByDateFlow(dateA).test {
            assertThat(awaitItem()).isNull()

            repository.addFoodToDiary(foodId = foodId, date = dateB, quantityGrams = 100.0)

            expectNoEvents()
        }
    }

    private suspend fun insertFood(name: String, caloriesPer100g: Double = 100.0): Long {
        return db.foodDao().insert(
            FoodEntity(
                name = name,
                caloriesPer100g = caloriesPer100g,
                proteinPer100g = 10.0,
                fatPer100g = 5.0,
                carbsPer100g = 20.0,
            )
        )
    }
}
