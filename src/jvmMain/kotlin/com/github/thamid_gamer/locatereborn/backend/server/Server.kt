package com.github.thamid_gamer.locatereborn.backend.server

import com.github.thamid_gamer.locatereborn.backend.db.SQLDatabaseInserter
import com.github.thamid_gamer.locatereborn.backend.datagen.generator.ScraperDataGenerator
import com.github.thamid_gamer.locatereborn.backend.datagen.classifier.ManualCourseTypeClassifier
import com.github.thamid_gamer.locatereborn.backend.datagen.generator.GeneratorRequest
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
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
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

fun main() {
    val path = System.getenv("LOCATE_DB_PATH") ?: "."
    val db = Database.connect("jdbc:sqlite:$path/locatereborn.db")
    db.transactionManager.defaultIsolationLevel = TRANSACTION_SERIALIZABLE

    val apacheClient = HttpClients.createDefault()
    val apacheHttpTransport = ApacheHttpTransport(apacheClient)

    val tokenVerifier = GoogleIdTokenVerifier.Builder(apacheHttpTransport, GsonFactory.getDefaultInstance())
        .setAudience(listOf(clientId))
        .build()

    val authRouteCreator = GoogleAuthRouteCreator(tokenVerifier)

    embeddedServer(Netty, System.getenv("PORT")?.toIntOrNull() ?: 80, "127.0.0.1") {
        install(CallLogging)
        install(ContentNegotiation) {
            json()
        }
        install(Sessions) {
            cookie<LocateSession>("locate_session")
        }

        routing {
            // TODO: this is ugly
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
                call.respondHtml(HttpStatusCode.OK, HTML::index)
            }

            authRouteCreator.createRoute(this)
            route("/api") {
                studentListRoute(db)
                studentInfoRoute(db)
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

private fun downloadData(): Database {
    val path = System.getenv("LOCATE_DB_PATH") ?: "."
    val db = Database.connect("jdbc:sqlite:$path/locatereborn.db")
    db.transactionManager.defaultIsolationLevel = TRANSACTION_SERIALIZABLE

    val client = HttpClient {
        install(HttpCookies)
        install(HttpRequestRetry)
    }

    val dataGenerator = ScraperDataGenerator(client, ManualCourseTypeClassifier())
    val username = System.getenv("SCHOOLOGY_USERNAME")
        ?: throw IllegalArgumentException("schoology.username environment variable undefined")
    val password = System.getenv("SCHOOLOGY_PASSWORD")
        ?: throw IllegalArgumentException("schoology.password environment variable undefined")
    val generatorRequest = GeneratorRequest(username, password, listOf("2233228305", "2232950152"))
    val databaseInserter = SQLDatabaseInserter(db, dataGenerator, generatorRequest)

    databaseInserter.refreshData()

    return db
}