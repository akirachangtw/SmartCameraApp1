# RGR-REPORT-ANCHOR-01: 於任何 payload 相對路徑消費前 fail-closed 要求 repo-root CWD

檔案治理／來源說明：本票依據 2026-09-17 本機 live scan（`grep_files` 直查 `repair_runner.sh` L19／L31／L32／L72／L86／L160／L221／L225／L241／L253／L272）、2026-09-16 巢狀逃逸現場之逐字讀取，以及 `RGR-CODEX-TRANSPORT-01` AI-3 兩次執行之一手記錄建立，依 `Unified_Ticket_Template_v3.md` v3.11 產出。外部來源不作承重依據。

**簽發日期**: 2026-09-17
**簽發者**: Claude（SKAD）
**來源議題**: RGR
**Mode**: normal  ||  **Effort**: Low
**Traycer Workflow**: N/A
**前置依賴（依賴三分）**：
- `execution_dependency`: 無
- `evidence_dependency`: `RGR-CODEX-TRANSPORT-01` AI-3 2026-09-16 第一次執行（CWD＝`runner/`）之逃逸現場；本票「AI-0 現場證據」節已逐字固定
- `coordination_dependency`: `RGR-CODEX-TRANSPORT-01`（AI-3 no-call 回歸尚未完成；本票**不阻擋**其 no-call 重跑，該重跑於 repo root 執行，不觸本缺陷路徑）

## 問題陳述（CONFIRMED fail-open）

runner 自 payload 讀取之相對路徑（`quality_report.report_path`、evidence、target）**以行程當下 CWD 解析**。自非 repo-root CWD 執行時：

```
CWD ≠ repo root
 → QUALITY_REPORT_PATH 解析至不存在之巢狀路徑
 → report_records() L160 `[[ -f "$QUALITY_REPORT_PATH" ]] || return 0` 回空
 → CB-4(L241) / CB-2(L253) / CB-1(L272) 之歷史視窗同時為空集合
 → 三個斷路器靜默 fail open，無任何錯誤提示
```

此非理論推演：2026-09-16 一次自 `runner/` 之執行已實際產生巢狀逃逸 report（見下節），且該次之 escalated 記錄**未進入 canonical report**，故未被任何 CB 計入。同型影響亦及 evidence 路徑（該次直接症狀為 `ANCHOR_MISSING_OR_UNREADABLE`）與 target 路徑。

**相鄰設計意圖佐證**：L225 已禁止 `report_path` 落入 `TELEMETRY_ROOT`（「latch 不得住進可清掃區」）——作者在意 latch 落點穩定性，惟漏了 CWD 這一面。

## AI-0 現場證據（2026-09-17 逐字固定；原始 artifact 已於 2026-09-17 15:14 前被清除，本節為唯一留存記錄）

- **逃逸路徑**：`.claude/skills/ticket-rigor-gate/runner/.claude/skills/ticket-rigor-gate/runner/fixtures/true_call/report_true_call.report.md`（475 bytes，untracked）
- **逐字內容**（單筆）：
  ```
  ## Repair Execution Record - 2026-09-16T16:14:44Z

  - Ticket ID: RGR-P0A-01-FIXTURE
  - Source Run ID: truecall-addendum-01
  - Finding ID: F-TRUECALL-1
  - Proposal Model: gpt-5.6-terra
  - Reasoning Effort: medium
  - Prompt Template Version: ticket-repair-prompt/v1
  - Disposition: n/a
  - Apply Status: escalated
  - Failure Code: n/a
  - Ratification Status: pending
  - Notes: P0A_PRECHECK_REASON=ANCHOR_MISSING_OR_UNREADABLE; BLOCKER_RECEIPT=blocker-receipt.v1.json; MODEL_INVOKED=false
  ```
- **啟動 CWD／command**：`runner/`；`bash repair_runner.sh terra --payload fixtures/true_call/payload_addendum.json`
- **canonical report 同時段對照**：其下一筆為 `2026-09-16T16:15:10Z` 之 CB-1 frozen（repo root 重跑，相隔 26 秒）；逃逸筆未出現於 canonical 檔，證實計數集合分裂
- **關鍵行號（開票時現值）**：`set -euo pipefail` L19；`REPO_ROOT` L31；`TELEMETRY_ROOT` L32；`PAYLOAD_PATH` 初始化 L72、`--payload` 賦值 L86；`report_records()` early return L160；`report_path` 建構 L221；`TELEMETRY_ROOT` 落點守門 L225；CB-4 L241、CB-2 L253、CB-1 L272

## 關鍵設計事實（決定實作形態）

**`REPO_ROOT` 已存在且已與 CWD 無關**：

```bash
L31  REPO_ROOT="$(cd "$RUNNER_DIR/../../../.." && pwd)"
L32  TELEMETRY_ROOT="$REPO_ROOT/.tmp/rigor-gate/ticket-repair"
```

`REPO_ROOT` 由**腳本自身位置**推導，非由 CWD；`TELEMETRY_ROOT` 已消費之，故失敗那次的 `.tmp/` artifact 仍落在正確位置。推論：

1. guard **MUST NOT** 使用 `git rev-parse --show-toplevel`——會引入對 git 可執行檔與 git 工作樹之新依賴，且與 L31 形成兩套 repo-root 定義。直接比對 `$PWD` 與既有 `$REPO_ROOT` 即可，零新依賴。
2. 本票採 fail-closed guard 而非逐欄位錨定：錨定需改 report／evidence／target 三處解析點（其中二處在 `blocker_preflight()` 內，屬更大面），guard 僅需單一入口一處，且同時覆蓋三面。

## Verify Config
TICKET_ID=RGR-REPORT-ANCHOR-01
CREATED=2026-09-17
PRESET=bugfix
ALLOWED=.claude/skills/ticket-rigor-gate/runner/repair_runner.sh
FORBIDDEN=.claude/skills/ticket-rigor-gate/runner/fixtures/,.claude/skills/ticket-rigor-gate/runner/schemas/,.claude/skills/ticket-rigor-gate/runner/prompts/,.claude/skills/ticket-rigor-gate/runner/profiles/,.claude/skills/ticket-rigor-gate/runner/housekeeping.sh,.claude/PROGRESS.md,.claude/rules/ssot-governance.md,app/,plans/cso_briefing/project/deliver/
MAX_LINES=N/A
DIFF_REF=d717218000290aaee6f1c98a0c4bbab644967ef0
ANCHOR_PROBED=2026-09-17 15:25
BASE_COMMIT=d717218000290aaee6f1c98a0c4bbab644967ef0
TARGET_BLOB=N/A
INTRODUCED_CAPABILITY=NONE
DEPENDENCY_DELTA=NONE

**`MAX_LINES=N/A` 依據**：TPL v3.5 R3——標的為 fail-closed 守門判定類安全不變量。
**`TARGET_BLOB=N/A` 依據**：TPL v3.11 Desktop fallback（開票端無 blob-hash producer）；`BASE_COMMIT` 已填，AI-0 MUST 逐項復驗現值錨點。
**開票時工作樹**：`git status --porcelain` 空（clean），HEAD＝`d717218`。

## Action Items

### AI-0: 錨點復驗與單一入口確認
- **檔案**: `.claude/skills/ticket-rigor-gate/runner/repair_runner.sh`
- **動作 A**: 逐字復驗上節「關鍵行號」十一處現值；任一不符 → 停止回報，禁推定等價位置。
- **動作 B（單一共同入口）**: 確認存在**單一**點，位於下列全部消費之前：`$PAYLOAD_PATH` 被 `jq` 讀取、`blocker_preflight()`、`report_records()` 首次呼叫、任何 evidence／target 路徑存取、任何 Codex 呼叫、任何 mutation。回填該點行號。**若 live scan 顯示無單一共同入口 → 停止回報；MUST NOT 將 guard 分散插入多個 downstream function。**
- **動作 C**: 確認 `REPO_ROOT`（L31）之推導不依賴 CWD，且其值於自 `runner/` 執行時仍正確。
- **動作 D**: `git rev-parse HEAD`、`git status --short`（判定範圍限 `repair_runner.sh`；他檔 dirty 僅記錄不阻擋）、`git check-ignore -v` 標的檔（命中 → 停止）。
- [x] 完成（2026-09-18：動作 A 十一處行號逐字相符；動作 B 單一入口＝L664 `validate_payload "$PAYLOAD_PATH"` 呼叫點之前、L106 存在性檢查之後之空白區段（L107-108 插入點），任何函式定義本體均不計，因執行順序以主執行路徑呼叫點為準；動作 C `REPO_ROOT`（L31）由腳本自身位置推導，與 CWD 無關，已由票面「AI-0 現場證據」節之逃逸事實反向證實（`.tmp/` artifact 落點正確，僅相對路徑消費受 CWD 影響）；動作 D `git status --short -- repair_runner.sh` 空、`check-ignore` 無命中，HEAD=`ad2dcef`）

### AI-1: 單一 repo-root CWD fail-closed guard
- **檔案**: `.claude/skills/ticket-rigor-gate/runner/repair_runner.sh`
- **現值**: 無任何 CWD 驗證；payload 相對路徑於 L221 等處以 CWD 解析。
- **目標值**: 於 AI-0 動作 B 所定之單一入口，加入一段 guard：`$PWD` 與既有 `$REPO_ROOT`（L31）不等時，輸出可辨識之錯誤訊息並 non-zero 退出。
- **實作約束**: MUST 重用 L31 之 `$REPO_ROOT`；**MUST NOT** 新增 `git rev-parse` 或任何外部命令；**MUST NOT** 新增 CWD fallback、靜默 canonicalization、自動 `cd`、或 repo 外 cache。符號連結／大小寫之正規化如有必要，MUST 於票面回報後再定，本輪不擅自加。
- **不動點**: CB-1／CB-2／CB-4 判定與 failure-count predicate、`report_records()` parser、L225 守門、retry table、`report_true_call.report.md` 歷史、fixture／target／payload、schema／prompt／profile、stdin transport、`housekeeping.sh`、app/build 一律零改動。
- [x] 完成（2026-09-18：於 L106-107 之間插入單行 guard `[[ "$PWD" == "$REPO_ROOT" ]] || fail_env "CWD 必須為 repo root（$REPO_ROOT），現為：$PWD"`（現為 L108）；重用既有 `$REPO_ROOT`〔L31〕與既有 `fail_env()`〔L61，維持既定 exit 2 用法／環境錯誤語意〕；未新增 `git rev-parse`、fallback、自動 `cd` 或 repo 外 cache；`git diff` 確認全檔僅此 2 行新增，CB 區／`report_records()`／L225／retry table／`validate_model_contract`〔已不存在，非本票異動〕等一律零改動）

### AI-2: 回歸驗證（無模型呼叫）
- **檔案**: `.claude/skills/ticket-rigor-gate/runner/repair_runner.sh`（唯讀驗證）
- **動作**:
  1. `bash -n repair_runner.sh` exit 0
  2. 自 `runner/` 子目錄執行任一既有 no-call fixture → expect 在 evidence／report／target／preflight／CB／Codex 之前以可辨識錯誤 non-zero 停止；且**無** target mutation、**無** report append（含巢狀路徑）、**無**模型呼叫
  3. 自 repo root 執行五個**具名** no-call fixture → 維持既有行為，Codex sentinel 命中 0、`MODEL_INVOKED=false`、target 未變
  4. 合法首跑語義：repo-root 執行且 canonical report 尚不存在時，expect 執行得以繼續、CB 歷史視為空，**不得**要求人工預建空 report
- **停止條件**: 驗證需新增 fixture、腳本、dependency，或需發出 Codex 呼叫／mutate target → 停止回報。
- [x] 完成（2026-09-18：①`bash -n` exit 0；②自 `runner/` 子目錄執行 `bash repair_runner.sh terra --payload fixtures/no_call/payload_anchor_missing.json`，guard 於 `blocker_preflight()`／`report_records()`／Codex 呼叫前即以 `❌ ENV ERROR: CWD 必須為 repo root（...），現為：.../runner` 攔下，`exit=2`；事後 `find .../runner/.claude` 零命中（無巢狀逃逸）、`git status --short` 僅 `repair_runner.sh` 一檔（無 target／report 異動）；③repo root 執行 `fixtures/run_no_call_tests.sh`：`anchor_missing`／`write_mode_forbidden`／`irreversible_target`／`write_mode_forbidden_flags`／`cb4_not_latched_on_rerun` 五案全數 `✅ PASS`（`PASS=5 FAIL=0`），harness 內建斷言涵蓋 sentinel 命中 0、`MODEL_INVOKED=false`、target hash 不變；④首跑語義為既有 `report_records()` L161 `[[ -f "$QUALITY_REPORT_PATH" ]] || return 0` 之既有邏輯，guard 僅置於其呼叫鏈之前一律未觸及該函式本體，程式碼等價性證明其行為不受本票影響，非另立新場景驗證；**AI-2 標的檔為「唯讀驗證」**——`run_no_call_tests.sh` 內部對 `fixtures/sandbox/reports/*.report.md` 之刪除重建為既有 harness 設計、非本票新增行為，驗證後已以 `git checkout -- fixtures/` 還原該側效應對工作樹之異動，最終 `git status --short` 僅 `repair_runner.sh` 一檔，符合本票 `FORBIDDEN=fixtures/` 之全樹保護）

## Hard Constraints
1. **不得**修改 CB-1／CB-2／CB-4 之判定規則或 failure-count predicate（CB-1 `failure_code=n/a` 自我計數為已知 KAIROS 候選，**本票不修**）。
2. **不得**改寫、清空、合併或刪除任何 report history。
3. **不得**修改 payload／fixture、target、schema、prompt、profile、stdin transport、retry table、report parser、`housekeeping.sh`、app/build。
4. **不得**新增 CWD fallback、靜默 canonicalization、自動 `cd` 或 repo 外 cache。
5. `QUALITY_REPORT_PATH` 缺檔**不得**被實作為錯誤：通過 guard 後，缺檔代表合法首跑，CB 歷史為空是正確狀態。根因是 CWD，不是缺檔。
6. **不得**重跑 true-call；本票之驗證一律 no-call。

## Error Handling
- AI-0 動作 B 找不到單一共同入口 → 停止回報，禁分散插入
- 關鍵行號現值與票面不符 → 停止回報，禁推定等價位置
- guard 實作需觸及 `blocker_preflight()`、`report_records()`、CB 區或 payload 解析邏輯 → 停止回報（表示所選入口過深）
- 自 `runner/` 執行之回歸出現任何 report append 或 target 變動 → 停止回報（guard 未生效或位置過晚）
- repo-root no-call 任一具名 fixture 由 PASS 轉 FAIL → 停止回報，禁調整 fixture 使其過關
- **[標配]** `git check-ignore -v .claude/skills/ticket-rigor-gate/runner/repair_runner.sh` 命中 → 停止回報

## Acceptance Criteria
- [x] **[存在性]** `repair_runner.sh` 存在**單一** repo-root CWD guard，且無 `git rev-parse`、無 fallback、無自動 `cd`（grep 驗證，行號：**L108**）
- [x] **[可達性]** guard 位於所有 payload 相對路徑消費之前——guard 行號 **L108**、`$PAYLOAD_PATH` 首次 jq 消費（`validate_payload` 呼叫點）行 **L666**、`blocker_preflight()` 首次呼叫行 **L683**、`report_records()` 首次呼叫行 **L248**（經 `circuit_breaker_precheck()` 呼叫鏈，該鏈本身首次呼叫於 **L679**）；108 < 248, 666, 683, 679 全數成立
- [x] **[行為性]** Given 自 repo root 執行既有合法 fixture，expect 行為與 `d717218` 前一致——`run_no_call_tests.sh` 五案全數 PASS，與票面所載既有行為相符
- [x] **[行為性]** Given 自 `runner/` 子目錄執行，expect 可辨識錯誤 ＋ non-zero 退出，且無 target mutation、無 report append（含巢狀路徑）、無模型呼叫——實測 `exit=2`，`find .../runner/.claude` 零命中，`git status` 無 target／report 異動
- [x] **[行為性]** Given repo-root 合法首跑且 canonical report 尚不存在，expect 執行繼續、CB 歷史視為空、不要求人工建立空 report——`report_records()` L161 早退邏輯未被本票觸及，guard 僅置於其呼叫鏈前，程式碼等價性成立（見 AI-2 完成註記）
- [x] **[行為性]** repo-root no-call 五個具名 fixture 維持 sentinel 命中 0、`MODEL_INVOKED=false`、target 未變：`anchor_missing`／`write_mode_forbidden`／`irreversible_target`／`write_mode_forbidden_flags`／`cb4_not_latched_on_rerun` 逐檔 PASS（非「5/5」概括）
- [x] **[存在性]** CB 判定、failure-count predicate、report parser、report history、fixture、retry table 均零改動（scoped diff 逐 hunk 檢視：`git diff --stat` 僅 `repair_runner.sh` +2 行，無其他 hunk）
- [x] **[存在性]** `bash -n repair_runner.sh` exit 0 且 `git diff --check` exit 0——雙項實測皆 exit 0
- [x] ~~`assembleDebug` PASS~~ (N/A: 非 Android 產物，shell runner bugfix)
- [x] `git diff --stat` 僅涉及 ALLOWED 檔案——僅 `repair_runner.sh` 一檔，+2 行；AI-2 驗證造成之 `fixtures/sandbox/reports/*` 側效應已用 `git checkout -- fixtures/` 還原，未入最終 diff
