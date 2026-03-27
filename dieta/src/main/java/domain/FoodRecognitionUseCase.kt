package com.example.dieta.domain

import android.content.Context
import android.os.Parcelable
import android.util.Log
import kotlinx.parcelize.Parcelize
import com.example.dieta.remote.FoodRecognitionResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import javax.inject.Inject

class FoodRecognitionUseCase @Inject constructor(
    private val repository: DietaRepository,
    @ApplicationContext private val context: Context
) {

    data class IngredientMetadata(
        val name: String,
        val calPerGram: Double,
        val fatPerGram: Double,
        val carbPerGram: Double,
        val proteinPerGram: Double
    )

    @Parcelize
    data class ProcessedIngredient(
        val name: String,
        val weight: Double,
        val calories: Double,
        val protein: Double,
        val fat: Double,
        val carbs: Double,
        val matchFound: Boolean
    ) : Parcelable

    suspend fun recognizeAndProcess(imageFile: File): List<ProcessedIngredient> {
        val results = mutableListOf<ProcessedIngredient>()
        try {
            val response: FoodRecognitionResponse = repository.recognizeFoodFromImage(imageFile)
            
            if (!response.sucesso || response.resultados == null) return emptyList()

            val metadataMap = loadCsvMetadata()

            response.resultados.ingredientes.forEach { detected ->
                val match = metadataMap[detected.nome.lowercase().trim()]
                val weight = detected.pesoEstimadoGramas
                
                if (match != null) {
                    results.add(ProcessedIngredient(
                        name = detected.nome,
                        weight = weight,
                        calories = match.calPerGram * weight,
                        protein = match.proteinPerGram * weight,
                        fat = match.fatPerGram * weight,
                        carbs = match.carbPerGram * weight,
                        matchFound = true
                    ))
                    Log.d("FoodScan", "${detected.nome}: ${weight.toInt()}g | Cal: ${(match.calPerGram * weight).toInt()} kcal")
                } else {
                    results.add(ProcessedIngredient(
                        name = detected.nome,
                        weight = weight,
                        calories = 0.0, protein = 0.0, fat = 0.0, carbs = 0.0,
                        matchFound = false
                    ))
                }
            }
        } catch (e: Exception) {
            Log.e("FoodScan", "Error processing food recognition", e)
        }
        return results
    }

    private fun loadCsvMetadata(): Map<String, IngredientMetadata> {
        val metadataMap = mutableMapOf<String, IngredientMetadata>()
        try {
            val inputStream = context.assets.open("ingredients_metadata.csv")
            val reader = BufferedReader(InputStreamReader(inputStream))
            
            // Skip header: ingr_name;ingr_id;cal/g;fat(g);carb(g);protein(g)
            val header = reader.readLine()
            
            var line: String? = reader.readLine()
            while (line != null) {
                val tokens = line.split(";")
                if (tokens.size >= 6) {
                    val name = tokens[0].lowercase().trim()
                    val metadata = IngredientMetadata(
                        name = name,
                        calPerGram = tokens[2].toDoubleOrNull() ?: 0.0,
                        fatPerGram = tokens[3].toDoubleOrNull() ?: 0.0,
                        carbPerGram = tokens[4].toDoubleOrNull() ?: 0.0,
                        proteinPerGram = tokens[5].toDoubleOrNull() ?: 0.0
                    )
                    metadataMap[name] = metadata
                }
                line = reader.readLine()
            }
            reader.close()
        } catch (e: Exception) {
            Log.e("FoodScan", "Error reading CSV", e)
        }
        return metadataMap
    }
}
