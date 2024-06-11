package com.example.craite.di

import com.example.craite.data.ProjectDao
import com.example.craite.data.ProjectDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object ProjectModule {
    // ... other database-related providers ...

    @Provides
    fun provideProjectDao(database: ProjectDatabase): ProjectDao {
        return database.projectDao()
    }
}