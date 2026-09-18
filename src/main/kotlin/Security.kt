package com.bagaspardanailham.bpiktorplayground

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.response.respond

fun Application.configureSecurity() {

    val jwtAudience = "ktor-audience"
    val jwtDomain = "https://jwt-provider-domain/"
    val jwtSecret = "kunci-rahasia-super-aman-playground-123"

    install(Authentication) {
        jwt("auth-jwt") {
            realm = "Access to 'tasks'"
            verifier(
                JWT.require(Algorithm.HMAC256(jwtSecret))
                    .withAudience(jwtAudience)
                    .withIssuer(jwtDomain)
//                    .acceptLeeway(0) // TAMBAHKAN INI: Set toleransi waktu ke 0 detik agar instan mati
                    .build()
            )
            validate { credential ->
                if (credential.payload.getClaim("username").asString() != "") {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
            challenge { defaultScheme, realm ->
                call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Token tidak valid atau kedaluwarsa"))
            }
        }
    }
}