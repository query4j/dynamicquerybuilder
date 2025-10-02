# JavaDoc Generation Guide

This guide provides comprehensive instructions for generating, validating, and publishing API documentation for Query4j Dynamic Query Builder.

## Overview

Query4j uses Gradle with Lombok integration to generate comprehensive JavaDoc documentation for all public APIs. The build configuration automatically handles delombok processing, ensuring that Lombok-generated methods are properly documented.

## Prerequisites

- **Java 17** or higher
- **Gradle 8.5+** (included via wrapper)
- **Git** for version control
- **Internet connection** for initial dependency downloads

## Quick Start

### Generate JavaDoc for All Modules

```bash
# Generate JavaDoc for all modules
./gradlew javadoc

# View generated documentation
# - Core: core/build/docs/javadoc/index.html
# - Cache: cache/build/docs/javadoc/index.html
# - Optimizer: optimizer/build/docs/javadoc/index.html
# - Examples: examples/build/docs/javadoc/index.html
```

### Generate JavaDoc for Specific Module

```bash
# Core module only
./gradlew core:javadoc

# Cache module only
./gradlew cache:javadoc

# Optimizer module only
./gradlew optimizer:javadoc
```

### Create JavaDoc Distribution JARs

```bash
# Build JavaDoc JARs for all modules
./gradlew javadocJar

# Output locations:
# - core/build/libs/dynamicquerybuilder-core-*-javadoc.jar
# - cache/build/libs/dynamicquerybuilder-cache-*-javadoc.jar
# - optimizer/build/libs/dynamicquerybuilder-optimizer-*-javadoc.jar
```

## Build Configuration

### Gradle JavaDoc Configuration

The project's `build.gradle` includes the following JavaDoc configuration applied to all subprojects:

```gradle
tasks.withType(Javadoc).tap {
    configureEach {
        dependsOn delombok
        source = fileTree("$buildDir/delombok")
        options.encoding = 'UTF-8'
        options.addStringOption('Xdoclint:all,-missing', '-quiet')
        failOnError = false
        doFirst {
            options.classpath = (configurations.compileClasspath + sourceSets.main.output.classesDirs).collect { it }
        }
    }
}
```

### Delombok Processing

Lombok annotations are processed before JavaDoc generation:

```gradle
tasks.register('delombok', JavaExec) {
    outputs.dir "$buildDir/delombok"
    inputs.files sourceSets.main.java.srcDirs

    classpath = configurations.compileClasspath + configurations.annotationProcessor
    mainClass = 'lombok.launch.Main'
    args 'delombok'
    args '-d'
    args "$buildDir/delombok"
    args sourceSets.main.java.srcDirs
}
```

## Documentation Standards

### Required JavaDoc Elements

All public APIs must include:

1. **Class/Interface Description**
   - Clear purpose and usage context
   - Thread safety guarantees
   - Performance characteristics where relevant
   - Example usage in `<pre>{@code ...}</pre>` blocks

2. **Method Documentation**
   - `@param` for each parameter with validation rules
   - `@return` with null safety guarantees
   - `@throws` for all checked and important runtime exceptions
   - `@since` version when the API was introduced
   - Usage examples for complex methods

3. **Field Documentation**
   - Purpose and constraints for public fields
   - Immutability guarantees
   - Valid value ranges

### Example Well-Documented Class

```java
/**
 * Interface representing a paginated result set with metadata.
 * 
 * <p>
 * Provides access to paginated query results along with pagination metadata
 * such as page number, total elements, and navigation helpers. This interface
 * follows immutability principles - all instances are read-only snapshots.
 * </p>
 * 
 * <p>
 * Example usage:
 * </p>
 * 
 * <pre>{@code
 * Page<User> userPage = QueryBuilder.forEntity(User.class)
 *     .where("active", true)
 *     .page(1, 20)
 *     .findPage();
 * 
 * List<User> users = userPage.getContent();
 * System.out.println("Page " + userPage.getNumber() + 
 *                    " of " + userPage.getTotalPages());
 * }</pre>
 *
 * @param <T> the entity type
 * @author query4j team
 * @version 1.0.0
 * @since 1.0.0
 */
public interface Page<T> {
    
    /**
     * Returns the content of the current page as a list.
     * 
     * <p>
     * The returned list is immutable and contains entities for this page only.
     * The list may be empty if the page contains no results, but never null.
     * </p>
     * 
     * @return the page content, never null but may be empty
     */
    List<T> getContent();
    
    // ... more methods ...
}
```

## Validation and Quality Checks

### Validate JavaDoc Compilation

```bash
# Generate JavaDoc with strict error checking
./gradlew javadoc --warning-mode all

# Check for specific warnings
./gradlew javadoc 2>&1 | grep -i "warning\|error"
```

### Common JavaDoc Issues

#### Issue: Missing Lombok-generated methods

**Symptom:** Methods from `@Data`, `@Value`, `@Builder` not appearing in JavaDoc

**Solution:** Ensure delombok task runs before JavaDoc:

```bash
# Clean and regenerate
./gradlew clean
./gradlew delombok
./gradlew javadoc
```

#### Issue: Broken @link references

**Symptom:** Warnings about unresolved links

**Solution:** Use fully qualified class names in `@link` tags:

```java
// ❌ May break if not imported
* @see QueryBuilder

// ✅ Always works
* @see com.github.query4j.core.QueryBuilder
```

#### Issue: Invalid HTML in JavaDoc

**Symptom:** Warnings about malformed HTML

**Solution:** Use HTML entities and proper tags:

```java
// ❌ Invalid
* Returns value > 0

// ✅ Valid
* Returns value &gt; 0

// ❌ Invalid
* <p>First paragraph
* <p>Second paragraph

// ✅ Valid
* <p>First paragraph</p>
* <p>Second paragraph</p>
```

## Publishing Documentation

### Local Testing

```bash
# Generate and open in browser (macOS)
./gradlew javadoc && open core/build/docs/javadoc/index.html

# Generate and open in browser (Linux)
./gradlew javadoc && xdg-open core/build/docs/javadoc/index.html

# Generate and open in browser (Windows)
./gradlew javadoc && start core/build/docs/javadoc/index.html
```

### Publishing to GitHub Pages

```bash
# 1. Generate JavaDoc for all modules
./gradlew clean javadoc

# 2. Collect JavaDoc into single directory
mkdir -p docs-site
cp -r core/build/docs/javadoc docs-site/core
cp -r cache/build/docs/javadoc docs-site/cache
cp -r optimizer/build/docs/javadoc docs-site/optimizer

# 3. Create index page (docs-site/index.html)
cat > docs-site/index.html << 'EOF'
<!DOCTYPE html>
<html>
<head>
    <title>Query4j API Documentation</title>
</head>
<body>
    <h1>Query4j Dynamic Query Builder - API Documentation</h1>
    <ul>
        <li><a href="core/index.html">Core Module</a></li>
        <li><a href="cache/index.html">Cache Module</a></li>
        <li><a href="optimizer/index.html">Optimizer Module</a></li>
    </ul>
</body>
</html>
EOF

# 4. Deploy to GitHub Pages
# (Assuming gh-pages branch is configured)
git checkout gh-pages
cp -r docs-site/* .
git add .
git commit -m "Update API documentation"
git push origin gh-pages
git checkout main
```

### Automated Publishing with CI/CD

Add to your GitHub Actions workflow (`.github/workflows/publish-docs.yml`):

```yaml
name: Publish Documentation

on:
  release:
    types: [published]

jobs:
  publish-docs:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      
      - name: Generate JavaDoc
        run: ./gradlew javadoc
      
      - name: Deploy to GitHub Pages
        uses: peaceiris/actions-gh-pages@v3
        with:
          github_token: ${{ secrets.GITHUB_TOKEN }}
          publish_dir: ./build/docs/javadoc
```

## Maintenance

### Updating Documentation

When adding or modifying public APIs:

1. **Update JavaDoc** with comprehensive documentation
2. **Add usage examples** for complex methods
3. **Document parameters and return values** thoroughly
4. **Include @since tag** with the version introducing the API
5. **Generate and review** JavaDoc locally
6. **Validate links and formatting** before committing

### Documentation Review Checklist

- [ ] All public classes have class-level JavaDoc
- [ ] All public methods have method-level JavaDoc
- [ ] All parameters have `@param` tags
- [ ] All return values have `@return` tags
- [ ] All exceptions have `@throws` tags
- [ ] Usage examples are included for complex APIs
- [ ] All `@link` references are valid
- [ ] HTML is properly formed
- [ ] JavaDoc builds without warnings
- [ ] Code examples compile and run correctly

### Continuous Integration

The project's CI pipeline automatically:

1. Validates JavaDoc builds successfully
2. Checks for broken links
3. Ensures consistent formatting
4. Publishes documentation on releases

## Troubleshooting

### Problem: "OutOfMemoryError during JavaDoc generation"

**Solution:** Increase Gradle memory:

```bash
# Set in gradle.properties
org.gradle.jvmargs=-Xmx2g -XX:MaxMetaspaceSize=512m

# Or use environment variable
export GRADLE_OPTS="-Xmx2g"
./gradlew javadoc
```

### Problem: "Delombok task not found"

**Solution:** Ensure Lombok is in classpath:

```gradle
dependencies {
    compileOnly 'org.projectlombok:lombok:1.18.28'
    annotationProcessor 'org.projectlombok:lombok:1.18.28'
}
```

### Problem: "JavaDoc links broken after refactoring"

**Solution:** Use automated link checking:

```bash
# Install lychee (link checker)
cargo install lychee

# Check links in generated JavaDoc
lychee 'core/build/docs/javadoc/**/*.html'
```

## Resources

- [JavaDoc Tool Documentation](https://docs.oracle.com/javase/8/docs/technotes/tools/windows/javadoc.html)
- [Lombok JavaDoc Support](https://projectlombok.org/features/delombok)
- [Gradle JavaDoc Plugin](https://docs.gradle.org/current/dsl/org.gradle.api.tasks.javadoc.Javadoc.html)
- [Query4j API Documentation Guide](API_DOCUMENTATION_GUIDE.md)

## Support

For issues with documentation generation:

1. Check this guide for common solutions
2. Review [API Documentation Guide](API_DOCUMENTATION_GUIDE.md)
3. Search [GitHub Issues](https://github.com/query4j/dynamicquerybuilder/issues)
4. Ask in [GitHub Discussions](https://github.com/query4j/dynamicquerybuilder/discussions)
