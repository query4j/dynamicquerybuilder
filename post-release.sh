#!/bin/bash

# Query4j Dynamic Query Builder - Post-Release Script
# This script helps with post-release tasks

set -e

echo "=================================="
echo "Query4j Post-Release Tasks"
echo "=================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print section header
print_section() {
    echo ""
    echo "=================================="
    echo "$1"
    echo "=================================="
}

# Function to print info
print_info() {
    echo -e "${BLUE}ℹ $1${NC}"
}

# Function to print success
print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

# Get current version
CURRENT_VERSION=$(grep "^version = " build.gradle | cut -d"'" -f2)
echo "Current released version: $CURRENT_VERSION"

# Calculate next version
IFS='.' read -r major minor patch <<< "$CURRENT_VERSION"
NEXT_MINOR=$((minor + 1))
NEXT_VERSION="${major}.${NEXT_MINOR}.0-SNAPSHOT"

print_section "Post-Release Checklist"

echo "Please complete the following tasks manually:"
echo ""
echo "1. GitHub Release"
print_info "   - Go to: https://github.com/query4j/dynamicquerybuilder/releases"
print_info "   - Verify release v${CURRENT_VERSION} is published"
print_info "   - Check that all artifacts are attached"
print_info "   - Review release notes for accuracy"
echo ""

echo "2. Announcements"
print_info "   - Post to GitHub Discussions"
print_info "   - Share on Twitter/X (see ANNOUNCEMENT.md)"
print_info "   - Share on LinkedIn (see ANNOUNCEMENT.md)"
print_info "   - Post to Reddit r/java (see ANNOUNCEMENT.md)"
echo ""

echo "3. Documentation"
print_info "   - Verify JavaDoc is published: https://query4j.github.io/dynamicquerybuilder/"
print_info "   - Check all documentation links work"
print_info "   - Verify README badges show correct version"
echo ""

echo "4. Repository Updates"
print_info "   - Close milestone for v${CURRENT_VERSION}"
print_info "   - Create milestone for v${NEXT_VERSION}"
print_info "   - Update roadmap with completed features"
print_info "   - Archive old issues if needed"
echo ""

print_section "Preparing Next Development Version"
echo ""
echo "The next development version will be: ${NEXT_VERSION}"
echo ""
read -p "Do you want to update build.gradle to ${NEXT_VERSION}? (y/n) " -n 1 -r
echo ""

if [[ $REPLY =~ ^[Yy]$ ]]; then
    # Update version in build.gradle
    sed -i.bak "s/version = '${CURRENT_VERSION}'/version = '${NEXT_VERSION}'/" build.gradle
    print_success "Updated build.gradle to version ${NEXT_VERSION}"
    
    # Show the change
    echo ""
    echo "Change:"
    diff build.gradle.bak build.gradle || true
    
    # Clean up backup
    rm build.gradle.bak
    
    echo ""
    echo "Next steps:"
    echo "  1. Review the version change"
    echo "  2. Commit: git add build.gradle"
    echo "  3. Commit: git commit -m 'Prepare for next development iteration: ${NEXT_VERSION}'"
    echo "  4. Push: git push origin main"
else
    print_info "Skipped version update"
fi

print_section "Post-Release Verification"
echo ""
print_info "Monitor the following:"
echo "  • GitHub Actions workflows: https://github.com/query4j/dynamicquerybuilder/actions"
echo "  • GitHub Issues for bug reports: https://github.com/query4j/dynamicquerybuilder/issues"
echo "  • GitHub Discussions for feedback: https://github.com/query4j/dynamicquerybuilder/discussions"
echo ""

print_section "Complete!"
echo ""
print_success "Post-release tasks checklist displayed"
echo ""
