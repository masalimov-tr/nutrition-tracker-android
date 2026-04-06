package dev.masalimov.nutritiontracker.core.ui


fun logD(message: String, tag: String? = null) {
    if (BuildConfig.DEBUG) {
        android.util.Log.d(tag ?: "NutritionTracker",  message)
    }
}