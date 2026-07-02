package dev.kotlin.analyzer.domain

data class Location(
    val path: String,
    val line: Int,
    val column: Int,
    val endLine: Int? = null,
    val endColumn: Int? = null,
) {
    fun format(): String = "$path:$line:$column"
}
