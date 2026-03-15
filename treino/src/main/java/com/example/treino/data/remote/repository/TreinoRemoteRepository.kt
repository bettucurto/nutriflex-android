package com.example.treino.data.remote.repository

import com.example.treino.data.remote.*
import javax.inject.Inject

class TreinoRemoteRepository @Inject constructor(
    private val api: TreinoApiService
) {
    // --- Pastas ---
    suspend fun getPastasByUser(userId: Int) = api.getPastasByUser(userId)
    suspend fun getPastasGlobal() = api.getPastasGlobal()
    suspend fun createPasta(nome: String, userId: Int, visibilidade: String) = 
        api.createPasta(CreatePastaRequest(nome, userId, visibilidade))
    suspend fun updatePasta(id: Int, nome: String) = 
        api.updatePasta(id, mapOf("nome" to nome))
    suspend fun deletePasta(id: Int) = api.deletePasta(id)
    suspend fun duplicatePastaGlobal(idPastaGlobal: Int, userId: Int) = 
        api.duplicatePastaGlobal(DuplicatePastaRequest(idPastaGlobal, userId))

    // --- Sessoes ---
    suspend fun getSessoesByPasta(idPasta: Int) = api.getSessoesByPasta(idPasta)
    suspend fun createSessao(nome: String, idPasta: Int) = 
        api.createSessao(CreateSessaoRequest(nome, idPasta))
    suspend fun updateSessao(id: Int, nome: String) = 
        api.updateSessao(id, mapOf("nome" to nome))
    suspend fun deleteSessao(id: Int) = api.deleteSessao(id)

    // --- Exercicios ---
    suspend fun getExerciciosBySessao(idSessao: Int) = api.getExerciciosBySessao(idSessao)
    suspend fun createExercicio(exercicioApiId: String, notas: String, idSessao: Int) = 
        api.createExercicio(CreateExercicioRequest(exercicioApiId, notas, idSessao))
    suspend fun updateExercicio(id: Int, notas: String) = 
        api.updateExercicio(id, mapOf("notas" to notas))
    suspend fun deleteExercicio(id: Int) = api.deleteExercicio(id)

    // --- Sets ---
    suspend fun getSetsByExercicio(idExercicio: Int) = api.getSetsByExercicio(idExercicio)
    suspend fun createSet(request: CreateSetRequest) = api.createSet(request)
    suspend fun updateSet(id: Int, request: CreateSetRequest) = api.updateSet(id, request)
    suspend fun deleteSet(id: Int) = api.deleteSet(id)

    // --- ExerciseDB ---
    suspend fun searchExercises(
        name: String? = null,
        bodyParts: String? = null,
        equipments: String? = null,
        targetMuscles: String? = null,
        exerciseType: String? = null,
        limit: Int? = null,
        after: String? = null,
        before: String? = null
    ) = api.searchExercises(name, bodyParts, equipments, targetMuscles, exerciseType, limit, after, before)

    suspend fun getExerciseDetails(id: String) = api.getExerciseDetails(id)
}
