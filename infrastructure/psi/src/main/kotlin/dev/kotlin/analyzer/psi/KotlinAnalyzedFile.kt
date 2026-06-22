package dev.kotlin.analyzer.psi

import dev.kotlin.analyzer.api.AnalyzedFile
import org.jetbrains.kotlin.psi.KtFile

public class KotlinAnalyzedFile(
    override val path: String,
    override val text: String,
    public val ktFile: KtFile,
) : AnalyzedFile {
    override val psiFile: Any get() = ktFile
}
