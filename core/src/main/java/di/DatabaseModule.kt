package di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import local.MIGRATION_1_2
import local.MIGRATION_2_3
import local.NutriflexDatabase
import local.UserLocalDao
import local.WeightHistoryDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): NutriflexDatabase {
        return Room.databaseBuilder(
            context,
            NutriflexDatabase::class.java,
            "nutriflex.db"
        )
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .build()
    }

    @Provides
    fun provideUserLocalDao(db: NutriflexDatabase): UserLocalDao = db.userLocalDao()

    @Provides
    fun provideWeightHistoryDao(db: NutriflexDatabase): WeightHistoryDao = db.weightHistoryDao()
}
