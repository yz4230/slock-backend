package dev.ishiyama.slock.core.repository

interface TransactionContext {
    fun commit()

    fun rollback()
}

interface TransactionManager {
    fun <T> start(block: TransactionContext.() -> T): T
}
