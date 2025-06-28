package dev.ishiyama.slock.codegen

import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STRING
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.SpecVersion
import io.swagger.v3.oas.models.media.Schema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class SchemasObjectBuilderTest {
    @Test
    fun testSimple() {
        // Assuming you have a mock OpenAPI object to pass
        val openAPI = OpenAPI(SpecVersion.V31)
        val testSchema =
            Schema<Any>().apply {
                type = "object"
                properties =
                    mapOf(
                        "id" to Schema<Any>().apply { type = "integer" },
                        "name" to Schema<Any>().apply { type = "string" },
                        "type" to Schema<Any>().apply { type = "string" },
                        "age" to Schema<Any>().apply { type = "integer" },
                    )
            }
        openAPI.components = Components()
        openAPI.components.schemas = mutableMapOf("TestSchema" to testSchema)

        val builder = SchemasObjectBuilder(openAPI)
        val modelsObject = builder.build()

        // check Models.TestSchema.id -> Int
        assertEquals("Models", modelsObject.name)
        val spec = modelsObject.typeSpecs.firstOrNull { it.name == "TestSchema" }
        assertNotNull(spec) // Should not be null
        assert(spec.annotations.size == 1) // @Serializable
        val props = spec.propertySpecs.associateBy { it.name }
        assertEquals(4, props.size)
        assertEquals(props["id"]?.type, INT)
        assertEquals(props["name"]?.type, STRING)
        assertEquals(props["type"]?.type, STRING)
        assertEquals(props["age"]?.type, INT)
    }

    @Test
    fun testLists() {
        val openAPI = OpenAPI(SpecVersion.V31)
        val testSchema =
            Schema<Any>().apply {
                type = "object"
                properties =
                    mapOf(
                        "items" to
                            Schema<Any>().apply {
                                type = "array"
                                items = Schema<Any>().apply { type = "string" }
                            },
                        "items2d" to
                            Schema<Any>().apply {
                                type = "array"
                                items =
                                    Schema<Any>().apply {
                                        type = "array"
                                        items = Schema<Any>().apply { type = "integer" }
                                    }
                            },
                    )
            }
        openAPI.components = Components()
        openAPI.components.schemas = mutableMapOf("TestSchema" to testSchema)

        val builder = SchemasObjectBuilder(openAPI)
        val modelsObject = builder.build()
        val spec = modelsObject.typeSpecs.firstOrNull { it.name == "TestSchema" }
        assertNotNull(spec)
        val props = spec.propertySpecs.associateBy { it.name }
        val itemType = props["items"]?.type // List<String>
        assertEquals(itemType, LIST.parameterizedBy(STRING))
        val item2dType = props["items2d"]?.type // List<List<Int>>
        assertEquals(item2dType, LIST.parameterizedBy(LIST.parameterizedBy(INT)))
    }

    @Test
    fun testObjects() {
        val openAPI = OpenAPI(SpecVersion.V31)
        val testSchema =
            Schema<Any>().apply {
                type = "object"
                properties =
                    mapOf(
                        "nested" to
                            Schema<Any>().apply {
                                type = "object"
                                properties =
                                    mapOf(
                                        "id" to Schema<Any>().apply { type = "integer" },
                                        "name" to Schema<Any>().apply { type = "string" },
                                    )
                            },
                        "nested2" to
                            Schema<Any>().apply {
                                type = "object"
                                properties =
                                    mapOf(
                                        "nested3" to
                                            Schema<Any>().apply {
                                                type = "object"
                                                properties =
                                                    mapOf(
                                                        "id" to Schema<Any>().apply { type = "integer" },
                                                        "name" to Schema<Any>().apply { type = "string" },
                                                    )
                                            },
                                    )
                            },
                    )
            }

        openAPI.components = Components()
        openAPI.components.schemas = mutableMapOf("TestSchema" to testSchema)
        val builder = SchemasObjectBuilder(openAPI)
        val modelsObject = builder.build()
        val spec = modelsObject.typeSpecs.firstOrNull { it.name == "TestSchema" }
        assertNotNull(spec)
        val types = spec.typeSpecs.associateBy { it.name }
        assertEquals(2, types.size) // nested and nested2
        val nested = types["Nested"]
        assertNotNull(nested)
        assertEquals(2, nested.propertySpecs.size)
        assertEquals("id", nested.propertySpecs[0].name)
        assertEquals("name", nested.propertySpecs[1].name)
        val nested2 = types["Nested2"]
        assertNotNull(nested2)
        assertEquals(1, nested2.propertySpecs.size)
        assertEquals("nested3", nested2.propertySpecs[0].name)
        val nested3 = nested2.typeSpecs.firstOrNull { it.name == "Nested3" }
        assertNotNull(nested3)
        assertEquals(2, nested3.propertySpecs.size)
        assertEquals("id", nested3.propertySpecs[0].name)
        assertEquals("name", nested3.propertySpecs[1].name)
    }
}
