package dev.masalimov.nutritiontracker.data.food

import javax.inject.Inject


class FoodApiFakeImpl @Inject constructor(): FoodApi {
    private val apiFood = listOf(
        FoodApiModel(1, "Apple From Api", 52.0),
        FoodApiModel(2, "Banana From Api", 96.0),
        FoodApiModel(3, "Orange From Api", 47.0),
        FoodApiModel(4, "Coca-cola", 300.0),
    )

    override suspend fun searchFood(query: String): List<FoodApiModel> {

        return apiFood.filter { it.name.contains(query) }
    }

}
data class FoodApiModel(
    val id: Long,
    val name: String,
    val caloriesPer100g: Double,
)

interface FoodApi {
    suspend fun searchFood(query: String): List<FoodApiModel>
}