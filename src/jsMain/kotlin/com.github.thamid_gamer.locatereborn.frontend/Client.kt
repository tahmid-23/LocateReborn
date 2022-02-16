package com.github.thamid_gamer.locatereborn.frontend

import com.github.thamid_gamer.locatereborn.frontend.helmet.helmetProvider
import com.github.thamid_gamer.locatereborn.frontend.view.courseStudentsDirectoryRoute
import com.github.thamid_gamer.locatereborn.frontend.view.loginRoute
import com.github.thamid_gamer.locatereborn.frontend.view.scheduleRoute
import com.github.thamid_gamer.locatereborn.frontend.view.studentDirectoryRoute
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import react.*
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.p
import react.dom.render
import react.router.Route
import react.router.Routes
import react.router.dom.BrowserRouter

// TODO: React Redux to store state when we go back?
fun main() {
    val client = HttpClient {
        install(ContentNegotiation) {
            json()
        }
    }
    val scope = CoroutineScope(Dispatchers.Default)

    window.onload = {
        val rootElement = document.getElementById("root") ?: throw IllegalStateException("No root element!")

        render(FC<Props> {
            helmetProvider {
                BrowserRouter {
                    Routes {
                        loginRoute()
                        scheduleRoute(client, scope)
                        studentDirectoryRoute(client, scope)
                        courseStudentsDirectoryRoute(client, scope)
                    }
                }
            }
        }.create(), rootElement)
    }
}
