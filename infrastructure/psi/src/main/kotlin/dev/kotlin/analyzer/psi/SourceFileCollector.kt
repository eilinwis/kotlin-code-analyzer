package dev.kotlin.analyzer.psi

import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isRegularFile

public class SourceFileCollector(
    private val includes: List<String> = listOf("**/*.kt"),
    private val excludes: List<String> = emptyList(),
) {
    public fun collect(paths: List<Path>): List<Path> {
        val fileSystem = FileSystems.getDefault()
        val includeMatchers = includes.map { fileSystem.getPathMatcher("glob:$it") }
        val excludeMatchers = excludes.map { fileSystem.getPathMatcher("glob:$it") }

        return paths.flatMap { path ->
            when {
                path.isRegularFile() -> listOf(path)
                Files.isDirectory(path) -> Files.walk(path).use { stream ->
                    stream
                        .filter { it.isRegularFile() }
                        .filter { file -> includeMatchers.any { it.matches(file) } }
                        .filter { file -> excludeMatchers.none { it.matches(file) } }
                        .toList()
                }
                else -> emptyList()
            }
        }.distinct().sorted()
    }
}
