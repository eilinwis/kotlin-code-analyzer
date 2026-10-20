package dev.kotlin.analyzer.psi

import java.nio.file.Path

public class KotlinProjectLoader(
    private val psiEnvironment: KotlinPsiEnvironment,
) {
    public fun loadFiles(paths: List<Path>): List<KotlinAnalyzedFile> =
        paths.map { path ->
            val ktFile = psiEnvironment.parseFile(path)
            KotlinAnalyzedFile(
                path = path.toString(),
                text = ktFile.text,
                ktFile = ktFile,
            )
        }
}
