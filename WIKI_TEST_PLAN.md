# Wiki Publishing Test Plan

This document outlines the steps to test and verify the wiki publishing functionality.

## Prerequisites

Before testing, ensure:

1. **Wiki is enabled** in repository settings:
   - Go to https://github.com/query4j/dynamicquerybuilder/settings
   - Scroll to "Features" section
   - Verify "Wikis" is checked

2. **Wiki repository is initialized**:
   - Visit https://github.com/query4j/dynamicquerybuilder/wiki
   - If it doesn't exist, create the first page manually via GitHub UI
   - This initializes the wiki Git repository

3. **Permissions are set**:
   - GitHub Actions has `contents: write` permission (already set in workflow)
   - GITHUB_TOKEN has access to wiki repository

## Test Scenarios

### Test 1: Automatic Publishing via GitHub Actions

**Trigger:** Push to master branch with wiki file changes

**Steps:**
1. Merge this PR to master branch
2. Check GitHub Actions workflow runs:
   - Go to https://github.com/query4j/dynamicquerybuilder/actions
   - Look for "Publish Wiki" workflow
   - Verify it runs successfully

**Expected Results:**
- Workflow completes successfully
- All wiki files are copied to wiki repository
- Wiki pages are accessible at https://github.com/query4j/dynamicquerybuilder/wiki
- Workflow summary shows list of published pages

**Verification:**
```bash
# Check workflow logs
# Visit: https://github.com/query4j/dynamicquerybuilder/actions/workflows/publish-wiki.yml

# Verify pages on wiki
# Visit each page:
# - https://github.com/query4j/dynamicquerybuilder/wiki/Home
# - https://github.com/query4j/dynamicquerybuilder/wiki/Getting-Started
# - https://github.com/query4j/dynamicquerybuilder/wiki/Core-Module
# etc.
```

---

### Test 2: Manual Publishing via Script

**Steps:**
1. Clone the repository
2. Run the manual publishing script:
   ```bash
   cd dynamicquerybuilder
   ./publish-wiki.sh
   ```
3. Confirm the prompt to push changes

**Expected Results:**
- Script clones/updates wiki repository
- Copies all wiki files
- Shows changes
- Prompts for confirmation
- Commits and pushes successfully
- Shows success message with wiki URL

**Verification:**
```bash
# Check if wiki files were copied
ls -la ../dynamicquerybuilder.wiki/*.md

# Check git status in wiki repo
cd ../dynamicquerybuilder.wiki
git log -1
```

---

### Test 3: Manual Workflow Trigger

**Trigger:** Manual workflow dispatch

**Steps:**
1. Go to https://github.com/query4j/dynamicquerybuilder/actions/workflows/publish-wiki.yml
2. Click "Run workflow" button
3. Select branch (master)
4. Click "Run workflow"

**Expected Results:**
- Workflow runs successfully
- Wiki is updated with latest content
- Workflow summary displays

**Verification:**
- Check workflow run logs
- Verify wiki pages are updated
- Check commit in wiki repository

---

### Test 4: Wiki File Update

**Trigger:** Update a wiki file and push to master

**Steps:**
1. Edit a file in `/wiki` directory (e.g., `wiki/Home.md`)
2. Make a small change (add a line, update date)
3. Commit and push to master:
   ```bash
   git add wiki/Home.md
   git commit -m "docs: update wiki home page"
   git push origin master
   ```

**Expected Results:**
- Workflow triggers automatically
- Only changed files are updated in wiki
- Wiki page reflects the changes

**Verification:**
```bash
# Check workflow was triggered
# Visit: https://github.com/query4j/dynamicquerybuilder/actions

# Verify change on wiki
# Visit: https://github.com/query4j/dynamicquerybuilder/wiki/Home
```

---

### Test 5: Wiki Navigation

**Steps:**
1. Visit https://github.com/query4j/dynamicquerybuilder/wiki
2. Check sidebar navigation appears
3. Click through various pages
4. Test internal links
5. Check footer displays

**Expected Results:**
- Sidebar navigation displays on all pages
- All internal links work (format: `[Page](Page-Title)`)
- Footer displays on all pages
- Code examples render correctly
- Tables format properly

**Verification Checklist:**
- [ ] Home page loads
- [ ] Sidebar visible on all pages
- [ ] Footer visible on all pages
- [ ] All 12 main pages accessible
- [ ] Internal links work (e.g., from Home to Getting Started)
- [ ] External links work (e.g., GitHub repo links)
- [ ] Code blocks render correctly
- [ ] Tables display properly
- [ ] Lists format correctly

---

### Test 6: Wiki Search and Discovery

**Steps:**
1. Use GitHub's wiki search feature
2. Search for specific terms (e.g., "QueryBuilder", "cache", "optimizer")
3. Verify search results are relevant

**Expected Results:**
- Search functionality works
- Results show relevant pages
- Can navigate to pages from search results

---

## Validation Checklist

After initial publishing, verify all aspects:

### Content Verification

- [ ] All 18 wiki files are published
- [ ] Home page is set as default wiki page
- [ ] Getting Started tutorial is complete
- [ ] API Reference is comprehensive
- [ ] All code examples are present
- [ ] All tables render correctly
- [ ] All lists format properly

### Navigation Verification

- [ ] Sidebar appears on all pages
- [ ] Sidebar links work
- [ ] Footer appears on all pages
- [ ] Footer links work
- [ ] Internal wiki links work (no .md extension)
- [ ] External links work
- [ ] Table of contents links work

### Technical Verification

- [ ] Workflow runs successfully
- [ ] Workflow has correct permissions
- [ ] Workflow triggers on wiki file changes
- [ ] Manual trigger works
- [ ] Script executes without errors
- [ ] Wiki repository receives commits
- [ ] Commit messages are descriptive

### Documentation Verification

- [ ] README links to wiki
- [ ] Wiki publishing guide is complete
- [ ] SETUP.md has correct instructions
- [ ] MAINTENANCE.md workflow is clear
- [ ] Script usage is documented

---

## Troubleshooting Common Issues

### Issue: Workflow Permission Denied

**Symptoms:**
- Workflow fails with permission error
- Cannot push to wiki repository

**Solution:**
```yaml
# Verify in .github/workflows/publish-wiki.yml
permissions:
  contents: write
```

**Alternative:**
- Check repository settings allow GitHub Actions to create/edit wiki

---

### Issue: Wiki Not Initialized

**Symptoms:**
- Cannot clone wiki repository
- Error: "Repository not found"

**Solution:**
1. Go to https://github.com/query4j/dynamicquerybuilder/wiki
2. Click "Create the first page"
3. Add any content and save
4. Delete the page if not needed
5. Wiki repository is now initialized

---

### Issue: Workflow Doesn't Trigger

**Symptoms:**
- Push to master doesn't trigger workflow
- Workflow doesn't run

**Solution:**
1. Check workflow trigger paths:
   ```yaml
   on:
     push:
       branches:
         - master
       paths:
         - 'wiki/**'
   ```
2. Ensure changes are in `/wiki` directory
3. Check branch name is correct (master vs main)

---

### Issue: Wiki Links Broken

**Symptoms:**
- Clicking internal links shows 404
- Links don't navigate properly

**Solution:**
- GitHub wiki links don't use `.md` extension
- Change `[Page](Page.md)` to `[Page](Page)`
- Update all wiki files to use correct format

---

## Post-Publishing Steps

After successful initial publishing:

1. **Update README badges** (if applicable):
   ```markdown
   ![Wiki](https://img.shields.io/badge/wiki-available-brightgreen)
   ```

2. **Announce wiki availability:**
   - Add to release notes
   - Post in GitHub Discussions
   - Update project documentation

3. **Monitor usage:**
   - Check wiki page views (if analytics available)
   - Watch for user questions
   - Gather feedback

4. **Establish maintenance:**
   - Follow maintenance workflow in wiki/MAINTENANCE.md
   - Update with each release
   - Review quarterly

---

## Success Criteria

Publishing is successful when:

✅ All wiki files are accessible on GitHub Wiki  
✅ Navigation (sidebar and footer) works correctly  
✅ All internal links navigate properly  
✅ Code examples render with syntax highlighting  
✅ Tables and lists format correctly  
✅ Search functionality works  
✅ Workflow runs successfully on file changes  
✅ Manual script publishes without errors  
✅ Documentation is complete and accurate

---

## Additional Resources

- **Wiki URL**: https://github.com/query4j/dynamicquerybuilder/wiki
- **Workflow**: .github/workflows/publish-wiki.yml
- **Script**: publish-wiki.sh
- **Documentation**: docs/WIKI_PUBLISHING.md
- **Maintenance**: wiki/MAINTENANCE.md

---

**Test Plan Version:** 1.0  
**Last Updated:** December 2025
