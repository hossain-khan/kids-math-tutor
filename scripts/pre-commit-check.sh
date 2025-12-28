#!/bin/bash

# Pre-commit check script for Kids Math Tutor project
# Runs formatters, linters, and builds for both Android and webapp
# Usage: ./scripts/pre-commit-check.sh

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}================================${NC}"
echo -e "${BLUE}Pre-Commit Checks${NC}"
echo -e "${BLUE}================================${NC}\n"

# Get the root directory
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

# Track overall status
FAILED=0

# ============================================================================
# ANDROID CHECKS
# ============================================================================
echo -e "${YELLOW}[1/8] Formatting Android code...${NC}"
if ./gradlew formatKotlin > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Android formatting complete${NC}\n"
else
    echo -e "${RED}✗ Android formatting failed${NC}\n"
    FAILED=1
fi

echo -e "${YELLOW}[2/8] Linting Android code...${NC}"
if ./gradlew lintKotlin > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Android linting passed${NC}\n"
else
    echo -e "${RED}✗ Android linting failed${NC}\n"
    FAILED=1
fi

echo -e "${YELLOW}[3/8] Running Android unit tests...${NC}"
if ./gradlew testDebugUnitTest > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Android unit tests passed${NC}\n"
else
    echo -e "${RED}✗ Android unit tests failed${NC}\n"
    FAILED=1
fi

echo -e "${YELLOW}[4/8] Linting Android debug build...${NC}"
if ./gradlew lintDebug > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Android debug lint passed${NC}\n"
else
    echo -e "${RED}✗ Android debug lint failed${NC}\n"
    FAILED=1
fi

echo -e "${YELLOW}[5/8] Building Android app...${NC}"
if ./gradlew assembleDebug > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Android build successful${NC}\n"
else
    echo -e "${RED}✗ Android build failed${NC}\n"
    FAILED=1
fi

# ============================================================================
# WEBAPP CHECKS
# ============================================================================
cd "$ROOT_DIR/webapp"

echo -e "${YELLOW}[6/8] Formatting webapp code...${NC}"
if pnpm format > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Webapp formatting complete${NC}\n"
else
    echo -e "${RED}✗ Webapp formatting failed${NC}\n"
    FAILED=1
fi

echo -e "${YELLOW}[7/8] Linting webapp code...${NC}"
if pnpm lint > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Webapp linting passed${NC}\n"
else
    echo -e "${RED}✗ Webapp linting failed${NC}\n"
    FAILED=1
fi

echo -e "${YELLOW}[8/8] Building webapp...${NC}"
if pnpm build > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Webapp build successful${NC}\n"
else
    echo -e "${RED}✗ Webapp build failed${NC}\n"
    FAILED=1
fi

# ============================================================================
# SUMMARY
# ============================================================================
echo -e "${BLUE}================================${NC}"
if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}All checks passed! Ready to commit.${NC}"
    echo -e "${BLUE}================================${NC}\n"
    exit 0
else
    echo -e "${RED}Some checks failed. Please fix the issues above.${NC}"
    echo -e "${BLUE}================================${NC}\n"
    exit 1
fi
