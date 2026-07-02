package dev.kotlin.analyzer.domain

enum class Severity {
    INFO,
    WARNING,
    ERROR,
    ;

    fun isAtLeast(other: Severity): Boolean = ordinal >= other.ordinal
}
