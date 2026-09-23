# RGR-FIXTURE-REPORT-PATH-01: 將 fixture payload 之 quality_report.report_path 移出版控路徑，止住回歸測試對工作樹的污染

**簽發日期**: 2026-09-23
**簽發者**: Claude（SKAD）
**來源議題**: RGR
**Mode**: normal  ||  **Effort**: Low
**Traycer Workflow**: N/A
**前置依賴（依賴三分）**：
- `execution_dependency`: RGR-VERIFY-CHECKLIST-01（Akira 2026-09-23 裁定執行順序固定：先修 scope verifier，本票 Step 0 方不被已知誤判阻斷；仍禁併批 commit）
- `evidence_dependency`: RGR-AUTO-CLEANUP-01（CLOSED；其 receipt 因本缺陷被覆寫，為正向事件證據）
- `coordination_dependency`: 無

> **檔案治理/來源說明**：裁決來源＝Akira 2026-09-23：reports 為 runner 必要 runtime state，**不得整體刪除**；移除的是 Git-tracked fixture output，非 runner 之 latch／讀取能力。開票期一手取證（Desktop 唯讀）：
> - **保留理由（CONFIRMED）**：`repair_runner.sh` L162／L238 `[[ -f "$QUALITY_REPORT_PATH" ]]` 後以 `report_records()` 回讀，驅動 CB-1～CB-4 停損（L251/L266/L292/L299）；`run_no_call_tests.sh` L69–74 對 report 內容做 `P0A_PRECHECK_REASON`／`MODEL_INVOKED=false`／`CB-4` 三項斷言。刪除等同拆除停損。
> - **缺陷（CONFIRMED）**：`fixtures/p0a02/payloads/payload_c1.json` L33 等將 `report_path` 指向 `…/fixtures/p0a02/reports/…`，該路徑受 Git 追蹤 → 每跑必污染工作樹，須 `git checkout --` 還原，並曾因此毀損 CLEANUP-01 之時序證據。
> - **落點邊界（CONFIRMED）**：`repair_runner.sh` L32 `TELEMETRY_ROOT="$REPO_ROOT/.tmp/rigor-gate/ticket-repair"`；L225–227 RTR-001 禁 report 落於 TELEMETRY_ROOT 內（latch 不得住進可清掃區）。`housekeeping.sh` 之 `rm -rf` 僅作用於 `$ticket_dir`（TELEMETRY_ROOT 下之 ticket 子樹），不及其兄弟目錄。`.gitignore` L55 已含 `.tmp/`。
> - **據此之候選 state root**：`.tmp/rigor-gate/fixture-latch/`——gitignored、不在 TELEMETRY_ROOT 內、不被 housekeeping 清掃。AI-0 MUST 實證後方可採用，禁憑名稱假設。
> - 票面寫入權：SKAD Desktop 唯一；執行端僅勾選＋附 receipt。

## Verify Config
TICKET_ID=RGR-FIXTURE-REPORT-PATH-01
CREATED=2026-09-23
PRESET=bugfix
ALLOWED=.claude/skills/ticket-rigor-gate/runner/fixtures/p0a02/payloads/,.claude/skills/ticket-rigor-gate/runner/fixtures/no_call/,.claude/skills/ticket-rigor-gate/runner/fixtures/true_call/,.claude/skills/ticket-rigor-gate/runner/fixtures/sandbox/,.claude/skills/ticket-rigor-gate/runner/fixtures/p0a02/reports/
FORBIDDEN=.claude/skills/ticket-rigor-gate/runner/repair_runner.sh,.claude/skills/ticket-rigor-gate/runner/schemas/,.claude/skills/qa/scripts/verify_checklist.sh,.claude/PROGRESS.md
MAX_LINES=N/A
DIFF_REF=552fd28
ANCHOR_PROBED=2026-09-23（Desktop 開票期）
BASE_COMMIT=552fd28
TARGET_BLOB=N/A（Desktop 無 blob-hash producer；AI-0 逐檔讀現值）
INTRODUCED_CAPABILITY=NONE
DEPENDENCY_DELTA=NONE

MAX_LINES=N/A 註：刪除既有 tracked report 之行數由現況決定，非估算對象。

## Action Items

### AI-0: state root 實證（不得憑名稱假設）
- 列出所有 payload fixture 內 `quality_report.report_path` 之現值與檔案清單（`grep -rn 'report_path' fixtures/`）。
- 對候選 `.tmp/rigor-gate/fixture-latch/`：①`git check-ignore -v` MUST 命中（已 ignore）；②`realpath -m` 後 MUST NOT 落於 `$REPO_ROOT/.tmp/rigor-gate/ticket-repair` 之下（否則觸 RTR-001）；③逐行核 `housekeeping.sh` 之刪除標的不及此路徑。
- 三項任一不成立 → 停止回報，附實證，由 SKAD 另擇落點。
- [x] 完成 — 三條件皆 PASS：①`git check-ignore -v` 命中（`.gitignore:55`）②`.tmp/rigor-gate/fixture-latch` 與 `.tmp/rigor-gate/ticket-repair` 為 sibling，非巢狀 ③`housekeeping.sh` 刪除候選僅在 `TELEMETRY_ROOT_ABS`（即 `ticket-repair`）之下逐一 realpath 核驗，不及其 sibling

### AI-1: 改寫 fixture payload 之 report_path
- **檔案**: 上述 AI-0 清單所列之 payload fixture（`p0a02/payloads/`、`no_call/`、`true_call/`、`sandbox/` 內）
- **現值**: `"report_path": ".claude/skills/ticket-rigor-gate/runner/fixtures/<...>/reports/<name>.report.md"`
- **目標值**: `"report_path": ".tmp/rigor-gate/fixture-latch/<同檔名>"`（或 AI-0 實證通過之替代 root）
- 僅改此一欄位；payload 其餘欄位、schema、runner 一律不動。
- [x] 完成 — 14 個 payload 檔（p0a02×8、no_call×4、true_call×2）逐一改寫，`git diff --stat` 確認每檔僅 1 行變更

### AI-2: 移除已追蹤之 report 產物
- `git rm` 掉 `fixtures/p0a02/reports/` 與 `fixtures/sandbox/reports/` 下由測試產生之 tracked report 檔。
- 若某 report 檔為測試**輸入**（非輸出）而被斷言讀取 → 停止回報，不得刪除。
- [x] 完成 — 12 檔皆為 runner 執行前 `rm -f` 清空之純輸出（`run_no_call_tests.sh:27`、`run_preauth_matrix.sh:46`），非輸入 fixture，已 `git rm` 移除

### AI-3: 回歸與提交
- 跑 `p0a02/run_preauth_matrix.sh` 與 `run_no_call_tests.sh`，附完整 stdout/stderr receipt。
- 兩者 PASS **且** 跑完後 `git status --short` 不得出現任何 reports 相關異動（本票之核心行為驗收）。
- `git diff --name-only HEAD` ⊆ ALLOWED → commit `[RGR-FIXTURE-REPORT-PATH-01] fix(rigor-gate): move fixture quality report latch out of VCS`。
- 本票單獨 commit，禁與 RGR-VERIFY-CHECKLIST-01 併批。
- [x] 完成 — commit `de6072d`（獨立，未與 VERIFY-CHECKLIST-01 併批）；commit 前與 commit 後各跑一輪回歸皆 8/8+5/5 PASS，兩輪 `git status --short` 均零 reports 異動

## Hard Constraints
1. **禁刪除或改動 report 的產生／讀取能力**：`append_report_entry()`、`report_records()`、CB-1～CB-4 判定、RTR-001 檢查一律不得觸碰。
2. 禁改 `repair_runner.sh` 與兩 schema（已列 FORBIDDEN）。
3. 禁將 report 落於 `TELEMETRY_ROOT` 內或任何受 housekeeping 清掃之路徑。
4. 禁 `git reset --hard`、`clean -fd`、改寫歷史；禁與另票合併 commit。

## Error Handling
- AI-0 三項實證任一不成立 → 停止回報。
- 任一回歸 FAIL → 禁 commit，停止回報（得標註 pre-existing，另票處理）。
- 跑完後 `git status --short` 仍見 reports 異動 → 停止回報（表示尚有未改到的 payload）。
- 需改動 runner 或 schema 方能成立 → 停止回報。
- **[標配]** 對 ALLOWED 內預計提交之 repo 路徑執行 `git check-ignore -v` 命中 → 停止回報（新 latch 路徑為預期 gitignored 標的，不納入本檢查）。

## Acceptance Criteria
- [x] **[存在性]** `grep -rn 'reports/' fixtures/*/payloads/ fixtures/no_call/ fixtures/true_call/ fixtures/sandbox/` 對版控 reports 路徑命中 0。
- [x] **[存在性]** `repair_runner.sh` 與兩 schema 之 `git diff 552fd28 HEAD` 為空（能力未被動到）。
- [x] **[行為性]** given 改寫後 fixture，expect 兩支回歸全 PASS（附 receipt），且執行後 `git status --short` 無 reports 異動。
- [x] **[行為性]** given 連跑兩次，expect CB 相關案例（`cb4_not_latched_on_rerun`）行為不變 — commit 前後各跑一輪，`cb4_not_latched_on_rerun` 兩輪皆 PASS。
- [x] ~~assembleDebug PASS~~ (N/A: 無 app/ 變更)
- [x] `git diff --stat` 僅涉及 ALLOWED 檔案 — 26 檔全落 ALLOWED（`p0a02/payloads/`×8、`no_call/`×4、`true_call/`×2、`p0a02/reports/`×8 刪除、`sandbox/reports/`×4 刪除）

## Receipt: 回歸完整輸出（commit 前，commit de6072d）
```
p0a02/run_preauth_matrix.sh: PASS=8 FAIL=0（c1_success_applied, c2_preimage_mismatch_rejected_before_rtr012, c3_ref_missing_selfreport_true_override_false, c3b_ref_missing_selfreport_false_not_triggered, c4_legacy_v1_payload_regression, c5_boundary_outside_fixture_root_fail_closed, c5b_sibling_prefix_boundary_rejected, c6_override_hit_marker_reject_no_leak）
run_no_call_tests.sh: PASS=5 FAIL=0（anchor_missing, write_mode_forbidden, irreversible_target, write_mode_forbidden_flags, cb4_not_latched_on_rerun rerun）
git status --short（跑後）：無 reports 相關異動
```

## Receipt: 回歸完整輸出（commit 後，HEAD=de6072d）
```
p0a02/run_preauth_matrix.sh: PASS=8 FAIL=0（同上 8 案）
run_no_call_tests.sh: PASS=5 FAIL=0（同上 5 案）
git status --short（跑後）：無 reports 相關異動；.tmp/rigor-gate/fixture-latch/ 下 12 個 report 檔（gitignored，不入 status）
```
