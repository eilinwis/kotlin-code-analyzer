package dev.kotlin.analyzer.domain

public data class RuleConfig(
    val enabled: Boolean = true,
    val severity: Severity? = null,
    val thresholds: Map<String, Int> = emptyMap(),
)
