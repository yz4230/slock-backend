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

class DataClassBuilder(
    val openAPI: OpenAPI,
    val schema: Schema<*>,
    val className: String,
) {
    private val children: MutableList<TypeSpec> = mutableListOf()

    init {
        val type = schema.type ?: schema.types?.firstOrNull()
        if (type != "object") {
            throw IllegalArgumentException("DataClassBuilder can only be used for object schemas, got type: $type")
        }
    }

    fun deref(schema: Schema<*>): Schema<*> {
        if (schema.`$ref` == null) return schema
        val refSchema = Utils.getRefSchema(openAPI, schema.`$ref`)
        return deref(checkNotNull(refSchema))
    }

    fun getChildType(
        name: String,
        schema: Schema<*>,
    ): TypeName {
        val type = schema.type ?: schema.types?.firstOrNull()
        return when (type) {
            "null" -> NOTHING
            "boolean" -> BOOLEAN
            "number" -> DOUBLE
            "string" -> STRING
            "integer" -> INT
            "array" -> LIST.parameterizedBy(schema.items?.let { getChildType(name, it) } ?: ANY)
            "object" -> {
                val child = DataClassBuilder(openAPI, schema, name).build()
                children.add(child)
                return ClassName("", checkNotNull(child.name))
            }

            else -> {
                schema.`$ref`?.let { return Utils.getRefTypeName(it) ?: ANY }
                ANY
            }
        }
    }

    fun build(): TypeSpec {
        val serializable = ClassName("kotlinx.serialization", "Serializable")
        val className = className.replaceFirstChar { it.titlecase() }
        val ctor = FunSpec.constructorBuilder()
        val clazz =
            TypeSpec
                .classBuilder(className)
                .addModifiers(KModifier.DATA)
                .addAnnotation(serializable)

        val properties = schema.properties ?: mutableMapOf()

        schema.allOf?.let { allOf ->
            for (allOfSchema in allOf) {
                deref(allOfSchema).properties?.let { properties.putAll(it) }
            }
        }

        if (properties.isNotEmpty()) {
            for ((propName, propSchema) in properties) {
                val type = getChildType(propName, propSchema)
                val propSpec = PropertySpec.builder(propName, type).initializer(propName).build()
                clazz.addProperty(propSpec)
                ctor.addParameter(propName, type)
            }
            for (child in children) clazz.addType(child)
            clazz.primaryConstructor(ctor.build())
        }

        return clazz.build()
    }
}
