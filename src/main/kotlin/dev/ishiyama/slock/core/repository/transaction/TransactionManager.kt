package dev.ishiyama.slock.core.repository.transaction

interface TransactionManager {
    fun <T> start(block: TransactionContext.() -> T): T
}
