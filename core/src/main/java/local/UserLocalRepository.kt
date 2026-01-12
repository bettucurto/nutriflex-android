// core/data/local/UserLocalRepository.kt
package local

import android.os.Build
import androidx.annotation.RequiresApi
import local.tables.UserLocal
import local.tables.WeightHistory
import java.time.LocalDate
import javax.inject.Inject

class UserLocalRepository @Inject constructor(
    private val userLocalDao: UserLocalDao,
    private val weightHistoryDao: WeightHistoryDao,
) {

    suspend fun saveUserLocal(
        userId: Int,
        token: String,
        bmi: Float,
        currentWeight: Float,
        goalWeight: Float,
        heightCm: Int,
        dailyCalories: Int,
        gender: String,
        birthDate: String
    ) {
        val today = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDate.now().toString()
        } else {
            ""
        }

        val user = UserLocal(
            userId = userId,
            token = token,
            bmi = bmi,
            currentWeight = currentWeight,
            goalWeight = goalWeight,
            dailyCalories = dailyCalories,
            eatenCaloriesToday = 0,
            heightCm = heightCm,
            lastCaloriesResetDate = today,
            gender = gender,
            birthDate = birthDate
        )
        userLocalDao.upsert(user)
    }

    suspend fun getUserLocal(): UserLocal? = userLocalDao.getUser()

    suspend fun updateCurrentWeight(weight: Float) {
        userLocalDao.updateCurrentWeight(weight)
    }

    suspend fun updateGoalWeight(weight: Float) {
        userLocalDao.updateGoalWeight(weight)
    }

    suspend fun updateBmi(bmi: Float) {
        userLocalDao.updateBmi(bmi)
    }

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

    suspend fun addWeightEntry(userId: Int, weight: Float, date: String) {
        // apaga qualquer registo anterior desse utilizador nesse dia
        weightHistoryDao.deleteByUserAndDate(userId, date)

        val entry = WeightHistory(
            userId = userId,
            weight = weight,
            date = date
        )
        weightHistoryDao.insert(entry)
    }

    suspend fun getWeightHistoryFrom(userId: Int, fromDate: String): List<WeightHistory> =
        weightHistoryDao.getFromDate(userId, fromDate)

    suspend fun getAllWeightHistory(userId: Int): List<WeightHistory> =
        weightHistoryDao.getAll(userId)

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun seedMockWeightHistory(userId: Int) {
        val today = java.time.LocalDate.now()

        // 12 semanas (cerca de 3 meses)
        val entries = (0 until 12).map { weekOffset ->
            val date = today.minusWeeks(weekOffset.toLong()).toString() // uma data por semana

            val base = 75f
            val variation = ((weekOffset % 5) - 2) * 0.5f

            WeightHistory(
                userId = userId,
                date = date,
                weight = base + variation
            )
        }.reversed()

        entries.forEach { weightHistoryDao.insert(it) }
    }

    suspend fun clearWeightHistoryForUser(userId: Int) {
        weightHistoryDao.deleteByUserId(userId)
    }

    suspend fun updateDailyCaloriesValue(calories: Int) {
        userLocalDao.updateDailyCaloriesValue(calories)
    }


}
