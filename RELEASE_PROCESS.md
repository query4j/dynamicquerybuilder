# Release Process Guide

This document describes the complete process for creating and publishing releases of Query4j Dynamic Query Builder.

---

## Table of Contents

1. [Release Types](#release-types)
2. [Pre-Release Preparation](#pre-release-preparation)
3. [Creating a Release](#creating-a-release)
4. [Publishing to Maven Central](#publishing-to-maven-central)
5. [Post-Release Tasks](#post-release-tasks)
6. [Hotfix Releases](#hotfix-releases)
7. [Troubleshooting](#troubleshooting)

---

## Release Types

### Major Release (X.0.0)
- Breaking changes or major architectural changes
- Requires migration guide
- Extensive testing and documentation updates
- Example: 1.0.0 → 2.0.0

### Minor Release (1.X.0)
- New features without breaking changes
- May include deprecations
- Enhanced documentation
- Example: 1.0.0 → 1.1.0

### Patch Release (1.0.X)
- Bug fixes only
- No new features
- Minimal documentation changes
- Example: 1.0.0 → 1.0.1

---

## Pre-Release Preparation

### 1. Version Planning

Determine the version number based on changes:
- **Breaking changes**: Major version increment
- **New features**: Minor version increment
- **Bug fixes**: Patch version increment

### 2. Update Version

Update version in `build.gradle`:

```groovy
// Change from
version = '1.0.0-SNAPSHOT'

// To stable version
version = '1.0.0'
```

### 3. Update Documentation

Update version references in:
- `README.md`
- All module `README.md` files
- `docs/API_GUIDE.md`
- Documentation examples

### 4. Prepare Release Notes

#### Update CHANGELOG.md

Add entry for new version:

```markdown
## [1.0.0] - 2024-10-01

### Added
- New feature descriptions

### Changed
- Modifications to existing features

### Fixed
- Bug fixes

### Breaking Changes
- Any breaking changes
```

#### Create Release Notes

Create `RELEASE_NOTES_v[VERSION].md` with:
- Executive summary
- Key highlights
- Detailed feature descriptions
- Performance benchmarks
- Migration instructions
- Known issues

### 5. Quality Assurance

Run comprehensive tests:

```bash
# Full test suite
./gradlew clean test

# Coverage report
./gradlew jacocoRootReport

# Build all modules
./gradlew clean build

# Generate JavaDoc
./gradlew javadoc

# Run benchmarks (optional but recommended)
./gradlew :benchmark:jmh
```

Verify:
- [ ] All tests pass (100% success rate)
- [ ] Code coverage ≥ 95%
- [ ] JavaDoc generates without warnings
- [ ] Build completes successfully
- [ ] Benchmarks show acceptable performance

---

## Creating a Release

### Automated Release (Recommended)

#### Step 1: Commit Changes

```bash
# Stage all changes
git add .

# Commit with descriptive message
git commit -m "Prepare release v1.0.0"

# Push to repository
git push origin master
```

#### Step 2: Create and Push Tag

```bash
# Create annotated tag
git tag -a v1.0.0 -m "Release version 1.0.0"

# Push tag to trigger release workflow
git push origin v1.0.0
```

#### Step 3: Monitor Release Workflow

1. Go to GitHub Actions: `https://github.com/query4j/dynamicquerybuilder/actions`
2. Check "Release" workflow execution
3. Verify all jobs complete successfully:
   - Build and test
   - Publish documentation
   - Create GitHub release

#### Step 4: Verify Release

Check that release artifacts are available:
1. GitHub Releases: `https://github.com/query4j/dynamicquerybuilder/releases`
2. Verify all JARs are uploaded:
   - Core module JAR
   - Cache module JAR
   - Optimizer module JAR
   - Source JARs
   - JavaDoc JARs
3. Verify checksums (SHA256, MD5) are present

### Manual Release (Fallback)

If automated release fails, perform manual release:

#### Step 1: Build Artifacts

```bash
# Clean build
./gradlew clean

# Build all artifacts
./gradlew assemble javadocJar sourcesJar

# Verify JARs are created
find . -name "*.jar" -not -name "*-plain.jar"
```

#### Step 2: Generate Checksums

```bash
# For each JAR file, generate checksums
for file in $(find . -name "*.jar" -not -name "*-plain.jar"); do
    sha256sum "$file" > "${file}.sha256"
    md5sum "$file" > "${file}.md5"
done
```

#### Step 3: Create GitHub Release

1. Go to: `https://github.com/query4j/dynamicquerybuilder/releases/new`
2. Tag version: `v1.0.0`
3. Release title: `Query4j Dynamic Query Builder v1.0.0`
4. Description: Copy from `RELEASE_NOTES_v1.0.0.md`
5. Upload artifacts:
   - All JAR files from `build/libs/`
   - All checksum files (`.sha256`, `.md5`)
6. Click "Publish release"

#### Step 4: Publish JavaDoc

```bash
# Generate JavaDoc
./gradlew javadoc

# Create docs site
mkdir -p docs-site
cp -r core/build/docs/javadoc docs-site/core
cp -r cache/build/docs/javadoc docs-site/cache
cp -r optimizer/build/docs/javadoc docs-site/optimizer

# Create index
cat > docs-site/index.html << 'EOF'
<!DOCTYPE html>
<html>
<head><title>Query4j API Documentation</title></head>
<body>
    <h1>Query4j API Documentation</h1>
    <ul>
        <li><a href="core/index.html">Core Module</a></li>
        <li><a href="cache/index.html">Cache Module</a></li>
        <li><a href="optimizer/index.html">Optimizer Module</a></li>
    </ul>
</body>
</html>
EOF

# Deploy to GitHub Pages (requires gh-pages branch)
git checkout gh-pages
cp -r docs-site/* api-docs/
git add api-docs/
git commit -m "Update API documentation for v1.0.0"
git push origin gh-pages
git checkout master
```

---

## Publishing to Maven Central

### Prerequisites

1. **Sonatype Account**: Register at https://issues.sonatype.org/
2. **GPG Key**: Generate GPG key for signing artifacts
3. **Gradle Configuration**: Configure credentials

### Step 1: Configure Gradle for Publishing

Add to `build.gradle`:

```groovy
plugins {
    id 'maven-publish'
    id 'signing'
}

publishing {
    publications {
        maven(MavenPublication) {
            from components.java
            
            artifact sourcesJar
            artifact javadocJar
            
            pom {
                name = 'Query4j Dynamic Query Builder'
                description = 'High-performance, thread-safe Java library for building dynamic SQL queries'
                url = 'https://github.com/query4j/dynamicquerybuilder'
                
                licenses {
                    license {
                        name = 'The Apache License, Version 2.0'
                        url = 'http://www.apache.org/licenses/LICENSE-2.0.txt'
                    }
                }
                
                developers {
                    developer {
                        id = 'query4j'
                        name = 'Query4j Team'
                    }
                }
                
                scm {
                    connection = 'scm:git:git://github.com/query4j/dynamicquerybuilder.git'
                    developerConnection = 'scm:git:ssh://github.com/query4j/dynamicquerybuilder.git'
                    url = 'https://github.com/query4j/dynamicquerybuilder'
                }
            }
        }
    }
    
    repositories {
        maven {
            def releasesRepoUrl = "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
            def snapshotsRepoUrl = "https://oss.sonatype.org/content/repositories/snapshots/"
            url = version.endsWith('SNAPSHOT') ? snapshotsRepoUrl : releasesRepoUrl
            
            credentials {
                username = project.findProperty("ossrhUsername") ?: System.getenv("OSSRH_USERNAME")
                password = project.findProperty("ossrhPassword") ?: System.getenv("OSSRH_PASSWORD")
            }
        }
    }
}

signing {
    sign publishing.publications.maven
}
```

### Step 2: Configure Credentials

Add to `~/.gradle/gradle.properties`:

```properties
ossrhUsername=YOUR_SONATYPE_USERNAME
ossrhPassword=YOUR_SONATYPE_PASSWORD

signing.keyId=YOUR_GPG_KEY_ID
signing.password=YOUR_GPG_PASSWORD
signing.secretKeyRingFile=/path/to/.gnupg/secring.gpg
```

### Step 3: Publish to Maven Central

```bash
# Publish to staging repository
./gradlew publishMavenPublicationToMavenRepository

# Close staging repository (via Sonatype UI or CLI)
# Verify staging artifacts
# Release staging repository

# Artifacts will sync to Maven Central within 2-4 hours
```

### Step 4: Verify Publication

After 2-4 hours, verify on Maven Central:
- https://search.maven.org/artifact/com.github.query4j/dynamicquerybuilder-core

---

## Post-Release Tasks

### 1. Update Repository

#### Create Next Development Version

```bash
# Update version in build.gradle
# Example: 1.0.0 → 1.1.0-SNAPSHOT

# Commit and push
git add build.gradle
git commit -m "Prepare for next development iteration"
git push origin master
```

#### Update Badges

Update README.md badges to reflect new version:
```markdown
[![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)](...)
```

### 2. Communicate Release

#### GitHub

- [ ] Create announcement in GitHub Discussions
- [ ] Update project description with latest version
- [ ] Close milestone for this release
- [ ] Create milestone for next release

#### Social Media

Use templates from `ANNOUNCEMENT.md`:

**Twitter/X**:
```
🎉 Query4j v1.0.0 is now available! 

✨ Fluent API for SQL queries
⚡ Sub-microsecond performance
🔒 Thread-safe & type-safe

Get started: https://github.com/query4j/dynamicquerybuilder

#Java #Query4j #OpenSource
```

**LinkedIn**: (See ANNOUNCEMENT.md for full template)

**Reddit** (r/java): (See ANNOUNCEMENT.md for full template)

### 3. Monitor Feedback

- [ ] Monitor GitHub Issues for release-related bugs
- [ ] Respond to questions in GitHub Discussions
- [ ] Track download statistics
- [ ] Gather user feedback

### 4. Update Documentation

- [ ] Verify all documentation links work
- [ ] Update wiki with release information
- [ ] Add release to version history
- [ ] Update roadmap with completed features

---

## Hotfix Releases

For critical bugs that require immediate patch:

### Step 1: Create Hotfix Branch

```bash
# From the release tag
git checkout -b hotfix/v1.0.1 v1.0.0

# Or from master if already ahead
git checkout -b hotfix/v1.0.1
```

### Step 2: Fix the Issue

```bash
# Make minimal changes to fix the bug
# Add tests to verify fix
./gradlew test

# Commit fix
git commit -m "Fix critical bug XYZ"
```

### Step 3: Update Version

```bash
# Update version in build.gradle to 1.0.1
# Update CHANGELOG.md with hotfix notes

git add build.gradle CHANGELOG.md
git commit -m "Prepare hotfix release v1.0.1"
```

### Step 4: Release Hotfix

```bash
# Merge to master
git checkout master
git merge hotfix/v1.0.1

# Create and push tag
git tag -a v1.0.1 -m "Hotfix release v1.0.1"
git push origin master v1.0.1

# Delete hotfix branch
git branch -d hotfix/v1.0.1
```

### Step 5: Communicate Hotfix

- [ ] Update GitHub release with hotfix notes
- [ ] Post announcement about critical fix
- [ ] Recommend users upgrade immediately
- [ ] Document the issue and resolution

---

## Troubleshooting

### Release Workflow Fails

**Issue**: GitHub Actions release workflow fails

**Solutions**:
1. Check workflow logs for specific error
2. Verify tag format matches `v*.*.*`
3. Ensure version in build.gradle matches tag
4. Check that all tests pass locally
5. Verify GitHub token has required permissions

### Artifacts Not Uploaded

**Issue**: Release created but artifacts missing

**Solutions**:
1. Check artifact upload step in workflow logs
2. Verify JAR files were built successfully
3. Ensure file paths in workflow are correct
4. Try manual upload via GitHub UI

### JavaDoc Generation Fails

**Issue**: JavaDoc task fails with errors

**Solutions**:
1. Review JavaDoc warnings and errors
2. Fix missing or malformed JavaDoc comments
3. Ensure all public APIs have JavaDoc
4. Check for broken links in JavaDoc

### Maven Central Publication Fails

**Issue**: Cannot publish to Maven Central

**Solutions**:
1. Verify Sonatype credentials are correct
2. Check GPG key configuration
3. Ensure POM has all required elements
4. Contact Sonatype support if issues persist

### Version Mismatch

**Issue**: Tag version doesn't match build version

**Solutions**:
1. Ensure build.gradle version is updated
2. Verify tag was created correctly
3. Check that changes were committed before tagging
4. Re-create tag if necessary

---

## Best Practices

### Do's ✅

- **Plan releases** with clear objectives
- **Test thoroughly** before releasing
- **Document everything** in changelog and release notes
- **Communicate clearly** with users
- **Follow semantic versioning** consistently
- **Automate** as much as possible
- **Verify artifacts** before announcing

### Don'ts ❌

- **Don't rush releases** without adequate testing
- **Don't skip documentation** updates
- **Don't release without changelog** entries
- **Don't forget to test** installation process
- **Don't neglect communication** with users
- **Don't make breaking changes** in patch releases
- **Don't delete releases** unless absolutely necessary

---

## Release Schedule

### Recommended Cadence

- **Major releases**: Annually or when breaking changes needed
- **Minor releases**: Quarterly for new features
- **Patch releases**: As needed for critical bugs

### Planning

- Create milestones 2-4 weeks before target release
- Freeze features 1 week before release
- Allow 3-5 days for testing and documentation
- Release early in the week (Tuesday-Wednesday preferred)

---

## Support

For questions about the release process:

- **GitHub Discussions**: [Ask questions](https://github.com/query4j/dynamicquerybuilder/discussions)
- **GitHub Issues**: [Report problems](https://github.com/query4j/dynamicquerybuilder/issues)
- **Documentation**: [Read guides](https://github.com/query4j/dynamicquerybuilder/tree/master/docs)

---

**Release Process Guide - Version 1.0.0**

*Last updated: October 1, 2024*
