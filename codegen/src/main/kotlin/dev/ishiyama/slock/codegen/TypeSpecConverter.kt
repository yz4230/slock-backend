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
import dev.ishiyama.slock.codegen.Utils.upperPascal
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

    fun deref(schema: Schema<*>): Schema<*> {
        if (schema.`$ref` == null) return schema
        val refSchema = Utils.getRefSchema(openAPI, schema.`$ref`)
        return deref(checkNotNull(refSchema))
    }

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
                val allOfSchema = deref(allOfSchema)
                allOfSchema.properties?.let { properties.putAll(it) }
                allOfSchema.required?.let { required.addAll(it) }
            }
        }

        if (properties.isNotEmpty()) {
            for ((propName, propSchema) in properties) {
                val result = TypeSpecConverter(openAPI, propSchema, propName).convert()
                result.children.forEach { clazz.addType(it) }
                val typeName = if (propName in required) result.typeName else result.typeName.copy(nullable = true)
                clazz.addProperty(PropertySpec.builder(propName, typeName).initializer(propName).build())
                val paramSpec = ParameterSpec.builder(propName, typeName)
                result.defaultValue?.let { paramSpec.defaultValue(it) }
                ctor.addParameter(paramSpec.build())
            }
            clazz.primaryConstructor(ctor.build())
        }

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
                    val enumName = schemaName.upperPascal()
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
                val className = schemaName.upperPascal()
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
