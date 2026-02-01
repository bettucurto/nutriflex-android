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
    version = 2,
    exportSchema = false
)
abstract class DietaDatabase : RoomDatabase() {

    abstract fun refeicoesDao(): RefeicoesDao
    abstract fun receitasFavoritasDao(): ReceitasFavoritasDao

    companion object {

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(connection: SQLiteConnection) {
                // 1. Adiciona a nova coluna tipoPorcao (String)
                connection.execSQL(
                    """
                    ALTER TABLE refeicao_ingredientes 
                    ADD COLUMN tipoPorcao TEXT NOT NULL DEFAULT ''
                    """.trimIndent()
                )

                // 3. Cria nova tabela com o schema atualizado (SEM porcaoGramas)
                connection.execSQL(
                    """
                    CREATE TABLE refeicao_ingredientes_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        alimentoApiId TEXT NOT NULL,
                        tipoPorcao TEXT NOT NULL,
                        quantidadePorcoes REAL NOT NULL,
                        idRefeicao INTEGER NOT NULL
                    )
                    """.trimIndent()
                )

                // 4. Copia os dados relevantes da antiga para a nova
                connection.execSQL(
                    """
                    INSERT INTO refeicao_ingredientes_new (id, alimentoApiId, tipoPorcao, quantidadePorcoes, idRefeicao)
                    SELECT id, alimentoApiId, tipoPorcao, quantidadePorcoes, idRefeicao
                    FROM refeicao_ingredientes
                    """.trimIndent()
                )

                // 5. Remove a tabela antiga
                connection.execSQL("DROP TABLE refeicao_ingredientes")

                // 6. Renomeia a nova tabela para o nome original
                connection.execSQL(
                    "ALTER TABLE refeicao_ingredientes_new RENAME TO refeicao_ingredientes"
                )
            }
        }
    }
}
