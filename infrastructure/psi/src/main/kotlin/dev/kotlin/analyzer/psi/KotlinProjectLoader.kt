package dev.kotlin.analyzer.psi

import java.nio.file.Path

class KotlinProjectLoader(
    private val psiEnvironment: KotlinPsiEnvironment,
) {
    fun loadFiles(paths: List<Path>): List<KotlinAnalyzedFile> =
        paths.map { path ->
            val ktFile = psiEnvironment.parseFile(path)
            KotlinAnalyzedFile(
                path = path.toString(),
                text = ktFile.text,
                ktFile = ktFile,
            )
        }
}
