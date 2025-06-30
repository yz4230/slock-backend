package dev.ishiyama.slock.codegen

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import dev.ishiyama.slock.codegen.Utils.toNullable
import dev.ishiyama.slock.codegen.Utils.upperPascal
import io.swagger.v3.oas.models.OpenAPI
import kotlin.collections.iterator

class PathsObjectBuilder(
    val openAPI: OpenAPI,
    val objectName: String = "Paths",
) {
    companion object {
        private val Resource = ClassName("io.ktor.resources", "Resource")
    }

    fun build(): TypeSpec {
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
                val ctor = FunSpec.constructorBuilder()
                val clazz =
                    TypeSpec
                        .classBuilder(operationId.upperPascal())
                        .addAnnotation(
                            AnnotationSpec
                                .builder(Resource)
                                .addMember("path = %S", path)
                                .build(),
                        )

                if (operation.parameters != null) {
                    for (param in operation.parameters) {
                        val paramName = param.name ?: error("Parameter name is required for $operationId")
                        val result = TypeSpecConverter(openAPI, param.schema, paramName).convert()
                        result.children.forEach { clazz.addType(it) }
                        val typeName = if (param.required == true) result.typeName else result.typeName.toNullable()
                        clazz.addProperty(PropertySpec.builder(paramName, typeName).initializer(paramName).build())
                        val paramSpec = ParameterSpec.builder(paramName, typeName)
                        result.defaultValue?.let { paramSpec.defaultValue(it) }
                        ctor.addParameter(paramSpec.build())
                    }
                    clazz.primaryConstructor(ctor.build())
                }

                dataclasses.add(clazz.build())
            }
        }

        return TypeSpec.objectBuilder(objectName).addTypes(dataclasses).build()
    }
}
