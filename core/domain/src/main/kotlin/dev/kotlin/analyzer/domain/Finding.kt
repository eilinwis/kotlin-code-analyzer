package dev.kotlin.analyzer.domain

 data class Finding(
    val ruleId: RuleId,
    val message: String,
    val severity: Severity,
    val location: Location,
    val entity: String? = null,
    val quickFixId: String? = null,
)
