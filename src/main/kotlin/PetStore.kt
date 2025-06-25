package dev.ishiyama

import kotlinx.serialization.Serializable

@Serializable
data class Pet(
    val id: Int,
    val name: String,
    val type: String,
    val age: Int,
)

interface PetRepository {
    fun listPets(): List<Pet> = emptyList()

    fun getPetById(id: Int): Pet?

    fun addPet(pet: Pet): Pet
}

class InMemoryPetRepository : PetRepository {
    private val pets = mutableListOf<Pet>()

    override fun listPets(): List<Pet> = pets

    override fun getPetById(id: Int): Pet? = pets.find { it.id == id }

    override fun addPet(pet: Pet): Pet {
        pets.add(pet)
        return pet
    }
}

class PetService(
    private val repository: PetRepository,
) {
    fun listPets(): List<Pet> = repository.listPets()

    fun getPet(id: Int): Pet? = repository.getPetById(id)

    fun addPet(pet: Pet): Pet = repository.addPet(pet)
}
