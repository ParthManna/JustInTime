package com.example.todolistapp

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [TodoModel2::class], version = 2)
abstract class AppDatabase3 : RoomDatabase() {
    abstract fun todoDao3(): TodoDao3

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase3? = null
        private const val DB_NAME3 = "todo_database"

        // Define the migration from version 1 to version 2
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add the "isFinished" column to the TodoModel2 table with a default value of 0
                db.execSQL("ALTER TABLE TodoModel2 ADD COLUMN isFinished INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getDatabase(context: Context): AppDatabase3 {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase3::class.java,
                    DB_NAME3
                )
                    .addMigrations(MIGRATION_1_2) // Add the migration
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
