package com.bagaspardanailham.bpiktorplayground.repository

import com.bagaspardanailham.bpiktorplayground.data.TaskTable
import com.bagaspardanailham.bpiktorplayground.model.Task
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class TaskRepositoryImpl: TaskRepository {
    override suspend fun getAllTasks(userId: Int): List<Task> = transaction {
        TaskTable.selectAll().where { TaskTable.userId eq userId }.map {
            Task(
                id = it[TaskTable.id],
                title = it[TaskTable.title],
                isCompleted = it[TaskTable.isCompleted],
                userId = it[TaskTable.userId]
            )
        }
    }

    override suspend fun getTaskById(
        id: Int,
        userId: Int
    ): Task? = transaction {
        TaskTable.selectAll()
            .where { (TaskTable.id eq id) and (TaskTable.userId eq userId) }
            .map {
                Task(
                    id = it[TaskTable.id],
                    title = it[TaskTable.title],
                    isCompleted = it[TaskTable.isCompleted],
                    userId = it[TaskTable.userId]
                )
            }
            .singleOrNull()
    }

    override suspend fun createTask(
        title: String,
        isCompleted: Boolean,
        userId: Int
    ): Task = transaction {
        val statement = TaskTable.insert {
            it[TaskTable.title] = title
            it[TaskTable.isCompleted] = isCompleted
            it[TaskTable.userId] = userId
        }
        Task(
            id = statement[TaskTable.id],
            title = title,
            isCompleted = isCompleted,
            userId = userId
        )
    }

    override suspend fun updateTask(
        id: Int,
        title: String,
        isCompleted: Boolean,
        userId: Int
    ): Boolean = transaction {
        val rowsUpdated = TaskTable.update({ (TaskTable.id eq id) and (TaskTable.userId eq userId) }) {
            it[TaskTable.title] = title
            it[TaskTable.isCompleted] = isCompleted
        }
        rowsUpdated > 0
    }

    override suspend fun deleteTask(id: Int, userId: Int): Boolean = transaction {
        val rowsDeleted = TaskTable.deleteWhere { (TaskTable.id eq id) and (TaskTable.userId eq userId) }
        rowsDeleted > 0
    }
}