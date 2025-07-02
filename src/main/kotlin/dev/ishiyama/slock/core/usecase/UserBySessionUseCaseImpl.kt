package dev.ishiyama.slock.core.usecase

import dev.ishiyama.slock.core.repository.SessionRepository
import dev.ishiyama.slock.core.repository.UserRepository
import dev.ishiyama.slock.core.repository.transaction.TransactionManager
import kotlinx.datetime.Clock
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class UserBySessionUseCaseImpl :
    UserBySessionUseCase,
    KoinComponent {
    companion object {
        const val SESSION_EXPIRATION_DAYS = 30
    }

    val transactionManager by inject<TransactionManager>()
    val userRepository by inject<UserRepository>()
    val sessionRepository by inject<SessionRepository>()

    override fun execute(input: UserBySessionUseCase.Input): UserBySessionUseCase.Output =
        transactionManager.start {
            val session = sessionRepository.get(input.sessionId)
            if (session == null) return@start UserBySessionUseCase.Output(user = null)

            val user = userRepository.get(session.userId)
            checkNotNull(user)

            if (session.expiresAt < Clock.System.now()) {
                sessionRepository.delete(input.sessionId)
                return@start UserBySessionUseCase.Output(user = null)
            }

            if (input.shouldRefreshExpiresAt) {
                val newExpiresAt = Clock.System.now() + SESSION_EXPIRATION_DAYS.toDuration(DurationUnit.DAYS)
                sessionRepository.update(
                    input.sessionId,
                    SessionRepository.UpdateSession(expiresAt = newExpiresAt),
                )
            }

            UserBySessionUseCase.Output(
                user =
                    UserBySessionUseCase.Output.User(
                        id = user.id,
                        name = user.name,
                        email = user.email,
                        createdAt = user.createdAt,
                        updatedAt = user.updatedAt,
                    ),
            )
        }
}
