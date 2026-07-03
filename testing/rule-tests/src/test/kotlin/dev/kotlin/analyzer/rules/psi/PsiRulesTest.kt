package dev.kotlin.analyzer.rules.psi

import dev.kotlin.analyzer.rules.psi.rules.NotNullAssertionRule
import dev.kotlin.analyzer.rules.psi.rules.TodoCommentRule
import dev.kotlin.analyzer.rules.psi.rules.UnusedImportRule
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

    @Test
    fun `UnusedImportRule detects unused import`() {
        val findings = harness.runRule(UnusedImportRule(), "import strikt.api.expectThat \nfun main() {}")
        expectThat(findings).hasSize(1)
        expectThat(findings.first().ruleId.value).isEqualTo("UnusedImport")
    }

    @Test
    fun `UnusedImportRule detects no unused imports`() {
        val findings = harness.runRule(UnusedImportRule(), "import strikt.api.expectThat \nfun main() { \n" +
                " expectThat(findings).hasSize(1) }")
        expectThat(findings).hasSize(0)
    }

    @Test
    fun `UnusedImportRule detects no unused imports having several`() {
        val findings = harness.runRule(UnusedImportRule(), "import strikt.api.expectThat \n" +
                "import com.github.ajalt.clikt.core.subcommands \nfun main() {")
        expectThat(findings).hasSize(2)
        expectThat(findings.first().ruleId.value).isEqualTo("UnusedImport")
        expectThat(findings[1].ruleId.value).isEqualTo("UnusedImport")
    }
}
