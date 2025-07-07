package dev.ishiyama.slock.codegen

import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.DOUBLE
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.NOTHING
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import dev.ishiyama.slock.codegen.Utils.capitalize
import dev.ishiyama.slock.codegen.Utils.escapeIdentifier
import dev.ishiyama.slock.codegen.Utils.toNullable
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.media.Schema

class TypeSpecConverter(
    val openAPI: OpenAPI,
    val schema: Schema<*>,
    val schemaName: String,
) {
    companion object {
        private val Serializable = ClassName("kotlinx.serialization", "Serializable")
        private val SerialName = ClassName("kotlinx.serialization", "SerialName")
    }

    data class Result(
        val typeName: TypeName,
        val children: List<TypeSpec>,
        val defaultValue: CodeBlock? = null,
    )

    fun buildDataClass(
        className: String,
        schema: Schema<*>,
    ): TypeSpec {
        val ctor = FunSpec.constructorBuilder()
        val clazz = TypeSpec.classBuilder(className)
        clazz.addModifiers(KModifier.DATA).addAnnotation(Serializable)

        val properties = schema.properties ?: mutableMapOf()
        val required = schema.required?.toMutableSet() ?: mutableSetOf()

        if (schema.allOf != null) {
            for (allOfSchema in schema.allOf) {
                val allOfSchema = Utils.deref(openAPI, allOfSchema)
                allOfSchema.properties?.let { properties.putAll(it) }
                allOfSchema.required?.let { required.addAll(it) }
            }
        }

        if (properties.isEmpty()) {
            // If there are no properties, we still need a primary constructor
            // to avoid "No primary constructor found" error.
            val dummyProp = "DO_NOT_USE_ME"
            clazz.addProperty(PropertySpec.builder(dummyProp, NOTHING).initializer(dummyProp).build())
            ctor.addParameter(ParameterSpec.builder(dummyProp, NOTHING).defaultValue(null).build())
        }

        for ((propName, propSchema) in properties) {
            val result = TypeSpecConverter(openAPI, propSchema, propName).convert()
            result.children.forEach { clazz.addType(it) }
            val typeName = if (propName in required) result.typeName else result.typeName.toNullable()
            clazz.addProperty(PropertySpec.builder(propName, typeName).initializer(propName).build())
            val paramSpec = ParameterSpec.builder(propName, typeName)
            result.defaultValue?.let { paramSpec.defaultValue(it) }
            ctor.addParameter(paramSpec.build())
        }
        clazz.primaryConstructor(ctor.build())

        return clazz.build()
    }

    fun buildEnum(
        className: String,
        schema: Schema<*>,
    ): TypeSpec {
        val enumValues = schema.enum ?: emptyList()
        val enumType = TypeSpec.enumBuilder(className).addAnnotation(Serializable)
        enumValues.forEach { value ->
            require(value is String)
            enumType.addEnumConstant(
                value.uppercase(),
                TypeSpec
                    .anonymousClassBuilder()
                    .addAnnotation(AnnotationSpec.builder(SerialName).addMember("%S", value).build())
                    .build(),
            )
        }
        return enumType.build()
    }

    fun convert(): Result {
        val children = mutableListOf<TypeSpec>()

        val type = schema.type ?: schema.types?.firstOrNull()
        val default = schema.default
        val typeName: TypeName
        var defaultValue: CodeBlock? = null

        when (type) {
            "null" -> {
                typeName = NOTHING
                defaultValue = CodeBlock.of("null")
            }

            "boolean" -> {
                typeName = BOOLEAN
                if (default is Boolean) defaultValue = CodeBlock.of("%L", default)
            }

            "number" -> {
                typeName = DOUBLE
                if (default is Number) defaultValue = CodeBlock.of("%L", default)
            }

            "string" -> {
                if (schema.enum != null) {
                    val enumName = schemaName.escapeIdentifier().capitalize()
                    val enumType = buildEnum(enumName, schema)
                    children.add(enumType)
                    typeName = ClassName("", enumName)
                    if (default is String) defaultValue = CodeBlock.of("%L.%L", enumName, default.uppercase())
                } else {
                    typeName = STRING
                    if (default is String) defaultValue = CodeBlock.of("%S", default)
                }
            }

            "integer" -> {
                typeName = INT
                if (default is Number) defaultValue = CodeBlock.of("%L", default)
            }

            "array" -> {
                if (schema.items == null) {
                    typeName = ANY
                } else {
                    val result = TypeSpecConverter(openAPI, schema.items, schemaName).convert()
                    children.addAll(result.children)
                    typeName = LIST.parameterizedBy(result.typeName)
                }
            }

            "object" -> {
                val className = schemaName.escapeIdentifier().capitalize()
                val clazz = buildDataClass(className, schema)
                children.add(clazz)
                typeName = ClassName("", className)
            }

            else -> {
                typeName = schema.`$ref`?.let { Utils.getRefTypeName(it) } ?: ANY
            }
        }

        return Result(
            typeName = typeName,
            children = children,
            defaultValue = defaultValue,
        )
    }
}
