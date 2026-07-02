package dev.kotlin.analyzer.domain

data class AnalysisConfig(
    val ruleSets: Map<RuleSetId, Map<RuleId, RuleConfig>> = emptyMap(),
    val excludes: List<String> = DEFAULT_EXCLUDES,
    val includes: List<String> = listOf("**/*.kt"),
    val failOnSeverity: Severity = Severity.ERROR,
    val parallel: Boolean = true,
    val jvmTarget: String = "17",
) {
    companion object {
        val DEFAULT_EXCLUDES: List<String> = listOf(
            "**/build/**",
            "**/.gradle/**",
            "**/generated/**",
        )
    }
}
