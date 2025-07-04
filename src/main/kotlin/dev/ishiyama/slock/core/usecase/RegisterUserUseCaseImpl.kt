package dev.ishiyama.slock.core.usecase

import dev.ishiyama.slock.core.logic.SessionLogic
import dev.ishiyama.slock.core.repository.SessionRepository
import dev.ishiyama.slock.core.repository.UserRepository
import dev.ishiyama.slock.core.repository.transaction.TransactionManager
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.springframework.security.crypto.bcrypt.BCrypt

class RegisterUserUseCaseImpl :
    RegisterUserUseCase,
    KoinComponent {
    companion object {
        const val MIN_PASSWORD_LENGTH = 8
    }

    val transactionManager by inject<TransactionManager>()
    val userRepository by inject<UserRepository>()
    val sessionRepository by inject<SessionRepository>()
    val sessionLogic by inject<SessionLogic>()

    override fun execute(input: RegisterUserUseCase.Input): RegisterUserUseCase.Output {
        require(input.name.isNotBlank()) { "Name cannot be blank" }
        require(input.email.isNotBlank()) { "Email cannot be blank" }
        require(input.password.isNotBlank()) { "Password cannot be blank" }
        require(input.password.length >= MIN_PASSWORD_LENGTH) { "Password must be at least 8 characters long" }

        val passwordHashed = BCrypt.hashpw(input.password, BCrypt.gensalt())
        val expiresAt = sessionLogic.getExpirationDate()

        return transactionManager.start {
            val existing = userRepository.getByEmail(input.email)
            println(existing)
            require(existing == null) { "Email is already registered" }

            val user =
                userRepository.create(
                    UserRepository.CreateUser(
                        name = input.name,
                        displayName = input.displayName,
                        email = input.email,
                        password = passwordHashed,
                    ),
                )

            val session =
                sessionRepository.create(
                    SessionRepository.CreateSession(
                        userId = user.id,
                        expiresAt = expiresAt,
                    ),
                )

            RegisterUserUseCase.Output(
                user =
                    RegisterUserUseCase.Output.User(
                        id = user.id,
                        name = user.name,
                        displayName = user.displayName,
                        email = user.email,
                        createdAt = user.createdAt,
                        updatedAt = user.updatedAt,
                    ),
                sessionId = session.id,
            )
        }
    }
}
