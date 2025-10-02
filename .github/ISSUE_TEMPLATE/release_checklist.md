---
name: Release Checklist
about: Template for preparing and publishing a new release
title: 'Release v[VERSION]'
labels: release
assignees: ''
---

# Release Checklist for v[VERSION]

## Pre-Release

### Version Updates
- [ ] Update version in `build.gradle` from `-SNAPSHOT` to stable version
- [ ] Update version references in `README.md`
- [ ] Update version references in all module `README.md` files
- [ ] Update version references in documentation

### Documentation
- [ ] Update `CHANGELOG.md` with release notes
- [ ] Create `RELEASE_NOTES_v[VERSION].md` with detailed changes
- [ ] Update `UPGRADE_GUIDE.md` if there are breaking changes
- [ ] Review and update all documentation for accuracy
- [ ] Verify all code examples work with new version

### Quality Checks
- [ ] All tests pass: `./gradlew clean test`
- [ ] Code coverage meets threshold (95%+): `./gradlew jacocoRootReport`
- [ ] Build succeeds: `./gradlew clean build`
- [ ] JavaDoc generates without errors: `./gradlew javadoc`
- [ ] No critical security vulnerabilities
- [ ] Performance benchmarks completed and documented

### Code Review
- [ ] All PRs for this release are merged
- [ ] Code review completed for all changes
- [ ] No outstanding critical or high-priority issues
- [ ] Release branch created (if applicable)

## Release

### Tag and Publish
- [ ] Create git tag: `git tag -a v[VERSION] -m "Release v[VERSION]"`
- [ ] Push tag: `git push origin v[VERSION]`
- [ ] Verify GitHub Actions release workflow succeeds
- [ ] Verify release artifacts are uploaded to GitHub Releases
- [ ] Verify checksums are generated for all artifacts

### Artifacts
- [ ] Core module JAR published
- [ ] Cache module JAR published
- [ ] Optimizer module JAR published
- [ ] Source JARs published
- [ ] JavaDoc JARs published
- [ ] Benchmark JAR published
- [ ] All checksums verified

### Documentation Publishing
- [ ] JavaDoc published to GitHub Pages
- [ ] Documentation site updated
- [ ] Release notes published
- [ ] Changelog accessible

## Post-Release

### Announcements
- [ ] Create GitHub release with notes
- [ ] Update repository description with latest version
- [ ] Post announcement to GitHub Discussions
- [ ] Share on Twitter/X (use template from ANNOUNCEMENT.md)
- [ ] Share on LinkedIn (use template from ANNOUNCEMENT.md)
- [ ] Post to Reddit r/java (use template from ANNOUNCEMENT.md)
- [ ] Update project wiki with release information

### Repository Updates
- [ ] Update README badges with new version
- [ ] Create next development version (e.g., v[NEXT]-SNAPSHOT)
- [ ] Update roadmap with completed features
- [ ] Close milestone for this release
- [ ] Create milestone for next release

### Communication
- [ ] Notify users via GitHub Discussions
- [ ] Respond to release feedback and questions
- [ ] Monitor GitHub Issues for release-related bugs
- [ ] Update support documentation if needed

### Verification
- [ ] Test installation via Maven Central (when published)
- [ ] Verify download links work
- [ ] Test sample application with released version
- [ ] Verify documentation links are not broken

## Maven Central (If Applicable)

- [ ] Artifacts uploaded to staging repository
- [ ] Artifacts signed with GPG
- [ ] Staging repository closed
- [ ] Staging repository released
- [ ] Artifacts appear in Maven Central (allow 2-4 hours)
- [ ] Artifacts searchable on search.maven.org

## Rollback Plan

In case of critical issues found post-release:

1. **Document the issue**: Create GitHub issue with "critical" label
2. **Assess impact**: Determine severity and number of affected users
3. **Communication**: Post announcement about the issue
4. **Fix or revert**: 
   - If fixable quickly: Release patch version
   - If serious: Mark release as pre-release and recommend previous version
5. **Follow-up**: Release corrected version with increment

## Notes

- Release date: [DATE]
- Release manager: @[USERNAME]
- Special considerations: [ANY SPECIAL NOTES]

## Approval

- [ ] Release manager approval: @[MANAGER]
- [ ] Technical lead approval: @[TECH_LEAD]
- [ ] Documentation review: @[DOC_REVIEWER]

---

## Post-Release Retrospective

After the release, document:

1. What went well
2. What could be improved
3. Any issues encountered
4. Lessons learned
5. Process improvements for next release
