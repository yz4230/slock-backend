plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    kotlin("plugin.serialization") version libs.versions.kotlin.version
}

group = "dev.ishiyama"
version = "0.0.1"

application {
    mainClass = "dev.ishiyama.slock.ApplicationKt"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.logback.classic)

    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.config.yaml)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.resources)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.server.cors)

    implementation(libs.koin.ktor)
    implementation(libs.koin.logger.slf4j)

    implementation(libs.exposed.core)
    implementation(libs.exposed.crypt)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.kotlin.datetime)
    implementation(libs.exposed.migration)
    implementation(libs.flyway.core)
    implementation(libs.flyway.database.postgresql)

    implementation(libs.postgresql)
    implementation(libs.dotenv.kotlin)
    implementation(libs.spring.security.crypto)

    testImplementation(kotlin("test"))
    testImplementation(libs.koin.test.junit5)
}

kotlin {
    compilerOptions {
        // @see https://kotlinlang.org/docs/type-aliases.html
        freeCompilerArgs.add("-Xnested-type-aliases")
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.register<JavaExec>("generateMigration") {
    group = "database"
    description = "Generates a database migration script."

    mainClass.set("dev.ishiyama.slock.scripts.GenerateMigrationKt")
    classpath = sourceSets.main.get().runtimeClasspath
}

tasks.register<JavaExec>("migrateDatabase") {
    group = "database"
    description = "Migrates the database schema."

    mainClass.set("dev.ishiyama.slock.scripts.MigrateDatabaseKt")
    classpath = sourceSets.main.get().runtimeClasspath
}

tasks.register<JavaExec>("generateOpenApi") {
    group = "codegen"
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
