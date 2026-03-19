package dev.masalimov.nutritiontracker.core.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class NavigationRoutes {

    @Serializable
    object FoodList : NavigationRoutes()

    @Serializable
    data class FoodDetail(val foodId: Long) : NavigationRoutes()

}