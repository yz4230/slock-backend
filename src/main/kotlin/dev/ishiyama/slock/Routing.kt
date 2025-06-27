package dev.ishiyama.slock

import dev.ishiyama.slock.core.repository.ChannelRepository
import dev.ishiyama.slock.core.repository.TransactionManager
import dev.ishiyama.slock.generated.Models
import dev.ishiyama.slock.generated.Paths
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

fun Application.configureRouting() {
    configurePetStoreRouting()

    val transactionManager by inject<TransactionManager>()
    val channelRepository by inject<ChannelRepository>()

    routing {
        get("/") {
            call.respondText("Hello, World!")
        }
        post<Paths.CreateChannel> { params ->
            val body = call.receive<Models.CreateChannelRequest>()
            val created =
                transactionManager.start {
                    channelRepository.create(
                        ChannelRepository.CreateChannel(
                            title = body.channel.name,
                            description = body.channel.description,
                            isDm = body.channel.isDirect,
                        ),
                    )
                }
            call.respond(
                HttpStatusCode.Created,
                Models.CreateChannelResponse(
                    channel =
                        Models.Channel(
                            id = created.id,
                            name = created.title,
                            description = created.description,
                            isDirect = created.isDm,
                        ),
                ),
            )
        }
        get<Paths.ListChannels> {
            val channels = transactionManager.start { channelRepository.list() }
            call.respond(
                Models.ListChannelsResponse(
                    items =
                        channels.map {
                            Models.Channel(
                                id = it.id,
                                name = it.title,
                                description = it.description,
                                isDirect = it.isDm,
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
        post<Paths.Login> {
        }
        get<Paths.Me> {
        }
    }
}
