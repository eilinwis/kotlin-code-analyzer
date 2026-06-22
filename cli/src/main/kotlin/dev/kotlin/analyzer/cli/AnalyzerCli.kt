package dev.kotlin.analyzer.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.path
import dev.kotlin.analyzer.analysis.StandaloneAnalysisSessionFactory
import dev.kotlin.analyzer.config.ConfigValidator
import dev.kotlin.analyzer.config.YamlConfigLoader
import dev.kotlin.analyzer.domain.Severity
import dev.kotlin.analyzer.engine.AnalyzeProjectUseCase
import dev.kotlin.analyzer.engine.RuleRegistry
import dev.kotlin.analyzer.reporting.ReporterFactory
import dev.kotlin.analyzer.reporting.parseReportFormats
import dev.kotlin.analyzer.rules.psi.BuiltinPsiRules
import dev.kotlin.analyzer.rules.semantic.BuiltinSemanticRules
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText
import kotlin.system.exitProcess

public class AnalyzerCli : CliktCommand(name = "kotlin-code-analyzer") {
    override fun run() = Unit

    override fun help(context: Context): String =
        "Static analysis tool for Kotlin source code using PSI and Analysis API"
}

public class AnalyzeCommand : CliktCommand(name = "analyze") {
    private val paths by argument("paths").path(mustExist = true).multiple()
    private val configPath by option("-c", "--config").path(mustExist = false)
    private val exclude by option("--exclude").multiple()
    private val include by option("--include").multiple()
    private val report by option("--report").multiple(default = listOf("console"))
    private val outputDir by option("--output-dir").path(mustExist = false)
    private val failOn by option("--fail-on").choice("info", "warning", "error").default("error")
    private val plugins by option("--plugins").path(mustExist = true).multiple()
    private val parallel by option("--parallel", help = "Enable parallel analysis").flag(default = true)
    private val noParallel by option("--no-parallel", help = "Disable parallel analysis").flag()
    private val verbose by option("--verbose").flag()

    override fun run() {
        val loader = YamlConfigLoader()
        val baseConfig = loader.load(configPath)
        val config = baseConfig.copy(
            excludes = if (exclude.isNotEmpty()) baseConfig.excludes + exclude else baseConfig.excludes,
            includes = if (include.isNotEmpty()) include else baseConfig.includes,
            failOnSeverity = Severity.valueOf(failOn.uppercase()),
            parallel = parallel && !noParallel,
        )

        val validationErrors = ConfigValidator().validate(config)
        if (validationErrors.isNotEmpty()) {
            validationErrors.forEach { echo(it, err = true) }
            exitProcess(ExitCodes.CONFIG_ERROR)
        }

        val contributors = listOf(BuiltinPsiRules.contributor, BuiltinSemanticRules.contributor)
        val useCase = AnalyzeProjectUseCase(contributors)
        val reporters = ReporterFactory().create(parseReportFormats(report))
        val output = outputDir?.also { it.createDirectories() }

        if (verbose) {
            echo("Analyzing ${paths.size} path(s) with ${config.parallel} parallelism")
        }

        StandaloneAnalysisSessionFactory().use { sessionFactory ->
            val result = useCase.analyze(
                paths = paths,
                config = config,
                reporters = reporters,
                outputDir = output,
                pluginJars = plugins,
                analysisSessionFactory = { psiEnv, files ->
                    try {
                        sessionFactory.createSession(psiEnv, files)
                    } catch (ex: Exception) {
                        if (verbose) {
                            echo("Semantic analysis unavailable: ${ex.message}", err = true)
                        }
                        null
                    }
                },
            )

            if (useCase.shouldFail(result, config.failOnSeverity)) {
                exitProcess(ExitCodes.ISSUES_FOUND)
            }
        }
    }
}

public class RulesCommand : CliktCommand(name = "rules") {
    override fun run() = Unit
}

public class RulesListCommand : CliktCommand(name = "list") {
    private val asJson by option("--json").flag()

    override fun run() {
        val registry = RuleRegistry.discover(
            builtInContributors = listOf(BuiltinPsiRules.contributor, BuiltinSemanticRules.contributor),
        )
        val descriptors = registry.allDescriptors().sortedBy { it.id.value }

        if (asJson) {
            val json = descriptors.joinToString(prefix = "[", postfix = "]") { descriptor ->
                """{"id":"${descriptor.id.value}","ruleSet":"${descriptor.ruleSetId.value}","severity":"${descriptor.defaultSeverity}","description":${descriptor.description.quoteJson()},"requiresAnalysisApi":${descriptor.requiresAnalysisApi}}"""
            }
            echo(json)
        } else {
            descriptors.forEach { descriptor ->
                echo(
                    "${descriptor.id.value} [${descriptor.ruleSetId.value}] " +
                        "(${descriptor.defaultSeverity}) - ${descriptor.description}",
                )
            }
        }
    }

    private fun String.quoteJson(): String = "\"" + replace("\"", "\\\"") + "\""
}

public class InitConfigCommand : CliktCommand(name = "init-config") {
    private val output by option("--output", "-o").path().default(java.nio.file.Path.of("analyzer.yml"))

    override fun run() {
        val loader = YamlConfigLoader()
        output.writeText(loader.defaultYaml())
        echo("Created default configuration at $output")
    }
}

public class VersionCommand : CliktCommand(name = "version") {
    override fun run() {
        echo("kotlin-code-analyzer 0.1.0-SNAPSHOT")
    }
}

public object ExitCodes {
    public const val SUCCESS: Int = 0
    public const val ISSUES_FOUND: Int = 1
    public const val CONFIG_ERROR: Int = 2
    public const val INTERNAL_ERROR: Int = 3
}

public fun main(args: Array<String>) {
    try {
        AnalyzerCli()
            .subcommands(
                AnalyzeCommand(),
                RulesCommand().subcommands(RulesListCommand()),
                InitConfigCommand(),
                VersionCommand(),
            )
            .main(args)
    } catch (ex: Exception) {
        System.err.println("Internal error: ${ex.message}")
        ex.printStackTrace()
        exitProcess(ExitCodes.INTERNAL_ERROR)
    }
}
