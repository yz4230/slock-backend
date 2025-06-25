package dev.ishiyama.slock

import dev.ishiyama.slock.petstore.configurePetStoreRouting
import io.ktor.server.application.Application
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun Application.configureRouting() {
    configurePetStoreRouting()

    routing {
        get("/") {
            call.respondText("Hello, World!")
        }
    }
}
