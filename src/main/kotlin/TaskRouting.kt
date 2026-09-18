package com.bagaspardanailham.bpiktorplayground

import com.bagaspardanailham.bpiktorplayground.data.TaskTable
import com.bagaspardanailham.bpiktorplayground.data.UserTable
import com.bagaspardanailham.bpiktorplayground.model.Task
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

fun Route.taskRouting() {
    // Semua endpoint di dalam blok ini otomatis diawali dengan /tasks
    authenticate("auth-jwt") {
        route("/tasks") {
            get {
                val principal = call.principal<JWTPrincipal>()
                val loggedInUserId = getUserIdFromPrincipal(principal)
                val username = principal?.payload?.getClaim("username")?.asString()

                if (loggedInUserId == null) {
                    call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "User tidak valid"))
                    return@get
                }

                val tasks = transaction {
                    TaskTable.selectAll().where { TaskTable.userId eq loggedInUserId }.map {
                        Task(
                            id = it[TaskTable.id],
                            title = it[TaskTable.title],
                            isCompleted = it[TaskTable.isCompleted],
                            userId = it[TaskTable.userId]
                        )
                    }
                }
                call.respond(tasks)
            }
            get("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                val loggedInUserId = getUserIdFromPrincipal(call.principal<JWTPrincipal>())

                if (id == null || loggedInUserId == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Permintaan tidak valid"))
                    return@get
                }

                // Mencari data di database berdasarkan ID
                val task = transaction {
                    TaskTable.selectAll()
                        .where { (TaskTable.id eq id) and (TaskTable.userId eq loggedInUserId) }
                        .map {
                            Task(
                                id = it[TaskTable.id],
                                title = it[TaskTable.title],
                                isCompleted = it[TaskTable.isCompleted],
                                userId = it[TaskTable.userId]
                            )
                        }
                        .singleOrNull() // Mengambil satu data saja atau null jika tidak ada
                }

                if (task != null) {
                    call.respond(task)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "Tugas tidak ditemukan atau bukan milik Anda"))
                }
            }
            post {
                val loggedInUserId = getUserIdFromPrincipal(call.principal<JWTPrincipal>())
                if (loggedInUserId == null) {
                    call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Sesi tidak valid"))
                    return@post
                }

                try {
                    val inputTask = call.receive<Task>()
                    val insertedTask = transaction {
                        val statement = TaskTable.insert {
                            it[title] = inputTask.title
                            it[isCompleted] = inputTask.isCompleted
                            it[userId] = loggedInUserId
                        }
                        Task(
                            id = statement[TaskTable.id],
                            title = inputTask.title,
                            isCompleted = inputTask.isCompleted,
                            userId = loggedInUserId
                        )
                    }
                    call.respond(HttpStatusCode.Created, insertedTask)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Format JSON salah"))
                }
            }
            put("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                val loggedInUserId = getUserIdFromPrincipal(call.principal<JWTPrincipal>())

                if (id == null || loggedInUserId == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Permintaan tidak valid"))
                    return@put
                }

                try {
                    val updatedTaskInput = call.receive<Task>()

                    // Melakukan update di database dan mengembalikan jumlah baris yang terpengaruh
                    val rowsUpdated = transaction {
                        TaskTable.update({ (TaskTable.id eq id) and (TaskTable.userId eq loggedInUserId) }) {
                            it[title] = updatedTaskInput.title
                            it[isCompleted] = updatedTaskInput.isCompleted
                        }
                    }

                    if (rowsUpdated > 0) {
                        call.respond(HttpStatusCode.OK, updatedTaskInput.copy(id = id, userId = loggedInUserId))
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf("message" to "Tugas tidak ditemukan atau bukan milik Anda"))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Format JSON salah"))
                }
            }
            delete("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                val loggedInUserId = getUserIdFromPrincipal(call.principal<JWTPrincipal>())

                if (id == null || loggedInUserId == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Permintaan tidak valid"))
                    return@delete
                }

                val rowsDeleted = transaction {
                    TaskTable.deleteWhere { (TaskTable.id eq id) and (TaskTable.userId eq loggedInUserId) }
                }

                if (rowsDeleted > 0) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Tugas berhasil dihapus"))
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "Tugas tidak ditemukan atau bukan milik Anda"))
                }
            }
        }
    }
}

fun getUserIdFromPrincipal(principal: JWTPrincipal?): Int? {
    val username = principal?.payload?.getClaim("username")?.asString() ?: return null
    return transaction {
        UserTable.selectAll().where { UserTable.username eq username }
            .map { it[UserTable.id] }
            .singleOrNull()
    }
}