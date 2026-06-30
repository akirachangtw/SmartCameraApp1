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

# 3.5.5 — Dead-State 掃描（CameraState enum/sealed 欄位 production reachability）
echo
echo "=== [3.5.5] Dead-State 掃描 ==="

CAMERASTATE_PATH=$(find app/src/main -name "CameraState.kt" 2>/dev/null | head -1)

if [[ -z "$CAMERASTATE_PATH" ]]; then
  echo "⚪ CameraState.kt 未找到，跳過 3.5.5"
else
  # Extract data class fields with non-primitive types (likely enum/sealed)
  ENUM_FIELDS=$(grep -E "^\s+val\s+\w+\s*:\s*[A-Z]\w+" "$CAMERASTATE_PATH" \
    | grep -viE ":\s*(String|Int|Float|Double|Boolean|Long|List|Map|Set|Flow|StateFlow|MutableStateFlow|Rect(F)?|PointF|Uri|Size|Bitmap|Any)" \
    | sed -E 's/.*val ([a-z][a-zA-Z0-9_]*)\s*:.*/\1/' \
    | grep -E "^[a-z]\w+$" \
    | sort -u || true)

  if [[ -z "$ENUM_FIELDS" ]]; then
    echo "⚪ 無 enum/sealed 型別欄位，跳過"
  else
    DEAD_COUNT=0
    for FIELD in $ENUM_FIELDS; do
      # Pattern: field = <non-=> char (excludes == comparisons)
      # Exclusions: val <field> = (local var decl), <field> = x.<field> (copy-through)
      ALL_WRITES=$(grep -rn "\b${FIELD}\s*=\s*[^=]" app/src/main/ --include="*.kt" 2>/dev/null \
        | grep -v "/CameraState.kt:" \
        | grep -viE "val\s+${FIELD}\s*=" \
        | grep -viE "\b${FIELD}\s*=\s*[a-z]\w*\.${FIELD}\b" \
        || true)

      if [[ -z "$ALL_WRITES" ]]; then
        echo "⚠️  ${FIELD}: production 無任何賦值點（dead field 候選）"
        DEAD_COUNT=$((DEAD_COUNT + 1))
        continue
      fi

      NON_RESET=$(echo "$ALL_WRITES" \
        | grep -viE "\b(Idle|None|Default|Initial|Unknown|NotStarted|Inactive|Off|Unset|Empty)\b" || true)
      if [[ -z "$NON_RESET" ]]; then
        echo "⚠️  ${FIELD}: 所有 production 賦值均為 reset/Idle（Guard input 不可達）"
        echo "$ALL_WRITES" | head -2 | sed 's/^/     /'
        DEAD_COUNT=$((DEAD_COUNT + 1))
      fi
    done

    if [[ $DEAD_COUNT -gt 0 ]]; then
      echo "→ /sv 階段 B-x MUST 確認上述欄位 Guard reachability，結果記錄於 sv_report.md"
      SCAN_FAIL=1
    else
      echo "✅ 所有欄位均有 production 非 reset 賦值點"
    fi
  fi
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
