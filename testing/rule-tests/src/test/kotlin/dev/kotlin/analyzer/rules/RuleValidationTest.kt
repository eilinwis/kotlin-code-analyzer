package dev.kotlin.analyzer.rules

import dev.kotlin.analyzer.rules.psi.rules.EmptyCatchBlockRule
import dev.kotlin.analyzer.rules.psi.rules.LargeClassRule
import dev.kotlin.analyzer.rules.psi.rules.LongMethodRule
import dev.kotlin.analyzer.testkit.RuleTestHarness
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isNotEmpty

class RuleValidationTest {
    private val harness = RuleTestHarness()

    @AfterEach
    fun tearDown() {
        harness.close()
    }

    @Test
    fun `EmptyCatchBlock detects empty catch`() {
        val code = """
            fun test() {
                try { risky() } catch (e: Exception) { }
            }
            fun risky() {}
        """.trimIndent()
        val findings = harness.runRule(EmptyCatchBlockRule(), code)
        expectThat(findings).isNotEmpty()
    }

    @Test
    fun `LongMethod detects long functions`() {
        val body = (1..70).joinToString("\n") { "    println($it)" }
        val code = "fun longFn() {\n$body\n}"
        val findings = harness.runRule(
            LongMethodRule(),
            code,
            config = dev.kotlin.analyzer.domain.RuleConfig(thresholds = mapOf("maxLines" to 60)),
        )
        expectThat(findings).isNotEmpty()
    }

    @Test
    fun `LargeClass detects many members`() {
        val members = (1..25).joinToString("\n") { "    fun m$it() {}" }
        val code = "class Big {\n$members\n}"
        val findings = harness.runRule(
            LargeClassRule(),
            code,
            config = dev.kotlin.analyzer.domain.RuleConfig(thresholds = mapOf("maxMembers" to 20)),
        )
        expectThat(findings).isNotEmpty()
    }
}
