#!/bin/bash
# Branch Documentation Sync Checker
# Purpose: Compare devspec/ and tools/ files between branches before merging to main
# Usage: ./tools/check_branch_docs_sync.sh <source-branch> [target-branch]
# Example: ./tools/check_branch_docs_sync.sh feature/camera-core-v2.11.0 main

SOURCE_BRANCH="$1"
TARGET_BRANCH="${2:-main}"

if [ -z "$SOURCE_BRANCH" ]; then
    echo "Usage: $0 <source-branch> [target-branch]"
    echo "Example: $0 feature/camera-core-v2.11.0 main"
    exit 1
fi

echo -e "\033[36m🔍 Checking devspec/ and tools/ documentation sync...\033[0m"
echo -e "   Source: \033[33m$SOURCE_BRANCH\033[0m"
echo -e "   Target: \033[33m$TARGET_BRANCH\033[0m"
echo ""

# Function to get files from a branch
get_branch_files() {
    local branch="$1"
    local path="$2"
    git ls-tree -r --name-only "$branch" "$path" 2>/dev/null | grep -E '\.(md|py|pyw|ps1|sh)$' | sort
}

# Get files from both branches
echo -e "\033[36m📂 Listing files in devspec/...\033[0m"
source_devspec=$(get_branch_files "$SOURCE_BRANCH" "devspec/")
target_devspec=$(get_branch_files "$TARGET_BRANCH" "devspec/")

echo -e "\033[36m📂 Listing files in tools/...\033[0m"
source_tools=$(get_branch_files "$SOURCE_BRANCH" "tools/")
target_tools=$(get_branch_files "$TARGET_BRANCH" "tools/")

# Compare devspec/
echo ""
echo -e "\033[90m$(printf '=%.0s' {1..80})\033[0m"
echo -e "\033[36m📋 devspec/ Comparison\033[0m"
echo -e "\033[90m$(printf '=%.0s' {1..80})\033[0m"

devspec_only_in_source=$(comm -23 <(echo "$source_devspec") <(echo "$target_devspec"))
devspec_only_in_target=$(comm -13 <(echo "$source_devspec") <(echo "$target_devspec"))

if [ -n "$devspec_only_in_source" ]; then
    echo ""
    echo -e "\033[33m⚠️  Files in $SOURCE_BRANCH but NOT in $TARGET_BRANCH:\033[0m"
    echo "$devspec_only_in_source" | while read -r file; do
        echo -e "   \033[32m+ $file\033[0m"
    done
fi

if [ -n "$devspec_only_in_target" ]; then
    echo ""
    echo -e "\033[33m⚠️  Files in $TARGET_BRANCH but NOT in $SOURCE_BRANCH:\033[0m"
    echo "$devspec_only_in_target" | while read -r file; do
        echo -e "   \033[31m- $file\033[0m"
    done
fi

if [ -z "$devspec_only_in_source" ] && [ -z "$devspec_only_in_target" ]; then
    echo ""
    echo -e "\033[32m✅ devspec/ files are in sync\033[0m"
fi

# Compare tools/
echo ""
echo -e "\033[90m$(printf '=%.0s' {1..80})\033[0m"
echo -e "\033[36m🛠️  tools/ Comparison\033[0m"
echo -e "\033[90m$(printf '=%.0s' {1..80})\033[0m"

tools_only_in_source=$(comm -23 <(echo "$source_tools") <(echo "$target_tools"))
tools_only_in_target=$(comm -13 <(echo "$source_tools") <(echo "$target_tools"))

if [ -n "$tools_only_in_source" ]; then
    echo ""
    echo -e "\033[33m⚠️  Files in $SOURCE_BRANCH but NOT in $TARGET_BRANCH:\033[0m"
    echo "$tools_only_in_source" | while read -r file; do
        echo -e "   \033[32m+ $file\033[0m"
    done
fi

if [ -n "$tools_only_in_target" ]; then
    echo ""
    echo -e "\033[33m⚠️  Files in $TARGET_BRANCH but NOT in $SOURCE_BRANCH:\033[0m"
    echo "$tools_only_in_target" | while read -r file; do
        echo -e "   \033[31m- $file\033[0m"
    done
fi

if [ -z "$tools_only_in_source" ] && [ -z "$tools_only_in_target" ]; then
    echo ""
    echo -e "\033[32m✅ tools/ files are in sync\033[0m"
fi

# Summary
echo ""
echo -e "\033[90m$(printf '=%.0s' {1..80})\033[0m"
echo -e "\033[36m📊 Summary\033[0m"
echo -e "\033[90m$(printf '=%.0s' {1..80})\033[0m"

total_missing=$(echo "$devspec_only_in_source $tools_only_in_source" | wc -w)
total_extra=$(echo "$devspec_only_in_target $tools_only_in_target" | wc -w)

echo ""
echo -e "Missing in $TARGET_BRANCH : \033[33m$total_missing files\033[0m"
echo -e "Extra in $TARGET_BRANCH   : \033[33m$total_extra files\033[0m"

# Recommendations
if [ "$total_missing" -gt 0 ]; then
    echo ""
    echo -e "\033[36m💡 Recommendation:\033[0m"
    echo -e "   \033[33mBefore merging to $TARGET_BRANCH, restore missing files:\033[0m"
    echo ""

    if [ -n "$devspec_only_in_source" ]; then
        echo -e "   \033[90m# Restore devspec/ files:\033[0m"
        echo "$devspec_only_in_source" | while read -r file; do
            echo -e "   git show ${SOURCE_BRANCH}:$file > $file"
        done
        echo ""
    fi

    if [ -n "$tools_only_in_source" ]; then
        echo -e "   \033[90m# Restore tools/ files:\033[0m"
        echo "$tools_only_in_source" | while read -r file; do
            echo -e "   git show ${SOURCE_BRANCH}:$file > $file"
        done
    fi
fi

echo ""
if [ "$total_missing" -eq 0 ] && [ "$total_extra" -eq 0 ]; then
    echo -e "\033[32m✅ All documentation is in sync!\033[0m"
    exit 0
else
    echo -e "\033[33m⚠️  Documentation sync check completed with differences\033[0m"
    exit 1
fi
