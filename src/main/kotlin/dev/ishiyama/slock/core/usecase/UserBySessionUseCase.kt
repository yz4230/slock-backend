package dev.ishiyama.slock.core.usecase

import kotlinx.datetime.Instant

interface UserBySessionUseCase {
    fun execute(input: Input): Output

    data class Input(
        val sessionId: String,
        val shouldRefreshExpiresAt: Boolean = true,
    )

    data class Output(
        val user: User?,
    ) {
        data class User(
            val id: String,
            val name: String,
            val displayName: String,
            val email: String,
            val createdAt: Instant,
            val updatedAt: Instant,
        )
    }
}
