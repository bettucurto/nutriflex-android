package local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS weight_history (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                userId INTEGER NOT NULL,
                date TEXT NOT NULL,
                weight REAL NOT NULL
            )
            """.trimIndent()
        )
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""ALTER TABLE user_local ADD COLUMN gender TEXT NOT NULL DEFAULT 'M'""")
        db.execSQL("""ALTER TABLE user_local ADD COLUMN birthDate TEXT NOT NULL DEFAULT ''""")
    }
}