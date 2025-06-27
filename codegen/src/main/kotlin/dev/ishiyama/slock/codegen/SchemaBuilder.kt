package dev.ishiyama.slock.codegen

import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.DOUBLE
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.KModifier
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

class SchemaBuilder(
    val openAPI: OpenAPI,
) {
    fun getTypeStr(schema: Schema<*>): String? = schema.type ?: schema.types?.firstOrNull() ?: schema.`$ref`

    fun getTypeName(schema: Schema<*>): TypeName {
        val typeStr = getTypeStr(schema) ?: return ANY
        return when (typeStr) {
            "null" -> NOTHING
            "boolean" -> BOOLEAN
            "number" -> DOUBLE
            "string" -> STRING
            "integer" -> INT
            "array" ->
                LIST.parameterizedBy(getTypeName(schema.items))

            else -> {
                val prefix = "#/components/schemas/"
                if (typeStr.startsWith(prefix)) {
                    val refName = typeStr.removePrefix(prefix)
                    ClassName("", refName)
                } else {
                    ANY // Fallback for unsupported types
                }
            }
        }
    }

    fun objectToDataClass(
        name: String,
        schema: Schema<*>,
    ): TypeSpec {
        val serializable = ClassName("kotlinx.serialization", "Serializable")
        val constructorBuilder = FunSpec.Companion.constructorBuilder()
        val classBuilder = TypeSpec.Companion.classBuilder(name)
        classBuilder.addModifiers(KModifier.DATA)

        for ((propName, propSchema) in schema.properties.orEmpty()) {
            val propTypeStr = getTypeStr(propSchema)
            when (propTypeStr) {
                "object" -> {
                    // Recursively build nested object types
                    val nestedType = toTypeSpec(propName.replaceFirstChar { it.uppercase() }, propSchema)
                    classBuilder.addType(nestedType)

                    val type = ClassName("", nestedType.name!!)
                    constructorBuilder.addParameter(propName, type)
                    val propertyBuilder = PropertySpec.Companion.builder(propName, type)
                    propertyBuilder.initializer(propName)
                    classBuilder.addProperty(propertyBuilder.build())
                }

                else -> {
                    val type = getTypeName(propSchema)
                    constructorBuilder.addParameter(propName, type)
                    val propertyBuilder = PropertySpec.Companion.builder(propName, type)
                    propertyBuilder.initializer(propName)
                    classBuilder.addProperty(propertyBuilder.build())
                }
            }
        }

        return classBuilder
            .primaryConstructor(constructorBuilder.build())
            .addAnnotation(serializable)
            .build()
    }

    fun toTypeSpec(
        name: String,
        schema: Schema<*>,
    ): TypeSpec {
        val typeStr = getTypeStr(schema)
        val serializable = ClassName("kotlinx.serialization", "Serializable")

        return when (typeStr) {
            "object" -> objectToDataClass(name, schema)
            // "null", "boolean", "number", "string", "integer", "array"
            else -> {
                val type = getTypeName(schema)
                TypeSpec.Companion
                    .classBuilder(name)
                    .addModifiers(KModifier.VALUE)
                    .addProperty(
                        PropertySpec.Companion
                            .builder("value", type)
                            .initializer("value")
                            .build(),
                    ).primaryConstructor(
                        FunSpec.Companion
                            .constructorBuilder()
                            .addParameter("value", type)
                            .build(),
                    ).addAnnotation(serializable)
                    .build()
            }
        }
    }
}
