# Kotlin Code Analyzer

[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)

A command-line static analysis tool for Kotlin source code. Built on **Kotlin PSI** and the **Standalone Analysis API** — the same foundations used by the Kotlin compiler and IDE.

## Quick Start

```bash
./gradlew :cli:shadowJar
java -jar cli/build/libs/cli-*-all.jar analyze ./
```

Initialize a configuration file:

```bash
java -jar cli/build/libs/cli-*-all.jar init-config
```

List available rules:

```bash
java -jar cli/build/libs/cli-*-all.jar rules list
```

## Installation

Everything ships as a single artifact `io.github.eilinwis:kotlin-code-analyzer`: rule API, engine, built-in rules, configuration, reporting, test kit and CLI.

### CLI

Download `kotlin-code-analyzer-<version>-all.jar` from Maven Central or `cli-<version>-all.jar` from [GitHub Releases](https://github.com/eilinwis/kotlin-code-analyzer/releases) and run it with Java 17+:

```bash
java -jar kotlin-code-analyzer-<version>-all.jar analyze ./
```

### Library

The Kotlin Analysis API is not published to Maven Central, so add the JetBrains repositories next to it:

```kotlin
repositories {
    mavenCentral()
    maven("https://packages.jetbrains.team/maven/p/ij/intellij-dependencies")
    maven("https://redirector.kotlinlang.org/maven/kotlin-ide-plugin-dependencies")
}

dependencies {
    implementation("io.github.eilinwis:kotlin-code-analyzer:<version>")
}
```

**GitHub Packages** — the same artifact is published there too; it requires a personal access token with the `read:packages` scope, even for public packages:

```kotlin
repositories {
    maven("https://maven.pkg.github.com/eilinwis/kotlin-code-analyzer") {
        credentials {
            username = providers.gradleProperty("gpr.user").get()
            password = providers.gradleProperty("gpr.key").get()
        }
    }
}
```

## Built-in Rules

| Rule                  | Category | Description                         |
|-----------------------|----------|-------------------------------------|
| `TodoComment`         | PSI      | TODO/FIXME/HACK comments            |
| `NotNullAssertion`    | PSI      | `!!` operator usage                 |
| `EmptyCatchBlock`     | PSI      | Empty catch blocks                  |
| `LongMethod`          | PSI      | Functions exceeding line threshold  |
| `NestedBlockDepth`    | PSI      | Excessive nesting                   |
| `LargeClass`          | PSI      | Classes with too many members/lines |
| `UnUsedImport`        | PSI      | Unused imports                      |
| `UnsafeNullability`   | Semantic | Nullable receivers used unsafely    |
| `SuspiciousExtension` | Semantic | Public extensions on built-in types |
| `ScopeFunctionMisuse` | Semantic | Misuse of let/apply/run/also        |
