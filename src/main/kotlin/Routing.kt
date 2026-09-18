package com.bagaspardanailham.bpiktorplayground

import com.bagaspardanailham.bpiktorplayground.model.Task
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.request.receive
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    val taskList = mutableListOf(
        Task(1, "Belajar Ktor dasar", true),
        Task(2, "Membuat REST API sederhana", false)
    )


    routing {
        get("/") {
            call.respondText("Hello, World!")
        }
        get("/tasks") {
            call.respond(taskList)
        }
        get("/tasks/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            val task = taskList.find { it.id == id }

            if (task != null) {
                call.respond(task)
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("message" to "Tugas tidak ditemukan"))
            }
        }
        post("/tasks") {
            try {
                val newTask = call.receive<Task>()
                taskList.add(newTask)
                call.respond(HttpStatusCode.Created, newTask)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Format JSON salah"))
            }
        }
        put("/tasks/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            val taskIndex = taskList.indexOfFirst { it.id == id }

            if (taskIndex != -1) {
                val updatedTask = call.receive<Task>()
                taskList[taskIndex] = updatedTask
                call.respond(HttpStatusCode.OK, updatedTask)
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("message" to "Tugas tidak ditemukan"))
            }
        }
        delete("/tasks/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            val removed = taskList.removeIf { it.id == id }

            if (removed) {
                call.respond(HttpStatusCode.OK, mapOf("message" to "Tugas berhasil dihapus"))
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("message" to "Tugas tidak ditemukan"))
            }
        }
        get("/json/kotlinx-serialization") {
            call.respond(mapOf("hello" to "world"))
        }
    }
}