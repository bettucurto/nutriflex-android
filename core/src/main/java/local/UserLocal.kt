// core/data/local/UserLocal.kt
package local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_local")
data class UserLocal(
    @PrimaryKey val userId: Int,
    val token: String,
    val bmi: Float,
    val currentWeight: Float,
    val goalWeight: Float,
    val dailyCalories: Int,
    val eatenCaloriesToday: Int,
    val lastCaloriesResetDate: String // "YYYY-MM-DD"
)
