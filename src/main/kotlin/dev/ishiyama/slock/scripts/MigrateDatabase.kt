package dev.ishiyama.slock.scripts

import dev.ishiyama.slock.connectToDatabase
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

fun main() {
    connectToDatabase()
    val flyway = loadFlywayConfig()
    transaction { flyway.migrate() }
}
