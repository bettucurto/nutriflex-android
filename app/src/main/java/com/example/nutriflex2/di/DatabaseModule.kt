// app/src/main/java/com/example/nutriflex2/di/DatabaseModule.kt
package com.example.nutriflex2.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import data.local.dao.ReceitasFavoritasDao
import data.local.dao.RefeicoesDao
import dieta.local.DietaDatabase
import local.MIGRATION_1_2
import local.MIGRATION_2_3
import local.MIGRATION_3_4
import local.MIGRATION_4_5
import local.MIGRATION_5_6
import local.NutriflexDatabase
import local.UserLocalDao
import local.WeightHistoryDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    // ---------- CORE DB ----------
    @Provides
    @Singleton
    fun provideNutriflexDatabase(
        @ApplicationContext context: Context
    ): NutriflexDatabase =
        Room.databaseBuilder(
            context,
            NutriflexDatabase::class.java,
            "nutriflex_core.db"
        )
            .addMigrations(
                MIGRATION_1_2,
                MIGRATION_2_3,
                MIGRATION_3_4,
                MIGRATION_4_5,
                MIGRATION_5_6
            )
            .build()

    @Provides
    fun provideUserLocalDao(db: NutriflexDatabase): UserLocalDao = db.userLocalDao()

    @Provides
    fun provideWeightHistoryDao(db: NutriflexDatabase): WeightHistoryDao =
        db.weightHistoryDao()

    // ---------- DIETA DB ----------
    @Provides
    @Singleton
    fun provideDietaDatabase(
        @ApplicationContext context: Context
    ): DietaDatabase =
        Room.databaseBuilder(
            context,
            DietaDatabase::class.java,
            "nutriflex_dieta.db"
        )
            .addMigrations(
                DietaDatabase.MIGRATION_1_2,
                DietaDatabase.MIGRATION_2_3,
                DietaDatabase.MIGRATION_3_4,
                DietaDatabase.MIGRATION_4_5
            )
            .build()

    @Provides
    fun provideRefeicoesDao(db: DietaDatabase): RefeicoesDao = db.refeicoesDao()

    @Provides
    fun provideReceitasFavoritasDao(db: DietaDatabase): ReceitasFavoritasDao =
        db.receitasFavoritasDao()
}
