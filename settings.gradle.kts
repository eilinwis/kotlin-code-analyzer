pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        mavenCentral()
        maven("https://packages.jetbrains.team/maven/p/ij/intellij-dependencies")
        maven("https://redirector.kotlinlang.org/maven/kotlin-ide-plugin-dependencies")
        maven("https://www.jetbrains.com/intellij-repository/releases")
    }
}

rootProject.name = "kotlin-code-analyzer"

include(
    ":core:domain",
    ":core:api",
    ":core:engine",
    ":infrastructure:psi",
    ":infrastructure:analysis",
    ":infrastructure:config",
    ":infrastructure:reporting",
    ":rules:builtin-psi",
    ":rules:semantic",
    ":cli",
    ":testing:testkit",
    ":testing:integration",
    ":testing:rule-tests",
    ":samples:custom-rules",
)

project(":rules:builtin-psi").projectDir = file("rules/psi")

project(":rules:builtin-psi").projectDir = file("rules/psi")
