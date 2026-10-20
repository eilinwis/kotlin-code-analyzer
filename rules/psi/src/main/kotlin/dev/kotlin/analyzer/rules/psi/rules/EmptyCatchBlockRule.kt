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
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtCatchClause
import org.jetbrains.kotlin.psi.KtExpression

public class EmptyCatchBlockRule : AbstractPsiRule(
    RuleDescriptor(
        id = RuleId("EmptyCatchBlock"),
        ruleSetId = RuleSetId("builtin.psi"),
        description = "Reports empty catch blocks that silently swallow exceptions",
        defaultSeverity = Severity.ERROR,
        tags = setOf("error-handling", "reliability"),
    ),
) {
    override fun visitPsi(file: AnalyzedFile, context: PsiRuleContext) {
        val ktFile = (file as KotlinAnalyzedFile).ktFile
        PsiTreeUtil.collectElementsOfType(ktFile, KtCatchClause::class.java).forEach { catchClause ->
            val body = catchClause.catchBody
            if (body == null || isEffectivelyEmpty(body)) {
                context.report(
                    message = "Empty catch block silently swallows exceptions",
                    location = PsiElementLocator.toLocation(file.path, catchClause),
                    entity = catchClause.catchParameter?.name,
                )
            }
        }
    }

    private fun isEffectivelyEmpty(body: KtExpression): Boolean =
        when (body) {
            is KtBlockExpression -> {
                val statements = body.statements
                statements.isEmpty() || statements.all { it is PsiComment }
            }
            else -> body.text.isBlank()
        }
}
