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
            // Sincronização: só apaga localmente se a chamada remota for bem sucedida
            local.deleteRefeicoesByUser(userId)
            remoteMeals.forEach { dto ->
                local.saveRefeicao(
                    RefeicaoFavoritaLocal(
                        id = dto.id,
                        nome = dto.nome,
                        idUser = dto.iduser,
                        calories = dto.calories,
                        image = dto.image,
                        carbsPct = dto.carbsPct,
                        proteinPct = dto.proteinPct,
                        fatPct = dto.fatPct,
                        description = dto.description
                    )
                )
                // Salvar ingredientes se existirem no DTO
                dto.ingredientes?.forEach { ingDto ->
                    local.saveIngrediente(
                        IngredienteRefeicaoLocal(
                            id = ingDto.id,
                            alimentoApiId = ingDto.alimentoapiid,
                            nomeAlimento = ingDto.nomealimento,
                            tipoPorcao = ingDto.tipoporcao,
                            quantidadePorcoes = ingDto.quantidadeporcoes,
                            idRefeicao = dto.id
                        )
                    )
                }
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
        android.util.Log.d("DietaRepository", "addMeal: Starting remote add for user $userId")
        val remoteId = remote.addRefeicao(
            nome = name,
            userId = userId,
            image = image,
            calories = calories,
            fatPct = fatPct,
            carbsPct = carbsPct,
            proteinPct = proteinPct,
            description = description
        )
        android.util.Log.d("DietaRepository", "addMeal: Remote success, got ID: $remoteId")

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
        val resultId = local.saveRefeicao(localEntity)
        android.util.Log.d("DietaRepository", "addMeal: Local save result: $resultId (expected $remoteId)")

        return remoteId
    }

    suspend fun updateMeal(
        id: Int,
        name: String,
        calories: Int? = 0,
        image: String? = null,
        carbsPct: Int? = 0,
        proteinPct: Int? = 0,
        fatPct: Int? = 0,
        description: String? = "Custom Meal"
    ) {
        // Atualizar dados da refeição
        remote.updateRefeicao(
            id = id,
            nome = name,
            image = image,
            calories = calories,
            fatPct = fatPct,
            carbsPct = carbsPct,
            proteinPct = proteinPct,
            description = description
        )
        
        // Limpar ingredientes antigos para evitar duplicação
        // O FavoriteMealEditorViewModel voltará a adicionar os ingredientes da lista atual
        remote.deleteIngredientesByRefeicao(id)
        local.deleteIngredientesByRefeicao(id)

        val current = local.getRefeicaoById(id)
        if (current != null) {
            local.updateRefeicao(
                current.copy(
                    nome = name,
                    calories = calories,
                    image = image,
                    carbsPct = carbsPct,
                    proteinPct = proteinPct,
                    fatPct = fatPct,
                    description = description
                )
            )
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
            remoteIngs.forEach { dto ->
                local.saveIngrediente(
                    IngredienteRefeicaoLocal(
                        id = dto.id,
                        alimentoApiId = dto.alimentoapiid,
                        nomeAlimento = dto.nomealimento,
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
        nomeAlimento: String,
        tipoPorcao: String,
        quantidadePorcoes: Double,
    ) {
        remote.addIngrediente(
            alimentoApiId = alimentoApiId,
            nomeAlimento = nomeAlimento,
            tipoPorcao = tipoPorcao,
            quantidadePorcoes = quantidadePorcoes,
            idRefeicao = mealId,
        )
        refreshIngredientsFromRemote(mealId)
    }

    suspend fun updateIngredient(
        id: Int,
        alimentoApiId: String,
        nomeAlimento: String,
        tipoPorcao: String,
        quantidadePorcoes: Double,
        mealId: Int,
    ) {
        remote.updateIngrediente(
            id = id,
            alimentoApiId = alimentoApiId,
            nomeAlimento = nomeAlimento,
            tipoPorcao = tipoPorcao,
            quantidadePorcoes = quantidadePorcoes,
            idRefeicao = mealId,
        )
        val localEntity = IngredienteRefeicaoLocal(
            id = id,
            alimentoApiId = alimentoApiId,
            nomeAlimento = nomeAlimento,
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
            // Só apaga localmente se a chamada remota for bem sucedida
            local.deleteReceitasFavoritasByUser(userId)
            remoteFavs.forEach { dto ->
                local.saveReceitaFavorita(
                    ReceitaFavoritaLocal(
                        id = dto.id,
                        idUser = dto.iduser,
                        idReceitaApi = dto.idreceitaapi,
                        nome = dto.nome,
                        image = dto.image,
                        calories = dto.calories,
                        carbsPct = dto.carbsPct,
                        proteinPct = dto.proteinPct,
                        fatPct = dto.fatPct,
                        description = dto.description
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
        val response = remote.addReceitaFavorita(
            userId = userId,
            recipeApiId = recipeApiId,
            nome = nome,
            image = image,
            calories = calories,
            carbsPct = carbsPct,
            proteinPct = proteinPct,
            fatPct = fatPct,
            description = description
        )
        val serverId = response.id ?: 0

        val localEntity = ReceitaFavoritaLocal(
            id = serverId,
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
