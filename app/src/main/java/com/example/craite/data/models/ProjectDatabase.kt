package com.example.craite.data.models

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.craite.utils.ProjectTypeConverters

@Database(entities = [Project::class], version = 3)
@TypeConverters(ProjectTypeConverters::class)
abstract class ProjectDatabase: RoomDatabase() {
    abstract fun projectDao(): ProjectDao
}