package dev.ishiyama.slock.core.repository

import kotlinx.datetime.Instant

interface UserRepository {
    fun create(user: CreateUser): ReadUser

    fun get(id: String): ReadUser?

    fun getByName(name: String): ReadUser?

    fun getPassword(id: String): String?

    fun update(
        id: String,
        update: UpdateUser,
    ): ReadUser?

    data class CreateUser(
        val name: String,
        val email: String,
        val password: String,
    )

    data class UpdateUser(
        val name: String? = null,
        val email: String? = null,
        val password: String? = null,
    )

    data class ReadUser(
        val id: String,
        val name: String,
        val email: String,
        val createdAt: Instant,
        val updatedAt: Instant,
    )
}
