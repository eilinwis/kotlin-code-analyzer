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
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtUnaryExpression
import org.jetbrains.kotlin.psi.KtVisitorVoid

public class NotNullAssertionRule : AbstractPsiRule(
    RuleDescriptor(
        id = RuleId("NotNullAssertion"),
        ruleSetId = RuleSetId("builtin.psi"),
        description = "Reports usage of the !! not-null assertion operator",
        defaultSeverity = Severity.WARNING,
        tags = setOf("nullability", "safety"),
    ),
) {
    override fun visitPsi(file: AnalyzedFile, context: PsiRuleContext) {
        val ktFile = (file as KotlinAnalyzedFile).ktFile
        val unaryExpressions = PsiTreeUtil.collectElementsOfType(ktFile, KtUnaryExpression::class.java)
        unaryExpressions.forEach { expression ->
            if (expression.operationToken == KtTokens.EXCLEXCL) {
                context.report(
                    message = "Avoid using the !! operator; prefer safe calls or explicit null checks",
                    location = PsiElementLocator.toLocation(file.path, expression),
                )
            }
        }
    }
}
