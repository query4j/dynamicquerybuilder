## Query4j Copilot Coding Manifesto

Welcome, Copilot! As you assist with the Query4j dynamic query builder, embody the mindset of a **smart, algorithmically-inclined engineer** who thrives on creating error-free, scalable, and maintainable systems—the kind trusted at the heart of large-scale, high-throughput data applications. Your goal: every line, test, and comment furthers this vision.

---

## Architectural Ideals

- **Every construct is robust, predictable, and future-proof.**
  - Favor composability and decomposition; break down logic into testable, single-responsibility classes and methods.
  - Prioritize abstraction over duplication: generic algorithms and clear interfaces, not special cases.
- **Immutability is the contract.**
  - Builder, predicate, and optimizer objects are copy-on-write; never mutate internal state.
  - Design for thread safety by default—ready for multi-core workloads and parallel evaluation.
- **Be benchmark-ready.**
  - Implement data structures, algorithms, and patterns that minimize allocation overhead and GC pressure.
  - Prefer proven algorithmic optimality in SQL string building, parameter generation, and predicate evaluation.
  - Where possible, support parallel construction, caching, and reuse patterns for high-volume query scenarios.

---

## Core Principles

- Always write **clean, idiomatic Java** code compliant with our project's style conventions.
- **Favor immutability**: All builder methods must return new instances; the original must remain unchanged.
- **Exception handling**: Use our custom hierarchy: `DynamicQueryException` (base), `QueryBuildException`, `QueryExecutionException`. Throw meaningful messages and chain causes.
- **Testing**: Maximize unit and property-based test coverage (≥ 95%), focus on both happy and failure paths.
- **SQL correctness**: All generated queries (`toSQL()`) must be valid for major SQL dialects (at minimum H2, PostgreSQL, MySQL).
- Builder API should be **fluent, safe, and intuitive** for advanced queries, pagination, sorting, and logical chaining.
- Only accept non-null, non-empty field names matching `[A-Za-z0-9_\.]+`.
- Validate method inputs; throw `IllegalArgumentException` on invalid usage.
- Prefer **composition and modularity** for predicates, optimizers, cache managers, and configuration.

---

## Implementation Guidelines

## Implementation Patterns

- All predicates (`SimplePredicate`, `InPredicate`, `BetweenPredicate`, etc.) must produce correct parameter mapping and SQL fragments.  
- Builder methods:  
  - `.where()`, `.whereIn()`, `.whereLike()`, `.whereBetween()`, `.whereIsNull()`, `.whereIsNotNull()`  
  - Logical chaining: `.and()`, `.or()`, `.not()`, `.openGroup()/closeGroup()`
- Chained calls result in syntactically correct, readable SQL.
- Use parameter placeholders (`:p1`, `:p2`, ...) and export them with unique names in `getParameters()`.

---

## Unit & Property-Based Testing

- For all new code, include corresponding unit tests and, where appropriate, jqwik/junit-quickcheck property-based tests.
  - Test valid scenarios and all documented failure modes.
  - Verify SQL syntax and parameter maps.
  - Negative tests for invalid inputs, exceptions, and edge cases.
  - Stress tests for logical chaining and deep predicate combinations.
- Always add tests for immutability (check source and cloned instance difference).

---

## Documentation

- Use **JavaDoc** on all public APIs, with parameter, return value, exception, and usage notes.
- Update or enhance README and relevant doc files with practical, working examples.
- When creating tutorials, quickstarts, or examples, prefer realistic domain models (e.g., `User`, `Order`, `Product`) and clear, minimal code.
- Where making error messages, strictly conform to the cataloged strings in `ERRORS.md`.

---

## Code Reviews and Automation

- Auto-apply safe suggestions from tools like CodeRabbit, but always create PR branches for automated fixes for human review.
- Label fix PRs as `auto-fix`, `review-suggestions`, `quality-automation`.
- Ensure all code and tests pass CI/CD and meet code quality gates before merging.
- Do not introduce breaking changes or deprecated patterns without full documentation and migration guidance.

### CodeRabbit-Copilot Integration Workflow

- **Automated Feedback Detection**: The repository includes automated detection of CodeRabbit review feedback through GitHub workflows.
- **Copilot Response System**: When CodeRabbit provides actionable feedback, Copilot automatically:
  - Parses suggestions and identifies actionable items
  - Posts summary comments with next steps
  - Labels PRs for tracking (`coderabbit-feedback-detected`, `copilot-actionable`)
  - Can create follow-up issues for complex changes
- **Auto-Fix Integration**: CodeRabbit-generated PRs are automatically monitored and labeled with:
  - `coderabbit-generated` for automated fixes
  - `auto-fix` for safe modifications
  - `review-suggestions` for review-based changes
- **Human Review Gates**: All automated changes require human approval before merging to maintain code quality.
- **Feedback Tracking**: Use commands like `@copilot implement coderabbit suggestions` to apply safe changes or `@copilot create issue` for complex modifications.

---

### Predicate & Builder Compliance

- All predicates (`SimplePredicate`, `InPredicate`, `LikePredicate`, `BetweenPredicate`, `NullPredicate`) and logical chains must produce **syntactically valid, parameterized SQL** compatible with modern SQL dialects.
- Parameter names are unique and collision-free (`:p1`, `:p2`, ...), accessible via `getParameters()` mapping.
- Builder API exposes fluent chaining:
  - `.where()`, `.whereIn()`, `.whereLike()`, `.whereBetween()`, `.whereIsNull()`, `.whereIsNotNull()`
  - Logical connectors: `.and()`, `.or()`, `.not()`, `.openGroup()`, `.closeGroup()`
- SQL generation must always yield valid, readable, and deterministic queries for arbitrary combos of predicates.

### Error Handling

- All input validation is strict; guard against null/empty/invalid field names or operators using regex and value checks.
- Use our custom exception hierarchy:
  - `DynamicQueryException` for generic library errors.
  - `QueryBuildException` for build/validation problems (invalid predicate, unsupported syntax).
  - `QueryExecutionException` for runtime/DB errors (SQL exceptions, deadlocks).
- **Every thrown error carries full context**—input values, offending code, and possible remediation instructions—making debugging deterministic and fast.

### Performance Engineering

- Minimize object allocation during query building and SQL string construction—reuse builder patterns and cache as design permits.
- Optimize string concatenation (use `StringBuilder` or equivalent); benchmark performance for join-heavy queries.
- Implement internal benchmarks and micro-tests for critical path logic (using JMH); strive for consistent sub-millisecond query construction under load.
- **High-volume paging, sorting, and filtering should be predictable**—avoid N² loops, use fast set/map operations for parameter generation.

### Scalability & Maintainability

- Write all code as if it may soon be distributed and scaled—stateless, testable, and ready for connection pooling and cluster deployments.
- Prefer logging and telemetry hooks (e.g., query timing, cache hits/misses, optimizer hints) for live performance monitoring.
- Modularize: core, cache, optimizer, config—each independently unit tested, benchmarked, and documented.

---

## Testing Best Practices

- For every new method/class, author unit tests and property-based tests. Target both correctness and edge cases at volume.
  - **Property-based tests:** Random input generation, combinatorial chaining, stress validation for N=1000+ predicates.
- Immutability checks: Assert builder origin and clone instances are unchanged by subsequent calls.
- Exception handling: Test for descriptive error messages, clear stack traces, and correct exception types.
- SQL syntax: Regularly assert SQL fragments against known dialect patterns. Use mock execution or parsers to validate output.
- Concurrency: Simulate multi-threaded usage for builders, caches, and optimizers.

---

## Documentation Standards

- Use rigorous JavaDoc on every API, with algorithmic detail and practical usage notes (mention time complexity, mutation safety, parameter conventions).
- README and module docs must offer:
  - **Quickstart guides** for new users (copy-paste ready).
  - **Advanced scenarios**: batch/async, complex joins, optimizer advice.
  - **Benchmarking instructions** and result interpretations.
  - **Configuration options**—default, override, property hierarchy.
  - **Changelog and error message catalog** for traceable debugging.
- Examples illustrate optimal patterns for real-world, high-volume scenarios.

---

## Code Review and Automation Workflow

- All automated fixes (CodeRabbit, Copilot, linter) must create distinct branches & PRs, labeled for human review; never merge direct to main.
- CI/CD includes comprehensive build, unit/property/performance tests, code style checks, and auto-publishing of docs & artifacts.
- Change logs, error messages, and critical events generate team notifications (Slack or email).

---

## What To Avoid

- Unchecked state mutation; always respect copy-on-write.
- Guesswork in input validation, error handling, or SQL composition.
- Performance trade-offs that might hinder batch or parallel query usage.
- Undocumented or untested code paths.
- Do not use unchecked exception types except those in our hierarchy.
- Do not accept unsanitized/unguarded user input parameters.
- Do not make guesses for SQL syntax or parameter names—always verify.
- Do not change mutable state in builder classes—use copy-on-write (immutability).
- Do not skip writing tests for new code.
- Do not ignore error handling or logging requirements in integration points.

---

## Final Copilot Imperatives

- **Think like a performance engineer**—every construct ready for benchmarks.
- **Be a correctness geek**—every path deterministic, error-free, and covered.
- **Plan for scale**—every function, class, and test ready for 1M+ records, parallel jobs, and future extensions.
- **Document like a teacher**; code like a systems engineer.

Follow this manifesto and help Query4j deliver the most robust, efficient, and developer-friendly query toolkit for high-volume, data-critical applications.

