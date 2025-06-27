package dev.ishiyama.slock.core.repository

interface TransactionManager {
    fun <T> start(block: () -> T): T
}
