package dev.ishiyama.slock.core.repository

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
                it[name] = channel.name
                it[description] = channel.description
                it[isDirect] = channel.isDirect
            }.let {
                ChannelRepository.ReadChannel(
                    id = it[Tables.Channels.id].toString(),
                    name = channel.name,
                    description = channel.description,
                    isDirect = channel.isDirect,
                )
            }

    override fun get(id: String): ChannelRepository.ReadChannel? =
        Tables.Channels
            .selectAll()
            .where { Tables.Channels.id eq UUID.fromString(id) }
            .map {
                ChannelRepository.ReadChannel(
                    id = it[Tables.Channels.id].toString(),
                    name = it[Tables.Channels.name],
                    description = it[Tables.Channels.description],
                    isDirect = it[Tables.Channels.isDirect],
                )
            }.singleOrNull()

    override fun list(): List<ChannelRepository.ReadChannel> =
        Tables.Channels
            .selectAll()
            .map {
                ChannelRepository.ReadChannel(
                    id = it[Tables.Channels.id].toString(),
                    name = it[Tables.Channels.name],
                    description = it[Tables.Channels.description],
                    isDirect = it[Tables.Channels.isDirect],
                )
            }

    override fun update(
        id: String,
        update: ChannelRepository.UpdateChannel,
    ): ChannelRepository.ReadChannel? {
        val updatedRows =
            Tables.Channels.update({ Tables.Channels.id eq UUID.fromString(id) }) {
                update.name?.let { value -> it[name] = value }
                update.description?.let { value -> it[description] = value }
                update.isDirect?.let { value -> it[isDirect] = value }
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
