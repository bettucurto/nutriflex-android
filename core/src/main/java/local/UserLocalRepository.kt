// core/data/local/UserLocalRepository.kt
package local

import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import local.tables.UserLocal
import local.tables.WeightHistory
import utils.calculateDailyMacros
import java.time.LocalDate
import javax.inject.Inject

class UserLocalRepository @Inject constructor(
    private val userLocalDao: UserLocalDao,
    private val weightHistoryDao: WeightHistoryDao,
) {

    suspend fun updateNextWorkoutId(userId: Int, nextWorkoutId: Int) {
        userLocalDao.updateNextWorkoutId(userId, nextWorkoutId)
    }

    suspend fun saveUserLocal(
        userId: Int,
        token: String,
        bmi: Float,
        currentWeight: Float,
        initialWeight: Float,
        goalWeight: Float,
        heightCm: Int,
        dailyCalories: Int,
        gender: String,
        birthDate: String,
        activityLevel: Int
    ) {
        val today = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDate.now().toString()
        } else {
            ""
        }

        val macros = calculateDailyMacros(dailyCalories)

        val user = UserLocal(
            userId = userId,
            token = token,
            bmi = bmi,
            currentWeight = currentWeight,
            initialWeight = initialWeight,
            goalWeight = goalWeight,
            dailyCalories = dailyCalories,
            eatenCaloriesToday = 0,
            heightCm = heightCm,
            lastCaloriesResetDate = today,
            gender = gender,
            birthDate = birthDate,
            dailyCarbsGrams = macros.carbsGrams,
            dailyProteinGrams = macros.proteinGrams,
            dailyFatGrams = macros.fatGrams,
            activityLevel = activityLevel,
            eatenCarbsToday = 0,
            eatenFatToday = 0,
            eatenProteinToday = 0
        )
        userLocalDao.upsert(user)
    }

    fun getUserLocal(): Flow<UserLocal?> = userLocalDao.getUser()

    suspend fun updateCurrentWeight(weight: Float) {
        userLocalDao.updateCurrentWeight(weight)
    }

    suspend fun updateGoalWeight(weight: Float) {
        userLocalDao.updateGoalWeight(weight)
    }

    suspend fun updateBmi(bmi: Float) {
        userLocalDao.updateBmi(bmi)
    }

    suspend fun updateAccountData(
        userId: Int,
        height: Int,
        activityLevel: Int,
        birthDate: String,
        dailyCalories: Int,
        bmi: Float
    ) {
        val macros = calculateDailyMacros(dailyCalories)
        userLocalDao.updateAccountData(
            userId = userId,
            height = height,
            activityLevel = activityLevel,
            birthDate = birthDate,
            dailyCalories = dailyCalories,
            dailyCarbs = macros.carbsGrams,
            dailyProtein = macros.proteinGrams,
            dailyFat = macros.fatGrams,
            bmi = bmi
        )
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

    //quando alterar a quantidade de calorias que a pessoa deve comer por dia
    suspend fun updateDailyCaloriesValue(calories: Int) {
        userLocalDao.updateDailyCaloriesValue(calories)
        val macros = calculateDailyMacros(calories)
        userLocalDao.updateDailyMacrosValue(
            carbs = macros.carbsGrams,
            protein = macros.proteinGrams,
            fat = macros.fatGrams
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun checkAndResetDailyCaloriesAndMacros() {
        val today = LocalDate.now().toString()
        // Use firstOrNull() to get a single snapshot from the flow for this check
        val user = userLocalDao.getUser().firstOrNull() ?: return
        if (user.lastCaloriesResetDate != today) {
            userLocalDao.resetDailyTotals(today)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun addCaloriesEaten(calories: Int) {
        val today = LocalDate.now().toString()
        userLocalDao.addCaloriesEaten(calories, today)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun addDailyMacrosEaten(protein: Double, carbs: Double, fat: Double) {
        val today = LocalDate.now().toString()
        userLocalDao.addDailyMacrosEaten(protein.toInt(), carbs.toInt(), fat.toInt(), today)
    }


}
