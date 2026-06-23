#!/usr/bin/env bash
# md-formatting.sh
# Cleans AI-generated annotation tags and trailing whitespace from Markdown files.
# Target paths (relative to repo root):
#   - tmp/*.md              (top-level only, non-recursive)
#   - plans/cso_briefing/**/*.md  (recursive)
#
# Usage:
#   bash .claude/skills/cleanchat/scripts/md-formatting.sh [REPO_ROOT]
#
# REPO_ROOT defaults to the git repo root of the current directory.

set -euo pipefail

REPO_ROOT="${1:-$(git rev-parse --show-toplevel)}"

# Each entry: "path:maxdepth"  (maxdepth=1 → non-recursive, 99 → fully recursive)
declare -A TARGET_DEPTHS=(
    ["$REPO_ROOT/tmp"]=1
    ["$REPO_ROOT/plans/cso_briefing"]=99
)

scanned=0
cleaned=0
cleaned_list=()

clean_file() {
    local file="$1"
    local original
    original=$(cat "$file")
    local content="$original"

    # Remove AI footnote definitions: [^1]: ...
    content=$(printf '%s' "$content" | sed -E '/^\[\^[[:space:]]*[0-9]+[[:space:]]*\]:.*/d')

    # Remove inline annotation tags: [file:1], [^1], [cite:1], [web:1]
    content=$(printf '%s' "$content" | sed -E 's/\[(\^|[a-zA-Z]+:)[[:space:]]*[0-9]+[[:space:]]*\]//g')

    # Remove Turns/Code stats lines
    content=$(printf '%s' "$content" | sed -E '/^[[:space:]]*Turns:.*\|.*Code:.*/d')

    # Remove Suggestion lines
    content=$(printf '%s' "$content" | sed -E '/^[[:space:]]*Suggestion:.*/d')

    # Remove /handoff header lines appearing in first 5 lines
    # Matches: "# /handoff" or "/handoff" (with optional leading spaces)
    content=$(printf '%s' "$content" | awk '
        NR <= 5 && /^[[:space:]]*(#[[:space:]]*)?\/handoff([[:space:]].*)?$/ { next }
        { print }
    ')

    # Collapse multiple consecutive blank lines into a single blank line
    content=$(printf '%s' "$content" | awk '
        /^[[:space:]]*$/ { blank++; next }
        { if (blank > 0) { print ""; blank=0 } print }
        END { if (blank == 1) print "" }
    ')

    # Trim trailing blank lines, ensure single trailing newline
    content=$(printf '%s\n' "$content" | sed -e 's/[[:space:]]*$//' | awk 'NF{found=NR} END{for(i=1;i<=found;i++) print lines[i]} {lines[NR]=$0}')
    content=$(printf '%s\n' "$content")

    if [ "$content" != "$original" ]; then
        printf '%s\n' "$content" > "$file"
        return 0
    fi
    return 1
}

for dir in "${!TARGET_DEPTHS[@]}"; do
    if [ ! -d "$dir" ]; then
        echo "WARN: directory not found, skipping: $dir"
        continue
    fi

    maxdepth="${TARGET_DEPTHS[$dir]}"

    while IFS= read -r -d '' file; do
        basename=$(basename "$file")
        [ "$basename" = "Clean_Chat_Canvas_Skill.md" ] && continue

        scanned=$((scanned + 1))
        rel_path="${file#$REPO_ROOT/}"
        if clean_file "$file"; then
            cleaned=$((cleaned + 1))
            cleaned_list+=("  - $rel_path")
        fi
    done < <(find "$dir" -maxdepth "$maxdepth" -name "*.md" -print0)
done

echo ""
echo "Scanned : $scanned file(s)"
if [ "$cleaned" -gt 0 ]; then
    echo "Cleaned : $cleaned file(s)"
    for entry in "${cleaned_list[@]}"; do
        echo "$entry"
    done
else
    echo "Cleaned : 0 files (all already clean)"
fi
