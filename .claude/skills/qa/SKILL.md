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

若 `tmp/verify_cmd.conf` 存在，執行：

```bash
bash .claude/skills/qa/scripts/verify_checklist.sh tmp/verify_cmd.conf
```

- ✅ PASS → 繼續 Step 1
- ⚠️ WARNING (STALE/MISMATCH) → 回報警告後繼續 Step 1
- ⛔ FAIL → 立即回報違規項目，**不執行 assembleDebug**
  - 修正範圍問題後重跑 /qa
- ⚪ `tmp/verify_cmd.conf` 不存在 → 跳過 Step 0，直接進 Step 1

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

**不在此步驟標記 Ticket Done**。`PROGRESS_ARCHIVE.md` ✅ Done 標記由 `/sv` PASS 後執行（見 sv/SKILL.md Step 10）。

## Step 4.5: Ticket Auto Commit

/qa Step 4 完成後，從 `tmp/verify_cmd.conf` 讀取 Ticket 範圍並自動提交：

1. **前置確認**：
   - `tmp/verify_cmd.conf` 必須存在且可讀；否則 → `⛔ Auto Commit BLOCKED: tmp/verify_cmd.conf 不存在`
   - 從 conf 讀取 `TICKET_ID`、`ALLOWED`（使用 `grep "^KEY=" | head -1 | cut -d'=' -f2-`，禁止 `source`）
   - `ALLOWED` 不得為空；否則 → `⛔ Auto Commit BLOCKED: ALLOWED 為空`

2. **Scope 汙染確認**：
   - 執行 `git status --short`，確認無非 ALLOWED 且非 `.claude/PROGRESS.md` 的 dirty 檔案
   - 若有 → 停止並回報 `⛔ Scope 汙染: [檔案名]`，**不執行 commit**

3. **Staging（精確 stage，禁止 `git add .`）**：
   - 只 stage `ALLOWED` 中每個檔案路徑 + `.claude/PROGRESS.md`

4. **Commit**：
   - Message 格式：`[TICKET_ID] <變更摘要>`
   - 必須加 `Co-Authored-By: Claude Sonnet 4.6 <noreply@anthropic.com>` trailer
   - Commit 失敗 → `⛔ Auto Commit FAILED`，**不進入 /sv**

5. **成功確認**：輸出 commit hash，在 QA Report 附加 `✅ Auto Commit: [hash] [TICKET_ID]`

6. **DIFF_END 更新**：Commit 成功後，將 commit hash 寫入 `tmp/verify_cmd.conf` 的 `DIFF_END`：
   ```bash
   _LAST_COMMIT=$(git rev-parse --short HEAD)
   if grep -q "^DIFF_END=" tmp/verify_cmd.conf 2>/dev/null; then
     sed -i "s/^DIFF_END=.*/DIFF_END=${_LAST_COMMIT}/" tmp/verify_cmd.conf
   else
     echo "DIFF_END=${_LAST_COMMIT}" >> tmp/verify_cmd.conf
   fi
   ```
   輸出：`📌 DIFF_END=${_LAST_COMMIT} → tmp/verify_cmd.conf（鎖定 Ticket 審查終點）`

---

## Step 4.5 完成後：Auto-Chain 銜接點

若 Step 0-4.5 全部 PASS（無任何 FAIL），輸出單行摘要後**自動進入 /sv**：

```
Auto-Chain: Build: PASS [Xs] | Tests: [N]/[N] PASS | Lint: [N] warnings (0 error)
```

**Auto-Chain 停止條件**（任一觸發即停止，不進入 /sv）：
- 任一 QA Step FAIL
- context 剩餘 ≤ 30K tokens

停止時輸出：`⛔ Auto-Chain STOPPED: [原因]`

## Step 5: Pipeline 結構校驗

/sv + Cross-Review PASS 後，執行最終結構完整性驗證：

```bash
bash .claude/skills/qa/scripts/structural_verify.sh ./sv_report.md "" --post
```

- ✅ PASS (exit 0) → Ticket 驗收完成，可提交 Traycer
- ⛔ FAIL (exit 1) → 回報具體失敗項，**不標記 Ticket Done**
- ⚪ ENV ERROR (exit 2) → 回報環境問題，不阻斷 Traycer 提交

`--post` 模式執行 A/B/C/D/E/F（D: sv_report 五段結構 / E: cross_review PASS / F: PROGRESS.md 格式）。
不帶 `--post`（cross_review.sh pre-check）只跑 A/B/C，不讀 `.tmp/cross_review_state`。

## 回報格式

```
=== QA Report ===
🔨 Build: ✅ PASS | assembleDebug (Xs)
🧪 Tests: ✅ 42 passed, 0 failed
🔍 Lint: ✅ 0 new warnings (3 existing baseline)
📋 Progress: ✅ PROGRESS.md updated
✅ Auto Commit: [hash] [TICKET_ID]

Auto-Chain: Build: PASS [Xs] | Tests: [N]/[N] PASS | Lint: [N] warnings (0 error)
🏗️ Step 5: ✅ STRUCTURAL VERIFY PASS (A/B/C/D/E/F)
```

## Session 健康度

參照 CLAUDE.md §Session Health 規則。在 Report 末尾附上上述📊摘要。
