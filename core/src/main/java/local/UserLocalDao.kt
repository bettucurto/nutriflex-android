package local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import local.tables.UserLocal

@Dao
interface UserLocalDao {

    @Query("SELECT * FROM user_local LIMIT 1")
    fun getUser(): Flow<UserLocal?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(user: UserLocal)

    @Query("UPDATE user_local SET eatenCaloriesToday = eatenCaloriesToday + :calories, lastCaloriesResetDate = :date")
    suspend fun addCaloriesEaten(calories: Int, date: String)

    @Query("UPDATE user_local SET eatenCaloriesToday = 0, eatenProteinToday = 0, eatenCarbsToday = 0, eatenFatToday = 0, lastCaloriesResetDate = :date")
    suspend fun resetDailyTotals(date: String)

    @Query("DELETE FROM user_local")
    suspend fun clear()

    @Query("UPDATE user_local SET currentWeight = :weight")
    suspend fun updateCurrentWeight(weight: Float)

    @Query("UPDATE user_local SET goalWeight = :weight")
    suspend fun updateGoalWeight(weight: Float)

    @Query("UPDATE user_local SET bmi = :bmi")
    suspend fun updateBmi(bmi: Float)

    @Query("""
        UPDATE user_local
        SET heightCm = :height,
            activityLevel = :activityLevel,
            birthDate = :birthDate,
            dailyCalories = :dailyCalories,
            dailyCarbsGrams = :dailyCarbs,
            dailyProteinGrams = :dailyProtein,
            dailyFatGrams = :dailyFat,
            bmi = :bmi
        WHERE userId = :userId
    """)
    suspend fun updateAccountData(
        userId: Int,
        height: Int,
        activityLevel: Int,
        birthDate: String,
        dailyCalories: Int,
        dailyCarbs: Int,
        dailyProtein: Int,
        dailyFat: Int,
        bmi: Float
    )

    @Query("UPDATE user_local SET dailyCalories = :calories")
    suspend fun updateDailyCaloriesValue(calories: Int)

    @Query(
        """
        UPDATE user_local
        SET dailyCarbsGrams = :carbs,
            dailyProteinGrams = :protein,
            dailyFatGrams = :fat
        """
    )
    suspend fun updateDailyMacrosValue(
        carbs: Int,
        protein: Int,
        fat: Int
    )

    @Query("""
        UPDATE user_local 
        SET eatenProteinToday = eatenProteinToday + :protein, 
            eatenCarbsToday = eatenCarbsToday + :carbs, 
            eatenFatToday = eatenFatToday + :fat,
            lastCaloriesResetDate = :date
    """)
    suspend fun addDailyMacrosEaten(protein: Int, carbs: Int, fat: Int, date: String)

}
