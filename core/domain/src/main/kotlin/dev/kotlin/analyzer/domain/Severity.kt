package dev.kotlin.analyzer.domain

public enum class Severity {
    INFO,
    WARNING,
    ERROR,
    ;

    public fun isAtLeast(other: Severity): Boolean = ordinal >= other.ordinal
}
