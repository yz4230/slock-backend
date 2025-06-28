@file:OptIn(ExperimentalSerializationApi::class)

package dev.ishiyama.slock

import dev.ishiyama.slock.core.repository.ChannelRepository
import dev.ishiyama.slock.core.repository.ChannelRepositoryImpl
import dev.ishiyama.slock.core.repository.Tables
import dev.ishiyama.slock.core.repository.TransactionManager
import dev.ishiyama.slock.core.repository.TransactionManagerImpl
import dev.ishiyama.slock.petstore.PetsTable
import dev.ishiyama.slock.petstore.petStoreModule
import io.github.cdimascio.dotenv.dotenv
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.netty.EngineMain
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.httpMethod
import io.ktor.server.resources.Resources
import io.ktor.server.response.respond
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import org.slf4j.event.Level

object Config {
    val databaseUrl: String by lazy { mustGet("DATABASE_URL") }
    val databaseUser: String by lazy { mustGet("DATABASE_USER") }
    val databasePassword: String by lazy { mustGet("DATABASE_PASSWORD") }

    private val filenames = listOf(".env.local", ".env")
    private val dotenv =
        filenames.map {
            dotenv {
                filename = it
                ignoreIfMissing = true
            }
        }

    private fun mustGet(key: String): String {
        for (env in dotenv) env[key]?.let { return it }
        throw IllegalStateException("Environment variable $key is not set")
    }
}

fun main(args: Array<String>) {
    println("こんにちは、世界！")
    Database.connect(
        url = Config.databaseUrl,
        user = Config.databaseUser,
        password = Config.databasePassword,
    )
    transaction {
        SchemaUtils.create(PetsTable)
        SchemaUtils.create(*Tables.allTables)
    }

    EngineMain.main(args)
}

fun Throwable.flatten(): List<Throwable> {
    val result = mutableListOf<Throwable>()
    var cause: Throwable? = this
    while (cause != null) {
        result.add(cause)
        if (cause.cause == cause) {
            break
        }
        cause = cause.cause
    }
    return result
}

fun Application.module() {
    val slockModule =
        module {
            singleOf(::TransactionManagerImpl) { bind<TransactionManager>() }
            singleOf(::ChannelRepositoryImpl) { bind<ChannelRepository>() }
        }

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
        modules(petStoreModule)
        modules(slockModule)
    }
    install(CallLogging) {
        level = Level.INFO
        format { call ->
            val status = call.response.status()
            val httpMethod = call.request.httpMethod.value
            val userAgent = call.request.headers["User-Agent"]
            "Status: $status, HTTP method: $httpMethod, User agent: $userAgent"
        }
    }
    install(Resources)
    install(StatusPages) {
        exception<BadRequestException> { call, cause ->
            val missingFieldException =
                cause
                    .flatten()
                    .firstOrNull { MissingFieldException::class.isInstance(it) }
                    .let { it as? MissingFieldException }
            if (missingFieldException != null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    FieldErrorResponse(missingFieldException),
                )
                return@exception
            }
            throw cause
        }
    }

    configureRouting()
}
