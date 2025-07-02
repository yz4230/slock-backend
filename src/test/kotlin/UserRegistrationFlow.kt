import dev.ishiyama.slock.core.logic.SessionLogic
import dev.ishiyama.slock.core.logic.SessionLogicImpl
import dev.ishiyama.slock.core.repository.SessionRepository
import dev.ishiyama.slock.core.repository.UserRepository
import dev.ishiyama.slock.core.repository.transaction.TransactionManager
import dev.ishiyama.slock.core.usecase.LoginUseCase
import dev.ishiyama.slock.core.usecase.LoginUseCaseImpl
import dev.ishiyama.slock.core.usecase.RegisterUserUseCase
import dev.ishiyama.slock.core.usecase.RegisterUserUseCaseImpl
import dev.ishiyama.slock.core.usecase.UserBySessionUseCase
import dev.ishiyama.slock.core.usecase.UserBySessionUseCaseImpl
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.koin.test.KoinTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class UserRegistrationFlow : KoinTest {
    @Test
    fun `test user registration flow`() {
        startKoin {
            printLogger()
            modules(
                module {
                    singleOf(::InMemoryTransactionManager) { bind<TransactionManager>() }
                    singleOf(::InMemoryUserRepository) { bind<UserRepository>() }
                    singleOf(::InMemorySessionRepository) { bind<SessionRepository>() }
                    singleOf(::SessionLogicImpl) { bind<SessionLogic>() }
                },
            )
        }

        val name = "johndoe"
        val email = "johndoe@example.com"
        val password = "securePassword"

        // register a user
        val sessionId =
            run {
                val usecase: RegisterUserUseCase = RegisterUserUseCaseImpl()
                val input =
                    RegisterUserUseCase.Input(name = name, email = email, password = password)
                val output = usecase.execute(input)
                output.sessionId
            }

        // get by token
        run {
            val usecase: UserBySessionUseCase = UserBySessionUseCaseImpl()
            val input = UserBySessionUseCase.Input(sessionId)
            val output = usecase.execute(input)
            assertNotNull(output.user)
            assertEquals(name, output.user.name)
            assertEquals(email, output.user.email)
        }

        // login
        run {
            val usecase: LoginUseCase = LoginUseCaseImpl()
            val input = LoginUseCase.Input(email = email, password = password)
            val output = usecase.execute(input)
            assertNotNull(output.user)
            assertEquals(name, output.user.name)
            assertEquals(email, output.user.email)
        }
    }
}
