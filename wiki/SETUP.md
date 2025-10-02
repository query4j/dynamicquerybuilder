# GitHub Wiki Setup Guide

Quick guide to publish the Query4j Wiki to GitHub.

## Prerequisites

- Repository admin access
- Git installed locally
- Wiki content ready (all .md files in `/wiki` directory)

## Setup Steps

### Step 1: Enable GitHub Wiki

1. Go to https://github.com/query4j/dynamicquerybuilder/settings
2. Scroll to "Features" section
3. Check the "Wikis" checkbox
4. Save changes

The wiki will now be accessible at: https://github.com/query4j/dynamicquerybuilder/wiki

### Step 2: Clone Wiki Repository

GitHub Wiki is a separate Git repository:

```bash
# Clone the wiki repository (will be empty initially)
git clone https://github.com/query4j/dynamicquerybuilder.wiki.git

# Navigate to wiki directory
cd dynamicquerybuilder.wiki
```

### Step 3: Copy Wiki Content

```bash
# Copy all wiki files from main repository
cp ../dynamicquerybuilder/wiki/*.md .

# Verify files copied
ls -la *.md
```

Expected files:
- Home.md
- Getting-Started.md
- Core-Module.md
- Cache-Manager.md
- Optimizer.md
- Configuration.md
- API-Reference.md
- Error-Handling.md
- Benchmarking.md
- FAQ-and-Troubleshooting.md
- Contributing.md
- Release-Notes.md
- _Sidebar.md
- _Footer.md
- README.md (optional - for wiki repo itself)
- MAINTENANCE.md (optional - for maintainers)

### Step 4: Commit and Push

```bash
# Stage all files
git add .

# Check what will be committed
git status

# Commit with descriptive message
git commit -m "Initial wiki setup: comprehensive documentation for Query4j v1.0.0

- Home page with project vision and roadmap
- Getting Started tutorial
- Complete module documentation (Core, Cache, Optimizer)
- Configuration guide
- API Reference
- Error handling and troubleshooting
- Performance benchmarks
- FAQ
- Contributing guidelines
- Release notes
- Navigation (sidebar and footer)"

# Push to GitHub
git push origin master
```

### Step 5: Verify Publication

1. Visit https://github.com/query4j/dynamicquerybuilder/wiki
2. Verify Home page displays correctly
3. Check sidebar navigation appears
4. Click through several pages to verify content
5. Test some internal links
6. Verify footer displays on pages

### Step 6: Set Home Page (if needed)

If Home.md doesn't display as the wiki home:

1. Go to wiki
2. Click "Home" in sidebar
3. Click "Edit" button
4. Click "Save Page"

This sets it as the wiki home page.

## Quick Verification Checklist

After setup, verify:

- [ ] Home page loads at wiki URL
- [ ] Sidebar navigation visible on all pages
- [ ] Footer visible on all pages
- [ ] All 12 main pages accessible
- [ ] Internal wiki links work (e.g., [Core Module](Core-Module))
- [ ] External links work (e.g., GitHub repo links)
- [ ] Code examples display correctly
- [ ] Tables render properly
- [ ] Lists and formatting look good

## One-Command Setup (Alternative)

Create a setup script:

```bash
#!/bin/bash
# setup-wiki.sh

set -e

echo "Setting up Query4j Wiki..."

# Clone wiki repository
if [ -d "dynamicquerybuilder.wiki" ]; then
    echo "Wiki directory exists, pulling latest..."
    cd dynamicquerybuilder.wiki
    git pull
    cd ..
else
    echo "Cloning wiki repository..."
    git clone https://github.com/query4j/dynamicquerybuilder.wiki.git
fi

# Copy content
echo "Copying wiki content..."
cp wiki/*.md dynamicquerybuilder.wiki/

# Commit and push
cd dynamicquerybuilder.wiki
git add .

if git diff-index --quiet HEAD --; then
    echo "No changes to commit"
else
    git commit -m "Update wiki content from main repository"
    git push origin master
    echo "✅ Wiki updated successfully!"
fi

cd ..

echo ""
echo "Wiki URL: https://github.com/query4j/dynamicquerybuilder/wiki"
echo ""
echo "Verify the wiki is accessible and all pages display correctly."
```

**Usage:**
```bash
chmod +x setup-wiki.sh
./setup-wiki.sh
```

## Updating the Wiki

### For Small Changes

```bash
cd dynamicquerybuilder.wiki
vim Page-Name.md      # Edit directly
git add Page-Name.md
git commit -m "docs: update Page Name - [brief description]"
git push origin master
```

### For Major Updates

Follow the workflow in [MAINTENANCE.md](MAINTENANCE.md):

1. Edit in main repository `/wiki` directory
2. Create PR for review
3. After merge, sync to wiki repository

## Troubleshooting Setup

### Wiki Not Enabled

**Error:** Can't access wiki URL

**Solution:** 
- Check repository settings
- Ensure "Wikis" feature is enabled
- You may need admin access

### Clone Fails

**Error:** `Repository not found`

**Solution:**
- Wiki must be enabled first (Step 1)
- Create at least one page via GitHub UI to initialize wiki
- Then clone will work

### Push Rejected

**Error:** `Updates were rejected`

**Solution:**
```bash
# Pull first
git pull origin master

# Resolve any conflicts
# Then push again
git push origin master
```

### Pages Not Displaying

**Issue:** Wiki pages exist but don't show content

**Solution:**
- Verify file names match exactly (case-sensitive)
- Check markdown syntax
- Clear browser cache
- Wait a few minutes for GitHub caching

### Sidebar Not Showing

**Issue:** Sidebar navigation doesn't appear

**Solution:**
- Verify file is named `_Sidebar.md` (with underscore)
- Ensure it's committed and pushed
- Clear browser cache

## Wiki Access Permissions

### Who Can Edit

By default:
- **Public repos:** Anyone can edit wiki
- **Private repos:** Repository collaborators only

To restrict wiki editing:
1. Go to repository Settings
2. Under "Features" → "Wikis"
3. Click "Restrict editing to collaborators only"

## Next Steps

After setup:

1. **Announce the wiki:**
   - Add link in repository README
   - Mention in next release notes
   - Post in GitHub Discussions

2. **Monitor usage:**
   - Watch for questions about wiki content
   - Track page views (if analytics enabled)
   - Gather user feedback

3. **Establish maintenance:**
   - Follow [MAINTENANCE.md](MAINTENANCE.md) workflow
   - Update with each release
   - Review quarterly for improvements

## Support

Need help with setup?
- **Issues:** https://github.com/query4j/dynamicquerybuilder/issues
- **Discussions:** https://github.com/query4j/dynamicquerybuilder/discussions

---

**Setup Time:** ~10 minutes  
**Maintenance:** Per [MAINTENANCE.md](MAINTENANCE.md)  
**Last Updated:** December 2024
