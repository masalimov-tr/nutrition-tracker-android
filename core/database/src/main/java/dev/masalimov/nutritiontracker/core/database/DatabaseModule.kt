package dev.masalimov.nutritiontracker.core.database

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext appContext: Context,
        prepopulateCallback: PrepopulateCallback,
    ) : NutritionAppDatabase = Room.databaseBuilder(
        context = appContext,
        NutritionAppDatabase::class.java,
        "nutrition_app_database",
    )   .fallbackToDestructiveMigration()
        .addCallback(prepopulateCallback)
        .build()

    @Provides
    @Singleton
    fun provideFoodDao(database: NutritionAppDatabase) = database.foodDao()

    @Provides
    @Singleton
    fun provideDiaryDao(database: NutritionAppDatabase) = database.diaryDao()

}