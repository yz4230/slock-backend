package dev.ishiyama.slock.codegen

import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.DOUBLE
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.NOTHING
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.media.Schema
import kotlin.collections.iterator

class PathsObjectBuilder(
    val openAPI: OpenAPI,
    val objectName: String = "Paths",
) {
    fun getParameterType(schema: Schema<*>): TypeName {
        val typeStr = schema.type ?: schema.types?.firstOrNull()
        return when (typeStr) {
            "null" -> NOTHING
            "boolean" -> BOOLEAN
            "number" -> DOUBLE
            "string" -> STRING
            "integer" -> INT
            "array" -> {
                val itemType = schema.items?.let { getParameterType(it) } ?: ANY
                LIST.parameterizedBy(itemType)
            }

            else -> ANY
        }
    }

    fun build(): TypeSpec {
        // ktor resource
        val resource = ClassName("io.ktor.resources", "Resource")

        val dataclasses = mutableListOf<TypeSpec>()
        for ((path, item) in openAPI.paths) {
            val operations =
                listOf(
                    item.get,
                    item.put,
                    item.post,
                    item.delete,
                    item.options,
                    item.head,
                    item.patch,
                )
            for (operation in operations) {
                val operationId = operation?.operationId ?: continue
                val name = operationId.replaceFirstChar { it.uppercase() }
                val ctor = FunSpec.constructorBuilder()
                val clazz =
                    TypeSpec
                        .classBuilder(name)
                        .addAnnotation(
                            AnnotationSpec
                                .builder(resource)
                                .addMember("path = %S", path)
                                .build(),
                        )

                operation.parameters?.let {
                    for (param in it) {
                        val paramName = param.name ?: continue
                        val paramType =
                            run {
                                val type: TypeName = getParameterType(param.schema)
                                if (param.required) type else type.copy(nullable = true)
                            }
                        clazz.addProperty(
                            PropertySpec
                                .builder(paramName, paramType)
                                .initializer(paramName)
                                .build(),
                        )
                        ctor.addParameter(paramName, paramType)
                    }
                    clazz.primaryConstructor(ctor.build())
                }

                dataclasses.add(clazz.build())
            }
        }

        return TypeSpec.Companion
            .objectBuilder(objectName)
            .addTypes(dataclasses)
            .build()
    }
}
