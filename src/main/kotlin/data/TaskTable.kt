package com.bagaspardanailham.bpiktorplayground.data

import org.jetbrains.exposed.sql.Table

// Mendefinisikan struktur tabel "tasks" di database
object TaskTable : Table("tasks") {
    val id = integer("id").autoIncrement() // ID Otomatis diatur oleh database
    val title = text("title")
    val isCompleted = bool("is_completed")

    val userId = integer("user_id").references(UserTable.id)

    override val primaryKey = PrimaryKey(id)
}