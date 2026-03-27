package com.example.treino.data.remote

import UpdateSetHistoryRequest
import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

// --- PASTAS ---
data class PastaDto(
    val id: Int,
    val nome: String,
    @SerializedName("id_user")
    val idUser: Int,
    val visibilidade: String, // 'publica' ou 'privada'
    @SerializedName("is_deletable")
    val isDeletable: Int = 1,
    val frequency: Int? = null,
    val experience: Int? = null
)

data class CreatePastaRequest(
    val nome: String,
    @SerializedName("id_user")
    val idUser: Int,
    val visibilidade: String = "privada",
    @SerializedName("is_deletable")
    val isDeletable: Int = 1
)

data class DuplicatePastaRequest(
    @SerializedName("id_pasta_global")
    val idPastaGlobal: Int,
    @SerializedName("id_user")
    val idUser: Int
)

// --- SESSOES ---
data class SessionWithDetailsDto(
    val id: Int,
    val nome: String,
    @SerializedName("id_pasta")
    val idPasta: Int,
    @SerializedName("id_Proxim_sessao")
    val idProximaSessao: Int = 0,
    val exercicios: List<ExerciseWithSetsDto> = emptyList()
)

data class ExerciseWithSetsDto(
    val id: Int,
    @SerializedName("exercicio_api_id")
    val exercicioApiId: String,
    val nome: String?,
    val notas: String,
    val ordem: Int,
    val imagem: String?,
    val bodypart: String?,
    val sets: List<ExercicioSetDto> = emptyList()
)

data class SessaoDto(
    val id: Int,
    val nome: String,
    @SerializedName("id_pasta")
    val idPasta: Int,
    @SerializedName("id_Proxim_sessao")
    val idProximaSessao: Int = 0
)

data class CreateSessaoRequest(
    val nome: String,
    @SerializedName("id_pasta")
    val idPasta: Int,
    val exercicios: List<SessionExerciseRequest> = emptyList()
)

data class SessionExerciseRequest(
    @SerializedName("id_exercicio")
    val idExercicio: String,
    val nome: String,
    val notas: String,
    val ordem: Int,
    val imagem: String?,
    val bodypart: String?,
    val sets: List<SessionSetRequest> = emptyList()
)

data class SessionSetRequest(
    @SerializedName("tipo_set")
    val tipoSet: String,
    val peso: Double,
    @SerializedName("repeticoes_min")
    val repeticoesMin: Int,
    @SerializedName("repeticoes_max")
    val repeticoesMax: Int,
    val ordem: Int
)

// --- EXERCICIOS ---
data class ExercicioDto(
    val id: Int,
    @SerializedName("exercicio_api_id")
    val exercicioApiId: String,
    val nome: String,
    val notas: String,
    @SerializedName("id_sessao")
    val idSessao: Int,
    val ordem: Int,
    val imagem: String? = null,
    val bodypart: String? = null
)

data class CreateExercicioRequest(
    @SerializedName("exercicio_api_id")
    val exercicioApiId: String,
    val notas: String = "",
    @SerializedName("id_sessao")
    val idSessao: Int
)

// --- SETS ---
data class ExercicioSetDto(
    val id: Int,
    @SerializedName("tipo_set")
    val tipoSet: String,
    val peso: Double,
    @SerializedName("repeticoes_min")
    val repeticoesMin: Int,
    @SerializedName("repeticoes_max")
    val repeticoesMax: Int,
    @SerializedName("peso_ultima_vez")
    val pesoUltimaVez: Double? = 0.0,
    @SerializedName("repeticoes_ultima_vez")
    val repeticoesUltimaVez: Int? = 0,
    val ordem: Int,
    @SerializedName("id_exercicio")
    val idExercicio: Int
)

data class CreateSetRequest(
    @SerializedName("tipo_set")
    val tipoSet: String,
    val peso: Double,
    @SerializedName("repeticoes_min")
    val repeticoesMin: Int,
    @SerializedName("repeticoes_max")
    val repeticoesMax: Int,
    @SerializedName("peso_ultima_vez")
    val pesoUltimaVez: Double = 0.0,
    @SerializedName("repeticoes_ultima_vez")
    val repeticoesUltimaVez: Int = 0,
    @SerializedName("id_exercicio")
    val idExercicio: Int,
    val ordem: Int
)

data class SimpleMessageResponse(
    val message: String? = null,
    val id: Int? = null,
    val pasta: PastaDto? = null,
    val sessao: SessaoDto? = null,
    val exercicio: ExercicioDto? = null,
    val setId: Int? = null
)

// --- EXERCICEDB ---
data class ExerciseDbMetaDto(
    val total: Int? = 0,
    val limit: Int? = 0,
    val after: String? = null,
    val before: String? = null
)

data class ExerciseDbSummaryDto(
    @SerializedName("exerciseId")
    val id: String,
    val name: String,
    @SerializedName("bodyPart")
    val bodyPart: String?,
    @SerializedName("bodyParts")
    val bodyParts: List<String>? = emptyList(),
    @SerializedName("equipment")
    val equipment: String?,
    @SerializedName("equipments")
    val equipments: List<String>? = emptyList(),
    @SerializedName("gifUrl")
    val gifUrl: String?,
    @SerializedName("imageUrl")
    val imageUrl: String?,
    @SerializedName("videoUrl")
    val videoUrl: String? = null,
    @SerializedName("overview")
    val overview: String? = null,
    @SerializedName("target")
    val target: String?,
    @SerializedName("targetMuscles")
    val targetMuscles: List<String>? = emptyList(),
    @SerializedName("secondaryMuscles")
    val secondaryMuscles: List<String>? = emptyList(),
    @SerializedName("instructions")
    val instructions: List<String>? = emptyList(),
    @SerializedName("variations")
    val variations: List<String>? = emptyList(),
    @SerializedName("exerciseType")
    val exerciseType: String? = null
)

data class ExerciseDbSearchResponse(
    val success: Boolean,
    val meta: ExerciseDbMetaDto?,
    val data: List<ExerciseDbSummaryDto>
)

data class ExerciseDbDetailsResponse(
    val success: Boolean,
    val data: ExerciseDbSummaryDto
)

interface TreinoApiService {

    // --- Pastas ---
    @GET("treinos/pastas/{id_user}")
    suspend fun getPastasByUser(@Path("id_user") idUser: Int): List<PastaDto>

    @GET("treinos/pastas/global")
    suspend fun getPastasGlobal(): List<PastaDto>

    @POST("treinos/pastas")
    suspend fun createPasta(@Body body: CreatePastaRequest): SimpleMessageResponse

    @PUT("treinos/pastas/{id}")
    suspend fun updatePasta(@Path("id") id: Int, @Body body: Map<String, String>): SimpleMessageResponse  

    @DELETE("treinos/pastas/{id}")
    suspend fun deletePasta(@Path("id") id: Int): SimpleMessageResponse

    @POST("treinos/duplicar-pasta")
    suspend fun duplicatePastaGlobal(@Body body: DuplicatePastaRequest): SimpleMessageResponse

    // --- Sessoes ---
    @GET("treinos/sessoes/{id_pasta}")
    suspend fun getSessoesByPasta(@Path("id_pasta") idPasta: Int): List<SessaoDto>

    @GET("treinos/sessao/details/{id}")
    suspend fun getSessaoById(@Path("id") id: Int): SessionWithDetailsDto

    @POST("treinos/sessao")
    suspend fun createSessao(@Body body: CreateSessaoRequest): SimpleMessageResponse

    @PUT("treinos/sessao/{id}")
    suspend fun updateSessao(@Path("id") id: Int, @Body body: CreateSessaoRequest): SimpleMessageResponse 

    @PUT("treinos/sessao/{id}")
    suspend fun updateSessaoName(@Path("id") id: Int, @Body body: Map<String, String>): SimpleMessageResponse

// --- Exercicios ---
    @DELETE("treinos/sessao/{id}")
    suspend fun deleteSessao(@Path("id") id: Int): SimpleMessageResponse

    // --- Exercicios ---
    @GET("treinos/exercicios/{id_sessao}")
    suspend fun getExerciciosBySessao(@Path("id_sessao") idSessao: Int): List<ExercicioDto>

    @POST("treinos/exercicio")
    suspend fun createExercicio(@Body body: CreateExercicioRequest): SimpleMessageResponse

    @PUT("treinos/exercicio/{id}")
    suspend fun updateExercicio(@Path("id") id: Int, @Body body: Map<String, String>): SimpleMessageResponse

    @DELETE("treinos/exercicio/{id}")
    suspend fun deleteExercicio(@Path("id") id: Int): SimpleMessageResponse

    // --- Sets ---
    @GET("treinos/sets/{id_exercicio}")
    suspend fun getSetsByExercicio(@Path("id_exercicio") idExercicio: Int): List<ExercicioSetDto>

    @POST("treinos/set")
    suspend fun createSet(@Body body: CreateSetRequest): SimpleMessageResponse

    @PUT("treinos/set/{id}")
    suspend fun updateSet(@Path("id") id: Int, @Body body: CreateSetRequest): SimpleMessageResponse       

    @PUT("treinos/set/history/{id}")
    suspend fun updateSetHistory(@Path("id") id: Int, @Body body: UpdateSetHistoryRequest): SimpleMessageResponse

    @DELETE("treinos/set/{id}")
    suspend fun deleteSet(@Path("id") id: Int): SimpleMessageResponse

    // --- ExerciseDB V2 ---
    @GET("treinos/exercises")
    suspend fun searchExercises(
        @Query("name") name: String? = null,
        @Query("bodyParts") bodyParts: String? = null,
        @Query("equipments") equipments: String? = null,
        @Query("targetMuscles") targetMuscles: String? = null,
        @Query("exerciseType") exerciseType: String? = null,
        @Query("limit") limit: Int? = null,
        @Query("after") after: String? = null,
        @Query("before") before: String? = null
    ): ExerciseDbSearchResponse

    @GET("treinos/exercise/{id}")
    suspend fun getExerciseDetails(@Path("id") id: String): ExerciseDbDetailsResponse

    @GET("treinos/public-workouts")
    suspend fun getPublicWorkouts(
        @Query("frequency") frequency: Int? = null,
        @Query("experience") experience: Int? = null
    ): List<PastaDto>
}
