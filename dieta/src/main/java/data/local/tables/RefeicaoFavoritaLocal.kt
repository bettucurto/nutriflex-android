// local/tables/RefeicaoFavoritaLocal.kt
package data.local.tables

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "refeicoes_favoritas")
data class RefeicaoFavoritaLocal(
    @PrimaryKey(autoGenerate = false) val id: Int = 0,
    val nome: String,
    @androidx.room.ColumnInfo(name = "id_user")
    val idUser: Int,
    val calories: Int? = 0,
    val image: String? = null,
    @androidx.room.ColumnInfo(name = "carbs_pct")
    val carbsPct: Int? = 0,
    @androidx.room.ColumnInfo(name = "protein_pct")
    val proteinPct: Int? = 0,
    @androidx.room.ColumnInfo(name = "fat_pct")
    val fatPct: Int? = 0,
    val description: String? = "Custom Meal"
)
