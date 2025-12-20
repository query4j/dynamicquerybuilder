# Wiki Publishing Guide

This guide explains how the Query4j GitHub Wiki is published and maintained.

## Overview

The Query4j project maintains comprehensive documentation in the GitHub Wiki. All wiki content is stored in the `/wiki` directory of the main repository and automatically published to GitHub Wiki.

## Automatic Publishing

### GitHub Actions Workflow

Wiki content is automatically published when:

1. **Changes are pushed to master branch** that affect files in the `/wiki` directory
2. **Manual trigger** via GitHub Actions workflow dispatch

The workflow (`.github/workflows/publish-wiki.yml`) automatically:
- Checks out both the main repository and wiki repository
- Copies all `.md` files from `/wiki` to the wiki repository
- Commits and pushes changes to the wiki
- Provides a summary of published pages

### Workflow Configuration

```yaml
# Triggers
on:
  push:
    branches:
      - master
    paths:
      - 'wiki/**'
  workflow_dispatch:
```

The workflow requires:
- `contents: write` permission to push to the wiki repository
- GitHub token (automatically provided by GitHub Actions)

## Manual Publishing

### Using the Script

For manual wiki publishing, use the provided script:

```bash
./publish-wiki.sh
```

The script:
1. Clones or updates the wiki repository
2. Copies all wiki files from `/wiki` directory
3. Shows changes and prompts for confirmation
4. Commits and pushes to GitHub Wiki

### Manual Steps

If you prefer manual control:

1. **Clone the wiki repository:**
   ```bash
   git clone https://github.com/query4j/dynamicquerybuilder.wiki.git
   cd dynamicquerybuilder.wiki
   ```

2. **Copy wiki files:**
   ```bash
   cp ../wiki/*.md .
   ```

3. **Commit and push:**
   ```bash
   git add *.md
   git commit -m "Update wiki content"
   git push origin master
   ```

## Wiki Structure

The wiki consists of the following pages:

### Core Documentation Pages

| File | Wiki Page | Description |
|------|-----------|-------------|
| `Home.md` | Home | Project vision, philosophy, and navigation |
| `Getting-Started.md` | Getting Started | Setup and installation tutorial |
| `Core-Module.md` | Core Module | QueryBuilder API and predicates |
| `Cache-Manager.md` | Cache Manager | Caching strategies and configuration |
| `Optimizer.md` | Optimizer | Query optimization and analysis |
| `Configuration.md` | Configuration | Configuration options and best practices |
| `API-Reference.md` | API Reference | Complete API documentation |
| `Error-Handling.md` | Error Handling | Exception catalog and troubleshooting |
| `Benchmarking.md` | Benchmarking | Performance benchmarks and results |
| `FAQ-and-Troubleshooting.md` | FAQ and Troubleshooting | Common questions and solutions |
| `Contributing.md` | Contributing | Contribution guidelines |
| `Release-Notes.md` | Release Notes | Version history and changelog |

### Navigation Pages

| File | Purpose |
|------|---------|
| `_Sidebar.md` | Sidebar navigation menu |
| `_Footer.md` | Footer with version and links |

### Maintenance Pages

| File | Purpose |
|------|---------|
| `README.md` | Wiki overview and structure |
| `SETUP.md` | Quick setup guide |
| `MAINTENANCE.md` | Maintenance workflow |
| `SUMMARY.md` | Complete package summary |

## Updating the Wiki

### Making Changes

1. **Edit files in the `/wiki` directory** of the main repository
2. **Create a pull request** with your changes
3. **After merge to master**, the wiki is automatically published

### Best Practices

**For content updates:**
- Edit files in `/wiki` directory, not directly in wiki repository
- Use pull requests for review before publishing
- Keep wiki content in sync with code changes
- Update version numbers and dates when making changes

**For formatting:**
- Use GitHub-flavored Markdown
- Internal wiki links: `[Page Title](Page-Title)` (no `.md` extension)
- External links: `[Link Text](https://url)`
- Test markdown rendering before committing

### Maintenance Checklist

When updating the wiki:

- [ ] Update content in `/wiki` directory
- [ ] Update "Last Updated" dates
- [ ] Update version numbers if applicable
- [ ] Test all internal links
- [ ] Verify code examples are correct
- [ ] Check markdown formatting
- [ ] Create PR and get review
- [ ] Merge to master (auto-publishes)
- [ ] Verify on GitHub Wiki

## Troubleshooting

### Wiki Not Publishing

**Problem:** Changes pushed but wiki not updated

**Solutions:**
1. Check workflow run in GitHub Actions tab
2. Verify workflow has necessary permissions
3. Check if wiki is enabled in repository settings
4. Manually run workflow via workflow dispatch

### Workflow Fails

**Problem:** GitHub Actions workflow fails

**Common causes and solutions:**
- **Permission denied**: Ensure workflow has `contents: write` permission
- **Wiki not initialized**: Create first wiki page manually via GitHub UI
- **Merge conflicts**: May need to manually resolve in wiki repository

### Links Not Working

**Problem:** Internal wiki links broken

**Solution:** GitHub wiki links don't use `.md` extension:
```markdown
❌ [Getting Started](Getting-Started.md)
✅ [Getting Started](Getting-Started)
```

### Pages Not Displaying

**Problem:** Wiki page exists but content doesn't show

**Solutions:**
- Verify file name exactly matches (case-sensitive)
- Check markdown syntax is valid
- Clear browser cache
- Wait a few minutes for GitHub caching

## Monitoring

### Verify Publication

After publishing, verify:

1. **Visit the wiki**: https://github.com/query4j/dynamicquerybuilder/wiki
2. **Check Home page** loads correctly
3. **Test sidebar navigation** appears and works
4. **Click through several pages** to verify content
5. **Test internal links** navigate correctly
6. **Verify footer** displays on pages

### GitHub Actions

Monitor workflow runs:
- Go to: https://github.com/query4j/dynamicquerybuilder/actions
- Select "Publish Wiki" workflow
- Review recent runs for success/failure
- Check workflow summary for published pages list

## Wiki Access and Permissions

### Who Can Edit

By default:
- **Public repos**: Anyone can edit wiki directly
- **Private repos**: Only collaborators can edit

**Security recommendation:** 
- Restrict wiki editing to collaborators only
- Use the automated workflow for publishing
- Review changes via pull requests before publishing

### Restricting Access

To restrict wiki editing:
1. Go to repository Settings
2. Navigate to "Features" section
3. Find "Wikis" and click "Restrict editing to collaborators only"

## Advanced Topics

### Automated Version Updates

The wiki can be programmatically updated for version bumps. Example script:

```bash
#!/bin/bash
NEW_VERSION="$1"
WIKI_DIR="./wiki"

# Update version in all wiki files
find "$WIKI_DIR" -name "*.md" -type f -exec sed -i \
    "s/Version: [0-9]\+\.[0-9]\+\.[0-9]\+/Version: $NEW_VERSION/g" {} \;

# Update last updated date
CURRENT_DATE=$(date +"%B %Y")
find "$WIKI_DIR" -name "*.md" -type f -exec sed -i \
    "s/Last Updated: .*/Last Updated: $CURRENT_DATE/g" {} \;
```

### CI/CD Integration

The wiki publishing workflow integrates with CI/CD:
- Runs on every merge to master that touches wiki files
- Can be triggered manually for immediate updates
- Provides summary in workflow output
- Links to all published pages

### Sync with Repository Docs

To keep wiki in sync with repository documentation:
- Store source of truth in `/wiki` directory
- Use workflow automation for publishing
- Include wiki updates in release checklist
- Cross-reference between wiki and repo docs

## Support

For wiki publishing issues:

- **GitHub Actions**: Check workflow runs in Actions tab
- **GitHub Issues**: [Report a problem](https://github.com/query4j/dynamicquerybuilder/issues)
- **Documentation**: See [wiki/README.md](../wiki/README.md) and [wiki/SETUP.md](../wiki/SETUP.md)

---

**Last Updated:** December 2025  
**Version:** 1.0.0
