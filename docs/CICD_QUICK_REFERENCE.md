# CI/CD Quick Reference Guide

Quick reference for common CI/CD operations in the Query4j Dynamic Query Builder project.

## 🚀 Quick Actions

### Creating a Release

```bash
# 1. Prepare release
git checkout master
git pull origin master

# 2. Create and push tag
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0

# 3. Monitor pipeline
# Go to: https://github.com/query4j/dynamicquerybuilder/actions
```

### Manual Workflow Triggers

**Trigger Documentation Build**:
```bash
# Via GitHub UI:
Actions → Publish Documentation → Run workflow → Run
```

**Trigger Security Scan**:
```bash
# Via GitHub UI:
Actions → Security Scanning → Run workflow → Run
```

**Trigger Release**:
```bash
# Via GitHub UI:
Actions → Release → Run workflow
# Enter version: 1.0.0
# Select prerelease: false/true
```

## 🔍 Monitoring

### Check Pipeline Status

**CI Status**:
```
https://github.com/query4j/dynamicquerybuilder/actions/workflows/ci.yml
```

**Latest Release**:
```
https://github.com/query4j/dynamicquerybuilder/releases/latest
```

**Coverage Reports**:
```
https://codecov.io/gh/query4j/dynamicquerybuilder
```

**Documentation**:
```
https://query4j.github.io/dynamicquerybuilder/
```

### Status Badges

```markdown
[![CI](https://github.com/query4j/dynamicquerybuilder/workflows/CI/badge.svg)](https://github.com/query4j/dynamicquerybuilder/actions/workflows/ci.yml)
[![Release](https://github.com/query4j/dynamicquerybuilder/workflows/Release/badge.svg)](https://github.com/query4j/dynamicquerybuilder/actions/workflows/release.yml)
[![Security](https://github.com/query4j/dynamicquerybuilder/workflows/Security%20Scanning/badge.svg)](https://github.com/query4j/dynamicquerybuilder/actions/workflows/security.yml)
[![codecov](https://codecov.io/gh/query4j/dynamicquerybuilder/branch/master/graph/badge.svg)](https://codecov.io/gh/query4j/dynamicquerybuilder)
```

## 🧪 Local Testing

### Run Full CI Pipeline Locally

```bash
# Clean build
./gradlew clean

# Build all modules
./gradlew build -x test

# Run tests
./gradlew test

# Generate coverage
./gradlew jacocoRootReport jacocoRootCoverageVerification

# Generate JavaDoc
./gradlew javadoc

# Build benchmark JAR
./gradlew benchmark:benchmarkJar
```

### Quick Checks

```bash
# Fast build (no tests)
./gradlew clean build -x test

# Test specific module
./gradlew core:test

# Run single test
./gradlew test --tests "QueryBuilderTest"

# Check coverage
./gradlew jacocoRootReport
open build/reports/jacoco/jacocoRootReport/html/index.html
```

## 🐛 Troubleshooting

### Build Failures

```bash
# Full stack trace
./gradlew build --stacktrace

# Info logging
./gradlew build --info

# Debug logging
./gradlew build --debug

# Clear cache
rm -rf ~/.gradle/caches/
./gradlew clean build --no-daemon
```

### Test Failures

```bash
# Verbose test output
./gradlew test --info

# Run single test class
./gradlew test --tests "ClassName"

# Run tests matching pattern
./gradlew test --tests "*Integration*"

# Re-run failed tests only
./gradlew test --rerun-tasks
```

### Coverage Issues

```bash
# Generate detailed coverage
./gradlew jacocoRootReport

# View HTML report
open build/reports/jacoco/jacocoRootReport/html/index.html

# Check verification rules
./gradlew jacocoRootCoverageVerification

# Current threshold: 85% (configured in build.gradle)
```

## 📦 Publishing

### Publish to GitHub Packages

```bash
# Set credentials
export GITHUB_ACTOR=your-username
export GITHUB_TOKEN=your-personal-access-token

# Publish
./gradlew publish
```

### Build Release Artifacts

```bash
# Build all artifacts
./gradlew clean assemble javadoc

# Artifacts location:
# - JARs: */build/libs/*.jar
# - JavaDoc: */build/docs/javadoc/
# - Sources: */build/libs/*-sources.jar
```

## 🔐 Security

### Run Security Scans Locally

```bash
# Dependency check (if configured)
./gradlew dependencyCheckAnalyze

# License check
./gradlew checkLicense

# Generate SBOM
./gradlew cyclonedxBom
```

### Check for Secrets

```bash
# Install TruffleHog
pip install trufflehog

# Scan repository
trufflehog git file://. --only-verified
```

## 📊 Benchmarks

### Run Benchmarks

```bash
# Build benchmark JAR
./gradlew benchmark:benchmarkJar

# Run all benchmarks (quick mode)
cd benchmark
java -jar build/libs/benchmarks-*.jar -wi 2 -i 3 -f 1

# Run specific benchmark
java -jar build/libs/benchmarks-*.jar BasicQueryBenchmark

# Generate detailed results
java -jar build/libs/benchmarks-*.jar -rf json -rff results.json
```

## 📚 Documentation

### Generate Documentation

```bash
# Generate JavaDoc for all modules
./gradlew javadoc

# View core module
open core/build/docs/javadoc/index.html

# View cache module
open cache/build/docs/javadoc/index.html

# View optimizer module
open optimizer/build/docs/javadoc/index.html
```

### Test Documentation Site

```bash
# Install Python HTTP server (if not available)
python3 -m http.server 8000 --directory docs-site

# Open browser to:
# http://localhost:8000
```

## 🔄 Workflow Files

### Workflow Locations

```
.github/workflows/
├── ci.yml              # Main CI pipeline
├── release.yml         # Release automation
├── security.yml        # Security scanning
└── publish-docs.yml    # Documentation publishing
```

### Edit Workflows

```bash
# Create feature branch
git checkout -b feature/update-workflows

# Edit workflow
vim .github/workflows/ci.yml

# Test by pushing
git add .github/workflows/ci.yml
git commit -m "Update CI workflow"
git push origin feature/update-workflows

# Create PR to test
```

## 🎯 Common Tasks

### Hotfix Release

```bash
# 1. Create hotfix branch from tag
git checkout -b hotfix/v1.0.1 v1.0.0

# 2. Apply fix
# ... make changes ...
git commit -am "Fix critical issue"

# 3. Test locally
./gradlew clean build test

# 4. Merge to master
git checkout master
git merge --no-ff hotfix/v1.0.1

# 5. Tag and push
git tag -a v1.0.1 -m "Hotfix release v1.0.1"
git push origin master v1.0.1
```

### Rollback Release

```bash
# 1. Mark release as problematic in GitHub UI
# 2. Revert problematic commits
git revert <commit-sha>

# 3. Create new release
git tag -a v1.0.2 -m "Rollback release"
git push origin master v1.0.2
```

### Update Dependencies

```bash
# Check for updates
./gradlew dependencyUpdates

# Update Gradle wrapper
./gradlew wrapper --gradle-version=8.5

# Test after updates
./gradlew clean build test
```

## 🆘 Emergency Procedures

### Pipeline is Down

1. Check GitHub status: https://www.githubstatus.com/
2. Review workflow logs in Actions tab
3. Check for quota limits
4. Contact GitHub support if needed

### Failed Release

1. Don't panic - releases can be recreated
2. Check error in Actions → Release workflow
3. Fix issue locally and test
4. Delete tag: `git tag -d v1.0.0 && git push origin :v1.0.0`
5. Recreate tag after fix

### Secrets Exposed

1. Immediately rotate all exposed credentials
2. Update GitHub secrets
3. Audit recent workflow runs
4. Review security scan results
5. Document incident

## 📞 Contacts

- **Pipeline Issues**: DevOps team
- **Security Issues**: Security team
- **General Questions**: Development team

## 🔗 Useful Links

- **Workflows**: https://github.com/query4j/dynamicquerybuilder/actions
- **Releases**: https://github.com/query4j/dynamicquerybuilder/releases
- **Documentation**: https://query4j.github.io/dynamicquerybuilder/
- **Coverage**: https://codecov.io/gh/query4j/dynamicquerybuilder
- **Full Guide**: [CICD_PIPELINE.md](./CICD_PIPELINE.md)

---

**For detailed information, see [CICD_PIPELINE.md](./CICD_PIPELINE.md)**
