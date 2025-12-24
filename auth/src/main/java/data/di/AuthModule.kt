package data.di

import android.util.Log
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import data.remote.AuthApiService
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApiService {
        Log.d("DI", "Retrofit recebido em AuthModule: $retrofit")
        return retrofit.create(AuthApiService::class.java)
    }
}
