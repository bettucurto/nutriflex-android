// dieta/src/main/java/com/example/dieta/remote/DietaRemoteRepository.kt
package com.example.dieta.remote

import javax.inject.Inject

class DietaRemoteRepository @Inject constructor(
    private val api: DietaApiService
) {

    // --- Refeições favoritas ---
    suspend fun getRefeicoesByUser(userId: Int): List<RefeicaoDto> =
        api.getRefeicoesByUser(userId)

    suspend fun getRefeicaoById(id: Int): RefeicaoDto =
        api.getRefeicaoById(id)

    suspend fun addRefeicao(nome: String, userId: Int): SimpleMessageResponse =
        api.addRefeicaoFavorita(CreateRefeicaoRequest(nome = nome, iduser = userId))

    suspend fun updateRefeicao(id: Int, nome: String): SimpleMessageResponse =
        api.updateRefeicaoFavorita(id, UpdateRefeicaoRequest(nome = nome))

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
    ): SimpleMessageResponse =
        api.addIngrediente(
            CreateIngredienteRequest(
                alimentoapiid = alimentoApiId,
                tipoporcao = tipoPorcao,
                quantidadeporcoes = quantidadePorcoes,
                idrefeicao = idRefeicao,
            )
        )

    suspend fun updateIngrediente(
        id: Int,
        alimentoApiId: String,
        tipoPorcao: String,
        quantidadePorcoes: Double,
        idRefeicao: Int,
    ): SimpleMessageResponse =
        api.updateIngrediente(
            id,
            UpdateIngredienteRequest(
                alimentoapiid = alimentoApiId,
                tipoporcao = tipoPorcao,
                quantidadeporcoes = quantidadePorcoes,
                idrefeicao = idRefeicao,
            )
        )

    suspend fun deleteIngrediente(id: Int): SimpleMessageResponse =
        api.deleteIngrediente(id)

    // --- Receitas favoritas ---
    suspend fun getReceitasFavoritasByUser(userId: Int): List<ReceitaFavoritaDto> =
        api.getReceitasFavoritasByUser(userId)

    suspend fun addReceitaFavorita(userId: Int, receitaApiId: String): SimpleMessageResponse =
        api.addReceitaFavorita(
            CreateReceitaFavoritaRequest(
                iduser = userId,
                idreceitaapi = receitaApiId,
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
}
