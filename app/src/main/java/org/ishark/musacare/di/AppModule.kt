package org.ishark.musacare.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import org.ishark.musacare.data.local.AppDao
import org.ishark.musacare.data.local.AppDatabase

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDb(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "musacare.db").build()

    @Provides
    fun provideDao(db: AppDatabase): AppDao = db.dao()
}
