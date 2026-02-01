// local/tables/IngredienteRefeicaoLocal.kt
package data.local.tables

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "refeicao_ingredientes")
data class IngredienteRefeicaoLocal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val alimentoApiId: String,
    val tipoPorcao: String,
    val quantidadePorcoes: Double,
    val idRefeicao: Int,
)
