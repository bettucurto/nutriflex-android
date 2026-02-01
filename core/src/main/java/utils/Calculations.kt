package utils

import kotlin.math.abs
import kotlin.math.roundToInt

data class MacrosResult(
    val carbsGrams: Int,
    val proteinGrams: Int,
    val fatGrams: Int,
)

// 50/30/20 com regra 4/4/9
fun calculateDailyMacros(dailyCalories: Int): MacrosResult {
    val carbsKcal = dailyCalories * 0.50f
    val proteinKcal = dailyCalories * 0.30f
    val fatKcal = dailyCalories * 0.20f

    val carbsGrams = (carbsKcal / 4f).roundToInt()
    val proteinGrams = (proteinKcal / 4f).roundToInt()
    val fatGrams = (fatKcal / 9f).roundToInt()

    return MacrosResult(
        carbsGrams = carbsGrams,
        proteinGrams = proteinGrams,
        fatGrams = fatGrams
    )
}

/**
 * Harris‑Benedict (versão clássica pedida no texto)
 *
 * Mulher:
 * TMB = 655 + (9,6 × peso kg) + (1,8 × altura cm) - (4,7 × idade)
 *
 * Homem:
 * TMB = 66 + (13,7 × peso kg) + (5 × altura cm) - (6,8 × idade)
 */
fun calculateBmrHarrisBenedict(
    isMale: Boolean,
    weightKg: Float,
    heightCm: Int,
    age: Int
): Float {
    return if (isMale) {
        66f + 13.7f * weightKg + 5f * heightCm - 6.8f * age
    } else {
        655f + 9.6f * weightKg + 1.8f * heightCm - 4.7f * age
    }
}

/**
 * Nível de atividade 0–4:
 * 0 -> Little to no exercise  -> 1.2
 * 1 -> Light exercise 1–3x    -> 1.375
 * 2 -> Moderate 3–5x          -> 1.55
 * 3 -> Intense 6–7x           -> 1.725
 * 4 -> Heavy job / intense    -> 1.9
 */
fun activityFactorFromLevel(level: Int): Float {
    return when (level) {
        0 -> 1.2f
        1 -> 1.375f
        2 -> 1.55f
        3 -> 1.725f
        4 -> 1.9f
        else -> 1.2f
    }
}

/**
 * GET (calorias de manutenção) = BMR * fator atividade.
 */
fun calculateMaintenanceCalories(
    isMale: Boolean,
    weightKg: Float,
    heightCm: Int,
    age: Int,
    activityLevel: Int
): Int {
    val bmr = calculateBmrHarrisBenedict(isMale, weightKg, heightCm, age)
    val factor = activityFactorFromLevel(activityLevel)
    return (bmr * factor).roundToInt()
}

/**
 * Ajuste de calorias em função do objetivo:
 *
 * objetivo:
 *  0 -> manter (0 kcal)
 *  1 -> perder peso (-300 a -500 kcal -> usar -400)
 *  2 -> ganhar massa (+300 a +500 kcal -> usar +400)
 */
fun adjustCaloriesForGoal(
    maintenanceCalories: Int,
    goal: Int
): Int {
    val delta = when (goal) {
        1 -> -400   // perda de peso
        2 -> +400   // ganho massa
        else -> 0   // manutenção
    }
    return (maintenanceCalories + delta).coerceAtLeast(1200)
}

/**
 * Regra 0.33 / 0.5 / 1.0 kg por semana.
 */
fun targetKgPerWeek(deltaKg: Float): Float {
    val absDelta = abs(deltaKg)
    return when {
        absDelta < 7f   -> 0.33f
        absDelta < 15f  -> 0.5f
        else            -> 1.0f
    }
}

/**
 * Cálculo de calorias para atingir peso‑objetivo usando 7700 kcal/kg.
 */
fun calculateDailyCaloriesForWeightChange(
    maintenanceCalories: Int,
    currentWeightKg: Float,
    goalWeightKg: Float
): Int {
    val deltaKg = goalWeightKg - currentWeightKg
    if (deltaKg == 0f) return maintenanceCalories

    val kgPerWeek = targetKgPerWeek(deltaKg)
    val kcalPerKg = 7700f
    val weeklyKcal = kgPerWeek * kcalPerKg
    val dailyKcalChange = weeklyKcal / 7f

    val caloriesDaily = if (deltaKg < 0f) {
        maintenanceCalories - dailyKcalChange
    } else {
        maintenanceCalories + dailyKcalChange
    }
    return caloriesDaily.roundToInt()
}
