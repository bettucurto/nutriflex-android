// local/tables/ReceitaFavoritaLocal.kt
package data.local.tables

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "receitas_favoritas")
data class ReceitaFavoritaLocal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @androidx.room.ColumnInfo(name = "id_user")
    val idUser: Int,
    @androidx.room.ColumnInfo(name = "id_receita_api")
    val idReceitaApi: String,
    val nome: String? = "Favorite Recipe",
    val image: String? = null,
    val calories: Int? = 0,
    val carbsPct: Int? = 0,
    val proteinPct: Int? = 0,
    val fatPct: Int? = 0,
    val description: String? = ""
)
