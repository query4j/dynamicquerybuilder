# API Documentation Maintenance Guide

## Overview

This guide provides guidelines for contributors on how to maintain and update the Query4j API documentation as the library evolves. Following these standards ensures consistency, completeness, and usability of our API documentation.

## Documentation Standards

### 1. JavaDoc Requirements

All public APIs must include comprehensive JavaDoc with:

#### Required Elements
- **Class/Interface Description**: Clear purpose and usage context
- **Method Description**: What the method does and when to use it  
- **Parameters**: `@param` for each parameter with validation rules
- **Return Values**: `@return` with null safety guarantees
- **Exceptions**: `@throws` for all checked and important runtime exceptions
- **Since Tags**: `@since` version when the API was introduced
- **Examples**: `@code` blocks for common usage patterns

#### JavaDoc Template
```java
/**
 * Brief description of what this method does.
 * 
 * <p>
 * Detailed description including usage context, behavior notes,
 * and any important implementation details that affect users.
 * </p>
 * 
 * <p>
 * Example usage:
 * </p>
 * 
 * <pre>{@code
 * QueryBuilder<User> query = QueryBuilder.forEntity(User.class)
 *     .where("active", true)
 *     .findAll();
 * }</pre>
 * 
 * @param paramName parameter description with validation requirements
 * @return description of return value and null safety guarantees  
 * @throws ExceptionType when and why this exception is thrown
 * @since 1.0.0
 */
```

### 2. API Guide Documentation

The [API_GUIDE.md](API_GUIDE.md) must be updated when:

#### Adding New APIs
1. **Update Entry Points Table**: Add new entry point with module and example
2. **Add Detailed Section**: Include full method signatures and examples  
3. **Update Usage Examples**: Add practical examples demonstrating the new API
4. **Cross-Reference**: Link related APIs and concepts

#### Modifying Existing APIs
1. **Update Method Signatures**: Ensure accuracy with actual implementation
2. **Update Examples**: Verify all code examples still compile and work
3. **Add Migration Notes**: Document breaking changes and migration path
4. **Update Performance Notes**: Include any performance implications

#### Removing APIs (Deprecation Process)
1. **Mark as Deprecated**: Add `@deprecated` in JavaDoc with replacement guidance
2. **Update Examples**: Replace deprecated usage with recommended alternatives
3. **Add Migration Section**: Provide clear upgrade path
4. **Timeline**: Include deprecation and removal timeline

### 3. Code Example Standards

All code examples in documentation must:

#### Compilation Requirements
- **Compile Successfully**: Examples must build without errors
- **Use Real APIs**: Only demonstrate actually implemented functionality
- **Import Statements**: Include necessary imports when showing complete examples
- **Type Safety**: Demonstrate proper generic usage

#### Content Requirements
- **Realistic Scenarios**: Use domain models that reflect real-world usage
- **Best Practices**: Show optimal patterns and performance considerations
- **Error Handling**: Include exception handling where appropriate
- **Complete Context**: Provide sufficient context for understanding

#### Example Structure
```java
// Always include imports for complete examples
import com.github.query4j.core.QueryBuilder;
import com.github.query4j.examples.model.User;

// Use realistic entity models
List<User> activeUsers = QueryBuilder.forEntity(User.class)
    .where("active", true)
    .and()
    .where("department", "Engineering") 
    .orderBy("lastName")
    .findAll();
```

## Documentation Update Process

### 1. API Development Workflow

When adding or modifying public APIs:

```
Code Change → JavaDoc Update → API Guide Update → Example Update → Review
```

#### Step-by-Step Process
1. **Implement API**: Write the new/modified API code
2. **Add JavaDoc**: Complete JavaDoc following our template
3. **Update API Guide**: Modify API_GUIDE.md with new information
4. **Create Examples**: Add working examples to examples module
5. **Test Examples**: Ensure all examples compile and run
6. **Peer Review**: Submit PR for documentation review

### 2. Review Checklist

Before merging API documentation changes:

#### JavaDoc Review
- [ ] All public methods have complete JavaDoc
- [ ] Parameter validation rules are documented
- [ ] Return value null safety is specified  
- [ ] All exceptions are documented with context
- [ ] Code examples compile and demonstrate proper usage
- [ ] Since tags are accurate

#### API Guide Review  
- [ ] Entry points table is updated
- [ ] New APIs have detailed sections with examples
- [ ] All code examples are tested and working
- [ ] Cross-references to related APIs are included
- [ ] Performance implications are documented

#### Example Review
- [ ] Examples compile without errors
- [ ] Examples demonstrate realistic usage patterns
- [ ] Model classes are appropriate and complete
- [ ] Examples show best practices

### 3. Validation Process

#### Automated Validation
The build process includes automated checks:

```bash
# Validate examples compile
./gradlew examples:compileJava

# Generate JavaDoc (fails on errors)  
./gradlew javadoc

# Run documentation tests
./gradlew test --tests '*DocumentationTest'
```

#### Manual Validation
For each API documentation update:

1. **Compile Examples**: Ensure all code examples build successfully
2. **Test Functionality**: Run examples to verify they work as documented  
3. **Review Cross-References**: Verify all links and references are accurate
4. **Check Consistency**: Ensure consistent terminology and formatting

## Versioning and Compatibility

### Version Documentation

#### Major Version Changes (2.0.0)
- **Breaking Changes**: Document all breaking changes with migration guide
- **New Features**: Highlight major new capabilities
- **Performance Changes**: Document significant performance improvements
- **API Reorganization**: Explain any package or class restructuring

#### Minor Version Changes (1.1.0)
- **New APIs**: Document new functionality and usage patterns
- **Enhancements**: Explain improvements to existing APIs
- **Deprecations**: Mark deprecated APIs and provide alternatives
- **Performance**: Note performance improvements

#### Patch Version Changes (1.0.1)  
- **Bug Fixes**: Document any API behavior fixes
- **Documentation**: Corrections to examples or JavaDoc
- **Clarifications**: Improved documentation clarity

### Backward Compatibility

#### Compatibility Promise
- **Public APIs**: Maintain backward compatibility within major versions
- **Documentation**: Keep examples working across minor versions
- **Migration Paths**: Provide clear upgrade guidance for breaking changes
- **Timeline**: Give users adequate time to migrate from deprecated APIs

## Tools and Automation

### Documentation Generation

#### JavaDoc Configuration
```gradle
// In build.gradle (applied to all subprojects)
tasks.withType(Javadoc) {
    options.encoding = 'UTF-8'
    options.addStringOption('Xdoclint:all,-missing', '-quiet')
    failOnError = false
}

#### Example Validation
Create tests that validate documentation examples:

```java
@Test
void allDocumentationExamplesCompile() {
    // Automated test to ensure examples compile
    // Validates API_GUIDE.md code blocks
}

@Test  
void quickStartExamplesWork() {
    // Integration test for README examples
    // Ensures basic usage patterns function correctly
}
```

### Continuous Integration

#### Documentation CI Checks
- **JavaDoc Generation**: Fail build if JavaDoc has errors
- **Example Compilation**: Ensure all examples build successfully
- **Link Validation**: Check that all cross-references are valid
- **Markdown Linting**: Validate markdown formatting

## Best Practices

### Writing Effective API Documentation

#### 1. User-Focused Content
- **Use Cases**: Lead with why someone would use this API
- **Context**: Explain when and how to use each method
- **Gotchas**: Warn about common mistakes or edge cases
- **Performance**: Note performance characteristics when relevant

#### 2. Progressive Disclosure
- **Overview First**: Start with high-level concepts  
- **Details Second**: Provide comprehensive reference information
- **Examples Throughout**: Show concrete usage at each level
- **Advanced Topics**: Cover optimization and advanced patterns

#### 3. Consistency
- **Terminology**: Use consistent terms throughout documentation
- **Formatting**: Follow established patterns for code examples
- **Structure**: Organize information predictably
- **Style**: Maintain consistent tone and voice

### Common Documentation Anti-Patterns

#### Avoid These Mistakes
- **Implementation Details**: Don't document internal implementation
- **Obvious Comments**: Avoid redundant or obvious descriptions  
- **Stale Examples**: Keep examples current with latest API
- **Missing Context**: Provide sufficient context for understanding
- **Broken Examples**: Test all code examples before publishing

## Contributing Guidelines

### Getting Started
1. **Read Existing Docs**: Understand current patterns and style
2. **Set Up Environment**: Ensure you can build examples and JavaDoc
3. **Start Small**: Begin with minor improvements before major additions
4. **Ask Questions**: Use GitHub Discussions for documentation questions

### Submission Process
1. **Fork Repository**: Create your own fork for changes
2. **Create Branch**: Use descriptive branch names like `docs/api-guide-caching`
3. **Make Changes**: Follow the standards in this guide
4. **Test Locally**: Validate examples compile and work
5. **Submit PR**: Include description of documentation changes
6. **Address Feedback**: Respond to review comments promptly

### Review Participation
- **Review Others**: Help review documentation PRs from other contributors
- **Provide Feedback**: Offer constructive suggestions for improvements
- **Test Examples**: Try out documented examples to verify they work
- **Ask Questions**: If documentation is unclear, ask for clarification

---

For questions about API documentation standards, please:
- **Create Issue**: [GitHub Issues](https://github.com/query4j/dynamicquerybuilder/issues) for specific problems
- **Start Discussion**: [GitHub Discussions](https://github.com/query4j/dynamicquerybuilder/discussions) for general questions  
- **Review Guide**: Refer to this guide and [CONTRIBUTING.md](../CONTRIBUTING.md) for standards