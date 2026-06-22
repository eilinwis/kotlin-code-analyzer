package dev.kotlin.analyzer.domain

public data class AnalysisResult(
    val findings: List<Finding>,
    val filesAnalyzed: Int,
    val rulesExecuted: Int,
    val durationMs: Long,
)
