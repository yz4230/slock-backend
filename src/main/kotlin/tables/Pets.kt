package dev.ishiyama.tables

import org.jetbrains.exposed.v1.core.Table

object Pets : Table("pets") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 64)
    val age = integer("age")
    val type = varchar("type", 64)

    override val primaryKey = PrimaryKey(id)
}
