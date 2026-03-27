// dieta/src/main/java/com/example/dieta/remote/DietaRemoteRepository.kt
package com.example.dieta.remote

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import javax.inject.Inject

class DietaRemoteRepository @Inject constructor(
    private val api: DietaApiService
) {

    // --- Refeições favoritas ---
    suspend fun getRefeicoesByUser(userId: Int): List<RefeicaoDto> =
        api.getRefeicoesByUser(userId)

    suspend fun getRefeicaoById(id: Int): RefeicaoDto =
        api.getRefeicaoById(id)

    suspend fun addRefeicao(
        nome: String,
        userId: Int,
        image: String? = null,
        calories: Int? = 0,
        fatPct: Int? = 0,
        carbsPct: Int? = 0,
        proteinPct: Int? = 0,
        description: String? = ""
    ): Int {
        val request = CreateRefeicaoRequest(
            nome = nome,
            idUser = userId,
            image = image,
            calories = calories,
            fatPct = fatPct,
            carbsPct = carbsPct,
            proteinPct = proteinPct,
            description = description
        )
        val response = api.addRefeicaoFavorita(request)
        return response.id ?: 0
    }

    suspend fun updateRefeicao(
        id: Int,
        nome: String,
        image: String? = null,
        calories: Int? = 0,
        fatPct: Int? = 0,
        carbsPct: Int? = 0,
        proteinPct: Int? = 0,
        description: String? = ""
    ): SimpleMessageResponse =
        api.updateRefeicaoFavorita(
            id,
            UpdateRefeicaoRequest(
                nome = nome,
                image = image,
                calories = calories,
                fatPct = fatPct,
                carbsPct = carbsPct,
                proteinPct = proteinPct,
                description = description
            )
        )

    suspend fun deleteRefeicao(id: Int): SimpleMessageResponse =
        api.deleteRefeicaoFavorita(id)

    // --- Ingredientes ---
    suspend fun getIngredientesByRefeicao(idRefeicao: Int): List<IngredienteDto> =
        api.getIngredientesByRefeicao(idRefeicao)

    suspend fun getIngredienteById(id: Int): IngredienteDto =
        api.getIngredienteById(id)

    suspend fun addIngrediente(
        alimentoApiId: String,
        tipoPorcao: String,
        quantidadePorcoes: Double,
        idRefeicao: Int,
        nomeAlimento: String
    ): SimpleMessageResponse =
        api.addIngrediente(
            CreateIngredienteRequest(
                alimentoapiid = alimentoApiId,
                tipoporcao = tipoPorcao,
                quantidadeporcoes = quantidadePorcoes,
                idrefeicao = idRefeicao,
                nomealimento = nomeAlimento
            )
        )

    suspend fun updateIngrediente(
        id: Int,
        alimentoApiId: String,
        tipoPorcao: String,
        quantidadePorcoes: Double,
        idRefeicao: Int,
        nomeAlimento: String
        ): SimpleMessageResponse =
        api.updateIngrediente(
            id,
            UpdateIngredienteRequest(
                alimentoapiid = alimentoApiId,
                tipoporcao = tipoPorcao,
                quantidadeporcoes = quantidadePorcoes,
                idrefeicao = idRefeicao,
                nomealimento = nomeAlimento
            )
        )

    suspend fun deleteIngrediente(id: Int): SimpleMessageResponse =
        api.deleteIngrediente(id)

    suspend fun deleteIngredientesByRefeicao(idRefeicao: Int): SimpleMessageResponse =
        api.deleteIngredientesByRefeicao(idRefeicao)

    // --- Receitas favoritas ---
    suspend fun getReceitasFavoritasByUser(userId: Int): List<ReceitaFavoritaDto> =
        api.getReceitasFavoritasByUser(userId)

    suspend fun addReceitaFavorita(
        userId: Int,
        recipeApiId: String,
        nome: String? = null,
        image: String? = null,
        calories: Int? = 0,
        carbsPct: Int? = 0,
        proteinPct: Int? = 0,
        fatPct: Int? = 0,
        description: String? = ""
    ): SimpleMessageResponse =
        api.addReceitaFavorita(
            CreateReceitaFavoritaRequest(
                iduser = userId,
                idreceitaapi = recipeApiId,
                nome = nome,
                image = image,
                calories = calories,
                carbsPct = carbsPct,
                proteinPct = proteinPct,
                fatPct = fatPct,
                description = description
            )
        )

    suspend fun deleteReceitaFavorita(id: Int): SimpleMessageResponse =
        api.deleteReceitaFavorita(id)

    // --- FatSecret alimentos ---
    suspend fun buscarAlimentosFatSecret(nome: String): List<FatSecretFoodDto> =
        api.buscarAlimentosFatSecret(nome)

    suspend fun buscarAlimentoFatSecretPorId(id: String): FatSecretFoodDetailsDto =
        api.buscarAlimentoFatSecretPorId(id)

    suspend fun buscarAutocomplete(query: String): List<StringDto> =
        api.buscarAutocomplete(query)


    // --- FatSecret receitas ---
    suspend fun searchReceitasFatSecret(
        query: String,
        maxResults: Int = 20,
        page: Int = 0,
        tipo: String? = null,
        calorias: String? = null,
    ): FatSecretRecipeSearchResponse =
        api.searchReceitasFatSecret(query, maxResults, page, tipo, calorias)

    suspend fun getReceitaById(id: String): FatSecretRecipeDetailsDto =
        api.getReceitaById(id).receita

    suspend fun recognizeFoodFromImage(image: java.io.File): FoodRecognitionResponse {
        val requestFile = okhttp3.RequestBody.create("image/*".toMediaTypeOrNull(), image)
        // CRÍTICO: O servidor espera o campo "imagem"
        val body = okhttp3.MultipartBody.Part.createFormData("imagem", image.name, requestFile)
        return api.recognizeFoodFromImage(body)
    }
}
