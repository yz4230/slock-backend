package dev.ishiyama.slock.core.repository

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.datetime.timestamp
import java.util.UUID

abstract class WithTimestamp(
    name: String,
) : Table(name) {
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp)
}

object Tables {
    object Users : WithTimestamp("users") {
        val id = uuid("id").clientDefault { UUID.randomUUID() }
        val name = varchar("name", 128)
        val displayName = varchar("display_name", 128).nullable()
        val email = varchar("email", 128)
        val password = varchar("password", 60)

        override val primaryKey = PrimaryKey(id)
    }

    object Sessions : WithTimestamp("sessions") {
        val id = uuid("id").clientDefault { UUID.randomUUID() }
        val userId = uuid("user_id").references(Users.id)
        val expiresAt = timestamp("expires_at")

        override val primaryKey = PrimaryKey(id)
    }

    object Channels : WithTimestamp("channels") {
        val id = uuid("id").clientDefault { UUID.randomUUID() }
        val name = varchar("name", 64)
        val description = varchar("description", 1024)
        val isDirect = bool("is_direct").default(false)

        override val primaryKey = PrimaryKey(id)
    }

    object Participates : WithTimestamp("participates") {
        val userId = uuid("user_id").references(Users.id)
        val channelId = uuid("channel_id").references(Channels.id)

        override val primaryKey = PrimaryKey(userId, channelId)
    }

    object Messages : WithTimestamp("messages") {
        val id = uuid("id").clientDefault { UUID.randomUUID() }
        val userId = uuid("user_id").references(Users.id)
        val channelId = uuid("channel_id").references(Channels.id)
        val threadId = uuid("thread_id").references(id).nullable()
        val content = varchar("content", 1024)

        override val primaryKey = PrimaryKey(id)
    }

    object Attachments : WithTimestamp("attachments") {
        val id = uuid("id").clientDefault { UUID.randomUUID() }
        val messageId = uuid("message_id").references(Messages.id)
        val filename = varchar("filename", 128)
        val path = text("path")

        override val primaryKey = PrimaryKey(id)
    }

    object Reactions : WithTimestamp("reactions") {
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
