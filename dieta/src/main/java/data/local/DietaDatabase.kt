// dieta/local/DietaDatabase.kt
package data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import data.local.dao.ReceitasFavoritasDao
import data.local.dao.RefeicoesDao
import data.local.tables.IngredienteRefeicaoLocal
import data.local.tables.ReceitaFavoritaLocal
import data.local.tables.RefeicaoFavoritaLocal


@Database(
    entities = [
        RefeicaoFavoritaLocal::class,
        IngredienteRefeicaoLocal::class,
        ReceitaFavoritaLocal::class,
    ],
    version = 1,
    exportSchema = false
)
abstract class DietaDatabase : RoomDatabase() {
    abstract fun refeicoesDao(): RefeicoesDao
    abstract fun receitasFavoritasDao(): ReceitasFavoritasDao
}
