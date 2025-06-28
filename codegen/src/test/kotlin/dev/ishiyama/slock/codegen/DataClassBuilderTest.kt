package dev.ishiyama.slock.codegen

import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.STRING
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.media.Schema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class DataClassBuilderTest {
    @Test
    fun testDataClassBuilder() {
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
                                },
                            "nested1" to
                                Schema<Any>().apply {
                                    type = "object"
                                    properties =
                                        mapOf(
                                            "nested2" to Schema<Any>().apply { type = "object" },
                                        )
                                },
                        )
                }
        val className = "TestDataClass"
        val builder = DataClassBuilder(openAPI, schema, className)
        val typeSpec = builder.build()

        assertNotNull(typeSpec.primaryConstructor)
        assertEquals(className, typeSpec.name)
        assertEquals(6, typeSpec.propertySpecs.size)
        val props = typeSpec.propertySpecs.associateBy { it.name }
        assertEquals(props["id"]?.type, INT)
        assertEquals(props["name"]?.type, STRING)
        assertEquals(props["isActive"]?.type, BOOLEAN)
    }
}
