package dev.masalimov.nutritiontracker.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import dev.masalimov.nutritiontracker.core.database.diary.DiaryDao
import dev.masalimov.nutritiontracker.core.database.diary.DiaryEntity
import dev.masalimov.nutritiontracker.core.database.diary.DiaryFoodPortionCrossRef
import dev.masalimov.nutritiontracker.core.database.food.FoodDao
import dev.masalimov.nutritiontracker.core.database.food.FoodEntity

@Database(
    entities = [
        FoodEntity::class,
        DiaryEntity::class,
        DiaryFoodPortionCrossRef::class,
    ],
    version = 1,
    exportSchema = true
)

abstract class NutritionAppDatabase : RoomDatabase() {

    abstract fun foodDao(): FoodDao

    abstract fun diaryDao(): DiaryDao
}