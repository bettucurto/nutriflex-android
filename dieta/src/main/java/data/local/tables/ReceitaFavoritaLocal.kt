// local/tables/ReceitaFavoritaLocal.kt
package data.local.tables

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "receitas_favoritas")
data class ReceitaFavoritaLocal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val idUser: Int,
    val idReceitaApi: String,
)
