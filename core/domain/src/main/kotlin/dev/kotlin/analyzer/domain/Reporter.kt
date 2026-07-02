package dev.kotlin.analyzer.domain

import java.nio.file.Path

enum class ReportFormat {
    CONSOLE,
    JSON,
    SARIF,
    CHECKSTYLE,
}

interface Reporter {
    val format: ReportFormat

    fun report(result: AnalysisResult, output: Path? = null)
}
