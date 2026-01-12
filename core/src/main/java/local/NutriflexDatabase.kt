package local

import androidx.room.Database
import androidx.room.RoomDatabase
import local.tables.UserLocal
import local.tables.WeightHistory

@Database(
    entities = [UserLocal::class, WeightHistory::class],
    version = 3,
    exportSchema = false
)
abstract class NutriflexDatabase : RoomDatabase() {
    abstract fun userLocalDao(): UserLocalDao
    abstract fun weightHistoryDao(): WeightHistoryDao
}
