package dev.ishiyama.slock.core.usecase

import dev.ishiyama.slock.core.logic.SessionLogic
import dev.ishiyama.slock.core.repository.SessionRepository
import dev.ishiyama.slock.core.repository.UserRepository
import dev.ishiyama.slock.core.repository.transaction.TransactionManager
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class UserBySessionUseCaseImpl :
    UserBySessionUseCase,
    KoinComponent {
    val transactionManager by inject<TransactionManager>()
    val userRepository by inject<UserRepository>()
    val sessionRepository by inject<SessionRepository>()
    val sessionLogic by inject<SessionLogic>()

    override fun execute(input: UserBySessionUseCase.Input): UserBySessionUseCase.Output =
        transactionManager.start {
            val session = sessionRepository.get(input.sessionId)

            if (session == null) return@start UserBySessionUseCase.Output(user = null)
            if (!sessionLogic.isValid(session.expiresAt)) {
                sessionRepository.delete(input.sessionId)
                return@start UserBySessionUseCase.Output(user = null)
            }

            val user = userRepository.get(session.userId)
            checkNotNull(user)

            if (input.shouldRefreshExpiresAt) {
                val expiresAt = sessionLogic.getExpirationDate()
                sessionRepository.update(
                    input.sessionId,
                    SessionRepository.UpdateSession(expiresAt),
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
