package dev.ishiyama.slock.core.logic

import kotlinx.datetime.Instant

interface SessionLogic {
    fun isValid(expiresAt: Instant): Boolean

    fun getExpirationDate(): Instant
}
