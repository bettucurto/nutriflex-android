package com.example.treino.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.treino.data.local.dao.TreinoDao
import com.example.treino.data.local.tables.ExercicioEntity
import com.example.treino.data.local.tables.PastaEntity
import com.example.treino.data.local.tables.SessaoEntity
import com.example.treino.data.local.tables.SetEntity

@Database(
    entities = [
        PastaEntity::class,
        SessaoEntity::class,
        ExercicioEntity::class,
        SetEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class TreinoDatabase : RoomDatabase() {
    abstract fun treinoDao(): TreinoDao

    companion object {
        val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE pastas_treinos ADD COLUMN visibilidade TEXT NOT NULL DEFAULT 'privada'")
                db.execSQL("ALTER TABLE pastas_treinos ADD COLUMN isDeletable INTEGER NOT NULL DEFAULT 1")
            }
        }
    }
}
