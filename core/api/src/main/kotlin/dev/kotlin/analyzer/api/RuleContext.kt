package dev.kotlin.analyzer.api

import dev.kotlin.analyzer.domain.Finding
import dev.kotlin.analyzer.domain.RuleConfig
import dev.kotlin.analyzer.domain.RuleDescriptor
import dev.kotlin.analyzer.domain.Severity

public interface RuleContext {
    public val descriptor: RuleDescriptor
    public val config: RuleConfig
    public val effectiveSeverity: Severity

    public fun emit(finding: Finding)
}

public interface PsiRuleContext : RuleContext

public interface SemanticRuleContext : RuleContext
