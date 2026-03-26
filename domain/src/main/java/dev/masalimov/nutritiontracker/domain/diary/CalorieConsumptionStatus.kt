package dev.masalimov.nutritiontracker.domain.diary

sealed interface CalorieConsumptionStatus {
    data object Unknown : CalorieConsumptionStatus
    data object Over : CalorieConsumptionStatus
    data object NotOver : CalorieConsumptionStatus
}