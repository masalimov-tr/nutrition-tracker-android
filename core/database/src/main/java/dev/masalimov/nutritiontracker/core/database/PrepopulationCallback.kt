package dev.masalimov.nutritiontracker.core.database

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import dev.masalimov.nutritiontracker.core.common.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class PrepopulateCallback @Inject constructor(
    @ApplicationScope private val appScope: CoroutineScope,
    val seederProvider: Provider<DatabaseSeeder>,
) : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        appScope.launch {
            seederProvider.get().seedIfEmpty()
        }
    }
}