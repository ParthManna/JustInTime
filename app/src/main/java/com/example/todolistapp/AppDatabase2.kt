package com.example.todolistapp

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [TodoModel::class], version = 1)
abstract class AppDatabase2 : RoomDatabase(){
    abstract fun todoDao2() : TodoDao


    companion object{
        @Volatile
        private var INSTANCE: AppDatabase2? = null

        fun getDatabase(context: Context): AppDatabase2{
            val tempInstance = INSTANCE
            if(tempInstance != null){
                return tempInstance
            }

            synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase2::class.java,
                    DB_NAME2
                ).build()
                INSTANCE = instance
                return instance
            }

        }
    }
}