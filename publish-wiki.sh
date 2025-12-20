#!/bin/bash
# publish-wiki.sh - Manually publish wiki content to GitHub Wiki
#
# This script provides a manual way to publish wiki content if needed.
# Normally, the GitHub Actions workflow (.github/workflows/publish-wiki.yml)
# handles automatic publishing on push to master.

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
WIKI_SOURCE_DIR="${SCRIPT_DIR}/wiki"
# Derive wiki repo name from current directory name
REPO_NAME="$(basename "$(git rev-parse --show-toplevel)")"
WIKI_REPO_DIR="${SCRIPT_DIR}/../${REPO_NAME}.wiki"
# Derive wiki URL from git remote
REPO_URL="$(git remote get-url origin | sed 's/\.git$//')"
WIKI_URL="${REPO_URL}.wiki.git"

echo "================================================"
echo "Query4j Wiki Publisher"
echo "================================================"
echo ""

# Check if wiki source directory exists
if [ ! -d "$WIKI_SOURCE_DIR" ]; then
    echo "❌ Error: Wiki source directory not found at: $WIKI_SOURCE_DIR"
    exit 1
fi

# Check if we have wiki files
if ! ls "$WIKI_SOURCE_DIR"/*.md 1> /dev/null 2>&1; then
    echo "❌ Error: No markdown files found in: $WIKI_SOURCE_DIR"
    exit 1
fi

echo "📂 Wiki source directory: $WIKI_SOURCE_DIR"
echo "📦 Wiki repository URL: $WIKI_URL"
echo ""

# Clone or update wiki repository
if [ -d "$WIKI_REPO_DIR" ]; then
    echo "📥 Wiki repository already exists, fetching latest..."
    cd "$WIKI_REPO_DIR"
    git fetch origin
    # Clean update to avoid merge conflicts
    git reset --hard origin/master
    cd "$SCRIPT_DIR"
else
    echo "📥 Cloning wiki repository..."
    git clone "$WIKI_URL" "$WIKI_REPO_DIR"
fi

echo ""
echo "📋 Copying wiki files..."
cp -v "$WIKI_SOURCE_DIR"/*.md "$WIKI_REPO_DIR/"

echo ""
echo "🔍 Checking for changes..."
cd "$WIKI_REPO_DIR"

if git diff-index --quiet HEAD --; then
    echo "✅ No changes to wiki content"
    echo ""
    echo "Wiki is already up to date!"
    exit 0
fi

echo ""
echo "📝 Changes detected:"
git status --short

echo ""
read -p "Do you want to commit and push these changes? (y/N) " -n 1 -r
echo ""

if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "❌ Cancelled by user"
    exit 1
fi

echo ""
echo "💾 Committing changes..."
git add *.md

COMMIT_MSG="Update wiki content from main repository

Manual sync via publish-wiki.sh script
Date: $(date '+%Y-%m-%d %H:%M:%S %Z')"

git commit -m "$COMMIT_MSG"

echo ""
echo "📤 Pushing to GitHub..."
git push origin master

echo ""
echo "================================================"
echo "✅ Wiki Published Successfully!"
echo "================================================"
echo ""
echo "View your wiki at:"
# Derive wiki URL from repository URL
echo "${REPO_URL}/wiki"
echo ""
echo "Published pages:"
for file in *.md; do
    if [ -f "$file" ]; then
        page_name="${file%.md}"
        echo "  • $page_name"
    fi
done
echo ""
