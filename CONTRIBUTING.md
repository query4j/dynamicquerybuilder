# Contributing to dynamicquerybuilder

Welcome! Thank you for your interest in contributing to the **dynamicquerybuilder** project. This document outlines the coding standards, contribution process, and quality expectations to maintain a world-class, high-performance Java library.

## Table of Contents

- [Code Style & Standards](#code-style--standards)
- [Branching & Commit Guidelines](#branching--commit-guidelines)
- [Issue Reporting & Feature Requests](#issue-reporting--feature-requests)
- [Pull Request Process](#pull-request-process)
- [Testing Requirements](#testing-requirements)
- [Performance & Quality Expectations](#performance--quality-expectations)
- [Security Considerations](#security-considerations)
- [Community Code of Conduct](#community-code-of-conduct)

---

## Code Style & Standards

- Follow [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html) and [Oracle Code Conventions](https://www.oracle.com/java/technologies/javase/codeconventions-programmingpractices.html).
- Use **UTF-8** encoding with 4 spaces indentation, no tabs.
- Class and interface names should use PascalCase; methods and variables should use camelCase.
- Use descriptive, meaningful, and pronounceable names — avoid abbreviations or ambiguous names.
- Keep methods focused and concise (generally 10–20 lines, max 50 lines).
- Use Javadoc extensively for all public API elements, including examples, parameters, return values, exceptions.
- Organize imports efficiently, grouping standard, third-party, and project imports separately.
- Prefer immutability and thread-safe constructs as required.
- Follow SOLID principles and avoid code duplication.
- Write clean, readable, and maintainable code; prioritize **readability over cleverness**.

---

## Branching & Commit Guidelines

- Use **feature branches** named `feat/your-feature-name`, `fix/bug-description` etc.
- Keep commits small and atomic, encapsulating a single logical change.
- Write clear commit messages using the [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/) format:
- Types: `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`.
- Example: `feat(core): add immutable query builder implementation`

---

## Issue Reporting & Feature Requests

- Search existing issues before reporting duplicates.
- Provide a clear and concise description with minimal reproduction steps.
- Include environment details (JDK version, OS, Maven version, etc.).
- For feature requests, explain the use case and expected benefits.

---

## Pull Request Process

- Fork the repository and work on a feature branch.
- Ensure all tests pass locally and code meets the coding standards.
- Reference related issue numbers in pull requests.
- Provide meaningful PR descriptions explaining rationale and impact.
- Collaborate and respond to review feedback promptly.

---

## Testing Requirements

- All new features and bug fixes must include comprehensive unit and integration tests.
- Target **100% test coverage** on all changed/new code paths.
- Use `JUnit 5` and `Mockito` for testing.
- Follow Test-Driven Development (TDD) principles where possible.
- Write performance benchmarks for critical sections.
- Tests should be deterministic and not depend on external state.

---

## Performance & Quality Expectations

- Prioritize performance optimizations to outperform existing libraries.
- Avoid premature optimization—profile first and optimize hotspot code carefully.
- Document any trade-offs or considerations made for performance gains.
- Maintain sound memory usage and avoid unnecessary object allocation.
- Use immutable collections and thread-safe practices consistently.
- Run static analysis tools and linters regularly (e.g., SpotBugs, Checkstyle).

---

## Security Considerations

- Avoid introducing security vulnerabilities (e.g., SQL Injection, unsafe reflection).
- Validate inputs rigorously.
- Use secure coding best practices and Java SE security guidelines.
- Report security issues privately to maintainers before public disclosure.

---

## Community Code of Conduct

- Be respectful and constructive in communication.
- Encourage diverse participation.
- Avoid toxic or abusive behavior.
- Follow the project's [CODE_OF_CONDUCT.md](link_to_code_of_conduct) document.

---

Thank you for contributing to making **dynamicquerybuilder** a powerful, reliable, and community-driven tool. Together, we can build a library that empowers developers worldwide.