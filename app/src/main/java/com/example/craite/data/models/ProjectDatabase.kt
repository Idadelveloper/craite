package com.example.craite.data.models

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.craite.utils.ProjectTypeConverters

//@Database(entities = [Project::class], version = 4,
//    autoMigrations = [AutoMigration(from = 3, to = 4)])


@Database(entities = [Project::class], version = 4)
@TypeConverters(ProjectTypeConverters::class)
abstract class ProjectDatabase: RoomDatabase() {
    abstract fun projectDao(): ProjectDao
}