package dev.masalimov.nutritiontracker.data.diary

import com.google.common.truth.Truth.assertThat
import dev.masalimov.nutritiontracker.core.database.diary.DiaryEntity
import dev.masalimov.nutritiontracker.core.database.diary.DiaryEntryFoodCrossRef
import dev.masalimov.nutritiontracker.core.database.diary.DiaryEntryWithFoods
import dev.masalimov.nutritiontracker.core.database.diary.DiaryFoodLink
import dev.masalimov.nutritiontracker.core.database.food.FoodEntity
import dev.masalimov.nutritiontracker.domain.diary.model.DiaryDate
import org.junit.Test

class MapperTest {

    private val date = DiaryDate.of(1000)

    private fun foodEntity(
        uid: Long = 1L,
        name: String = "Apple",
        caloriesPer100g: Double = 52.0,
        proteinPer100g: Double = 0.3,
        fatPer100g: Double = 0.2,
        carbsPer100g: Double = 14.0,
    ) = FoodEntity(
        uid = uid,
        name = name,
        caloriesPer100g = caloriesPer100g,
        proteinPer100g = proteinPer100g,
        fatPer100g = fatPer100g,
        carbsPer100g = carbsPer100g,
    )

    private fun diaryWithFoods(
        diaryUid: Long = 42L,
        goalCaloriesPerDay: Int = 2000,
        items: List<DiaryFoodLink> = emptyList(),
    ) = DiaryEntryWithFoods(
        diaryEntry = DiaryEntity(
            uid = diaryUid,
            dateEpochDay = date.toEpochDay(),
            goalCaloriesPerDay = goalCaloriesPerDay,
        ),
        items = items,
    )

    @Test
    fun toDiary_mapsScalarFields_correctly() {
        val dbModel = diaryWithFoods(diaryUid = 7L, goalCaloriesPerDay = 1800)

        val result = dbModel.toDiary(date)

        assertThat(result.id.id).isEqualTo(7L)
        assertThat(result.date).isEqualTo(date)
        assertThat(result.goalCaloriesPerDay).isEqualTo(1800)
    }

    @Test
    fun toDiary_withEmptyFoodList_returnsEmptyEatenFood() {
        val result = diaryWithFoods(items = emptyList()).toDiary(date)

        assertThat(result.eatenFood).isEmpty()
        assertThat(result.caloriesEaten).isEqualTo(0)
    }

    @Test
    fun toDiary_mapsEatenFood_nameAndQuantity() {
        val food = foodEntity(uid = 5L, name = "Banana")
        val link = DiaryFoodLink(
            link = DiaryEntryFoodCrossRef(diaryEntryId = 42L, foodId = 5L, quantityGrams = 120.0),
            food = food,
        )
        val result = diaryWithFoods(items = listOf(link)).toDiary(date)

        assertThat(result.eatenFood).hasSize(1)
        assertThat(result.eatenFood[0].food.name).isEqualTo("Banana")
        assertThat(result.eatenFood[0].food.id.id).isEqualTo(5L)
        assertThat(result.eatenFood[0].quantityGram).isEqualTo(120.0)
    }

    @Test
    fun toDiary_calculatesCalories_fromQuantityAndCaloriesPer100g() {
        // 150g of food with 52 cal/100g → 78 calories
        val food = foodEntity(caloriesPer100g = 52.0)
        val link = DiaryFoodLink(
            link = DiaryEntryFoodCrossRef(diaryEntryId = 42L, foodId = 1L, quantityGrams = 150.0),
            food = food,
        )
        val result = diaryWithFoods(items = listOf(link)).toDiary(date)

        assertThat(result.eatenFood[0].calories).isEqualTo(78)
        assertThat(result.caloriesEaten).isEqualTo(78)
    }

    @Test
    fun toDiary_sumsTotalCalories_acrossMultipleFoods() {
        // Apple 100g * 52/100 = 52; Chicken 200g * 165/100 = 330; total = 382
        val apple = foodEntity(uid = 1L, name = "Apple", caloriesPer100g = 52.0)
        val chicken = foodEntity(uid = 2L, name = "Chicken", caloriesPer100g = 165.0)
        val items = listOf(
            DiaryFoodLink(DiaryEntryFoodCrossRef(42L, 1L, 100.0), apple),
            DiaryFoodLink(DiaryEntryFoodCrossRef(42L, 2L, 200.0), chicken),
        )
        val result = diaryWithFoods(items = items).toDiary(date)

        assertThat(result.eatenFood).hasSize(2)
        assertThat(result.caloriesEaten).isEqualTo(382)
    }

    @Test
    fun toEpochDay_roundTrips_withDiaryDateOf() {
        val original = DiaryDate.of(12345)
        val roundTripped = DiaryDate.of(original.toEpochDay())
        assertThat(roundTripped).isEqualTo(original)
    }
}
