package com.bagaspardanailham.bpiktorplayground.routing

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.bagaspardanailham.bpiktorplayground.data.UserRequest
import com.bagaspardanailham.bpiktorplayground.data.UserTable
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import java.util.*

fun Route.authRouting() {

    val jwtSecret = "kunci-rahasia-super-aman-playground-123"
    val jwtDomain = "https://jwt-provider-domain/"
    val jwtAudience = "ktor-audience"

    // 1. ENDPOINT REGISTRASI
    post("/register") {
        try {
            val userReq = call.receive<UserRequest>()

            // Enkripsi password menggunakan BCrypt sebelum disimpan
            val hashedPassword = BCrypt.hashpw(userReq.password, BCrypt.gensalt())

            transaction {
                UserTable.insert {
                    it[username] = userReq.username
                    it[password] = hashedPassword
                }
            }
            call.respond(HttpStatusCode.Created, mapOf("message" to "Registrasi pengguna sukses!"))
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Username sudah terdaftar atau format salah"))
        }
    }

    // 2. ENDPOINT LOGIN (Menghasilkan Token JWT)
    post("/login") {
        val userReq = call.receive<UserRequest>()

        // Cari pengguna berdasarkan username
        val userDb = transaction {
            UserTable.selectAll().where { UserTable.username eq userReq.username }
                .map { it[UserTable.username] to it[UserTable.password] }
                .singleOrNull()
        }

        if (userDb == null || !BCrypt.checkpw(userReq.password, userDb.second)) {
            call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Username atau password salah"))
            return@post
        }

        // Jika password cocok, buat token JWT (berlaku selama 24 jam)
        val token = JWT.create()
            .withAudience(jwtAudience)
            .withIssuer(jwtDomain)
            .withClaim("username", userDb.first)
//            .withExpiresAt(Date(System.currentTimeMillis() + 86400000)) // 24 Jam
            .withExpiresAt(Date(System.currentTimeMillis() + 300000)) // 300 Detik / 5 Menit
            .sign(Algorithm.HMAC256(jwtSecret))

        call.respond(HttpStatusCode.OK, mapOf("token" to token))
    }
}
