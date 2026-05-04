package dev.masalimov.nutritiontracker.core.database.food

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import dev.masalimov.nutritiontracker.core.database.NutritionAppDatabase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
class FoodDaoTest {

    private lateinit var db: NutritionAppDatabase
    private lateinit var dao: FoodDao

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, NutritionAppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.foodDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    // ── getAll ────────────────────────────────────────────────────────────────

    @Test
    fun getAll_returnsOnlySavedEntities() = runTest {
        dao.insert(FoodEntity(uid = 0, name = "Apple", caloriesPer100g = 52.0, proteinPer100g = 0.3, fatPer100g = 0.2, carbsPer100g = 14.0, isSaved = true))
        dao.insert(FoodEntity(uid = 0, name = "Deleted food", caloriesPer100g = 100.0, proteinPer100g = 0.0, fatPer100g = 0.0, carbsPer100g = 0.0, isSaved = false))

        val result = dao.getAll()

        assertThat(result).hasSize(1)
        assertThat(result.first().name).isEqualTo("Apple")
    }

    @Test
    fun getAll_returnsEmptyList_whenNoFoodSaved() = runTest {
        assertThat(dao.getAll()).isEmpty()
    }

    // ── insert ────────────────────────────────────────────────────────────────

    @Test
    fun insert_returnsAssignedId() = runTest {
        val id = dao.insert(FoodEntity(uid = 0, name = "Apple", caloriesPer100g = 52.0, proteinPer100g = 0.3, fatPer100g = 0.2, carbsPer100g = 14.0))
        assertThat(id).isGreaterThan(0L)
    }

    // ── insertAll ─────────────────────────────────────────────────────────────

    @Test
    fun insertAll_insertsAllEntities() = runTest {
        dao.insertAll(
            listOf(
                FoodEntity(uid = 0, name = "Apple", caloriesPer100g = 52.0, proteinPer100g = 0.3, fatPer100g = 0.2, carbsPer100g = 14.0),
                FoodEntity(uid = 1, name = "Banana", caloriesPer100g = 89.0, proteinPer100g = 1.1, fatPer100g = 0.3, carbsPer100g = 23.0),
            )
        )
        assertThat(dao.getAll()).hasSize(2)
    }

    // ── getById ───────────────────────────────────────────────────────────────

    @Test
    fun getById_returnsCorrectEntity() = runTest {
        val id = dao.insert(FoodEntity(uid = 0, name = "Apple", caloriesPer100g = 52.0, proteinPer100g = 0.3, fatPer100g = 0.2, carbsPer100g = 14.0))

        val entity = dao.getById(id)

        assertThat(entity.name).isEqualTo("Apple")
        assertThat(entity.caloriesPer100g).isEqualTo(52.0)
    }

    // ── deleteById ────────────────────────────────────────────────────────────

    @Test
    fun deleteById_softDeletesEntity_removedFromGetAll() = runTest {
        val id = dao.insert(FoodEntity(uid = 0, name = "Apple", caloriesPer100g = 52.0, proteinPer100g = 0.3, fatPer100g = 0.2, carbsPer100g = 14.0))

        dao.deleteById(id)

        assertThat(dao.getAll()).isEmpty()
    }

    @Test
    fun deleteById_softDeletesEntity_stillReturnedByGetById() = runTest {
        // Soft delete: isSaved = false, but the row is not physically removed
        val id = dao.insert(FoodEntity(uid = 0, name = "Apple", caloriesPer100g = 52.0, proteinPer100g = 0.3, fatPer100g = 0.2, carbsPer100g = 14.0))

        dao.deleteById(id)

        val entity = dao.getById(id)
        assertThat(entity.isSaved).isFalse()
        assertThat(entity.name).isEqualTo("Apple")
    }

    // ── countAll ──────────────────────────────────────────────────────────────

    @Test
    fun countAll_returnsZero_whenTableEmpty() = runTest {
        assertThat(dao.countAll()).isEqualTo(0L)
    }

    @Test
    fun countAll_includesAllRows_regardlessOfIsSaved() = runTest {
        dao.insert(FoodEntity(uid = 0, name = "Apple", caloriesPer100g = 52.0, proteinPer100g = 0.3, fatPer100g = 0.2, carbsPer100g = 14.0, isSaved = true))
        dao.insert(FoodEntity(uid = 0, name = "Deleted", caloriesPer100g = 100.0, proteinPer100g = 0.0, fatPer100g = 0.0, carbsPer100g = 0.0, isSaved = false))

        assertThat(dao.countAll()).isEqualTo(2L)
    }

    // ── getAllStream ──────────────────────────────────────────────────────────

    @Test
    fun getAllStream_emitsEmptyList_initially() = runTest {
        dao.getAllStream().test {
            assertThat(awaitItem()).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getAllStream_emitsUpdatedList_afterInsert() = runTest {
        dao.getAllStream().test {
            assertThat(awaitItem()).isEmpty()

            dao.insert(FoodEntity(uid = 0, name = "Apple", caloriesPer100g = 52.0, proteinPer100g = 0.3, fatPer100g = 0.2, carbsPer100g = 14.0))

            assertThat(awaitItem()).hasSize(1)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getAllStream_emitsUpdatedList_afterDelete() = runTest {
        val id = dao.insert(FoodEntity(uid = 0, name = "Apple", caloriesPer100g = 52.0, proteinPer100g = 0.3, fatPer100g = 0.2, carbsPer100g = 14.0))

        dao.getAllStream().test {
            assertThat(awaitItem()).hasSize(1)

            dao.deleteById(id)

            assertThat(awaitItem()).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }
}
