// Bundles all analyzer modules into the single published artifact io.github.eilinwis:kotlin-code-analyzer.
plugins {
    kotlin("jvm")
    alias(libs.plugins.kotlin.serialization)
}

val bundledModules = listOf(
    ":core:domain",
    ":core:api",
    ":core:engine",
    ":infrastructure:psi",
    ":infrastructure:analysis",
    ":infrastructure:config",
    ":infrastructure:reporting",
    ":rules:builtin-psi",
    ":rules:semantic",
    ":testing:testkit",
    ":cli",
)

// Test-only and CLI-runtime libraries that must not leak into consumers of the library.
val excludedGroups = setOf("org.junit.jupiter", "io.strikt", "ch.qos.logback")

// Libraries whose types appear in the public API (KtFile, PsiElement) are exposed to consumers' compile classpath.
val apiLibraries = setOf("org.jetbrains.kotlin:kotlin-compiler")

bundledModules.forEach { evaluationDependsOn(it) }

kotlin {
    sourceSets.main {
        bundledModules.forEach { kotlin.srcDir(project(it).file("src/main/kotlin")) }
    }
}

val bundledProjects = bundledModules.map { project(it) }

dependencies {
    listOf("api", "implementation", "runtimeOnly").forEach { configuration ->
        bundledProjects
            .flatMap { it.configurations[configuration].dependencies.withType<ExternalModuleDependency>() }
            .filter { it.group !in excludedGroups }
            // A library declared both with and without transitive dependencies keeps the transitive declaration.
            .groupBy { "${it.group}:${it.name}" }
            .values
            .map { declarations -> declarations.firstOrNull { it.isTransitive } ?: declarations.first() }
            .forEach { add(if ("${it.group}:${it.name}" in apiLibraries) "api" else configuration, it.copy()) }
    }
}

// Ship the runnable CLI in the same package: kotlin-code-analyzer-<version>-all.jar
afterEvaluate {
    extensions.configure<PublishingExtension> {
        publications.withType<MavenPublication>().configureEach {
            artifact(project(":cli").tasks.named("shadowJar")) {
                classifier = "all"
            }
        }
    }
}
