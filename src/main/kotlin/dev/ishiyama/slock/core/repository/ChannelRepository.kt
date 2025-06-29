package dev.ishiyama.slock.core.repository

import java.time.OffsetDateTime

interface ChannelRepository {
    fun create(channel: CreateChannel): ReadChannel

    fun get(id: String): ReadChannel?

    fun list(): List<ReadChannel>

    fun update(
        id: String,
        update: UpdateChannel,
    ): ReadChannel?

    fun delete(id: String): Boolean

    data class CreateChannel(
        val name: String,
        val description: String,
        val isDirect: Boolean,
    )

    data class UpdateChannel(
        val name: String? = null,
        val description: String? = null,
        val isDirect: Boolean? = null,
    )

    data class ReadChannel(
        val id: String,
        val name: String,
        val description: String,
        val isDirect: Boolean,
        val createdAt: OffsetDateTime,
        val updatedAt: OffsetDateTime,
    )
}
