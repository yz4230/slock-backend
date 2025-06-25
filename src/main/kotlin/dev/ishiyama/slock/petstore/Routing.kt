package dev.ishiyama.slock.petstore

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.koin.ktor.ext.inject

val petStoreModule =
    module {
        singleOf(::DatabasePetRepository) { bind<PetRepository>() }
        singleOf(::PetService)
    }

fun Application.configurePetStoreRouting() {
    val service by inject<PetService>()

    routing {
        get("/pets/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respondText("Invalid pet ID", status = HttpStatusCode.BadRequest)
                return@get
            }
            val pet = service.getPet(id)
            if (pet != null) {
                call.respond(pet)
            } else {
                call.respondText("Pet not found", status = HttpStatusCode.NotFound)
            }
        }
        get("/pets") {
            println("Received a request to list pets")
            val pets = service.listPets()
            call.respond(pets)
        }
        post("/pets") {
            println("Received a request to add a pet")
            val pet = call.receive<Pet>()
            val addedPet = service.addPet(pet)
            call.respond(addedPet)
        }
    }
}
