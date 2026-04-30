package dev.masalimov.nutritiontracker

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import dev.masalimov.nutritiontracker.core.database.DatabaseModule
import dev.masalimov.nutritiontracker.core.database.NutritionAppDatabase
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DatabaseModule::class],
)
object TestDatabaseModule {

    @Provides
    @Singleton
    fun provideInMemoryDatabase(@ApplicationContext context: Context): NutritionAppDatabase =
        Room.inMemoryDatabaseBuilder(context, NutritionAppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

    @Provides
    @Singleton
    fun provideFoodDao(database: NutritionAppDatabase) = database.foodDao()

    @Provides
    @Singleton
    fun provideDiaryDao(database: NutritionAppDatabase) = database.diaryDao()
}
