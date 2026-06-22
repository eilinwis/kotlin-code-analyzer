package dev.kotlin.analyzer.engine

import dev.kotlin.analyzer.api.Rule
import dev.kotlin.analyzer.api.RuleConfigurationSchema
import dev.kotlin.analyzer.api.RuleContributor
import dev.kotlin.analyzer.domain.AnalysisConfig
import dev.kotlin.analyzer.domain.RuleConfig
import dev.kotlin.analyzer.domain.RuleId
import dev.kotlin.analyzer.domain.RuleSetId
import java.net.URLClassLoader
import java.nio.file.Path
import java.util.ServiceLoader

public class RuleRegistry(
    private val contributors: List<RuleContributor>,
) {
    public fun allRules(config: AnalysisConfig): List<ConfiguredRule> {
        val schema = ConfigRuleSchema(config)
        return contributors.flatMap { contributor ->
            contributor.contribute(schema).map { rule ->
                ConfiguredRule(
                    rule = rule,
                    config = schema.ruleConfig(rule.descriptor.id),
                )
            }
        }.filter { it.config.enabled }
    }

    public fun allDescriptors(): List<dev.kotlin.analyzer.domain.RuleDescriptor> =
        contributors.flatMap { contributor ->
            contributor.contribute(AlwaysEnabledSchema).map { it.descriptor }
        }

    public companion object {
        public fun discover(
            extraPluginJars: List<Path> = emptyList(),
            builtInContributors: List<RuleContributor>,
        ): RuleRegistry {
            val contributors = mutableListOf<RuleContributor>()
            contributors.addAll(builtInContributors)

            if (extraPluginJars.isNotEmpty()) {
                val urls = extraPluginJars.map { it.toUri().toURL() }.toTypedArray()
                val classLoader = URLClassLoader(urls, RuleContributor::class.java.classLoader)
                ServiceLoader.load(RuleContributor::class.java, classLoader).forEach { contributors.add(it) }
            } else {
                ServiceLoader.load(RuleContributor::class.java).forEach { contributors.add(it) }
            }

            return RuleRegistry(contributors.distinctBy { it.id })
        }
    }
}

public data class ConfiguredRule(
    val rule: Rule,
    val config: RuleConfig,
)

private class ConfigRuleSchema(
    private val analysisConfig: AnalysisConfig,
) : RuleConfigurationSchema {
  private val ruleConfigs: Map<RuleId, RuleConfig> = analysisConfig.ruleSets.values
        .flatMap { it.entries }
        .associate { (id, config) -> id to config }

    fun ruleConfig(ruleId: RuleId): RuleConfig = ruleConfigs[ruleId] ?: RuleConfig(enabled = false)

    override fun threshold(ruleId: RuleId, key: String, default: Int): Int =
        ruleConfig(ruleId).thresholds[key] ?: default

    override fun isEnabled(ruleId: RuleId): Boolean = ruleConfig(ruleId).enabled
}

private object AlwaysEnabledSchema : RuleConfigurationSchema {
    override fun threshold(ruleId: RuleId, key: String, default: Int): Int = default
    override fun isEnabled(ruleId: RuleId): Boolean = true
}
