package dev.kotlin.analyzer.rules.semantic.rules

import dev.kotlin.analyzer.api.AbstractSemanticRule
import dev.kotlin.analyzer.api.AnalysisSession
import dev.kotlin.analyzer.api.AnalyzedFile
import dev.kotlin.analyzer.api.RequiresAnalysisApi
import dev.kotlin.analyzer.api.SemanticRuleContext
import dev.kotlin.analyzer.domain.RuleDescriptor
import dev.kotlin.analyzer.domain.RuleId
import dev.kotlin.analyzer.domain.RuleSetId
import dev.kotlin.analyzer.domain.Severity
import dev.kotlin.analyzer.psi.KotlinAnalyzedFile
import dev.kotlin.analyzer.psi.PsiElementLocator
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtVisitorVoid

@RequiresAnalysisApi
public class ScopeFunctionMisuseRule : AbstractSemanticRule(
    RuleDescriptor(
        id = RuleId("ScopeFunctionMisuse"),
        ruleSetId = RuleSetId("builtin.semantic"),
        description = "Reports potential misuse of Kotlin scope functions (let, run, apply, also)",
        defaultSeverity = Severity.INFO,
        tags = setOf("idioms", "scope-functions"),
        requiresAnalysisApi = true,
    ),
) {
    private val scopeFunctions = setOf("let", "run", "apply", "also")

    override fun visitSemantic(file: AnalyzedFile, session: AnalysisSession, context: SemanticRuleContext) {
        val ktFile = (file as KotlinAnalyzedFile).ktFile
        ktFile.accept(
            object : KtVisitorVoid() {
                override fun visitCallExpression(expression: KtCallExpression) {
                    val callee = expression.calleeExpression?.text ?: ""
                    val functionName = callee.substringAfterLast('.')
                    if (functionName in scopeFunctions) {
                        checkScopeFunction(file, context, expression, functionName)
                    }
                    super.visitCallExpression(expression)
                }
            },
        )
    }

    private fun checkScopeFunction(
        file: AnalyzedFile,
        context: SemanticRuleContext,
        expression: KtCallExpression,
        functionName: String,
    ) {
        when (functionName) {
            "apply", "also" -> {
                if (isReturnValueUsed(expression)) {
                    context.report(
                        message = "Return value of '$functionName' is used; consider 'let' or 'run' instead",
                        location = PsiElementLocator.toLocation(file.path, expression),
                    )
                }
            }
            "let", "run" -> {
                val hasImplicitIt = expression.lambdaArguments.firstOrNull()
                    ?.getLambdaExpression()
                    ?.valueParameters
                    ?.isEmpty() == true
                if (hasImplicitIt && expression.parent is KtDotQualifiedExpression) {
                    context.report(
                        message = "Nested scope function with implicit 'it' may reduce readability",
                        location = PsiElementLocator.toLocation(file.path, expression),
                    )
                }
            }
        }
    }

    private fun isReturnValueUsed(expression: KtCallExpression): Boolean {
        val parent = expression.parent ?: return false
        return when (parent) {
            is KtDotQualifiedExpression -> parent.receiverExpression == expression
            else -> parent.text.startsWith("val") || parent.text.startsWith("return") || parent.text.contains("=")
        }
    }
}
