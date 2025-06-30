package dev.ishiyama.slock.codegen

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.media.Schema

object Utils {
    fun String.upperPascal(): String =
        this
            .split('_', '-')
            .joinToString("") { it.replaceFirstChar { c -> c.uppercase() } }

    fun getRefTypeName(ref: String): TypeName? {
        val parts = ref.split('/')
        if (parts.getOrNull(0) == "#") {
            if (parts.getOrNull(1) == "components") {
                val rest =
                    parts
                        .slice(2..parts.lastIndex)
                        .map { it.replaceFirstChar { c -> c.titlecase() } }
                return ClassName("", rest)
            }
        }
        return null
    }

    fun getRefSchema(
        openAPI: OpenAPI,
        ref: String,
    ): Schema<*>? {
        val parts = ref.split('/')
        if (parts.getOrNull(0) == "#") {
            if (parts.getOrNull(1) == "components") {
                if (parts.getOrNull(2) == "schemas") {
                    val refName = parts.getOrNull(3) ?: return null
                    val schema = openAPI.components?.schemas?.get(refName) ?: return null
                    return if (schema.`$ref` == null) schema else getRefSchema(openAPI, schema.`$ref`)
                }
            }
        }
        return null
    }
}
