# Contributing

1. Fork the repository
2. Create a feature branch
3. Add tests for new rules
4. Run `./gradlew check`
5. Open a pull request

## Adding a new rule

1. Implement in `rules/psi` or `rules/semantic`
2. Register in the appropriate `RuleContributor`
3. Add configuration to `config/default-config.yml`
4. Add rule-validation tests
5. Document in `docs/rules/`
