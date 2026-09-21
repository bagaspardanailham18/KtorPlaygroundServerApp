package com.bagaspardanailham.bpiktorplayground.routing

import com.bagaspardanailham.bpiktorplayground.DataNotFoundException
import com.bagaspardanailham.bpiktorplayground.ValidationException
import com.bagaspardanailham.bpiktorplayground.data.UserTable
import com.bagaspardanailham.bpiktorplayground.model.Task
import com.bagaspardanailham.bpiktorplayground.repository.TaskRepositoryImpl
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
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.taskRouting() {
    val repository = TaskRepositoryImpl()

    // Semua endpoint di dalam blok ini otomatis diawali dengan /tasks
    authenticate("auth-jwt") {
        route("/tasks") {
            get {
                val principal = call.principal<JWTPrincipal>()
                val loggedInUserId = getUserIdFromPrincipal(principal)
//                val username = principal?.payload?.getClaim("username")?.asString()

                if (loggedInUserId == null) {
                    call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "User tidak valid"))
                    return@get
                }

                val tasks = repository.getAllTasks(loggedInUserId)
                call.respond(tasks)
            }
            get("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                val loggedInUserId = getUserIdFromPrincipal(call.principal<JWTPrincipal>())

                if (id == null || loggedInUserId == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Permintaan tidak valid"))
                    return@get
                }

                val task = repository.getTaskById(id, loggedInUserId)
                    ?: throw DataNotFoundException("Tugas tidak ditemukan")

                call.respond(task)
            }
            post {
                val loggedInUserId = getUserIdFromPrincipal(call.principal<JWTPrincipal>()) ?: throw ValidationException("Sesi tidak valid")

                val inputTask = call.receive<Task>()
                if (inputTask.title.isBlank()) throw ValidationException("Judul tidak boleh kosong")

                val newTask = repository.createTask(inputTask.title, inputTask.isCompleted, loggedInUserId)
                call.respond(HttpStatusCode.Created, newTask)
            }
            put("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull() ?: throw ValidationException("ID tidak valid")
                val loggedInUserId = getUserIdFromPrincipal(call.principal<JWTPrincipal>()) ?: throw ValidationException("Sesi tidak valid")

                val inputTask = call.receive<Task>()
                val isSuccess = repository.updateTask(id, inputTask.title, inputTask.isCompleted, loggedInUserId)
                if (!isSuccess) throw DataNotFoundException("Gagal mengupdate, data tidak ditemukan")
                call.respond(HttpStatusCode.OK, inputTask.copy(id = id, userId = loggedInUserId))
            }
            delete("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull() ?: throw ValidationException("ID tidak valid")
                val loggedInUserId = getUserIdFromPrincipal(call.principal<JWTPrincipal>()) ?: throw ValidationException("Sesi tidak valid")

                val isSuccess = repository.deleteTask(id, loggedInUserId)

                if (!isSuccess) throw DataNotFoundException("Gagal menghapus, data tidak ditemukan")

                call.respond(HttpStatusCode.OK, mapOf("message" to "Tugas berhasil dihapus"))
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