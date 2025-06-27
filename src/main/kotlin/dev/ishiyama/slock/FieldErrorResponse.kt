@file:OptIn(ExperimentalSerializationApi::class)

package dev.ishiyama.slock

import io.ktor.http.HttpStatusCode
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.Serializable

@Serializable
class FieldErrorResponse(
    override val code: Int,
    override val message: String,
    val fields: List<Field> = emptyList(),
) : ErrorResponse {
    @Serializable
    class Field(
        val field: String,
        val message: String,
    )

    constructor(exception: MissingFieldException) : this(
        code = HttpStatusCode.Companion.BadRequest.value,
        message = "Missing required fields",
        fields =
            exception.missingFields.map { field ->
                Field(
                    field = field,
                    message = "Field '$field' is required.",
                )
            },
    )
}
