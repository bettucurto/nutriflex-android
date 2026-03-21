package com.example.treino.domain.repository

import com.example.treino.data.remote.CreateSessaoRequest
import com.example.treino.data.remote.CreateSetRequest
import com.example.treino.data.remote.ExerciseDbSearchResponse
import com.example.treino.data.remote.SessionWithDetailsDto
import com.example.treino.domain.models.Exercicio
import com.example.treino.domain.models.ExercicioSet
import com.example.treino.domain.models.Pasta
import com.example.treino.domain.models.Sessao
import kotlinx.coroutines.flow.Flow

interface TreinoRepository {
    // --- Pastas ---
    fun observePastas(userId: Int): Flow<List<Pasta>>
    suspend fun refreshPastas(userId: Int)
    suspend fun createPasta(nome: String, userId: Int, visibilidade: String = "privada", isDeletable: Boolean = true): Int
    suspend fun updatePasta(id: Int, nome: String, userId: Int)
    suspend fun deletePasta(id: Int, userId: Int)
    suspend fun duplicatePastaGlobal(idPastaGlobal: Int, userId: Int)

    // --- Sessoes ---
    fun observeSessoes(idPasta: Int): Flow<List<Sessao>>
    suspend fun refreshSessoes(idPasta: Int)
    suspend fun createSessao(request: CreateSessaoRequest): Int
    suspend fun getSessaoWithDetails(id: Int): SessionWithDetailsDto
    suspend fun updateSessao(id: Int, request: CreateSessaoRequest)
    suspend fun updateSessaoName(id: Int, nome: String, idPasta: Int)
    suspend fun deleteSessao(id: Int, idPasta: Int)

    // --- Exercicios ---
    fun observeExercicios(idSessao: Int): Flow<List<Exercicio>>
    suspend fun refreshExercicios(idSessao: Int)
    suspend fun createExercicio(exercicioApiId: String, notas: String, idSessao: Int): Int
    suspend fun updateExercicio(id: Int, notas: String, idSessao: Int)
    suspend fun deleteExercicio(id: Int, idSessao: Int)

    // --- Sets ---
    fun observeSets(idExercicio: Int): Flow<List<ExercicioSet>>
    suspend fun refreshSets(idExercicio: Int)
    suspend fun createSet(request: CreateSetRequest): Int
    suspend fun updateSet(id: Int, request: CreateSetRequest)
    suspend fun deleteSet(id: Int, idExercicio: Int)

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
    ): ExerciseDbSearchResponse
    suspend fun getExerciseDetails(id: String): com.example.treino.data.remote.ExerciseDbDetailsResponse
}
