# Kotlin Code Analyzer

[![JitPack](https://jitpack.io/v/eilinwis/kotlin-code-analyzer.svg)](https://jitpack.io/#eilinwis/kotlin-code-analyzer)
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

### CLI

Download `cli-<version>-all.jar` from [GitHub Releases](https://github.com/eilinwis/kotlin-code-analyzer/releases) and run it with Java 17+:

```bash
java -jar cli-<version>-all.jar analyze ./
```

### Libraries

| Artifact                  | Purpose                                        |
|---------------------------|------------------------------------------------|
| `analyzer-api`            | API for writing custom rules                   |
| `analyzer-domain`         | Findings, locations, severities, configuration |
| `analyzer-psi`            | Kotlin PSI environment and helpers             |
| `analyzer-engine`         | Rule engine                                    |
| `analyzer-testkit`        | Test harness for custom rules                  |
| `analyzer-rules-psi`      | Built-in PSI rules                             |
| `analyzer-rules-semantic` | Built-in semantic rules (Analysis API)         |
| `analyzer-analysis`       | Standalone Analysis API session                |
| `analyzer-config`         | YAML configuration loader                      |
| `analyzer-reporting`      | Report formatters                              |
| `analyzer-cli`            | Command-line interface                         |

**Maven Central**

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    compileOnly("io.github.eilinwis:analyzer-api:<version>")
    testImplementation("io.github.eilinwis:analyzer-testkit:<version>")
}
```

**JitPack** — group is `com.github.eilinwis.kotlin-code-analyzer`, version is a git tag:

```kotlin
repositories {
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("com.github.eilinwis.kotlin-code-analyzer:analyzer-api:v<version>")
}
```

**GitHub Packages** — requires a personal access token with the `read:packages` scope, even for public packages:

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

### Analysis API dependencies

`analyzer-analysis` and `analyzer-rules-semantic` depend on Kotlin Analysis API artifacts that are not published to Maven Central. Add the JetBrains repositories when using these modules:

```kotlin
repositories {
    mavenCentral()
    maven("https://packages.jetbrains.team/maven/p/ij/intellij-dependencies")
    maven("https://redirector.kotlinlang.org/maven/kotlin-ide-plugin-dependencies")
    maven("https://www.jetbrains.com/intellij-repository/releases")
}
```

The PSI-only modules (`analyzer-api`, `analyzer-domain`, `analyzer-psi`, `analyzer-engine`, `analyzer-testkit`, `analyzer-rules-psi`) resolve from Maven Central alone.

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
