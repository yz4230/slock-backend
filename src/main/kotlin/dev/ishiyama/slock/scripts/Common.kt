package dev.ishiyama.slock.scripts

import dev.ishiyama.slock.Config
import org.flywaydb.core.Flyway

fun loadFlywayConfig(): Flyway =
    Flyway
        .configure()
        .dataSource(Config.databaseUrl, Config.databaseUser, Config.databasePassword)
        .locations("filesystem:${Constants.MIGRATIONS_DIR}")
        .baselineOnMigrate(true)
        .load()
