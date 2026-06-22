# Kotlin Code Analyzer

[![CI](https://github.com/kotlin-code-analyzer/kotlin-code-analyzer/actions/workflows/ci.yml/badge.svg)](https://github.com/kotlin-code-analyzer/kotlin-code-analyzer/actions/workflows/ci.yml)

A production-quality command-line static analysis tool for Kotlin source code. Built on **Kotlin PSI** and the **Standalone Analysis API** — the same foundations used by the Kotlin compiler and IDE.

## Quick Start

```bash
./gradlew :cli:shadowJar
java -jar cli/build/libs/cli-0.1.0-SNAPSHOT-all.jar analyze src/
```

Initialize a configuration file:

```bash
java -jar cli/build/libs/cli-0.1.0-SNAPSHOT-all.jar init-config
java -jar cli/build/libs/cli-0.1.0-SNAPSHOT-all.jar analyze src/ -c analyzer.yml
```

List available rules:

```bash
java -jar cli/build/libs/cli-0.1.0-SNAPSHOT-all.jar rules list
```

## Built-in Rules

| Rule | Category | Description |
|------|----------|-------------|
| `TodoComment` | PSI | TODO/FIXME/HACK comments |
| `NotNullAssertion` | PSI | `!!` operator usage |
| `EmptyCatchBlock` | PSI | Empty catch blocks |
| `LongMethod` | PSI | Functions exceeding line threshold |
| `NestedBlockDepth` | PSI | Excessive nesting |
| `LargeClass` | PSI | Classes with too many members/lines |
| `UnsafeNullability` | Semantic | Nullable receivers used unsafely |
| `SuspiciousExtension` | Semantic | Public extensions on built-in types |
| `ScopeFunctionMisuse` | Semantic | Misuse of let/apply/run/also |

## Architecture

- **Clean Architecture** with pure domain module (no compiler deps)
- **Tiered rules**: fast PSI syntax rules + semantic Analysis API rules
- **SPI plugin system** for custom rule JARs
- **Multiple report formats**: console, JSON, SARIF, Checkstyle XML

See [docs/architecture.md](docs/architecture.md) for details.

## Development

```bash
./gradlew check          # Run all tests
./gradlew koverXmlReport # Coverage report
```

## License

Apache License 2.0 — see [LICENSE](LICENSE).
