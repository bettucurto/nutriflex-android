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
    version = 5,
    exportSchema = false
)
abstract class DietaDatabase : RoomDatabase() {

    abstract fun refeicoesDao(): RefeicoesDao
    abstract fun receitasFavoritasDao(): ReceitasFavoritasDao

    companion object {

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(connection: SQLiteConnection) {
                // Migração inicial (placeholder ou tabelas base)
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(connection: SQLiteConnection) {
                // Adicionar a coluna nome_alimento à tabela refeicao_ingredientes
                connection.execSQL("ALTER TABLE refeicao_ingredientes ADD COLUMN nome_alimento TEXT")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(connection: SQLiteConnection) {
                // 1. Renomear colunas em refeicoes_favoritas
                connection.execSQL("ALTER TABLE refeicoes_favoritas RENAME COLUMN carbsPct TO carbs_pct")
                connection.execSQL("ALTER TABLE refeicoes_favoritas RENAME COLUMN proteinPct TO protein_pct")
                connection.execSQL("ALTER TABLE refeicoes_favoritas RENAME COLUMN fatPct TO fat_pct")

                // 2. Renomear colunas em receitas_favoritas (para consistência)
                connection.execSQL("ALTER TABLE receitas_favoritas RENAME COLUMN carbsPct TO carbs_pct")
                connection.execSQL("ALTER TABLE receitas_favoritas RENAME COLUMN proteinPct TO protein_pct")
                connection.execSQL("ALTER TABLE receitas_favoritas RENAME COLUMN fatPct TO fat_pct")

                // 3. Adicionar Foreign Key a refeicao_ingredientes (Requer recriar a tabela no SQLite)
                connection.execSQL("""
                    CREATE TABLE IF NOT EXISTS `refeicao_ingredientes_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `alimento_api_id` TEXT NOT NULL, 
                        `tipo_porcao` TEXT NOT NULL, 
                        `quantidade_porcoes` REAL NOT NULL, 
                        `nome_alimento` TEXT, 
                        `id_refeicao` INTEGER NOT NULL, 
                        FOREIGN KEY(`id_refeicao`) REFERENCES `refeicoes_favoritas`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """.trimIndent())
                
                connection.execSQL("""
                    INSERT INTO `refeicao_ingredientes_new` (id, alimento_api_id, tipo_porcao, quantidade_porcoes, nome_alimento, id_refeicao)
                    SELECT id, alimento_api_id, tipo_porcao, quantidade_porcoes, nome_alimento, id_refeicao FROM `refeicao_ingredientes`
                """.trimIndent())
                
                connection.execSQL("DROP TABLE `refeicao_ingredientes`")
                connection.execSQL("ALTER TABLE `refeicao_ingredientes_new` RENAME TO `refeicao_ingredientes`")
            }
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
