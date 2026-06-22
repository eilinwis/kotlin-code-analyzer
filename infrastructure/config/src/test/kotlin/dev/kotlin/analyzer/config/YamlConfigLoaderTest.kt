package dev.kotlin.analyzer.config

import dev.kotlin.analyzer.domain.RuleId
import dev.kotlin.analyzer.domain.Severity
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo

class YamlConfigLoaderTest {
    @Test
    fun `loads default yaml`() {
        val loader = YamlConfigLoader()
        val yaml = loader.defaultYaml()
        val config = loader.load(null).copy(
            ruleSets = loader.load(null).ruleSets,
        )
        expectThat(config.failOnSeverity).isEqualTo(Severity.ERROR)
        expectThat(yaml.contains("TodoComment")).isEqualTo(true)
    }

    @Test
    fun `validator accepts valid config`() {
        val config = YamlConfigLoader().load(null)
        val errors = ConfigValidator().validate(config)
        expectThat(errors).isEmpty()
    }
}
