plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    kotlin("plugin.serialization") version libs.versions.kotlin.version
}

group = "dev.ishiyama"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.slf4j.simple)

    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.logback.classic)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.config.yaml)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.resources)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.koin.ktor)
    implementation(libs.koin.logger.slf4j)

    implementation(libs.exposed.core)
    implementation(libs.exposed.crypt)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.kotlin.datetime)

    implementation(libs.postgresql)
}

tasks.register<JavaExec>("generateCode") {
    group = "build"
    description = "Generates source code from an OpenAPI schema."

    val codegen = project(":codegen")
    mainClass.set(codegen.the<JavaApplication>().mainClass)
    classpath =
        codegen.sourceSets.main
            .get()
            .runtimeClasspath

    val schemaFile = rootProject.file("slock-openapi/tsp-output/schema/openapi.yaml").absolutePath
    val outputDir = rootProject.file("src/main/kotlin").absolutePath
    val packageName = "dev.ishiyama.slock.generated"
    args =
        listOf(
            "--schema-file=$schemaFile",
            "--output-dir=$outputDir",
            "--package-name=$packageName",
        )
}
