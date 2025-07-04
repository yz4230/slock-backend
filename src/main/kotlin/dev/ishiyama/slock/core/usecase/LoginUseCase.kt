package dev.ishiyama.slock.core.usecase

import kotlinx.datetime.Instant

interface LoginUseCase {
    fun execute(input: Input): Output

    data class Input(
        val email: String,
        val password: String,
    )

    data class Output(
        val user: User,
        val sessionId: String,
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
