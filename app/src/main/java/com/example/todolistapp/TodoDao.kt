package com.example.todolistapp

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TodoDao {

    @Insert
    suspend fun insetTask(todoModel: TodoModel): Long

//    @Insert
//    suspend fun insetTask2(todoModel: TodoModel2): Long


    @Query("Select * from TodoModel where isFinished == -1")
    fun getTask(): LiveData<List<TodoModel>>

    @Query("SELECT * FROM TodoModel WHERE isFinished = 1")
    fun getCompletedTasks(): LiveData<List<TodoModel>>

    @Query("SELECT * FROM TodoModel WHERE id = :uid")
    suspend fun getTaskById(uid: Long): TodoModel?

//    @Query("SELECT * FROM TodoModel2 WHERE id = :uid")
//    suspend fun getTaskById2(uid: Long): TodoModel2?


    @Query("Update TodoModel set isFinished = 1 where id=:uid")
    suspend fun finishTask(uid: Long)

    @Query("Delete from TodoModel where id=:uid")
    suspend fun deleteTask(uid: Long)

    @Query("DELETE FROM TodoModel") // Replace 'tasks_table' with your actual table name
    suspend fun clearAllTasks()


    @Query("SELECT * FROM TodoModel")
    suspend fun getTasksList(): List<TodoModel>

}