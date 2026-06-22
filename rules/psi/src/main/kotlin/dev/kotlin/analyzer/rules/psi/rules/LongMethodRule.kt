package dev.kotlin.analyzer.rules.psi.rules

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
import org.jetbrains.kotlin.psi.KtNamedFunction

public class LongMethodRule : AbstractPsiRule(
    RuleDescriptor(
        id = RuleId("LongMethod"),
        ruleSetId = RuleSetId("builtin.psi"),
        description = "Reports functions that exceed the configured maximum line count",
        defaultSeverity = Severity.WARNING,
        tags = setOf("complexity", "maintainability"),
    ),
) {
    override fun visitPsi(file: AnalyzedFile, context: PsiRuleContext) {
        val maxLines = context.threshold("maxLines", DEFAULT_MAX_LINES)
        val ktFile = (file as KotlinAnalyzedFile).ktFile
        PsiTreeUtil.collectElementsOfType(ktFile, KtNamedFunction::class.java).forEach { function ->
            if (function.bodyExpression == null) return@forEach
            val lineCount = countLines(function)
            if (lineCount > maxLines) {
                context.report(
                    message = "Function '${function.name}' has $lineCount lines (max $maxLines)",
                    location = PsiElementLocator.toLocation(file.path, function),
                    entity = function.name,
                )
            }
        }
    }

    private fun countLines(function: KtNamedFunction): Int {
        val document = function.containingFile.viewProvider.document
        if (document != null) {
            val start = document.getLineNumber(function.textRange.startOffset)
            val end = document.getLineNumber(function.textRange.endOffset)
            return end - start + 1
        }
        return function.text.lines().size
    }

    private companion object {
        const val DEFAULT_MAX_LINES = 60
    }
}
