package dev.kotlin.analyzer.testkit

import dev.kotlin.analyzer.api.PsiRule
import dev.kotlin.analyzer.api.PsiRuleContext
import dev.kotlin.analyzer.api.effectiveSeverity
import dev.kotlin.analyzer.domain.Finding
import dev.kotlin.analyzer.domain.RuleConfig
import dev.kotlin.analyzer.domain.Severity
import dev.kotlin.analyzer.engine.RuleEngine
import dev.kotlin.analyzer.engine.ConfiguredRule
import dev.kotlin.analyzer.domain.AnalysisConfig
import dev.kotlin.analyzer.psi.KotlinAnalyzedFile
import dev.kotlin.analyzer.psi.KotlinPsiEnvironment

public class RuleTestHarness {
    private val psiEnvironment = KotlinPsiEnvironment()

    public fun runRule(
        rule: PsiRule,
        code: String,
        path: String = "Test.kt",
        config: RuleConfig = RuleConfig(),
    ): List<Finding> {
        val ktFile = psiEnvironment.parseText(path, code)
        val file = KotlinAnalyzedFile(path, code, ktFile)
        val findings = mutableListOf<Finding>()
        val context = object : PsiRuleContext {
            override val descriptor = rule.descriptor
            override val config = config
            override val effectiveSeverity: Severity = config.effectiveSeverity(descriptor)
            override fun emit(finding: Finding) {
                findings.add(finding)
            }
        }
        rule.visitPsi(file, context)
        return findings
    }

    public fun runRules(
        rules: List<PsiRule>,
        code: String,
        path: String = "Test.kt",
    ): List<Finding> {
        val ktFile = psiEnvironment.parseText(path, code)
        val file = KotlinAnalyzedFile(path, code, ktFile)
        val configured = rules.map { ConfiguredRule(it, RuleConfig()) }
        return RuleEngine().analyze(listOf(file), configured, AnalysisConfig(parallel = false))
            .findings
    }

    public fun close() {
        psiEnvironment.close()
    }
}
