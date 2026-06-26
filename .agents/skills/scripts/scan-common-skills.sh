#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="${1:-.}"
SCAN_ROOT="$ROOT_DIR/.agents/skills"
MANIFEST_FILE="$SCAN_ROOT/MANIFEST.md"
EXIT_CODE=0
BLOCKERS=0
WARNS=0
INFOS=0

if [[ ! -d "$SCAN_ROOT" ]]; then
  echo "[INFO][I000] $SCAN_ROOT not found; nothing to scan"
  exit 0
fi

report() {
  local level="$1"
  local code="$2"
  local file="$3"
  local msg="$4"
  printf '[%s][%s] %s: %s\n' "$level" "$code" "$file" "$msg"
  case "$level" in
    BLOCKER) BLOCKERS=$((BLOCKERS+1)); EXIT_CODE=2 ;;
    WARN) WARNS=$((WARNS+1)); [[ $EXIT_CODE -lt 1 ]] && EXIT_CODE=1 ;;
    INFO) INFOS=$((INFOS+1)) ;;
  esac
}

is_governance_file() {
  local file="$1"
  [[ "$file" == "$MANIFEST_FILE" ]] && return 0
  [[ "$file" == "$SCAN_ROOT/scripts/scan-common-skills.sh" ]] && return 0
  [[ "$file" == *"/resources/governance/"* ]] && return 0
  return 1
}

is_exempt_file() {
  local file="$1"
  is_governance_file "$file" && return 0
  [[ "$file" == *"/fixtures/"* ]] && return 0
  [[ "$file" == *"/testdata/"* ]] && return 0
  [[ "$file" == *"/examples/"* ]] && grep -q 'SCAN_ALLOW_EXAMPLE_RUNTIME_PATH=true' "$file" 2>/dev/null && return 0
  return 1
}

has_marker() {
  local file="$1"
  local marker="$2"
  grep -q "$marker" "$file" 2>/dev/null
}

scan_file_blockers() {
  local file="$1"
  is_exempt_file "$file" && return 0

  if grep -nE '\.claude/skills|\.claude/PROGRESS\.md|\.claude/rules' "$file" >/dev/null 2>&1; then
    if ! has_marker "$file" 'ADAPTER_SCOPE=claude'; then
      report BLOCKER B001 "$file" 'hard-coded Claude runtime root found in common layer'
    fi
  fi

  if grep -nE '\.codex/skills|agents/openai\.yaml|\.codex/' "$file" >/dev/null 2>&1; then
    if ! has_marker "$file" 'ADAPTER_SCOPE=codex'; then
      report BLOCKER B002 "$file" 'hard-coded Codex runtime root found in common layer'
    fi
  fi

  if grep -nE 'replace AGENTS\.md|sole runtime entrypoint' "$file" >/dev/null 2>&1; then
    report BLOCKER B003 "$file" 'common layer appears to replace AGENTS.md semantics'
  fi

  if grep -nE '\.claude/PROGRESS\.md|PROGRESS\.md' "$file" >/dev/null 2>&1; then
    if ! has_marker "$file" 'ADAPTER_SCOPE=claude'; then
      report BLOCKER B004 "$file" 'progress/state file mutation path detected in common layer'
    fi
  fi

  if grep -nE '/qa\b|/sv\b|cross_review\.sh|ticket-breakdown|strict-verification|conversation-lifecycle' "$file" >/dev/null 2>&1; then
    if [[ "$file" != *"SKILL.md"* ]] && ! has_marker "$file" 'ADAPTER_SCOPE='; then
      report BLOCKER B005 "$file" 'runtime wrapper invocation pattern detected in common layer'
    fi
  fi
}

scan_file_warnings() {
  local file="$1"
  is_exempt_file "$file" && return 0

  if grep -nE '\.claude\b|\.codex\b|openai\.yaml|PROGRESS\.md' "$file" >/dev/null 2>&1; then
    report WARN W001 "$file" 'runtime-looking string found; verify this is not hidden coupling'
  fi

  case "$file" in
    *.sh|*.py)
      if ! grep -qE 'REPO_ROOT|RUNTIME_ROOT|SCRIPT_DIR|--repo-root|--runtime-root|ROOT_DIR|SCAN_ROOT' "$file"; then
        report WARN W002 "$file" 'missing obvious path parameterization markers'
      fi
      ;;
  esac
}

while IFS= read -r -d '' file; do
  scan_file_blockers "$file"
  scan_file_warnings "$file"
done < <(find "$SCAN_ROOT" -type f \( -name '*.md' -o -name '*.sh' -o -name '*.py' -o -name '*.js' -o -name '*.ts' -o -name '*.json' -o -name '*.yaml' -o -name '*.yml' \) -print0)

if [[ -f "$MANIFEST_FILE" ]]; then
  while IFS= read -r skill_dir; do
    skill_name="$(basename "$skill_dir")"
    case "$skill_name" in
      scripts|resources) continue ;;
    esac
    if ! grep -q "| $skill_name |" "$MANIFEST_FILE"; then
      report WARN W003 "$skill_dir" 'skill directory exists but MANIFEST.md has no registry entry'
    fi
  done < <(find "$SCAN_ROOT" -mindepth 1 -maxdepth 1 -type d | sort)
else
  report WARN W003 "$MANIFEST_FILE" 'MANIFEST.md not found'
fi

if [[ -f "$MANIFEST_FILE" ]]; then
  if ! grep -q 'prompt_contract_path' "$MANIFEST_FILE"; then
    report INFO I001 "$MANIFEST_FILE" 'telemetry field prompt_contract_path not mentioned'
  fi
  if ! grep -q 'skill_source_root' "$MANIFEST_FILE"; then
    report INFO I001 "$MANIFEST_FILE" 'telemetry field skill_source_root not mentioned'
  fi
  if ! grep -q 'runtime_adapter_root' "$MANIFEST_FILE"; then
    report INFO I001 "$MANIFEST_FILE" 'telemetry field runtime_adapter_root not mentioned'
  fi
fi

echo "---"
echo "Summary: blockers=$BLOCKERS warns=$WARNS infos=$INFOS"

if [[ $BLOCKERS -gt 0 ]]; then
  echo "Result: FAIL"
elif [[ $WARNS -gt 0 ]]; then
  echo "Result: WARN"
else
  echo "Result: PASS"
fi

exit "$EXIT_CODE"

