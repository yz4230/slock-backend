package dev.ishiyama

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject


fun Application.configureRouting() {
    val service by inject<PetService>()

    routing {
        get("/") {
            call.respondText("Hello World!")
        }
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
