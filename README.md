# Kotlin Code Analyzer

A command-line static analysis tool for Kotlin source code. Built on **Kotlin PSI** and the **Standalone Analysis API** — the same foundations used by the Kotlin compiler and IDE.

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

## Development

```bash
./gradlew check          # Run all tests
./gradlew koverXmlReport # Coverage report
```