package com.example.treino.data.repository

import com.example.treino.data.remote.CreateSessaoRequest
import com.example.treino.data.remote.CreateSetRequest
import com.example.treino.data.remote.ExerciseDbDetailsResponse
import com.example.treino.data.remote.ExerciseDbSearchResponse
import com.example.treino.data.remote.SessionWithDetailsDto
import com.example.treino.data.remote.repository.TreinoRemoteRepository
import com.example.treino.domain.mappers.toDomain
import com.example.treino.domain.mappers.toEntity
import com.example.treino.domain.models.Exercicio
import com.example.treino.domain.models.ExercicioSet
import com.example.treino.domain.models.Pasta
import com.example.treino.domain.models.Sessao
import com.example.treino.domain.repository.TreinoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TreinoRepositoryImpl @Inject constructor(
    private val remote: TreinoRemoteRepository,
    private val local: TreinoLocalRepository
) : TreinoRepository {

    // --- Pastas ---
    override fun observePastas(userId: Int): Flow<List<Pasta>> =
        local.observePastas(userId).map { list -> list.map { it.toDomain() } }

    override suspend fun refreshPastas(userId: Int) {
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

    override suspend fun createPasta(nome: String, userId: Int, visibilidade: String, isDeletable: Boolean): Int {
        val response = remote.createPasta(nome, userId, visibilidade)
        val newId = response.pasta?.id ?: response.id ?: 0
        refreshPastas(userId)
        return newId
    }

    override suspend fun updatePasta(id: Int, nome: String, userId: Int) {
        remote.updatePasta(id, nome)
        refreshPastas(userId)
    }

    override suspend fun deletePasta(id: Int, userId: Int) {
        remote.deletePasta(id)
        local.deletePastaById(id)
    }

    override suspend fun duplicatePastaGlobal(idPastaGlobal: Int, userId: Int) {
        remote.duplicatePastaGlobal(idPastaGlobal, userId)
        refreshPastas(userId)
    }

    // --- Sessoes ---
    override fun observeSessoes(idPasta: Int): Flow<List<Sessao>> =
        local.observeSessoes(idPasta).map { list -> list.map { it.toDomain() } }

    override suspend fun refreshSessoes(idPasta: Int) {
        try {
            val sessoes = remote.getSessoesByPasta(idPasta)
            local.deleteSessoesByPasta(idPasta)
            local.saveSessoes(sessoes.map { it.toEntity() })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun createSessao(request: CreateSessaoRequest): Int {
        val response = remote.createSessao(request)
        val newId = response.sessao?.id ?: response.id ?: 0
        refreshSessoes(request.idPasta)
        return newId
    }

    override suspend fun getSessaoWithDetails(id: Int): SessionWithDetailsDto {
        return remote.getSessaoById(id)
    }

    override suspend fun updateSessao(id: Int, request: CreateSessaoRequest) {
        remote.updateSessao(id, request)
        refreshSessoes(request.idPasta)
    }

    override suspend fun updateSessaoName(id: Int, nome: String, idPasta: Int) {
        remote.updateSessaoName(id, nome)
        refreshSessoes(idPasta)
    }

    override suspend fun deleteSessao(id: Int, idPasta: Int) {
        remote.deleteSessao(id)
        local.deleteSessaoById(id)
    }

    // --- Exercicios ---
    override fun observeExercicios(idSessao: Int): Flow<List<Exercicio>> =
        local.observeExercicios(idSessao).map { list -> list.map { it.toDomain() } }

    override suspend fun refreshExercicios(idSessao: Int) {
        try {
            val exercicios = remote.getExerciciosBySessao(idSessao)
            local.deleteExerciciosBySessao(idSessao)
            local.saveExercicios(exercicios.map { it.toEntity() })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun createExercicio(exercicioApiId: String, notas: String, idSessao: Int): Int {
        val response = remote.createExercicio(exercicioApiId, notas, idSessao)
        val newId = response.exercicio?.id ?: response.id ?: 0
        refreshExercicios(idSessao)
        return newId
    }

    override suspend fun updateExercicio(id: Int, notas: String, idSessao: Int) {
        remote.updateExercicio(id, notas)
        refreshExercicios(idSessao)
    }

    override suspend fun deleteExercicio(id: Int, idSessao: Int) {
        remote.deleteExercicio(id)
        local.deleteExercicioById(id)
    }

    // --- Sets ---
    override fun observeSets(idExercicio: Int): Flow<List<ExercicioSet>> =
        local.observeSets(idExercicio).map { list -> list.map { it.toDomain() } }

    override suspend fun refreshSets(idExercicio: Int) {
        try {
            val sets = remote.getSetsByExercicio(idExercicio)
            local.deleteSetsByExercicio(idExercicio)
            local.saveSets(sets.map { it.toEntity() })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun createSet(request: CreateSetRequest): Int {
        val response = remote.createSet(request)
        val newId = response.setId ?: response.id ?: 0
        refreshSets(request.idExercicio)
        return newId
    }

    override suspend fun updateSet(id: Int, request: CreateSetRequest) {
        remote.updateSet(id, request)
        refreshSets(request.idExercicio)
    }

    override suspend fun deleteSet(id: Int, idExercicio: Int) {
        remote.deleteSet(id)
        local.deleteSetById(id)
    }

    // --- ExerciseDB ---
    override suspend fun searchExercises(
        name: String?,
        bodyParts: String?,
        equipments: String?,
        targetMuscles: String?,
        exerciseType: String?,
        limit: Int?,
        after: String?,
        before: String?
    ): ExerciseDbSearchResponse = remote.searchExercises(name, bodyParts, equipments, targetMuscles, exerciseType, limit, after, before)

    override suspend fun getExerciseDetails(id: String): ExerciseDbDetailsResponse = remote.getExerciseDetails(id)
}
