package dev.kotlin.analyzer.engine

import dev.kotlin.analyzer.api.AnalysisSession
import dev.kotlin.analyzer.api.RuleContributor
import dev.kotlin.analyzer.domain.AnalysisConfig
import dev.kotlin.analyzer.domain.AnalysisResult
import dev.kotlin.analyzer.domain.Reporter
import dev.kotlin.analyzer.psi.KotlinAnalyzedFile
import dev.kotlin.analyzer.psi.KotlinPsiEnvironment
import dev.kotlin.analyzer.psi.KotlinProjectLoader
import dev.kotlin.analyzer.psi.SourceFileCollector
import java.nio.file.Path

public class AnalyzeProjectUseCase(
    private val builtInContributors: List<RuleContributor>,
) {
    public fun analyze(
        paths: List<Path>,
        config: AnalysisConfig,
        reporters: List<Reporter>,
        outputDir: Path? = null,
        pluginJars: List<Path> = emptyList(),
        analysisSessionFactory: ((KotlinPsiEnvironment, List<KotlinAnalyzedFile>) -> AnalysisSession?)? = null,
    ): AnalysisResult {
        val registry = RuleRegistry.discover(pluginJars, builtInContributors)
        val configuredRules = registry.allRules(config)
        val needsSemanticSession = configuredRules.any { it.rule.descriptor.requiresAnalysisApi }

        KotlinPsiEnvironment().use { psiEnv ->
            val collector = SourceFileCollector(config.includes, config.excludes)
            val sourceFiles = collector.collect(paths)
            val loader = KotlinProjectLoader(psiEnv)
            val files = loader.loadFiles(sourceFiles)

            val session = if (needsSemanticSession) {
                analysisSessionFactory?.invoke(psiEnv, files)
            } else {
                null
            }

            val engine = RuleEngine()
            val result = engine.analyze(files, configuredRules, config, session)

            reporters.forEach { reporter ->
                reporter.report(result, outputDir)
            }

            return result
        }
    }

    public fun shouldFail(result: AnalysisResult, failOn: dev.kotlin.analyzer.domain.Severity): Boolean =
        result.findings.any { it.severity.isAtLeast(failOn) }
}
