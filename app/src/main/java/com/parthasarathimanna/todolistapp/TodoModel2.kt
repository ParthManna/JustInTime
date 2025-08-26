package com.parthasarathimanna.todolistapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TodoModel2 (
    var soundUri: String? = null, // Corrected field with a nullable String type
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
)
