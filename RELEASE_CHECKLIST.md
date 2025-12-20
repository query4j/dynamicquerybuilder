# Release v1.0.0 - Execution Checklist

Print this checklist and check off items as you complete them.

## Pre-Release Verification
- [ ] Run `./release-prepare.sh`
- [ ] All checks show ✓ (green checkmarks)
- [ ] No critical issues found
- [ ] Working directory is clean (or only expected changes)

## Release Execution

### Creating the Release
- [ ] Open terminal in repository root
- [ ] Run: `git tag -a v1.0.0 -m "Release v1.0.0 - First stable release"`
- [ ] Run: `git push origin v1.0.0`
- [ ] Note the time: ___:___

### Monitoring Release Workflow
- [ ] Open: https://github.com/query4j/dynamicquerybuilder/actions
- [ ] Find "Release" workflow run
- [ ] Monitor "Validate Release" job - Status: ____
- [ ] Monitor "Build Release Artifacts" job - Status: ____
- [ ] Monitor "Publish to GitHub Releases" job - Status: ____
- [ ] Monitor "Publish to GitHub Packages" job - Status: ____
- [ ] Monitor "Publish Documentation" job - Status: ____
- [ ] All jobs completed successfully
- [ ] Workflow completion time: ___:___
- [ ] Total duration: ___ minutes

### Verifying Release Artifacts

#### GitHub Release
- [ ] Go to: https://github.com/query4j/dynamicquerybuilder/releases/tag/v1.0.0
- [ ] Release exists and is published
- [ ] Release notes are correct
- [ ] Verify JARs are attached:
  - [ ] dynamicquerybuilder-core-1.0.0.jar
  - [ ] dynamicquerybuilder-core-1.0.0-sources.jar
  - [ ] dynamicquerybuilder-core-1.0.0-javadoc.jar
  - [ ] dynamicquerybuilder-cache-1.0.0.jar
  - [ ] dynamicquerybuilder-cache-1.0.0-sources.jar
  - [ ] dynamicquerybuilder-cache-1.0.0-javadoc.jar
  - [ ] dynamicquerybuilder-optimizer-1.0.0.jar
  - [ ] dynamicquerybuilder-optimizer-1.0.0-sources.jar
  - [ ] dynamicquerybuilder-optimizer-1.0.0-javadoc.jar
  - [ ] benchmarks-1.0.0.jar

#### GitHub Packages
- [ ] Go to: https://github.com/orgs/query4j/packages
- [ ] Packages are published

#### Documentation
- [ ] Go to: https://query4j.github.io/dynamicquerybuilder/
- [ ] JavaDoc is accessible
- [ ] All three modules documented:
  - [ ] Core module
  - [ ] Cache module
  - [ ] Optimizer module

## Post-Release Tasks

### Immediate Actions
- [ ] Run: `./post-release.sh`
- [ ] Review post-release checklist
- [ ] Update to v1.1.0-SNAPSHOT (if prompted): Y/N

### GitHub Repository Updates
- [ ] Go to: https://github.com/query4j/dynamicquerybuilder/milestones
- [ ] Close milestone "v1.0.0"
- [ ] Create milestone "v1.1.0"
- [ ] Update repository description with latest version

### Announcements

#### GitHub Discussions
- [ ] Go to: https://github.com/query4j/dynamicquerybuilder/discussions
- [ ] Create new announcement post
- [ ] Title: "🎉 Query4j v1.0.0 Released!"
- [ ] Use template from RELEASE_v1.0.0_GUIDE.md
- [ ] Post announcement
- [ ] Pin the announcement

#### Social Media
- [ ] Twitter/X post (see ANNOUNCEMENT.md)
- [ ] LinkedIn post (see ANNOUNCEMENT.md)
- [ ] Reddit r/java post (see ANNOUNCEMENT.md)

### Documentation Updates
- [ ] Update README badges (if applicable)
- [ ] Verify all documentation links work
- [ ] Update project wiki with release info

### Monitoring (First 24 Hours)
- [ ] Check GitHub Issues for bug reports: _____ new issues
- [ ] Check GitHub Discussions for questions: _____ new discussions
- [ ] Monitor release artifact downloads: _____ downloads
- [ ] Check for workflow failures

## Issue Tracking

### Issues Found During Release
1. ________________________________________________
   Severity: _____ Status: _____ Action: ___________
   
2. ________________________________________________
   Severity: _____ Status: _____ Action: ___________

3. ________________________________________________
   Severity: _____ Status: _____ Action: ___________

## Sign-Off

- [ ] All release tasks completed
- [ ] All artifacts verified
- [ ] Announcements posted
- [ ] Post-release monitoring active

**Release Manager**: ___________________ Date: ___/___/______

**Technical Lead**: ___________________ Date: ___/___/______

**Notes**:
_______________________________________________________________
_______________________________________________________________
_______________________________________________________________
_______________________________________________________________

---

## Quick Reference Commands

```bash
# Pre-release verification
./release-prepare.sh

# Create release tag
git tag -a v1.0.0 -m "Release v1.0.0 - First stable release"

# Push release tag
git push origin v1.0.0

# Post-release tasks
./post-release.sh

# Update to next version
git add build.gradle
git commit -m "Prepare for next development iteration: 1.1.0-SNAPSHOT"
git push origin main
```

## Important Links

- Workflow: https://github.com/query4j/dynamicquerybuilder/actions
- Releases: https://github.com/query4j/dynamicquerybuilder/releases
- Packages: https://github.com/orgs/query4j/packages
- JavaDoc: https://query4j.github.io/dynamicquerybuilder/
- Issues: https://github.com/query4j/dynamicquerybuilder/issues
- Discussions: https://github.com/query4j/dynamicquerybuilder/discussions

## Emergency Contacts

If critical issues are found:
1. Create GitHub issue with "critical" label
2. Contact: @query4j, @sandeep-jaiswar
3. Follow rollback procedure in RELEASE_v1.0.0_GUIDE.md
