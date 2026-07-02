package dev.kotlin.analyzer.domain

data class Suppression(
    val ruleId: RuleId?,
    val path: String?,
    val line: Int?,
    val reason: String? = null,
)
