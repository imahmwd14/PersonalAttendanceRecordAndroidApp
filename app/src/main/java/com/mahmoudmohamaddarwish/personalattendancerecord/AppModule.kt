package com.mahmoudmohamaddarwish.personalattendancerecord

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDB(@ApplicationContext context: Context): DB =
        Room
            .databaseBuilder(
                context,
                DB::class.java,
                "records_db"
            )
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideDao(db: DB) = db.getRecordDao()
}