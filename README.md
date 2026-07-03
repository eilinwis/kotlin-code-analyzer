# Kotlin Code Analyzer

A command-line static analysis tool for Kotlin source code. Built on **Kotlin PSI** and the **Standalone Analysis API** — the same foundations used by the Kotlin compiler and IDE.

## Quick Start

```bash
./gradlew :cli:shadowJar
java -jar cli/build/libs/cli-1.0.0-all.jar analyze ./
```

Initialize a configuration file:

```bash
java -jar cli/build/libs/cli-1.0.0-all.jar init-config
```

List available rules:

```bash
java -jar cli/build/libs/cli-1.0.0-all.jar rules list
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
