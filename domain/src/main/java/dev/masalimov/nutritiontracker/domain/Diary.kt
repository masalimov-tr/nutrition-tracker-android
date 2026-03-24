package dev.masalimov.nutritiontracker.domain


@JvmInline
value class DiaryId(val id: Long)

data class EatenFood(
    val quantityGram: Double,
    val food: Food
) {
    val calories: Int
        get() = (quantityGram / 100 * food.caloriesPer100g).toInt()
}

data class Diary(
    val id: DiaryId,
    val eatenFood: EatenFood,
)