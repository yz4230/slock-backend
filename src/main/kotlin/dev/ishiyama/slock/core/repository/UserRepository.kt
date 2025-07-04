package dev.ishiyama.slock.core.repository

import kotlinx.datetime.Instant

interface UserRepository {
    fun create(user: CreateUser): ReadUser

    fun get(id: String): ReadUser?

    fun getByEmail(email: String): ReadUser?

    fun getPassword(id: String): String?

    fun getPasswordByEmail(email: String): String?

    fun update(
        id: String,
        update: UpdateUser,
    ): ReadUser?

    data class CreateUser(
        val name: String,
        val displayName: String,
        val email: String,
        val password: String,
    )

    data class UpdateUser(
        val name: String? = null,
        val displayName: String? = null,
        val email: String? = null,
        val password: String? = null,
    )

    data class ReadUser(
        val id: String,
        val name: String,
        val displayName: String,
        val email: String,
        val createdAt: Instant,
        val updatedAt: Instant,
    )
}
