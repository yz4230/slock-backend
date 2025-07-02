package dev.ishiyama.slock.core.usecase

import kotlinx.datetime.Instant

interface ListChannelsUseCase {
    fun execute(): Output

    data class Output(
        val channels: List<Channel>,
    ) {
        data class Channel(
            val id: String,
            val name: String,
            val description: String,
            val isDirect: Boolean,
            val createdAt: Instant,
            val updatedAt: Instant,
        )
    }
}
