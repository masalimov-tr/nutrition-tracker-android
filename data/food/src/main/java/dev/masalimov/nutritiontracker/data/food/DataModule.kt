package dev.masalimov.nutritiontracker.data.food

object DataModule {

    fun getFoodRepository(): FoodRepository {
        return FakeFoodRepository()
    }
}