// dieta/src/main/java/com/example/dieta/domain/DietaRepository.kt
package com.example.dieta.domain

import com.example.dieta.remote.DietaRemoteRepository
import data.local.repository.DietaLocalRepository
import data.local.tables.IngredienteRefeicaoLocal
import data.local.tables.ReceitaFavoritaLocal
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

    suspend fun refreshMealsFromRemote(userId: Int) {
        val remoteMeals = remote.getRefeicoesByUser(userId)
        // aqui podes implementar uma estratégia de sync; por agora só garantimos fetch remoto
        // e deixamos a gravação local para mais tarde, se quiseres.
    }

    suspend fun addMeal(name: String, userId: Int) {
        remote.addRefeicao(name, userId)
        // opcional: re-sync depois de criar
    }

    suspend fun updateMeal(id: Int, name: String) {
        remote.updateRefeicao(id, name)
    }

    suspend fun deleteMeal(id: Int) {
        remote.deleteRefeicao(id)
    }

    // ---------- INGREDIENTES DA REFEIÇÃO ----------

    fun observeIngredients(mealId: Int): Flow<List<IngredienteRefeicao>> =
        local.getIngredientesByRefeicao(mealId).map { list -> list.map { it.toDomain() } }

    suspend fun addIngredientToMeal(
        mealId: Int,
        alimentoApiId: String,
        porcaoGramas: Double,
        quantidadePorcoes: Double,
    ) {
        // remoto
        remote.addIngrediente(
            alimentoApiId = alimentoApiId,
            porcaoGramas = porcaoGramas,
            quantidadePorcoes = quantidadePorcoes,
            idRefeicao = mealId,
        )
        // local
        val localEntity = IngredienteRefeicaoLocal(
            id = 0,
            alimentoApiId = alimentoApiId,
            porcaoGramas = porcaoGramas,
            quantidadePorcoes = quantidadePorcoes,
            idRefeicao = mealId,
        )
        local.saveIngrediente(localEntity)
    }

    suspend fun updateIngredient(
        id: Int,
        alimentoApiId: String,
        porcaoGramas: Double,
        quantidadePorcoes: Double,
        mealId: Int,
    ) {
        remote.updateIngrediente(
            id = id,
            alimentoApiId = alimentoApiId,
            porcaoGramas = porcaoGramas,
            quantidadePorcoes = quantidadePorcoes,
            idRefeicao = mealId,
        )
        val localEntity = IngredienteRefeicaoLocal(
            id = id,
            alimentoApiId = alimentoApiId,
            porcaoGramas = porcaoGramas,
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

    suspend fun addFavoriteRecipe(userId: Int, recipeApiId: String) {
        remote.addReceitaFavorita(userId, recipeApiId)
        val localEntity = ReceitaFavoritaLocal(
            id = 0,
            idUser = userId,
            idReceitaApi = recipeApiId,
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
