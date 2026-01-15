// core/local/NutriflexDatabase.kt
package local

import androidx.room.Database
import androidx.room.RoomDatabase
import local.tables.UserLocal
import local.tables.WeightHistory

@Database(
    entities = [
        UserLocal::class,
        WeightHistory::class,
    ],
    version = 5, // manténs a versão que já tinhas no core
    exportSchema = false
)
abstract class NutriflexDatabase : RoomDatabase() {

    abstract fun userLocalDao(): UserLocalDao
    abstract fun weightHistoryDao(): WeightHistoryDao
}
