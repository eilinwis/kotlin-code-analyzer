package dev.kotlin.analyzer.domain

public data class Suppression(
    val ruleId: RuleId?,
    val path: String?,
    val line: Int?,
    val reason: String? = null,
)
