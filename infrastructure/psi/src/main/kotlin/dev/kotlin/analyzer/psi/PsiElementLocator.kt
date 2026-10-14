package dev.kotlin.analyzer.psi

import com.intellij.psi.PsiElement
import dev.kotlin.analyzer.domain.Location

public object PsiElementLocator {
    public fun toLocation(path: String, element: PsiElement): Location {
        val document = element.containingFile.viewProvider.document
            ?: return Location(path, line = 1, column = 1)

        val startOffset = element.textRange.startOffset
        val endOffset = element.textRange.endOffset
        val startLine = document.getLineNumber(startOffset)
        val startColumn = startOffset - document.getLineStartOffset(startLine)
        val endLine = document.getLineNumber(endOffset)
        val endColumn = endOffset - document.getLineStartOffset(endLine)

        return Location(
            path = path,
            line = startLine + 1,
            column = startColumn + 1,
            endLine = endLine + 1,
            endColumn = endColumn + 1,
        )
    }
}
