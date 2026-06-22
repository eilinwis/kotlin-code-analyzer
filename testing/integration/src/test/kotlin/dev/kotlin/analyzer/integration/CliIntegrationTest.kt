package dev.kotlin.analyzer.integration

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.nio.file.Path
import kotlin.io.path.Path

class CliIntegrationTest {
    @Test
    fun `cli analyze finds issues in bad project`() {
        val projectRoot = Path(System.getProperty("project.root", System.getProperty("user.dir")))
        val badProject = projectRoot.resolve("samples/bad-project/src/main/kotlin")
        val jar = projectRoot.resolve("cli/build/libs/cli-0.1.0-SNAPSHOT-all.jar")

        val process = ProcessBuilder(
            "java",
            "-jar",
            jar.toString(),
            "analyze",
            badProject.toString(),
            "--report",
            "json",
            "--fail-on",
            "error",
        )
            .directory(projectRoot.toFile())
            .redirectErrorStream(true)
            .start()

        val output = process.inputStream.bufferedReader().readText()
        val exitCode = process.waitFor()

        expectThat(exitCode).isEqualTo(1)
        expectThat(output.contains("NotNullAssertion") || output.contains("EmptyCatchBlock")).isEqualTo(true)
    }
}
