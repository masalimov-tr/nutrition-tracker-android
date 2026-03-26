package dev.masalimov.nutritiontracker.core.database.diary

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class DiaryEntity(
    @PrimaryKey(autoGenerate = true) val uid: Long = 0L,
    val dateEpochDay: Int,
    // snapshot: goal calories per day
    val goalCaloriesPerDay: Int,
)