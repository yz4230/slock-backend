package dev.ishiyama.slock.core.repository

import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class TransactionManagerImpl : TransactionManager {
    override fun <T> start(block: () -> T): T = transaction { block() }
}
