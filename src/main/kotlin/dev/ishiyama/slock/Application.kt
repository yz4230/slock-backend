@file:OptIn(ExperimentalSerializationApi::class)

package dev.ishiyama.slock

import dev.ishiyama.slock.core.logic.SessionLogic
import dev.ishiyama.slock.core.logic.SessionLogicImpl
import dev.ishiyama.slock.core.repository.ChannelRepository
import dev.ishiyama.slock.core.repository.ChannelRepositoryImpl
import dev.ishiyama.slock.core.repository.SessionRepository
import dev.ishiyama.slock.core.repository.SessionRepositoryImpl
import dev.ishiyama.slock.core.repository.UserRepository
import dev.ishiyama.slock.core.repository.UserRepositoryImpl
import dev.ishiyama.slock.core.repository.transaction.TransactionManager
import dev.ishiyama.slock.core.repository.transaction.TransactionManagerImpl
import dev.ishiyama.slock.core.usecase.ListChannelsUseCase
import dev.ishiyama.slock.core.usecase.ListChannelsUseCaseImpl
import dev.ishiyama.slock.core.usecase.LoginUseCase
import dev.ishiyama.slock.core.usecase.LoginUseCaseImpl
import dev.ishiyama.slock.core.usecase.RegisterUserUseCase
import dev.ishiyama.slock.core.usecase.RegisterUserUseCaseImpl
import dev.ishiyama.slock.core.usecase.UserBySessionUseCase
import dev.ishiyama.slock.core.usecase.UserBySessionUseCaseImpl
import dev.ishiyama.slock.petstore.petStoreModule
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.netty.EngineMain
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.plugins.statuspages.exception
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
    install(ContentNegotiation) {
        json(
            Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
                encodeDefaults = true
            },
        )
    }
    install(Koin) {
        slf4jLogger()
        modules(petStoreModule)
        modules(
            module {
                singleOf(::TransactionManagerImpl) { bind<TransactionManager>() }

                singleOf(::ChannelRepositoryImpl) { bind<ChannelRepository>() }
                singleOf(::UserRepositoryImpl) { bind<UserRepository>() }
                singleOf(::SessionRepositoryImpl) { bind<SessionRepository>() }

                singleOf(::ListChannelsUseCaseImpl) { bind<ListChannelsUseCase>() }
                singleOf(::RegisterUserUseCaseImpl) { bind<RegisterUserUseCase>() }
                singleOf(::UserBySessionUseCaseImpl) { bind<UserBySessionUseCase>() }
                singleOf(::LoginUseCaseImpl) { bind<LoginUseCase>() }

                singleOf(::SessionLogicImpl) { bind<SessionLogic>() }
            },
        )
    }
    install(CallLogging) { level = Level.INFO }
    install(Resources)
    install(StatusPages) {
        exception<Exception> { call, cause ->
            for (cause in cause.flattenCauses()) {
                if (cause is MissingFieldException) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        FieldErrorResponse(cause),
                    )
                    return@exception
                }
            }
            throw cause
        }
    }
    install(CORS) {
        Config.corsAllowedOrigins.forEach { allowHost(it) }
        anyMethod()
        allowHeader(HttpHeaders.AccessControlAllowOrigin)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
    }

    configureRouting()
}
