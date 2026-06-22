package dev.kotlin.analyzer.rules.psi.rules

import com.intellij.psi.PsiElement
import dev.kotlin.analyzer.api.AbstractPsiRule
import dev.kotlin.analyzer.api.AnalyzedFile
import dev.kotlin.analyzer.api.PsiRuleContext
import dev.kotlin.analyzer.domain.RuleDescriptor
import dev.kotlin.analyzer.domain.RuleId
import dev.kotlin.analyzer.domain.RuleSetId
import dev.kotlin.analyzer.domain.Severity
import dev.kotlin.analyzer.psi.KotlinAnalyzedFile
import dev.kotlin.analyzer.psi.PsiElementLocator
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtDeclarationWithBody
import org.jetbrains.kotlin.psi.KtIfExpression
import org.jetbrains.kotlin.psi.KtLoopExpression
import org.jetbrains.kotlin.psi.KtTryExpression
import org.jetbrains.kotlin.psi.KtWhenExpression
import org.jetbrains.kotlin.psi.KtVisitorVoid

public class NestedBlockDepthRule : AbstractPsiRule(
    RuleDescriptor(
        id = RuleId("NestedBlockDepth"),
        ruleSetId = RuleSetId("builtin.psi"),
        description = "Reports code with excessive nesting depth",
        defaultSeverity = Severity.WARNING,
        tags = setOf("complexity", "readability"),
    ),
) {
    override fun visitPsi(file: AnalyzedFile, context: PsiRuleContext) {
        val maxDepth = context.threshold("maxDepth", DEFAULT_MAX_DEPTH)
        val ktFile = (file as KotlinAnalyzedFile).ktFile

        ktFile.accept(
            object : KtVisitorVoid() {
                override fun visitElement(element: PsiElement) {
                    if (element is KtDeclarationWithBody) {
                        checkDepth(element.bodyExpression, depth = 0, maxDepth, file, context)
                    }
                    super.visitElement(element)
                }
            },
        )
    }

    private fun checkDepth(
        element: org.jetbrains.kotlin.psi.KtExpression?,
        depth: Int,
        maxDepth: Int,
        file: AnalyzedFile,
        context: PsiRuleContext,
    ) {
        if (element == null) return

        when (element) {
            is KtBlockExpression -> {
                element.statements.forEach { statement ->
                    val nested = nestingDepth(statement)
                    if (nested > 0) {
                        val newDepth = depth + nested
                        if (newDepth > maxDepth) {
                            context.report(
                                message = "Nesting depth $newDepth exceeds maximum of $maxDepth",
                                location = PsiElementLocator.toLocation(file.path, statement),
                            )
                        }
                        checkDepth(statement, newDepth, maxDepth, file, context)
                    }
                }
            }
            else -> {
                val nested = nestingDepth(element)
                if (nested > 0) {
                    val newDepth = depth + nested
                    if (newDepth > maxDepth) {
                        context.report(
                            message = "Nesting depth $newDepth exceeds maximum of $maxDepth",
                            location = PsiElementLocator.toLocation(file.path, element),
                        )
                    }
                }
            }
        }
    }

    private fun nestingDepth(element: org.jetbrains.kotlin.psi.KtExpression): Int =
        when (element) {
            is KtIfExpression, is KtLoopExpression, is KtWhenExpression, is KtTryExpression, is KtBlockExpression -> 1
            else -> 0
        }

    private companion object {
        const val DEFAULT_MAX_DEPTH = 4
    }
}
