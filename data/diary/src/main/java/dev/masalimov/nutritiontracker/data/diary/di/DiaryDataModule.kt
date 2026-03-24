package dev.masalimov.nutritiontracker.data.diary.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.masalimov.nutritiontracker.data.diary.AppDiaryRepository
import dev.masalimov.nutritiontracker.domain.diary.DiaryRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DiaryDataModule {

    @Binds
    @Singleton
    internal abstract fun bindDiaryRepository(impl: AppDiaryRepository): DiaryRepository
}