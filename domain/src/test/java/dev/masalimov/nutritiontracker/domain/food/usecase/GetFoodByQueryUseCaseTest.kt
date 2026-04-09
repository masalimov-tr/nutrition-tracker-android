package dev.masalimov.nutritiontracker.domain.food.usecase

import dev.masalimov.nutritiontracker.domain.food.Food
import dev.masalimov.nutritiontracker.domain.food.FoodId
import dev.masalimov.nutritiontracker.domain.food.FoodRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class GetFoodByQueryUseCaseTest {

    @Test
    fun blankQuery_returnsSavedOnly_andDoesNotSearch() = runBlocking {
        // Arrange
        val saved = listOf(
            Food(FoodId(1), "Apple", 52.0, 0.3, 0.2, 14.0),
            Food(FoodId(2), "Banana", 96.0, 1.3, 0.3, 27.0),
        )
        val fakeRepo = FakeFoodRepository(
            allFood = saved,
            searchFoodResponse = emptyList(),
        )
        val useCase = GetFoodByQueryUseCase(fakeRepo)

        // Act
        val result = useCase.invoke("")

        // Assert
        assertEquals(saved, result.savedFood)
        assertEquals(emptyList<Food>(), result.searchFood)
        assertEquals(emptyList<String>(), fakeRepo.queries)
    }

    @Test
    fun nonBlankQuery_filtersSavedCaseInsensitive_andKeepsSearched() = runBlocking {
        // Arrange
        val saved = listOf(
            Food(FoodId(1), "Apple", 52.0, 0.3, 0.2, 14.0),
            Food(FoodId(2), "Banana", 96.0, 1.3, 0.3, 27.0),
            Food(FoodId(4), "grape", 69.0, 0.7, 0.2, 18.0),
        )
        val searched = listOf(
            Food(FoodId(3), "Apricot", 48.0, 1.4, 0.4, 11.0),
        )
        val fakeRepo = FakeFoodRepository(
            allFood = saved,
            searchFoodResponse = searched,
        )
        val useCase = GetFoodByQueryUseCase(fakeRepo)

        // Act
        val resultLower = useCase.invoke("ap")
        val resultUpper = useCase.invoke("AP")

        // Assert
        assertEquals(listOf(saved[0], saved[2]), resultLower.savedFood) // Apple, grape
        assertEquals(searched, resultLower.searchFood)
        assertEquals(listOf(saved[0], saved[2]), resultUpper.savedFood) // Case-insensitive
        assertEquals(searched, resultUpper.searchFood)
    }

    @Test
    fun nonMatchingQuery_yieldsEmptySaved_and_yieldsEmptySearched() = runBlocking {
        // Arrange
        val saved = listOf(
            Food(FoodId(1), "Apple", 52.0, 0.3, 0.2, 14.0),
            Food(FoodId(2), "Banana", 96.0, 1.3, 0.3, 27.0),
        )

        val fakeRepo = FakeFoodRepository(
            allFood = saved,
            searchFoodResponse = emptyList(),
        )
        val useCase = GetFoodByQueryUseCase(fakeRepo)

        // Act
        val result = useCase.invoke("zzz")

        // Assert
        assertEquals(emptyList<Food>(), result.savedFood)
        assertEquals(emptyList<Food>(), result.searchFood)
        assertEquals(listOf("zzz"), fakeRepo.queries)
    }

    private class FakeFoodRepository(
        private val allFood: List<Food> = emptyList(),
        private val searchFoodResponse: List<Food> = emptyList(),
    ) : FoodRepository {
        val queries = mutableListOf<String>()
        override suspend fun getAllFood(): List<Food> = allFood
        override fun getAllFoodStream(): Flow<List<Food>> = flowOf(allFood)
        override suspend fun getFoodById(foodId: FoodId): Food = allFood.first { it.id == foodId }
        override suspend fun searchFood(query: String): List<Food> {
            queries += query
            return searchFoodResponse
        }
        override suspend fun deleteFood(foodId: Long) { /* no-op */ }
        override suspend fun getSuggestedFood(eatenFood: List<Food>): List<Food> = emptyList()
    }
}
