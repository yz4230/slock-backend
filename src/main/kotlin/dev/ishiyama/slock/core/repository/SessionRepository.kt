package dev.ishiyama.slock.core.repository

import kotlinx.datetime.Instant

interface SessionRepository {
    fun create(session: CreateSession): ReadSession

    fun get(id: String): ReadSession?

    fun update(
        id: String,
        update: UpdateSession,
    ): ReadSession?

    fun delete(id: String): Boolean

    data class CreateSession(
        val userId: String,
        val expiresAt: Instant,
    )

    data class ReadSession(
        val id: String,
        val userId: String,
        val expiresAt: Instant,
        val createdAt: Instant,
        val updatedAt: Instant,
    )

    data class UpdateSession(
        val expiresAt: Instant? = null,
    )
}
