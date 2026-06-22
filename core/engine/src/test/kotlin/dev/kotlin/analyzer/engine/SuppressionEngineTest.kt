package dev.kotlin.analyzer.engine

import dev.kotlin.analyzer.domain.Finding
import dev.kotlin.analyzer.domain.Location
import dev.kotlin.analyzer.domain.RuleId
import dev.kotlin.analyzer.domain.Severity
import dev.kotlin.analyzer.domain.Suppression
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.hasSize

class SuppressionEngineTest {
    @Test
    fun `filters suppressed findings by rule id`() {
        val engine = SuppressionEngine()
        val findings = listOf(
            Finding(
                ruleId = RuleId("TodoComment"),
                message = "todo",
                severity = Severity.INFO,
                location = Location("Test.kt", 1, 1),
            ),
            Finding(
                ruleId = RuleId("NotNullAssertion"),
                message = "bang",
                severity = Severity.WARNING,
                location = Location("Test.kt", 2, 1),
            ),
        )

        val filtered = engine.filter(findings)
        expectThat(filtered).hasSize(2)
    }
}
