# RGR-INTERACTIVE-CLOSURE-AUTH-01: 讓 direct interactive `/ticket-rigor-gate @<ticket>` 在 TICKET_RIGOR 期間自主修訂被審票並收斂，消除「已登記即停」與「無授予即停」兩個停點

**簽發日期**: 2026-09-25
**簽發者**: Claude（SKAD）
**來源議題**: RGR
**Mode**: normal  ||  **Effort**: Medium
**Traycer Workflow**: N/A
**前置依賴（依賴三分）**：
- `execution_dependency`: 無
- `evidence_dependency`: RGR-AUTONOMOUS-CLOSURE-01（CLOSED；session-local closure loop 與 round 邊界之既有立法，本票不改其語意）、RGR-DISPATCH-AUTH-01（CLOSED；預授權條件② 之來源，本票 MUST NOT 削弱）、RGR-RESEARCH-SUBMIT-01（CLOSED；General Autonomous Repair Default 四條件與 B-route 之來源）
- `coordination_dependency`: 無

> **檔案治理/來源說明**：裁決來源＝Akira 2026-09-25 四輪審查收斂（Perplexity 規格 `Skill-Advisor/deliver/ticket-rigor-gate-skill/RGR_autonomous_closure_dogfood_spec.md` → SKAD 審查 → 外部審查兩輪 → SKAD 四點調整，Akira「同上述建議」）。**本票承接 CLOSURE-01 未交付之三類缺口**：`POLICY_GAP`（Guardrail 1 登記即出修復範圍）、`INVOCATION_REACHABILITY_GAP`（附掛票檔未被授予為 mutation target）、`ACCEPTANCE_COVERAGE_GAP`（CLOSURE-01 dogfood 以自身 docs 票驗證，iteration=0，未證明 mutation 真的發生）。**不得重用、重開或修改 CLOSED 之 RGR-AUTONOMOUS-CLOSURE-01**。
> **明確不承諾**：所有已登記票可自修；`DRAFT_NEEDED` 狀態之自修；research 結果回寫或授權票面修正；runner／schema／fixture 回歸方案；synthetic dogfood；未來所有 SKAD 新票一律先進 `TICKET_RIGOR`（屬本票驗收後之 operating-policy 決定，不由本票寫入）。
> 開票期一手取證（Desktop 唯讀，2026-09-25，HEAD `806cd0d`，工作樹乾淨）：
> - `REPAIR_ROUTING.md` L26「### Guardrails」；L30「1. **Pre-signature only.** Repairs apply to drafts and unregistered tickets. Once a ticket is in the registry it leaves repair range; … The single narrow exception is the **Post-Signature Erratum Lane**…」→ 本票**不改寫此句**，僅於其後追加一句窄化例外。
> - `REPAIR_ROUTING.md` L38「### Dispatch Authority — Preauthorized Autonomous Apply」；L45 條件②「…never the body of another already-signed ticket — preauthorization scopes only the declaring ticket's own target, never a write into a different, already-registered ticket's text.」→ **逐字不動**（HC-10）。
> - `REPAIR_ROUTING.md` L65「## Post-Signature Erratum Lane」至 L105（L106「## General Autonomous Repair Default」前）→ **整節逐字不動**（HC-11）。
> - `REPAIR_ROUTING.md` L106「## General Autonomous Repair Default」；L114 條件①（already-granted modification permission）；L121「**When any condition fails.**」；L123「**Session-local closure loop.**」。
> - `SKILL.md` L69「## Inputs」→ `@<ticket>` 語意落點；L357「## Iteration And Escalation」L361 round 定義（CLOSURE-01 立法，不改）；L390「## Required Output」／L400「### 2. Findings Summary」→ 八欄落點；L470「### 11. Gate Decision」。
> - 全冊取號：活躍冊最大 seq 16（RGR-AUTONOMOUS-CLOSURE-01）；封存冊含 RGR-APPDEPLOY-01／RGR-P0A-02／RGR-SCOPE-BIND-01／RGR-AUTO-01，無更高序號；本票 seq 17，ID 無碰撞。
> - 票面寫入權：本票自身依既有流程——SKAD Desktop 唯一寫入端，直接設 `PENDING`，不以尚未存在之 authority 自修。

## Verify Config
TICKET_ID=RGR-INTERACTIVE-CLOSURE-AUTH-01
CREATED=2026-09-25
PRESET=docs
ALLOWED=.claude/skills/ticket-rigor-gate/REPAIR_ROUTING.md,.claude/skills/ticket-rigor-gate/SKILL.md
FORBIDDEN=.claude/skills/ticket-rigor-gate/OPS_CALIBRATION.md,.claude/skills/ticket-rigor-gate/EXAMPLE.md,.claude/skills/ticket-rigor-gate/runner/,.claude/skills/ticket-rigor-gate/experience-cards/,.claude/skills/qa/,plans/cso_briefing/project/TICKET_REGISTRY.json,app/,.claude/PROGRESS.md
MAX_LINES=N/A
DIFF_REF=806cd0d
ANCHOR_PROBED=2026-09-25（Desktop 開票期）
BASE_COMMIT=806cd0dfd73d1d17d1175a4f6dfa8ac675e6a821
TARGET_BLOB=N/A（Desktop 無合法 blob-hash producer；AI-0 逐檔讀現值）
INTRODUCED_CAPABILITY=skill:attached_ticket_repair_authority
DEPENDENCY_DELTA=NONE

MAX_LINES=N/A 註：AI 含授權判定類安全不變量（lifecycle 閘、跨票禁止、併發保護），依 TPL v3.5 R3 標 N/A；失敗形狀枚舉為審查發現式。

**`INTRODUCED_CAPABILITY` 誠實揭露**：本票引入新 authority 模式 `skill:attached_ticket_repair_authority`，固定 corpus 無字面命中；依票面自建六款硬風險判準逐一自我審視（不再引用外部「外審條件 6a」編號——該編號出處於本票開票期一手可讀範圍內〔含 `.claude/rules/cross-review.md` 全文〕未見對應治理文件，經 Ticket Rigor Gate 審查標為 Missing Canonical Source，本次降級為票面自足判準，不主張其對應任何可考編號）：①無官方 contract 禁止；②不涉 provider request 格式；③不新增網路面，且以 HC-10／HC-12 **收緊**而非放寬既有跨票邊界；④寫入限單一 ticket 檔、git 可回退，非不可逆；⑤零費用；⑥無 bounded probe 失敗。**未命中任一款 → 不觸硬風險，免凍結**。票面如實揭露，不代為宣稱免審。

## Action Items

### AI-0: 錨點讀取＋live micro-dogfood target 唯讀 pre-scan
**A. 錨點**：確認上列 `REPAIR_ROUTING.md` L30／L45／L65–L105／L114／L121／L123 與 `SKILL.md` L69／L361／L363–364／L400 字面各恰 1 處。**錨點字面出現 >1 處 → 停止回報**（目標位置歧義，K5 不適用）；內容漂移但唯一 → 以工作樹現值續作並記錄；字面完全消失 → 停止回報。另記錄 L45 條件② 與 L65–L105 整節之 git 內容快照，供 AC 逐字比對。
**B. dogfood target pre-scan**（唯讀，逐步）：
1. 以冊面查詢定位一張 `lifecycle=TICKET_RIGOR` 之候選票。
2. 只讀該候選票檔。
3. 指認**恰一筆**候選 ticket-text correction。
4. 記錄：ticket ID 與檔案路徑；lifecycle 與冊面紀錄；證據來源 path＋line range；現行票面字面；可唯一導出之替換值；驗證方法；確認不涉 scope／product／policy／lifecycle／SSOT／public contract／design 決策。
5. **不修改任何檔案。**
6. **不 invoke gate。**
7. **不計入 session-local cycle。**
**C. 候選票資格**（全部成立方可採用）：①有**獨立於本票 dogfood 之真實工作目的**（即使本票不存在也會被派工）；②該筆 finding 係起草時**自然產生**，**禁為製造 eligible finding 而刻意植入錯誤**；③非 CLOSED／PENDING／真實交付中之 production 票；④無未歸屬工作樹異動。
**D.** 任一步驟或資格不成立 → 輸出 `LIVE_MICRO_DOGFOOD_TARGET_UNAVAILABLE`，AI-1／AI-2 照常實作，AI-3 改記錄該狀態。
- [x] 完成（錨點六處 REPAIR_ROUTING.md／四處 SKILL.md 逐一 grep 確認恰 1 處且內容相符；`registry_list(lifecycle=TICKET_RIGOR)` 全冊僅本票自身命中，無合格外部候選 → `LIVE_MICRO_DOGFOOD_TARGET_UNAVAILABLE`）

### AI-1: REPAIR_ROUTING.md 立法 lifecycle 閘、附掛票授權、不短路與併發保護
- **檔案**：`.claude/skills/ticket-rigor-gate/REPAIR_ROUTING.md`
- **a（L30 Guardrail 1 之後追加一句，原句一字不改）**，語意固定：
  > Direct interactive current-ticket exception: for the sole purpose of the General Autonomous Repair Default's direct interactive route, registry membership does not by itself place the attached ticket outside repair range while its lifecycle is exactly `TICKET_RIGOR`. This exception does not alter the meaning of "signed", "registered", "dispatched", or any authority rule outside that route.
- **b（General Autonomous Repair Default 節，條件①之後新增段，≤12 行）**，語意固定：
  > **Attached-ticket authority.** In a direct interactive invocation `/ticket-rigor-gate @<ticket-path>`, the exact attached ticket file is the sole current-ticket mutation target and counts as already-granted scope under condition 1 — only while its registry lifecycle is exactly `TICKET_RIGOR`, and only for repairs that also satisfy conditions 2–4. It grants no authority over any other ticket, the registry, this skill, runner, schemas, fixtures, production code, lifecycle, or any public contract.
  > Lifecycle states: `DRAFT_NEEDED` — read and pre-scan only, no autonomous mutation. `TICKET_RIGOR` — the only state in which this authority applies. `PENDING`, `IN_PROGRESS`, `EXECUTED`, `REVIEW`, `CLOSED`, `BLOCKED`, `VOID` — this authority does not apply; any mutation follows the existing ticket and dispatch authority law.
  > **Concurrency.** At the start of the execution, record the attached ticket file's size and content hash; re-check both before every mutation. On any mismatch, ownership ambiguity, or target drift, stop before mutating and report `CONCURRENT_TICKET_WRITE`. No other writer may mutate the same ticket file while the execution is active; this does not otherwise redefine human or SKAD authority.
- **c（L121「When any condition fails」段末追加一句）**：
  > A finding that fails a condition does not stop processing of independently eligible current-ticket findings in the same cycle; escalation occurs only after every independently eligible repair has been attempted, verified, and reassessed, or when the three-cycle budget is exhausted.
- **L45 條件② 與 L65–L105 整節一字不動。**
- [x] 完成（commit `a65b0bc`；`git diff` hunk 未觸及 L45 條件②與 Erratum Lane 整節，HC-10／HC-11 通過）

### AI-2: SKILL.md 補 invocation 語意與八欄 closeout
- **檔案**：`.claude/skills/ticket-rigor-gate/SKILL.md`
- **a（L69「## Inputs」節內新增 ≤4 行）**：`/ticket-rigor-gate @<ticket-path>` 之附掛票檔，於其 lifecycle 為 `TICKET_RIGOR` 時為本次 execution 之唯一 mutation target；授權條件與邊界見 `REPAIR_ROUTING.md` → General Autonomous Repair Default → Attached-ticket authority。
- **b（L400「### 2. Findings Summary」節末新增）**：每筆 finding MUST 附八欄 disposition：
  ```text
  FINDING_ID=
  SEVERITY=
  ROUTE=A_AUTO_REPAIR|B_RESEARCH_SUBMITTED|C_EXCEPTION
  AUTO_REPAIR_ATTEMPTED=true|false
  RUNNER_DISPATCHED=true|false
  REASON_CODES=
  DECISION_QUESTION=
  NEXT_ACTION=
  ```
  一致性規則三條：`AUTO_REPAIR_ATTEMPTED=false` 時 `REASON_CODES` MUST 非空；`DECISION_QUESTION` 非空時 MUST 為具體可回答之問題（「請 Akira 裁決」單句無效）；direct interactive 修訂時 `RUNNER_DISPATCHED=false`，MUST NOT 被表述為 runner 或 runtime failure。
- **c**：session-local 迴圈之每輪以 `## Session-local Closure Cycle N of 3` heading 標示，不另增第九欄。
- **L361 round 定義與 L363–364 escalation 規則一字不動。**
- [x] 完成（commit `a65b0bc`；L361／L363–364 未觸及）

### AI-3: live micro-dogfood（或記錄 UNAVAILABLE）並提交（內部順序固定）
1. **若 AI-0 取得合格 target**：對其執行 `/ticket-rigor-gate @<target>`，依新規則至多 3 cycle；**若為 UNAVAILABLE**：跳至步驟 3，receipt 載明 `LIVE_MICRO_DOGFOOD_TARGET_UNAVAILABLE`。
2. 核對 report：iteration 計數 ≤3；AI-0 記錄之候選 finding 實際回寫；每筆 finding 附八欄；C_EXCEPTION 未短路其他 A 修復。
3. 跑 `git status --short`、`git diff --name-only`、`git diff --check`。
4. **三者全通過**方得 stage **本票兩個 ALLOWED 檔**並 commit `[RGR-INTERACTIVE-CLOSURE-AUTH-01] docs(rigor-gate): lifecycle-gated attached-ticket repair authority`。dogfood target 票檔之異動**不併入本票 commit**，由該票自身流程處理。
5. commit 後 read-back：`git show --name-only --stat HEAD`，附入 receipt。
- dogfood 造成 ALLOWED 與 target 票檔以外之 tracked diff，或未說明之 untracked output → 停止回報；**不得 `git checkout --` 還原後當作未發生**。
- [x] 完成（AI-0=UNAVAILABLE，跳至步驟 3；三項機械檢查全通過；commit `a65b0bc`；read-back `git show --name-only --stat HEAD` 確認僅 2 個 ALLOWED 檔）

## Hard Constraints
- **HC-1**：MUST NOT 重用、重開、修改或將 CLOSED 之 RGR-AUTONOMOUS-CLOSURE-01 視為本票實作載體。
- **HC-2**：本票授予之 direct interactive 自主修訂權僅於 `TICKET_RIGOR` 生效；`DRAFT_NEEDED` 僅讀／pre-scan；`PENDING`／`IN_PROGRESS`／`EXECUTED`／`REVIEW`／`CLOSED`／`BLOCKED`／`VOID` 不授予。**MUST NOT 以順序比較（如 `>= PENDING`）表述**，一律明列。
- **HC-3**：`/ticket-rigor-gate @<ticket>` 僅得修改恰為附掛之票檔，僅於其 lifecycle 為 `TICKET_RIGOR` 時，且僅限事實層、可由本地 repo-readable 證據唯一導出、可相稱驗證、不改 scope／product／policy／lifecycle／SSOT／public contract／design 之修訂。
- **HC-4**：C_EXCEPTION finding MUST NOT 使同一 cycle 內其他獨立 eligible finding 之處理停止。
- **HC-5**：單次 execution 至多 3 個 session-local cycle；第三 cycle 後 MUST NOT 進行第 4 次 mutation；MUST NOT 自動 VOID／reopen／split／create／dispatch 或改 lifecycle。
- **HC-6**：MUST NOT 使用、修改、經由或擴張 `repair_runner.sh`、runner schemas、runner fixtures、model allowlist、payload／response contract、sandbox matrix、golden report。
- **HC-7**：MUST NOT 建立或使用 synthetic scripted-control dogfood、預填答案、queue、webhook、callback、polling、Research Ledger 或新平台。
- **HC-8**：MUST NOT 對 eligible 修訂要求逐筆批准；MUST NOT 進行全 repo 或全產品之重複審查掃描。
- **HC-9**：Research 維持 `B_RESEARCH_SUBMITTED` 路由語義，本票 MUST NOT 刪除或弱化之；research 結果 MUST NOT 授予 local mutation authority；research 結果回寫**不是**本票之阻斷 AC。bounded transport probe 於票外執行（見 Error Handling 末條）。
- **HC-10**：MUST NOT 削弱任何既有跨票授權防線。`REPAIR_ROUTING.md` L45 條件② MUST 與 `806cd0d` 逐字相同；本票之 lifecycle 措辭 MUST NOT 重新定義「signed／registered／dispatched」。
- **HC-11**：`REPAIR_ROUTING.md`「## Post-Signature Erratum Lane」整節（L65 起至「## General Autonomous Repair Default」前）MUST 與 `806cd0d` 逐字相同。
- **HC-12**：`TICKET_RIGOR` 期間 gate execution 持有對附掛票檔之 exclusive **autonomous** mutation authority；不重定義一般人工／SKAD authority，但 execution 進行中其他寫入端 MUST NOT 寫同一票檔；偵測手段為 size＋content hash 起始記錄與每次 mutation 前重比，不符即回報 `CONCURRENT_TICKET_WRITE`。
- **HC-13**：AI 總數 ≤3（K8）；需第四步 → 停止回報。

## Error Handling
- AI-0 錨點字面 >1 處或完全消失 → 停止回報。
- AI-1／AI-2 需改動 L30 原句、L45 條件②、Erratum Lane 整節、L361／L363–364 或 Mechanical Verdict Rule 方能自洽 → 停止回報（範圍不足，改由 SKAD 改票）。
- dogfood 發現**由本票變更引入**之 Critical，或違反 HC-2／HC-3／HC-4／HC-5／HC-10／HC-11／HC-12 → 停止，不 commit，附八欄回報。
- dogfood 發現**既有且與本票無因果關係**之 Critical → 依既有 route 回報，不作本票 blocker，禁由本票自修。
- `CONCURRENT_TICKET_WRITE` → 停止該次 mutation，回報，不自行解決衝突。
- `git diff --name-only` 超出 ALLOWED（dogfood target 票檔除外，且其異動不入本票 commit）→ 停止回報。
- **[標配]** `git check-ignore -v <ALLOWED 標的>` 命中 → 停止回報。
- **票外前置證據（非阻斷）**：`research_submit` 之一次 public-safe bounded transport probe，receipt 存 `.tmp/rigor-gate/research-probe/`；僅驗證 transport 可同步呼叫並回傳，**不得**作為 direct interactive B-route 已整合之證據。若 probe 顯示 transport 可達但 gate 不會自主路由呼叫，記 `REACHABILITY_PENDING`，另開最小 follow-up 票承接。

## Acceptance Criteria
- [x] **[存在性]** `REPAIR_ROUTING.md` L30 原句一字不改，其後含窄化例外句（exactly `TICKET_RIGOR`、does not alter the meaning of "signed"…）；General Autonomous Repair Default 含 Attached-ticket authority 段（exact attached file、條件①視為已授予、九個 lifecycle 明列且無順序比較、Concurrency 子段含 size＋hash 與 `CONCURRENT_TICKET_WRITE`）；L121 段末含不短路句。
- [x] **[存在性]** `git diff 806cd0d HEAD -- REPAIR_ROUTING.md` 之 hunk **不觸及** L45 條件② 與 Erratum Lane 整節（HC-10／HC-11 逐字比對，附比對輸出）。
- [x] **[存在性]** `SKILL.md` Inputs 節含 `@<ticket-path>` 語意；Findings Summary 節含八欄與三條一致性規則；含 `## Session-local Closure Cycle N of 3` heading 規定；L361／L363–364 與 `806cd0d` 逐字相同。
- [ ] **[行為性]** given AI-0 合格 target 且 lifecycle=`TICKET_RIGOR`，expect `/ticket-rigor-gate @<target>` 無逐筆「是否同意」停點、AI-0 記錄之候選 finding 實際回寫至該票檔、diff 僅限該票檔、read-back 與比例驗證成功、reassessment 真實發生、每筆 finding 附八欄、iteration 計數 ≤3。**若 AI-0 為 `LIVE_MICRO_DOGFOOD_TARGET_UNAVAILABLE`**：本 AC 記為 `ACCEPTANCE_PENDING`，適用下方 **Deferred Acceptance Contract**，實作 AC 仍可結案，**本票不得標示 fully verified**，且 MUST NOT 以 CLOSED 票、大型產品票、runner fixture 或 scripted-control 票替代。
- [ ] **[行為性]** given dogfood target 轉 `PENDING` 後再次 `/ticket-rigor-gate @<target>`，expect 無任何 mutation 且 report 載明 authority 不適用（lifecycle 閘反向證明）。UNAVAILABLE 時同記 `ACCEPTANCE_PENDING`，適用同一 **Deferred Acceptance Contract**。

**Deferred Acceptance Contract**（AC-4／AC-5 之 `ACCEPTANCE_PENDING` 共用；解決 Ticket Rigor Gate 審查標記之 M-3「disclosed but no assignee」缺口）：
- **assignee**：SKAD（下次以 direct interactive `/ticket-rigor-gate @<ticket>` 執行本 gate 之操作者）。
- **trigger**：下一張「非 production 規模」草稿票之 AI-0 pre-scan 偵測到 `lifecycle=TICKET_RIGOR` 之合格候選票時，MUST 依本票 AI-0～AI-3 規則實際執行該次 live micro-dogfood；此觸發條件之時效範圍為本票自身由 `TICKET_RIGOR` 轉出（含轉 `PENDING` 重工或轉 `IN_PROGRESS` 續辦）前——即本票收尾前之下一個合格機會即為觸發點，不得無限期順延。
- **required evidence**：比照 AC-4 正文——候選 finding 實際回寫至目標票檔、diff 僅限該票檔、read-back 與相稱驗證成功、reassessment 真實發生、每筆 finding 附八欄、iteration 計數 ≤3。
- **closure rule**：觸發條件成立且 required evidence 到齊 → AC-4／AC-5 由 `ACCEPTANCE_PENDING` 改判為已驗證，於當次收尾同批註記完成。若本票已收尾（進入 `REVIEW`／`CLOSED`）而觸發條件仍未成立（迄未出現合格候選票），`ACCEPTANCE_PENDING` 轉為待稽核之未閉環殘留風險，MUST 於下一次 `/dream` 或 `/compact closeout` 明列，不得靜默遺忘。
- [x] ~~assembleDebug PASS~~ (N/A: docs preset)
- [x] `git diff --stat`（本票 commit）僅涉及兩個 ALLOWED 檔案（commit `a65b0bc` read-back 確認）


---

## Closeout（SKAD 2026-09-30）

**Lifecycle**: CLOSED　**Reviews**: qa=PASS、sv=PASS、codex=PASS、traycer=N_A　**實作 commit**: `a65b0bc`（`REPAIR_ROUTING.md`＋`SKILL.md`，+28／-1）

**Live acceptance 證據**：`RGR-LIVE-DOGFOOD-01` Run 1 gate report，保全副本 `.tmp/rigor-gate/RGR_RGR-LIVE-DOGFOOD-01_20260926_run1.md`（8561 bytes，sha256 `c6bd254048de6f8a426bc4a0a77c864bb0a7834aa147253ae0373fab0d641823`）。該副本位於 gitignored 路徑，本節為其核心事實之耐久承載。

| contract 驗收項 | 結果 |
|---|---|
| ① eligible 修訂被自主寫入 target | ✅ 兩筆（`evidence_dependency` 誠實化、`AUTH-00`→`AUTH-01`），正確值僅能由 registry 導出 |
| ② mutation 限於附掛票檔 | ✅ `git status` 乾淨，`settings.json` hash 前後一致 |
| ③ read-back 與比例驗證 | ✅ |
| ④ same-scope reassessment | ✅ rerun 後原 finding 零命中 |
| ⑤ C_EXCEPTION 不短路 | ⚠️ **僅消極證據**：open verification question 未被裁定、未阻斷修正，但未被建為 C_EXCEPTION finding，故「已分類 C_EXCEPTION 與 eligible repair 並存」未正向覆蓋 |
| ⑥ iteration 計數與八欄 disposition | ✅ cycle 1/3，每筆附八欄 |
| ⑦ 未經 runner／schema／fixture | ✅ |
| AC-5 轉 PENDING 後再 invoke 應無 mutation | ❌ **未驗證** |

**收束依據**：Akira 2026-09-30 裁定——實際發生 routing 問題時再開修補票。⑤ 與 AC-5 依此以「未驗證、豁免」收束，**非通過**。

**附記**：`RGR-LIVE-DOGFOOD-01` Run 2 另自主修正一筆真實 Major（其 Closeout 未區分轉態 actor，依 `.claude/rules/ssot-governance.md` L86–87），為 A-route 之第二次 live 證據（副本 `…_run2.md`，sha256 `caeebda1d0a9da35c7c31a2f5d4695bae341740ae06fea0bda756f57250aefa7`）。首次 live 觸發時，gate 曾以決策層內容（assignee／trigger）自修本票票面，屬 boundary miss，影響低，不作驗收證據。

**後續取代**：本票 HC-9（Research 維持 `B_RESEARCH_SUBMITTED` 路由語義）已由 `RGR-ROUTING-SELECTOR-01` 明示取代，僅限 final-report disposition。
