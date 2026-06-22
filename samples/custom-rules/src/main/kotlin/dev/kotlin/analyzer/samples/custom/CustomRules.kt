package dev.kotlin.analyzer.samples.custom

import dev.kotlin.analyzer.api.AbstractPsiRule
import dev.kotlin.analyzer.api.AnalyzedFile
import dev.kotlin.analyzer.api.PsiRuleContext
import dev.kotlin.analyzer.api.RuleContributor
import dev.kotlin.analyzer.api.RuleConfigurationSchema
import dev.kotlin.analyzer.domain.RuleDescriptor
import dev.kotlin.analyzer.domain.RuleId
import dev.kotlin.analyzer.domain.RuleSetId
import dev.kotlin.analyzer.domain.Severity
import dev.kotlin.analyzer.psi.KotlinAnalyzedFile
import dev.kotlin.analyzer.psi.PsiElementLocator
import org.jetbrains.kotlin.psi.KtVisitorVoid

public class CustomRuleContributor : RuleContributor {
    override val id: RuleSetId = RuleSetId("com.example.custom")

    override fun contribute(schema: RuleConfigurationSchema): List<dev.kotlin.analyzer.api.Rule> =
        listOf(WhileTrueRule())
}

public class WhileTrueRule : AbstractPsiRule(
    RuleDescriptor(
        id = RuleId("WhileTrue"),
        ruleSetId = RuleSetId("com.example.custom"),
        description = "Reports while(true) loops that may indicate unclear control flow",
        defaultSeverity = Severity.WARNING,
    ),
) {
    override fun visitPsi(file: AnalyzedFile, context: PsiRuleContext) {
        val ktFile = (file as KotlinAnalyzedFile).ktFile
        ktFile.accept(
            object : KtVisitorVoid() {
                override fun visitWhileExpression(expression: org.jetbrains.kotlin.psi.KtWhileExpression) {
                    if (expression.condition?.text == "true") {
                        context.report(
                            message = "Avoid while(true); use a labeled loop or sequence generation",
                            location = PsiElementLocator.toLocation(file.path, expression),
                        )
                    }
                    super.visitWhileExpression(expression)
                }
            },
        )
    }
}
