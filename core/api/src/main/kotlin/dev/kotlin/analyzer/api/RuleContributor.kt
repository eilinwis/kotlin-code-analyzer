package dev.kotlin.analyzer.api

import dev.kotlin.analyzer.domain.RuleId
import dev.kotlin.analyzer.domain.RuleSetId

public data class RuleSet(
    val id: RuleSetId,
    val rules: List<Rule>,
)

public interface RuleConfigurationSchema {
    public fun threshold(ruleId: RuleId, key: String, default: Int): Int
    public fun isEnabled(ruleId: RuleId): Boolean
}

public interface RuleContributor {
    public val id: RuleSetId

    public fun contribute(schema: RuleConfigurationSchema): List<Rule>
}
