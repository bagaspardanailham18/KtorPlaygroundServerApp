package com.bagaspardanailham.bpiktorplayground

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond

fun Application.configureStatusPages() {
    install(StatusPages) {

        // 1. Tangkap jika ada validasi input yang gagal (Status 400 Bad Request)
        exception<ValidationException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to cause.message))
        }

        // 2. Tangkap jika data tidak ditemukan di database (Status 404 Not Found)
        exception<DataNotFoundException> { call, cause ->
            call.respond(HttpStatusCode.NotFound, mapOf("error" to cause.message))
        }

        // 3. Tangkap semua eror tidak terduga lainnya (Status 500 Internal Server Error)
        // Menjaga agar server tidak crash total dan tidak membocorkan detail query internal database
        exception<Throwable> { call, cause ->
            cause.printStackTrace() // Tetap cetak log asli di terminal IntelliJ untuk debugging Anda
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Terjadi kesalahan internal pada server"))
        }
    }
}