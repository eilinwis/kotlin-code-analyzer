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
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtVisitorVoid

@RequiresAnalysisApi
public class SuspiciousExtensionRule : AbstractSemanticRule(
    RuleDescriptor(
        id = RuleId("SuspiciousExtension"),
        ruleSetId = RuleSetId("builtin.semantic"),
        description = "Reports public extension functions on built-in types that may pollute the API surface",
        defaultSeverity = Severity.WARNING,
        tags = setOf("api-design", "extensions"),
        requiresAnalysisApi = true,
    ),
) {
    private val suspiciousReceivers = setOf(
        "String", "Int", "Long", "Boolean", "Double", "Float", "Any", "Unit", "List", "Map", "Set",
    )

    override fun visitSemantic(file: AnalyzedFile, session: AnalysisSession, context: SemanticRuleContext) {
        val ktFile = (file as KotlinAnalyzedFile).ktFile
        ktFile.accept(
            object : KtVisitorVoid() {
                override fun visitNamedFunction(function: KtNamedFunction) {
                    if (function.receiverTypeReference == null) {
                        super.visitNamedFunction(function)
                        return
                    }

                    val receiverType = function.receiverTypeReference?.text?.trim()
                    if (receiverType != null && suspiciousReceivers.any { receiverType == it || receiverType.endsWith(".$it") }) {
                        val isPublic = function.hasModifier(KtTokens.PUBLIC_KEYWORD) ||
                            !function.hasModifier(KtTokens.PRIVATE_KEYWORD) &&
                            !function.hasModifier(KtTokens.INTERNAL_KEYWORD) &&
                            !function.hasModifier(KtTokens.PROTECTED_KEYWORD)

                        if (isPublic) {
                            context.report(
                                message = "Public extension on built-in type '$receiverType' may be confusing; consider a top-level function or wrapper type",
                                location = PsiElementLocator.toLocation(file.path, function),
                                entity = function.name,
                            )
                        }
                    }
                    super.visitNamedFunction(function)
                }
            },
        )
    }
}
