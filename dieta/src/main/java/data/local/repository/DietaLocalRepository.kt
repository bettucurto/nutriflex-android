// dieta/src/main/java/com/example/dieta/local/DietaLocalRepository.kt
package data.local.repository


import data.local.dao.ReceitasFavoritasDao
import data.local.dao.RefeicoesDao
import data.local.tables.IngredienteRefeicaoLocal
import data.local.tables.ReceitaFavoritaLocal
import data.local.tables.RefeicaoFavoritaLocal
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DietaLocalRepository @Inject constructor(
    private val refeicoesDao: RefeicoesDao,
    private val receitasFavoritasDao: ReceitasFavoritasDao,
) {

    // dentro de DietaLocalRepository

    suspend fun deleteIngredienteById(id: Int) =
        refeicoesDao.deleteIngredienteById(id)

    suspend fun saveReceitaFavorita(receita: ReceitaFavoritaLocal): Long =
        receitasFavoritasDao.insertReceitaFavorita(receita)

    suspend fun deleteReceitaFavoritaById(id: Int) =
        receitasFavoritasDao.deleteReceitaFavoritaById(id)

    fun getRefeicoesByUser(userId: Int): Flow<List<RefeicaoFavoritaLocal>> =
        refeicoesDao.getRefeicoesByUser(userId)

    suspend fun getRefeicaoById(id: Int): RefeicaoFavoritaLocal? =
        refeicoesDao.getRefeicaoById(id)

    fun observeRefeicaoById(id: Int): Flow<RefeicaoFavoritaLocal?> =
        refeicoesDao.observeRefeicaoById(id)

    suspend fun saveRefeicao(refeicao: RefeicaoFavoritaLocal): Long =
        refeicoesDao.insertRefeicao(refeicao)

    suspend fun updateRefeicao(refeicao: RefeicaoFavoritaLocal) =
        refeicoesDao.updateRefeicao(refeicao)

    suspend fun deleteRefeicao(refeicao: RefeicaoFavoritaLocal) =
        refeicoesDao.deleteRefeicao(refeicao)

    suspend fun deleteRefeicaoById(id: Int) =
        refeicoesDao.deleteRefeicaoById(id)

    suspend fun deleteRefeicoesByUser(userId: Int) =
        refeicoesDao.deleteRefeicoesByUser(userId)

    fun getIngredientesByRefeicao(idRefeicao: Int): Flow<List<IngredienteRefeicaoLocal>> =
        refeicoesDao.getIngredientesByRefeicao(idRefeicao)

    suspend fun saveIngrediente(ingrediente: IngredienteRefeicaoLocal): Long =
        refeicoesDao.insertIngrediente(ingrediente)

    suspend fun updateIngrediente(ingrediente: IngredienteRefeicaoLocal) =
        refeicoesDao.updateIngrediente(ingrediente)

    suspend fun deleteIngrediente(ingrediente: IngredienteRefeicaoLocal) =
        refeicoesDao.deleteIngrediente(ingrediente)

    fun getReceitasFavoritasByUser(userId: Int): Flow<List<ReceitaFavoritaLocal>> =
        receitasFavoritasDao.getReceitasFavoritasByUser(userId)


    suspend fun deleteReceitaFavorita(receita: ReceitaFavoritaLocal) =
        receitasFavoritasDao.deleteReceitaFavorita(receita)

    suspend fun deleteReceitasFavoritasByUser(userId: Int) =
        receitasFavoritasDao.deleteReceitasFavoritasByUser(userId)
}
