// local/RefeicoesDao.kt
package data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import data.local.tables.IngredienteRefeicaoLocal
import data.local.tables.RefeicaoFavoritaLocal
import kotlinx.coroutines.flow.Flow

@Dao
interface RefeicoesDao {

    @Query("SELECT * FROM refeicoes_favoritas WHERE id_user = :userId")
    fun getRefeicoesByUser(userId: Int): Flow<List<RefeicaoFavoritaLocal>>

    @Query("SELECT * FROM refeicoes_favoritas WHERE id = :id LIMIT 1")
    suspend fun getRefeicaoById(id: Int): RefeicaoFavoritaLocal?

    @Query("SELECT * FROM refeicoes_favoritas WHERE id = :id LIMIT 1")
    fun observeRefeicaoById(id: Int): Flow<RefeicaoFavoritaLocal?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRefeicao(refeicao: RefeicaoFavoritaLocal): Long

    @Update
    suspend fun updateRefeicao(refeicao: RefeicaoFavoritaLocal)

    @Delete
    suspend fun deleteRefeicao(refeicao: RefeicaoFavoritaLocal)

    @Query("DELETE FROM refeicoes_favoritas WHERE id = :id")
    suspend fun deleteRefeicaoById(id: Int)

    @Query("DELETE FROM refeicoes_favoritas WHERE id_user = :userId")
    suspend fun deleteRefeicoesByUser(userId: Int)

    // Ingredientes
    @Query("SELECT * FROM refeicao_ingredientes WHERE id_refeicao = :idRefeicao")
    fun getIngredientesByRefeicao(idRefeicao: Int): Flow<List<IngredienteRefeicaoLocal>>

    @Query("SELECT * FROM refeicao_ingredientes WHERE id = :id LIMIT 1")
    suspend fun getIngredienteById(id: Int): IngredienteRefeicaoLocal?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngrediente(ingrediente: IngredienteRefeicaoLocal): Long

    @Update
    suspend fun updateIngrediente(ingrediente: IngredienteRefeicaoLocal)

    @Delete
    suspend fun deleteIngrediente(ingrediente: IngredienteRefeicaoLocal)

    @Query("DELETE FROM refeicao_ingredientes WHERE id = :id")
    suspend fun deleteIngredienteById(id: Int)

    @Query("DELETE FROM refeicao_ingredientes WHERE id_refeicao = :idRefeicao")
    suspend fun deleteIngredientesByRefeicao(idRefeicao: Int)

}
