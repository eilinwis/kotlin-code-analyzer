package dev.kotlin.analyzer.domain

import java.nio.file.Path

data class ProjectDescriptor(
    val root: Path,
    val sourceRoots: List<Path>,
    val classpath: List<Path> = emptyList(),
)
