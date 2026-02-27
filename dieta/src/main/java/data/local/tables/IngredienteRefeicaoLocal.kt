// local/tables/IngredienteRefeicaoLocal.kt
package data.local.tables

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "refeicao_ingredientes")
data class IngredienteRefeicaoLocal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "alimento_api_id")
    val alimentoApiId: String,
    @ColumnInfo(name = "tipo_porcao")
    val tipoPorcao: String,
    @ColumnInfo(name = "quantidade_porcoes")
    val quantidadePorcoes: Double,
    @ColumnInfo(name = "id_refeicao")
    val idRefeicao: Int,
)
