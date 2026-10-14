package dev.kotlin.analyzer.rules.psi

import dev.kotlin.analyzer.api.RuleContributor
import dev.kotlin.analyzer.api.RuleConfigurationSchema
import dev.kotlin.analyzer.domain.RuleSetId
import dev.kotlin.analyzer.rules.psi.rules.*

public class BuiltinPsiRuleContributor : RuleContributor {
    override val id: RuleSetId = RuleSetId("builtin.psi")

    override fun contribute(schema: RuleConfigurationSchema): List<dev.kotlin.analyzer.api.Rule> = listOf(
        UnusedImportRule(),
        TodoCommentRule(),
        NotNullAssertionRule(),
        EmptyCatchBlockRule(),
        LongMethodRule(),
        NestedBlockDepthRule(),
        LargeClassRule()
    )
}

public object BuiltinPsiRules {
    public val contributor: RuleContributor = BuiltinPsiRuleContributor()
}
