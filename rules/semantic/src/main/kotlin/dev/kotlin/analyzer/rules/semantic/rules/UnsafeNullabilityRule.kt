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
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.types.KaTypeNullability
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtVisitorVoid

@RequiresAnalysisApi
public class UnsafeNullabilityRule : AbstractSemanticRule(
    RuleDescriptor(
        id = RuleId("UnsafeNullability"),
        ruleSetId = RuleSetId("builtin.semantic"),
        description = "Reports nullable expressions used without safe calls or null checks",
        defaultSeverity = Severity.WARNING,
        tags = setOf("nullability", "safety"),
        requiresAnalysisApi = true,
    ),
) {
    override fun visitSemantic(file: AnalyzedFile, session: AnalysisSession, context: SemanticRuleContext) {
        val ktFile = (file as KotlinAnalyzedFile).ktFile
        ktFile.accept(
            object : KtVisitorVoid() {
                override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
                    val receiver = expression.receiverExpression
                    analyze(receiver) {
                        val type = receiver.expressionType
                        if (type != null && type.nullability == KaTypeNullability.NULLABLE) {
                            context.report(
                                message = "Nullable receiver used with unsafe dot call; prefer ?. or explicit null check",
                                location = PsiElementLocator.toLocation(file.path, expression),
                            )
                        }
                    }
                    super.visitDotQualifiedExpression(expression)
                }
            },
        )
    }
}
