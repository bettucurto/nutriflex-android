// app/src/main/java/com/example/nutriflex2/di/DietaRemoteModule.kt
package com.example.nutriflex2.di

import com.example.dieta.remote.DietaApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DietaRemoteModule {

    @Provides
    @Singleton
    fun provideDietaApiService(
        retrofit: Retrofit
    ): DietaApiService =
        retrofit.create(DietaApiService::class.java)
}
