package dev.kotlin.analyzer.rules.psi

import dev.kotlin.analyzer.api.RuleContributor
import dev.kotlin.analyzer.api.RuleConfigurationSchema
import dev.kotlin.analyzer.api.RuleSet
import dev.kotlin.analyzer.domain.RuleSetId
import dev.kotlin.analyzer.rules.psi.rules.EmptyCatchBlockRule
import dev.kotlin.analyzer.rules.psi.rules.LargeClassRule
import dev.kotlin.analyzer.rules.psi.rules.LongMethodRule
import dev.kotlin.analyzer.rules.psi.rules.NestedBlockDepthRule
import dev.kotlin.analyzer.rules.psi.rules.NotNullAssertionRule
import dev.kotlin.analyzer.rules.psi.rules.TodoCommentRule

public class BuiltinPsiRuleContributor : RuleContributor {
    override val id: RuleSetId = RuleSetId("builtin.psi")

    override fun contribute(schema: RuleConfigurationSchema): List<dev.kotlin.analyzer.api.Rule> = listOf(
        TodoCommentRule(),
        NotNullAssertionRule(),
        EmptyCatchBlockRule(),
        LongMethodRule(),
        NestedBlockDepthRule(),
        LargeClassRule(),
    )
}

public object BuiltinPsiRules {
    public val contributor: RuleContributor = BuiltinPsiRuleContributor()
}
