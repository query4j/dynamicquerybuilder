# Query4j GitHub Wiki Content

This directory contains the complete content for the Query4j GitHub Wiki. The wiki serves as the central, living repository for architecture explanations, API guides, advanced usage, troubleshooting, benchmarking, and contribution practices.

## Overview

The Query4j Wiki is structured to provide comprehensive documentation for all levels of users—from getting started guides to advanced optimization techniques and contribution workflows.

## Wiki Structure

### Core Pages

| Page | Description | Status |
|------|-------------|--------|
| [Home.md](Home.md) | Project vision, philosophy, roadmap, and quick navigation | ✅ Complete |
| [Getting-Started.md](Getting-Started.md) | Setup, installation, and first query tutorial | ✅ Complete |
| [Core-Module.md](Core-Module.md) | QueryBuilder API, predicates, JOINs, aggregations | ✅ Complete |
| [Cache-Manager.md](Cache-Manager.md) | Caching strategies, configuration, performance tuning | ✅ Complete |
| [Optimizer.md](Optimizer.md) | Query optimization, index suggestions, analysis | ✅ Complete |
| [Configuration.md](Configuration.md) | Configuration options, sources, best practices | ✅ Complete |
| [API-Reference.md](API-Reference.md) | Complete API documentation for all modules | ✅ Complete |
| [Error-Handling.md](Error-Handling.md) | Exception catalog, troubleshooting steps | ✅ Complete |
| [Benchmarking.md](Benchmarking.md) | Performance benchmarks, JMH results, analysis | ✅ Complete |
| [FAQ-and-Troubleshooting.md](FAQ-and-Troubleshooting.md) | Common questions and solutions | ✅ Complete |
| [Contributing.md](Contributing.md) | Contribution guidelines, workflow, standards | ✅ Complete |
| [Release-Notes.md](Release-Notes.md) | Version history, changelog, migration guides | ✅ Complete |

### Navigation Pages

| Page | Purpose | Status |
|------|---------|--------|
| [_Sidebar.md](_Sidebar.md) | Sidebar navigation for all wiki pages | ✅ Complete |
| [_Footer.md](_Footer.md) | Footer with version info and links | ✅ Complete |

## Setting Up the Wiki

### Initial Setup

1. **Enable GitHub Wiki** for the repository:
   - Go to repository Settings
   - Scroll to "Features" section
   - Check "Wikis"

2. **Clone the Wiki Repository**:
   ```bash
   # GitHub Wiki is a separate Git repository
   git clone https://github.com/query4j/dynamicquerybuilder.wiki.git
   ```

3. **Copy Wiki Content**:
   ```bash
   # Copy all .md files from this directory to the wiki repository
   cp wiki/*.md dynamicquerybuilder.wiki/
   cd dynamicquerybuilder.wiki
   ```

4. **Commit and Push**:
   ```bash
   git add .
   git commit -m "Initial wiki setup with comprehensive documentation"
   git push origin master
   ```

5. **Verify**:
   - Visit: https://github.com/query4j/dynamicquerybuilder/wiki
   - Confirm all pages are visible and navigation works

### Alternative: Manual Upload

If you prefer manual setup:

1. Go to the Wiki tab in the repository
2. Create each page manually:
   - Click "Create the first page" or "New Page"
   - Copy content from corresponding .md file
   - Save with same title (without .md extension)

3. Set up sidebar:
   - Create a page named "_Sidebar"
   - Copy content from _Sidebar.md

4. Set up footer:
   - Create a page named "_Footer"
   - Copy content from _Footer.md

## Wiki Maintenance Workflow

### Regular Updates

Update the wiki when:
- **New Features**: Document new APIs, modules, or capabilities
- **API Changes**: Update affected pages and add migration notes
- **Bug Fixes**: Update troubleshooting guides if relevant
- **Performance Changes**: Update benchmarks and performance recommendations
- **Configuration Changes**: Update Configuration page
- **Version Releases**: Update Release Notes

### Maintenance Checklist

Use this checklist for each release:

- [ ] Review all pages for accuracy
- [ ] Update version numbers in footers
- [ ] Update "Last Updated" dates
- [ ] Add new features to Home page roadmap
- [ ] Update Release Notes with changelog
- [ ] Review and update code examples
- [ ] Verify all internal links work
- [ ] Verify all external links work
- [ ] Check for outdated information
- [ ] Update screenshots if UI changed

### Update Process

1. **Edit Locally**:
   ```bash
   cd dynamicquerybuilder.wiki
   # Edit the .md files
   ```

2. **Review Changes**:
   ```bash
   git diff
   ```

3. **Commit and Push**:
   ```bash
   git add .
   git commit -m "docs: update [page name] for version X.Y.Z"
   git push origin master
   ```

4. **Verify on GitHub**:
   - Check https://github.com/query4j/dynamicquerybuilder/wiki
   - Verify changes appear correctly

### Cross-Reference Maintenance

When updating documentation, maintain consistency across:

1. **Wiki Pages** (this directory)
2. **Repository README.md**
3. **Module READMEs** (core/, cache/, optimizer/)
4. **JavaDoc** in source code
5. **Tutorial Files** (QUICKSTART.md, ADVANCED.md)
6. **API_GUIDE.md** in docs/

### Style Guidelines

**Consistency:**
- Use consistent terminology across all pages
- Follow the same formatting style
- Use similar section structures

**Clarity:**
- Write for the target audience (beginners to advanced)
- Use clear, actionable language
- Include code examples for complex concepts
- Provide links to related topics

**Completeness:**
- Include all necessary information
- Link to source code for implementation details
- Provide both simple and complex examples
- Include troubleshooting for common issues

**Maintenance:**
- Keep version numbers up to date
- Update "Last Updated" dates when changing content
- Remove or update deprecated features
- Add migration guides for breaking changes

## Content Standards

### Page Structure

Each wiki page should follow this structure:

```markdown
# Page Title

Brief introduction paragraph.

## Table of Contents

1. [Section 1](#section-1)
2. [Section 2](#section-2)
...

---

## Section 1

Content...

### Subsection

Content...

---

## See Also

- [Related Page 1](Related-Page-1)
- [External Resource](https://...)

---

**Last Updated:** Month Year  
**Version:** X.Y.Z
```

### Code Example Standards

**Java Code:**
```java
// ✅ Good - clear, complete example
import com.github.query4j.core.QueryBuilder;

public class Example {
    public void demonstrateFeature() {
        QueryBuilder<User> query = QueryBuilder.forEntity(User.class)
            .where("active", true)
            .orderBy("lastName");
        
        String sql = query.toSQL();
        // Output: SELECT * FROM User WHERE active = :p1 ORDER BY lastName
    }
}
```

**Configuration Examples:**
```yaml
# YAML with comments explaining each option
query4j:
  cache:
    enabled: true        # Enable result caching
    maxSize: 1000        # Maximum 1000 cached entries
    strategy: LRU        # Least Recently Used eviction
```

### Link Format

**Internal Wiki Links:**
```markdown
[Page Title](Page-Title)
```

**External Links:**
```markdown
[Link Text](https://full-url.com)
```

**Repository Links:**
```markdown
[File](https://github.com/query4j/dynamicquerybuilder/blob/master/path/to/file)
```

## Troubleshooting Wiki Issues

### Links Not Working

**Problem:** Internal wiki links show as broken.

**Solution:** GitHub wiki links don't use `.md` extension:
```markdown
❌ [Getting Started](Getting-Started.md)
✅ [Getting Started](Getting-Started)
```

### Sidebar Not Showing

**Problem:** Sidebar doesn't appear on wiki pages.

**Solution:** Ensure `_Sidebar.md` (with underscore) exists and is committed to wiki repository.

### Images Not Displaying

**Problem:** Images in wiki pages don't show.

**Solution:** 
1. Upload images to wiki repository
2. Reference with relative path: `![Alt text](images/screenshot.png)`
3. Or use absolute GitHub URLs

### Formatting Issues

**Problem:** Markdown not rendering correctly.

**Solution:**
- Verify markdown syntax (especially indentation for lists and code blocks)
- Test locally with a markdown previewer
- Check for special characters that need escaping

## Automation Opportunities

### Automated Wiki Updates

Consider setting up automation for:

1. **Version Number Updates:**
   - Script to update version numbers across all wiki pages
   - Run before each release

2. **Link Validation:**
   - Regular checks for broken internal/external links
   - Can integrate with CI/CD

3. **Sync with Repository Docs:**
   - Automatically sync certain sections from repository docs
   - Keep wiki in sync with main branch

### Example Update Script

```bash
#!/bin/bash
# update-wiki-version.sh

NEW_VERSION="$1"
WIKI_DIR="./dynamicquerybuilder.wiki"

if [ -z "$NEW_VERSION" ]; then
    echo "Usage: $0 <version>"
    exit 1
fi

cd "$WIKI_DIR"

# Update version in all .md files
find . -name "*.md" -type f -exec sed -i \
    "s/Version: [0-9]\+\.[0-9]\+\.[0-9]\+/Version: $NEW_VERSION/g" {} \;

# Update last updated date
CURRENT_DATE=$(date +"%B %Y")
find . -name "*.md" -type f -exec sed -i \
    "s/Last Updated: .*/Last Updated: $CURRENT_DATE/g" {} \;

# Commit and push
git add .
git commit -m "docs: update wiki to version $NEW_VERSION"
git push origin master

echo "Wiki updated to version $NEW_VERSION"
```

## Review Process

### Before Publishing Wiki Updates

1. **Spell Check:** Run spell checker on all modified pages
2. **Link Check:** Verify all links work
3. **Code Examples:** Test all code examples compile and run
4. **Formatting:** Preview markdown rendering
5. **Consistency:** Ensure terminology and style match other pages
6. **Accuracy:** Verify technical information is correct
7. **Completeness:** Check no required information is missing

### Peer Review

For significant wiki changes:
1. Create a PR in the main repository with wiki changes in `/wiki` directory
2. Request review from maintainers
3. Address feedback
4. Once approved, sync to wiki repository

## Support

### Questions About Wiki

For questions about wiki content or setup:
- **GitHub Issues:** [Create an issue](https://github.com/query4j/dynamicquerybuilder/issues)
- **GitHub Discussions:** [Start a discussion](https://github.com/query4j/dynamicquerybuilder/discussions)
- **Maintainers:** Tag @query4j-maintainers

### Contributing to Wiki

See [Contributing.md](Contributing.md) for contribution guidelines.

For wiki-specific contributions:
1. Edit files in this `/wiki` directory
2. Submit a PR to the main repository
3. After review and merge, maintainers will sync to GitHub Wiki

---

## Wiki Statistics

- **Total Pages:** 12 core pages + 2 navigation pages
- **Total Content:** ~90,000 words
- **Code Examples:** 150+ examples
- **Coverage:**
  - Getting Started: Complete ✅
  - API Documentation: Complete ✅
  - Troubleshooting: Complete ✅
  - Performance: Complete ✅
  - Contributing: Complete ✅

---

**Maintained by:** Query4j Development Team  
**Last Updated:** October 2025  
**Version:** 1.0.0
