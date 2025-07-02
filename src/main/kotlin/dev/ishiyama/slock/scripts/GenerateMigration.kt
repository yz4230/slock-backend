@file:OptIn(ExperimentalDatabaseMigrationApi::class)

package dev.ishiyama.slock.scripts

import dev.ishiyama.slock.connectToDatabase
import dev.ishiyama.slock.core.repository.Tables
import org.jetbrains.exposed.v1.core.ExperimentalDatabaseMigrationApi
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.migration.MigrationUtils
import java.io.File

fun main(args: Array<String>) {
    val migrationName = args.firstOrNull() ?: error("Migration name must be provided.")
    val migrationsDir = File(Constants.MIGRATIONS_DIR)
    if (!migrationsDir.exists()) {
        migrationsDir.mkdirs()
    }

    val lastVersion =
        migrationsDir
            .walk()
            .filter { it.isFile && it.name.startsWith("V") && it.extension == "sql" }
            .map {
                it.name
                    .substringAfter("V")
                    .substringBefore("__")
                    .toIntOrNull() ?: 0
            }.maxOrNull() ?: 0
    val newVersion = lastVersion + 1
    val newScriptName = "V${newVersion}__$migrationName"

    connectToDatabase()
    transaction {
        MigrationUtils.generateMigrationScript(
            *Tables.allTables,
            scriptDirectory = Constants.MIGRATIONS_DIR,
            scriptName = newScriptName,
        )
    }
}
