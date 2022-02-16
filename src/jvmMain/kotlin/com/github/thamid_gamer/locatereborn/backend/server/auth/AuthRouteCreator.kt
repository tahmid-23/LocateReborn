package com.github.thamid_gamer.locatereborn.backend.server.auth

import io.ktor.server.routing.*

interface AuthRouteCreator {

    fun createRoute(routing: Routing)

}