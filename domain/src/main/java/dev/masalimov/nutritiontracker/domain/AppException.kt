package dev.masalimov.nutritiontracker.domain

sealed class AppException(
    val errorMessage: String
) : Exception()

class FoodSearchException: AppException(
    errorMessage = "An error occurred while searching food"
)