---
name: qa
description: 啟動本地端完整 QA 流程：編譯 → 單元測試 → Warning 清理 → PROGRESS.md 更新。
disable-model-invocation: false
---
# Description
啟動本地端完整 QA 流程：編譯 → 單元測試 → Warning 清理 → PROGRESS.md 更新。

# Prompt
你現在是本專案的「自動化 QA」。請**嚴格依序**執行以下步驟：

## Step 0: Ticket Scope 驗證 (Scope Gate)

如果當前 Ticket 包含 `## Verify Command` 區塊，執行該命令：

```bash
# 從 Ticket 複製 Verify Command，例如：
bash .claude/skills/qa/scripts/verify_checklist.sh \
  --allowed "TapFocusRing.kt,TapFocusController.kt" \
  --forbidden "CameraViewModel.kt,CameraRepository.kt" \
  --max-lines 80
```

- ✅ PASS → 繼續 Step 1
- ⛔ FAIL → 立即回報違規項目，**不執行 assembleDebug**
  - 修正範圍問題後重跑 /qa
- ⚪ Ticket 無 Verify Command → 跳過 Step 0，直接進 Step 1

**重要**：Step 0 是確定性腳本（零 AI 配額），不需解讀輸出，只看退出碼。

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

## Step 3.5: 靜態盲區掃描

在 Step 3（Build + Test / Lint）之後自動執行以下確定性掃描。掃描範圍僅限 `app/src/main/`，不掃描 test 目錄。

```bash
bash .claude/skills/qa/scripts/static_blind_scan.sh
```

Step 3.5 產出 ⚠️ WARNING 而非硬 FAIL；所有 ⚠️ 項目必須在 `sv_report.md` 中記錄豁免理由，由 `cross_review.sh` Layer 3 和 Traycer 做最終判斷。

## Step 4: 更新 PROGRESS.md Dashboard

讀取 `PROGRESS.md`，更新以下欄位：
- Sprint Health 指標（Compile/Tests/Lint）
- Active Blockers（新增或移除阻塞點）
- Next Actions（更新下一步）
- 若 Ticket 完成，在 `PROGRESS_ARCHIVE.md` 對應 Day 段落標記 ✅ DONE

## 回報格式

```
=== QA Report ===
🔨 Build: ✅ PASS | assembleDebug (Xs)
🧪 Tests: ✅ 42 passed, 0 failed
🔍 Lint: ✅ 0 new warnings (3 existing baseline)
📋 Progress: ✅ PROGRESS.md updated

→ QA 通過。可執行 /sv 進行語意驗收。
📊 Session: ~[N] turns | [狀態 emoji]
```

## Session 健康度

參照 CLAUDE.md §Session Health 規則。在 Report 末尾附上上述📊摘要。
