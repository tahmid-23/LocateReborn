package com.github.thamid_gamer.locatereborn.backend.server.auth

import com.github.thamid_gamer.locatereborn.backend.db.tables.User
import com.github.thamid_gamer.locatereborn.backend.server.session.LocateSession
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class GoogleAuthRouteCreator(
    private val db: Database,
    private val tokenVerifier: GoogleIdTokenVerifier
) : AuthRouteCreator {

    private val allowedEmails = listOf("tigritik@gmail.com")

    override fun createRoute(routing: Routing) {
        routing.post("/google-auth") {
            val session = call.sessions.get<LocateSession>()

            if (session != null) {
                call.response.header(HttpHeaders.Location, "/students")
                call.respond(HttpStatusCode.SeeOther)
                return@post
            }

            val parameters = call.receiveParameters()
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

            val username = verifiedToken.payload["name"] as? String
            val email = verifiedToken.payload.email
            call.sessions.set(LocateSession(username ?: "Unknown"))

            call.response.header(HttpHeaders.Location, "/students")
            call.respond(HttpStatusCode.SeeOther)

            if (username != null) {
                newSuspendedTransaction(Dispatchers.IO, db) {
                    SchemaUtils.create(User)

                    val present = User.select(User.username eq username).toList().isNotEmpty()
                    if (present) {
                        // NO-OP: just for future if Users becomes a larger table
                        /*
                        Users.update({ Users.username eq username }) {

                        }
                         */
                    }
                    else {
                        User.insert {
                            it[User.username] = username
                            it[User.email] = email
                        }
                    }
                }
            }
        }
    }

    private fun verifyAccessAllowed(payload: GoogleIdToken.Payload): Boolean {
        return payload.hostedDomain == "bergen.org" || payload.email in allowedEmails
    }

}