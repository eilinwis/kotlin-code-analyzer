package dev.kotlin.analyzer.rules.psi.rules

import com.intellij.psi.PsiComment
import com.intellij.psi.util.PsiTreeUtil
import dev.kotlin.analyzer.api.AbstractPsiRule
import dev.kotlin.analyzer.api.AnalyzedFile
import dev.kotlin.analyzer.api.PsiRuleContext
import dev.kotlin.analyzer.domain.RuleDescriptor
import dev.kotlin.analyzer.domain.RuleId
import dev.kotlin.analyzer.domain.RuleSetId
import dev.kotlin.analyzer.domain.Severity
import dev.kotlin.analyzer.psi.KotlinAnalyzedFile
import dev.kotlin.analyzer.psi.PsiElementLocator

public class TodoCommentRule : AbstractPsiRule(
    RuleDescriptor(
        id = RuleId("TodoComment"),
        ruleSetId = RuleSetId("builtin.psi"),
        description = "Reports TODO, FIXME, and HACK comments left in source code",
        defaultSeverity = Severity.INFO,
        tags = setOf("comments", "maintainability"),
    ),
) {
    private val pattern = Regex("""\b(TODO|FIXME|HACK)\b""", RegexOption.IGNORE_CASE)

    override fun visitPsi(file: AnalyzedFile, context: PsiRuleContext) {
        val ktFile = (file as KotlinAnalyzedFile).ktFile
        val comments = PsiTreeUtil.collectElementsOfType(ktFile, PsiComment::class.java)
        if (comments.isNotEmpty()) {
            comments.forEach { comment ->
                reportIfTodo(comment, file, context)
            }
            return
        }

        // Fallback for lightweight PSI fixtures where comments may not be modeled as PsiComment nodes.
        file.text.lineSequence().forEachIndexed { index, line ->
            if (pattern.containsMatchIn(line)) {
                val match = pattern.find(line)?.value ?: "TODO"
                val column = line.indexOf(match).let { if (it < 0) 1 else it + 1 }
                context.report(
                    message = "Found $match comment in source code",
                    location = dev.kotlin.analyzer.domain.Location(file.path, index + 1, column),
                )
            }
        }
    }

    private fun reportIfTodo(comment: PsiComment, file: AnalyzedFile, context: PsiRuleContext) {
        val text = comment.text
        if (pattern.containsMatchIn(text)) {
            val match = pattern.find(text)?.value ?: "TODO"
            context.report(
                message = "Found $match comment in source code",
                location = PsiElementLocator.toLocation(file.path, comment),
            )
        }
    }
}
