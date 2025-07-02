package dev.ishiyama.slock.core.usecase

import dev.ishiyama.slock.core.logic.SessionLogic
import dev.ishiyama.slock.core.repository.SessionRepository
import dev.ishiyama.slock.core.repository.UserRepository
import dev.ishiyama.slock.core.repository.transaction.TransactionManager
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.springframework.security.crypto.bcrypt.BCrypt
import kotlin.getValue

class LoginUseCaseImpl :
    LoginUseCase,
    KoinComponent {
    val transactionManager by inject<TransactionManager>()
    val userRepository by inject<UserRepository>()
    val sessionRepository by inject<SessionRepository>()
    val sessionLogic by inject<SessionLogic>()

    override fun execute(input: LoginUseCase.Input): LoginUseCase.Output {
        require(input.email.isNotBlank()) { "Email cannot be blank" }
        require(input.password.isNotBlank()) { "Password cannot be blank" }

        return transactionManager.start {
            val password =
                userRepository.getPasswordByEmail(input.email)
                    ?: throw IllegalArgumentException("Invalid email or password")

            if (!BCrypt.checkpw(input.password, password)) {
                throw IllegalArgumentException("Invalid email or password")
            }

            val user =
                userRepository.getByEmail(input.email)
                    ?: throw IllegalArgumentException("Invalid email or password")

            val expiresAt = sessionLogic.getExpirationDate()
            val session =
                sessionRepository.create(
                    SessionRepository.CreateSession(
                        userId = user.id,
                        expiresAt = expiresAt,
                    ),
                )

            LoginUseCase.Output(
                user =
                    LoginUseCase.Output.User(
                        id = user.id,
                        name = user.name,
                        email = user.email,
                        createdAt = user.createdAt,
                        updatedAt = user.updatedAt,
                    ),
                sessionId = session.id,
            )
        }
    }
}
