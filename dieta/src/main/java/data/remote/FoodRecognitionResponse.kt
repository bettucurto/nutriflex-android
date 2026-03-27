package com.example.dieta.remote

import com.google.gson.annotations.SerializedName

data class FoodRecognitionResponse(
    @SerializedName("sucesso") val sucesso: Boolean,
    @SerializedName("resultados") val resultados: RecognitionResults?
)

data class RecognitionResults(
    @SerializedName("ingredientes") val ingredientes: List<DetectedIngredient>,
    @SerializedName("top_k_usado") val topKUsado: Int,
    @SerializedName("total_detetados") val totalDetetados: Int
)

data class DetectedIngredient(
    @SerializedName("id") val id: Int,
    @SerializedName("nome") val nome: String,
    @SerializedName("peso_estimado_gramas") val pesoEstimadoGramas: Double,
    @SerializedName("probabilidade") val probabilidade: Double
)
