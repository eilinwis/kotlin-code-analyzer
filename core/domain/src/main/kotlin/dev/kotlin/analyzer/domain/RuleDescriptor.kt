package dev.kotlin.analyzer.domain

public data class RuleDescriptor(
    val id: RuleId,
    val ruleSetId: RuleSetId,
    val description: String,
    val defaultSeverity: Severity,
    val tags: Set<String> = emptySet(),
    val requiresAnalysisApi: Boolean = false,
)
