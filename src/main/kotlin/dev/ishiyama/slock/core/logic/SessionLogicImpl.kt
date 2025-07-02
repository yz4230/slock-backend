package dev.ishiyama.slock.core.logic

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class SessionLogicImpl : SessionLogic {
    companion object {
        const val EXPIRATION_DAYS = 30
    }

    private fun now(): Instant = Clock.System.now()

    override fun isValid(expiresAt: Instant): Boolean = expiresAt > now()

    override fun getExpirationDate(): Instant = now() + EXPIRATION_DAYS.toDuration(DurationUnit.DAYS)
}
