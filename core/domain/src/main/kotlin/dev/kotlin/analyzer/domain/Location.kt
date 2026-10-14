package dev.kotlin.analyzer.domain

public data class Location(
    val path: String,
    val line: Int,
    val column: Int,
    val endLine: Int? = null,
    val endColumn: Int? = null,
) {
    public fun format(): String = "$path:$line:$column"
}
