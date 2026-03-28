package dev.masalimov.nutritiontracker.data.food.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.masalimov.nutritiontracker.data.food.AppFoodRepository
import dev.masalimov.nutritiontracker.data.food.FoodApi
import dev.masalimov.nutritiontracker.data.food.FoodApiFakeImpl
import dev.masalimov.nutritiontracker.domain.food.FoodRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FoodDataModule {

    @Binds
    @Singleton
    internal abstract fun bindFoodRepository(impl: AppFoodRepository): FoodRepository

    @Binds
    @Singleton
    internal abstract fun bindFoodApi(impl: FoodApiFakeImpl): FoodApi
}