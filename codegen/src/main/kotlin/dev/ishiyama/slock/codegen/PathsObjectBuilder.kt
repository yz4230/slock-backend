package dev.ishiyama.slock.codegen

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import io.swagger.v3.oas.models.OpenAPI
import kotlin.collections.iterator

class PathsObjectBuilder(
    val openAPI: OpenAPI,
    val objectName: String = "Paths",
) {
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

                for (param in operation.parameters.orEmpty()) {
                    val paramName = param.name ?: continue
                    var paramType = SchemaBuilder(openAPI).getTypeName(param.schema ?: continue)
                    if (!param.required) {
                        paramType = paramType.copy(nullable = true)
                    }
                    clazz.addProperty(
                        PropertySpec
                            .builder(paramName, paramType)
                            .initializer(paramName)
                            .build(),
                    )
                    ctor.addParameter(paramName, paramType)
                }

                dataclasses.add(
                    clazz
                        .primaryConstructor(ctor.build())
                        .build(),
                )
            }
        }

        return TypeSpec.Companion
            .objectBuilder(objectName)
            .addTypes(dataclasses)
            .build()
    }
}
