package com.bagaspardanailham.bpiktorplayground

import com.bagaspardanailham.bpiktorplayground.data.TaskTable
import com.bagaspardanailham.bpiktorplayground.data.UserTable
import com.bagaspardanailham.bpiktorplayground.routing.authRouting
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureRouting() {
    Database.connect("jdbc:sqlite:tasks.db", driver = "org.sqlite.JDBC")

    transaction {
        SchemaUtils.create(TaskTable, UserTable)
    }

    routing {
        authRouting()
        taskRouting()

        get("/") {
            call.respondText("Hello, World!")
        }
    }
}