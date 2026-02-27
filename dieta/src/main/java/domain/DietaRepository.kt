// dieta/src/main/java/com/example/dieta/domain/DietaRepository.kt
package com.example.dieta.domain

import com.example.dieta.remote.DietaRemoteRepository
import data.local.repository.DietaLocalRepository
import data.local.tables.IngredienteRefeicaoLocal
import data.local.tables.ReceitaFavoritaLocal
import data.local.tables.RefeicaoFavoritaLocal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DietaRepository @Inject constructor(
    private val local: DietaLocalRepository,
    private val remote: DietaRemoteRepository,
) {

    // ---------- MEALS (refeições favoritas) ----------

    fun observeMeals(userId: Int): Flow<List<Refeicao>> =
        local.getRefeicoesByUser(userId).map { list -> list.map { it.toDomain() } }

    fun observeMeal(mealId: Int): Flow<Refeicao?> =
        local.observeRefeicaoById(mealId).map { it?.toDomain() }

    suspend fun refreshMealsFromRemote(userId: Int) {
        try {
            val remoteMeals = remote.getRefeicoesByUser(userId)
            // Simples: Limpar local do user e inserir novos do remoto
            local.deleteRefeicoesByUser(userId)
            remoteMeals.forEach { dto ->
                local.saveRefeicao(
                    RefeicaoFavoritaLocal(
                        id = dto.id,
                        nome = dto.nome,
                        idUser = dto.iduser
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun addMeal(
        name: String,
        userId: Int,
        calories: Int? = 0,
        image: String? = null,
        carbsPct: Int? = 0,
        proteinPct: Int? = 0,
        fatPct: Int? = 0,
        description: String? = "Custom Meal"
    ): Int {
        val remoteId = remote.addRefeicao(name, userId)
        val localEntity = RefeicaoFavoritaLocal(
            id = remoteId,
            nome = name,
            idUser = userId,
            calories = calories,
            image = image,
            carbsPct = carbsPct,
            proteinPct = proteinPct,
            fatPct = fatPct,
            description = description
        )
        local.saveRefeicao(localEntity)
        return remoteId
    }

    suspend fun updateMeal(id: Int, name: String) {
        remote.updateRefeicao(id, name)
        // Buscar o user id para manter integridade se necessário, ou assumir que o DAO trata se tivermos o objeto completo
        val current = local.getRefeicaoById(id)
        if (current != null) {
            local.updateRefeicao(current.copy(nome = name))
        }
    }

    suspend fun deleteMeal(id: Int) {
        remote.deleteRefeicao(id)
        local.deleteRefeicaoById(id)
    }

    // ---------- INGREDIENTES DA REFEIÇÃO ----------

    fun observeIngredients(mealId: Int): Flow<List<IngredienteRefeicao>> =
        local.getIngredientesByRefeicao(mealId).map { list -> list.map { it.toDomain() } }

    suspend fun refreshIngredientsFromRemote(mealId: Int) {
        try {
            val remoteIngs = remote.getIngredientesByRefeicao(mealId)
            // Aqui poderíamos ter um deleteByRefeicao no DAO se necessário
            // Por agora, o saveRefeicao usa REPLACE se o ID for o mesmo
            remoteIngs.forEach { dto ->
                local.saveIngrediente(
                    IngredienteRefeicaoLocal(
                        id = dto.id,
                        alimentoApiId = dto.alimentoapiid,
                        tipoPorcao = dto.tipoporcao,
                        quantidadePorcoes = dto.quantidadeporcoes,
                        idRefeicao = dto.idrefeicao ?: mealId
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun addIngredientToMeal(
        mealId: Int,
        alimentoApiId: String,
        tipoPorcao: String,
        quantidadePorcoes: Double,
    ) {
        // No remote.addIngrediente o backend devia devolver o ID criado!
        // Como a DietaApiService.addIngrediente devolve SimpleMessageResponse,
        // vamos ter de fazer refresh ou o backend mudar para devolver o ID.
        // Assumindo que por agora fazemos os dois e o local auto-gera se for 0,
        // mas o ideal é o ID vir do remote.
        remote.addIngrediente(
            alimentoApiId = alimentoApiId,
            tipoPorcao = tipoPorcao,
            quantidadePorcoes = quantidadePorcoes,
            idRefeicao = mealId,
        )
        
        // Para manter sincronia perfeita sem ID de volta, fazemos refresh
        refreshIngredientsFromRemote(mealId)
    }

    suspend fun updateIngredient(
        id: Int,
        alimentoApiId: String,
        tipoPorcao: String,
        quantidadePorcoes: Double,
        mealId: Int,
    ) {
        remote.updateIngrediente(
            id = id,
            alimentoApiId = alimentoApiId,
            tipoPorcao = tipoPorcao,
            quantidadePorcoes = quantidadePorcoes,
            idRefeicao = mealId,
        )
        val localEntity = IngredienteRefeicaoLocal(
            id = id,
            alimentoApiId = alimentoApiId,
            tipoPorcao = tipoPorcao,
            quantidadePorcoes = quantidadePorcoes,
            idRefeicao = mealId,
        )
        local.updateIngrediente(localEntity)
    }

    suspend fun deleteIngredient(id: Int) {
        remote.deleteIngrediente(id)
        local.deleteIngredienteById(id)
    }

    // ---------- RECEITAS FAVORITAS (FatSecret) ----------

    fun observeFavoriteRecipes(userId: Int): Flow<List<ReceitaFavorita>> =
        local.getReceitasFavoritasByUser(userId).map { list -> list.map { it.toDomain() } }

    suspend fun refreshFavoriteRecipesFromRemote(userId: Int) {
        try {
            val remoteFavs = remote.getReceitasFavoritasByUser(userId)
            local.deleteReceitasFavoritasByUser(userId)
            remoteFavs.forEach { dto ->
                local.saveReceitaFavorita(
                    ReceitaFavoritaLocal(
                        id = dto.id,
                        idUser = dto.iduser,
                        idReceitaApi = dto.idreceitaapi
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun addFavoriteRecipe(
        userId: Int,
        recipeApiId: String,
        nome: String? = null,
        image: String? = null,
        calories: Int? = 0,
        carbsPct: Int? = 0,
        proteinPct: Int? = 0,
        fatPct: Int? = 0,
        description: String? = ""
    ) {
        remote.addReceitaFavorita(userId, recipeApiId)
        // Salvamos logo localmente para ter os dados, o ID pode vir a 0 se o backend não devolver
        // Mas o refresh seguinte vai tentar sincronizar.
        val localEntity = ReceitaFavoritaLocal(
            idUser = userId,
            idReceitaApi = recipeApiId,
            nome = nome,
            image = image,
            calories = calories,
            carbsPct = carbsPct,
            proteinPct = proteinPct,
            fatPct = fatPct,
            description = description
        )
        local.saveReceitaFavorita(localEntity)
    }

    suspend fun deleteFavoriteRecipe(localId: Int) {
        // aqui assumimos que o id local coincide com o id da tabela receitasfavoritas no backend
        remote.deleteReceitaFavorita(localId)
        local.deleteReceitaFavoritaById(localId)
    }


    // ---------- FATSECRET FOODS (Search Meals) ----------

    suspend fun searchFoods(query: String): List<FatSecretFood> =
        remote.buscarAlimentosFatSecret(query).map { it.toDomain() }

    suspend fun getFoodDetails(id: String): FatSecretFoodDetails =
        remote.buscarAlimentoFatSecretPorId(id).toDomain()

    suspend fun searchAutocomplete(query: String): List<String> =
        remote.buscarAutocomplete(query).map { it.nome }


    // ---------- FATSECRET RECIPES (para uso futuro) ----------

    suspend fun searchRecipes(
        query: String,
        maxResults: Int = 20,
        page: Int = 0,
        type: String? = null,
        calories: String? = null,
    ): FatSecretRecipeSearchResult =
        remote.searchReceitasFatSecret(query, maxResults, page, type, calories).toDomain()


    suspend fun getRecipeDetails(id: String): FatSecretRecipe =
        remote.getReceitaById(id).toDomain()
}
