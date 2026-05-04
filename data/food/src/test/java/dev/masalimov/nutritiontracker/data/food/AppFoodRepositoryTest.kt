package dev.masalimov.nutritiontracker.data.food

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import dev.masalimov.nutritiontracker.core.database.food.FoodDao
import dev.masalimov.nutritiontracker.core.database.food.FoodEntity
import dev.masalimov.nutritiontracker.data.food.datasource.FoodRemoteDataSource
import dev.masalimov.nutritiontracker.domain.food.Food
import dev.masalimov.nutritiontracker.domain.food.FoodId
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.runs
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class AppFoodRepositoryTest {

    @MockK
    private lateinit var foodDao: FoodDao

    @MockK
    private lateinit var foodRemoteDataSource: FoodRemoteDataSource

    private lateinit var repo: AppFoodRepository

    private val appleEntity = FoodEntity(
        uid = 1L,
        name = "Apple",
        caloriesPer100g = 52.0,
        proteinPer100g = 0.3,
        fatPer100g = 0.2,
        carbsPer100g = 14.0,
    )
    private val appleFood = Food(FoodId(1), "Apple", 52.0, 0.3, 0.2, 14.0)

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        repo = AppFoodRepository(foodDao, foodRemoteDataSource)
    }

    // ── getAllFood ────────────────────────────────────────────────────────────

    @Test
    fun `getAllFood returns mapped domain Food list`() = runTest {
        coEvery { foodDao.getAll() } returns listOf(appleEntity)

        val result = repo.getAllFood()

        assertThat(result).isEqualTo(listOf(appleFood))
    }

    @Test
    fun `getAllFood returns empty list when DAO is empty`() = runTest {
        coEvery { foodDao.getAll() } returns emptyList()

        assertThat(repo.getAllFood()).isEmpty()
    }

    // ── getAllFoodStream ───────────────────────────────────────────────────────

    @Test
    fun `getAllFoodStream emits mapped Food list`() = runTest {
        every { foodDao.getAllStream() } returns flowOf(listOf(appleEntity))

        repo.getAllFoodStream().test {
            assertThat(awaitItem()).isEqualTo(listOf(appleFood))
            awaitComplete()
        }
    }

    // ── getFoodById ───────────────────────────────────────────────────────────

    @Test
    fun `getFoodById returns mapped Food for given id`() = runTest {
        coEvery { foodDao.getById(1L) } returns appleEntity

        val result = repo.getFoodById(FoodId(1))

        assertThat(result).isEqualTo(appleFood)
    }

    // ── getSuggestedFood ──────────────────────────────────────────────────────

    @Test
    fun `getSuggestedFood returns up to 5 foods from DAO`() = runTest {
        val entities = (1..6).map { i ->
            FoodEntity(uid = i.toLong(), name = "Food $i", caloriesPer100g = 100.0, proteinPer100g = 0.0, fatPer100g = 0.0, carbsPer100g = 0.0)
        }
        coEvery { foodDao.getAll() } returns entities

        val result = repo.getSuggestedFood(emptyList())

        assertThat(result).hasSize(5)
    }

    @Test
    fun `getSuggestedFood returns empty list when DAO is empty`() = runTest {
        coEvery { foodDao.getAll() } returns emptyList()

        assertThat(repo.getSuggestedFood(emptyList())).isEmpty()
    }

    // ── searchFood ────────────────────────────────────────────────────────────

    @Test
    fun `searchFood returns mapped results from remote data source`() = runTest {
        val apiModel = FoodApiModel(id = 1L, name = "Apple From Api", caloriesPer100g = 52.0)
        coEvery { foodRemoteDataSource.searchFood("apple") } returns listOf(apiModel)

        val result = repo.searchFood("apple")

        assertThat(result).hasSize(1)
        assertThat(result.first().name).isEqualTo("Apple From Api")
        assertThat(result.first().caloriesPer100g).isEqualTo(52.0)
    }

    @Test
    fun `searchFood returns empty list when no API results`() = runTest {
        coEvery { foodRemoteDataSource.searchFood(any()) } returns emptyList()

        assertThat(repo.searchFood("xyz")).isEmpty()
    }

    // ── deleteFood ────────────────────────────────────────────────────────────

    @Test
    fun `deleteFood calls DAO deleteById with correct id`() = runTest {
        coEvery { foodDao.deleteById(1L) } just runs

        repo.deleteFood(1L)

        coVerify(exactly = 1) { foodDao.deleteById(1L) }
    }
}
