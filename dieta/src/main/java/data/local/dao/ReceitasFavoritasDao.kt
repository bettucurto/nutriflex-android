// local/ReceitasFavoritasDao.kt
package data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import data.local.tables.ReceitaFavoritaLocal
import kotlinx.coroutines.flow.Flow

@Dao
interface ReceitasFavoritasDao {

    @Query("SELECT * FROM receitas_favoritas WHERE id_user = :userId")
    fun getReceitasFavoritasByUser(userId: Int): Flow<List<ReceitaFavoritaLocal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceitaFavorita(receita: ReceitaFavoritaLocal): Long

    @Delete
    suspend fun deleteReceitaFavorita(receita: ReceitaFavoritaLocal)

    @Query("DELETE FROM receitas_favoritas WHERE id = :id")
    suspend fun deleteReceitaFavoritaById(id: Int)

    @Query("DELETE FROM receitas_favoritas WHERE id_user = :userId")
    suspend fun deleteReceitasFavoritasByUser(userId: Int)

}
