# RGR-AUTONOMOUS-CLOSURE-01: 定義 review round 邊界並為自主修復立法 session-local closure loop（單次 execution 最多 3 次 iteration），取代「不得 second review pass」

**簽發日期**: 2026-09-24
**簽發者**: Claude（SKAD）
**來源議題**: RGR
**Mode**: normal  ||  **Effort**: Low
**Traycer Workflow**: N/A
**Revision**:
- Rev A（2026-09-24，SPEC_DEFECT，Akira 校正）：Rev 0 誤將 ticket-level「3 review rounds」改為 session-local「3 iterations」，混同兩層計數器。Rev A 改為兩層分立：`SKILL.md` 僅定義 round 邊界（L362 字面不改）；`REPAIR_ROUTING.md` 承載 closure loop。另補 HC「禁自動 VOID／reopen／改票」。
- Rev B（2026-09-24，SPEC_DEFECT，Akira 採納外部審查四項）：①AI-2b 目標值前兩句補 scope guard（same ticket scope＋既有 eligibility＋既有 Mechanical Verdict Rule），防 rerun 被讀成全 repo 重掃；②Error Handling 之 dogfood Critical 條件改依因果分流；③AI-3 內部順序寫死五步並禁「restore 後假裝未發生」；④AI-0 錨點不唯一改為停止回報（K5 治基線漂移，不治目標位置歧義）。駁回「刪 `git check-ignore` 標配」——屬 TPL v3.11 標配條款。**4a 計數＝2（門檻 3 未達）**。
**前置依賴（依賴三分）**：
- `execution_dependency`: 無
- `evidence_dependency`: RGR-RESEARCH-SUBMIT-01（CLOSED；General Autonomous Repair Default 與「second review pass」禁令之來源）、RGR-AUTO-CLEANUP-01（CLOSED；K1–K8 裁決載體）
- `coordination_dependency`: 無

> **檔案治理/來源說明**：規格來源＝Handoff 2026-09-23 K5／K8（精簡、三步上限）＋ Akira 2026-09-24 校正裁決（兩層計數器分立、freeze≠VOID、PASS 僅得自 rerun）。K6／K7 之「差分重審」概念由「每次 iteration 皆 rerun gate」取代（更硬、少一概念）；K7②「內部活動不增 round」保留於 round 邊界定義中。
> **MVP 價值自證**：`REPAIR_ROUTING.md` L119 禁 second review pass，`SKILL.md` L362 又以未定義之「review round」計 freeze——agent 自修後不知能否重審、重審是否吃掉 parent ticket 容額。本票以 ≤12 行補「round 邊界」與「session-local 上限」，消除歧義且不動既有 escalation 語意。
> 開票期一手取證（Desktop 唯讀，2026-09-24）：
> - `SKILL.md` L357「## Iteration And Escalation」；L361「If a ticket receives `FAIL`, revise the parent ticket and rerun the gate.」；L362「If the same parent ticket fails or remains partial for **3 review rounds**, freeze autonomous progression and escalate to L2 / designated human adjudication.」→ **L361／L362 一字不改**，僅於其前補 round 定義。
> - `REPAIR_ROUTING.md` L119「…a retry system, or a second review pass — none of that exists for this track…」→ 待取代之禁令字面；同段其餘七項否定保留。
> - `SKILL.md` L184「### Mechanical Verdict Rule」→ rerun gate 之 verdict 判準，引用不改。
> - 票面寫入權：SKAD Desktop 唯一；執行端僅勾選＋附 receipt。

## Verify Config
TICKET_ID=RGR-AUTONOMOUS-CLOSURE-01
CREATED=2026-09-24
PRESET=docs
ALLOWED=.claude/skills/ticket-rigor-gate/SKILL.md,.claude/skills/ticket-rigor-gate/REPAIR_ROUTING.md
FORBIDDEN=.claude/skills/ticket-rigor-gate/OPS_CALIBRATION.md,.claude/skills/ticket-rigor-gate/runner/,.claude/skills/ticket-rigor-gate/experience-cards/,.claude/skills/qa/,.claude/PROGRESS.md
MAX_LINES=N/A
DIFF_REF=1382bc6
ANCHOR_PROBED=2026-09-24（Desktop 開票期）
BASE_COMMIT=1382bc6f92a75239751d67fcb8be3bc96100c067
TARGET_BLOB=N/A（Desktop 無合法 blob-hash producer；AI-0 逐檔讀現值。依 K5，BASE_COMMIT 漂移不作停止事由）
INTRODUCED_CAPABILITY=NONE
DEPENDENCY_DELTA=NONE

MAX_LINES=N/A 註：docs preset；觸及 escalation 判定（守門類），依 TPL v3.5 R3 標 N/A。

## Action Items

### AI-0: 錨點現值讀取
- 確認 `SKILL.md` L357 節標題、L361／L362 兩條字面，與 `REPAIR_ROUTING.md` L119 含「second review pass」之句，各恰 1 處。
- 錨點內容不符但唯一 → 以工作樹現值續作並記錄（K5：Git 歷史非 gate 治基線漂移）。
- **錨點字面出現 >1 處（目標位置歧義）→ 停止回報**；K5 不適用於目標位置歧義，禁賭選位置。字面完全消失 → 停止回報。
- [x] 完成 — 三處錨點各恰 1 處，逐字核對相符

### AI-1: SKILL.md 定義 review round 邊界（2–4 行）
- **檔案**：`.claude/skills/ticket-rigor-gate/SKILL.md` → 「## Iteration And Escalation」節，插於 L361 之前
- **現值**：無 round 定義；L361／L362 直接引用「review rounds」。
- **目標值**（語意固定，措辭得微調）：
  > A review round is one completed gate execution that reaches a final PASS, PARTIAL, or FAIL disposition for the parent ticket. Internal repair, verification, and rerun activity performed within that same interactive execution does not create additional review rounds; only the final disposition of that execution is counted.
- **L361／L362 一字不改**。禁改「3 review rounds」為任何其他計數。
- [x] 完成 — round 定義插於 L359/L361 之間，L361/L362 逐位元組核對未變

### AI-2: REPAIR_ROUTING.md 立法 session-local closure loop 並取代禁令句
- **檔案**：`.claude/skills/ticket-rigor-gate/REPAIR_ROUTING.md`
- **步驟 a（L119 禁令句）**：「or a second review pass」改為「or an open-ended re-review」；同段其餘七項否定（Repair Ledger／queue／batch-approval／webhook／poller／notifier／retry）一字不動。
- **步驟 b（新增段，≤8 行，置於 General Autonomous Repair Default 節內、Calibration boundary 段之前）**，語意固定：
  > Within one interactive review execution, and only for findings that remain within the same ticket scope and satisfy the existing General Autonomous Repair Default eligibility, perform at most three autonomous repair iterations. Each iteration is repair → proportionate verification → rerun gate under the existing Mechanical Verdict Rule. PASS may be reported only from the rerun. If the third rerun is not PASS, stop autonomous repair and report all completed repairs, verification outcomes, remaining findings, the reason for non-convergence, and the final gate disposition. Do not void, reopen, or revise the ticket automatically. This loop is internal to a single review round as defined in `SKILL.md` → Iteration And Escalation; only the execution's final disposition counts toward the ticket-level three-round escalation.
- [x] 完成 — L119 禁令句改「an open-ended re-review」，其餘七項否定字面不變；closure loop 段插於 General Autonomous Repair Default 節內、Calibration boundary 段之前

### AI-3: dogfood 一次並提交（內部順序固定，禁跳步）
1. 以本票自身為對象跑一次 ticket-rigor-gate，依新規則執行（至多 3 次 iteration）。PASS 時 report 僅列最終 verdict＋未解 Critical／Major（應為空）；非 PASS 時依 AI-2b 之五項回報格式。
2. 核對 report 之 iteration 計數 ≤3，且 verdict 來源為 rerun gate 而非修復自述。
3. 跑 `git status --short`、`git diff --name-only`、`git diff --check`。
4. **三者全數通過**方得 stage 兩個 ALLOWED 檔案並 commit `[RGR-AUTONOMOUS-CLOSURE-01] docs(rigor-gate): define review round boundary and session-local closure loop`。
5. commit 後 read-back：`git show --name-only --stat HEAD`，附入 receipt。
- **dogfood 造成任何 ALLOWED 以外之 tracked diff，或產生未說明之 untracked output → 停止回報；不得 `git checkout --` 還原後當作未發生**（先例：RGR-AUTO-CLEANUP-01 之時序證據即遭還原動作洗除）。
- [x] 完成 — dogfood verdict PASS（iteration 0/3，report `.tmp/rigor-gate/RGR_RGR-AUTONOMOUS-CLOSURE-01_20260924.md`）；commit `a8435af`（read-back 恰 2 檔）；worktree 另有 3 份外部 SKAD 寫入（非本 dogfood 造成，已揭露排除，未觸碰未還原）

## Hard Constraints
1. **禁改 `SKILL.md` L361／L362 字面**；禁改跨 execution 之「3 review rounds → freeze＋L2」語意或計數。
2. 禁新增 Repair Ledger 義務、queue、batch-approval、webhook、poller、notifier、retry system。
3. **禁任何自動 VOID／reopen／改票／拆票／另開票之規則**——freeze 僅停止自主推進，lifecycle 終態一律人工裁決。
4. 禁改 Mechanical Verdict Rule 本體；禁觸碰 `OPS_CALIBRATION.md`、runner、experience-cards。
5. session-local 上限固定 3，禁參數化、禁依票級別浮動。
6. AI 總數 ≤3（K8）；需第四步 → 停止回報。

## Error Handling
- AI-0 字面錨點完全消失 → 停止回報。
- 新增文字需改動 L361／L362 或 Mechanical Verdict Rule 方能自洽 → 停止回報。
- dogfood 發現**由本票變更引入**之 Critical，或 closure loop 違反 ALLOWED／eligibility／scope／三次上限／lifecycle hard constraint → 停止，不 commit，附五項回報。
- dogfood 發現**既有且與本票無因果關係**之 Critical → 依既有 route 回報，**不作本票 blocker**，且**禁由本票自修**（scope 外）。
- `git diff --name-only` 超出 ALLOWED → 停止回報。
- **[標配]** `git check-ignore -v <ALLOWED 標的>` 命中 → 停止回報。

## Acceptance Criteria
- [x] **[存在性]** `SKILL.md` Iteration And Escalation 節含 review round 定義（final disposition 計一輪、內部 repair／verify／rerun 不增 round），且 L361／L362 字面與 `1382bc6` 完全相同。
- [x] **[存在性]** `REPAIR_ROUTING.md` grep「second review pass」命中 0；含 closure loop 段，且該段明載：same ticket scope＋既有 eligibility 之限定、rerun gate 依既有 Mechanical Verdict Rule、至多三次 iteration、PASS 僅得自 rerun、第三次非 PASS 即停並五項回報、禁自動 void／reopen／revise、明示 internal to a single review round；L119 段其餘七項否定不變。
- [x] **[行為性]** given dogfood 執行，expect report 標示 iteration 次數 ≤3，且 PASS（若 PASS）之 verdict 來自 rerun gate 而非修復自述。— PASS，iteration=0/3，見 `.tmp/rigor-gate/RGR_RGR-AUTONOMOUS-CLOSURE-01_20260924.md`
- [x] **[行為性]** given dogfood 完成，expect `git status --short` 無 ALLOWED 以外之 tracked diff 且無未說明 untracked output；commit 後 `git show --name-only --stat HEAD` 之檔案清單恰為兩個 ALLOWED 檔（receipt 附輸出）。— PASS，`git show --name-only --stat a8435af` 恰列 2 檔；worktree 3 份外部 SKAD 檔非 ALLOWED、非本票 diff 範圍，已揭露排除
- [x] ~~assembleDebug PASS~~ (N/A: docs preset)
- [x] `git diff --stat` 僅涉及 ALLOWED 檔案 — `git diff --stat 1382bc6..a8435af` 恰 2 檔

## Receipt: SV/Cross-Review
```
Codex Cross-Review: PASS（REACHABILITY: NONE），Scope=1382bc6..a8435af（2 輪 REPORT_FIX 皆措辭澄清）
Structural Verify: PASS (A/B/C/D/E/F/G) [ADVISORY — PROGRESS.md 78/80 行，非阻斷]
Dogfood Gate: PASS，Critical=0/Major=0/Minor=1，iteration 0/3
```
