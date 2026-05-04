package dev.masalimov.nutritiontracker.domain.diary.model

import com.google.common.truth.Truth.assertThat
import dev.masalimov.nutritiontracker.domain.food.Food
import dev.masalimov.nutritiontracker.domain.food.FoodId
import org.junit.Test

class EatenFoodTest {

    private val apple = Food(FoodId(1), "Apple", caloriesPer100g = 52.0, 0.3, 0.2, 14.0)

    @Test
    fun `calories truncates result to integer`() {
        // 150g × 160 kcal/100g = 240.0 exactly → 240
        val chicken = Food(FoodId(2), "Chicken", caloriesPer100g = 160.0, 30.0, 3.0, 0.0)
        val eaten = EatenFood(quantityGram = 150.0, food = chicken)
        assertThat(eaten.calories).isEqualTo(240)
    }

    @Test
    fun `calories uses floor division when result is fractional`() {
        // 100g × 52 kcal/100g = 52.0 → 52
        val eaten = EatenFood(quantityGram = 100.0, food = apple)
        assertThat(eaten.calories).isEqualTo(52)
    }

    @Test
    fun `calories is zero when quantity is zero`() {
        val eaten = EatenFood(quantityGram = 0.0, food = apple)
        assertThat(eaten.calories).isEqualTo(0)
    }

    @Test
    fun `calories scales linearly with quantity`() {
        val eaten100g = EatenFood(quantityGram = 100.0, food = apple)
        val eaten200g = EatenFood(quantityGram = 200.0, food = apple)
        assertThat(eaten200g.calories).isEqualTo(eaten100g.calories * 2)
    }

    @Test
    fun `DiaryEntryForDate caloriesEaten sums all eaten food calories`() {
        val entry = DiaryEntryForDate(
            id = DiaryId(1),
            date = DiaryDate.today(),
            eatenFood = listOf(
                EatenFood(100.0, apple),                           // 52 kcal
                EatenFood(150.0, Food(FoodId(2), "Chicken", 160.0, 30.0, 3.0, 0.0)), // 240 kcal
            ),
            goalCaloriesPerDay = 2000,
        )
        assertThat(entry.caloriesEaten).isEqualTo(52 + 240)
    }

    @Test
    fun `DiaryEntryForDate caloriesEaten is zero when no food logged`() {
        val entry = DiaryEntryForDate(
            id = DiaryId(1),
            date = DiaryDate.today(),
            eatenFood = emptyList(),
            goalCaloriesPerDay = 2000,
        )
        assertThat(entry.caloriesEaten).isEqualTo(0)
    }
}
