package local.tables

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weight_history")
data class WeightHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Int,
    val date: String,       // "YYYY-MM-DD"
    val weight: Float
)