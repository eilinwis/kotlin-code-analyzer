package dev.kotlin.analyzer.rules.semantic

import dev.kotlin.analyzer.api.RuleContributor
import dev.kotlin.analyzer.api.RuleConfigurationSchema
import dev.kotlin.analyzer.domain.RuleSetId
import dev.kotlin.analyzer.rules.semantic.rules.ScopeFunctionMisuseRule
import dev.kotlin.analyzer.rules.semantic.rules.SuspiciousExtensionRule
import dev.kotlin.analyzer.rules.semantic.rules.UnsafeNullabilityRule

public class BuiltinSemanticRuleContributor : RuleContributor {
    override val id: RuleSetId = RuleSetId("builtin.semantic")

    override fun contribute(schema: RuleConfigurationSchema): List<dev.kotlin.analyzer.api.Rule> = listOf(
        UnsafeNullabilityRule(),
        SuspiciousExtensionRule(),
        ScopeFunctionMisuseRule(),
    )
}

public object BuiltinSemanticRules {
    public val contributor: RuleContributor = BuiltinSemanticRuleContributor()
}
