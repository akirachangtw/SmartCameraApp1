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

### Evidence Record（`INFRA-MAXLINES-EVIDENCE-01`）

conf 模式下，Step 0 收尾 **MUST** 落一筆 append-only evidence record 至
`<worktree>/tmp/<TICKET_ID>/evidence/step0_<commit_sha>.txt`（scratch 隔離制 §1：每票專屬子目錄，
禁共享單槽位），含六欄位：`TICKET_ID` / `WORKTREE`（`git rev-parse --show-toplevel`）/
`COMMIT_SHA`（`git rev-parse HEAD` 全長）/ `DIFF_REF` / `DIFF_END_RESOLVED` / `MEASURED_LINES`。
檔名含 `commit_sha` 使同票不同端點各自留痕；record 寫入失敗或缺失 **MUST NOT** 影響 Step 0
exit code（交付阻斷權屬五類第②項，非本層）。收口三件套之 receipt **MUST** 於收口當刻複製進
耐久載體，禁僅留路徑引用（`.claude/rules/concurrency.md` §1 律 B）。

### DIFF_REF 回填三原子動作（開工時執行，`INFRA-GOV-DEESC-01` Rev A AI-2b）

票務開工時（非 session Boot），**MUST** 依序執行三原子動作，缺一即停：
1. `git rev-parse --short HEAD` 回填票面 `DIFF_REF`。
2. 同步再生 `tmp/verify_cmd.conf`（含回填後 `DIFF_REF`）。
3. 回填值與 conf 摘要記入證據欄。

### Anchor Drift STOP（首次成文，`INFRA-GOV-DEESC-01` Rev A AI-2c）

回填制下，Anchor Drift STOP 僅保留一種觸發：回填後之 `DIFF_REF..HEAD` 之間出現**觸及本票 ALLOWED 檔**之 commit（票面現值描述失真風險，需人裁決）；其餘 HEAD 前進一律不觸發。**本判定目前尚無 hook 機械強制點**，屬 /sv、/qa 執行者行為準則；未來若機械化須另立票。

### Per-ticket config 落點（`INFRA-QA-PERTICKET-CONF-01`，根治 `tmp/verify_cmd.conf` 全域單槽位污染）

`verify_checklist.sh` 於 conf 模式下，讀取優先序：`tmp/verify/<TICKET_ID>.<REVISION>.conf`（`REVISION` 缺省時以 `rev-0` 佔位）若已存在即為來源；否則以傳入之 `tmp/verify_cmd.conf`（legacy，**MUST** 保留、**MUST NOT** 刪除或改寫）為 fallback 讀入，並立即快照一份至前述 per-ticket 路徑，同時於 stdout 輸出一行棄用警示。不同 `TICKET_ID` 各自持有獨立快照，互不覆寫；同票同 revision 之後續呼叫直接讀快照，不受 legacy 檔於期間被其他票覆寫影響。

`tmp/verify/active.json` 七欄位（`ticket_id`／`ticket_revision`／`repo_root`／`branch`／`head_sha`／`activated_at` UTC ISO-8601／`activation_source` 枚舉 `resume|new-session|ticket-switch`）由 conf-mode 每次呼叫**原子寫入**（temp file + atomic rename），寫入後 read-back 驗四欄（`ticket_id`／`ticket_revision`／`repo_root`／`head_sha`）；不一致僅警示，**不破壞**既有 per-ticket `.conf`。本檔案僅建立寫入器，**不接線**至任何 Write Authority 閘（該接線屬 `INFRA-QA-ACTIVATION-01` P2）。

`tmp/verify/*.conf` 依 mtime 保留最新 **10** 份，超額者刪除；`active.json` **永不輪替**；輪替/寫入失敗僅 `echo >&2`，**MUST NOT** 影響 Step 0 exit code。

**與 `tmp/<TICKET_ID>/evidence/` 之職責邊界**：`tmp/verify/` 為**設定層**（啟用態、per-ticket config 快照），`tmp/<TICKET_ID>/evidence/`（見上節）為**取證層**（Step 0 結果留痕）；兩路徑不相交，**MUST NOT** 合併或互相覆寫（`INFRA-MAXLINES-EVIDENCE-01` 既有成果）。

## Ticket Activation Preflight（`INFRA-QA-ACTIVATION-01`）

`.claude/skills/qa/scripts/preflight_activation.sh` 於下列四時機 **MUST** 執行一次：新 session 開工／`/resume`／切票／`post-compact`。**`/resume` ≠ 可立即續 edit**——preflight 未跑或未 PASS 前，**MUST NOT** 逕行修改 ALLOWED 檔案。

**四層語義**：
- **V0（環境）**：repo root／branch（僅照實輸出，**MUST NOT** 比對白名單——本專案無 allowed-branch 機制）／HEAD（detached → `BLOCKED_ENV_MISMATCH`）／`git status --short`／active ticket identity／票檔於 `TICKET_REGISTRY.json` 之 target-scope 存在性；不符 → `STATUS=BLOCKED_ENV_MISMATCH`，`ACTION=No writes, no test execution with ticket authority, no AC claim`。**顯式傳入 ticket_id**＝activation intent（切票），`activation_source="ticket-switch"`，identity 不比對；**省略**時 ticket_id 取自 `active.json`，mismatch 結構上不可能。
- **V1（baseline）**：現行 HEAD 對票面 `BASE_COMMIT`；`TARGET_BLOB`（格式 `path:sha8`，分號分隔多標的，`NEW`／`N/A` 跳過比對）逐標的以 `git hash-object` 比對，不符 → `BLOCKED_SCOPE_OR_TARGET_MISMATCH`。四態輸出沿用票面字面：`BASELINE_MATCHED`／`ALREADY_EXECUTED_PENDING_CLOSURE`／`CANDIDATE_SUPERSEDED`／`BLOCKED_UNATTRIBUTED_STATE`（R1 範圍不相交規則：`BASE_COMMIT..HEAD` 對 targets 之 diff 為空 → 視同 `BASELINE_MATCHED` 直進 V2）。
- **V2（語義探針）**：唯讀（**MUST NOT** 寫入任何檔案），輸出 `overall`（`MISSING`／`SATISFIED`／`PARTIAL`）與 `provenance`；`overall=SATISFIED` 且 `provenance=UNKNOWN` → 狀態改判 `CANDIDATE_SUPERSEDED`，**MUST NOT** 自動判 `DONE`／`ZERO-DIFF-DECLARED`。
- **Write Authority（AI-5.1 起由 `preflight_core.py::decide()` 唯一權威判定；Bash 側同名 `write_authority()` 已於 AI-6 刪除，不再有第二來源）**：判定公式、R-4 identity binding、R-7 fail-closed 契約全文見下方「## 決策核心契約」節，本節僅保留 stdout 欄位語義描述。

輸出快照寫入 `.claude/PROGRESS.md` `## Active Ticket Activation` 區塊（十一欄）為**人工依 preflight 輸出手動同步**（腳本本身不自動改寫 `.claude/PROGRESS.md`）；此為**最近一次實跑快照**，非即時輪詢，下次四時機任一觸發時 **MUST** 重跑並手動更新。

## 決策核心契約（AI-7，`INFRA-PREFLIGHT-CORE-01` Rev D.3-3；本節五項為 MUST 級契約，違反即 preflight adapter 行為 FAIL）

Bash（`preflight_activation.sh`）自 AI-6 起僅承擔 `FACT_COLLECTION`／`CORE_INVOCATION`／`CORE_DIRECTED_SIDE_EFFECT`／`LEGACY_STDOUT_SHIM` 四類職責（票面 AI-6 收尾職責歸類表，`INFRA-PREFLIGHT-CORE-01_Preflight_Decision_Core_Extraction.md:400`–`439`），**MUST NOT** 簡化為「一切副作用皆由 core 指示」——`CORE_DIRECTED_SIDE_EFFECT` 之 R-7 一格是規格預先指定之固定回應，非核心於現場指示。所有 `final_status`／`write_authority`／`reason_code` 之規則選定，唯一權威為 `.claude/skills/qa/scripts/preflight_core.py::decide()`。

1. **決策表為規則 SSOT**：`.claude/skills/qa/scripts/preflight_rules.json`（15 rows）為規則判定之唯一真相來源。新增或修改任何決策規則 **MUST** 同步修改該表與 `test_preflight_core.py`（決策語義測試），**MUST NOT** 改動 `preflight_activation.sh` 內任何 `if`／`case` 條件式以達成規則變更——Bash 條件式僅供事實蒐集（見上方四類職責），不承載規則。（回鏈：AC「收斂完整性」`INFRA-PREFLIGHT-CORE-01_...md:477`，`test_all_rows` 全 PASS；AC「回歸」同檔 `:487`）
2. **Model A 字面**：`write_authority=YES` 僅限同時滿足 `v0_env=PASS ∧ baseline_check=VALID ∧ v1_scope=UNCHANGED_TARGETS ∧ blob_check∈{MATCH,SKIPPED_NEW,SKIPPED_NA} ∧ v2_semantics=MISSING`（`preflight_core.py:271`–`284` `_write_authority()`）；`v2_semantics=PARTIAL` 或任何人工例外恆 `NO` 且 `reason_code` 含 `CONSERVATIVE_MANUAL_NOT_MACHINE_READABLE`；`manual_authorization` 固定 `NOT_MACHINE_READABLE`（無 producer）——**人工續作不構成工具背書**。（回鏈：AC「R-3，Model A」`INFRA-PREFLIGHT-CORE-01_...md:481`，`test_all_rows` 逐列斷言）
3. **R-4 四欄 identity binding**：adapter（`main()` 之 `core_identity_ok` 判定，`preflight_activation.sh:510`–`517`）於任何副作用前 **MUST** 逐欄比對 `ticket_id`／`repo_root`／`worktree_path`／`head_sha`——core Decision 回傳值與 adapter 當下實測值全等方得續行；`context_id` 為診斷／evidence 欄，adapter **MUST NOT** 重算 Python 之 normalized JSON hash 作判準。任一欄不符 → 依 R-7 fail-closed。（回鏈：AC「R-4，並行安全」`INFRA-PREFLIGHT-CORE-01_...md:482`，`test_preflight_adapter.sh` Scenario ④ 四欄逐一注入斷言）
   - **如實揭露（pending）**：現行 `repo_root` 與 `worktree_path` 兩事實欄皆源自同一命令 `git rev-parse --show-toplevel`（`collect_worktree_path_fact()` `preflight_activation.sh:385`–`389` 與 `V0_REPO_ROOT` 賦值 `:70`），非跨 worktree 實機下兩欄現為同值。R-4 綁定目的（防重放／防跨票污染）已達，但「跨 worktree 隔離」語義未經多 worktree 並行實測驗證——AC「並行 QA」（`INFRA-PREFLIGHT-CORE-01_...md:483`）未勾。**MUST NOT** 讀作「已支援 worktree 隔離」，僅得讀作「四欄比對機制已在盤，跨 worktree 分辨力 pending 實測」。
4. **R-7 fail-closed 契約**：core 缺席（`python3` 不可達、`preflight_core.py` 不在盤）、core 輸出 protocol 無效（必要欄位缺漏／解析失敗），或 R-4 identity 不符時 ＝ **blocked**，**非** advisory 放行。adapter **MUST**：`process_exit_code` 固定 `1`；**MUST NOT** 呼叫 P3 writer；輸出 stdout key `CORE_FINAL_STATUS=BLOCKED_ENV_MISMATCH` 與 `ADVISORY_ONLY=true`（AI-6 新增，`preflight_activation.sh:534`–`535`），另於 stderr 附人類可讀 `ADVISORY_ONLY` 警示行。（回鏈：AC「R-7」`INFRA-PREFLIGHT-CORE-01_...md:492`–`494`，AI-6 收尾五子句全達，`test_preflight_adapter.sh` 36/36 PASS，commit `28df403`／`5b5d294`）
5. **並行邊界**：同一 worktree 同時對多張 ticket 執行寫入型操作 **MUST** 禁止或序列化（票面非目標 §8 明文，`INFRA-PREFLIGHT-CORE-01_...md:459`）；preflight 本身**不提供**跨 worktree 鎖機制，屬既定 non-goal 而非缺陷——本票未新增鎖／佇列實作。

### 已知未達項（D.3-3 忠實性揭露，MUST 標 pending／planned）

| # | 事項 | 狀態 | 說明 |
| --- | --- | --- | --- |
| 1 | R-1 四型阻斷 | `pending`（adapter 層） | `blob_check`＝`MISMATCH`／`TARGET_MISSING`／`COMMAND_FAILED`／`INVALID_TICKET_VALUE` 四型於 `preflight_core.py::decide()`（核心層）皆已驗證阻斷（`test_preflight_core.py` `UnlistedCombinationTest`）；**adapter 層以真實 `active.json` mtime 不變驗證者僅 `MISMATCH` 一型**，其餘三型之 adapter 全流程重跑驗證為 `pending`。（AC `INFRA-PREFLIGHT-CORE-01_...md:475`，未勾） |
| 2 | 多 worktree 並行 | `pending`（未實測） | 見上方「R-4 四欄 identity binding」如實揭露；`repo_root`／`worktree_path` 現為同值，跨 worktree 隔離未實機驗證。**MUST NOT** 寫成「已支援 worktree 隔離」。（AC `:483`，未勾） |
| 3 | 四入口實機（`/resume`／新 session／切票／`post-compact`） | `planned` | 腳本本身二分支（顯式切票／取自 `active.json`）皆已真實執行，但未經真實 SessionStart hook 之 `startup`／`resume`／`compact`／`clear` matcher 觸發驗證。（AC `:486`，部分達成，未全勾） |
| 4 | 「新增一條規則」可維護性演練 | `planned` | 上方「決策表為規則 SSOT」條款所述之維護流程尚未實際演練一次（新增一列 `preflight_rules.json` 並同步測試）；**MUST NOT** 寫成已驗證之可維護性保證，僅為設計時契約。（AC `:490`，未執行） |

**另兩項如實描述義務**：

- `V2_STATE=` stdout 行之計算函式 `route_v2_state()` 屬 `LEGACY_STDOUT_SHIM`（AI-6 職責歸類表 `INFRA-PREFLIGHT-CORE-01_...md:410`，唯一消費點為診斷 echo），**MUST NOT** 寫成契約狀態或決策依據——真實決策路徑讀 `V2_OVERALL`／`V2_PROVENANCE`（經 `map_v2_facts()`），不讀 `route_v2_state()` 之收斂值。
- `v2_provenance=KNOWN` 與 `v2_semantics=FAILED` 為 schema 外 schema-outside 值，**未列入** `preflight_rules.json` 15 rows 決策表（R-6 producer closure，票面 AI-2 記載）；任何注入值一律落 R-2 `UNLISTED_COMBINATION`。**MUST NOT** 寫成「決策表已涵蓋全部 V2 語義」。

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
