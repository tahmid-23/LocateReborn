package com.github.thamid_gamer.locatereborn.backend.server.auth

import com.github.thamid_gamer.locatereborn.backend.server.session.LocateSession
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*

class GoogleAuthRouteCreator(private val tokenVerifier: GoogleIdTokenVerifier) : AuthRouteCreator {

    private val allowedEmails = listOf("tigritik@gmail.com")

    override fun createRoute(routing: Routing) {
        routing.post("/google-login") {
            val session = call.sessions.get<LocateSession>()

            if (session != null) {
                call.response.header(HttpHeaders.Location, "/students")
                call.respond(HttpStatusCode.SeeOther)
                return@post
            }

            val cookieToken = call.request.cookies["g_csrf_token"]
            if (cookieToken == null) {
                call.response.header(HttpHeaders.Location, "/?loginFailed")
                call.respond(HttpStatusCode.SeeOther)
                return@post
            }

            val parameters = call.receiveParameters()
            
            val parameterToken = parameters["g_csrf_token"]
            if (parameterToken == null) {
                call.response.header(HttpHeaders.Location, "/?loginFailed")
                call.respond(HttpStatusCode.SeeOther)
                return@post
            }

            if (cookieToken != parameterToken) {
                call.response.header(HttpHeaders.Location, "/?loginFailed")
                call.respond(HttpStatusCode.SeeOther)
                return@post
            }

            val idToken = parameters["credential"]
            if (idToken == null) {
                call.response.header(HttpHeaders.Location, "/?loginFailed")
                call.respond(HttpStatusCode.SeeOther)
                return@post
            }

            val verifiedToken = tokenVerifier.verify(idToken)
            if (verifiedToken == null) {
                call.response.header(HttpHeaders.Location, "/?loginFailed")
                call.respond(HttpStatusCode.SeeOther)
                return@post
            }

            if (!verifyAccessAllowed(verifiedToken.payload)) {
                call.response.header(HttpHeaders.Location, "/?loginFailed")
                call.respond(HttpStatusCode.SeeOther)
                return@post
            }

            call.sessions.set(LocateSession(verifiedToken.payload["name"] as? String ?: "Unknown"))

            call.response.header(HttpHeaders.Location, "/students")
            call.respond(HttpStatusCode.SeeOther)
        }
    }

    private fun verifyAccessAllowed(payload: GoogleIdToken.Payload): Boolean {
        return payload.hostedDomain == "bergen.org" || payload.email in allowedEmails
    }

}