package dev.ishiyama

import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val appModule =
    module {
//        singleOf(::InMemoryPetRepository) { bind<PetRepository>() }
        singleOf(::DatabasePetRepository) { bind<PetRepository>() }
        singleOf(::PetService)
    }
