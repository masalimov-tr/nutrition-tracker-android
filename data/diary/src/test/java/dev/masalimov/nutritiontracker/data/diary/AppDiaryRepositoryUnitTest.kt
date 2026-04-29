package dev.masalimov.nutritiontracker.data.diary

import dev.masalimov.nutritiontracker.core.database.diary.DiaryDao
import dev.masalimov.nutritiontracker.domain.GoalCalories
import dev.masalimov.nutritiontracker.domain.diary.model.DiaryDate
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class AppDiaryRepositoryUnitTest {

    @Test
    fun addFoodToDiary_forwardsParameters_toDaoTransaction() = runTest {
        val dao = mockk<DiaryDao>(relaxed = true)
        val goalCalories = GoalCalories()
        val repository = AppDiaryRepository(dao, goalCalories)

        val date = DiaryDate.of(12_345)
        val foodId = 7L
        val quantity = 150.0

        repository.addFoodToDiary(foodId = foodId, date = date, quantityGrams = quantity)
        coVerify {  dao.addFoodToDiaryTx(epochDay = date.toEpochDay(), goalCaloriesPerDay = goalCalories.caloriesPerDay, foodId = foodId, quantityGrams = quantity) }
    }

}
