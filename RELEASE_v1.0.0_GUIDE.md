# Release v1.0.0 - Execution Guide

This document provides step-by-step instructions for executing the v1.0.0 release of Query4j Dynamic Query Builder.

## Pre-Release Status ✅

All pre-release checks have been completed and verified:

- ✅ Version updated to `1.0.0` (stable, not SNAPSHOT)
- ✅ All tests passing (574+ tests)
- ✅ Code coverage: 95%+ across all modules
- ✅ Build successful with all artifacts
- ✅ JavaDoc generation successful
- ✅ Release notes prepared (`RELEASE_NOTES_v1.0.0.md`)
- ✅ CHANGELOG updated with v1.0.0 details
- ✅ No critical security vulnerabilities detected
- ✅ Documentation comprehensive and accurate

## Automated Release Scripts

Two scripts have been created to assist with the release process:

### 1. Pre-Release Verification Script

**File**: `release-prepare.sh`

Run this script before creating the release tag to verify all checks pass:

```bash
./release-prepare.sh
```

This script:
- Verifies Java 17+ is installed
- Checks version in build.gradle is not SNAPSHOT
- Runs all tests
- Checks code coverage
- Builds all modules
- Generates JavaDoc
- Verifies release documentation exists
- Checks that the release tag doesn't already exist

### 2. Post-Release Tasks Script

**File**: `post-release.sh`

Run this script after the release is published:

```bash
./post-release.sh
```

This script:
- Displays post-release checklist
- Helps prepare the next development version
- Updates build.gradle to next SNAPSHOT version

## Release Execution Steps

### Step 1: Final Pre-Release Verification

```bash
# Run the pre-release verification script
./release-prepare.sh
```

**Expected outcome**: All checks should pass with green checkmarks (✓)

### Step 2: Create and Push Release Tag

Once all checks pass, create and push the release tag:

```bash
# Create annotated tag
git tag -a v1.0.0 -m "Release v1.0.0 - First stable release"

# Push tag to remote
git push origin v1.0.0
```

**What happens next**: 
- GitHub Actions will detect the new tag
- The release workflow (`.github/workflows/release.yml`) will automatically trigger
- The workflow will:
  - Validate the release version
  - Build all artifacts
  - Run full test suite
  - Generate code coverage report
  - Create GitHub Release with artifacts
  - Publish to GitHub Packages
  - Deploy documentation to GitHub Pages

### Step 3: Monitor Release Workflow

1. Go to: https://github.com/query4j/dynamicquerybuilder/actions
2. Find the "Release" workflow run
3. Monitor each job:
   - ✅ Validate Release
   - ✅ Build Release Artifacts
   - ✅ Publish to GitHub Releases
   - ✅ Publish to GitHub Packages
   - ✅ Publish Documentation
   - ✅ Notify Release

**Estimated time**: 10-15 minutes

### Step 4: Verify Release Artifacts

After the workflow completes, verify:

1. **GitHub Release**: https://github.com/query4j/dynamicquerybuilder/releases/tag/v1.0.0
   - Release notes are correct
   - All JARs are attached:
     - `dynamicquerybuilder-core-1.0.0.jar`
     - `dynamicquerybuilder-core-1.0.0-sources.jar`
     - `dynamicquerybuilder-core-1.0.0-javadoc.jar`
     - `dynamicquerybuilder-cache-1.0.0.jar`
     - `dynamicquerybuilder-cache-1.0.0-sources.jar`
     - `dynamicquerybuilder-cache-1.0.0-javadoc.jar`
     - `dynamicquerybuilder-optimizer-1.0.0.jar`
     - `dynamicquerybuilder-optimizer-1.0.0-sources.jar`
     - `dynamicquerybuilder-optimizer-1.0.0-javadoc.jar`
     - `benchmarks-1.0.0.jar`

2. **GitHub Packages**: https://github.com/orgs/query4j/packages
   - Verify packages are published

3. **Documentation**: https://query4j.github.io/dynamicquerybuilder/
   - Verify JavaDoc is accessible
   - Check all module documentation links

### Step 5: Post-Release Tasks

Run the post-release script:

```bash
./post-release.sh
```

Follow the prompts to:
- Update to next development version (1.1.0-SNAPSHOT)
- Commit and push version update

### Step 6: Announcements

#### GitHub Discussions

Post announcement to: https://github.com/query4j/dynamicquerybuilder/discussions

Use this template:

```markdown
# 🎉 Query4j v1.0.0 Released!

We're excited to announce the first stable release of Query4j Dynamic Query Builder!

## What's New

- Production-ready with 95%+ test coverage
- High-performance query building (sub-microsecond operations)
- Thread-safe immutable builder pattern
- Comprehensive documentation and examples
- Optional caching and query optimization modules

## Getting Started

```xml
<dependency>
    <groupId>com.github.query4j</groupId>
    <artifactId>dynamicquerybuilder-core</artifactId>
    <version>1.0.0</version>
</dependency>
```

For full release notes, see: [RELEASE_NOTES_v1.0.0.md](../RELEASE_NOTES_v1.0.0.md)

## Resources

- [Quick Start Guide](../QUICKSTART.md)
- [API Documentation](https://query4j.github.io/dynamicquerybuilder/)
- [GitHub Repository](https://github.com/query4j/dynamicquerybuilder)

Thank you to all contributors who made this release possible! 🙏
```

#### Social Media

See `ANNOUNCEMENT.md` for templates for:
- Twitter/X
- LinkedIn
- Reddit r/java

### Step 7: Repository Updates

1. **Close Milestone**: https://github.com/query4j/dynamicquerybuilder/milestones
   - Close "v1.0.0" milestone

2. **Create Next Milestone**: 
   - Create "v1.1.0" milestone

3. **Update README Badges** (if applicable):
   - Version badge
   - Release date badge

4. **Update Project Wiki**:
   - Add release information
   - Update getting started guides

## Rollback Plan

If critical issues are discovered after release:

### Option 1: Quick Patch Release

For easily fixable bugs:

```bash
# Create hotfix branch
git checkout -b hotfix/1.0.1 v1.0.0

# Apply fixes and test
# ... make changes ...

# Update version
sed -i "s/version = '1.0.0'/version = '1.0.1'/" build.gradle

# Commit and tag
git commit -am "Hotfix: [describe fix]"
git tag -a v1.0.1 -m "Hotfix release v1.0.1"
git push origin v1.0.1
```

### Option 2: Mark as Pre-Release

For serious issues:

1. Go to GitHub Release page
2. Edit release v1.0.0
3. Check "This is a pre-release"
4. Update description with warning
5. Recommend users stay on previous version (if applicable)

### Option 3: Delete Release

**Only as last resort** for critical security issues:

```bash
# Delete tag locally
git tag -d v1.0.0

# Delete tag remotely
git push origin :refs/tags/v1.0.0

# Delete GitHub Release manually via UI
```

Then fix issues and re-release.

## Troubleshooting

### Release Workflow Fails

1. Check GitHub Actions logs for error details
2. Common issues:
   - Test failures: Review test logs
   - Build failures: Check dependencies
   - Publishing failures: Verify credentials/permissions

### Artifacts Missing

1. Check workflow artifacts in GitHub Actions UI
2. Verify upload steps completed successfully
3. May need to manually upload missing artifacts

### Documentation Not Publishing

1. Check "Publish Documentation" job in workflow
2. Verify GitHub Pages is enabled in repository settings
3. Check for errors in deployment step

## Post-Release Monitoring

Monitor for 24-48 hours after release:

- **GitHub Issues**: Watch for bug reports
- **GitHub Discussions**: Respond to questions
- **GitHub Actions**: Monitor for any workflow failures
- **Downloads**: Track release asset downloads

## Success Criteria

Release is considered successful when:

- ✅ All GitHub Actions workflows complete successfully
- ✅ All artifacts are available in GitHub Releases
- ✅ Documentation is accessible
- ✅ No critical bugs reported within 24 hours
- ✅ Community feedback is positive

## Contact

For release questions or issues:
- GitHub Issues: https://github.com/query4j/dynamicquerybuilder/issues
- GitHub Discussions: https://github.com/query4j/dynamicquerybuilder/discussions

---

**Release Manager**: @query4j  
**Technical Lead**: @sandeep-jaiswar  
**Release Date**: TBD (See GitHub Release for actual date)
