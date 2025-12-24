package local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [UserLocal::class],
    version = 1,
    exportSchema = false
)
abstract class NutriflexDatabase : RoomDatabase() {
    abstract fun userLocalDao(): UserLocalDao
}
