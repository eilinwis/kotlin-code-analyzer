package dev.kotlin.analyzer.rules.psi.rules

import com.intellij.psi.util.PsiTreeUtil
import dev.kotlin.analyzer.api.AbstractPsiRule
import dev.kotlin.analyzer.api.AnalyzedFile
import dev.kotlin.analyzer.api.PsiRuleContext
import dev.kotlin.analyzer.domain.*
import dev.kotlin.analyzer.psi.KotlinAnalyzedFile
import org.jetbrains.kotlin.psi.KtImportDirective
import org.jetbrains.kotlin.psi.KtNameReferenceExpression

class UnusedImportRule : AbstractPsiRule(
    RuleDescriptor(
        id = RuleId("UnusedImport"),
        ruleSetId = RuleSetId("builtin.psi"),
        description = "Reports unused imports",
        defaultSeverity = Severity.WARNING,
        tags = setOf("readability")
    ),
) {

    override fun visitPsi(file: AnalyzedFile, context: PsiRuleContext) {
        val ktFile = (file as KotlinAnalyzedFile).ktFile
        val usedNames = buildSet {
            ktFile.declarations.forEach { declaration ->
                PsiTreeUtil.collectElementsOfType(
                    declaration,
                    KtNameReferenceExpression::class.java
                ).forEach { reference ->
                    add(reference.getReferencedName())
                }
            }
        }

        ktFile.importDirectives
            .filterNot { it.isAllUnder }
            .forEach { importDirective ->

                val importedName =
                    importDirective.aliasName
                        ?: importDirective.importPath?.importedName?.asString()
                        ?: return@forEach


                if (importedName !in usedNames) {
                    context.report(
                        message = "Unused import '$importedName'",
                        location = Location(
                            path = file.path,
                            line = importDirective.getLineNumber(),
                            column = 1
                        )
                    )
                }
            }
    }

    private fun KtImportDirective.getLineNumber(): Int {
        val document = containingFile.viewProvider.document ?: return 1
        return document.getLineNumber(textOffset) + 1
    }
}