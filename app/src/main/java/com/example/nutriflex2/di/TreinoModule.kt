package com.example.nutriflex2.di

import android.content.Context
import androidx.room.Room
import com.example.treino.data.local.TreinoDatabase
import com.example.treino.data.local.dao.TreinoDao
import com.example.treino.data.remote.TreinoApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TreinoModule {

    // --- API ---
    @Provides
    @Singleton
    fun provideTreinoApiService(
        retrofit: Retrofit
    ): TreinoApiService =
        retrofit.create(TreinoApiService::class.java)

    // --- Database ---
    @Provides
    @Singleton
    fun provideTreinoDatabase(
        @ApplicationContext context: Context
    ): TreinoDatabase =
        Room.databaseBuilder(
            context,
            TreinoDatabase::class.java,
            "nutriflex_treino.db"
        )
        .addMigrations(
            TreinoDatabase.MIGRATION_1_2,
            TreinoDatabase.MIGRATION_2_3
        )
        .fallbackToDestructiveMigration()
        .build()

    @Provides
    fun provideTreinoDao(db: TreinoDatabase): TreinoDao = db.treinoDao()
}
