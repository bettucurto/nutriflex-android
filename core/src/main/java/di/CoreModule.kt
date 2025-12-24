// core/src/main/java/core/di/CoreModule.kt
package di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import datastore.TokenManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoreModule {

    @Provides
    @Singleton
    fun provideTokenManager(
        @ApplicationContext context: Context
    ): TokenManager = TokenManager(context)
}
