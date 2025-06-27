package dev.ishiyama.slock.codegen

data class BuildOptions(
    val schemaFile: String,
    val outputDir: String,
    val packageName: String,
) {
    companion object {
        fun fromArgs(args: Array<String>): BuildOptions {
            val map = parseArgs(args)
            return BuildOptions(
                schemaFile = map["--schema-file"] ?: error("Missing --schema-file argument"),
                outputDir = map["--output-dir"] ?: error("Missing --output-dir argument"),
                packageName = map["--package-name"] ?: error("Missing --package-name argument"),
            )
        }

        fun parseArgs(args: Array<String>): Map<String, String> =
            args.associate {
                val parts = it.split("=", limit = 2)
                if (parts.size == 2) {
                    parts[0] to parts[1]
                } else {
                    parts[0] to ""
                }
            }
    }
}
