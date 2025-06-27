package dev.ishiyama.slock.codegen

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import io.swagger.v3.oas.models.OpenAPI
import java.io.File

class ModelsGenerator(
    val openAPI: OpenAPI,
    val buildOptions: BuildOptions,
) {
    fun generate() {
        val annot =
            AnnotationSpec
                .builder(ClassName("", "Suppress"))
                .addMember("%S", "ktlint")
                .build()

        val modelsObject = ModelsObjectBuilder(openAPI).build()
        val modelsKt =
            FileSpec.Companion
                .builder(buildOptions.packageName, "Models")
                .addAnnotation(annot)
                .addType(modelsObject)
                .build()

        modelsKt.writeTo(File(buildOptions.outputDir))
    }
}
