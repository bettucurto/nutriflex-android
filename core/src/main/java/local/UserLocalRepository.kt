// core/data/local/UserLocalRepository.kt
package local

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import javax.inject.Inject

class UserLocalRepository @Inject constructor(
    private val userLocalDao: UserLocalDao
) {

    suspend fun saveUserLocal(
        userId: Int,
        token: String,
        bmi: Float,
        currentWeight: Float,
        goalWeight: Float,
        dailyCalories: Int
    ) {
        val today = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDate.now().toString()
        } else {
            "" // ou outra estratégia
        }

        val user = UserLocal(
            userId = userId,
            token = token,
            bmi = bmi,
            currentWeight = currentWeight,
            goalWeight = goalWeight,
            dailyCalories = dailyCalories,
            eatenCaloriesToday = 0,
            lastCaloriesResetDate = today
        )
        userLocalDao.upsert(user)
    }

    suspend fun getUserLocal(): UserLocal? = userLocalDao.getUser()

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun addCaloriesEaten(calories: Int) {
        val user = userLocalDao.getUser() ?: return
        val today = java.time.LocalDate.now().toString()
        val newTotal =
            if (user.lastCaloriesResetDate == today) user.eatenCaloriesToday + calories
            else calories
        userLocalDao.updateDailyCalories(newTotal, today)
    }

    suspend fun clear() = userLocalDao.clear()
}
