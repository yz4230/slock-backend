import dev.ishiyama.slock.core.repository.SessionRepository
import kotlinx.datetime.Clock
import java.util.UUID

class InMemorySessionRepository : SessionRepository {
    private val sessions = mutableMapOf<String, SessionRepository.ReadSession>()

    override fun create(session: SessionRepository.CreateSession): SessionRepository.ReadSession {
        val newSession =
            SessionRepository.ReadSession(
                id = UUID.randomUUID().toString(),
                userId = session.userId,
                expiresAt = session.expiresAt,
                createdAt = Clock.System.now(),
                updatedAt = Clock.System.now(),
            )
        sessions[newSession.id] = newSession
        return newSession
    }

    override fun get(id: String): SessionRepository.ReadSession? = sessions[id]

    override fun update(
        id: String,
        update: SessionRepository.UpdateSession,
    ): SessionRepository.ReadSession? {
        val existingSession = sessions[id] ?: return null
        val updatedSession =
            existingSession.copy(
                expiresAt = update.expiresAt ?: existingSession.expiresAt,
                updatedAt = Clock.System.now(),
            )
        sessions[id] = updatedSession
        return updatedSession
    }

    override fun delete(id: String): Boolean = sessions.remove(id) != null
}
