import dev.ishiyama.slock.core.repository.UserRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.util.UUID

class InMemoryUserRepository : UserRepository {
    private val users = mutableMapOf<String, User>()

    data class User(
        val id: String,
        val name: String,
        val email: String,
        val password: String,
        val createdAt: Instant,
        val updatedAt: Instant,
    )

    override fun create(user: UserRepository.CreateUser): UserRepository.ReadUser {
        val newUser =
            User(
                id = UUID.randomUUID().toString(),
                name = user.name,
                email = user.email,
                password = user.password, // In a real implementation, this should be hashed
                createdAt = Clock.System.now(),
                updatedAt = Clock.System.now(),
            )
        users[newUser.id] = newUser
        return UserRepository.ReadUser(
            id = newUser.id,
            name = newUser.name,
            email = newUser.email,
            createdAt = newUser.createdAt,
            updatedAt = newUser.updatedAt,
        )
    }

    override fun get(id: String): UserRepository.ReadUser? =
        users[id]?.let {
            UserRepository.ReadUser(
                id = it.id,
                name = it.name,
                email = it.email,
                createdAt = it.createdAt,
                updatedAt = it.updatedAt,
            )
        }

    override fun getByEmail(email: String): UserRepository.ReadUser? =
        users.values.find { it.email == email }?.let {
            UserRepository.ReadUser(
                id = it.id,
                name = it.name,
                email = it.email,
                createdAt = it.createdAt,
                updatedAt = it.updatedAt,
            )
        }

    override fun getPassword(id: String): String? = users[id]?.password

    override fun getPasswordByEmail(email: String): String? = users.values.find { it.email == email }?.password

    override fun update(
        id: String,
        update: UserRepository.UpdateUser,
    ): UserRepository.ReadUser? {
        val user = users[id] ?: return null
        val updatedUser =
            user.copy(
                name = update.name ?: user.name,
                email = update.email ?: user.email,
                password = update.password ?: user.password,
                updatedAt = Clock.System.now(),
            )
        users[id] = updatedUser
        return UserRepository.ReadUser(
            id = updatedUser.id,
            name = updatedUser.name,
            email = updatedUser.email,
            createdAt = updatedUser.createdAt,
            updatedAt = updatedUser.updatedAt,
        )
    }
}
