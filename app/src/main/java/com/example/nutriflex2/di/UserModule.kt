package com.example.nutriflex2.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import datastore.TokenManager
import remote.UserApiService
import remote.UserRepository
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UserModule {

    @Provides
    @Singleton
    fun provideUserApi(retrofit: Retrofit): UserApiService {
        return retrofit.create(UserApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        api: UserApiService,
        tokenManager: TokenManager
    ): UserRepository {
        return UserRepository(api, tokenManager)
    }
}