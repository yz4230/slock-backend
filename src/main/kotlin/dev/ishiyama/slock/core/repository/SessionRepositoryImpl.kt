package dev.ishiyama.slock.core.repository

import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import java.util.UUID

class SessionRepositoryImpl : SessionRepository {
    override fun create(session: SessionRepository.CreateSession): SessionRepository.ReadSession =
        Tables.Sessions
            .insert {
                it[userId] = UUID.fromString(session.userId)
                it[expiresAt] = session.expiresAt
            }.let {
                SessionRepository.ReadSession(
                    id = it[Tables.Sessions.id].toString(),
                    userId = session.userId,
                    expiresAt = session.expiresAt,
                    createdAt = it[Tables.Sessions.createdAt],
                    updatedAt = it[Tables.Sessions.updatedAt],
                )
            }

    override fun get(id: String): SessionRepository.ReadSession? =
        Tables.Sessions
            .selectAll()
            .where { Tables.Sessions.id eq UUID.fromString(id) }
            .map {
                SessionRepository.ReadSession(
                    id = it[Tables.Sessions.id].toString(),
                    userId = it[Tables.Sessions.userId].toString(),
                    expiresAt = it[Tables.Sessions.expiresAt],
                    createdAt = it[Tables.Sessions.createdAt],
                    updatedAt = it[Tables.Sessions.updatedAt],
                )
            }.singleOrNull()

    override fun update(
        id: String,
        update: SessionRepository.UpdateSession,
    ): SessionRepository.ReadSession? {
        val updatedRows =
            Tables.Sessions.update({ Tables.Sessions.id eq UUID.fromString(id) }) {
                update.expiresAt?.let { value -> it[expiresAt] = value }
            }
        return if (updatedRows > 0) get(id) else null
    }

    override fun delete(id: String): Boolean = Tables.Sessions.deleteWhere { Tables.Sessions.id eq UUID.fromString(id) } > 0
}
