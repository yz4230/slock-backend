package dev.ishiyama.slock

import io.github.cdimascio.dotenv.dotenv

object Config {
    val databaseUrl: String by lazy { mustGet("DATABASE_URL") }
    val databaseUser: String by lazy { mustGet("DATABASE_USER") }
    val databasePassword: String by lazy { mustGet("DATABASE_PASSWORD") }
    val corsAllowedOrigins: List<String> by lazy { get("CORS_ALLOWED_ORIGINS")?.split(",") ?: emptyList() }

    private val filenames = listOf(".env.local", ".env")
    private val dotenv =
        filenames.map {
            dotenv {
                filename = it
                ignoreIfMissing = true
            }
        }

    private fun get(key: String): String? {
        for (env in dotenv) env[key]?.let { return it }
        return null
    }

    private fun mustGet(key: String): String = get(key) ?: throw IllegalStateException("Environment variable $key is not set")
}
