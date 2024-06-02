package com.example.craite

import android.app.Application
import androidx.room.Room
import com.example.craite.data.ProjectDatabase
import com.example.craite.data.ProjectRepository


class MyApplication : Application() {
    val database: ProjectDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            ProjectDatabase::class.java,
            "project_database"
        ).build()
    }

    val projectRepository: ProjectRepository by lazy {
        ProjectRepository(database)
    }

    override fun onCreate() {
        super.onCreate()
        // Initialize any global resources or components here
    }
}