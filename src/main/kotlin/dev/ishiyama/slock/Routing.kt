package dev.ishiyama.slock

import dev.ishiyama.slock.core.repository.ChannelRepository
import dev.ishiyama.slock.core.repository.transaction.TransactionManager
import dev.ishiyama.slock.core.usecase.ListChannelsUseCase
import dev.ishiyama.slock.core.usecase.LoginUseCase
import dev.ishiyama.slock.core.usecase.RegisterUserUseCase
import dev.ishiyama.slock.core.usecase.UserBySessionUseCase
import dev.ishiyama.slock.generated.Paths
import dev.ishiyama.slock.generated.Schemas
import dev.ishiyama.slock.petstore.configurePetStoreRouting
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.request.receive
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject
import kotlin.getValue

fun Application.configureRouting() {
    configurePetStoreRouting()

    val transactionManager by inject<TransactionManager>()
    val channelRepository by inject<ChannelRepository>()

    routing {
        get("/") {
            call.respondText("Hello, World!")
        }
        post<Paths.CreateChannel> { params ->
            val body = call.receive<Schemas.CreateChannelRequest>()
            val created =
                transactionManager.start {
                    channelRepository.create(
                        ChannelRepository.CreateChannel(
                            name = body.channel.name,
                            description = body.channel.description,
                            isDirect = body.channel.isDirect,
                        ),
                    )
                }
            call.respond(
                HttpStatusCode.Created,
                Schemas.CreateChannelResponse(
                    channel =
                        Schemas.Channel(
                            id = created.id,
                            name = created.name,
                            description = created.description,
                            isDirect = created.isDirect,
                            createdAt = created.createdAt.toString(),
                            updatedAt = created.updatedAt.toString(),
                        ),
                ),
            )
        }
        get<Paths.ListChannels> {
            val useCase by inject<ListChannelsUseCase>()
            val output = useCase.execute()
            call.respond(
                Schemas.ListChannelsResponse(
                    items =
                        output.channels.map {
                            Schemas.Channel(
                                id = it.id,
                                name = it.name,
                                description = it.description,
                                isDirect = it.isDirect,
                                createdAt = it.createdAt.toString(),
                                updatedAt = it.updatedAt.toString(),
                            )
                        },
                ),
            )
        }
        get<Paths.ListMessages> { params ->
            call.respondText(
                "List messages for channel: ${params.channelId}, thread: ${params.threadId ?: "none"}",
            )
        }
        post<Paths.Register> {
            val body = call.receive<Schemas.RegisterRequest>()
            val useCase by inject<RegisterUserUseCase>()
            val output =
                useCase.execute(
                    RegisterUserUseCase.Input(
                        name = body.name,
                        displayName = body.displayName,
                        email = body.email,
                        password = body.password,
                    ),
                )
            call.respond(
                HttpStatusCode.Created,
                Schemas.RegisterResponse(
                    user =
                        Schemas.User(
                            id = output.user.id,
                            name = output.user.name,
                            email = output.user.email,
                            displayName = output.user.displayName,
                            createdAt = output.user.createdAt.toString(),
                            updatedAt = output.user.updatedAt.toString(),
                        ),
                    token = output.sessionId,
                ),
            )
        }
        post<Paths.Login> {
            val useCase by inject<LoginUseCase>()
            val body = call.receive<Schemas.LoginRequest>()

            val output =
                useCase.execute(
                    LoginUseCase.Input(
                        email = body.name,
                        password = body.password,
                    ),
                )

            call.respond(
                Schemas.LoginResponse(
                    user =
                        Schemas.User(
                            id = output.user.id,
                            name = output.user.name,
                            email = output.user.email,
                            displayName = output.user.displayName,
                            createdAt = output.user.createdAt.toString(),
                            updatedAt = output.user.updatedAt.toString(),
                        ),
                    token = output.sessionId,
                ),
            )
        }
        get<Paths.Me> {
            val useCase by inject<UserBySessionUseCase>()
            val sessionId = call.request.headers["Authorization"]?.removePrefix("Bearer ")

            if (sessionId.isNullOrBlank()) {
                call.respond(HttpStatusCode.Unauthorized, "Missing or invalid token")
                return@get
            }

            val output =
                useCase.execute(
                    UserBySessionUseCase.Input(
                        sessionId = sessionId,
                    ),
                )

            if (output.user == null) {
                call.respond(HttpStatusCode.Unauthorized, "Invalid session")
            } else {
                call.respond(
                    Schemas.User(
                        id = output.user.id,
                        name = output.user.name,
                        displayName = output.user.displayName,
                        email = output.user.email,
                        createdAt = output.user.createdAt.toString(),
                        updatedAt = output.user.updatedAt.toString(),
                    ),
                )
            }
        }
    }
}
