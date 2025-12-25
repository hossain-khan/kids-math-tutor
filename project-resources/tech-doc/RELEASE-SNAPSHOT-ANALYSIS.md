# Release Snapshot Generation Guide

This document provides exact instructions for AI agents to generate a new `RELEASE-SNAPSHOT-v*.md` document for future releases.

## Overview

A Release Snapshot is a comprehensive historical record of the project state at the time of a release. It captures code statistics, git history, features, and technical metrics that serve as a memento of the release milestone.

**Output File Location**: `project-resources/tech-doc/RELEASE-SNAPSHOT-v{VERSION}.md`

---

## Prerequisites

- Access to the project repository with git history
- `cloc` command-line tool installed (for code statistics)
- Gradle build system available
- Ability to run shell commands

## Step-by-Step Generation Process

### Step 1: Gather Code Statistics with cloc

**Purpose**: Count lines of code, excluding generated/build artifacts

**Command**:
```bash
cd /Users/hossain/dev/repos/hk-projects/kids-math-tutor
cloc . --exclude-dir=node_modules,dist,build,.gradle,.vite,.wrangler
```

**Why these exclusions**:
- `node_modules`: NPM dependencies (not our code)
- `dist`: Built webapp output
- `build`: Android build artifacts
- `.gradle`: Gradle cache
- `.vite`: Vite build cache
- `.wrangler`: Cloudflare Workers build cache

**Expected Output Format**:
```
Language                  Files      Blank    Comment      Code
─────────────────────────────────────────────────────────────
Kotlin                      264      5620        7277     39426
Markdown                     40      3995            2     18551
[... more languages ...]
─────────────────────────────────────────────────────────────
TOTAL                       396     11395        7840     69370
```

**What to Extract**:
- Each language row (Language, Files, Blank, Comment, Code)
- TOTAL row with sum of all lines
- Calculate primary languages by percentage
- Primary Kotlin LOC (production app code)

### Step 2: Gather Git Statistics

**Purpose**: Count commits and contributions by author

**Commands**:
```bash
# Total commits
git rev-list --count HEAD

# Commits by author
git shortlog -sn

# Recent tags for version context
git tag --sort=-version:refname | head -5
```

**Expected Output Examples**:
```
Total Commits: 747
     510  Hossain Khan
     217  copilot-swe-agent[bot]
      20  renovate[bot]
```

**What to Extract**:
- Total commit count
- Top contributors with their commit counts
- Contributor percentages
- Latest version tags

### Step 2.5: Analyze Repository Size with git-sizer (Optional but Recommended)

**Purpose**: Get detailed repository bloat analysis and identify potential git performance issues

**Prerequisites**:
```bash
# Install git-sizer (macOS with Homebrew)
brew install git-sizer

# Or from GitHub releases: https://github.com/github/git-sizer/releases
# Download the binary for your platform and add to PATH
```

**Commands**:
```bash
# Run git-sizer with verbose output
git-sizer --verbose

# Or get JSON output for structured analysis
git-sizer --json
```

**Expected Output Summary**:
```
| Name                         | Value     | Level of concern
| Overall repository size      |           |
| * Commits                    |   747     | (low concern)
| * Trees                      |  X.XX MiB | (low concern)
| * Blobs                      |   X.XX MB | (low concern)
| Biggest objects              |           |
| * Commits max size           |  X.X KiB  |
| * Blobs max size             |  X.X MiB  |
| History structure            |           |
| * Maximum history depth      |   747     |
| Biggest checkouts            |           |
| * Number of directories      |   X.XX k  |
| * Number of files            |   X.XX k  |
| * Total size of files        |   X.XX MB |
```

**What to Extract**:
- Repository size concerns (asterisks indicate severity)
- Biggest individual objects (commits, trees, blobs)
- Maximum history depth (git operations cost)
- Largest single checkout (disk space when cloned)
- Path depth and file count
- Any warnings about potential issues (excessive branching, huge blobs, etc.)

**Interpretation Guide**:
- **No asterisks**: Normal range (healthy)
- **\* to \*\***: Minor concern (monitor)
- **\*\*\* to \*\*\*\*\***: Significant concern (may impact performance)
- **\!\*\*\*\*\***: Critical concern (definite problems)

**Note**: For the kids-math-tutor project, git-sizer should show low concern across all metrics since it's a healthy, well-maintained repository under 1 GiB total size.

### Step 3: Get App Build Information

**Source File**: `app/build.gradle.kts`

**Command**:
```bash
grep -E "versionCode|versionName|minSdk|targetSdk|compileSdk" app/build.gradle.kts
```

**Expected Output**:
```
versionCode = 14
versionName = "1.13.0"
minSdk = 28
targetSdk = 36
compileSdk = 36
```

**What to Extract**:
- Current `versionCode` and `versionName`
- Min/Target/Compile SDK values
- Map SDK numbers to Android versions (28=Android 9, 36=Android 15, etc.)

### Step 4: Count Android App Source Files

**Purpose**: Quantify Kotlin codebase size and structure

**Commands**:
```bash
# Total Kotlin source files
find app/src/main -name "*.kt" | wc -l

# Total Kotlin test files
find app/src/test -name "*.kt" | wc -l

# Count Compose UI files (estimate)
grep -r "@Composable" app/src/main/java --include="*.kt" | wc -l

# Count Circuit pattern files (estimate)
grep -r "@CircuitInject\|Presenter\|Screen" app/src/main/java --include="*.kt" | wc -l

# Total Kotlin LOC (from cloc output)
# Use the Kotlin row from cloc: "Code" column value
```

**Expected Output**:
```
Kotlin Source Files: 182
Test Files: 69
Composables: ~80 functions
Circuit patterns: ~40 files
Total Kotlin LOC: 39,426
```

**What to Extract**:
- Number of source files (main/java)
- Number of test files (test)
- Estimated composables and circuit patterns
- Total lines of code from cloc

### Step 5: Get Build Artifact Sizes

**Purpose**: Understand app size and optimization potential

**Commands**:
```bash
# Build debug APK
./gradlew assembleDebug

# Get debug APK size
ls -lh app/build/outputs/apk/debug/app-debug.apk | awk '{print $5, $9}'

# Estimate release size (typically 15-20MB after R8 minification)
# Note: Actual size depends on ProGuard rules in proguard-rules.pro
```

**Expected Output**:
```
108M app-debug.apk
```

**What to Extract**:
- Debug APK size in MB
- Add note about estimated release size (typically 15-20MB after R8 minification)
- Explain why debug is larger (debug symbols, full debugging info)

### Step 6: Get Project Size Breakdown

**Purpose**: Understand overall disk usage

**Commands**:
```bash
# Total project size
du -sh /Users/hossain/dev/repos/hk-projects/kids-math-tutor

# Git repository size only
du -sh .git

# Subdirectory breakdown
du -sh webapp/node_modules webapp/dist app/build project-resources

# Verify build artifacts are in git
git ls-files webapp/node_modules | wc -l
git ls-files webapp/dist | wc -l
```

**Expected Output**:
```
2.2G     total
45M      .git (source history)
617M     webapp/node_modules
2.6M     webapp/dist
0        files in git from node_modules/dist
```

**What to Extract**:
- Total project size
- Size of key directories
- Confirmation that build artifacts are NOT tracked in git

### Step 7: Gather Feature and Release Information

**Source Files**: 
- `CHANGELOG.md` (for latest version changes)
- `GOOGLE-PLAY.md` (for feature descriptions)
- `app/build.gradle.kts` (for dependencies)
- `gradle/libs.versions.toml` (for dependency versions)

**What to Extract**:
- Major features released in current version
- Key dependencies and their versions
- Libraries: Circuit, Metro, Compose, WorkManager, Firebase, etc.
- Release date (current date when snapshot is created)

### Step 8: Create Release Checklist

**Purpose**: Document that release was production-ready

**Verification Steps**:
```bash
# 1. Verify clean build
./gradlew clean build

# 2. Check build success
echo $?  # Should be 0

# 3. Verify no uncommitted changes
git status  # Should be clean

# 4. Verify all tests passed
./gradlew test

# 5. Verify proguard configuration
cat proguard-rules.pro | grep -E "^-keep|^-dontwarn" | head -10
```

**Checklist Items**:
- ✅ All tests passing
- ✅ Build successful with no errors
- ✅ Release APK generated
- ✅ No uncommitted changes
- ✅ GitHub PR merged to main
- ✅ Version tag created and pushed
- ✅ Google Play documentation complete

---

## Document Structure Template

Use this structure for the release snapshot:

```markdown
# Math Pup v{VERSION} - Release Snapshot
**Release Date**: {TODAY'S DATE}  
**Version**: {VERSION} (versionCode: {CODE})  
**Status**: {STATUS} ✅

---

## 📊 Project Statistics

### Code Statistics (cloc)
*Excluding generated artifacts: node_modules, dist/, build/, .gradle, .vite, .wrangler*

[Include cloc output table here]

**Total Project Lines of Code**: {TOTAL} lines (source code only)  
**Primary Languages**: {LANG1} ({PCT1}%), {LANG2} ({PCT2}%), ...

### Git Statistics
[Include contributor breakdown]

### Repository Size Analysis (git-sizer)
*Optional but provides valuable insights into repository health and performance*

[Include git-sizer output showing:
- Overall repository size and concerns
- Biggest objects (commits, trees, blobs)
- History structure metrics
- Biggest checkout stats
- Any warnings or alerts]

### App Build Information
[Include versionCode, versionName, SDKs]

### Android App Source Files
[Include file counts and LOC breakdown]

### Build Artifacts
[Include APK sizes]

### Web (React/TypeScript) Statistics
[Include webapp stats]

### Project Size
[Include disk usage breakdown]

---

## 🎯 Release Features

### Major Features
[List of new features in this release]

### Key Improvements
[List of improvements]

### Bug Fixes
[List of bug fixes]

---

## 📚 Technical Details

### Key Dependencies
[List of major dependencies and versions]

### Architecture Patterns
[Brief overview of Circuit UDF, Metro DI, Compose]

### Code Quality
[Test coverage, build status, formatting]

---

## ✅ Release Checklist

[Verification status of build, tests, deployment readiness]

---

## 📝 Notes

[Any additional context about this release]

---

## 🔗 Related Files

- Changelog: [CHANGELOG.md](../../../CHANGELOG.md)
- Release Notes: [GOOGLE-PLAY.md](../../../GOOGLE-PLAY.md)
- Build Config: [app/build.gradle.kts](../../../app/build.gradle.kts)
```

---

## Exact Commands Sequence (Quick Reference)

For future agents, here's the complete command sequence:

```bash
#!/bin/bash
cd /Users/hossain/dev/repos/hk-projects/kids-math-tutor

# 1. Code statistics
echo "=== CODE STATISTICS ===" && \
cloc . --exclude-dir=node_modules,dist,build,.gradle,.vite,.wrangler

# 2. Git statistics
echo "=== GIT STATISTICS ===" && \
echo "Total commits: $(git rev-list --count HEAD)" && \
git shortlog -sn

# 2.5. Repository size analysis (optional)
echo "=== REPOSITORY SIZE ANALYSIS ===" && \
git-sizer --verbose

# 3. App version info
echo "=== APP BUILD INFO ===" && \
grep -E "versionCode|versionName|minSdk|targetSdk|compileSdk" app/build.gradle.kts

# 4. Kotlin file counts
echo "=== KOTLIN FILE COUNTS ===" && \
echo "Source files: $(find app/src/main -name '*.kt' | wc -l)" && \
echo "Test files: $(find app/src/test -name '*.kt' | wc -l)"

# 5. Build artifacts
echo "=== BUILD ARTIFACTS ===" && \
ls -lh app/build/outputs/apk/debug/app-debug.apk 2>/dev/null || echo "Run ./gradlew assembleDebug first"

# 6. Project size
echo "=== PROJECT SIZE ===" && \
du -sh . && \
du -sh webapp/node_modules webapp/dist 2>/dev/null

# 7. Build status
echo "=== BUILD STATUS ===" && \
./gradlew clean build --no-daemon 2>&1 | tail -5
```

---

## Common Pitfalls to Avoid

1. **Include build artifacts in code count**: Always use `--exclude-dir` for cloc
2. **Missing git history context**: Run git commands from project root
3. **Outdated dependency versions**: Always check `gradle/libs.versions.toml`
4. **Forgetting to exclude generated files**: node_modules, dist/, build/ must be excluded
5. **Incorrect SDK version mapping**: Document SDK numbers with Android version names (28=Android 9, 36=Android 15)
6. **Missing date context**: Always include the date the snapshot was created (release date)
7. **Skipping git-sizer output**: While optional, git-sizer provides valuable performance insights - don't skip it for production releases
8. **Misinterpreting git-sizer results**: Remember that asterisks indicate concern levels; no asterisks = healthy, more asterisks = potential problems

---

## File Naming Convention

**Format**: `RELEASE-SNAPSHOT-v{MAJOR}.{MINOR}.{PATCH}.md`

**Examples**:
- `RELEASE-SNAPSHOT-v1.13.0.md`
- `RELEASE-SNAPSHOT-v2.0.0.md`

**Location**: `project-resources/tech-doc/`

---

## After Creating the Document

1. **Commit the file**:
   ```bash
   git add project-resources/tech-doc/RELEASE-SNAPSHOT-v{VERSION}.md
   git commit -m "docs: Create release snapshot for v{VERSION}"
   ```

2. **Push to repository**:
   ```bash
   git push origin main
   ```

3. **Verification**:
   ```bash
   # Verify file is tracked
   git ls-files project-resources/tech-doc/RELEASE-SNAPSHOT-v{VERSION}.md
   ```

---

## Support

For questions about specific metrics or data extraction, refer to:
- Code statistics: `man cloc`
- Git history: `git help shortlog`
- Gradle info: `grep` in `app/build.gradle.kts`
- Build output: Check `app/build/outputs/`
