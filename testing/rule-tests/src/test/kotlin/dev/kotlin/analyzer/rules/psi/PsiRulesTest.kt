package dev.kotlin.analyzer.rules.psi

import dev.kotlin.analyzer.rules.psi.rules.NotNullAssertionRule
import dev.kotlin.analyzer.rules.psi.rules.TodoCommentRule
import dev.kotlin.analyzer.testkit.RuleTestHarness
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isNotEmpty

class PsiRulesTest {
    private val harness = RuleTestHarness()

    @AfterEach
    fun tearDown() {
        harness.close()
    }

    @Test
    fun `TodoComment detects TODO`() {
        val findings = harness.runRule(TodoCommentRule(), "// TODO: fix this\nfun main() {}")
        expectThat(findings).hasSize(1)
        expectThat(findings.first().ruleId.value).isEqualTo("TodoComment")
    }

    @Test
    fun `TodoComment ignores clean code`() {
        val findings = harness.runRule(TodoCommentRule(), "fun main() { println(\"ok\") }")
        expectThat(findings).hasSize(0)
    }

    @Test
    fun `NotNullAssertion detects double bang`() {
        val findings = harness.runRule(NotNullAssertionRule(), "fun test(s: String?) { val x = s!! }")
        expectThat(findings).isNotEmpty()
        expectThat(findings.first().ruleId.value).isEqualTo("NotNullAssertion")
    }
}
