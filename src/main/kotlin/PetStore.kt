package dev.ishiyama

import dev.ishiyama.tables.Pets
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

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

    fun <T> tx(block: PetRepository.() -> T): T = transaction { block() }
}

class DatabasePetRepository : PetRepository {
    override fun listPets(): List<Pet> =
        Pets.selectAll().map {
            Pet(
                id = it[Pets.id],
                name = it[Pets.name],
                type = it[Pets.type],
                age = it[Pets.age],
            )
        }

    override fun getPetById(id: Int): Pet? =
        Pets
            .selectAll()
            .where { Pets.id eq id }
            .map {
                Pet(
                    id = it[Pets.id],
                    name = it[Pets.name],
                    type = it[Pets.type],
                    age = it[Pets.age],
                )
            }.singleOrNull()

    override fun addPet(pet: Pet): Pet {
        val id =
            Pets.insert {
                it[name] = pet.name
                it[type] = pet.type
                it[age] = pet.age
            } get Pets.id
        return pet.copy(id = id)
    }
}

class PetService(
    private val repository: PetRepository,
) {
    fun listPets(): List<Pet> = repository.tx { listPets() }

    fun getPet(id: Int): Pet? = repository.tx { getPetById(id) }

    fun addPet(pet: Pet): Pet = repository.tx { addPet(pet) }
}
