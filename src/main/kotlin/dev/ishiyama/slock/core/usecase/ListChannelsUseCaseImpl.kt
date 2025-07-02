package dev.ishiyama.slock.core.usecase

import dev.ishiyama.slock.core.repository.ChannelRepository
import dev.ishiyama.slock.core.repository.TransactionManager
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ListChannelsUseCaseImpl :
    ListChannelsUseCase,
    KoinComponent {
    private val transactionManager by inject<TransactionManager>()
    private val repository by inject<ChannelRepository>()

    override fun execute(): ListChannelsUseCase.Output {
        val channels = transactionManager.start { repository.list() }
        return ListChannelsUseCase.Output(
            channels =
                channels.map {
                    ListChannelsUseCase.Output.Channel(
                        id = it.id,
                        name = it.name,
                        description = it.description,
                        isDirect = it.isDirect,
                        createdAt = it.createdAt,
                        updatedAt = it.updatedAt,
                    )
                },
        )
    }
}
