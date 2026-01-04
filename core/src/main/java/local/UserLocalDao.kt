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

    @Query("UPDATE user_local SET currentWeight = :weight")
    suspend fun updateCurrentWeight(weight: Float)

    @Query("UPDATE user_local SET goalWeight = :weight")
    suspend fun updateGoalWeight(weight: Float)

    @Query("UPDATE user_local SET bmi = :bmi")
    suspend fun updateBmi(bmi: Float)

    @Query("UPDATE user_local SET dailyCalories = :calories")
    suspend fun updateDailyCaloriesValue(calories: Int)

}
