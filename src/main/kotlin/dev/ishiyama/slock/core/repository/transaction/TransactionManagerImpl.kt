package dev.ishiyama.slock.core.repository.transaction

import org.jetbrains.exposed.v1.core.StdOutSqlLogger
import org.jetbrains.exposed.v1.jdbc.addLogger
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class TransactionManagerImpl : TransactionManager {
    override fun <T> start(block: TransactionContext.() -> T): T =
        transaction {
            addLogger(StdOutSqlLogger)
            block(TransactionContextImpl(this))
        }
}
