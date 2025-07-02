package dev.ishiyama.slock.core.repository.transaction

interface TransactionContext {
    fun commit()

    fun rollback()
}
