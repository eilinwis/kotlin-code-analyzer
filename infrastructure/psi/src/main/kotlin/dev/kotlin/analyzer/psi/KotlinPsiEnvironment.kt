package dev.kotlin.analyzer.psi

import com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtPsiFactory
import java.nio.file.Path
import kotlin.io.path.name
import kotlin.io.path.readText

public class KotlinPsiEnvironment : AutoCloseable {
    private val disposable = Disposer.newDisposable("KotlinPsiEnvironment")
    private val environment = KotlinCoreEnvironment.createForProduction(
        disposable,
        CompilerConfiguration(),
        EnvironmentConfigFiles.JVM_CONFIG_FILES,
    )

    public val project get() = environment.project
    private val psiFactory = KtPsiFactory(project, eventSystemEnabled = false)

    public fun parseFile(path: Path): KtFile {
        val text = path.readText()
        return parseText(path.toString(), text)
    }

    public fun parseText(path: String, text: String): KtFile {
        val fileName = Path.of(path).name
        return psiFactory.createFile(fileName, text)
    }

    override fun close() {
        Disposer.dispose(disposable)
    }
}
