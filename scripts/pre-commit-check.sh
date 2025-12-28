#!/bin/bash

# Pre-commit check script for Kids Math Tutor project
# Runs formatters, linters, and builds for both Android and webapp
# Usage: ./scripts/pre-commit-check.sh [--android-only|--web-only]
#   ./scripts/pre-commit-check.sh          # Run all checks
#   ./scripts/pre-commit-check.sh --android-only  # Run Android checks only
#   ./scripts/pre-commit-check.sh --web-only      # Run web checks only

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Parse arguments
RUN_ANDROID=true
RUN_WEB=true

case "${1:-}" in
  --android-only)
    RUN_WEB=false
    ;;
  --web-only)
    RUN_ANDROID=false
    ;;
  --help|-h)
    echo "Pre-commit check script for Kids Math Tutor project"
    echo ""
    echo "Usage: ./scripts/pre-commit-check.sh [--android-only|--web-only]"
    echo ""
    echo "Options:"
    echo "  (no args)      Run all checks (Android + Web)"
    echo "  --android-only Run Android checks only"
    echo "  --web-only     Run Web checks only"
    echo "  --help, -h     Show this help message"
    exit 0
    ;;
  *)
    if [ -n "$1" ]; then
      echo -e "${RED}Unknown option: $1${NC}"
      echo "Use --help for usage information"
      exit 1
    fi
    ;;
esac

echo -e "${BLUE}================================${NC}"
echo -e "${BLUE}Pre-Commit Checks${NC}"
if [ "$RUN_ANDROID" = true ] && [ "$RUN_WEB" = true ]; then
  echo -e "${BLUE}(Android + Web)${NC}"
elif [ "$RUN_ANDROID" = true ]; then
  echo -e "${BLUE}(Android only)${NC}"
else
  echo -e "${BLUE}(Web only)${NC}"
fi
echo -e "${BLUE}================================${NC}\n"

# Get the root directory
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

# Track overall status
FAILED=0
CHECK_COUNT=0

# ============================================================================
# ANDROID CHECKS
# ============================================================================
if [ "$RUN_ANDROID" = true ]; then
  echo -e "${YELLOW}[1/5] Formatting Android code...${NC}"
  if ./gradlew formatKotlin > /dev/null 2>&1; then
      echo -e "${GREEN}✓ Android formatting complete${NC}\n"
  else
      echo -e "${RED}✗ Android formatting failed${NC}\n"
      FAILED=1
  fi

  echo -e "${YELLOW}[2/5] Linting Android code...${NC}"
  if ./gradlew lintKotlin > /dev/null 2>&1; then
      echo -e "${GREEN}✓ Android linting passed${NC}\n"
  else
      echo -e "${RED}✗ Android linting failed${NC}\n"
      FAILED=1
  fi

  echo -e "${YELLOW}[3/5] Running Android unit tests...${NC}"
  if ./gradlew testDebugUnitTest > /dev/null 2>&1; then
      echo -e "${GREEN}✓ Android unit tests passed${NC}\n"
  else
      echo -e "${RED}✗ Android unit tests failed${NC}\n"
      FAILED=1
  fi

  echo -e "${YELLOW}[4/5] Linting Android debug build...${NC}"
  if ./gradlew lintDebug > /dev/null 2>&1; then
      echo -e "${GREEN}✓ Android debug lint passed${NC}\n"
  else
      echo -e "${RED}✗ Android debug lint failed${NC}\n"
      FAILED=1
  fi

  echo -e "${YELLOW}[5/5] Building Android app...${NC}"
  if ./gradlew assembleDebug > /dev/null 2>&1; then
      echo -e "${GREEN}✓ Android build successful${NC}\n"
  else
      echo -e "${RED}✗ Android build failed${NC}\n"
      FAILED=1
  fi
fi

# ============================================================================
# WEBAPP CHECKS
# ============================================================================
if [ "$RUN_WEB" = true ]; then
  cd "$ROOT_DIR/webapp"

  WEB_CHECK_START=1
  if [ "$RUN_ANDROID" = true ]; then
    WEB_CHECK_START=6
  fi

  echo -e "${YELLOW}[$WEB_CHECK_START/5] Formatting webapp code...${NC}"
  if pnpm format > /dev/null 2>&1; then
      echo -e "${GREEN}✓ Webapp formatting complete${NC}\n"
  else
      echo -e "${RED}✗ Webapp formatting failed${NC}\n"
      FAILED=1
  fi

  LINT_CHECK=$((WEB_CHECK_START + 1))
  echo -e "${YELLOW}[$LINT_CHECK/5] Linting webapp code...${NC}"
  if pnpm lint > /dev/null 2>&1; then
      echo -e "${GREEN}✓ Webapp linting passed${NC}\n"
  else
      echo -e "${RED}✗ Webapp linting failed${NC}\n"
      FAILED=1
  fi

  BUILD_CHECK=$((WEB_CHECK_START + 2))
  echo -e "${YELLOW}[$BUILD_CHECK/5] Building webapp...${NC}"
  if pnpm build > /dev/null 2>&1; then
      echo -e "${GREEN}✓ Webapp build successful${NC}\n"
  else
      echo -e "${RED}✗ Webapp build failed${NC}\n"
      FAILED=1
  fi
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
