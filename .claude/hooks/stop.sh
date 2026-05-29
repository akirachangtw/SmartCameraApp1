#!/usr/bin/env bash
# D12-T1: Stop Hook — CLAUDE.md micro-learning suggester
# Outputs ≤2 CLAUDE.md update suggestions when session modifies >50 lines of code.
# NEVER auto-modifies CLAUDE.md. Suggestions only.

set -euo pipefail

CLAUDE_MD=".claude/CLAUDE.md"
DIFF_REF="${DIFF_REF:-HEAD~1}"

# Error handling: missing CLAUDE.md → silent exit
[[ -f "$CLAUDE_MD" ]] || exit 0

# Count changed lines (.kt/.xml/.sh/.py only, exclude docs/tmp)
changed_lines=$(git diff "$DIFF_REF" HEAD -- \
  '*.kt' '*.xml' '*.sh' '*.py' \
  ':!tmp/' ':!docs/' ':!plans/' \
  2>/dev/null | grep -c '^[+-]' || true)

# Threshold: only suggest if >50 lines changed
(( changed_lines > 50 )) || exit 0

# Extract existing CLAUDE.md rules (numbered rules + section headers) for dedup check
existing_rules=$(grep -oE '### [0-9]+\.' "$CLAUDE_MD" | grep -oE '[0-9]+' || true)
existing_text=$(grep -E '^[-*] |^### ' "$CLAUDE_MD" | tr '[:upper:]' '[:lower:]' || true)

# Collect candidate patterns from this session's diff
diff_content=$(git diff "$DIFF_REF" HEAD -- '*.kt' '*.xml' '*.sh' ':!tmp/' ':!docs/' 2>/dev/null || true)
[[ -n "$diff_content" ]] || exit 0

suggestions=()

# --- Pattern detectors ---

# P1: Arg parsing positional array pattern (generalizable: not single-case)
if echo "$diff_content" | grep -qE 'POSITIONAL\+?=|for arg in.*\"\$@\"'; then
  rule_text='CLI flags 解析必須用 positional array loop（非 shift），確保 flag 可出現在任意位置'
  coverage_check=$(echo "$existing_text" | grep -c 'positional' || true)
  if (( coverage_check == 0 )); then
    suggestions+=("$(printf '[建議 %d]\n已有覆蓋？ 否\n泛化程度：原則級\n規則草稿：%s\ndiff 區塊：\n%s' \
      "${#suggestions[@]}" \
      "$rule_text" \
      "$(echo "$diff_content" | grep -A3 -B1 'POSITIONAL\+?=\|for arg in.*"\$@"' | head -20 || true)"
    )")
  fi
fi

# P2: Pre/post mode split in verification scripts (generalizable: tooling design)
if echo "$diff_content" | grep -qE '\-\-post|pre.?mode|post.?mode'; then
  rule_text='驗證腳本必須以 --post flag 區分「pre-check（提交前）」與「post-check（CI 後）」，避免讀取 stale state 造成循環依賴'
  coverage_check=$(echo "$existing_text" | grep -c '\-\-post\|pre.*post.*mode\|stale state' || true)
  if (( coverage_check == 0 )); then
    suggestions+=("$(printf '[建議 %d]\n已有覆蓋？ 否\n泛化程度：原則級\n規則草稿：%s\ndiff 區塊：\n%s' \
      "${#suggestions[@]}" \
      "$rule_text" \
      "$(echo "$diff_content" | grep -A3 -B1 '\-\-post\|pre_mode\|post_mode' | head -20 || true)"
    )")
  fi
fi

# P3: exit 0 on stale/absent state (generalizable: script robustness)
if echo "$diff_content" | grep -qE 'absent.*skip|PASS.*ADVISORY.*OK|exit 0.*absent'; then
  rule_text='驗收腳本讀取外部狀態檔時，absent（檔案不存在）必須視為 ⚪ skip 而非 FAIL，避免 bootstrap 順序問題'
  coverage_check=$(echo "$existing_text" | grep -c 'absent.*skip\|bootstrap' || true)
  if (( coverage_check == 0 )); then
    suggestions+=("$(printf '[建議 %d]\n已有覆蓋？ 否\n泛化程度：類別級\n規則草稿：%s\ndiff 區塊：\n%s' \
      "${#suggestions[@]}" \
      "$rule_text" \
      "$(echo "$diff_content" | grep -A3 -B1 'absent\|ADVISORY.*OK' | head -20 || true)"
    )")
  fi
fi

# P4: Auto-chain skill handoff (generalizable: skill pipeline design)
if echo "$diff_content" | grep -qE 'Auto.Chain|auto_chain|→.*\/sv|→.*\/qa'; then
  rule_text='Skill 間自動銜接（Auto-Chain）必須在 SKILL.md 明確標注停止條件（FAIL / context ≤ 30K），避免 silent pass-through 掩蓋問題'
  coverage_check=$(echo "$existing_text" | grep -c 'auto.chain\|auto chain\|stop.*condition' || true)
  if (( coverage_check == 0 )); then
    suggestions+=("$(printf '[建議 %d]\n已有覆蓋？ 否\n泛化程度：類別級\n規則草稿：%s\ndiff 區塊：\n%s' \
      "${#suggestions[@]}" \
      "$rule_text" \
      "$(echo "$diff_content" | grep -A2 -B1 'Auto.Chain\|→.*\/sv' | head -20 || true)"
    )")
  fi
fi

# Limit to ≤2 suggestions (Hard Constraint)
output_count=${#suggestions[@]}
(( output_count > 2 )) && output_count=2

[[ $output_count -eq 0 ]] && exit 0

echo ""
echo "╔══════════════════════════════════════════════════════════╗"
echo "║  📚 CLAUDE.md 微學習建議（本 session diff: ${changed_lines} 行）        ║"
echo "║  以下為建議，不自動寫入。如採用請手動更新 CLAUDE.md。      ║"
echo "╚══════════════════════════════════════════════════════════╝"
echo ""

for (( i=0; i<output_count; i++ )); do
  echo "────────────────────────────────────────"
  echo "${suggestions[$i]}"
  echo ""
done

echo "（共 ${output_count} 條建議，上限 ≤2）"
echo ""

exit 0
