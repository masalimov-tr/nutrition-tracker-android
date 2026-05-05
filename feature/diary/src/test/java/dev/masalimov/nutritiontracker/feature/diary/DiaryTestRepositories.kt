package dev.masalimov.nutritiontracker.feature.diary

import dev.masalimov.nutritiontracker.domain.diary.DiaryRepository
import dev.masalimov.nutritiontracker.domain.diary.model.DiaryDate
import dev.masalimov.nutritiontracker.domain.diary.model.DiaryEntryForDate
import dev.masalimov.nutritiontracker.domain.diary.model.DiaryId
import dev.masalimov.nutritiontracker.domain.diary.model.EatenFood
import dev.masalimov.nutritiontracker.domain.food.Food
import dev.masalimov.nutritiontracker.domain.food.FoodId
import dev.masalimov.nutritiontracker.domain.food.FoodRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class InMemoryFoodRepository : FoodRepository {
    private val foods: MutableMap<Long, Food> = mutableMapOf()

    fun addFood(food: Food) { foods[food.id.id] = food }

    fun getSync(foodId: FoodId): Food =
        foods[foodId.id] ?: error("Food not found: ${foodId.id}")

    override suspend fun getAllFood(): List<Food> = foods.values.toList()
    override fun getAllFoodStream(): Flow<List<Food>> =
        MutableStateFlow(foods.values.toList()).asStateFlow()
    override suspend fun getFoodById(foodId: FoodId): Food = getSync(foodId)
    override suspend fun getSuggestedFood(eatenFood: List<Food>): List<Food> =
        foods.values.filter { it !in eatenFood }.take(5)
    override suspend fun searchFood(query: String): List<Food> = emptyList()
    override suspend fun deleteFood(foodId: Long) { foods.remove(foodId) }
}

internal class InMemoryDiaryRepository(
    private val foodRepository: InMemoryFoodRepository,
) : DiaryRepository {
    private val entries: MutableMap<Long, DiaryEntryForDate> = mutableMapOf()
    private val flows: MutableMap<Long, MutableStateFlow<DiaryEntryForDate?>> = mutableMapOf()

    override fun getDiaryByDateFlow(date: DiaryDate): Flow<DiaryEntryForDate?> {
        val key = epochKey(date)
        return flows.getOrPut(key) { MutableStateFlow(entries[key]) }.asStateFlow()
    }

    override suspend fun addFoodToDiary(foodId: Long, date: DiaryDate, quantityGrams: Double) {
        val key = epochKey(date)
        val food = foodRepository.getSync(FoodId(foodId))
        val current = entries[key]
        val updated = current?.copy(
            eatenFood = current.eatenFood + EatenFood(quantityGram = quantityGrams, food = food)
        ) ?: DiaryEntryForDate(
            id = DiaryId(key),
            date = date,
            eatenFood = listOf(EatenFood(quantityGram = quantityGrams, food = food)),
            goalCaloriesPerDay = 2000,
        )
        entries[key] = updated
        flows.getOrPut(key) { MutableStateFlow(null) }.value = updated
    }

    private fun epochKey(date: DiaryDate): Long = date.date.toEpochDays().toLong()
}
