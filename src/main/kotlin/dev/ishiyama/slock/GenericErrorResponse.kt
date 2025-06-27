package dev.ishiyama.slock

import kotlinx.serialization.Serializable

@Serializable
class GenericErrorResponse(
    override val code: Int,
    override val message: String,
) : ErrorResponse
