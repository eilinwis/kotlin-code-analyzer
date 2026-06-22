package dev.kotlin.analyzer.api

import dev.kotlin.analyzer.domain.Finding
import dev.kotlin.analyzer.domain.Location
import dev.kotlin.analyzer.domain.RuleConfig
import dev.kotlin.analyzer.domain.RuleDescriptor
import dev.kotlin.analyzer.domain.Severity

public abstract class AbstractPsiRule(
    override val descriptor: RuleDescriptor,
) : PsiRule {

    protected fun PsiRuleContext.report(
        message: String,
        location: Location,
        entity: String? = null,
    ) {
        emit(
            Finding(
                ruleId = descriptor.id,
                message = message,
                severity = effectiveSeverity,
                location = location,
                entity = entity,
            ),
        )
    }

    protected fun PsiRuleContext.threshold(key: String, default: Int): Int =
        config.thresholds[key] ?: default
}

public abstract class AbstractSemanticRule(
    override val descriptor: RuleDescriptor,
) : SemanticRule {

    protected fun SemanticRuleContext.report(
        message: String,
        location: Location,
        entity: String? = null,
    ) {
        emit(
            Finding(
                ruleId = descriptor.id,
                message = message,
                severity = effectiveSeverity,
                location = location,
                entity = entity,
            ),
        )
    }

    protected fun SemanticRuleContext.threshold(key: String, default: Int): Int =
        config.thresholds[key] ?: default
}

public fun RuleConfig.effectiveSeverity(descriptor: RuleDescriptor): Severity =
    severity ?: descriptor.defaultSeverity
