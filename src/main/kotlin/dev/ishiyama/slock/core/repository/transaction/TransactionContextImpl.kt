package dev.ishiyama.slock.core.repository.transaction

import org.jetbrains.exposed.v1.jdbc.JdbcTransaction

class TransactionContextImpl(
    val jdbcTransaction: JdbcTransaction,
) : TransactionContext {
    override fun commit() = jdbcTransaction.commit()

    override fun rollback() = jdbcTransaction.rollback()
}
