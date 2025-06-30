package dev.ishiyama.slock.codegen

import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.DOUBLE
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STRING
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.media.Schema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TypeSpecConverterTest {
    @Test
    fun testBasic() {
        val openAPI = OpenAPI()
        val schema =
            Schema<Any>()
                .apply {
                    type = "object"
                    properties =
                        mapOf(
                            "id" to Schema<Any>().apply { type = "integer" },
                            "name" to Schema<Any>().apply { type = "string" },
                            "isActive" to Schema<Any>().apply { type = "boolean" },
                            "tags" to
                                Schema<Any>().apply {
                                    type = "array"
                                    items =
                                        Schema<Any>().apply {
                                            type = "object"
                                            properties =
                                                mapOf(
                                                    "id" to Schema<Any>().apply { type = "integer" },
                                                    "name" to Schema<Any>().apply { type = "string" },
                                                )
                                            required = listOf("id", "name")
                                        }
                                },
                            "details" to
                                Schema<Any>().apply {
                                    type = "object"
                                    properties =
                                        mapOf(
                                            "description" to Schema<Any>().apply { type = "string" },
                                            "count" to Schema<Any>().apply { type = "integer" },
                                        )
                                    required = listOf("description", "count")
                                },
                        )
                    required = listOf("id", "name", "isActive", "tags", "details")
                }
        // Example object:
        // {
        //   "id": 1,
        //   "name": "Test",
        //   "isActive": true,
        //   "tags": [
        //      { "id": 1, "name": "Tag1" },
        //      { "id": 2, "name": "Tag2" }
        //   ],
        //   "details": {
        //      "description": "Detailed info",
        //      "count": 42
        //   }
        // }
        val className = "TestDataClass"
        val typeSpec = TypeSpecConverter(openAPI, schema, className).convert().children.first()

        assertEquals(className, typeSpec.name)
        assertEquals(5, typeSpec.propertySpecs.size)
        val props = typeSpec.propertySpecs.associateBy { it.name }
        assertEquals(INT, props["id"]?.type)
        assertEquals(STRING, props["name"]?.type)
        assertEquals(BOOLEAN, props["isActive"]?.type)
        assertEquals(LIST.parameterizedBy(ClassName("", "Tags")), props["tags"]?.type)
        assertEquals(ClassName("", "Details"), props["details"]?.type)
        val tagsType = typeSpec.typeSpecs.firstOrNull { it.name == "Tags" }
        assertNotNull(tagsType)
        val tagProps = tagsType.propertySpecs.associateBy { it.name }
        assertEquals(2, tagProps.size)
        assertEquals(INT, tagProps["id"]?.type)
        assertEquals(STRING, tagProps["name"]?.type)
        val detailsType = typeSpec.typeSpecs.firstOrNull { it.name == "Details" }
        assertNotNull(detailsType)
        val detailsProps = detailsType.propertySpecs.associateBy { it.name }
        assertEquals(2, detailsProps.size)
        assertEquals(STRING, detailsProps["description"]?.type)
        assertEquals(INT, detailsProps["count"]?.type)
    }

    @Test
    fun testNested() {
        val openAPI = OpenAPI()
        openAPI.components = Components()
        val schema =
            Schema<Any>()
                .apply {
                    type = "object"
                    properties =
                        mapOf(
                            "parent" to
                                Schema<Any>().apply {
                                    type = "object"
                                    properties =
                                        mapOf(
                                            "child" to
                                                Schema<Any>().apply {
                                                    type = "object"
                                                    properties =
                                                        mapOf(
                                                            "id" to Schema<Any>().apply { type = "integer" },
                                                            "name" to Schema<Any>().apply { type = "string" },
                                                        )
                                                    required = listOf("id", "name")
                                                },
                                        )
                                    required = listOf("child")
                                },
                        )
                    required = listOf("parent")
                }
        // Example object:
        // {
        //   "parent": {
        //     "child": {
        //         "id": 1,
        //         "name": "Child"
        //     }
        //   }
        // }

        val className = "NestedDataClass"
        openAPI.components.schemas = mapOf(className to schema)

        val typeSpec = TypeSpecConverter(openAPI, schema, className).convert().children.first()

        val parent = typeSpec.typeSpecs.firstOrNull { it.name == "Parent" }
        assertNotNull(parent)
        val child = parent.typeSpecs.firstOrNull { it.name == "Child" }
        assertNotNull(child)
        assertEquals(2, child.propertySpecs.size)
        val props = child.propertySpecs.associateBy { it.name }
        assertEquals(INT, props["id"]?.type)
        assertEquals(STRING, props["name"]?.type)
    }

    @Test
    fun testAllOf() {
        val openAPI = OpenAPI()
        openAPI.components = Components()

        val typeSpec =
            run {
                val schema1 =
                    Schema<Any>()
                        .apply {
                            type = "object"
                            properties =
                                mapOf(
                                    "property1" to Schema<Any>().apply { type = "string" },
                                    "property2" to Schema<Any>().apply { type = "integer" },
                                )
                            required = listOf("property1", "property2")
                        }
                val schema2 =
                    Schema<Any>()
                        .apply {
                            type = "object"
                            properties =
                                mapOf(
                                    "property3" to Schema<Any>().apply { type = "boolean" },
                                    "property4" to Schema<Any>().apply { type = "number" },
                                )
                            required = listOf("property3", "property4")
                        }
                val schema3 =
                    Schema<Any>()
                        .apply {
                            type = "object"
                            allOf =
                                listOf(
                                    schema1,
                                    Schema<Any>().apply { `$ref` = "#/components/schemas/Schema2" },
                                )
                        }

                openAPI.components.schemas = mapOf("Schema2" to schema2, "Schema3" to schema3)
                TypeSpecConverter(openAPI, schema3, "Schema3").convert().children.first()
            }
        // Example object:
        // {
        //   "property1": "value1",
        //   "property2": 42,
        //   "property3": true,
        //   "property4": 3.14
        // }

        val props = typeSpec.propertySpecs.associateBy { it.name }
        assertEquals(4, props.size)
        assertEquals(STRING, props["property1"]?.type)
        assertEquals(INT, props["property2"]?.type)
        assertEquals(BOOLEAN, props["property3"]?.type)
        assertEquals(DOUBLE, props["property4"]?.type)
    }
}
