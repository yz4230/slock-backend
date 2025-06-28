package dev.ishiyama.slock

import io.github.cdimascio.dotenv.dotenv

object Config {
    val databaseUrl: String by lazy { mustGet("DATABASE_URL") }
    val databaseUser: String by lazy { mustGet("DATABASE_USER") }
    val databasePassword: String by lazy { mustGet("DATABASE_PASSWORD") }

    private val filenames = listOf(".env.local", ".env")
    private val dotenv =
        filenames.map {
            dotenv {
                filename = it
                ignoreIfMissing = true
            }
        }

    private fun mustGet(key: String): String {
        for (env in dotenv) env[key]?.let { return it }
        throw IllegalStateException("Environment variable $key is not set")
    }
}
