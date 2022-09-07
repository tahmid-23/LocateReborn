package com.github.thamid_gamer.locatereborn.frontend

import com.github.thamid_gamer.locatereborn.frontend.helmet.helmetProvider
import com.github.thamid_gamer.locatereborn.frontend.view.*
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import react.*
import react.dom.client.createRoot
import react.router.Routes
import react.router.dom.BrowserRouter

fun main() {
    val client = HttpClient {
        install(ContentNegotiation) {
            json()
        }
    }
    val scope = CoroutineScope(Dispatchers.Default)

    window.onload = {
        val rootElement = document.getElementById("root") ?: throw IllegalStateException("No root element!")

        createRoot(rootElement).render(FC<Props> {
            helmetProvider {
                BrowserRouter {
                    Routes {
                        loginRoute()
                        scheduleRoute(client, scope)
                        studentDirectoryRoute(client, scope)
                        courseDirectoryRoute(client, scope)
                        courseStudentsDirectoryRoute(client, scope)
                    }
                }
            }
        }.create())
    }
}
