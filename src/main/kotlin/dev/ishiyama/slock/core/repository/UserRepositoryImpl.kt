package dev.ishiyama.slock.core.repository

import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import java.util.UUID

class UserRepositoryImpl : UserRepository {
    override fun create(user: UserRepository.CreateUser): UserRepository.ReadUser =
        Tables.Users
            .insert {
                it[name] = user.name
                it[displayName] = user.displayName
                it[email] = user.email
                it[password] = user.password
            }.let {
                UserRepository.ReadUser(
                    id = it[Tables.Users.id].toString(),
                    name = user.name,
                    displayName = user.displayName,
                    email = user.email,
                    createdAt = it[Tables.Users.createdAt],
                    updatedAt = it[Tables.Users.updatedAt],
                )
            }

    override fun get(id: String): UserRepository.ReadUser? =
        Tables.Users
            .selectAll()
            .where { Tables.Users.id eq UUID.fromString(id) }
            .map {
                UserRepository.ReadUser(
                    id = it[Tables.Users.id].toString(),
                    name = it[Tables.Users.name],
                    displayName = it[Tables.Users.displayName],
                    email = it[Tables.Users.email],
                    createdAt = it[Tables.Users.createdAt],
                    updatedAt = it[Tables.Users.updatedAt],
                )
            }.singleOrNull()

    override fun getByEmail(email: String): UserRepository.ReadUser? =
        Tables.Users
            .selectAll()
            .where { Tables.Users.email eq email }
            .map {
                UserRepository.ReadUser(
                    id = it[Tables.Users.id].toString(),
                    name = it[Tables.Users.name],
                    displayName = it[Tables.Users.displayName],
                    email = it[Tables.Users.email],
                    createdAt = it[Tables.Users.createdAt],
                    updatedAt = it[Tables.Users.updatedAt],
                )
            }.firstOrNull()

    override fun getPassword(id: String): String? =
        Tables.Users
            .select(Tables.Users.password)
            .where { Tables.Users.id eq UUID.fromString(id) }
            .map { it[Tables.Users.password] }
            .singleOrNull()

    override fun getPasswordByEmail(email: String): String? =
        Tables.Users
            .select(Tables.Users.password)
            .where { Tables.Users.email eq email }
            .map { it[Tables.Users.password] }
            .singleOrNull()

    override fun update(
        id: String,
        update: UserRepository.UpdateUser,
    ): UserRepository.ReadUser? {
        val updatedRows =
            Tables.Users.update({ Tables.Users.id eq UUID.fromString(id) }) {
                update.name?.let { value -> it[name] = value }
                update.displayName?.let { value -> it[displayName] = value }
                update.email?.let { value -> it[email] = value }
                update.password?.let { value -> it[password] = value }
                it[updatedAt] = CurrentTimestamp
            }

        return if (updatedRows > 0) get(id) else null
    }
}
