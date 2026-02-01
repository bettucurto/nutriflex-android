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
        db.execSQL("""ALTER TABLE user_local ADD COLUMN birthDate TEXT NOT NULL DEFAULT ''""")
        db.execSQL("""ALTER TABLE user_local ADD COLUMN gender TEXT NOT NULL DEFAULT 'M'""")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""ALTER TABLE user_local ADD COLUMN dailyCarbsGrams INTEGER NOT NULL DEFAULT 0""")
        db.execSQL("""ALTER TABLE user_local ADD COLUMN dailyProteinGrams INTEGER NOT NULL DEFAULT 0""")
        db.execSQL("""ALTER TABLE user_local ADD COLUMN dailyFatGrams INTEGER NOT NULL DEFAULT 0""")
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""ALTER TABLE user_local ADD COLUMN activityLevel INTEGER NOT NULL DEFAULT 0""")
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""ALTER TABLE user_local ADD COLUMN eatenProteinToday INTEGER NOT NULL DEFAULT 0""")
        db.execSQL("""ALTER TABLE user_local ADD COLUMN eatenCarbsToday INTEGER NOT NULL DEFAULT 0""")
        db.execSQL("""ALTER TABLE user_local ADD COLUMN eatenFatToday INTEGER NOT NULL DEFAULT 0""")
    }
}