#!/bin/bash
# SmartCameraApp Git Auto Commit Script
# Version: 1.0.0
# Purpose: Automated git commit with version tagging

set -e  # Exit on error

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Get version from build.gradle.kts
VERSION=$(grep 'versionName = ' app/build.gradle.kts | sed 's/.*versionName = "\(.*\)"/\1/')
VERSION_CODE=$(grep 'versionCode = ' app/build.gradle.kts | sed 's/.*versionCode = \(.*\)/\1/')

echo -e "${GREEN}=== SmartCameraApp Git Auto Commit ===${NC}"
echo -e "${YELLOW}Version: ${VERSION} (Code: ${VERSION_CODE})${NC}"

# Check if there are changes
if [[ -z $(git status -s) ]]; then
    echo -e "${YELLOW}No changes to commit${NC}"
    exit 0
fi

# Show status
echo -e "\n${YELLOW}Current changes:${NC}"
git status -s

# Confirm with user
read -p "Proceed with commit? (y/n) " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo -e "${RED}Commit cancelled${NC}"
    exit 1
fi

# Stage all changes
echo -e "\n${GREEN}Staging changes...${NC}"
git add .

# Extract latest CHANGELOG entry
CHANGELOG_ENTRY=$(sed -n "/^## \[${VERSION}\]/,/^## \[/p" CHANGELOG.md | sed '1d;$d')

# Create commit message
COMMIT_MSG="[v${VERSION}] 版本發布

${CHANGELOG_ENTRY}

🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude <noreply@anthropic.com>"

# Commit
echo -e "\n${GREEN}Creating commit...${NC}"
git commit -m "$COMMIT_MSG"

# Create tag
echo -e "\n${GREEN}Creating tag v${VERSION}...${NC}"
git tag -a "v${VERSION}" -m "v${VERSION} - Release"

# Show commit
echo -e "\n${GREEN}Commit created:${NC}"
git log -1 --oneline

# Ask about push
read -p "Push to remote? (y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo -e "\n${GREEN}Pushing to remote...${NC}"
    git push origin main --tags
    echo -e "${GREEN}✓ Pushed successfully${NC}"
else
    echo -e "${YELLOW}Skipped push. Run 'git push origin main --tags' manually${NC}"
fi

echo -e "\n${GREEN}✓ Git operations completed${NC}"
