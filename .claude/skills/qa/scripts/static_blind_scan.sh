#!/usr/bin/env bash
# /qa Step 3.5 — 靜態盲區掃描（確定性，零 AI 配額）
# gstack v1.6 | 來源: Phase 0 退件經驗萃取

set -u

SCAN_FAIL=0

echo "# /qa Step 3.5 — 靜態盲區掃描"
echo

# 3.5.1 — 1.dp 可疑用法掃描
# 合法場景豁免：Divider thickness、BorderStroke width
echo "=== [3.5.1] 1.dp 可疑用法掃描 ==="
SUSPECT_1DP=$(grep -rn "1\.dp\b" app/src/main/ --include="*.kt" \
  | grep -viE "Divider|BorderStroke|border.*width" || true)
if [[ -n "$SUSPECT_1DP" ]]; then
  echo "⚠️ 以下 1.dp 用法需人工審查（可能應使用 LocalDensity）："
  echo "$SUSPECT_1DP"
  SCAN_FAIL=1
else
  echo "✅ 無可疑 1.dp 用法"
fi

# 3.5.2 — Hardcoded Insets 掃描
# 偵測 padding(top = XX.dp) 但未使用 System Insets API 的情況
echo
echo "=== [3.5.2] Hardcoded Insets 掃描 ==="
SUSPECT_INSETS=$(grep -rn "padding.*top.*=.*[0-9]\+\.dp" app/src/main/ --include="*.kt" \
  | grep -viE "statusBarsPadding|navigationBarsPadding|displayCutoutPadding|systemBarsPadding" || true)
if [[ -n "$SUSPECT_INSETS" ]]; then
  echo "⚠️ 以下 padding 可能是 hardcoded insets（應使用 System Insets API）："
  echo "$SUSPECT_INSETS"
  SCAN_FAIL=1
else
  echo "✅ 無 hardcoded insets"
fi

# 3.5.3 — Hardcoded 字串掃描（i18n Rule 9 違規）
echo
echo "=== [3.5.3] Hardcoded 字串掃描 ==="
SUSPECT_I18N=$(grep -rn 'text\s*=\s*"[A-Za-z]' app/src/main/ --include="*.kt" \
  | grep -viE "stringResource|@StringRes|ContentDescription|test|Test|TODO|FIXME|Log\." || true)
if [[ -n "$SUSPECT_I18N" ]]; then
  echo "⚠️ 以下可能是 hardcoded 字串（應使用 stringResource）："
  echo "$SUSPECT_I18N"
  SCAN_FAIL=1
else
  echo "✅ 無 hardcoded 字串"
fi

# 3.5.4 — Debug/FPS Overlay 殘留掃描
echo
echo "=== [3.5.4] Debug Overlay 殘留掃描 ==="
SUSPECT_DEBUG=$(grep -rn "FpsOverlay\|DebugOverlay\|debugMode\s*=\s*true\|isDebug\s*=\s*true" \
  app/src/main/ --include="*.kt" \
  | grep -viE "test|Test|BuildConfig\.DEBUG" || true)
if [[ -n "$SUSPECT_DEBUG" ]]; then
  echo "⚠️ 以下可能是 Debug UI 殘留："
  echo "$SUSPECT_DEBUG"
  SCAN_FAIL=1
else
  echo "✅ 無 Debug Overlay 殘留"
fi

# 結果彙總
echo
echo "══════════════════════════════════════"
if [[ "$SCAN_FAIL" -eq 1 ]]; then
  echo "⚠️ Step 3.5 — 發現可疑項目，需人工審查後決定是否放行"
  echo "  （上述 ⚠️ 項目不自動 FAIL，但必須在 sv_report.md 中說明豁免理由）"
else
  echo "✅ Step 3.5 — 靜態盲區掃描全部通過"
fi
echo "══════════════════════════════════════"

exit 0
