# Wiki Maintenance Workflow

This document describes the complete workflow for maintaining and updating the Query4j GitHub Wiki.

## Overview

The Query4j Wiki is the central living documentation for the project. It must be kept current, accurate, and comprehensive with each release and significant change.

## Workflow Checklist

### For Each Release

Use this checklist when preparing a new release:

#### 1. Pre-Release Wiki Review (1-2 weeks before release)

- [ ] Review all wiki pages for accuracy
- [ ] Update code examples to match latest API
- [ ] Verify all internal links work
- [ ] Verify all external links are valid
- [ ] Check for outdated screenshots or diagrams
- [ ] Review performance benchmarks for currency
- [ ] Update configuration examples if options changed
- [ ] Review error catalog for new error codes
- [ ] Check FAQ for new common questions

#### 2. Release Documentation Updates (during release)

- [ ] Update version numbers in all pages
- [ ] Update "Last Updated" dates
- [ ] Add release notes entry in Release-Notes.md
- [ ] Update Home.md roadmap section
- [ ] Document any breaking changes
- [ ] Add migration guide if needed
- [ ] Update API Reference for new/changed APIs
- [ ] Update benchmarks if performance changed

#### 3. Post-Release Verification (after release)

- [ ] Verify wiki reflects released version
- [ ] Test all code examples with released version
- [ ] Verify JavaDoc links point to correct version
- [ ] Monitor for user questions indicating documentation gaps
- [ ] Review GitHub Issues for documentation-related problems

### For Feature Additions

When adding a new feature:

1. **Update Affected Pages:**
   - [ ] Core Module / Cache Manager / Optimizer (depending on feature)
   - [ ] API Reference (if new public APIs)
   - [ ] Configuration (if new config options)
   - [ ] Getting Started (if affects basic usage)

2. **Add Examples:**
   - [ ] Code examples demonstrating feature
   - [ ] Best practices for feature usage
   - [ ] Common pitfalls to avoid

3. **Update Cross-References:**
   - [ ] Add links from related pages
   - [ ] Update "See Also" sections
   - [ ] Update sidebar if major feature

### For Bug Fixes

When fixing significant bugs:

1. **Update Troubleshooting:**
   - [ ] Add to FAQ if commonly encountered
   - [ ] Update Error Handling page if error-related
   - [ ] Document workarounds if applicable

2. **Update Examples:**
   - [ ] Fix any examples affected by the bug
   - [ ] Add examples showing correct usage

### For Configuration Changes

When modifying configuration:

1. **Update Configuration Page:**
   - [ ] Document new/changed options
   - [ ] Update default values
   - [ ] Add/update examples
   - [ ] Note any deprecations

2. **Update Related Pages:**
   - [ ] Core Module / Cache / Optimizer pages
   - [ ] Getting Started (if affects setup)
   - [ ] Migration guide (if breaking change)

## Update Procedures

### Method 1: Direct Wiki Repository Update

For small changes (typos, minor corrections):

```bash
# Clone wiki repository
git clone https://github.com/query4j/dynamicquerybuilder.wiki.git
cd dynamicquerybuilder.wiki

# Make changes
vim Page-Name.md

# Commit and push
git add .
git commit -m "docs: fix typo in Page Name"
git push origin master
```

### Method 2: Main Repository PR (Recommended)

For significant changes (new content, restructuring):

```bash
# In main repository
cd dynamicquerybuilder
git checkout -b docs/update-wiki-feature-x

# Make changes in wiki/ directory
vim wiki/Core-Module.md

# Commit changes
git add wiki/
git commit -m "docs: update Core Module wiki for feature X"

# Push and create PR
git push origin docs/update-wiki-feature-x
# Create PR on GitHub

# After PR approval and merge, sync to wiki:
cd ../dynamicquerybuilder.wiki
cp ../dynamicquerybuilder/wiki/*.md .
git add .
git commit -m "docs: sync wiki with main repository after feature X"
git push origin master
```

### Method 3: Automated Sync (Future Enhancement)

Consider implementing automation:

```yaml
# .github/workflows/sync-wiki.yml
name: Sync Wiki

on:
  push:
    branches:
      - main
    paths:
      - 'wiki/**'

jobs:
  sync:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout main repo
        uses: actions/checkout@v3
        with:
          path: main
      
      - name: Checkout wiki repo
        uses: actions/checkout@v3
        with:
          repository: query4j/dynamicquerybuilder.wiki
          path: wiki
      
      - name: Sync files
        run: |
          cp main/wiki/*.md wiki/
          cd wiki
          git config user.name "GitHub Actions"
          git config user.email "actions@github.com"
          git add .
          git diff-index --quiet HEAD || git commit -m "docs: auto-sync from main repository"
          git push
```

## Version Update Script

Use this script to update version numbers across all wiki pages:

```bash
#!/bin/bash
# scripts/update-wiki-version.sh

set -e

NEW_VERSION="$1"
WIKI_DIR="wiki"

if [ -z "$NEW_VERSION" ]; then
    echo "Usage: $0 <version>"
    echo "Example: $0 1.1.0"
    exit 1
fi

echo "Updating wiki to version $NEW_VERSION..."

# Update version in footer
find "$WIKI_DIR" -name "*.md" -type f -exec sed -i \
    "s/Version [0-9]\+\.[0-9]\+\.[0-9]\+/Version $NEW_VERSION/g" {} \;

# Update last updated date
CURRENT_DATE=$(date +"%B %Y")
find "$WIKI_DIR" -name "*.md" -type f -exec sed -i \
    "s/Last Updated: [A-Za-z]* [0-9]*/Last Updated: $CURRENT_DATE/g" {} \;

# Update JavaDoc links (if version in URL)
OLD_VERSION_PATTERN="[0-9]\+\.[0-9]\+\.[0-9]\+"
find "$WIKI_DIR" -name "*.md" -type f -exec sed -i \
    "s|javadoc/$OLD_VERSION_PATTERN|javadoc/$NEW_VERSION|g" {} \;

echo "✅ Wiki updated to version $NEW_VERSION"
echo "   Last Updated: $CURRENT_DATE"
echo ""
echo "Next steps:"
echo "1. Review changes: git diff wiki/"
echo "2. Test examples with new version"
echo "3. Commit: git add wiki/ && git commit -m 'docs: update wiki to version $NEW_VERSION'"
echo "4. Sync to wiki repository"
```

**Usage:**
```bash
chmod +x scripts/update-wiki-version.sh
./scripts/update-wiki-version.sh 1.1.0
```

## Quality Assurance

### Pre-Publish Checks

Before publishing wiki updates:

1. **Content Quality:**
   - [ ] Spell check all modified pages
   - [ ] Grammar check
   - [ ] Technical accuracy verification
   - [ ] Consistency with other pages

2. **Code Examples:**
   - [ ] All code examples compile
   - [ ] Examples use latest API
   - [ ] Import statements included where needed
   - [ ] Examples follow project coding standards

3. **Links:**
   - [ ] All internal wiki links work
   - [ ] All external links are valid (not 404)
   - [ ] GitHub repository links point to correct branch/tag
   - [ ] JavaDoc links point to correct version

4. **Formatting:**
   - [ ] Markdown renders correctly
   - [ ] Code blocks have proper syntax highlighting
   - [ ] Tables display correctly
   - [ ] Lists are properly formatted
   - [ ] Headings follow hierarchy

5. **Navigation:**
   - [ ] New pages added to sidebar
   - [ ] "See Also" sections updated
   - [ ] Table of contents accurate

### Automated Checks

Consider using these tools:

**Link Checker:**
```bash
# Check for broken links
npm install -g markdown-link-check
find wiki -name "*.md" -exec markdown-link-check {} \;
```

**Spell Checker:**
```bash
# Install aspell
sudo apt-get install aspell

# Check spelling
find wiki -name "*.md" -exec aspell check {} \;
```

**Markdown Linter:**
```bash
# Install markdownlint
npm install -g markdownlint-cli

# Lint markdown files
markdownlint wiki/*.md
```

## Review Process

### Self-Review Checklist

Before submitting changes:

- [ ] Changes address the stated goal
- [ ] No unintended modifications
- [ ] Consistent with project style
- [ ] All acceptance criteria met
- [ ] Examples tested and working

### Peer Review

For significant updates:

1. Create PR with wiki changes in main repository
2. Request review from at least one maintainer
3. Address all feedback
4. Get approval before merging
5. Sync to wiki repository after merge

### Review Guidelines for Reviewers

Focus on:
- **Accuracy:** Technical information is correct
- **Clarity:** Easy to understand for target audience
- **Completeness:** No important information missing
- **Consistency:** Matches other documentation
- **Examples:** Code examples work and are helpful

## Maintenance Schedule

### Regular Reviews

| Frequency | Tasks |
|-----------|-------|
| **Weekly** | Monitor for user questions indicating doc gaps |
| **Monthly** | Review most-visited pages for improvements |
| **Quarterly** | Comprehensive review of all pages |
| **Per Release** | Full update cycle (see checklist above) |

### Continuous Monitoring

Set up monitoring for:
- GitHub Issues labeled "documentation"
- GitHub Discussions in Q&A category
- Stack Overflow questions tagged "query4j"
- User feedback in other channels

## Metrics and Analytics

### Track These Metrics

1. **Page Views:**
   - Most visited pages
   - Least visited pages (may need promotion)
   - Traffic trends over time

2. **User Engagement:**
   - Time spent on pages
   - Bounce rate
   - Navigation patterns

3. **Feedback:**
   - GitHub Issues related to documentation
   - Discussions asking for clarification
   - External feedback

### Using Insights

Based on metrics:
- Improve high-traffic pages first
- Promote useful but under-visited pages
- Address documentation gaps causing questions
- Restructure if navigation patterns suggest confusion

## Communication

### Announcing Wiki Updates

When making significant wiki updates:

1. **GitHub Release Notes:**
   ```markdown
   ## Documentation
   - Updated Wiki with comprehensive guide for Feature X
   - Added troubleshooting section for common Issue Y
   - Updated benchmarks with latest results
   ```

2. **GitHub Discussions:**
   - Post in Announcements category
   - Highlight important changes
   - Link to updated pages

3. **README Update:**
   - Add link to new wiki pages in main README
   - Update documentation section

## Backup and Version Control

### Wiki Backup Strategy

The wiki is in a Git repository, so it's version controlled. Additionally:

1. **Periodic Backups:**
   ```bash
   # Backup script
   #!/bin/bash
   DATE=$(date +%Y%m%d)
   git clone https://github.com/query4j/dynamicquerybuilder.wiki.git \
       wiki-backup-$DATE
   tar -czf wiki-backup-$DATE.tar.gz wiki-backup-$DATE
   # Upload to backup storage
   ```

2. **Main Repository Sync:**
   - Keep copy in main repository `/wiki` directory
   - Provides additional backup
   - Enables PR-based workflow

### Recovery Procedure

If wiki is accidentally corrupted:

1. **Revert Recent Changes:**
   ```bash
   cd dynamicquerybuilder.wiki
   git log
   git revert <commit-hash>
   git push
   ```

2. **Restore from Main Repository:**
   ```bash
   cd dynamicquerybuilder.wiki
   cp ../dynamicquerybuilder/wiki/*.md .
   git add .
   git commit -m "docs: restore wiki from main repository"
   git push origin master --force
   ```

## Troubleshooting Common Issues

### Wiki Not Updating

**Problem:** Changes pushed but not showing on GitHub.

**Solutions:**
- Clear browser cache
- Wait a few minutes (GitHub caching)
- Verify push went to correct branch (master)
- Check GitHub Wiki is enabled in repository settings

### Broken Links After Update

**Problem:** Internal links broken after renaming pages.

**Solutions:**
- Use find-and-replace to update all references
- Keep page names stable (avoid renames)
- Redirect old page names if necessary

### Formatting Issues

**Problem:** Markdown not rendering correctly.

**Solutions:**
- Test locally with markdown previewer
- Check for special characters needing escaping
- Verify code block language tags
- Ensure list indentation is correct

## Contact

For questions about wiki maintenance:
- **GitHub Issues:** [Create an issue](https://github.com/query4j/dynamicquerybuilder/issues)
- **GitHub Discussions:** [Start discussion](https://github.com/query4j/dynamicquerybuilder/discussions)
- **Maintainers:** @query4j-maintainers

---

**Last Updated:** December 2024  
**Maintained by:** Query4j Development Team
