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
import org.jetbrains.kotlin.psi.KtClass

public class LargeClassRule : AbstractPsiRule(
    RuleDescriptor(
        id = RuleId("LargeClass"),
        ruleSetId = RuleSetId("builtin.psi"),
        description = "Reports classes that exceed configured member or line count thresholds",
        defaultSeverity = Severity.WARNING,
        tags = setOf("complexity", "maintainability"),
    ),
) {
    override fun visitPsi(file: AnalyzedFile, context: PsiRuleContext) {
        val maxMembers = context.threshold("maxMembers", DEFAULT_MAX_MEMBERS)
        val maxLines = context.threshold("maxLines", DEFAULT_MAX_LINES)
        val ktFile = (file as KotlinAnalyzedFile).ktFile

        PsiTreeUtil.collectElementsOfType(ktFile, KtClass::class.java).forEach { klass ->
            val members = klass.declarations.size
            val lines = countLines(klass)

            if (members > maxMembers) {
                context.report(
                    message = "Class '${klass.name}' has $members members (max $maxMembers)",
                    location = PsiElementLocator.toLocation(file.path, klass),
                    entity = klass.name,
                )
            }
            if (lines > maxLines) {
                context.report(
                    message = "Class '${klass.name}' has $lines lines (max $maxLines)",
                    location = PsiElementLocator.toLocation(file.path, klass),
                    entity = klass.name,
                )
            }
        }
    }

    private fun countLines(klass: KtClass): Int {
        val document = klass.containingFile.viewProvider.document
        if (document != null) {
            val start = document.getLineNumber(klass.textRange.startOffset)
            val end = document.getLineNumber(klass.textRange.endOffset)
            return end - start + 1
        }
        return klass.text.lines().size
    }

    private companion object {
        const val DEFAULT_MAX_MEMBERS = 20
        const val DEFAULT_MAX_LINES = 300
    }
}
