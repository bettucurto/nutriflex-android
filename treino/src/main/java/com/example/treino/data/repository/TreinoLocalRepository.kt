package com.example.treino.data.repository

import com.example.treino.data.local.dao.TreinoDao
import com.example.treino.data.local.tables.ExercicioEntity
import com.example.treino.data.local.tables.PastaEntity
import com.example.treino.data.local.tables.SessaoEntity
import com.example.treino.data.local.tables.SetEntity
import javax.inject.Inject

class TreinoLocalRepository @Inject constructor(
    private val dao: TreinoDao
) {
    // --- Pastas ---
    fun observePastas(userId: Int) = dao.observePastas(userId)
    suspend fun savePastas(pastas: List<PastaEntity>) = dao.insertPastas(pastas)
    suspend fun deleteUserPastas(userId: Int) = dao.deleteUserPastas(userId)
    suspend fun deletePastaById(id: Int) = dao.deletePastaById(id)

    // --- Sessoes ---
    fun observeSessoes(idPasta: Int) = dao.observeSessoes(idPasta)
    fun observeAllSessoesByUser(userId: Int) = dao.observeAllSessoesByUser(userId)
    fun observeNextSessaoWithExercises(userId: Int, nextWorkoutId: Int?) = dao.observeNextSessaoWithExercises(userId, nextWorkoutId)
    suspend fun saveSessoes(sessoes: List<SessaoEntity>) = dao.insertSessoes(sessoes)
    suspend fun deleteSessoesByPasta(idPasta: Int) = dao.deleteSessoesByPasta(idPasta)
    suspend fun deleteSessaoById(id: Int) = dao.deleteSessaoById(id)

    // --- Exercicios ---
    fun observeExercicios(idSessao: Int) = dao.observeExercicios(idSessao)
    suspend fun saveExercicios(exercicios: List<ExercicioEntity>) = dao.insertExercicios(exercicios)
    suspend fun deleteExerciciosBySessao(idSessao: Int) = dao.deleteExerciciosBySessao(idSessao)
    suspend fun deleteExercicioById(id: Int) = dao.deleteExercicioById(id)

    // --- Sets ---
    fun observeSets(idExercicio: Int) = dao.observeSets(idExercicio)
    suspend fun saveSets(sets: List<SetEntity>) = dao.insertSets(sets)
    suspend fun deleteSetsByExercicio(idExercicio: Int) = dao.deleteSetsByExercicio(idExercicio)
    suspend fun deleteSetById(id: Int) = dao.deleteSetById(id)
    suspend fun updateSetHistory(setId: Int, peso: Double, reps: Int) = dao.updateSetHistory(setId, peso, reps)
}
