# Contributing to Query4j

Thank you for your interest in contributing to Query4j! This guide will help you get started.

## Table of Contents

1. [Getting Started](#getting-started)
2. [Development Setup](#development-setup)
3. [Contribution Workflow](#contribution-workflow)
4. [Coding Standards](#coding-standards)
5. [Testing Guidelines](#testing-guidelines)
6. [Documentation](#documentation)
7. [Pull Request Process](#pull-request-process)

---

## Getting Started

### Ways to Contribute

- 🐛 **Report bugs** - Submit detailed bug reports
- ✨ **Suggest features** - Propose new capabilities
- 📝 **Improve documentation** - Fix typos, add examples
- 🧪 **Write tests** - Increase test coverage
- 💻 **Submit code** - Fix bugs, implement features
- 🔍 **Review PRs** - Help review pull requests
- 💬 **Answer questions** - Help in Discussions

### Before You Start

1. Check [existing issues](https://github.com/query4j/dynamicquerybuilder/issues)
2. Read [Code of Conduct](https://github.com/query4j/dynamicquerybuilder/blob/master/CODE_OF_CONDUCT.md)
3. Review [Architecture Overview](#architecture-overview)

---

## Development Setup

### Prerequisites

- **Java 17** or higher
- **Gradle 8.5+** (wrapper included)
- **Git** for version control
- **IDE** (IntelliJ IDEA recommended)

### Fork and Clone

```bash
# Fork repository on GitHub, then:
git clone https://github.com/YOUR_USERNAME/dynamicquerybuilder.git
cd dynamicquerybuilder

# Add upstream remote
git remote add upstream https://github.com/query4j/dynamicquerybuilder.git
```

### Build Project

```bash
# Build all modules
./gradlew build

# Run tests
./gradlew test

# Generate JavaDoc
./gradlew javadoc

# Run benchmarks
./gradlew benchmark:benchmark
```

### IDE Setup

**IntelliJ IDEA:**

1. Import as Gradle project
2. Enable annotation processing (for Lombok)
3. Install Lombok plugin
4. Set Java 17 as project SDK

**Eclipse:**

1. Import as existing Gradle project
2. Install Lombok from [projectlombok.org](https://projectlombok.org)
3. Configure Java 17 compiler

---

## Contribution Workflow

### 1. Create an Issue

For significant changes, create an issue first to discuss:

```markdown
### Problem
Describe the issue or feature request

### Proposed Solution
Outline your approach

### Alternatives Considered
List other options you evaluated

### Additional Context
Any relevant information
```

### 2. Create a Branch

```bash
# Update main branch
git checkout main
git pull upstream main

# Create feature branch
git checkout -b feature/your-feature-name

# Or for bug fixes
git checkout -b fix/issue-123
```

**Branch Naming:**
- `feature/` - New features
- `fix/` - Bug fixes
- `docs/` - Documentation only
- `refactor/` - Code refactoring
- `test/` - Test improvements

### 3. Make Changes

Follow [Coding Standards](#coding-standards) and write tests.

### 4. Commit Changes

```bash
git add .
git commit -m "feat: add index suggestion caching

- Implement LRU cache for optimization results
- Add cache configuration options
- Include unit tests for cache behavior

Resolves #123"
```

**Commit Message Format:**
```
<type>: <subject>

<body>

<footer>
```

**Types:**
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation
- `test`: Test changes
- `refactor`: Code refactoring
- `perf`: Performance improvement
- `chore`: Maintenance tasks

### 5. Push and Create PR

```bash
git push origin feature/your-feature-name
```

Then create a Pull Request on GitHub.

---

## Coding Standards

### Java Code Style

Follow Query4j conventions:

**1. Immutability:**
```java
// ✅ Good - immutable with builder
@Value
@Builder
public class QueryConfig {
    private final int maxDepth;
    private final boolean strictValidation;
}

// ❌ Bad - mutable
public class QueryConfig {
    private int maxDepth;
    public void setMaxDepth(int depth) { ... }
}
```

**2. Copy-on-Write:**
```java
// ✅ Good - returns new instance
public QueryBuilder<T> where(String field, Object value) {
    return this.toBuilder()
        .predicates(addPredicate(field, value))
        .build();
}

// ❌ Bad - mutates state
public void where(String field, Object value) {
    this.predicates.add(new Predicate(field, value));
}
```

**3. Validation:**
```java
// ✅ Good - validate inputs
public QueryBuilder<T> where(String field, Object value) {
    if (field == null || field.trim().isEmpty()) {
        throw new QueryBuildException("Field name cannot be null or empty");
    }
    // ...
}
```

**4. Exception Handling:**
```java
// ✅ Good - use custom exceptions
throw new QueryBuildException(
    "Invalid field name: " + field,
    cause
);

// ❌ Bad - generic exceptions
throw new RuntimeException("Error");
```

### Code Formatting

- **Indentation:** 4 spaces
- **Line Length:** 120 characters
- **Braces:** K&R style
- **Imports:** Organize and remove unused

```bash
# Format code (if formatter configured)
./gradlew spotlessApply
```

---

## Testing Guidelines

### Test Coverage Target

- **Minimum:** 90% line coverage
- **Target:** 95%+ line coverage
- **Critical paths:** 100% coverage

### Test Types

**1. Unit Tests (JUnit 5):**
```java
@Test
void shouldBuildSimpleWhereClause() {
    QueryBuilder<User> query = QueryBuilder.forEntity(User.class)
        .where("username", "john");
    
    assertEquals("SELECT * FROM User WHERE username = :p1", query.toSQL());
    assertEquals("john", query.getParameters().get("p1"));
}
```

**2. Property-Based Tests (jqwik):**
```java
@Property
void shouldHandleArbitraryFieldNames(@ForAll("validFieldNames") String field) {
    QueryBuilder<User> query = QueryBuilder.forEntity(User.class)
        .where(field, "value");
    
    assertNotNull(query.toSQL());
}
```

**3. Integration Tests:**
```java
@Test
void shouldExecuteComplexQuery() {
    // Test with actual database
}
```

### Running Tests

```bash
# All tests
./gradlew test

# Specific module
./gradlew core:test

# Single test class
./gradlew test --tests QueryBuilderTest

# With coverage
./gradlew test jacocoTestReport
```

### Writing Good Tests

```java
// ✅ Good - descriptive name, clear assertion
@Test
void shouldThrowExceptionWhenFieldNameIsEmpty() {
    QueryBuildException exception = assertThrows(
        QueryBuildException.class,
        () -> QueryBuilder.forEntity(User.class).where("", "value")
    );
    
    assertTrue(exception.getMessage().contains("Field name cannot be null or empty"));
}

// ❌ Bad - unclear test
@Test
void test1() {
    try {
        QueryBuilder.forEntity(User.class).where("", "value");
        fail();
    } catch (Exception e) {
        // Test passes
    }
}
```

---

## Documentation

### JavaDoc Requirements

All public APIs must have comprehensive JavaDoc:

```java
/**
 * Creates a WHERE clause with an equality predicate.
 * 
 * <p>This method adds a simple equality condition to the query.
 * Field names must match the pattern [A-Za-z0-9_\.]+</p>
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * QueryBuilder<User> query = QueryBuilder.forEntity(User.class)
 *     .where("username", "john")
 *     .where("active", true);
 * }</pre>
 *
 * @param field the field name (cannot be null or empty)
 * @param value the value to compare (null values are supported)
 * @return a new QueryBuilder instance with the predicate added
 * @throws QueryBuildException if field name is invalid
 * @since 1.0.0
 */
public QueryBuilder<T> where(String field, Object value) {
    // Implementation
}
```

### README Updates

Update module READMEs when:
- Adding new features
- Changing public APIs
- Modifying configuration options
- Adding dependencies

### Wiki Updates

Update wiki pages when:
- Adding major features
- Changing architecture
- Updating best practices
- Adding examples

See [Wiki Setup](https://github.com/query4j/dynamicquerybuilder/tree/master/wiki) for instructions.

---

## Pull Request Process

### PR Checklist

Before submitting, ensure:

- [ ] Code builds successfully (`./gradlew build`)
- [ ] All tests pass (`./gradlew test`)
- [ ] New code has tests (≥90% coverage)
- [ ] JavaDoc added for public APIs
- [ ] README updated if needed
- [ ] No compiler warnings
- [ ] Follows coding standards
- [ ] Commit messages are descriptive

### PR Template

```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Documentation update
- [ ] Performance improvement
- [ ] Code refactoring

## Related Issues
Closes #123

## Testing
- [ ] Unit tests added/updated
- [ ] Integration tests added/updated
- [ ] Manual testing performed

## Checklist
- [ ] Code builds cleanly
- [ ] Tests pass
- [ ] Documentation updated
- [ ] Follows coding standards
```

### Review Process

1. **Automated Checks:** CI/CD runs tests and checks
2. **Code Review:** Maintainers review code
3. **Feedback:** Address review comments
4. **Approval:** At least one maintainer approval required
5. **Merge:** Maintainer merges after approval

### After Merge

- PR branch is automatically deleted
- Changes appear in next release
- Contributors credited in release notes

---

## Architecture Overview

### Module Structure

```
dynamicquerybuilder/
├── core/           # Query builder fundamentals
├── cache/          # Result caching
├── optimizer/      # Query optimization
├── examples/       # Sample applications
├── benchmark/      # Performance tests
└── docs/           # Documentation
```

### Key Design Principles

1. **Immutability** - All operations return new instances
2. **Thread Safety** - Concurrent access without locks
3. **Composability** - Small, focused components
4. **Type Safety** - Compile-time validation where possible
5. **Performance** - Sub-millisecond query construction

---

## Getting Help

### Questions?

- **GitHub Discussions:** [Ask questions](https://github.com/query4j/dynamicquerybuilder/discussions)
- **Issue Tracker:** [Report bugs](https://github.com/query4j/dynamicquerybuilder/issues)
- **Email:** maintainers@query4j.org (for sensitive issues)

### Resources

- [API Guide](https://github.com/query4j/dynamicquerybuilder/blob/master/docs/API_GUIDE.md)
- [Architecture Decisions](https://github.com/query4j/dynamicquerybuilder/tree/master/docs/architecture)
- [Examples](https://github.com/query4j/dynamicquerybuilder/tree/master/examples)

---

## License

By contributing, you agree that your contributions will be licensed under the [Apache License 2.0](https://github.com/query4j/dynamicquerybuilder/blob/master/LICENSE).

---

**Thank you for contributing to Query4j!** 🎉

Your contributions help make Query4j better for everyone. We appreciate your time and effort!

---

**Last Updated:** December 2024  
**Version:** 1.0.0
