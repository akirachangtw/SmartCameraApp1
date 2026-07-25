---
name: qa
description: 啟動本地端完整 QA 流程：編譯 → 單元測試 → Warning 清理 → PROGRESS.md 更新。
disable-model-invocation: false
---

> RFC 2119 Keywords: **MUST** = 違反即 FAIL; **SHOULD** = 強烈建議，可附理由跳過; **MAY** = 純可選。

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
- ⛔ FAIL → **MUST** 立即回報違規項目，**不執行 assembleDebug**
  - 修正範圍問題後重跑 /qa
- ⚪ `tmp/verify_cmd.conf` 不存在 → 跳過 Step 0，直接進 Step 1

**重要**：Step 0 是確定性腳本（零 AI 配額），不需解讀輸出，只看退出碼。

### DIFF_REF 回填三原子動作（開工時執行，`INFRA-GOV-DEESC-01` Rev A AI-2b）

票務開工時（非 session Boot），**MUST** 依序執行三原子動作，缺一即停：
1. `git rev-parse --short HEAD` 回填票面 `DIFF_REF`。
2. 同步再生 `tmp/verify_cmd.conf`（含回填後 `DIFF_REF`）。
3. 回填值與 conf 摘要記入證據欄。

### Anchor Drift STOP（首次成文，`INFRA-GOV-DEESC-01` Rev A AI-2c）

回填制下，Anchor Drift STOP 僅保留一種觸發：回填後之 `DIFF_REF..HEAD` 之間出現**觸及本票 ALLOWED 檔**之 commit（票面現值描述失真風險，需人裁決）；其餘 HEAD 前進一律不觸發。**本判定目前尚無 hook 機械強制點**，屬 /sv、/qa 執行者行為準則；未來若機械化須另立票。

## Step 1: 編譯 (Build)

執行 `./gradlew assembleDebug 2>&1 | tail -50`

- ✅ 成功 → 繼續 Step 2
- 🔴 失敗 → 擷取錯誤 Log，**MUST** 立即提出修復方案並執行。**MUST** 禁止沉默失敗。
  - 修復後重新編譯。最多重試 2 次。
  - 第 3 次仍失敗 → 回報 `🔴 QA BLOCKED: Compile` 並**停止整個 QA 流程**。

## Step 2: 單元測試 (Unit Tests)

執行 `./gradlew testDebugUnitTest 2>&1 | tail -30`

- ✅ 全部通過 → 繼續 Step 3
- 🔴 有失敗 → 列出失敗的測試名稱與錯誤訊息。
  - 判斷是「測試本身需更新」還是「代碼有 Bug」。
  - 若是代碼 Bug → **MUST** 修復代碼，重跑測試。
  - 若是測試過期 → **MUST** 更新測試，重跑。

> ⚠️ **Reachability 紀律（INFRA-REACH-01）**：Unit test 綠燈僅證明 isolated logic 正確，**不證明**測試注入的 state 在 production 可達。涉及硬體 / runtime state（如 `afState`、camera lifecycle state）的 AC，reachability 由 Step 3.5.5 + /sv 階段 B-x 把關。**Unit test pass ≠ production reachability confirmed**。

## Step 3: Warning 審查

執行 `./gradlew lint 2>&1 | grep -E "(Warning|Error)" | head -20`

- 列出所有 Warning，分類為：
  - 🔴 **MUST** 修：可能導致 Runtime Crash 的（null safety、deprecated API without replacement）
  - 🟡 **SHOULD** 修：代碼品質問題（unused import、magic number）
  - ⚪ **MAY** 忽略：工具誤報或已知豁免

- 🔴 類 **MUST** 立即修復。🟡 類在不影響進度時修復。

## Step 3.5: 靜態盲區掃描

在 Step 3（Build + Test / Lint）之後自動執行以下確定性掃描。掃描範圍僅限 `app/src/main/`，不掃描 test 目錄。

```bash
bash .claude/skills/qa/scripts/static_blind_scan.sh
```

Step 3.5 產出 ⚠️ WARNING 而非硬 FAIL；所有 ⚠️ 項目 **SHOULD** 在 `sv_report.md` 中記錄豁免理由，由 `cross_review.sh` Layer 3 和 Traycer 做最終判斷。

## Step 4 / 4.5 例外：票面明文優先序

Step 4（PROGRESS.md 更新）與 Step 4.5（Auto Commit / Auto-Chain）之通用 MUST，於下列情形自動改判 **SKIP**，不計 FAIL、不觸發 STOP：

- 票面 FORBIDDEN 命中該步驟之目標檔案，或
- 票面 Hard Constraints 明文例外該步驟之通用流程

命中時 QA 報告標注 `SKIPPED — DEFERRED-TO-DOCSYNC`。

**承接條件（缺一仍 STOP）**：存在承接 DOCSYNC 票，**或** QA 報告「待開票」節登記同步債（含本票 ID 與完成狀態）——防同步債靜默累積。

**適用範圍**：本條款僅適用「Skill 通用流程 vs 票面明文例外（FORBIDDEN 或 Hard Constraints）」此一衝突型；其餘規則衝突維持 Rule 11 停下確認，**不得推廣為任意衝突之自動裁決**。

## Step 4: 更新 PROGRESS.md Dashboard

讀取 `PROGRESS.md`，更新以下欄位：
- Sprint Health 指標（Compile/Tests/Lint）
- Active Blockers（新增或移除阻塞點）
- Next Actions（更新下一步）

**不在此步驟標記 Ticket Done**。`PROGRESS_ARCHIVE.md` ✅ Done 標記由 `/sv` PASS 後執行（見 sv/SKILL.md Step 10）。

## Step 4.5: Ticket Auto Commit

/qa Step 4 完成後，從 `tmp/verify_cmd.conf` 讀取 Ticket 範圍並自動提交：

1. **前置確認**：
   - `tmp/verify_cmd.conf` **MUST** 存在且可讀；否則 → `⛔ Auto Commit BLOCKED: tmp/verify_cmd.conf 不存在`
   - 從 conf 讀取 `TICKET_ID`、`ALLOWED`（使用 `grep "^KEY=" | head -1 | cut -d'=' -f2-`，**MUST** 禁止 `source`）
   - `ALLOWED` **MUST** 不得為空；否則 → `⛔ Auto Commit BLOCKED: ALLOWED 為空`

2. **Scope 汙染確認**：
   - 執行 `git status --short`，確認無非 ALLOWED 且非 Pipeline Artifact 豁免（`.claude/PROGRESS.md` + `.claude/PROGRESS_ARCHIVE.md`，範圍與行數界線以 `verify_checklist.sh` 的 `PIPELINE_ARTIFACT` / `PIPELINE_ARTIFACT_MAX_LINES` 為單一真相來源，本行僅描述性引用）的 dirty 檔案
   - 若有 → **MUST** 停止並回報 `⛔ Scope 汙染: [檔案名]`，**不執行 commit**

3. **零 diff 判定**：
   - **Malformed ALLOWED 防禦**：執行前述 `git status --short` 前，若 `ALLOWED` 任一條目含 wildcard／glob／pathspec 異常字元或格式無法安全解析 → **STOP**，回報 **MUST** 逐字引述 `tmp/verify_cmd.conf` 實際內容（禁猜測、禁憑記憶轉述、禁自行修正後續行）
   - 執行 `git status --short -- <ALLOWED 逐檔清單>`
   - 輸出為空 → 真零 diff（untracked `??` 新增檔視為有變更，**不算**輸出為空）：
     - 跳過下列「4. Staging」「5. Commit」子步驟
     - `tmp/verify_cmd.conf` 的 `DIFF_END` 寫回與 `DIFF_REF` 相同值（沿用第 7 項寫入邏輯，來源值改為 `grep "^DIFF_REF=" tmp/verify_cmd.conf | cut -d'=' -f2-`）
     - QA Report 對應行輸出 `✅ Auto Commit: SKIPPED（ZERO-DIFF-DECLARED，DIFF_END=DIFF_REF）`
     - 直接進入「Step 4.5 完成後：Auto-Chain 銜接點」
   - 輸出非空 → 非零 diff，繼續下列步驟（行為零變更）

4. **Staging（精確 stage，**MUST** 禁止 `git add .`）**：
   - 只 stage `ALLOWED` 中每個檔案路徑 + `.claude/PROGRESS.md`

5. **Commit**：
   - Message 格式：`[TICKET_ID] <變更摘要>`
   - **SHOULD** 加 `Co-Authored-By: Claude Sonnet 4.6 <noreply@anthropic.com>` trailer
   - Commit 失敗 → `⛔ Auto Commit FAILED`，**不進入 /sv**

6. **成功確認**：輸出 commit hash，在 QA Report 附加 `✅ Auto Commit: [hash] [TICKET_ID]`

7. **DIFF_END 更新**：Commit 成功後，將 commit hash 寫入 `tmp/verify_cmd.conf` 的 `DIFF_END`：
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

**Auto-Chain 停止條件**（任一觸發即停止，**MUST** 不進入 /sv）：
- 任一 QA Step FAIL
- context 剩餘 ≤ 30K tokens

停止時輸出：`⛔ Auto-Chain STOPPED: [原因]`

## Step 5: Pipeline 結構校驗

/sv + Cross-Review PASS 後，執行最終結構完整性驗證：

```bash
bash .claude/skills/qa/scripts/structural_verify.sh ./sv_report.md "" --post
```

**四級 Verdict 語義**（`structural_verify.sh v2.0`）：

| Verdict | Exit | 行動 |
|---------|------|------|
| PASS | 0 | ✅ Ticket 驗收完成，可提交 Traycer |
| ADVISORY | 0 | ✅ 可繼續；警告項記錄於報告，建議下輪處理 |
| REPORT_FIX | 1 | 📋 修正 sv_report.md 後重新執行（不需重跑 /qa） |
| BLOCKER | 1 | 🔴 修程式碼或 Cross-Review 後重新 /qa → /sv |

Verdict 順序（嚴重度升序）：`PASS < ADVISORY < REPORT_FIX < BLOCKER`

- ⚪ ENV ERROR (exit 2) → 回報環境問題（lib_verify.sh 缺失 / tmp/ 無法建立）

`--post` 模式執行 A/B/C/D/E/F（D: sv_report 五段結構 / E: cross_review PASS / F: PROGRESS.md 格式）。
不帶 `--post`（cross_review.sh pre-check）只跑 A/B/C，不讀 `.tmp/cross_review_state`。

執行後自動在 `tmp/sv_report_YYYYMMDD_HHMMSS.md` 歸檔當次 sv_report 快照。

## 回報格式

```
=== QA Report ===
🔨 Build: ✅ PASS | assembleDebug (Xs)
🧪 Tests: ✅ 42 passed, 0 failed
🔍 Lint: ✅ 0 new warnings (3 existing baseline)
📋 Progress: ✅ PROGRESS.md updated
✅ Auto Commit: [hash] [TICKET_ID]（非零 diff）
✅ Auto Commit: SKIPPED（ZERO-DIFF-DECLARED，DIFF_END=DIFF_REF）（真零 diff）

Auto-Chain: Build: PASS [Xs] | Tests: [N]/[N] PASS | Lint: [N] warnings (0 error)
🏗️ Step 5: ✅ STRUCTURAL VERIFY PASS (A/B/C/D/E/F)
📊 Status: ~[X]K used | [狀態 emoji]（健康度依 Turns 判定）
Turns: [N] | Code: ~[M] lines
Suggestion: [建議]
Token 估算基準
```

`✅ Auto Commit` 行實際輸出**兩種互斥形態擇一**：形態由 Step 4.5 分支判定結果直取，**MUST** 禁止自由措辭。

## Session 健康度

參照 CLAUDE.md §Session Health 規則（以 Turns 判定，非 Context 佔比）。在 Report 末尾附上上述📊摘要。
📊 只顯示已用 token 絕對值，不顯示佔比：1M 模型佔比因 upstream bug #63447 不可靠（詳見 `status/SKILL.md`）。

## SV Archive Retention

`structural_verify.sh` archives each run to `tmp/sv_report_YYYYMMDD_HHMMSS.md`.
The archive retention policy is latest 5 snapshots only.
