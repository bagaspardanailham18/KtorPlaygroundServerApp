package com.bagaspardanailham.bpiktorplayground.data

import org.jetbrains.exposed.sql.Table

// Mendefinisikan struktur tabel "tasks" di database
object TaskTable : Table("tasks") {
    val id = integer("id").autoIncrement() // ID Otomatis diatur oleh database
    val title = text("title")
    val isCompleted = bool("is_completed")

    override val primaryKey = PrimaryKey(id)
}