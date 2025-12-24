package local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserLocalDao {

    @Query("SELECT * FROM user_local LIMIT 1")
    suspend fun getUser(): UserLocal?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(user: UserLocal)

    @Query("UPDATE user_local SET eatenCaloriesToday = :calories, lastCaloriesResetDate = :date")
    suspend fun updateDailyCalories(calories: Int, date: String)

    @Query("DELETE FROM user_local")
    suspend fun clear()
}
