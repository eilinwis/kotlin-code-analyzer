package dev.kotlin.analyzer.engine

import dev.kotlin.analyzer.domain.Finding
import dev.kotlin.analyzer.domain.Suppression
import dev.kotlin.analyzer.psi.KotlinAnalyzedFile
import org.jetbrains.kotlin.psi.KtAnnotated
import org.jetbrains.kotlin.psi.KtFile

public class SuppressionEngine {
    private val fileSuppressions: MutableMap<String, List<Suppression>> = mutableMapOf()

    public fun indexFile(file: KotlinAnalyzedFile) {
        val suppressions = mutableListOf<Suppression>()
        collectSuppressions(file.ktFile, file.path, suppressions)
        fileSuppressions[file.path] = suppressions
    }

    public fun filter(findings: List<Finding>): List<Finding> =
        findings.filterNot { finding -> isSuppressed(finding) }

    private fun isSuppressed(finding: Finding): Boolean {
        val suppressions = fileSuppressions[finding.location.path].orEmpty()
        return suppressions.any { suppression ->
            val ruleMatches = suppression.ruleId == null || suppression.ruleId == finding.ruleId
            val lineMatches = suppression.line == null || suppression.line == finding.location.line
            ruleMatches && lineMatches
        }
    }

    private fun collectSuppressions(element: KtAnnotated, path: String, sink: MutableList<Suppression>) {
        element.annotationEntries.forEach { annotation ->
            val name = annotation.shortName?.asString() ?: return@forEach
            if (name != "Suppress" && name != "SuppressWarnings") return@forEach

            annotation.valueArguments.forEach { arg ->
                val ruleName = arg.getArgumentExpression()?.text?.trim('"') ?: return@forEach
                sink.add(
                    Suppression(
                        ruleId = dev.kotlin.analyzer.domain.RuleId(ruleName),
                        path = path,
                        line = element.containingFile.let { file ->
                            if (file is KtFile) {
                                val offset = element.textRange.startOffset
                                val doc = file.viewProvider.document ?: return@let null
                                doc.getLineNumber(offset) + 1
                            } else {
                                null
                            }
                        },
                    ),
                )
            }
        }
    }
}
