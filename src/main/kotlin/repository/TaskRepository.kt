package com.bagaspardanailham.bpiktorplayground.repository

import com.bagaspardanailham.bpiktorplayground.model.Task

interface TaskRepository {
    suspend fun getAllTasks(userId: Int): List<Task>
    suspend fun getTaskById(id: Int, userId: Int): Task?
    suspend fun createTask(title: String, isCompleted: Boolean, userId: Int): Task
    suspend fun updateTask(id: Int, title: String, isCompleted: Boolean, userId: Int): Boolean
    suspend fun deleteTask(id: Int, userId: Int): Boolean
}