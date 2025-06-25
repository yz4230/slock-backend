package dev.ishiyama

import dev.ishiyama.tables.Pets
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.netty.EngineMain
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import org.slf4j.event.Level

fun main(args: Array<String>) {
    println("こんにちは、世界！")
    Database.connect(
        "jdbc:postgresql://localhost:5432/slock",
        driver = "org.postgresql.Driver",
        user = "postgres",
        password = "password",
    )
    transaction {
        SchemaUtils.create(Pets)
    }

    EngineMain.main(args)
}

fun Application.module() {
    install(ContentNegotiation) {
        json(
            Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            },
        )
    }
    install(Koin) {
        slf4jLogger()
        modules(appModule)
    }
    install(CallLogging) {
        level = Level.DEBUG
    }

    configureRouting()
}
