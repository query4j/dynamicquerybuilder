# Initial Wiki Setup Instructions

This document provides step-by-step instructions for the **first-time setup** of the Query4j GitHub Wiki. Follow these instructions **before** merging the wiki publishing automation PR.

## Prerequisites

- Repository admin access
- GitHub Wiki feature available for the repository

## Step-by-Step Setup

### Step 1: Enable GitHub Wiki Feature

1. Navigate to the repository settings:
   ```
   https://github.com/query4j/dynamicquerybuilder/settings
   ```

2. Scroll down to the "Features" section

3. Find the "Wikis" checkbox and **enable it** if not already enabled

4. Click "Save changes" (if button appears)

**Result:** Wiki will be accessible at:
```
https://github.com/query4j/dynamicquerybuilder/wiki
```

### Step 2: Initialize Wiki Repository

The GitHub Wiki is backed by a separate Git repository that must be initialized before the automation can push to it.

**Option A: Via GitHub UI (Recommended for first-time setup)**

1. Visit: https://github.com/query4j/dynamicquerybuilder/wiki

2. Click "Create the first page" button

3. Enter any content (this will be replaced by automation):
   ```markdown
   # Welcome
   
   This wiki is being set up...
   ```

4. Click "Save Page"

**Result:** Wiki Git repository is now initialized and ready for automation.

**Option B: Via Git Command Line**

```bash
# Clone the empty wiki repository
git clone https://github.com/query4j/dynamicquerybuilder.wiki.git
cd dynamicquerybuilder.wiki

# Create initial page
echo "# Welcome" > Home.md
git add Home.md
git commit -m "Initialize wiki"
git push origin master
```

### Step 3: Verify Wiki is Ready

Confirm the wiki is accessible and the repository is initialized:

```bash
# Try cloning the wiki repository
git clone https://github.com/query4j/dynamicquerybuilder.wiki.git test-wiki-clone

# Should succeed with no errors
# Clean up
rm -rf test-wiki-clone
```

**Expected result:** Clone succeeds without "repository not found" error.

### Step 4: Merge the Wiki Publishing PR

Once Steps 1-3 are complete:

1. Review the PR: https://github.com/query4j/dynamicquerybuilder/pull/[PR-NUMBER]

2. Merge to master branch

3. The workflow will automatically:
   - Detect the merge contains changes to `/wiki/**`
   - Run the "Publish Wiki" GitHub Action
   - Sync all wiki files to the wiki repository
   - Publish all pages

### Step 5: Verify Automatic Publishing

After merging the PR:

1. **Check workflow run:**
   - Go to: https://github.com/query4j/dynamicquerybuilder/actions
   - Look for "Publish Wiki" workflow
   - Click on the most recent run
   - Verify it completed successfully

2. **Visit the wiki:**
   - Go to: https://github.com/query4j/dynamicquerybuilder/wiki
   - Verify Home page displays
   - Check sidebar navigation appears
   - Click through a few pages

3. **Verify all pages:**
   - Home: https://github.com/query4j/dynamicquerybuilder/wiki/Home
   - Getting Started: https://github.com/query4j/dynamicquerybuilder/wiki/Getting-Started
   - Core Module: https://github.com/query4j/dynamicquerybuilder/wiki/Core-Module
   - Cache Manager: https://github.com/query4j/dynamicquerybuilder/wiki/Cache-Manager
   - Optimizer: https://github.com/query4j/dynamicquerybuilder/wiki/Optimizer
   - Configuration: https://github.com/query4j/dynamicquerybuilder/wiki/Configuration
   - API Reference: https://github.com/query4j/dynamicquerybuilder/wiki/API-Reference
   - Error Handling: https://github.com/query4j/dynamicquerybuilder/wiki/Error-Handling
   - Benchmarking: https://github.com/query4j/dynamicquerybuilder/wiki/Benchmarking
   - FAQ: https://github.com/query4j/dynamicquerybuilder/wiki/FAQ-and-Troubleshooting
   - Contributing: https://github.com/query4j/dynamicquerybuilder/wiki/Contributing
   - Release Notes: https://github.com/query4j/dynamicquerybuilder/wiki/Release-Notes

## What Happens After Setup

### Automatic Publishing

Once setup is complete, the wiki will automatically update whenever:

- Changes are pushed to the `master` branch that affect files in `/wiki/**`
- The workflow is manually triggered via GitHub Actions UI

### Manual Publishing

You can also manually publish wiki updates using the provided script:

```bash
./publish-wiki.sh
```

## Troubleshooting First-Time Setup

### Issue: Can't Enable Wiki

**Problem:** Wikis checkbox is not available or grayed out

**Possible causes:**
- Repository is a fork (wikis disabled for forks by default)
- Organization policy restricts wikis
- Need admin permissions

**Solution:**
- Verify you have admin access to the repository
- Check organization settings if applicable
- Contact repository owner if needed

### Issue: Can't Clone Wiki Repository

**Problem:** `git clone https://github.com/query4j/dynamicquerybuilder.wiki.git` fails

**Error message:** "Repository not found"

**Solution:**
- Wiki must be enabled first (Step 1)
- Wiki repository must be initialized (Step 2)
- Create at least one page via GitHub UI
- Then try cloning again

### Issue: Workflow Fails on First Run

**Problem:** "Publish Wiki" workflow fails after merging PR

**Common causes:**
- Wiki not enabled
- Wiki repository not initialized
- Permission issues

**Solutions:**

1. **Check wiki is enabled:**
   - Visit repository settings
   - Verify "Wikis" is checked

2. **Check wiki is initialized:**
   - Visit the wiki URL
   - Create a page if needed

3. **Check workflow permissions:**
   - Open `.github/workflows/publish-wiki.yml`
   - Verify `permissions: contents: write` is set

4. **Re-run workflow:**
   - Go to Actions tab
   - Select failed workflow run
   - Click "Re-run all jobs"

## Important Notes

### Wiki Permissions

**Public repositories:**
- By default, anyone can edit wiki pages
- Consider restricting to collaborators:
  1. Go to repository settings
  2. Under Features → Wikis
  3. Check "Restrict editing to collaborators only"

**Private repositories:**
- Only repository collaborators can edit wiki
- No additional configuration needed

### First-Time Publishing

The initial publish will:
- Create 18+ wiki pages
- Set up navigation (sidebar and footer)
- Establish the wiki structure
- Make all documentation immediately available

This is a **one-time setup process**. Future updates are automatic.

### Workflow Behavior

After initial setup:
- Workflow only runs when wiki files change
- Updates are incremental (only changed files)
- Preserves manual edits to wiki (if any)
- Provides summary of published pages

## Verification Checklist

Use this checklist to verify successful setup:

- [ ] Wiki feature is enabled in repository settings
- [ ] Can access wiki URL without errors
- [ ] Wiki repository can be cloned
- [ ] PR has been reviewed and merged
- [ ] GitHub Actions workflow ran successfully
- [ ] Home page displays correctly
- [ ] Sidebar navigation appears on all pages
- [ ] Footer appears on all pages
- [ ] All 12+ main pages are accessible
- [ ] Internal links work (no 404 errors)
- [ ] Code examples render with syntax highlighting
- [ ] Tables format properly

## Next Steps After Setup

Once the wiki is successfully published:

1. **Announce availability:**
   - Update project README (already done in PR)
   - Add to release notes
   - Post in GitHub Discussions

2. **Establish maintenance routine:**
   - Follow workflow in `wiki/MAINTENANCE.md`
   - Update wiki with each release
   - Review content quarterly

3. **Monitor feedback:**
   - Watch for user questions
   - Track page views (if available)
   - Gather feedback for improvements

4. **Keep content updated:**
   - Update with new features
   - Fix errors promptly
   - Improve based on user feedback

## Support

If you encounter issues during setup:

- **Documentation:** See `wiki/SETUP.md` for detailed instructions
- **Test Plan:** See `WIKI_TEST_PLAN.md` for validation steps
- **Issues:** Create a GitHub issue with details
- **Contact:** Tag repository maintainers

---

**Setup Guide Version:** 1.0  
**Last Updated:** December 2025  
**For:** Query4j Dynamic Query Builder v1.0.0
