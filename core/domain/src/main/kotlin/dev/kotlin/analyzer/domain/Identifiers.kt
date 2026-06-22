package dev.kotlin.analyzer.domain

@JvmInline
public value class RuleId(public val value: String) {
    override fun toString(): String = value
}

@JvmInline
public value class RuleSetId(public val value: String) {
    override fun toString(): String = value
}
