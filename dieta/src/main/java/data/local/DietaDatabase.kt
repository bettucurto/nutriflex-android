// dieta/local/DietaDatabase.kt
package dieta.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
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
    version = 3,
    exportSchema = false
)
abstract class DietaDatabase : RoomDatabase() {

    abstract fun refeicoesDao(): RefeicoesDao
    abstract fun receitasFavoritasDao(): ReceitasFavoritasDao

    companion object {

        val MIGRATION_1_2 = object : Migration(1, 2) {
            // ... (mantém o código atual)
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(connection: SQLiteConnection) {
                // Colunas para refeicoes_favoritas
                connection.execSQL("ALTER TABLE refeicoes_favoritas ADD COLUMN calories INTEGER DEFAULT 0")
                connection.execSQL("ALTER TABLE refeicoes_favoritas ADD COLUMN image TEXT")
                connection.execSQL("ALTER TABLE refeicoes_favoritas ADD COLUMN carbsPct INTEGER DEFAULT 0")
                connection.execSQL("ALTER TABLE refeicoes_favoritas ADD COLUMN proteinPct INTEGER DEFAULT 0")
                connection.execSQL("ALTER TABLE refeicoes_favoritas ADD COLUMN fatPct INTEGER DEFAULT 0")
                connection.execSQL("ALTER TABLE refeicoes_favoritas ADD COLUMN description TEXT DEFAULT 'Custom Meal'")

                // Colunas para receitas_favoritas
                connection.execSQL("ALTER TABLE receitas_favoritas ADD COLUMN nome TEXT DEFAULT 'Favorite Recipe'")
                connection.execSQL("ALTER TABLE receitas_favoritas ADD COLUMN image TEXT")
                connection.execSQL("ALTER TABLE receitas_favoritas ADD COLUMN calories INTEGER DEFAULT 0")
                connection.execSQL("ALTER TABLE receitas_favoritas ADD COLUMN carbsPct INTEGER DEFAULT 0")
                connection.execSQL("ALTER TABLE receitas_favoritas ADD COLUMN proteinPct INTEGER DEFAULT 0")
                connection.execSQL("ALTER TABLE receitas_favoritas ADD COLUMN fatPct INTEGER DEFAULT 0")
                connection.execSQL("ALTER TABLE receitas_favoritas ADD COLUMN description TEXT DEFAULT ''")
            }
        }
    }
}
