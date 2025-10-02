# Release Process Example

This document provides step-by-step examples for common release scenarios.

## Example 1: Standard Feature Release (v1.1.0)

### Scenario
You've completed development of new features and are ready to release version 1.1.0.

### Prerequisites
- All features merged to `master` branch
- All tests passing
- Documentation updated
- CHANGELOG.md updated with release notes

### Step-by-Step Process

#### 1. Verify Master Branch
```bash
git checkout master
git pull origin master
```

#### 2. Update Version and Changelog
```bash
# Edit CHANGELOG.md
cat >> CHANGELOG.md << 'EOF'
## [1.1.0] - 2024-01-15

### Added
- New predicate types for complex queries
- Enhanced caching with TTL support
- Additional optimizer heuristics

### Changed
- Improved query execution performance by 20%
- Enhanced error messages

### Fixed
- Fixed memory leak in cache module
- Corrected null handling in optimizer
EOF

# Commit changelog
git add CHANGELOG.md
git commit -m "Update changelog for v1.1.0 release"
git push origin master
```

#### 3. Run Final Checks Locally
```bash
# Full build with tests
./gradlew clean build test

# Generate coverage report
./gradlew jacocoRootReport

# Build benchmarks
./gradlew benchmark:benchmarkJar

# Verify all artifacts build successfully
./gradlew assemble javadoc
```

#### 4. Create and Push Release Tag
```bash
# Create annotated tag
git tag -a v1.1.0 -m "Release version 1.1.0

New Features:
- Enhanced predicate system
- TTL-based caching
- Advanced optimizer heuristics

Performance Improvements:
- 20% faster query execution
- Reduced memory footprint

Bug Fixes:
- Cache memory leak resolved
- Optimizer null handling corrected"

# Push tag to trigger release pipeline
git push origin v1.1.0
```

#### 5. Monitor Release Pipeline
1. Go to: https://github.com/query4j/dynamicquerybuilder/actions
2. Find the "Release" workflow run
3. Monitor each job:
   - ✅ Validate: Check version format
   - ✅ Build: Compile and test all modules
   - ✅ Publish GitHub: Create release with artifacts
   - ✅ Publish Packages: Upload to GitHub Packages
   - ✅ Publish Docs: Deploy documentation

#### 6. Verify Release
```bash
# Check GitHub Releases page
open https://github.com/query4j/dynamicquerybuilder/releases/tag/v1.1.0

# Verify artifacts are present:
# - query4j-core-1.1.0.jar
# - query4j-cache-1.1.0.jar
# - query4j-optimizer-1.1.0.jar
# - benchmarks-1.1.0.jar
# - Source JARs
# - JavaDoc JARs

# Check documentation is deployed
open https://query4j.github.io/dynamicquerybuilder/

# Verify packages in GitHub Packages
open https://github.com/query4j/dynamicquerybuilder/packages
```

#### 7. Post-Release Actions
```bash
# Prepare for next development cycle
sed -i "s/version = '1.1.0'/version = '1.2.0-SNAPSHOT'/" build.gradle

git add build.gradle
git commit -m "Prepare for v1.2.0 development"
git push origin master
```

#### 8. Announce Release
- Update project README if needed
- Post announcement in discussion forums
- Notify users via appropriate channels
- Update documentation wiki if applicable

---

## Example 2: Hotfix Release (v1.0.1)

### Scenario
A critical bug is discovered in production release v1.0.0 that requires immediate patching.

### Step-by-Step Process

#### 1. Create Hotfix Branch
```bash
# Create hotfix branch from the release tag
git checkout -b hotfix/v1.0.1 v1.0.0
```

#### 2. Apply Fix
```bash
# Make necessary code changes
# ... edit files ...

# Add tests for the fix
# ... create test cases ...

# Run tests to verify fix
./gradlew test
```

#### 3. Update Changelog
```bash
cat >> CHANGELOG.md << 'EOF'
## [1.0.1] - 2024-01-16

### Fixed
- Critical: Fixed connection leak in query executor
- Fixed incorrect predicate ordering in edge case
EOF

git add .
git commit -m "Fix critical connection leak (issue #123)"
```

#### 4. Test Thoroughly
```bash
# Full build with all tests
./gradlew clean build test

# Run integration tests
./gradlew integrationTest

# Manual testing if needed
```

#### 5. Merge to Master
```bash
git checkout master
git merge --no-ff hotfix/v1.0.1
git push origin master
```

#### 6. Create Hotfix Tag
```bash
git tag -a v1.0.1 -m "Hotfix release v1.0.1

Critical Fixes:
- Connection leak in query executor
- Predicate ordering edge case"

git push origin v1.0.1
```

#### 7. Monitor and Verify
Follow steps 5-6 from Example 1 to monitor pipeline and verify release.

#### 8. Backport to Development Branch (if needed)
```bash
git checkout develop
git merge hotfix/v1.0.1
git push origin develop
```

#### 9. Cleanup
```bash
git branch -d hotfix/v1.0.1
```

---

## Example 3: Pre-Release (v2.0.0-beta.1)

### Scenario
You're preparing a major version with breaking changes and want to release a beta for testing.

### Step-by-Step Process

#### 1. Create Release Branch
```bash
git checkout -b release/v2.0.0-beta.1 develop
```

#### 2. Update Version for Beta
```bash
sed -i "s/version = '.*'/version = '2.0.0-beta.1'/" build.gradle
git add build.gradle
git commit -m "Prepare v2.0.0-beta.1 release"
git push origin release/v2.0.0-beta.1
```

#### 3. Update Changelog
```bash
cat >> CHANGELOG.md << 'EOF'
## [2.0.0-beta.1] - 2024-01-20

### Breaking Changes
- Removed deprecated API methods (see MIGRATION.md)
- Changed configuration property names
- Updated minimum Java version to 17

### Added
- Async query execution API
- Reactive Streams support
- Enhanced optimizer with cost-based optimization

### Migration Guide
See MIGRATION.md for detailed upgrade instructions.
EOF

git add CHANGELOG.md
git commit -m "Update changelog for v2.0.0-beta.1"
```

#### 4. Create Beta Tag
```bash
git tag -a v2.0.0-beta.1 -m "Beta release v2.0.0-beta.1

Breaking Changes:
- API modernization
- Configuration updates
- Java 17 minimum

New Features:
- Async/Reactive support
- Cost-based optimizer"

git push origin v2.0.0-beta.1
```

#### 5. Monitor Release
The release pipeline will:
- Detect "beta" in version → mark as pre-release
- Build and publish artifacts
- Deploy documentation

#### 6. Verify Pre-Release Status
```bash
# Check that release is marked as pre-release on GitHub
open https://github.com/query4j/dynamicquerybuilder/releases/tag/v2.0.0-beta.1

# Look for "Pre-release" badge
```

---

## Example 4: Manual Release Dispatch

### Scenario
You want to create a release without pushing a tag (e.g., for testing or special circumstances).

### Step-by-Step Process

#### 1. Go to GitHub Actions
```
https://github.com/query4j/dynamicquerybuilder/actions/workflows/release.yml
```

#### 2. Click "Run workflow"
- Select branch: `master` (or appropriate branch)
- Enter version: `1.1.0`
- Is prerelease?: Select `false` or `true`

#### 3. Click "Run workflow" button

#### 4. Monitor Execution
Watch the workflow progress in the Actions tab.

---

## Example 5: Rollback After Problematic Release

### Scenario
Version 1.2.0 was released but has a critical issue. Need to rollback and release 1.2.1.

### Step-by-Step Process

#### 1. Mark Problematic Release
```bash
# Go to GitHub Releases page
# Edit v1.2.0 release
# Add warning to release notes:
"⚠️ WARNING: This release has a critical issue. Please use v1.2.1 instead."
# Mark as pre-release
```

#### 2. Revert Problematic Changes
```bash
git checkout master

# Option A: Revert specific commits
git revert <commit-sha>

# Option B: Revert to previous stable state
git reset --hard v1.1.0
# Then cherry-pick good commits
git cherry-pick <good-commit-1> <good-commit-2>

git push origin master
```

#### 3. Create Rollback Release
```bash
# Update changelog
cat >> CHANGELOG.md << 'EOF'
## [1.2.1] - 2024-01-18

### Fixed
- Rolled back problematic changes from v1.2.0
- Restored stable functionality from v1.1.0
- Applied selective fixes without breaking changes

### Note
This release supersedes v1.2.0 which contained critical issues.
EOF

git add CHANGELOG.md
git commit -m "Rollback release v1.2.1"
git push origin master

# Create tag
git tag -a v1.2.1 -m "Rollback release v1.2.1"
git push origin v1.2.1
```

#### 4. Communicate
- Update README with notice
- Send notification to users
- Update documentation
- Create incident post-mortem

---

## Release Checklist Template

Use this checklist for every release:

### Pre-Release
- [ ] All features merged to master
- [ ] All tests passing locally
- [ ] Code coverage meets threshold (≥85%)
- [ ] CHANGELOG.md updated
- [ ] Documentation updated
- [ ] Migration guide created (for breaking changes)
- [ ] Version number decided (semantic versioning)
- [ ] Release notes drafted

### Release
- [ ] Master branch up to date
- [ ] Final build and test run successful
- [ ] Tag created and pushed
- [ ] Pipeline execution monitored
- [ ] All workflow jobs completed successfully

### Post-Release
- [ ] GitHub Release created with correct artifacts
- [ ] Packages published to GitHub Packages
- [ ] Documentation deployed to GitHub Pages
- [ ] Release announcement posted
- [ ] Version bumped for next development cycle
- [ ] Team notified
- [ ] Known issues documented

### Verification
- [ ] Artifacts downloadable from GitHub Releases
- [ ] Packages installable from GitHub Packages
- [ ] Documentation accessible online
- [ ] Links in README work correctly
- [ ] Release notes complete and accurate

---

## Troubleshooting Release Issues

### Issue: Release Pipeline Fails at Build Stage
**Solution**:
1. Check workflow logs for specific error
2. Run build locally: `./gradlew clean build test`
3. Fix issue, commit, and recreate tag

### Issue: Artifacts Not Uploaded
**Solution**:
1. Check GitHub token permissions
2. Verify artifact paths in workflow
3. Re-run workflow from Actions tab

### Issue: Documentation Deployment Fails
**Solution**:
1. Ensure GitHub Pages is enabled
2. Check workflow permissions
3. Verify JavaDoc generation: `./gradlew javadoc`

### Issue: Version Conflict
**Solution**:
1. Delete tag: `git tag -d v1.0.0 && git push origin :v1.0.0`
2. Update version in build.gradle
3. Recreate tag with correct version

---

## Best Practices

1. **Always test locally first** - Run full build before creating tags
2. **Use semantic versioning** - Follow MAJOR.MINOR.PATCH format
3. **Write detailed release notes** - Help users understand changes
4. **Monitor pipelines** - Don't assume success, verify each step
5. **Keep changelog updated** - Document as you develop, not at release time
6. **Tag consistently** - Use format `v1.2.3` for all releases
7. **Test rollback procedures** - Ensure you can revert if needed
8. **Communicate clearly** - Announce releases through appropriate channels

---

**For complete documentation, see [CICD_PIPELINE.md](./CICD_PIPELINE.md)**
