package com.bagaspardanailham.bpiktorplayground.data

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table

object UserTable : Table("users") {
    val id = integer("id").autoIncrement()
    val username = varchar("username", 50).uniqueIndex()
    val password = text("password")

    override val primaryKey = PrimaryKey(id)
}

@Serializable
data class UserRequest(val username: String, val password: String)