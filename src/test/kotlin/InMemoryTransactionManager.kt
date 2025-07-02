import dev.ishiyama.slock.core.repository.transaction.TransactionContext
import dev.ishiyama.slock.core.repository.transaction.TransactionManager

class InMemoryTransactionManager : TransactionManager {
    object Context : TransactionContext {
        override fun commit() {
            // do nothing
        }

        override fun rollback() {
            // do nothing
        }
    }

    override fun <T> start(block: TransactionContext.() -> T): T = block(Context)
}
