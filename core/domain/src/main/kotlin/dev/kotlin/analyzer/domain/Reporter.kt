package dev.kotlin.analyzer.domain

import java.nio.file.Path

public enum class ReportFormat {
    CONSOLE,
    JSON,
    SARIF,
    CHECKSTYLE,
}

public interface Reporter {
    public val format: ReportFormat

    public fun report(result: AnalysisResult, output: Path? = null)
}
