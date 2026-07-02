package dev.kotlin.analyzer.rules.psi

import dev.kotlin.analyzer.api.RuleContributor
import dev.kotlin.analyzer.api.RuleConfigurationSchema
import dev.kotlin.analyzer.domain.RuleSetId
import dev.kotlin.analyzer.rules.psi.rules.*

class BuiltinPsiRuleContributor : RuleContributor {
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

object BuiltinPsiRules {
    val contributor: RuleContributor = BuiltinPsiRuleContributor()
}
