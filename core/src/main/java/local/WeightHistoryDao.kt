package local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import local.tables.WeightHistory

@Dao
interface WeightHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: WeightHistory)

    // últimos N dias
    @Query("""
        SELECT * FROM weight_history
        WHERE userId = :userId AND date >= :fromDate
        ORDER BY date
    """)
    suspend fun getFromDate(userId: Int, fromDate: String): List<WeightHistory>

    // tudo
    @Query("""
        SELECT * FROM weight_history
        WHERE userId = :userId
        ORDER BY date
    """)
    suspend fun getAll(userId: Int): List<WeightHistory>

    @Query("""
    DELETE FROM weight_history
    WHERE userId = :userId AND date = :date
""")
    suspend fun deleteByUserAndDate(userId: Int, date: String)

    @Query("DELETE FROM weight_history WHERE userId = :userId")
    suspend fun deleteByUserId(userId: Int)
}