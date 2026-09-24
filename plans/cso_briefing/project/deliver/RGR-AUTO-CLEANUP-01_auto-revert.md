# RGR-AUTO-CLEANUP-01: 撤回 RGR-AUTO-01 差分（commit 47813ac），Advisor Repair Mode legacy 回歸維持 PASS

**簽發日期**: 2026-09-23
**簽發者**: Claude（SKAD）
**Rev A**（2026-09-23，SPEC_DEFECT，Akira 裁定）：①AC1 字面檢查限三份 `.md` 與 fixtures 目錄；runner／schema／prompt／`run_no_call_tests.sh` 之乾淨度改由 AC2「與 `65fd8c4` 逐位元組相同」承載，避免要求無法證明來源之字面歸零而誤傷 legacy。②AC2 由三檔擴為五檔。③回歸紅燈禁 commit。4a 計數=1。
**來源議題**: RGR
**Mode**: normal  ||  **Effort**: Medium
**Traycer Workflow**: N/A
**前置依賴（依賴三分）**：
- `execution_dependency`: 無
- `evidence_dependency`: RGR-AUTO-01（VOID，2026-09-23 落冊）
- `coordination_dependency`: RGR-RESEARCH-SUBMIT-01（本票完成後收束落冊）

> **檔案治理/來源說明**：裁決來源＝`Skill-Advisor/Handoff/HANDOFF_2026-09-23-0000_RGR_Cleanup_Closure.md` K1（VOID）、K2（清除非休眠）、K3（清除範圍）、K4（Guardrail 2 裁決句）。開票期取證（Desktop，唯讀）：
> - AUTO-01 唯一實作 commit＝`47813ac`（45 檔，+1209/−5）；`f78337e` 為 PROGRESS 紀錄，**不撤**。
> - `47813ac..b39ebb4` 僅觸及 `REPAIR_ROUTING.md`、`SKILL.md` → revert 預期衝突面為此二檔（Handoff 稱三檔，`OPS_CALIBRATION.md` 實無後續 commit，預期乾淨撤回）。
> - **K4 已落地**：`REPAIR_ROUTING.md`「General Autonomous Repair Default」節末之「Calibration boundary」段（`b39ebb4`）語義等同 K4 裁決句，原「Open question, not resolved here」段已不存在。本票不再改寫，僅驗其於 revert 後存續。
> - 孤兒引用現值：`REPAIR_ROUTING.md` 同節「Scope」段含 `` `PERMITTED_REPAIR_CLASSES`, or the Single-Attachment Live Lane Carve-out (including any ticket, such as `RGR-AUTO-01`, that instantiates it)``。
> - **票面寫入權（2026-09-23，Akira 指定）**：本票票面唯一寫入端＝SKAD Desktop session。執行端（Claude Code）僅得勾選 AI／AC 核取框並附 receipt 證據；其餘任何票面修改需求 → 停止回報，由 SKAD 改寫。

## Verify Config
TICKET_ID=RGR-AUTO-CLEANUP-01
CREATED=2026-09-23
PRESET=refactor
ALLOWED=.claude/skills/ticket-rigor-gate/OPS_CALIBRATION.md,.claude/skills/ticket-rigor-gate/REPAIR_ROUTING.md,.claude/skills/ticket-rigor-gate/SKILL.md,.claude/skills/ticket-rigor-gate/runner/fixtures/rgr-auto/,.claude/skills/ticket-rigor-gate/runner/fixtures/run_no_call_tests.sh,.claude/skills/ticket-rigor-gate/runner/prompts/ticket-repair-prompt.v1.txt,.claude/skills/ticket-rigor-gate/runner/repair_runner.sh,.claude/skills/ticket-rigor-gate/runner/schemas/ticket-repair-payload.v1.json,.claude/skills/ticket-rigor-gate/runner/schemas/ticket-repair-response.v1.json
FORBIDDEN=.claude/PROGRESS.md,.claude/skills/ticket-rigor-gate/runner/fixtures/p0a02/,.claude/skills/ticket-rigor-gate/runner/fixtures/no_call/
MAX_LINES=N/A
DIFF_REF=b39ebb40eaa14154b2a39ed5e497e53d3cdd5e1d
ANCHOR_PROBED=2026-09-23（Desktop 開票期）
BASE_COMMIT=b39ebb40eaa14154b2a39ed5e497e53d3cdd5e1d
TARGET_BLOB=N/A（Desktop 無 blob-hash producer；AI-0 逐檔讀現值）
INTRODUCED_CAPABILITY=NONE
DEPENDENCY_DELTA=NONE

MAX_LINES=N/A 註：純撤回票，diff 規模由 `47813ac` 決定，非估算對象。

## Action Items

### AI-0: 現值確認（5 分鐘，資訊性）
- `git log -1 --format=%H 47813ac` 可解析；`git show --stat 47813ac` 檔數＝45。
- grep 五字面 `single_attachment_live_lane`／`PERMITTED_REPAIR_CLASSES`／`lane_entry_guard`／`rgr-auto`／`RGR-AUTO-01` 於 `.claude/skills/ticket-rigor-gate/` 取命中清單留作前值。
- 漂移依 TPL v3.11 三分流記錄後以工作樹現值續作。
- [x] 完成

### AI-1: 撤回 AUTO-01 差分
- `git revert --no-commit 47813ac`。
- 衝突僅以「保留 `b39ebb4` 內容、移除 `47813ac` 新增內容」解（`REPAIR_ROUTING.md`、`SKILL.md`）。
- `repair_runner.sh`、兩 schema、prompt、`run_no_call_tests.sh`：**回到 `47813ac` 前內容，禁刪檔**（K3：早於 AUTO-01 存在，Advisor Repair Mode 依賴）。
- `runner/fixtures/rgr-auto/**`：由 revert 整體移除。
- [x] 完成

### AI-2: 清孤兒引用＋K4 存續確認
- **檔案**: `.claude/skills/ticket-rigor-gate/REPAIR_ROUTING.md`（General Autonomous Repair Default → Scope 段）
- **現值**: `` `OPS_CALIBRATION.md`'s per-class calibration counters, `PERMITTED_REPAIR_CLASSES`, or the Single-Attachment Live Lane Carve-out (including any ticket, such as `RGR-AUTO-01`, that instantiates it).``
- **目標值**: `` or `OPS_CALIBRATION.md`'s per-class calibration counters.``（前一列舉項之逗號依英文語法調整）
- revert 後再 grep 五字面於 `OPS_CALIBRATION.md`、`REPAIR_ROUTING.md`、`SKILL.md`；三檔內其餘命中同法刪除（僅刪指向 AUTO-01 之片語，不改判準）。runner／schema／prompt／`run_no_call_tests.sh` 不做字面刪除，由 revert 回到 `65fd8c4` 基線（AC2）。
- 確認「Calibration boundary」段仍在（K4）。
- [x] 完成

### AI-3: 回歸＋提交＋垃圾清除
- 跑 `bash .claude/skills/ticket-rigor-gate/runner/fixtures/p0a02/run_preauth_matrix.sh` 與 `bash .claude/skills/ticket-rigor-gate/runner/fixtures/run_no_call_tests.sh`，輸出存入 receipt。
- 兩者 PASS → `git diff --name-only HEAD` 核 ⊆ ALLOWED → commit `[RGR-AUTO-CLEANUP-01] revert(ticket-rigor-gate): remove RGR-AUTO-01 live lane`。
- 刪除 `tmp/RGR-AUTO-01_bundle.patch`、`tmp/RGR-AUTO-01_pending_diffs.md`、`tmp/RGR-AUTO-01_scratch/`（gitignored，不入 diff、不入 ALLOWED 計數）。
- [x] 完成 — receipt: `tmp/RGR-AUTO-CLEANUP-01/evidence/ai3_regression_receipt.txt`（p0a02 8/8 PASS, no_call 5/5 PASS, exit=0/0）；commits `0adfa40` + `86ee90e`（後者修正首次 `git add` 因 rgr-auto/ pathspec 原子性失敗漏掉的 AI-2 orphan-phrase 修正）

## Hard Constraints
1. 禁 `git reset --hard`、廣泛 `git clean -fd`、改寫歷史、`push --force`。
2. 禁刪 `runner/repair_runner.sh` 及兩 schema 檔本體（K3）。
3. 禁改動 `b39ebb4` 引入之 General Autonomous Repair Default／Research Fallback 判準，AI-2 片語刪除除外。
4. 不修改 `RGR-RESEARCH-SUBMIT-01` 票檔（收束時於冊面記載其 L49/L87 對 Open question 之陳舊引用）。

## Error Handling
- 回歸任一 FAIL → **禁 commit**，停止回報（保留未提交工作樹）。得於報告標註為 pre-existing／unrelated，但紅燈狀態下不得建立 cleanup commit；如確屬既有紅燈，另開票處理或由 Akira 明示豁免。
- 衝突解決需改動 `b39ebb4` 判準文字方能成立 → 停止回報。
- `git diff --name-only` 超出 ALLOWED → 停止回報。
- **[標配]** `git check-ignore -v <ALLOWED 標的>` 命中 → 停止回報。本檢查僅作用於 ALLOWED 之 repo 路徑；`tmp/RGR-AUTO-01_*` 為預期 gitignored 清除標的，不在 ALLOWED、不納入本檢查。

## Acceptance Criteria
- [x] **[存在性]** `OPS_CALIBRATION.md`、`REPAIR_ROUTING.md`、`SKILL.md` 三檔內五字面 grep 命中 0（`single_attachment_live_lane`／`PERMITTED_REPAIR_CLASSES`／`lane_entry_guard`／`rgr-auto`／`RGR-AUTO-01`）；`runner/fixtures/rgr-auto/` 不存在。
- [x] **[存在性]** 下列五檔仍存在，且 `git diff 65fd8c4 HEAD -- <五檔>` 為空（＝回到 AUTO-01 前狀態，其內字面組成不另行判定）：`runner/repair_runner.sh`、`runner/schemas/ticket-repair-payload.v1.json`、`runner/schemas/ticket-repair-response.v1.json`、`runner/prompts/ticket-repair-prompt.v1.txt`、`runner/fixtures/run_no_call_tests.sh`。
- [x] **[存在性]** `REPAIR_ROUTING.md` 含「Calibration boundary」段與「## Research Fallback」節（K4 存續）。
- [x] **[行為性]** given 撤回後 runner，expect `p0a02/run_preauth_matrix.sh` 與 `run_no_call_tests.sh` 全 PASS（輸出附 receipt）— receipt: `tmp/RGR-AUTO-CLEANUP-01/evidence/ai3_regression_receipt.txt`，8/8 + 5/5 PASS
- [x] ~~assembleDebug PASS~~ (N/A: 無 app/ 變更)
- [x] `git diff --stat` 僅涉及 ALLOWED 檔案 — 人工逐檔核對 45/45 ⊆ ALLOWED（`verify_checklist.sh` Step 0 對 `rgr-auto/` 目錄前綴型 ALLOWED token 之 basename-only 誤判，Codex Cross-Review PASS + Structural Verify PASS(ADVISORY) 已交叉確認）


---

## Closeout（SKAD 補登 2026-09-24）

**Lifecycle**: CLOSED（`closed_at` 2026-09-23）　**Reviews**: sv=PASS、codex=PASS、qa=N_A、traycer=N_A

| 項目 | 結果 |
|---|---|
| Commits | `0adfa40`（主撤回，45 檔 -1210）、`86ee90e`（REPAIR_ROUTING 孤兒引用補 staging，1 行）、`552fd28`（GOVERNANCE-SYNC：checkbox＋票檔搬 deliver/） |
| AC1 | PASS — 三份 `.md` 五字面 grep 命中 0；`runner/fixtures/rgr-auto/` 不存在 |
| AC2 | PASS — 五檔（runner／兩 schema／prompt／`run_no_call_tests.sh`）對 `65fd8c4` diff 為空 |
| K4 | PASS — `REPAIR_ROUTING.md` 之「Calibration boundary」段與「## Research Fallback」節存續 |
| AC6 scope | PASS — `git diff 3ff2910..86ee90e` 共 45 檔，37 檔在 `rgr-auto/` 前綴下、8 檔為 ALLOWED 明列 |
| 回歸 | `p0a02/run_preauth_matrix.sh` PASS 8/8；`run_no_call_tests.sh` PASS 5/5；tracked side effects 已還原 |

**QA Step 0 覆寫裁決**：`verify_checklist.sh` 回報之 SCOPE VIOLATION 判定為 false positive（SKAD 以 `git diff` 逐檔核對推翻），本票 override Step 0，Auto-Chain 自 Step 1 續跑。真因非 basename-only，而是目錄型 token 未被識別為路徑前綴——已由 `RGR-VERIFY-CHECKLIST-01`（CLOSED）修復。

**證據等級揭露**：條件④「回歸須早於 `0adfa40` 執行」之原始 mtime 證據**不可複驗**（receipt 檔 mtime 15:10:37、`report_c1` 14:43:48，均晚於 commit 14:24:12，第一次跑動產物已被二次跑動覆寫），僅標 `SUPPORTED`。條件④改以**內容等價性**成立並標 `CONFIRMED`：`0adfa40..86ee90e` 僅改 `REPAIR_ROUTING.md` 一行文字，runner／schema／prompt／fixture 與 `65fd8c4` 逐位元組相同，故後跑測試驗到的是與 commit 前相同之可執行狀態。執行端已誠實揭露二次覆寫插曲，未以不可複驗之 mtime 充作硬證據。

**衍生票**：`RGR-VERIFY-CHECKLIST-01`（scope verifier 目錄型 token 缺陷）、`RGR-FIXTURE-REPORT-PATH-01`（回歸寫入版控路徑毀損時序證據）——二者均已 CLOSED。

**禁再修改**：本票及其兩筆 cleanup commit 不得再改動；任何 AUTO-01 遺留一律新票處理。
