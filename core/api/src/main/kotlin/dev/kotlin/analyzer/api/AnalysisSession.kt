package dev.kotlin.analyzer.api

public interface AnalyzedFile {
    public val path: String
    public val text: String
    public val psiFile: Any
}

public interface AnalysisSession {
    public fun <T> analyze(file: AnalyzedFile, block: () -> T): T
}
