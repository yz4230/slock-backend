@file:OptIn(ExperimentalDatabaseMigrationApi::class)

package dev.ishiyama.slock.scripts

import dev.ishiyama.slock.connectToDatabase
import dev.ishiyama.slock.core.repository.Tables
import org.jetbrains.exposed.v1.core.ExperimentalDatabaseMigrationApi
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.migration.MigrationUtils
import java.io.File

fun main() {
    File(Constants.MIGRATIONS_DIR).apply { if (!exists()) mkdirs() }

    connectToDatabase()
    transaction {
        MigrationUtils.generateMigrationScript(
            *Tables.allTables,
            scriptDirectory = Constants.MIGRATIONS_DIR,
            scriptName = "V4__remove_timestamp_zones",
        )
    }
}
