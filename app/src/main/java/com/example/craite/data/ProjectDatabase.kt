package com.example.craite.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.craite.utils.Converters

@Database(entities = [Project::class], version = 1)
@TypeConverters(Converters::class)
abstract class ProjectDatabase: RoomDatabase() {
    abstract fun projectDao(): ProjectDao

    companion object {
        @Volatile
        private var INSTANCE: ProjectDatabase? = null

        fun getInstance(context: android.content.Context?): ProjectDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: android.content.Context?): ProjectDatabase {
            return Room.databaseBuilder(
                context!!.applicationContext,
                ProjectDatabase::class.java,
                "project_database"
            ).build()
        }
    }
}