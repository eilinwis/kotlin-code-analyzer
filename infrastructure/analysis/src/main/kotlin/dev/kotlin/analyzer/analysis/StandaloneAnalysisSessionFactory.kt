package dev.kotlin.analyzer.analysis

import com.intellij.openapi.util.Disposer
import dev.kotlin.analyzer.api.AnalysisSession
import dev.kotlin.analyzer.api.AnalyzedFile
import dev.kotlin.analyzer.psi.KotlinAnalyzedFile
import dev.kotlin.analyzer.psi.KotlinPsiEnvironment
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.standalone.StandaloneAnalysisAPISession
import org.jetbrains.kotlin.analysis.api.standalone.buildStandaloneAnalysisAPISession
import org.jetbrains.kotlin.analysis.project.structure.builder.buildKtSdkModule
import org.jetbrains.kotlin.analysis.project.structure.builder.buildKtSourceModule
import org.jetbrains.kotlin.platform.jvm.JvmPlatforms
import java.nio.file.Path
import java.nio.file.Paths

public class StandaloneAnalysisSessionFactory : AutoCloseable {
    private val disposable = Disposer.newDisposable("StandaloneAnalysisSessionFactory")
    private var aaSession: StandaloneAnalysisAPISession? = null

    public fun createSession(
        @Suppress("UNUSED_PARAMETER") psiEnvironment: KotlinPsiEnvironment,
        sourceFiles: List<KotlinAnalyzedFile>,
    ): AnalysisSession {
        aaSession?.let { return StandaloneKotlinAnalysisSession(it) }

        val sourceRoots = sourceFiles
            .map { Paths.get(it.path).parent }
            .filterNotNull()
            .distinct()

        val session = buildStandaloneAnalysisAPISession(disposable, unitTestMode = true) {
            buildKtModuleProvider {
                platform = JvmPlatforms.defaultJvmPlatform

                val sdk = buildKtSdkModule {
                    addBinaryRootsFromJdkHome(Paths.get(System.getProperty("java.home")), isJre = true)
                    platform = JvmPlatforms.defaultJvmPlatform
                    libraryName = "jdk"
                }

                addModule(
                    buildKtSourceModule {
                        moduleName = "analyzer-sources"
                        platform = JvmPlatforms.defaultJvmPlatform
                        sourceRoots.forEach { addSourceRoot(it) }
                        addRegularDependency(sdk)
                    },
                )
            }
        }

        aaSession = session
        return StandaloneKotlinAnalysisSession(session)
    }

    override fun close() {
        Disposer.dispose(disposable)
        aaSession = null
    }
}

private class StandaloneKotlinAnalysisSession(
    @Suppress("UNUSED_PARAMETER") private val aaSession: StandaloneAnalysisAPISession,
) : AnalysisSession {
    override fun <T> analyze(file: AnalyzedFile, block: () -> T): T {
        val ktFile = (file as KotlinAnalyzedFile).ktFile
        return analyze(ktFile) {
            block()
        }
    }
}
