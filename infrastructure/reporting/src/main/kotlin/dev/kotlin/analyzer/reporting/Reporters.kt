package dev.kotlin.analyzer.reporting

import dev.kotlin.analyzer.domain.AnalysisResult
import dev.kotlin.analyzer.domain.Finding
import dev.kotlin.analyzer.domain.ReportFormat
import dev.kotlin.analyzer.domain.Reporter
import dev.kotlin.analyzer.domain.Severity
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.nio.file.Path
import kotlin.io.path.writeText

public class ConsoleReporter : Reporter {
    override val format: ReportFormat = ReportFormat.CONSOLE

    override fun report(result: AnalysisResult, output: Path?) {
        if (result.findings.isEmpty()) {
            println("No issues found. Analyzed ${result.filesAnalyzed} file(s) in ${result.durationMs}ms.")
            return
        }

        result.findings.forEach { finding ->
            val color = when (finding.severity) {
                Severity.ERROR -> "\u001B[31m"
                Severity.WARNING -> "\u001B[33m"
                Severity.INFO -> "\u001B[36m"
            }
            println(
                "${finding.location.format()}: $color${finding.severity}\u001B[0m " +
                    "[${finding.ruleId}] ${finding.message}",
            )
        }

        println()
        printSummary(result)
    }

    private fun printSummary(result: AnalysisResult) {
        val bySeverity = result.findings.groupingBy { it.severity }.eachCount()
        println(
            "Summary: ${result.findings.size} issue(s) in ${result.filesAnalyzed} file(s), " +
                "${result.rulesExecuted} rule(s), ${result.durationMs}ms",
        )
        Severity.entries.forEach { severity ->
            bySeverity[severity]?.let { count ->
                println("  $severity: $count")
            }
        }
    }
}

public class JsonReporter : Reporter {
    override val format: ReportFormat = ReportFormat.JSON

    private val json = Json { prettyPrint = true }

    override fun report(result: AnalysisResult, output: Path?) {
        val payload = JsonReport.from(result)
        val text = json.encodeToString(payload)
        if (output != null) {
            val file = output.resolve("analyzer-report.json")
            file.writeText(text)
            println("JSON report written to $file")
        } else {
            println(text)
        }
    }
}

@Serializable
internal data class JsonReport(
    val filesAnalyzed: Int,
    val rulesExecuted: Int,
    val durationMs: Long,
    val findings: List<JsonFinding>,
) {
    companion object {
        fun from(result: AnalysisResult): JsonReport = JsonReport(
            filesAnalyzed = result.filesAnalyzed,
            rulesExecuted = result.rulesExecuted,
            durationMs = result.durationMs,
            findings = result.findings.map { JsonFinding.from(it) },
        )
    }
}

@Serializable
internal data class JsonFinding(
    val ruleId: String,
    val message: String,
    val severity: String,
    val path: String,
    val line: Int,
    val column: Int,
    val entity: String? = null,
) {
    companion object {
        fun from(finding: Finding): JsonFinding = JsonFinding(
            ruleId = finding.ruleId.value,
            message = finding.message,
            severity = finding.severity.name,
            path = finding.location.path,
            line = finding.location.line,
            column = finding.location.column,
            entity = finding.entity,
        )
    }
}

public class SarifReporter(
    private val toolVersion: String = "0.1.0",
) : Reporter {
    override val format: ReportFormat = ReportFormat.SARIF

    override fun report(result: AnalysisResult, output: Path?) {
        val sarif = buildSarif(result)
        if (output != null) {
            val file = output.resolve("analyzer-report.sarif")
            file.writeText(sarif)
            println("SARIF report written to $file")
        } else {
            println(sarif)
        }
    }

    private fun buildSarif(result: AnalysisResult): String {
        val rules = result.findings.map { it.ruleId.value }.distinct().sorted()
        val rulesJson = rules.joinToString(",\n") { ruleId ->
            """      {
        "id": "$ruleId",
        "name": "$ruleId",
        "shortDescription": { "text": "$ruleId" },
        "helpUri": "https://github.com/kotlin-code-analyzer/docs/rules/$ruleId.md"
      }"""
        }

        val resultsJson = result.findings.joinToString(",\n") { finding ->
            val level = when (finding.severity) {
                Severity.ERROR -> "error"
                Severity.WARNING -> "warning"
                Severity.INFO -> "note"
            }
            """      {
        "ruleId": "${finding.ruleId.value}",
        "level": "$level",
        "message": { "text": ${jsonEscape(finding.message)} },
        "locations": [{
          "physicalLocation": {
            "artifactLocation": { "uri": ${jsonEscape(finding.location.path)} },
            "region": {
              "startLine": ${finding.location.line},
              "startColumn": ${finding.location.column}
            }
          }
        }]
      }"""
        }

        return """
{
  "${"$"}schema": "https://raw.githubusercontent.com/oasis-tcs/sarif-spec/master/Schemata/sarif-schema-2.1.0.json",
  "version": "2.1.0",
  "runs": [{
    "tool": {
      "driver": {
        "name": "kotlin-code-analyzer",
        "version": "$toolVersion",
        "informationUri": "https://github.com/kotlin-code-analyzer",
        "rules": [
$rulesJson
        ]
      }
    },
    "results": [
$resultsJson
    ]
  }]
}
""".trimIndent()
    }

    private fun jsonEscape(value: String): String =
        "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\""
}

public class CheckstyleXmlReporter : Reporter {
    override val format: ReportFormat = ReportFormat.CHECKSTYLE

    override fun report(result: AnalysisResult, output: Path?) {
        val xml = buildString {
            appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")
            appendLine("""<checkstyle version="10.0">""")
            result.findings.groupBy { it.location.path }.forEach { (path, findings) ->
                appendLine("""  <file name="$path">""")
                findings.forEach { finding ->
                    appendLine(
                        """    <error line="${finding.location.line}" column="${finding.location.column}" """ +
                            """severity="${finding.severity.name.lowercase()}" message="${escapeXml(finding.message)}" """ +
                            """source="${finding.ruleId.value}"/>""",
                    )
                }
                appendLine("  </file>")
            }
            appendLine("</checkstyle>")
        }

        if (output != null) {
            val file = output.resolve("analyzer-checkstyle.xml")
            file.writeText(xml)
            println("Checkstyle report written to $file")
        } else {
            println(xml)
        }
    }

    private fun escapeXml(value: String): String =
        value.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
}

public class ReporterFactory {
    public fun create(formats: Set<ReportFormat>): List<Reporter> =
        formats.map { format ->
            when (format) {
                ReportFormat.CONSOLE -> ConsoleReporter()
                ReportFormat.JSON -> JsonReporter()
                ReportFormat.SARIF -> SarifReporter()
                ReportFormat.CHECKSTYLE -> CheckstyleXmlReporter()
            }
        }
}

public fun parseReportFormats(values: List<String>): Set<ReportFormat> =
    values.flatMap { value ->
        value.split(",").map { it.trim().uppercase() }
    }.map { name ->
        ReportFormat.valueOf(name)
    }.toSet()
