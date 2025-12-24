package di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import local.NutriflexDatabase
import local.UserLocalDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): NutriflexDatabase =
        Room.databaseBuilder(
            context,
            NutriflexDatabase::class.java,
            "nutriflex.db"
        ).build()

    @Provides
    fun provideUserLocalDao(db: NutriflexDatabase): UserLocalDao = db.userLocalDao()
}
