package dev.kotlin.analyzer.domain

@JvmInline
value class RuleId(val value: String) {
    override fun toString(): String = value
}

@JvmInline
value class RuleSetId(val value: String) {
    override fun toString(): String = value
}
