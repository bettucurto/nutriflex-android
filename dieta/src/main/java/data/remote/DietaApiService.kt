// dieta/src/main/java/com/example/dieta/remote/DietaApiService.kt
package com.example.dieta.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

data class RefeicaoDto(
    val id: Int,
    val nome: String,
    @SerializedName("id_user")
    val iduser: Int,
    val image: String?,
    val calories: Int?,
    @SerializedName("fat_pct")
    val fatPct: Int?,
    @SerializedName("carbs_pct")
    val carbsPct: Int?,
    @SerializedName("protein_pct")
    val proteinPct: Int?,
    val description: String?,
    val ingredientes: List<IngredienteDto>? = emptyList()
)

data class CreateRefeicaoRequest(
    val nome: String,
    @SerializedName("id_user")
    val idUser: Int,
    val image: String? = null,
    val calories: Int? = 0,
    @SerializedName("fat_pct")
    val fatPct: Int? = 0,
    @SerializedName("carbs_pct")
    val carbsPct: Int? = 0,
    @SerializedName("protein_pct")
    val proteinPct: Int? = 0,
    val description: String? = ""
)

data class CreateRefeicaoResponse(
    val message: String,
    val id: Int
)

data class UpdateRefeicaoRequest(
    val nome: String,
    val image: String? = null,
    val calories: Int? = 0,
    @SerializedName("fat_pct")
    val fatPct: Int? = 0,
    @SerializedName("carbs_pct")
    val carbsPct: Int? = 0,
    @SerializedName("protein_pct")
    val proteinPct: Int? = 0,
    val description: String? = ""
)

data class IngredienteDto(
    val id: Int,
    @SerializedName("alimento_api_id")
    val alimentoapiid: String,
    @SerializedName("nome_alimento")
    val nomealimento: String?,
    @SerializedName("tipo_porcao")
    val tipoporcao: String,
    @SerializedName("quantidade_porcoes")
    val quantidadeporcoes: Double,
    @SerializedName("id_refeicao")
    val idrefeicao: Int?,
)

data class CreateIngredienteRequest(
    @SerializedName("alimento_api_id")
    val alimentoapiid: String,

    @SerializedName("nome_alimento")
    val nomealimento: String,

    @SerializedName("tipo_porcao")
    val tipoporcao: String,

    @SerializedName("quantidade_porcoes")
    val quantidadeporcoes: Double,

    @SerializedName("id_refeicao")
    val idrefeicao: Int,
)

data class UpdateIngredienteRequest(
    @SerializedName("alimento_api_id")
    val alimentoapiid: String,

    @SerializedName("nome_alimento")
    val nomealimento: String,

    @SerializedName("tipo_porcao")
    val tipoporcao: String,

    @SerializedName("quantidade_porcoes")
    val quantidadeporcoes: Double,

    @SerializedName("id_refeicao")
    val idrefeicao: Int,
)

data class StringDto(val nome: String)


data class ReceitaFavoritaDto(
    val id: Int,
    @SerializedName("id_user")
    val iduser: Int,
    @SerializedName("id_receita_api")
    val idreceitaapi: String,
    val nome: String?,
    val image: String?,
    val calories: Int?,
    @SerializedName("carbs_pct")
    val carbsPct: Int?,
    @SerializedName("protein_pct")
    val proteinPct: Int?,
    @SerializedName("fat_pct")
    val fatPct: Int?,
    val description: String?
)

data class CreateReceitaFavoritaRequest(
    @SerializedName("id_user")
    val iduser: Int,
    @SerializedName("id_receita_api")
    val idreceitaapi: String,
    val nome: String?,
    val image: String?,
    val calories: Int?,
    @SerializedName("carbs_pct")
    val carbsPct: Int?,
    @SerializedName("protein_pct")
    val proteinPct: Int?,
    @SerializedName("fat_pct")
    val fatPct: Int?,
    val description: String?
)

data class FatSecretFoodDto(
    val id: String,
    val nome_en: String,
    val descricao_en: String,
    val tipo: String,
    val url: String,
    val image: String?,
    val calories: String?,
    val carbs_grams: String?,
    val protein_grams: String?,
    val fat_grams: String?,
    val macro_split: MacroSplitDto?,
)


data class MacroSplitDto(
    val carbs: Int?,
    val protein: Int?,
    val fat: Int?
)

data class FatSecretAllergenDto(
    val id: String,
    val name: String,
    val value: String
)

data class FatSecretPreferenceDto(
    val id: String,
    val name: String,
    val value: String
)

data class FatSecretFoodDetailsDto(
    val id: String,
    val nomeen: String,
    val descricaoen: String,
    val porcao: String,
    val calorias: String,
    val proteina: String,
    val gordura: String,
    val carboidratos: String,
    val image: String?,
    val servings: List<FatSecretServingDto>,
    val allergens: List<FatSecretAllergenDto> = emptyList(),
    val preferences: List<FatSecretPreferenceDto> = emptyList()
)


// NOVO
data class FatSecretServingDto(
    val servingid: String,
    val servingdescription: String,
    val metricservingamount: Double?,
    val metricservingunit: String?,   // "g", "ml" ou "oz"
    val numberofunits: Double?,
    val measurementdescription: String?,
    val calories: String,
    val carbohydrate: String,
    val protein: String,
    val fat: String,
    val saturatedfat: String?,
    val polyunsaturatedfat: String?,
    val monounsaturatedfat: String?,
    val cholesterol: String?,
    val sodium: String?,
    val potassium: String?,
    val fiber: String?,
    val sugar: String?,
    val vitamina: String?,
    val vitaminc: String?,
    val calcium: String?,
    val iron: String?
)


data class FatSecretRecipeNutritionDto(
    val calories: String,
    val carbohydrate: String,
    val fat: String,
    val protein: String,
)

data class FatSecretRecipeSummaryDto(
    val id: String,
    val nome_en: String,
    val descricao_en: String,
    val image: String?,                // pode faltar
    val nutrition: FatSecretRecipeNutritionDto,
    val macro_split: Any?,             // vem null no JSON
    val calories: String,
    val types: List<String>,
)

data class FatSecretRecipeSearchResponse(
    val sucesso: Boolean,
    val encontrados: Int,
    val receitas: List<FatSecretRecipeSummaryDto>,
)

// --------- detalhes de receita ---------

data class FatSecretRecipeCategoryDto(
    val name: String,
    val url: String,
)

data class FatSecretRecipeServingDto(
    val calcium: String,
    val calories: String,
    val carbohydrate: String,
    val cholesterol: String,
    val fat: String,
    val fiber: String,
    val iron: String,
    val monounsaturated_fat: String,
    val polyunsaturated_fat: String,
    val potassium: String,
    val protein: String,
    val saturated_fat: String,
    val serving_size: String,
    val sodium: String,
    val sugar: String,
    val trans_fat: String,
    val vitamin_a: String,
    val vitamin_c: String,
)

data class FatSecretRecipeIngredientDto(
    val food_id: String,
    val food_name: String,
    val ingredient_description: String,
    val ingredient_url: String,
    val measurement_description: String,
    val number_of_units: String,
    val serving_id: String,
)

data class FatSecretRecipeDirectionDto(
    val direction_description: String,
    val direction_number: String,
)

data class FatSecretRecipeDetailsDto(
    val id: String,
    val name: String,
    val url: String,
    val description: String,
    val number_of_servings: Int,
    val grams_per_portion: Double,
    val preparation_time_min: Int,
    val cooking_time_min: Int,
    val rating: Int,
    val images: List<String>,
    val types: List<String>,
    val categories: List<FatSecretRecipeCategoryDto>,
    val servings: List<FatSecretRecipeServingDto>,
    val ingredients: List<FatSecretRecipeIngredientDto>,
    val directions: List<FatSecretRecipeDirectionDto>,
)



data class FatSecretRecipeDetailsResponse(
    val sucesso: Boolean,
    val receita: FatSecretRecipeDetailsDto,
)

data class SimpleMessageResponse(
    val message: String?,
    val id: Int? = null
)

interface DietaApiService {

    // --- Refeições favoritas ---
    @GET("refeicoes/user/{userId}")
    suspend fun getRefeicoesByUser(
        @Path("userId") userId: Int,
    ): List<RefeicaoDto>

    @GET("refeicoes/{id}")
    suspend fun getRefeicaoById(
        @Path("id") id: Int,
    ): RefeicaoDto

    @POST("refeicoes")
    suspend fun addRefeicaoFavorita(
        @Body body: CreateRefeicaoRequest,
    ): CreateRefeicaoResponse

    @PUT("refeicoes/{id}")
    suspend fun updateRefeicaoFavorita(
        @Path("id") id: Int,
        @Body body: UpdateRefeicaoRequest,
    ): SimpleMessageResponse

    @DELETE("refeicoes/{id}")
    suspend fun deleteRefeicaoFavorita(
        @Path("id") id: Int,
    ): SimpleMessageResponse

    // --- Ingredientes ---
    @GET("refeicoes/ingredientes/{idrefeicao}")
    suspend fun getIngredientesByRefeicao(
        @Path("idrefeicao") idRefeicao: Int,
    ): List<IngredienteDto>

    @GET("refeicoes/ingredientes/item/{id}")
    suspend fun getIngredienteById(
        @Path("id") id: Int,
    ): IngredienteDto

    @POST("refeicoes/ingredientes")
    suspend fun addIngrediente(
        @Body body: CreateIngredienteRequest,
    ): SimpleMessageResponse

    @GET("refeicoes/autocomplete")
    suspend fun buscarAutocomplete(@Query("q") q: String): List<StringDto>

    @PUT("refeicoes/ingredientes/{id}")
    suspend fun updateIngrediente(
        @Path("id") id: Int,
        @Body body: UpdateIngredienteRequest,
    ): SimpleMessageResponse

    @DELETE("refeicoes/ingredientes/{id}")
    suspend fun deleteIngrediente(
        @Path("id") id: Int,
    ): SimpleMessageResponse

    @DELETE("refeicoes/ingredientes/refeicao/{idrefeicao}")
    suspend fun deleteIngredientesByRefeicao(
        @Path("idrefeicao") idRefeicao: Int,
    ): SimpleMessageResponse

    // --- Receitas favoritas ---
    @GET("refeicoes/receitas/favoritas/{userId}")
    suspend fun getReceitasFavoritasByUser(
        @Path("userId") userId: Int,
    ): List<ReceitaFavoritaDto>

    @POST("refeicoes/receitas/favoritas")
    suspend fun addReceitaFavorita(
        @Body body: CreateReceitaFavoritaRequest,
    ): SimpleMessageResponse

    @DELETE("refeicoes/receitas/favoritas/{id}")
    suspend fun deleteReceitaFavorita(
        @Path("id") id: Int,
    ): SimpleMessageResponse

    // --- FatSecret alimentos ---
    @GET("refeicoes/search")
    suspend fun buscarAlimentosFatSecret(
        @Query("nome") nome: String,
    ): List<FatSecretFoodDto>

    @GET("refeicoes/search/{id}")
    suspend fun buscarAlimentoFatSecretPorId(
        @Path("id") id: String,
    ): FatSecretFoodDetailsDto

    // --- FatSecret receitas ---
    @GET("refeicoes/receitas/search")
    suspend fun searchReceitasFatSecret(
        @Query("q") query: String,
        @Query("maxresults") maxResults: Int = 20,
        @Query("page") page: Int = 0,
        @Query("tipo") tipo: String? = null,
        @Query("calorias") calorias: String? = null,
    ): FatSecretRecipeSearchResponse

    @GET("refeicoes/receitas/{id}")
    suspend fun getReceitaById(
        @Path("id") id: String,
    ): FatSecretRecipeDetailsResponse

    @retrofit2.http.Multipart
    @POST("refeicoes/image")
    suspend fun recognizeMeal(
        @retrofit2.http.Part image: okhttp3.MultipartBody.Part
    ): okhttp3.ResponseBody
}
