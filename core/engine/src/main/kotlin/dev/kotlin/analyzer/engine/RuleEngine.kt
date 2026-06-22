package dev.kotlin.analyzer.engine

import dev.kotlin.analyzer.api.AnalysisSession
import dev.kotlin.analyzer.api.PsiRule
import dev.kotlin.analyzer.api.PsiRuleContext
import dev.kotlin.analyzer.api.SemanticRule
import dev.kotlin.analyzer.api.SemanticRuleContext
import dev.kotlin.analyzer.api.effectiveSeverity
import dev.kotlin.analyzer.domain.AnalysisConfig
import dev.kotlin.analyzer.domain.AnalysisResult
import dev.kotlin.analyzer.domain.Finding
import dev.kotlin.analyzer.domain.RuleConfig
import dev.kotlin.analyzer.domain.Severity
import dev.kotlin.analyzer.psi.KotlinAnalyzedFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking

public class RuleEngine(
    private val suppressionEngine: SuppressionEngine = SuppressionEngine(),
) {
    public fun analyze(
        files: List<KotlinAnalyzedFile>,
        configuredRules: List<ConfiguredRule>,
        config: AnalysisConfig,
        analysisSession: AnalysisSession? = null,
    ): AnalysisResult {
        val start = System.currentTimeMillis()
        files.forEach { suppressionEngine.indexFile(it) }

        val psiRules = configuredRules.mapNotNull { (rule, ruleConfig) ->
            (rule as? PsiRule)?.let { it to ruleConfig }
        }
        val semanticRules = configuredRules.mapNotNull { (rule, ruleConfig) ->
            (rule as? SemanticRule)?.let { it to ruleConfig }
        }

        val findings = mutableListOf<Finding>()

        val psiFindings = if (config.parallel) {
            runBlocking {
                files.map { file ->
                    async(Dispatchers.Default) {
                        analyzePsiRules(file, psiRules)
                    }
                }.awaitAll().flatten()
            }
        } else {
            files.flatMap { file -> analyzePsiRules(file, psiRules) }
        }
        findings.addAll(psiFindings)

        if (semanticRules.isNotEmpty() && analysisSession != null) {
            files.forEach { file ->
                findings.addAll(analyzeSemanticRules(file, semanticRules, analysisSession))
            }
        }

        val deduplicated = deduplicate(findings)
        val filtered = suppressionEngine.filter(deduplicated)

        return AnalysisResult(
            findings = filtered.sortedWith(compareBy({ it.location.path }, { it.location.line }, { it.location.column })),
            filesAnalyzed = files.size,
            rulesExecuted = configuredRules.size,
            durationMs = System.currentTimeMillis() - start,
        )
    }

    private fun analyzePsiRules(
        file: KotlinAnalyzedFile,
        psiRules: List<Pair<PsiRule, RuleConfig>>,
    ): List<Finding> {
        val findings = mutableListOf<Finding>()
        psiRules.forEach { (rule, ruleConfig) ->
            val context = MutablePsiRuleContext(rule.descriptor, ruleConfig, findings)
            rule.visitPsi(file, context)
        }
        return findings
    }

    private fun analyzeSemanticRules(
        file: KotlinAnalyzedFile,
        semanticRules: List<Pair<SemanticRule, RuleConfig>>,
        analysisSession: AnalysisSession,
    ): List<Finding> {
        val findings = mutableListOf<Finding>()
        try {
            analysisSession.analyze(file) {
                semanticRules.forEach { (rule, ruleConfig) ->
                    val context = MutableSemanticRuleContext(rule.descriptor, ruleConfig, findings)
                    rule.visitSemantic(file, analysisSession, context)
                }
            }
        } catch (_: Exception) {
            // Semantic analysis unavailable; PSI findings are still returned.
        }
        return findings
    }

    private fun deduplicate(findings: List<Finding>): List<Finding> =
        findings.distinctBy { "${it.ruleId}|${it.location.path}|${it.location.line}|${it.location.column}|${it.message}" }
}

private class MutablePsiRuleContext(
    override val descriptor: dev.kotlin.analyzer.domain.RuleDescriptor,
    override val config: RuleConfig,
    private val sink: MutableList<Finding>,
) : PsiRuleContext {
    override val effectiveSeverity: Severity = config.effectiveSeverity(descriptor)

    override fun emit(finding: Finding) {
        sink.add(finding)
    }
}

private class MutableSemanticRuleContext(
    override val descriptor: dev.kotlin.analyzer.domain.RuleDescriptor,
    override val config: RuleConfig,
    private val sink: MutableList<Finding>,
) : SemanticRuleContext {
    override val effectiveSeverity: Severity = config.effectiveSeverity(descriptor)

    override fun emit(finding: Finding) {
        sink.add(finding)
    }
}
