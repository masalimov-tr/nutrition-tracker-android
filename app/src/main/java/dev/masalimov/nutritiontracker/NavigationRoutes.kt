package dev.masalimov.nutritiontracker

import kotlinx.serialization.Serializable


sealed class NavigationRoutes {

    @Serializable
    object FoodList : NavigationRoutes()

    @Serializable
    data class FoodDetail(val foodId: Long) : NavigationRoutes()

}
