package dev.kotlin.analyzer.config

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import dev.kotlin.analyzer.domain.AnalysisConfig
import dev.kotlin.analyzer.domain.RuleConfig
import dev.kotlin.analyzer.domain.RuleId
import dev.kotlin.analyzer.domain.RuleSetId
import dev.kotlin.analyzer.domain.Severity
import kotlinx.serialization.Serializable
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readText

public class YamlConfigLoader {
    private val yaml = Yaml(configuration = YamlConfiguration(strictMode = false))

    public fun load(path: Path?): AnalysisConfig {
        if (path == null || !path.exists()) {
            return loadFromYaml(defaultYaml())
        }
        return loadFromYaml(path.readText())
    }

    private fun loadFromYaml(yamlText: String): AnalysisConfig =
        yaml.decodeFromString(ConfigFile.serializer(), yamlText).toDomain()

    public fun defaultYaml(): String = DEFAULT_CONFIG_YAML

    private companion object {
        const val DEFAULT_CONFIG_YAML = """
analysis:
  failOnSeverity: ERROR
  parallel: true
  jvmTarget: "17"
  includes:
    - "**/*.kt"
  excludes:
    - "**/build/**"
    - "**/.gradle/**"
    - "**/generated/**"

ruleSets:
  builtin.psi:
    TodoComment:
      enabled: true
      severity: INFO
    NotNullAssertion:
      enabled: true
      severity: WARNING
    EmptyCatchBlock:
      enabled: true
      severity: ERROR
    LongMethod:
      enabled: true
      severity: WARNING
      thresholds:
        maxLines: 60
    NestedBlockDepth:
      enabled: true
      severity: WARNING
      thresholds:
        maxDepth: 4
    LargeClass:
      enabled: true
      severity: WARNING
      thresholds:
        maxMembers: 20
        maxLines: 300
  builtin.semantic:
    UnsafeNullability:
      enabled: false
      severity: WARNING
    SuspiciousExtension:
      enabled: false
      severity: WARNING
    ScopeFunctionMisuse:
      enabled: false
      severity: INFO
"""
    }
}

@Serializable
internal data class ConfigFile(
    val analysis: AnalysisSection = AnalysisSection(),
    val ruleSets: Map<String, Map<String, RuleSection>> = emptyMap(),
) {
    fun toDomain(): AnalysisConfig = AnalysisConfig(
        ruleSets = ruleSets.mapKeys { (k, _) -> RuleSetId(k) }
            .mapValues { (_, rules) ->
                rules.mapKeys { (k, _) -> RuleId(k) }
                    .mapValues { (_, v) -> v.toDomain() }
            },
        excludes = analysis.excludes,
        includes = analysis.includes,
        failOnSeverity = Severity.valueOf(analysis.failOnSeverity.uppercase()),
        parallel = analysis.parallel,
        jvmTarget = analysis.jvmTarget,
    )
}

@Serializable
internal data class AnalysisSection(
    val failOnSeverity: String = "ERROR",
    val parallel: Boolean = true,
    val jvmTarget: String = "17",
    val includes: List<String> = AnalysisConfig.DEFAULT_EXCLUDES.let { listOf("**/*.kt") },
    val excludes: List<String> = AnalysisConfig.DEFAULT_EXCLUDES,
)

@Serializable
internal data class RuleSection(
    val enabled: Boolean = true,
    val severity: String? = null,
    val thresholds: Map<String, Int> = emptyMap(),
) {
    fun toDomain(): RuleConfig = RuleConfig(
        enabled = enabled,
        severity = severity?.let { Severity.valueOf(it.uppercase()) },
        thresholds = thresholds,
    )
}

public class ConfigValidator {
    public fun validate(config: AnalysisConfig): List<String> {
        val errors = mutableListOf<String>()
        config.ruleSets.forEach { (setId, rules) ->
            rules.forEach { (ruleId, ruleConfig) ->
                ruleConfig.severity?.let {
                    if (it !in Severity.entries) {
                        errors.add("Invalid severity for rule $ruleId in set $setId")
                    }
                }
            }
        }
        return errors
    }
}
