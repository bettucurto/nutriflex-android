package com.example.treino.domain.repository

import com.example.treino.data.remote.CreateSetRequest
import com.example.treino.data.remote.repository.TreinoRemoteRepository
import com.example.treino.data.repository.TreinoLocalRepository
import com.example.treino.domain.mappers.toDomain
import com.example.treino.domain.mappers.toEntity
import com.example.treino.domain.models.Exercicio
import com.example.treino.domain.models.ExercicioSet
import com.example.treino.domain.models.Pasta
import com.example.treino.domain.models.Sessao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TreinoRepository @Inject constructor(
    private val remote: TreinoRemoteRepository,
    private val local: TreinoLocalRepository
) {
    // --- Pastas ---
    fun observePastas(userId: Int): Flow<List<Pasta>> =
        local.observePastas(userId).map { list -> list.map { it.toDomain() } }

    suspend fun refreshPastas(userId: Int) {
        try {
            val userPastas = remote.getPastasByUser(userId)
            val globalPastas = remote.getPastasGlobal()
            val allPastas = userPastas + globalPastas
            
            local.deleteUserPastas(userId)
            local.savePastas(allPastas.map { it.toEntity() })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun createPasta(nome: String, userId: Int, visibilidade: String = "privada", isDeletable: Boolean = true): Int {
        val response = remote.createPasta(nome, userId, visibilidade)
        val newId = response.pasta?.id ?: response.id ?: 0
        refreshPastas(userId)
        return newId
    }

    suspend fun updatePasta(id: Int, nome: String, userId: Int) {
        remote.updatePasta(id, nome)
        refreshPastas(userId)
    }

    suspend fun deletePasta(id: Int, userId: Int) {
        remote.deletePasta(id)
        local.deletePastaById(id)
    }

    suspend fun duplicatePastaGlobal(idPastaGlobal: Int, userId: Int) {
        remote.duplicatePastaGlobal(idPastaGlobal, userId)
        refreshPastas(userId)
    }

    // --- Sessoes ---
    fun observeSessoes(idPasta: Int): Flow<List<Sessao>> =
        local.observeSessoes(idPasta).map { list -> list.map { it.toDomain() } }

    suspend fun refreshSessoes(idPasta: Int) {
        try {
            val sessoes = remote.getSessoesByPasta(idPasta)
            local.deleteSessoesByPasta(idPasta)
            local.saveSessoes(sessoes.map { it.toEntity() })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun createSessao(nome: String, idPasta: Int): Int {
        val response = remote.createSessao(nome, idPasta)
        val newId = response.sessao?.id ?: response.id ?: 0
        refreshSessoes(idPasta)
        return newId
    }

    suspend fun updateSessao(id: Int, nome: String, idPasta: Int) {
        remote.updateSessao(id, nome)
        refreshSessoes(idPasta)
    }

    suspend fun deleteSessao(id: Int, idPasta: Int) {
        remote.deleteSessao(id)
        local.deleteSessaoById(id)
    }

    // --- Exercicios ---
    fun observeExercicios(idSessao: Int): Flow<List<Exercicio>> =
        local.observeExercicios(idSessao).map { list -> list.map { it.toDomain() } }

    suspend fun refreshExercicios(idSessao: Int) {
        try {
            val exercicios = remote.getExerciciosBySessao(idSessao)
            local.deleteExerciciosBySessao(idSessao)
            local.saveExercicios(exercicios.map { it.toEntity() })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun createExercicio(exercicioApiId: String, notas: String, idSessao: Int): Int {
        val response = remote.createExercicio(exercicioApiId, notas, idSessao)
        val newId = response.exercicio?.id ?: response.id ?: 0
        refreshExercicios(idSessao)
        return newId
    }

    suspend fun updateExercicio(id: Int, notas: String, idSessao: Int) {
        remote.updateExercicio(id, notas)
        refreshExercicios(idSessao)
    }

    suspend fun deleteExercicio(id: Int, idSessao: Int) {
        remote.deleteExercicio(id)
        local.deleteExercicioById(id)
    }

    // --- Sets ---
    fun observeSets(idExercicio: Int): Flow<List<ExercicioSet>> =
        local.observeSets(idExercicio).map { list -> list.map { it.toDomain() } }

    suspend fun refreshSets(idExercicio: Int) {
        try {
            val sets = remote.getSetsByExercicio(idExercicio)
            local.deleteSetsByExercicio(idExercicio)
            local.saveSets(sets.map { it.toEntity() })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun createSet(request: CreateSetRequest): Int {
        val response = remote.createSet(request)
        val newId = response.setId ?: response.id ?: 0
        refreshSets(request.idExercicio)
        return newId
    }

    suspend fun updateSet(id: Int, request: CreateSetRequest) {
        remote.updateSet(id, request)
        refreshSets(request.idExercicio)
    }

    suspend fun deleteSet(id: Int, idExercicio: Int) {
        remote.deleteSet(id)
        local.deleteSetById(id)
    }

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
    ) = remote.searchExercises(name, bodyParts, equipments, targetMuscles, exerciseType, limit, after, before)

    suspend fun getExerciseDetails(id: String) = remote.getExerciseDetails(id)
}
