package dev.ishiyama.slock.core.repository

import org.jetbrains.exposed.v1.core.StdOutSqlLogger
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import org.jetbrains.exposed.v1.jdbc.addLogger
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class TransactionContextImpl(
    val jdbcTransaction: JdbcTransaction,
) : TransactionContext {
    override fun commit() = jdbcTransaction.commit()

    override fun rollback() = jdbcTransaction.rollback()
}

class TransactionManagerImpl : TransactionManager {
    override fun <T> start(block: TransactionContext.() -> T): T =
        transaction {
            addLogger(StdOutSqlLogger)
            block(TransactionContextImpl(this))
        }
}
