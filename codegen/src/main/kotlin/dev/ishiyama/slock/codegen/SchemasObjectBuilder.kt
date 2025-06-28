package dev.ishiyama.slock.codegen

import com.squareup.kotlinpoet.TypeSpec
import io.swagger.v3.oas.models.OpenAPI
import kotlin.collections.iterator

class SchemasObjectBuilder(
    val openAPI: OpenAPI,
    val objectName: String = "Schemas",
) {
    fun build(): TypeSpec {
        val dataclasses = mutableListOf<TypeSpec>()
        for ((name, schema) in openAPI.components.schemas) {
            val spec = DataClassBuilder(openAPI, schema, name).build()
            dataclasses.add(spec)
        }

        return TypeSpec
            .objectBuilder(objectName)
            .addTypes(dataclasses)
            .build()
    }
}
