# CI/CD Pipeline Documentation

This document provides comprehensive information about the Query4j Dynamic Query Builder CI/CD pipeline, including workflows, deployment processes, troubleshooting, and maintenance.

## Table of Contents

- [Overview](#overview)
- [Pipeline Architecture](#pipeline-architecture)
- [Workflows](#workflows)
- [Release Process](#release-process)
- [Security and Access](#security-and-access)
- [Monitoring and Alerts](#monitoring-and-alerts)
- [Troubleshooting](#troubleshooting)
- [Maintenance](#maintenance)

## Overview

The Query4j CI/CD pipeline is built on GitHub Actions and provides:

- **Continuous Integration**: Automated testing, code quality checks, and build verification
- **Continuous Deployment**: Automated release creation, artifact publishing, and documentation deployment
- **Security Scanning**: Automated vulnerability detection and compliance checks
- **Performance Monitoring**: Benchmark tracking and performance regression detection

### Key Features

✅ **Automated Testing**: Unit tests, integration tests, and coverage reporting  
✅ **Multi-JDK Support**: Tests on Java 17 and 21  
✅ **Code Quality**: Static analysis and style enforcement  
✅ **Performance Benchmarks**: JMH benchmarks with automated reporting  
✅ **Security Scanning**: CodeQL, dependency checks, and secret scanning  
✅ **Artifact Publishing**: GitHub Packages and GitHub Releases  
✅ **Documentation**: Automated JavaDoc generation and publishing  
✅ **Notifications**: Automated alerts on failures

## Pipeline Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                      CODE COMMIT/PR                          │
└────────────────────────┬────────────────────────────────────┘
                         │
        ┌────────────────┼────────────────┐
        │                │                │
        ▼                ▼                ▼
   ┌────────┐      ┌─────────┐     ┌──────────┐
   │   CI   │      │Security │     │   Docs   │
   │Pipeline│      │Scanning │     │Publishing│
   └────┬───┘      └────┬────┘     └────┬─────┘
        │               │               │
        └───────────────┴───────────────┘
                        │
                        ▼
              ┌──────────────────┐
              │   All Checks     │
              │     Passed?      │
              └────────┬─────────┘
                       │
           ┌───────────┼───────────┐
           │           │           │
           ▼           ▼           ▼
     ┌─────────┐  ┌────────┐  ┌──────┐
     │ Merge   │  │Release │  │Deploy│
     │ Ready   │  │Pipeline│  │ Docs │
     └─────────┘  └────┬───┘  └──────┘
                       │
        ┌──────────────┼──────────────┐
        │              │              │
        ▼              ▼              ▼
  ┌──────────┐  ┌──────────┐  ┌──────────┐
  │ GitHub   │  │ GitHub   │  │  GitHub  │
  │Releases  │  │Packages  │  │  Pages   │
  └──────────┘  └──────────┘  └──────────┘
```

## Workflows

### 1. CI Workflow (`.github/workflows/ci.yml`)

**Trigger**: Push to master/develop, Pull Requests

**Jobs**:
- **build-test**: Builds and tests on JDK 17 & 21
- **code-quality**: Static analysis and code style checks
- **benchmarks**: Performance benchmark execution (master branch only)
- **documentation**: JavaDoc generation

**Key Features**:
- Multi-version Java testing
- Code coverage reporting with Codecov
- Artifact archiving for test reports and benchmarks
- Caching for faster builds

**Example Run**:
```bash
# Local simulation
./gradlew clean build test jacocoRootReport
./gradlew benchmark:benchmarkJar
```

### 2. Release Workflow (`.github/workflows/release.yml`)

**Trigger**: Git tags (`v*.*.*`), Manual dispatch

**Jobs**:
1. **validate**: Version validation and prerelease detection
2. **build**: Full build with tests and artifacts
3. **publish-github**: Create GitHub Release with artifacts
4. **publish-packages**: Publish to GitHub Packages
5. **publish-docs**: Deploy documentation to GitHub Pages
6. **notify**: Send notifications and create issues on failure

**Triggering a Release**:

Option 1: Create and push a tag
```bash
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0
```

Option 2: Manual workflow dispatch
- Go to Actions → Release → Run workflow
- Enter version number (e.g., `1.0.0`)
- Select if it's a prerelease

**Release Artifacts**:
- Core, Cache, and Optimizer JAR files
- Sources JAR files
- JavaDoc JAR files
- Benchmark suite JAR

### 3. Security Workflow (`.github/workflows/security.yml`)

**Trigger**: Push, Pull Requests, Weekly schedule (Monday 00:00 UTC), Manual

**Jobs**:
- **codeql**: CodeQL security analysis
- **dependency-check**: Dependency vulnerability scanning
- **secret-scan**: TruffleHog secret detection
- **sbom**: Software Bill of Materials generation
- **license-check**: License compliance verification

**Security Reports**:
Results are available in:
- GitHub Security tab
- Workflow artifacts
- Dependabot alerts

### 4. Documentation Workflow (`.github/workflows/publish-docs.yml`)

**Trigger**: Push to master (docs changes), Manual dispatch

**Process**:
1. Generate JavaDoc for all modules
2. Create unified documentation site
3. Deploy to GitHub Pages

**Access Documentation**:
- Live URL: `https://query4j.github.io/dynamicquerybuilder/`
- Module JavaDocs: `/core/`, `/cache/`, `/optimizer/`

## Release Process

### Standard Release

1. **Preparation**:
   ```bash
   # Ensure master is up to date
   git checkout master
   git pull origin master
   
   # Update version in build.gradle (if not automated)
   # Update CHANGELOG.md with release notes
   git add .
   git commit -m "Prepare for release v1.0.0"
   git push origin master
   ```

2. **Create Release Tag**:
   ```bash
   git tag -a v1.0.0 -m "Release version 1.0.0"
   git push origin v1.0.0
   ```

3. **Pipeline Execution**:
   - Release workflow triggers automatically
   - All tests run
   - Artifacts are built
   - Release is created on GitHub
   - Packages are published
   - Documentation is updated

4. **Verification**:
   - Check GitHub Releases page
   - Verify artifacts are published
   - Test documentation links
   - Verify packages in GitHub Packages

### Hotfix Release

For urgent fixes to production:

1. **Create Hotfix Branch**:
   ```bash
   git checkout -b hotfix/v1.0.1 v1.0.0
   ```

2. **Apply Fix and Test**:
   ```bash
   # Make changes
   git commit -am "Fix critical issue"
   
   # Test locally
   ./gradlew clean build test
   ```

3. **Merge and Release**:
   ```bash
   git checkout master
   git merge --no-ff hotfix/v1.0.1
   git tag -a v1.0.1 -m "Hotfix release v1.0.1"
   git push origin master v1.0.1
   ```

### Rollback Process

If a release has critical issues:

1. **Immediate Actions**:
   - Mark the release as a draft or prerelease in GitHub
   - Document the issue in release notes
   - Notify users through appropriate channels

2. **Create Rollback Release**:
   ```bash
   # Revert to previous stable version
   git revert <problematic-commit>
   git tag -a v1.0.2 -m "Rollback to stable state"
   git push origin master v1.0.2
   ```

3. **Publish Rollback**:
   - Release pipeline creates new stable version
   - Update documentation with rollback notes

## Security and Access

### Required Secrets

Configure these in GitHub Settings → Secrets and variables → Actions:

| Secret Name | Purpose | Required For |
|-------------|---------|--------------|
| `CODECOV_TOKEN` | Upload coverage reports | CI workflow |
| `GITHUB_TOKEN` | Automated (GitHub provides) | All workflows |
| `OSSRH_USERNAME` | Maven Central publishing | Release (optional) |
| `OSSRH_PASSWORD` | Maven Central credentials | Release (optional) |
| `SIGNING_KEY` | GPG key for signing | Release (optional) |
| `SIGNING_PASSWORD` | GPG key password | Release (optional) |

### Permissions

Workflows require these permissions:

**CI Workflow**:
- `contents: read`
- `actions: read`

**Release Workflow**:
- `contents: write` (create releases)
- `packages: write` (publish packages)

**Security Workflow**:
- `security-events: write` (CodeQL)
- `contents: read`

**Documentation Workflow**:
- `contents: write` (GitHub Pages)

### Access Control

**Who Can Trigger Workflows**:
- Push/PR: Any contributor
- Manual dispatch: Repository admins and maintainers
- Release tags: Repository admins only

**Protected Branches**:
Configure branch protection on `master`:
- Require PR reviews
- Require status checks (CI) to pass
- Require signed commits (recommended)
- Restrict who can push

## Monitoring and Alerts

### GitHub Actions Dashboard

Monitor pipeline health:
1. Go to repository → Actions tab
2. View workflow runs and status
3. Filter by workflow, branch, or status

### Email Notifications

GitHub sends emails for:
- Workflow failures on your commits
- Failed scheduled workflows (security scans)

Configure in: Settings → Notifications

### Status Badges

Add to README.md:
```markdown
[![CI](https://github.com/query4j/dynamicquerybuilder/workflows/CI/badge.svg)](https://github.com/query4j/dynamicquerybuilder/actions/workflows/ci.yml)
[![Release](https://github.com/query4j/dynamicquerybuilder/workflows/Release/badge.svg)](https://github.com/query4j/dynamicquerybuilder/actions/workflows/release.yml)
[![Security](https://github.com/query4j/dynamicquerybuilder/workflows/Security%20Scanning/badge.svg)](https://github.com/query4j/dynamicquerybuilder/actions/workflows/security.yml)
```

### Metrics Tracking

Key metrics to monitor:
- Build success rate
- Average build time
- Test coverage percentage
- Security vulnerabilities
- Dependency freshness

Access via:
- GitHub Insights → Pulse
- Actions → Workflow analytics
- Codecov dashboard

## Troubleshooting

### Common Issues

#### Build Failures

**Symptom**: Build fails with compilation errors

**Solution**:
```bash
# Test locally first
./gradlew clean build --no-daemon --stacktrace

# Check Java version
java -version  # Should be 17 or 21

# Clear Gradle cache if needed
rm -rf ~/.gradle/caches/
```

#### Test Failures

**Symptom**: Tests pass locally but fail in CI

**Common Causes**:
- Timing issues (increase timeouts)
- Environment differences (check OS-specific code)
- Resource constraints (use smaller test datasets in CI)

**Debug**:
```bash
# Run with more verbose output
./gradlew test --info

# Run specific test
./gradlew test --tests "ClassName.testMethod"

# Check test reports in CI artifacts
```

#### Coverage Failures

**Symptom**: Coverage verification fails

**Solution**:
```bash
# Check coverage locally
./gradlew jacocoRootReport

# View report
open build/reports/jacoco/jacocoRootReport/html/index.html

# Coverage threshold is 85% (configured in build.gradle)
```

#### Publishing Failures

**Symptom**: Cannot publish artifacts

**Check**:
1. Verify secrets are configured
2. Check permissions on tokens
3. Verify version format
4. Check if version already exists

#### Documentation Deployment Fails

**Symptom**: GitHub Pages deployment fails

**Solution**:
1. Enable GitHub Pages in repository settings
2. Set source to "GitHub Actions"
3. Check workflow permissions

### Debug Mode

Enable debug logging:

1. Repository Settings → Secrets → Actions
2. Add secret: `ACTIONS_STEP_DEBUG` = `true`
3. Add secret: `ACTIONS_RUNNER_DEBUG` = `true`
4. Re-run workflow

### Getting Help

1. **Check Workflow Logs**: Click on failed step for details
2. **Review Recent Changes**: Compare with last successful run
3. **Search Issues**: Look for similar problems
4. **Ask Team**: Create issue with workflow run link

## Maintenance

### Regular Tasks

#### Weekly
- Review security scan results
- Check for dependency updates
- Monitor build performance

#### Monthly
- Update GitHub Actions versions
- Review and update documentation
- Audit access and permissions

#### Quarterly
- Review pipeline metrics
- Update workflow configurations
- Performance optimization review

### Updating Workflows

When modifying workflows:

1. **Test in Branch**:
   ```bash
   git checkout -b feature/update-ci
   # Edit workflow files
   git commit -am "Update CI workflow"
   git push origin feature/update-ci
   ```

2. **Create PR**: Test workflows run on the PR
3. **Review Changes**: Verify in PR workflow runs
4. **Merge**: Deploy to production

### Dependency Updates

GitHub Actions dependencies:

```yaml
# Keep actions up to date
uses: actions/checkout@v4  # Check for v5
uses: actions/setup-java@v4  # Check for updates
uses: gradle/actions/setup-gradle@v3  # Monitor releases
```

**Automatic Updates**: Consider enabling Dependabot for GitHub Actions

### Pipeline Performance

Optimize build times:

1. **Use Caching**:
   ```yaml
   - uses: actions/setup-java@v4
     with:
       cache: 'gradle'
   ```

2. **Parallel Jobs**: Keep jobs independent
3. **Minimal Builds**: Use `-x test` when tests aren't needed
4. **Matrix Strategy**: Test only on necessary JDK versions

### Archiving Old Workflows

For deprecated workflows:
1. Keep file but disable in repository settings
2. Add deprecation note in file header
3. Document migration path

## Best Practices

### Workflow Design
- Keep jobs focused and independent
- Use artifacts for job-to-job communication
- Always include cleanup steps
- Set appropriate timeouts

### Security
- Never commit secrets
- Use GitHub secrets for sensitive data
- Limit token permissions
- Regularly rotate credentials

### Testing
- Test workflows in branches first
- Use workflow dispatch for manual testing
- Validate all paths (success/failure)

### Documentation
- Keep this guide updated
- Document all custom scripts
- Add comments in workflows
- Update runbooks for incidents

## Support

For pipeline issues:

1. **Documentation**: Check this guide first
2. **Team Chat**: Ask in development channel
3. **GitHub Issues**: Create issue with:
   - Workflow run link
   - Error messages
   - Steps to reproduce
4. **Escalation**: Contact DevOps team

## Resources

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Gradle Build Scans](https://scans.gradle.com/)
- [Codecov Documentation](https://docs.codecov.com/)
- [Maven Publishing Guide](https://central.sonatype.org/publish/)

---

**Last Updated**: 2024-01  
**Maintained By**: Query4j DevOps Team  
**Version**: 1.0.0
