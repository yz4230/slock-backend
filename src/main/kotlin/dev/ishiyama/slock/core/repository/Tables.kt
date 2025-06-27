package dev.ishiyama.slock.core.repository

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.timestampWithTimeZone
import java.util.UUID

object Tables {
    object Users : Table("users") {
        val id = uuid("id").clientDefault { UUID.randomUUID() }
        val name = varchar("name", 64)
        val password = varchar("password", 72)

        override val primaryKey = PrimaryKey(id)
    }

    object Sessions : Table("sessions") {
        val id = uuid("id").clientDefault { UUID.randomUUID() }
        val userId = uuid("user_id").references(Users.id)
        val token = char("token", 16)
        val expiresAt = timestampWithTimeZone("expires_at")

        override val primaryKey = PrimaryKey(id)
    }

    object Channels : Table("channels") {
        val id = uuid("id").clientDefault { UUID.randomUUID() }
        val title = varchar("title", 64)
        val description = varchar("description", 1024)
        val isDm = bool("is_dm").default(false)

        override val primaryKey = PrimaryKey(id)
    }

    object Participates : Table("participates") {
        val userId = uuid("user_id").references(Users.id)
        val channelId = uuid("channel_id").references(Channels.id)

        override val primaryKey = PrimaryKey(userId, channelId)
    }

    object Messages : Table("messages") {
        val id = uuid("id").clientDefault { UUID.randomUUID() }
        val userId = uuid("user_id").references(Users.id)
        val channelId = uuid("channel_id").references(Channels.id)
        val threadId = uuid("thread_id").references(id).nullable()
        val content = varchar("content", 1024)

        override val primaryKey = PrimaryKey(id)
    }

    object Attachments : Table("attachments") {
        val id = uuid("id").clientDefault { UUID.randomUUID() }
        val messageId = uuid("message_id").references(Messages.id)
        val filename = varchar("filename", 128)
        val path = text("path")

        override val primaryKey = PrimaryKey(id)
    }

    object Reactions : Table("reactions") {
        val id = uuid("id").clientDefault { UUID.randomUUID() }
        val messageId = uuid("message_id").references(Messages.id)
        val userId = uuid("user_id").references(Users.id)
        val emoji = varchar("emoji", 64)

        override val primaryKey = PrimaryKey(id)
    }

    val allTables =
        arrayOf(
            Users,
            Sessions,
            Channels,
            Participates,
            Messages,
            Attachments,
            Reactions,
        )
}
