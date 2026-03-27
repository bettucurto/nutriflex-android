package local.tables

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workouts",
    foreignKeys = [
        ForeignKey(
            entity = UserLocal::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"])]
)
data class WorkoutLocal(
    @PrimaryKey(autoGenerate = true) val workoutId: Int = 0,
    val userId: Int,
    val name: String,
    val description: String? = null,
    val isActive: Boolean = true,
    val lastPerformedDate: String? = null // YYYY-MM-DD
)
