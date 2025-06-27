package dev.ishiyama.slock.core.repository

import dev.ishiyama.slock.core.repository.Tables
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import java.util.UUID

class ChannelRepositoryImpl : ChannelRepository {
    override fun create(channel: ChannelRepository.CreateChannel): ChannelRepository.ReadChannel =
        Tables.Channels
            .insert {
                it[title] = channel.title
                it[description] = channel.description
                it[isDm] = channel.isDm
            }.let {
                ChannelRepository.ReadChannel(
                    id = it[Tables.Channels.id].toString(),
                    title = channel.title,
                    description = channel.description,
                    isDm = channel.isDm,
                )
            }

    override fun get(id: String): ChannelRepository.ReadChannel? =
        Tables.Channels
            .selectAll()
            .where { Tables.Channels.id eq UUID.fromString(id) }
            .map {
                ChannelRepository.ReadChannel(
                    id = it[Tables.Channels.id].toString(),
                    title = it[Tables.Channels.title],
                    description = it[Tables.Channels.description],
                    isDm = it[Tables.Channels.isDm],
                )
            }.singleOrNull()

    override fun list(): List<ChannelRepository.ReadChannel> =
        Tables.Channels
            .selectAll()
            .map {
                ChannelRepository.ReadChannel(
                    id = it[Tables.Channels.id].toString(),
                    title = it[Tables.Channels.title],
                    description = it[Tables.Channels.description],
                    isDm = it[Tables.Channels.isDm],
                )
            }

    override fun update(
        id: String,
        update: ChannelRepository.UpdateChannel,
    ): ChannelRepository.ReadChannel? {
        val updatedRows =
            Tables.Channels.update({ Tables.Channels.id eq UUID.fromString(id) }) {
                update.title?.let { value -> it[title] = value }
                update.description?.let { value -> it[description] = value }
                update.isDm?.let { value -> it[isDm] = value }
            }
        return if (updatedRows > 0) {
            get(id)
        } else {
            null
        }
    }

    override fun delete(id: String): Boolean =
        Tables.Channels.deleteWhere {
            Tables.Channels.id eq UUID.fromString(id)
        } > 0
}
