package com.github.thamid_gamer.locatereborn.backend.server

import com.github.thamid_gamer.locatereborn.backend.datagen.classifier.HistoryLunchStudentClassifier
import com.github.thamid_gamer.locatereborn.backend.db.SQLDatabaseInserter
import com.github.thamid_gamer.locatereborn.backend.datagen.generator.ScraperDataGenerator
import com.github.thamid_gamer.locatereborn.backend.datagen.classifier.ManualCourseTypeClassifier
import com.github.thamid_gamer.locatereborn.backend.datagen.generator.GeneratorRequest
import com.github.thamid_gamer.locatereborn.backend.db.DatabaseInserter
import com.github.thamid_gamer.locatereborn.backend.server.auth.GoogleAuthRouteCreator
import com.github.thamid_gamer.locatereborn.backend.server.routes.*
import com.github.thamid_gamer.locatereborn.backend.server.session.LocateSession
import com.github.thamid_gamer.locatereborn.shared.google.clientId
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.apache.v2.ApacheHttpTransport
import com.google.api.client.json.gson.GsonFactory
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.cookies.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.coroutines.runBlocking
import kotlinx.html.*
import org.apache.http.impl.client.HttpClients
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transactionManager
import java.sql.Connection.TRANSACTION_SERIALIZABLE

fun HTML.index() {
    head {
        link(rel = "icon", type = "image/png", href = "/static/image/monocle-cat.png")
        link(rel = "stylesheet", type = "text/css", href = "/static/css/common-styles.css")
        script(src = "/static/LocateReborn.js") {}
        script(src = "https://accounts.google.com/gsi/client") {
            async = true
            defer = true
        }
    }
    body {
        div {
            id = "root"
        }
    }
}

fun main(args: Array<String>) {
    val path = System.getenv("LOCATE_DB_PATH") ?: "."
    val db = Database.connect("jdbc:sqlite:$path/locatereborn.db")
    db.transactionManager.defaultIsolationLevel = TRANSACTION_SERIALIZABLE

    if ("downloadData" in args) {
        runBlocking {
            downloadData(SQLDatabaseInserter(db))
        }
    }

    val apacheClient = HttpClients.createDefault()
    val apacheHttpTransport = ApacheHttpTransport(apacheClient)

    val tokenVerifier = GoogleIdTokenVerifier.Builder(apacheHttpTransport, GsonFactory.getDefaultInstance())
        .setAudience(listOf(clientId))
        .build()

    val authRouteCreator = GoogleAuthRouteCreator(db, tokenVerifier)

    val port = System.getenv("PORT")?.toIntOrNull() ?: 80
    embeddedServer(Netty, port, "127.0.0.1") {
        install(CallLogging)
        install(ContentNegotiation) {
            json()
        }
        /* TODO: figure out how to upgrade http
        install(HttpsRedirect) {
            sslPort = port
        }
         */
        install(Sessions) {
            cookie<LocateSession>("locate_session")
        }

        routing {
            get("/") {
                val session = call.sessions.get<LocateSession>()
                if (session != null) {
                    call.response.header(HttpHeaders.Location, "/students")
                    call.respond(HttpStatusCode.SeeOther)
                    return@get
                }

                call.respondHtml(HttpStatusCode.OK, HTML::index)
            }
            get("*") {
                call.respondHtml(HttpStatusCode.NotFound, HTML::index)
            }

            authRouteCreator.createRoute(this)
            route("/api") {
                studentListRoute(db)
                studentCoursesRoute(db)
                courseListRoute(db)
                courseStudentsRoute(db)
            }
            static("/static") {
                resources()
            }
        }
    }.start(true)
}

private suspend fun downloadData(databaseInserter: DatabaseInserter) {
    val client = HttpClient {
        install(HttpCookies)
        install(HttpRequestRetry)
    }

    val dataGenerator = ScraperDataGenerator(client, HistoryLunchStudentClassifier(), ManualCourseTypeClassifier())
    val username = System.getenv("SCHOOLOGY_USERNAME")
        ?: throw IllegalArgumentException("SCHOOLOGY_USERNAME environment variable undefined")
    val password = System.getenv("SCHOOLOGY_PASSWORD")
        ?: throw IllegalArgumentException("SCHOOLOGY_PASSWORD environment variable undefined")
    val generatorRequest = GeneratorRequest(username, password, listOf("2233228305", "2232950152"))
    databaseInserter.updateData(dataGenerator.generateData(generatorRequest))
}