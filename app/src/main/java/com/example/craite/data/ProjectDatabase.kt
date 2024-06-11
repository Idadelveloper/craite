package com.example.craite.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.craite.utils.ProjectTypeConverters

@Database(entities = [Project::class], version = 2)
@TypeConverters(ProjectTypeConverters::class)
abstract class ProjectDatabase: RoomDatabase() {
    abstract fun projectDao(): ProjectDao
}