package com.example.treino.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.treino.data.local.tables.ExercicioEntity
import com.example.treino.data.local.tables.PastaEntity
import com.example.treino.data.local.tables.SessaoEntity
import com.example.treino.data.local.tables.SetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TreinoDao {

    // --- Pastas ---
    @Query("SELECT * FROM pastas_treinos WHERE idUser = :userId OR visibilidade = 'publica'")
    fun observePastas(userId: Int): Flow<List<PastaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPastas(pastas: List<PastaEntity>)

    @Query("DELETE FROM pastas_treinos WHERE idUser = :userId")
    suspend fun deleteUserPastas(userId: Int)

    @Query("DELETE FROM pastas_treinos WHERE id = :id")
    suspend fun deletePastaById(id: Int)

    // --- Sessoes ---
    @Query("SELECT * FROM sessoes WHERE idPasta = :idPasta")
    fun observeSessoes(idPasta: Int): Flow<List<SessaoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessoes(sessoes: List<SessaoEntity>)

    @Query("DELETE FROM sessoes WHERE idPasta = :idPasta")
    suspend fun deleteSessoesByPasta(idPasta: Int)

    @Query("DELETE FROM sessoes WHERE id = :id")
    suspend fun deleteSessaoById(id: Int)

    // --- Exercicios ---
    @Query("SELECT * FROM sessao_exercicios WHERE idSessao = :idSessao ORDER BY ordem ASC")
    fun observeExercicios(idSessao: Int): Flow<List<ExercicioEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercicios(exercicios: List<ExercicioEntity>)

    @Query("DELETE FROM sessao_exercicios WHERE idSessao = :idSessao")
    suspend fun deleteExerciciosBySessao(idSessao: Int)

    @Query("DELETE FROM sessao_exercicios WHERE id = :id")
    suspend fun deleteExercicioById(id: Int)

    // --- Sets ---
    @Query("SELECT * FROM exercicio_sets WHERE idExercicio = :idExercicio ORDER BY ordem ASC")
    fun observeSets(idExercicio: Int): Flow<List<SetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSets(sets: List<SetEntity>)

    @Query("DELETE FROM exercicio_sets WHERE idExercicio = :idExercicio")
    suspend fun deleteSetsByExercicio(idExercicio: Int)

    @Query("DELETE FROM exercicio_sets WHERE id = :id")
    suspend fun deleteSetById(id: Int)

    @Query("UPDATE exercicio_sets SET pesoUltimaVez = :peso, repeticoesUltimaVez = :reps WHERE id = :setId")
    suspend fun updateSetHistory(setId: Int, peso: Double, reps: Int)
}
