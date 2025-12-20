#!/bin/bash

# Query4j Dynamic Query Builder - Release Preparation Script
# Version: 1.0.0
# This script performs all pre-release verification checks

set -e

echo "=================================="
echo "Query4j Release Preparation v1.0.0"
echo "=================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print success
print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

# Function to print error
print_error() {
    echo -e "${RED}✗ $1${NC}"
}

# Function to print warning
print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

# Function to print section header
print_section() {
    echo ""
    echo "=================================="
    echo "$1"
    echo "=================================="
}

# Check Java version
print_section "1. Checking Java Version"
JAVA_VERSION=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -ge 17 ] 2>/dev/null; then
    print_success "Java 17+ detected"
    java -version 2>&1 | head -1
else
    print_error "Java 17+ required (detected: $JAVA_VERSION)"
    exit 1
fi

# Check Gradle
print_section "2. Checking Gradle"
if [ -f "./gradlew" ]; then
    print_success "Gradle wrapper found"
else
    print_error "Gradle wrapper not found"
    exit 1
fi

# Check version in build.gradle
print_section "3. Verifying Version"
VERSION=$(./gradlew properties -q | grep "^version:" | awk '{print $2}')
if [ -z "$VERSION" ]; then
    # Fallback to direct parsing if Gradle command fails
    VERSION=$(grep "^version = " build.gradle | sed "s/version = ['\"]//;s/['\"]//")
fi
echo "Current version: $VERSION"
if [[ $VERSION == *"SNAPSHOT"* ]]; then
    print_error "Version is still SNAPSHOT. Please update to stable version."
    exit 1
else
    print_success "Version is stable: $VERSION"
fi

# Clean build
print_section "4. Clean Build"
echo "Running: ./gradlew clean"
if ./gradlew clean > /dev/null; then
    print_success "Clean completed"
else
    print_error "Clean failed"
    exit 1
fi

# Run tests
print_section "5. Running Tests"
echo "This may take a few minutes..."
./gradlew test
if [ $? -eq 0 ]; then
    print_success "All tests passed"
else
    print_error "Tests failed"
    exit 1
fi

# Check code coverage
print_section "6. Checking Code Coverage"
./gradlew jacocoRootReport jacocoRootCoverageVerification
if [ $? -eq 0 ]; then
    print_success "Code coverage meets threshold (95%+)"
else
    print_warning "Code coverage below threshold"
fi

# Build all modules
print_section "7. Building All Modules"
./gradlew clean build
if [ $? -eq 0 ]; then
    print_success "Build successful"
else
    print_error "Build failed"
    exit 1
fi

# Generate JavaDoc
print_section "8. Generating JavaDoc"
./gradlew javadoc
if [ $? -eq 0 ]; then
    print_success "JavaDoc generation successful"
else
    print_error "JavaDoc generation failed"
    exit 1
fi

# Check for uncommitted changes
print_section "9. Checking Git Status"
if [ -n "$(git status --porcelain)" ]; then
    print_warning "There are uncommitted changes:"
    git status --short
else
    print_success "Working directory is clean"
fi

# Verify release notes exist
print_section "10. Verifying Release Documentation"
if [ -f "RELEASE_NOTES_v${VERSION}.md" ]; then
    print_success "Release notes found: RELEASE_NOTES_v${VERSION}.md"
else
    print_warning "Release notes not found: RELEASE_NOTES_v${VERSION}.md"
fi

if [ -f "CHANGELOG.md" ]; then
    if grep -q "\[${VERSION}\]" CHANGELOG.md; then
        print_success "Version ${VERSION} found in CHANGELOG.md"
    else
        print_warning "Version ${VERSION} not found in CHANGELOG.md"
    fi
else
    print_error "CHANGELOG.md not found"
fi

# Check for existing tag
print_section "11. Checking Git Tags"
if git rev-parse "v${VERSION}" >/dev/null 2>&1; then
    print_error "Tag v${VERSION} already exists"
    exit 1
else
    print_success "Tag v${VERSION} does not exist (ready to create)"
fi

# Summary
print_section "Pre-Release Verification Complete"
echo ""
print_success "All pre-release checks passed!"
echo ""
echo "Next steps:"
echo "  1. Review all changes one final time"
echo "  2. Create release tag: git tag -a v${VERSION} -m 'Release v${VERSION}'"
echo "  3. Push tag: git push origin v${VERSION}"
echo "  4. Monitor GitHub Actions release workflow"
echo "  5. Verify artifacts are published"
echo ""
echo "=================================="
