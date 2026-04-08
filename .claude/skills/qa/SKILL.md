---
disable-model-invocation: false
---
# Description
啟動本地端完整 QA 流程：編譯 → 單元測試 → Warning 清理 → PROGRESS.md 更新。

# Prompt
你現在是本專案的「自動化 QA」。請**嚴格依序**執行以下步驟：

## Step 1: 編譯 (Build)

執行 `./gradlew assembleDebug 2>&1 | tail -50`

- ✅ 成功 → 繼續 Step 2
- 🔴 失敗 → 擷取錯誤 Log，**立即提出修復方案並執行**。禁止沉默失敗。
  - 修復後重新編譯。最多重試 2 次。
  - 第 3 次仍失敗 → 回報 `🔴 QA BLOCKED: Compile` 並**停止整個 QA 流程**。

## Step 2: 單元測試 (Unit Tests)

執行 `./gradlew testDebugUnitTest 2>&1 | tail -30`

- ✅ 全部通過 → 繼續 Step 3
- 🔴 有失敗 → 列出失敗的測試名稱與錯誤訊息。
  - 判斷是「測試本身需更新」還是「代碼有 Bug」。
  - 若是代碼 Bug → 修復代碼，重跑測試。
  - 若是測試過期 → 更新測試，重跑。

## Step 3: Warning 審查

執行 `./gradlew lint 2>&1 | grep -E "(Warning|Error)" | head -20`

- 列出所有 Warning，分類為：
  - 🔴 必修：可能導致 Runtime Crash 的（null safety、deprecated API without replacement）
  - 🟡 建議修：代碼品質問題（unused import、magic number）
  - ⚪ 可忽略：工具誤報或已知豁免

- 🔴 類立即修復。🟡 類在不影響進度時修復。

## Step 4: PROGRESS.md 更新

讀取 `PROGRESS.md`，更新以下欄位：
- Today's Tickets 中已完成項目的 Status → ✅ DONE
- Files Modified 欄位填入實際修改的檔案
- Sprint Health 的 Compile/Lint 指標更新
- 如有新的 Blocking Issue，加入 Blocking Issues 表

## 回報格式

```
=== QA Report ===
🔨 Build: ✅ PASS | assembleDebug (Xs)
🧪 Tests: ✅ 42 passed, 0 failed
🔍 Lint: ✅ 0 new warnings (3 existing baseline)
📋 Progress: ✅ PROGRESS.md updated

→ QA 通過。可執行 /sv 進行語意驗收。
📊 Session: ~[N] turns | [🟢 繼續 / 🟡 留意 / 🔴 建議 /compact]
```

或失敗時：
```
=== QA Report ===
🔨 Build: 🔴 FAIL — Unresolved reference: FoodCompositionRule
🧪 Tests: ⏭️ SKIPPED (build failed)
🔍 Lint: ⏭️ SKIPPED
📋 Progress: ⚠️ Blocking issue logged

→ 🚫 QA 未通過。已嘗試修復 1 次，仍有 1 項阻塞。
📊 Session: ~[N] turns | [🟢 繼續 / 🟡 留意 / 🔴 建議 /compact]
```

## Session 健康度判斷規則

在 Report 末尾，根據當前對話狀態估算 Session 健康度：

| 對話輪數 | 狀態 | 建議 |
|---------|------|------|
| ≤ 10 turns | 🟢 | 繼續工作 |
| 11-18 turns | 🟡 | 留意，密集 Coding 可考慮下個 Ticket 前 /compact |
| ≥ 19 turns | 🔴 | 建議立即 /compact 開新 Session |

「turns」= 本次對話中 user 訊息的總數（不含系統 Prompt）。若對話中有大量代碼貼入（單則 >200 行），每則代碼額外計為 +2 turns。
