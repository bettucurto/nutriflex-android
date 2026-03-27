package com.example.treino.di

import com.example.treino.data.repository.TreinoRepositoryImpl
import com.example.treino.domain.repository.TreinoRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TreinoModule {

    @Binds
    @Singleton
    abstract fun bindTreinoRepository(
        treinoRepositoryImpl: TreinoRepositoryImpl
    ): TreinoRepository
}
