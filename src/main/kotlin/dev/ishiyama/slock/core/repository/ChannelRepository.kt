package dev.ishiyama.slock.core.repository

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
        val title: String,
        val description: String,
        val isDm: Boolean,
    )

    data class UpdateChannel(
        val title: String? = null,
        val description: String? = null,
        val isDm: Boolean? = null,
    )

    data class ReadChannel(
        val id: String,
        val title: String,
        val description: String,
        val isDm: Boolean,
    )
}
