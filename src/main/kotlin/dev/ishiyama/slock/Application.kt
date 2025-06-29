@file:OptIn(ExperimentalSerializationApi::class)

package dev.ishiyama.slock

import dev.ishiyama.slock.core.repository.ChannelRepository
import dev.ishiyama.slock.core.repository.ChannelRepositoryImpl
import dev.ishiyama.slock.core.repository.TransactionManager
import dev.ishiyama.slock.core.repository.TransactionManagerImpl
import dev.ishiyama.slock.petstore.petStoreModule
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.netty.EngineMain
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.resources.Resources
import io.ktor.server.response.respond
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.jdbc.Database
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import org.slf4j.event.Level

fun connectToDatabase(): Database =
    Database.connect(
        url = Config.databaseUrl,
        user = Config.databaseUser,
        password = Config.databasePassword,
    )

fun main(args: Array<String>) {
    println("こんにちは、世界！")
    connectToDatabase()
    EngineMain.main(args)
}

fun Throwable.flattenCauses(): List<Throwable> {
    val result = mutableListOf<Throwable>()
    var cause: Throwable? = this
    while (cause != null) {
        result.add(cause)
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
    install(CallLogging) { level = Level.INFO }
    install(Resources)
    install(StatusPages) {
        exception<BadRequestException> { call, cause ->
            for (cause in cause.flattenCauses()) {
                when (cause) {
                    is MissingFieldException ->
                        call.respond(
                            HttpStatusCode.BadRequest,
                            FieldErrorResponse(cause),
                        )
                }
            }
            throw cause
        }
    }

    configureRouting()
}
