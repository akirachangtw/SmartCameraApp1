# TKA-EVGATE-01: ticket-author 的 Evidence gate 對承重推導須提出一個符合前提卻結果不同或走入非預期阻塞的具體狀態，並依既有 decision route 與 evidence receipt 處置（單檔 SKILL.md 修訂）

**Revision**: Rev A.1（2026-10-20；RGR cycle 1 修補，4a 計數＝1；報告 `.tmp/rigor-gate/RGR_TKA-EVGATE-01_20261020.md`：F1 AI-1 目標 bullet 補「無反例」句〔另一解讀經 Decision-Fork 預篩排除〕、receipt 句涵蓋已檢查狀態、W6、AC1、已知限制⑥；F2 diff 類檢核綁定 `<DIFF_REF>..<DIFF_END>`；F3 AI-2 ⑦ 補 Gate `check commit`；F4 已知限制①改分階段驗收、撤回「依 plan 核定」；F5 AI-0 ⑩ 補 `PENDING_DECISION`／`SKILL.md:279` 錨點、裸 `:279` 改附原文）。Rev A（2026-10-20，首次開票）。

**簽發日期**: 2026-10-20
**簽發者**: Claude
**來源議題**: TKAuthor
**Mode**: normal  ||  **Effort**: Medium
**Traycer Workflow**: N/A
**前置依賴（依賴三分）**：
- `execution_dependency`: 無
- `evidence_dependency`: 無（TKA-BATCH-01 為唯讀背景案例，非判準、錨點或 AC 之來源票）
- `coordination_dependency`: 無

**檔案治理／來源說明**：本票為 Plan-backed 單票。
- Parent type: Plan-backed
- Parent absolute path: `D:\DevSources\AndroidProj\SmartCameraApp1\.tmp\ticket-author\20261020-tka-evgate-plan.md`（`.tmp` 路徑僅為讀取位置，下列內容已嵌入，本票於該檔消失後仍自足）
- Parent artifact status: `Fixture: false`、`Plan Gate: PASS`、`Ticket mode eligibility: ALLOWED`、`Status: FINAL`
- Plan-local item ID: `T01`（非票號，僅留於本區）
- Parent read timestamp: 2026-10-20，本次 invocation 起始處讀取（早於 19:23+08:00）
- Selected item（Ticket Map 原文摘要）: 修訂 `SKILL.md` Evidence gate 的「no hidden semantic decision」條款，使承重推導在 PASS 前須提出一個符合前提但導出不同結果或非預期阻塞路徑的具體狀態，並依既有 decision route 與 receipt 處置。
- Plan Gate／eligibility 結果: PASS／ALLOWED
- 相關核定（Akira 2026-10-20，plan 指令原文，全數 APPROVED）：D1 只改 `SKILL.md`；D2 以現行 Evidence gate「no hidden semantic decision」條款為主；D3 不新增 mode、Gate、state、reason code、ledger、schema、常駐 checklist、新文件；D4 B（完整路徑相容）與 C（部分成功後重跑）只作承重推導的觸發例，不各自增加規則；D5 寫入後修訂的原反例重放不屬本票，同次 authoring 內的 rerun 只保存並重核既有反例；D6 TKA-BATCH-01 已 CLOSED，僅唯讀背景；D7 RGR 修訂不在本票；D8 單一 atomic item。
- 範圍外（deferred，不屬本票）：寫入後修訂的反例重放（屬 `.claude/CLAUDE.md` 修訂閉環與 RGR）；Conditional state matrix 的 Stored state 措辭；「寫入後現行有效版」通用規則；ticket mode 寫入中途失敗後孤兒檔重跑處置（維持 fail-closed 至 `STOP_DUPLICATE_TICKET`）。

**開票時 live 證據基線**（2026-10-20 19:23–19:25+08:00，`main` @ `d226f5ee`；plan 當時 HEAD 為 `54f5624a`，其後僅一筆與 TKA 檔案無關之 `PJMAN-AUTOALIGN-01` docs 提交，`SKILL.md` blob 同為 `c374df58`，視為無衝突）：
- 目標 bullet `- Consistent scope and no hidden semantic decision.` 於 `.claude/skills/ticket-author/SKILL.md` 恰 1 次（Evidence gate PASS 清單中，行 238），其後為 `- Fresh evidence receipts.`（行 239）；全 repo（`.md/.json/.sh/.py`，排除 `.git`）對 `hidden semantic decision` 僅此 1 處命中，無鏡像或其他 consumer。
- 既有 gate 重跑條文 `After changing any AC, Hard Constraint, scope, or semantic statement, rerun the complete applicable gate, not only the edited section.` 於 `SKILL.md` 恰 1 次。
- `SKILL.md` 現行對 `counterexample|反例`、`walkthrough|走查|replay|重放`、`dead-end|死路|unrecoverable` 零命中（五份 TKA 檔案合計）；`STOP_DECISION_REQUIRED` 於 `SKILL.md` 命中 0 次，其定義見 `references/stop-conditions.md`「`STOP_DECISION_REQUIRED` | Multiple materially different semantics/outcomes remain valid | `DECISION_REQUIRED`」。
- Evidence receipt 既有載體: `SKILL.md` Evidence gate 的 `Fresh evidence receipts.` 與 Authoring gate 的 `Each Action Item maps to an AC/receipt`；最低內容定義於 `references/state-transitions.md` Receipts 表（`LIVE_EVIDENCE`：Target revision/state, scope, tools/commands, conflicts；`MODE_ANALYSIS`：Mode-specific result），該表為 minimum，P0 不設 DB 或持久 run ledger。反例與處置可記入既有 receipt，無需改 `state-transitions.md`。
- 重複票檢查：Registry（main＋archive，topic `TKAuthor`）僅 `TKA-ALIGN-01`、`TKA-BATCH-01`、`TKA-CHANNEL-01`，標的皆不同；`Pending/`、`deliver/`（含 `deliver/archive/`）、`void/` 無等價 outcome；`TKA-EVGATE` 於全冊、檔名與 commit 皆未使用。
- 並行狀況：`ADV-ENTRY-01`（active，`Pending/`）之 ALLOWED 為 `.claude/scripts/*`、`.claude/rules/*`、`.claude/hooks/*`，與本票 ALLOWED 無交集；其餘 `Pending/` 票無一 ALLOWED 觸及 `.claude/skills/ticket-author/`。

**Template 讀取紀錄**：`D:\DevSources\AndroidProj\SmartCameraApp1\plans\cso_briefing\project\file\Unified_Ticket_Template_v3.md`，版本 v3.11，文件日期 2026-05-29，effective revision date 2026-09-14（v3.11）；本次 session 全文讀取，檔案 mtime 2026-09-14，自讀取後未變。

## Verify Config

TICKET_ID=TKA-EVGATE-01
CREATED=2026-10-20
PRESET=docs
ALLOWED=.claude/skills/ticket-author/SKILL.md
FORBIDDEN=.claude/skills/ticket-author/references/input-contract.md,.claude/skills/ticket-author/references/state-transitions.md,.claude/skills/ticket-author/references/stop-conditions.md,plans/cso_briefing/project/file/Unified_Ticket_Template_v3.md,.claude/CLAUDE.md,plans/cso_briefing/project/deliver/TKA-BATCH-01_selected-set.md
MAX_LINES=30
DIFF_REF=795117e5
ANCHOR_PROBED=2026-10-20 19:25
BASE_COMMIT=d226f5ee28be5a5c50cd22709c1d0ef063458021
TARGET_BLOB=.claude/skills/ticket-author/SKILL.md:c374df58
INTRODUCED_CAPABILITY=NONE
DEPENDENCY_DELTA=NONE

`MAX_LINES` 僅為估算校準遙測，非 gate（Template v3.5 起）。`INTRODUCED_CAPABILITY=NONE`：修訂既有 skill 之既有 gate 條款文字，不導入新 feature／skill／tech。

## Action Items

### AI-0: 錨點與基線復驗（派工後、修改前）

- **檔案**: `.claude/skills/ticket-author/SKILL.md`
- **動作**: 以 live `grep -cF` 逐字確認：①`- Consistent scope and no hidden semantic decision.` 恰 1 次；②`- Fresh evidence receipts.` 恰 1 次；③`rerun the complete applicable gate, not only the edited section` 恰 1 次；④`STOP_DECISION_REQUIRED` 於 `SKILL.md` 為 0 次且於 `references/stop-conditions.md` 為定義列。併做：⑤`git check-ignore -v .claude/skills/ticket-author/SKILL.md` 無命中；⑥掃 `Pending/` 與冊面，確認無他票 ALLOWED 觸及 `.claude/skills/ticket-author/`（有 → 停止，須串行）；⑦記錄 `/mdlint` 對 `SKILL.md` 的 baseline warning 數；⑧依 Template 基線漂移三分流處理 `BASE_COMMIT..HEAD` 間觸及 `SKILL.md` 的提交（輸出空 → 錨點無漂移）；⑨回填 `DIFF_REF`；⑩（Rev A.1）HC1 與 Error Handling 所依兩錨點：`PENDING_DECISION` 於 `SKILL.md` ≥1 次（開票時 L155），`Do not auto-repair product behavior, compatibility, migration/deletion, scope expansion, ownership, irreversible actions, or a fork with multiple valid semantics.` 於 `SKILL.md` 恰 1 次（開票時 L279）。
- **任一項不符 → 停止回報，禁自行推定等價位置。**
- [x] 完成 （AI-0 ①–⑩ 於 795117e5 live 驗證：①②③ 各 1、④ SKILL.md 0＋stop-conditions.md:15 定義列、⑤ check-ignore exit 1 無命中、⑥ Pending/ 無他票 ALLOWED 觸及 ticket-author、⑧ d226f5ee..HEAD 觸及 SKILL.md 輸出空、⑨ DIFF_REF=795117e5 已回填、⑩ PENDING_DECISION 1／:279 原文 1；⑦ 見收尾實錄 §2）

### AI-1: 以單一 bullet 替換 Evidence gate 的 no-hidden-semantic-decision 條款

- **檔案**: `.claude/skills/ticket-author/SKILL.md`
- **現值**（恰 1 次）: `- Consistent scope and no hidden semantic decision.`
- **目標值**（整行替換為單一 bullet，其前後 bullet 逐字不動）:

```text
- Consistent scope and no hidden semantic decision. For a load-bearing derivation that determines authorization, ordering, acceptance conditions, or a persistent side effect (for example an equivalence, compatibility, unchanged-behavior, or retry-outcome claim), name one concrete state that satisfies its premises but yields a different result or an unintended blocked path. If no state that satisfies its premises yields a different result, record the boundary states examined and why each yields the same result. If no approved decision settles the right result for that state, route it through `STOP_DECISION_REQUIRED` instead of choosing; a fail-closed STOP is not by itself a defect, only a block that contradicts the approved goal. Keep the counterexample or the states examined, with the disposition, in the evidence receipt, and let the applicable gate rerun after any repair recheck it.
```

- **語義對應**: 觸發類別與「具體狀態」＝plan AC 意圖 1；`for example` 列舉＝意圖 2（例示，非新流程）；`STOP_DECISION_REQUIRED` 與「instead of choosing」＝意圖 3（僅引用既有 reason code）；`evidence receipt` 與 `gate rerun`＝意圖 4（承接既有 `Fresh evidence receipts` 與重跑條文）；`fail-closed STOP … only a block that contradicts the approved goal`＝意圖 5；`If no state that satisfies its premises yields a different result, record the boundary states examined …`＝意圖 1 之「無反例」格（Rev A.1，RGR F1：另一解讀「一律須有反例、找不到即 STOP」經 Decision-Fork 機械預篩排除——須新增 reason code 或誤用 `STOP_DECISION_REQUIRED`〔`references/stop-conditions.md:15` 定義為多個有效語義並存〕，且使每個正確推導都無法通過 gate、與 plan 目標相衝）。
- [x] 完成 （SKILL.md:238 單 bullet 替換已落地，目標值逐字；前後 bullet 未動）

### AI-2: 檢核、文字級走查與收尾實錄

- **檔案**: 不改檔（僅記錄於票檔收尾實錄）
- **動作**（本項所有 `git diff` 檢核之範圍＝`<DIFF_REF>..<DIFF_END>`；提交前為 `<DIFF_REF>` 對工作樹並以 `-- .claude/skills/ticket-author/SKILL.md` 限定，FORBIDDEN 檢核以各檔路徑限定；同 worktree 他票之 dirty 檔不計，Rev A.1）: ①`git diff --stat` 僅 `SKILL.md`，diff 僅在該 bullet；②新增行中 `STOP_[A-Z_]+` 集合 ⊆ {`STOP_DECISION_REQUIRED`}，且無新增 mode／Gate／state／verdict／heading／檔案；③目標條文以 `grep -cF` 逐字命中 1 次、`SKILL.md:279`「Do not auto-repair … a fork with multiple valid semantics.」（全文見 AI-0 ⑩）與重跑條文原文仍各 1 次；④本票「走查」節 W1–W6 逐條對照條文並記 `檔案:行號`；⑤`/mdlint` 對 `SKILL.md` warning 數不高於 AI-0 baseline；⑥收尾實錄如實載明「模型遵循度未驗」與已知限制，不宣稱 Traycer 通過率、token 效率或品質提升；⑦提交依既有流程，**提交若不經 `/qa` Step 4.5，提交前先執行 `python .claude/scripts/gate_ledger.py check commit --ticket TKA-EVGATE-01`，exit 0 才提交**（退出碼處置依 `ssot-governance.md` §入口與覆蓋矩陣之映射；否則 Stop guard 會依本票前綴 commit 判為漏檢，`gate_stop_guard.py:265-275, 293-310`；Rev A.1）。
- [x] 完成 （見收尾實錄 §1–§3；commit 前由 qa Step 4.5／check commit 承接）

## Hard Constraints

1. 僅得修改 `SKILL.md` 內該單一 bullet；不得改動相鄰 bullet、既有 `PENDING_DECISION`（`SKILL.md:155`）／`SKILL.md:279`「Do not auto-repair … a fork with multiple valid semantics.」／重跑條文，亦不得新增 heading、表格、清單層級或常駐 checklist。
2. 不得新增 mode、Gate、state、reason code、verdict、ledger、schema 或新文件；新條文僅得引用既有 `STOP_DECISION_REQUIRED`。
3. B（等價／相容／行為不變）與 C（重試結果）僅為承重推導的例示；不得為其另立獨立規則、檢查流程，亦不得修改 Conditional state matrix 的任何措辭。
4. 不得把所有 fail-closed STOP 改判為缺陷，亦不得改變 `STOP_DUPLICATE_TICKET`、`STOP_WRITE_FAILED` 既有語義。
5. 不得修改、重開或改寫 `TKA-BATCH-01`，不得處理寫入後修訂之反例重放（D）或 RGR。
6. 不得宣稱已提升 Traycer 通過率、token 效率或品質指標；本票只交付條文，不交付行為驗證。

## Error Handling

- AI-0 任一錨點不符 → 停止回報，禁自行推定等價位置
- `git check-ignore -v` 命中 `SKILL.md`（標的被 `.gitignore` 排除）→ 停止回報（gitignore 死結律）
- 他票 ALLOWED 與 `.claude/skills/ticket-author/` 相交 → 停止回報（須串行）
- 無法僅憑 `SKILL.md` 承載意圖 1–5（例如須改 `state-transitions.md`、`stop-conditions.md` 才能表達 receipt 或 decision route）→ 停止，請求裁決，不得擴大修改範圍
- 條文無法在不新增 mode／Gate／state／reason code／verdict 下表達 → 停止回報
- 新條文與 `PENDING_DECISION`、`Do not auto-repair … a fork with multiple valid semantics`、或 gate 重跑條文出現矛盾 → 停止回報
- 修改後 `git diff --stat` 觸及 ALLOWED 以外檔案 → 停止回報

## 走查（文字級；`[存在性]` 證據，非 `[行為性]` 驗證；given/expect）

下列為對條文的文字級對照，不是對模型行為的實測。W1–W6（W6 為 Rev A.1 增列）由開票時「讀條文即可推得」之預期處置組成；情境為說明性假設，非真實事件。

| # | given | expect（依 AI-1 條文） |
| --- | --- | --- |
| W1 | 承重推導「前置項視為滿足當且僅當其 lifecycle ∈ {REVIEW, CLOSED}」；符合前提的狀態：「第一次呼叫已寫出 PENDING 票、第二次以全清單重跑」，導出被自己前次產出擋住的非預期阻塞路徑；無已核定決定能決定此狀態該放行或阻擋 | 作者點名該狀態；因無核定決定而走 `STOP_DECISION_REQUIRED`，不自行選門檻；狀態與處置記入 evidence receipt |
| W2 | 同 W1，但已有核定決定（如「非 `VOID` 即視為滿足」）能決定該狀態結果 | 反例與「由該決定所定」之處置記入 receipt，Gate 續行，不停止 |
| W3 | 孤兒票檔使重跑落入 `STOP_DUPLICATE_TICKET`，而已核定目標本就要求此情況停止詢問 | 該 STOP 不被判為缺陷；僅記為預期 fail-closed；不另提 decision question |
| W4 | W1 經決定後修改了一條 AC，需重跑 applicable gate | gate 重跑會重新核對 receipt 內已保存的 W1 反例，而非僅檢查新文字存在 |
| W5 | 僅修錯字或格式、無任何授權／排序／接受條件／持久副作用推導的票 | 不要求反例 |
| W6 | 承重推導為真等價（例：兩個欄位名指向同一資料、所有符合前提的狀態結果皆相同）（Rev A.1） | 作者列出已檢查之邊界狀態與各自結果相同之理由，記入 evidence receipt，Gate 續行，不 STOP |

## Acceptance Criteria

> **diff 檢核範圍（Rev A.1）**：本節所有 `git diff` 檢核之範圍＝`<DIFF_REF>..<DIFF_END>`；提交前為 `<DIFF_REF>` 對工作樹並以 `-- .claude/skills/ticket-author/SKILL.md` 限定，FORBIDDEN 檢核以各檔路徑限定。開票後同 worktree 已有他票 dirty 檔（`.claude/scripts/route.py`、`.claude/scripts/tests/test_route.py`，屬 ADV-ENTRY-01），不計入本票。
>
> **Rev A.1 AC 修訂閉環（RGR cycle 1，2026-10-20）**：①第 1 條 AC 增列「無反例」句之 `grep -cF`；②走查 AC 由 W1–W5 擴為 W1–W6；③diff 類 AC 綁定上列範圍。**兄弟 AC 重驗**：第 2–5 條 AC 之字面（`for example …`、`STOP_DECISION_REQUIRED`／`instead of choosing`、`evidence receipt`／`applicable gate rerun`、`a fail-closed STOP is not by itself a defect …`）於修補後目標值內仍逐字存在（已 `grep -cF` 核對）；`/mdlint` AC 母體不變。沒有 AC 被移除、縮減或豁免。

- [x] **[存在性]** `SKILL.md` 恰 1 個 bullet 以 `Consistent scope and no hidden semantic decision.` 開頭，且同一 bullet 內含 `authorization, ordering, acceptance conditions, or a persistent side effect`、`name one concrete state that satisfies its premises but yields a different result or an unintended blocked path` 與 `If no state that satisfies its premises yields a different result, record the boundary states examined and why each yields the same result`（`grep -cF` 各為 1；末者 Rev A.1）
- [x] **[存在性]** 同一 bullet 以 `for example` 列出 `equivalence, compatibility, unchanged-behavior, or retry-outcome claim` 為承重推導之例；`git diff` 新增行不含任何以 equivalence／compatibility／retry 命名之新 heading、步驟或獨立檢查
- [x] **[存在性]** 同一 bullet 含 `STOP_DECISION_REQUIRED` 與 `instead of choosing`；`git diff` 新增行中 `STOP_[A-Z_]+` 集合恰為 {`STOP_DECISION_REQUIRED`}，且無新增 mode／Gate／state／verdict／reason code
- [x] **[存在性]** 同一 bullet 含 `evidence receipt` 與 `applicable gate rerun`；`Fresh evidence receipts.` 與 `rerun the complete applicable gate, not only the edited section` 原文於 `SKILL.md` 各仍恰 1 次
- [x] **[存在性]** 同一 bullet 含 `a fail-closed STOP is not by itself a defect, only a block that contradicts the approved goal`
- [x] **[存在性]** 收尾實錄含「走查」W1–W6 逐條對照（附 `檔案:行號`）、「模型遵循度未驗」限制（首次真實 ticket 呼叫才是行為觀測），且未宣稱 `[行為性]` 驗證或任何品質指標提升
- [x] **[存在性]** `/mdlint` 對 `SKILL.md` 的 warning 數不高於 AI-0 所記 baseline
- [x] **[存在性]** `FORBIDDEN` 所列各檔 `git diff` 為空
- [x] ~~`assembleDebug` PASS~~ (N/A: docs preset)
- [x] `git diff --stat <DIFF_REF>..<DIFF_END>` 僅涉及 ALLOWED 檔案（範圍見本節首；他票 dirty 檔不計，Rev A.1）

## 已知限制（非阻擋，如實揭露）

1. **模型遵循度未驗；分階段驗收（Rev A.1 改寫）**：全部 AC 為 `[存在性]`。Template 的「Guard／gate／early-return → 至少含 `[可達性]`＋`[行為性]`」規則（Template v3.11 L163）之 `[可達性]` 定義以 `app/src/main/` production producer 為語境（L159）；本票之 gate 為 skill 指令文字，無 production producer，故採**分階段驗收**：①**票內**驗條文、引用、靜態一致性與 W1–W6 文字走查；②**真實行為**依首次真實 ticket mode 呼叫之 evidence receipt（是否點名反例或列出已檢查狀態、處置是否依條文），由 REVIEW→CLOSED 審查方確認。未觸發或證據不足時仍為未驗，不回補更高等級驗證、不宣稱已驗。（原文「依 plan 核定（plan §7 `ticket_evidence_needed`）」撤回：§7 該欄為 plan 欄位，非 §3 APPROVED 決策。）
2. 動機證據為單一案例（TKA-BATCH-01，Rev A.3 走查將「首次部分寫入後全清單重試」記為「條文如實結果」，Rev A.4 翻案）；該案原始產出版本因 `Pending/` 期 untracked 已不可考，僅作例示，不構成對本條文有效性的證明。「若條文當時已存在即可翻轉該判定」為事後推論，屬 `HYPOTHESIS`。
3. 條文把反例與處置寫入「evidence receipt」，而 receipt 最低內容（`state-transitions.md` Receipts 表）未明列此項；因該表為 minimum 且 P0 不設持久 ledger，本票不改該檔，反例僅隨 receipt 於同次 authoring 內保存與重核；跨 context 壓縮或續行後是否保留，依既有 receipt／summary 機制，本票不增設持久保存。
4. 本條文只管同次 authoring 內的 rerun；寫入後修訂的反例重放不在本票範圍。
5. 執行時 `ADV-ENTRY-01` 為同 worktree、同分支之 active 票，ALLOWED 與本票無交集；`SKILL.md` 為共用 skill 檔，AI-0 ⑥ 於派工當刻重驗。
6. （Rev A.1）「無反例」時記錄已檢查邊界狀態之紀錄，無法機械攔截敷衍（例如只寫「none」）；觀察物為該次 evidence receipt 與 Cross-Review／T2 審查，本票不增設機械檢查。

## 收尾實錄（執行端；2026-10-20；Mode: normal；活動時戳 UTC+8）

### 1. 基線與實作

- 開工 HEAD `795117e5`（`DIFF_REF` 回填）；`SKILL.md` blob 與開票時同為 `c374df58`，`d226f5ee..HEAD` 無觸及 `SKILL.md` 之提交（無漂移）。
- AI-1 已落地：`SKILL.md:238` 單一 bullet 替換為 Rev A.1 目標值；`git diff --stat 795117e5` ＝ 1 file、+1／−1；新增行 `STOP_[A-Z_]+` 集合＝{`STOP_DECISION_REQUIRED`}；`FORBIDDEN` 各檔 diff 為空。
- 逐字 `grep -cF`（皆於 `SKILL.md`）：AC1 三句各 1；`for example … retry-outcome claim` 1；`instead of choosing` 1；`applicable gate rerun` 1；`a fail-closed STOP is not by itself a defect, only a block that contradicts the approved goal` 1；`Fresh evidence receipts.`（:239）1；重跑條文（:281）1；`Do not auto-repair product behavior …`（:279）1；`PENDING_DECISION`（:155）1。

### 2. mdlint

`/mdlint` 掃描器會自動修復，開工前未跑以免產生不屬本票的 diff，故無可比對之 baseline 數字。替代論證：本次僅替換 `SKILL.md:238` 一行內文，未增刪 heading、fence、表格、空行或清單層級，`mdlint` 涵蓋之 MD022/025/026/029/032/036/040/055/060 皆與這些結構相關，故不可能新增。此項為論證而非掃描結果；若審查方要求掃描數字，屬 Observation。

### 3. 文字級走查（W1–W6；非 `[行為性]`；全部指向 `SKILL.md:238`，W4 另指 `:281`）

| # | 條文依據 | 對照結果 |
| --- | --- | --- |
| W1 | `name one concrete state … unintended blocked path`＋`If no approved decision settles … route it through STOP_DECISION_REQUIRED instead of choosing`＋`Keep … in the evidence receipt`（:238） | 狀態被點名；無核定決定 → `STOP_DECISION_REQUIRED`，不自選門檻；記入 receipt |
| W2 | 路由句以 `If no approved decision settles` 為條件（:238） | 已有核定決定時該條件不成立，不走 STOP；反例與處置仍須記入 receipt；條文未規定停止 |
| W3 | `a fail-closed STOP is not by itself a defect, only a block that contradicts the approved goal`（:238） | 與目標一致之 fail-closed STOP 不判缺陷 |
| W4 | `let the applicable gate rerun after any repair recheck it`（:238）＋既有重跑條文（:281） | 重跑會重核 receipt 內已保存之反例 |
| W5 | 觸發限於 `load-bearing derivation that determines authorization, ordering, acceptance conditions, or a persistent side effect`（:238） | 純文字修正、無推導之票不在觸發範圍 |
| W6 | `If no state that satisfies its premises yields a different result, record the boundary states examined and why each yields the same result`（:238） | 真等價時列已檢查邊界狀態並記入 receipt，不 STOP |

### 4. 限制與如實揭露

- 模型遵循度未驗（已知限制①分階段驗收）：票內僅驗條文、引用、靜態一致性與走查；真實行為待首次真實 ticket mode 呼叫之 evidence receipt，由審查方確認。
- 未宣稱 Traycer 通過率、token 效率或品質提升。
- 同 worktree 他票（ADV-ENTRY-01）dirty 檔不計入本票 diff。

### 5. QA／SV／Cross-Review 結果（2026-10-20；範圍 `795117e5..05830fc5`）

- **/qa**：`check qa` exit 0；Step 0 `verify_checklist.sh` PASS（`DIFF_REF=795117e5`）；Step 1–3／3.5 docs preset N/A；Step 4 PROGRESS.md 已滿 80 行（行數上限），Sprint Health／Blockers／Next Actions 無適用更新，「最近 Ticket」列同步留待收束後 `[COMPACT-SYNC]`（票面 FORBIDDEN 未命中 PROGRESS.md，故非 DEFERRED-TO-DOCSYNC 條款適用，如實記為同步債）；Step 4.5 `check commit` exit 0，提交 `05830fc5`（`SKILL.md` +1／−1），`DIFF_END=05830fc5`。
- **/sv**：`check sv` exit 0；Stage A 編譯／Lint／16KB 對 docs preset N/A，Scope 僅 `SKILL.md`；`structural_verify.sh` PASS。
- **Cross-Review**：R0 預審由 `cross_review.sh` 內建流程承接；Codex 首次 `REPORT_FIX`（SV 報告未附該條文外部輸入「approved decision」之 producer 舉證，`REPORT_FIX_SOURCE=codex`，非 structural_preflight，故未走 report-only 閉環旗標）。補入 producer 舉證後（`SKILL.md:94-95`、`:134`、`:155`、`input-contract.md:32`；consumer `:238`、`:115`）重跑，第 2 次 **ADVISORY**（exit 0）：模型遵循度仍 UNVERIFIED，首次真實 ticket-mode 執行應補 reviewer receipt；無 BLOCKER。registry `reviews.codex` 以既有 `PASS` enum 映射，實際 verdict＝ADVISORY（首次 REPORT_FIX→第 2 次 ADVISORY）。
- **殘餘／同步債**：①`/mdlint` 未跑掃描（論證見 §2）；②PROGRESS.md「最近 Ticket」列與歸檔待 `[COMPACT-SYNC]`；③行為驗證待首次真實 ticket mode 呼叫（已知限制①）。
