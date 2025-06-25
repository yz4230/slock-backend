package dev.ishiyama.slock.petstore

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

object PetsTable : Table("pets") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 64)
    val age = integer("age")
    val type = varchar("type", 64)

    override val primaryKey = PrimaryKey(id)
}

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
        PetsTable.selectAll().map {
            Pet(
                id = it[PetsTable.id],
                name = it[PetsTable.name],
                type = it[PetsTable.type],
                age = it[PetsTable.age],
            )
        }

    override fun getPetById(id: Int): Pet? =
        PetsTable
            .selectAll()
            .where { PetsTable.id eq id }
            .map {
                Pet(
                    id = it[PetsTable.id],
                    name = it[PetsTable.name],
                    type = it[PetsTable.type],
                    age = it[PetsTable.age],
                )
            }.singleOrNull()

    override fun addPet(pet: Pet): Pet {
        val id =
            PetsTable.insert {
                it[name] = pet.name
                it[type] = pet.type
                it[age] = pet.age
            } get PetsTable.id
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
