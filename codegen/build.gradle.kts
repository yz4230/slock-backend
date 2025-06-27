plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

group = "dev.ishiyama"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.slf4j.simple)

    implementation("com.squareup:kotlinpoet:2.2.0")
    implementation("io.swagger.parser.v3:swagger-parser:2.1.30")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("dev.ishiyama.slock.codegen.MainKt")
}
