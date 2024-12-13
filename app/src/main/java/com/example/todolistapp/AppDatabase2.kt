package com.example.todolistapp

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import android.util.Log


@Database(entities = [TodoModel::class], version = 2) // Increment version for schema changes
abstract class AppDatabase2 : RoomDatabase() {
    abstract fun todoDao2(): TodoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase2? = null

        fun getDatabase(context: Context): AppDatabase2 {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }

            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase2::class.java,
                    DB_NAME2
                )
                    .fallbackToDestructiveMigration() // For development; remove in production
                    // Add logging callback
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            Log.d("AppDatabase", "Database created")
                        }

                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            Log.d("AppDatabase", "Database opened")
                        }
                    })
                    .build()

                INSTANCE = instance
                return instance
            }
        }
    }
}
