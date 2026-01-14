// local/tables/RefeicaoFavoritaLocal.kt
package data.local.tables

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "refeicoes_favoritas")
data class RefeicaoFavoritaLocal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nome: String,
    val idUser: Int,
)
