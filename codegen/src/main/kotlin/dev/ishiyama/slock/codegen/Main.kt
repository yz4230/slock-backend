package dev.ishiyama.slock.codegen

import io.swagger.parser.OpenAPIParser
import io.swagger.v3.parser.core.models.ParseOptions

fun main(args: Array<String>) {
    println("👋 Hello, Slock Codegen!")
    val options = BuildOptions.fromArgs(args)

    println("⚙️ OpenAPI Schema: ${options.schemaFile}")
    println("⚙️ Output directory: ${options.outputDir}")

    val parseOptions = ParseOptions().apply { isResolve = true }
    val result = OpenAPIParser().readLocation(options.schemaFile, null, parseOptions)
    val openAPI = result.openAPI

    SchemasGenerator(openAPI, options).generate()
    PathsGenerator(openAPI, options).generate()

    println("✅ Code generation finished.")
}
