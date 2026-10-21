# RGR-DERIV-01: ticket-rigor-gate 於 Step 3 核對承重推導的票面是否記錄反例或已檢查邊界狀態（單檔 SKILL.md 增補一段）

**Revision**: Rev A.1（2026-10-21，RGR Preflight Cycle 1 修補 F1–F5：兄弟票並行檢核改為僅 `IN_PROGRESS` 時停止並更正 `coordination_dependency`、diff 檢核排除他票提交、判定邊界走查 W7／W8、錨點反斜線註記、已知限制⑦⑧；報告 `.tmp/rigor-gate/RGR_RGR-DERIV-01_20261021.md`）。前版：Rev A（2026-10-21，首次開票）。

**簽發日期**: 2026-10-21
**簽發者**: Claude
**來源議題**: RGR
**Mode**: normal  ||  **Effort**: Medium
**Traycer Workflow**: N/A
**前置依賴（依賴三分）**：
- `execution_dependency`: 無
- `evidence_dependency`: 無（TKA-EVGATE-01 為作者側既有條文之背景來源，非本票判準、錨點或 AC 之來源票）
- `coordination_dependency`: 與同 plan 的 RGR-EQUIV-01、RGR-RERUN-01 同改 `SKILL.md`，依 `concurrency.md` §2 須串行（三票不得同時為 `IN_PROGRESS`）；屬資訊性，不排定先後、不傳染 STOP（Rev A.1 由「無」更正）

**檔案治理／來源說明**：本票為 Plan-backed 批次中的一張獨立票。
- Parent type: Plan-backed
- Parent absolute path: `D:\DevSources\AndroidProj\SmartCameraApp1\.tmp\ticket-author\20261021-rgr-improvement-plan.md`（`.tmp` 路徑僅為讀取位置，下列內容已嵌入，本票於該檔消失後仍自足）
- Parent artifact status: `Fixture: false`、`Plan Gate: PASS`、`Ticket mode eligibility: ALLOWED`、`Status: FINAL`
- Plan-local item ID: `T01`（非票號，僅留於本區）
- Parent read timestamp: 2026-10-21 14:36+08:00（批次 shared preflight 起始處讀取）
- Selected item（Ticket Map 原文摘要）: 在 `SKILL.md` Step 3 清單後加一段，使票面的承重推導被核對是否記錄了「符合前提卻結果不同或走入非預期阻塞的具體狀態」或「已檢查的邊界狀態與各自結果相同的理由」。trigger：票面載有決定授權、順序、驗收條件或持久副作用的推導（等價、相容、行為不變、重試結果為例）；純文字修正、無推導的票不觸發。最小動作：審查者只核對紀錄有無，不須自行窮舉反例；缺紀錄歸既有 Missing Contract Expansion，結果無核定決策可定歸既有 Decision-Fork。退出條件：紀錄存在即退出，不觸發即不檢，fail-closed 的既有阻塞不因其阻塞性被判為缺陷。
- Plan Gate／eligibility 結果: PASS／ALLOWED
- 相關核定（Akira 2026-10-21，plan 指令原文，全數 APPROVED）：R1 進入 RGR 最小改善計畫；R2 A 承重推導反證落 `SKILL.md`；R7 新檢查不新增 token、severity 列、reason code、Gate、lifecycle 或 repair authority；R8 新檢查預設不影響 verdict；R9 B、C 不折疊成 A 的示例（本票只做 A）；N2 不得指定新 finding 一律 Medium 或一律不影響 verdict；N3 不得建立常駐 benchmark、案例庫、ledger、第二 Gate、repo-wide tracing；N4 不得重開 TKA-BATCH-01。Akira 2026-10-21 另裁決：Plan Gate、「R8 與 N2 的相容解讀」（新條文對嚴重度保持沉默）、四項獨立票均接受；隔離成對評測維持未授權，不阻擋開票。
- 範圍外（deferred，不屬本票）：B（RGR-EQUIV-01）、C（RGR-RERUN-01）、D（RGR-REPLAY-01）；隔離評測；Severity 表缺 Medium 判準列與 Missing Contract Expansion 專列之補正；`.claude/CLAUDE.md` AC 修訂閉環。

**開票時 live 證據基線**（2026-10-21 14:36–14:39+08:00，`main` @ `c8385e64`，工作樹乾淨）：
- 錨點 `If not, mark \`Missing Contract Expansion\`.` 於 `.claude/skills/ticket-rigor-gate/SKILL.md` 恰 1 次（L314），其前為 Step 3 清單（L303–L312，末項 `- legal-but-borderline values`），其後為 `### Step 4: Identify canonical sources`（L316）。`SKILL.md` blob `24d2d444`，648 行。
- `ticket-rigor-gate/` 目錄全文對 `counterexample`／`反例` 零命中；`Decision-Fork Findings` 小節（L164）與 Finding Taxonomy 中 `Missing Contract Expansion`、`Decision-Fork` 均為既有類別。
- consumer 盤點：`.claude/` 內無檔案依賴 Step 編號、Finding Taxonomy 枚舉或 Step 3 文字；`runner/` 無 taxonomy 枚舉；無鏡像副本。單檔修改可行。
- 重複票檢查：Registry（topic `RGR`，main＋archive 共 23 筆）無等價 outcome（最近者 `RGR-MIGRATION-LITE-01`〔資料類完整性〕、`RGR-ACTYPE-01`〔AC 標記〕標的不同）；`Pending/`、`deliver/`（含 `deliver/archive/`）、`void/` 僅 `TKA-EVGATE-01`（作者側，標的為 `ticket-author/SKILL.md`）涉及相同概念，標的不同；`RGR-DERIV-01` 於全冊、檔名、commit 皆未使用。
- 並行狀況：`Pending/` 內無他票 ALLOWED 觸及 `.claude/skills/ticket-rigor-gate/`（`TKA-CLOSE-01` 的 ALLOWED 為 `ticket-author/`）。本 plan 另三票中 `RGR-EQUIV-01`、`RGR-RERUN-01` 同改 `SKILL.md`，須序列執行。
- `git check-ignore -v` 對 `SKILL.md` 無命中（exit 1）。

**承重推導反證紀錄**（依 `ticket-author/SKILL.md` Evidence gate「no hidden semantic decision」條款；本票推導：「新條文對嚴重度保持沉默，故 verdict 只能經既有 Severity 規則改變」）：
- 反證狀態：Severity 表（`SKILL.md:156-162`）沒有 Medium 判準列，也沒有 Missing Contract Expansion 專列；一個未被任何列匹配的 finding 沒有條文決定其等級。若某審查者把「缺紀錄」判為 Major（表無此列，僅憑印象），兩個此類 finding 即使 verdict 變為 PARTIAL；若判為 Medium 則不影響 verdict。符合推導前提（新條文沉默），卻得到不同 verdict。
- 處置：已核定決策可定——Akira 2026-10-21 接受「新條文對嚴重度保持沉默」（R8＋N2 相容解讀），並明確要求不指定固定等級（N2）；此既有缺口（表無 Medium 判準列）列為 plan §9 Observation，不在本票修補。故不走 `STOP_DECISION_REQUIRED`，反證與處置記錄於此，並於已知限制⑥揭露。

**Template 讀取紀錄**：`D:\DevSources\AndroidProj\SmartCameraApp1\plans\cso_briefing\project\file\Unified_Ticket_Template_v3.md`，版本 v3.11，文件日期 2026-05-29，effective revision date 2026-09-14（v3.11）；本次 invocation 全文讀取（322 行）。

## Verify Config

TICKET_ID=RGR-DERIV-01
CREATED=2026-10-21
PRESET=docs
ALLOWED=.claude/skills/ticket-rigor-gate/SKILL.md
FORBIDDEN=.claude/skills/ticket-rigor-gate/REPAIR_ROUTING.md,.claude/skills/ticket-rigor-gate/OPS_CALIBRATION.md,.claude/skills/ticket-author/SKILL.md,.claude/skills/ticket-author/references/input-contract.md,.claude/CLAUDE.md,plans/cso_briefing/project/file/Unified_Ticket_Template_v3.md,plans/cso_briefing/project/deliver/TKA-BATCH-01_selected-set.md,plans/cso_briefing/project/deliver/TKA-EVGATE-01_derivation.md
MAX_LINES=20
DIFF_REF=cf12e18a
ANCHOR_PROBED=2026-10-21 14:38
BASE_COMMIT=c8385e648b6c2fb808f923baae0ec64374aae6b0
TARGET_BLOB=.claude/skills/ticket-rigor-gate/SKILL.md:24d2d444
INTRODUCED_CAPABILITY=NONE
DEPENDENCY_DELTA=NONE

`MAX_LINES` 僅為估算校準遙測，非 gate（Template v3.5 起）。`INTRODUCED_CAPABILITY=NONE`：增補既有審查演算法一段文字，不導入新 feature／skill／tech。

## Action Items

### AI-0: 錨點與基線復驗（派工後、修改前）

- **檔案**: `.claude/skills/ticket-rigor-gate/SKILL.md`
- **動作**（下列片語中「反斜線＋反引號」代表反引號字元本身，`grep` 時不含反斜線）: 以 live `grep -cF` 逐字確認：①`If not, mark \`Missing Contract Expansion\`.` 恰 1 次；②`### Step 3: Expand hidden truth tables` 與 `### Step 4: Identify canonical sources` 各恰 1 次；③`### Decision-Fork Findings` 恰 1 次；④`boundary states examined` 於 `ticket-rigor-gate/` 為 0 次（尚未落地）。併做：⑤`git check-ignore -v .claude/skills/ticket-rigor-gate/SKILL.md` 無命中；⑥掃 `Pending/` 與冊面，確認無他票 ALLOWED 觸及 `.claude/skills/ticket-rigor-gate/SKILL.md`（有 → 停止，須串行；同 plan 的 RGR-EQUIV-01、RGR-RERUN-01 僅於其 lifecycle 為 `IN_PROGRESS` 時停止，其餘狀態屬資訊性，記錄後續作）；⑦記錄 `/mdlint` 對 `SKILL.md` 的 baseline warning 數；⑧依 Template 基線漂移三分流處理 `BASE_COMMIT..HEAD` 間觸及 `SKILL.md` 的提交（同 plan 的 RGR-EQUIV-01／RGR-RERUN-01 若已先落地，屬資訊性分類，記錄歸因票 ID 後以現值續作）；⑨回填 `DIFF_REF`。
- **任一項不符 → 停止回報，禁自行推定等價位置。**
- [x] 完成 （見收尾實錄）

### AI-1: 於 Step 3 末段增補一段承重推導核對

- **檔案**: `.claude/skills/ticket-rigor-gate/SKILL.md`
- **現值**（恰 1 次）: `If not, mark \`Missing Contract Expansion\`.`
- **目標值**（保留原行不動，其後空一行接下列單一段落；再空一行接原有 `### Step 4: Identify canonical sources`）:

```text
For a load-bearing derivation in the ticket that determines authorization, ordering, acceptance conditions, or a persistent side effect (for example an equivalence, compatibility, unchanged-behavior, or retry-outcome claim), also check that the ticket records either one concrete state that satisfies the derivation's premises but yields a different result or an unintended blocked path, or the boundary states examined and why each yields the same result. If neither record is present, mark `Missing Contract Expansion`; if no approved decision settles the right result for the recorded state, treat it as a `Decision-Fork` finding. The reviewer is not required to produce the counterexample, and a fail-closed block is not by itself a defect, only a block that contradicts the approved goal.
```

- **語義對應**（plan T01 `acceptance_intent`）: 觸發類別與兩種合格紀錄＝意圖①；缺紀錄只引用 `Missing Contract Expansion`、無核定決策可定只引用 `Decision-Fork`＝意圖②；整段不含 token、severity 等級語句、reason code、新章節＝意圖③與 R7／R8／N2；`The reviewer is not required to produce the counterexample`＝plan「最小動作：審查者不須自行窮舉反例」；`fail-closed block … contradicts the approved goal`＝plan「退出條件」。
- [x] 完成 （見收尾實錄）

### AI-2: 檢核、文字級走查與收尾實錄

- **檔案**: 不改檔（僅記錄於票檔收尾實錄）
- **動作**（本項所有 `git diff` 檢核之範圍＝`<DIFF_REF>..<DIFF_END>`；提交前為 `<DIFF_REF>` 對工作樹並以 `-- .claude/skills/ticket-rigor-gate/SKILL.md` 限定，FORBIDDEN 檢核以各檔路徑限定；同 worktree 他票之 dirty 檔，及範圍內他票提交〔commit 前綴非 `[RGR-DERIV-01]`〕觸及之檔案，皆不計）: ①`git diff --stat` 僅 `SKILL.md`，diff 僅在 Step 3 末段新增該段；②新增行不含 `Medium`、`verdict`、`token`、`REASON_CODES` 字樣，且不新增任何 heading、表格、清單層級或檔案；③目標段落以 `grep -cF` 逐字命中 1 次，錨點 `If not, mark \`Missing Contract Expansion\`.` 仍恰 1 次；④本票「走查」W1–W8 逐條對照條文並記 `檔案:行號`；⑤`/mdlint` 對 `SKILL.md` warning 數不高於 AI-0 baseline；⑥收尾實錄如實載明「模型遵循度未驗」與已知限制，不宣稱 Traycer 通過率、token 效率或品質提升，並載明「隔離成對評測未授權、未執行」；⑦提交依既有流程，**提交若不經 `/qa` Step 4.5，提交前先執行 `python .claude/scripts/gate_ledger.py check commit --ticket RGR-DERIV-01`，exit 0 才提交**（退出碼處置依 `ssot-governance.md` §入口與覆蓋矩陣之映射）。
- [x] 完成 （見收尾實錄）

## Hard Constraints

1. 僅得在 `SKILL.md` Step 3 末段新增上列單一段落；不得改動 Step 3 清單、相鄰 Step、Severity 表、Mechanical Verdict Rule、Finding Taxonomy 清單或十欄 disposition，亦不得新增 heading、表格、清單層級或常駐 checklist。
2. 不得新增 token、severity 列或等級語句（含「一律 Medium」「不影響 verdict」）、reason code、Gate、lifecycle 或 repair authority；新段落僅得引用既有 `Missing Contract Expansion` 與 `Decision-Fork`。
3. 不得要求審查者自行提出或窮舉反例，不得引入 TKA 的 evidence receipt 概念。
4. 不得修改 `REPAIR_ROUTING.md`、`OPS_CALIBRATION.md` 或重述 D（修後反例重放）之內容；B（RGR-EQUIV-01）、C（RGR-RERUN-01）不得在本票併入。
5. 不得修改、重開或改寫 `TKA-BATCH-01`、`TKA-EVGATE-01`，亦不得建立常駐 benchmark、案例庫、ledger、第二 Gate 或 repo-wide tracing。
6. 不得宣稱已提升 Traycer 通過率、token 效率或品質指標；本票只交付條文，不交付行為驗證。

## Error Handling

- AI-0 任一錨點不符 → 停止回報，禁自行推定等價位置
- `git check-ignore -v` 命中 `SKILL.md`（標的被 `.gitignore` 排除）→ 停止回報（gitignore 死結律）
- 他票 ALLOWED 與 `.claude/skills/ticket-rigor-gate/SKILL.md` 相交 → 停止回報（須串行）；同 plan 的 RGR-EQUIV-01、RGR-RERUN-01 僅於其 lifecycle 為 `IN_PROGRESS` 時適用本條
- 無法僅憑 `SKILL.md` 單段承載 plan T01 意圖①–④（例如須改 Severity 表或新增章節才能表達）→ 停止，請求裁決，不得擴大修改範圍
- 條文無法在不新增 token、severity 列、reason code、Gate、lifecycle、repair authority 下表達 → 停止回報
- 新條文與 `Decision-Fork Findings` 小節、Mechanical Verdict Rule 或 Severity Assignment Rules 出現矛盾 → 停止回報
- 修改後 `git diff --stat` 觸及 ALLOWED 以外檔案 → 停止回報

## 走查（文字級；`[存在性]` 證據，非 `[行為性]` 驗證；given/expect）

下列為對條文的文字級對照，不是對模型行為的實測。情境為說明性假設，非真實事件；E1 對應 plan §8。

| # | given | expect（依 AI-1 條文） |
| --- | --- | --- |
| W1（E1 flawed） | 票面宣稱「新冷卻邏輯與舊邏輯在所有拍攝路徑等價」，沒有任何「臉部在冷卻期內遺失又重現」之類狀態紀錄，也沒有已檢查邊界狀態 | 觸發（推導決定授權／順序／驗收之類結果）；缺兩種紀錄 → `Missing Contract Expansion`；嚴重度依既有規則，本段不指定 |
| W2（E1 valid） | 同 W1，但票面記錄該狀態且結果指向已核定規則（`phase0-lessons.md` Rule 22） | 紀錄存在即退出，新段落不產生 finding |
| W3 | 票面記錄了反例狀態，但無任何已核定決策能決定該狀態的正確結果 | 依條文視為 `Decision-Fork` finding，走既有 `ROUTE=C_EXCEPTION`，不由審查者代選 |
| W4 | 僅修錯字或格式、無授權／排序／接受條件／持久副作用推導之票 | 不觸發，不要求紀錄 |
| W5 | 票面的 fail-closed 阻塞與已核定目標一致 | 該阻塞不被判為缺陷 |
| W6 | 票面為真等價（所有符合前提的狀態結果皆相同），並列出已檢查邊界狀態與各自結果相同的理由 | 第二種合格紀錄成立，退出，不要求另提反例 |
| W7 | 票面的 fail-closed 阻塞與已核定目標相悖 | 依條文為缺陷；核定目標已決定正確結果，不符合 `Decision-Fork` 的「no approved decision settles」條件，且 HC2 僅允許引用兩類 → 記 `Missing Contract Expansion` |
| W8 | 票面無紀錄，但審查者自行看到一個符合前提、結果未定的狀態 | 新段落僅判缺紀錄 → `Missing Contract Expansion`；該狀態若同時構成多個互斥可行路徑，既有 `### Decision-Fork Findings` 獨立適用，新段落不排除也不取代它 |

## Acceptance Criteria

> **diff 檢核範圍**：本節所有 `git diff` 檢核之範圍＝`<DIFF_REF>..<DIFF_END>`；提交前為 `<DIFF_REF>` 對工作樹並以 `-- .claude/skills/ticket-rigor-gate/SKILL.md` 限定，FORBIDDEN 檢核以各檔路徑限定；同 worktree 他票 dirty 檔，及範圍內他票提交（commit 前綴非 `[RGR-DERIV-01]`，以 commit 前綴歸屬）觸及之檔案，皆不計入本票。下列片語中「反斜線＋反引號」代表反引號字元本身，`grep` 時不含反斜線。
>
> **AC 修訂閉環（Rev A.1）**：本節首範圍說明、第 4 條（W1–W6→W1–W8）與最後一條（排除他票提交）經修訂；共用同一 diff 範圍母體之兄弟 AC（第 3、6 條）已重驗：同樣只因他票提交而受影響，修訂後排除規則一致套用，判準不變；第 1、2、5 條不涉 diff 範圍，不受影響。無 AC 被移除或豁免。

- [x] **[存在性]** `SKILL.md` Step 3 末段含一個以 `For a load-bearing derivation in the ticket that determines authorization, ordering, acceptance conditions, or a persistent side effect` 開頭的段落，且同段內含 `one concrete state that satisfies the derivation's premises but yields a different result or an unintended blocked path`、`the boundary states examined and why each yields the same result`（`grep -cF` 各為 1）
- [x] **[存在性]** 同段含 `If neither record is present, mark \`Missing Contract Expansion\``、`treat it as a \`Decision-Fork\` finding`、`The reviewer is not required to produce the counterexample` 與 `a fail-closed block is not by itself a defect, only a block that contradicts the approved goal`（`grep -cF` 各為 1）
- [x] **[存在性]** `git diff` 新增行不含 `Medium`、`verdict`、`token`、`REASON_CODES`，且無新增 heading、表格、清單層級、reason code、Gate 或 lifecycle 字樣；原錨點 `If not, mark \`Missing Contract Expansion\`.` 仍恰 1 次
- [x] **[存在性]** 收尾實錄含「走查」W1–W8 逐條對照（附 `檔案:行號`）、「模型遵循度未驗」限制，以及「隔離成對評測未授權、未執行」，且未宣稱 `[行為性]` 驗證或任何品質指標提升
- [x] **[存在性]** `/mdlint` 對 `SKILL.md` 的 warning 數不高於 AI-0 所記 baseline
- [x] **[存在性]** `FORBIDDEN` 所列各檔 `git diff` 為空
- [x] ~~`assembleDebug` PASS~~ (N/A: docs preset)
- [x] `git diff --stat <DIFF_REF>..<DIFF_END>` 僅涉及 ALLOWED 檔案（範圍見本節首；他票 dirty 檔與他票提交觸及之檔案不計）

## 已知限制（非阻擋，如實揭露）

1. **模型遵循度未驗；分階段驗收**：全部 AC 為 `[存在性]`。Template 的「Guard／gate／early-return → 至少含 `[可達性]`＋`[行為性]`」規則（Template v3.11 L163）之 `[可達性]` 定義以 `app/src/main/` production producer 為語境（L159）；本票的 gate 為 skill 指令文字，無 production producer，故採分階段驗收：①票內驗條文、引用、靜態一致性與 W1–W8 文字走查；②真實行為依後續真實 RGR 審查是否點名票面缺失的反例紀錄、處置是否依條文，由 REVIEW→CLOSED 審查方確認。未觸發或證據不足時仍為未驗，不回補更高等級驗證、不宣稱已驗。
2. 動機證據為少數案例（TKA-BATCH-01 的 Rev A.3→A.4 翻案、TKA-EVGATE-01 的作者側條文），「現行文字不要求」不等於「審查從不抓到」；缺口判斷屬現行文字比對，不構成對本條文有效性的證明。
3. **隔離成對評測未授權、未執行**（Akira 2026-10-21 裁決）；plan §8 的 E1 預期僅作預先寫定的判準，未實測。誤擋率、審查成本、token 與時間資料皆無（UNKNOWN）。
4. TKA-EVGATE-01 的反例紀錄落在作者側 evidence receipt，P0 不設持久 ledger（`ticket-author/references/state-transitions.md:49`）；TKA 之後開出的正式票是否把該紀錄寫進票面尚不知（EVGATE-01 後尚無新的真實 ticket 呼叫）。本票條文因此以「票面」為核對對象，不依賴 receipt。
5. 執行時 `RGR-EQUIV-01`、`RGR-RERUN-01` 為同檔票，須序列執行；`SKILL.md` 為共用 skill 檔，AI-0 ⑥ 於派工當刻重驗。
6. **既有缺口（不修）**：Severity 表沒有 Medium 判準列，Missing Contract Expansion 亦無專列（`SKILL.md:156-162`）；實務上靠先例落在 Medium。本票條文依 R8／N2 對嚴重度保持沉默，因此缺紀錄 finding 的等級仍由既有規則與先例決定，審查者之間可能不一致。補正屬後續議題。
7. **存在即退出的敷衍風險**：退出條件只看紀錄是否存在（plan T01 退出條件，Akira 核定）；一筆敷衍的已檢查狀態紀錄，或記了反例卻未給處置，本段都不會攔下。承接者：REVIEW→CLOSED 審查方與後續真實 RGR 審查。
8. **與兄弟票重疊觸發**：同一個等價、相容或重試宣稱，可能同時觸發本段與 RGR-EQUIV-01（Step 5）、RGR-RERUN-01（Step 3 清單項）的檢查，同一缺口可能被記兩筆；去重由該兩票的 RGR 與落地處理，本票不處理。

## 收尾實錄（執行端；2026-10-21；Mode: normal）

### 1. 基線與實作

- 開工 HEAD `cf12e18a`（`DIFF_REF` 回填）；`SKILL.md` blob `24d2d444` 與開票時相同，`c8385e64..HEAD` 無觸及該檔之提交（無漂移）。同 plan 兄弟票 RGR-EQUIV-01、RGR-RERUN-01 當時 lifecycle 為 `TICKET_RIGOR`（非 `IN_PROGRESS`），屬資訊性，依 AI-0 ⑥ 續作。
- AI-0 ①–⑨ live 驗證：①②③ 各恰 1，④ `boundary states examined` 修改前 0，⑤ check-ignore exit 1，⑥ 無 `IN_PROGRESS` 兄弟票，⑧ 無漂移。⑦ 見 §2。
- AI-1 已落地並提交 `ebd96a78`：Step 3 末（`SKILL.md` 錨點行之後）新增 1 空行＋1 段落，目標值逐字；`git diff` ＝ 1 file、+2（空行與段落）／−0；`DIFF_END=ebd96a78`。檔案為 CRLF，新增行同為 CRLF。

### 2. mdlint

未跑 `/mdlint` 掃描（會自動修復）。替代論證：僅新增一個以空行分隔的段落，未增刪 heading、fence、表格或清單層級；此為論證而非掃描結果。

### 3. 檢核與走查

- 目標段落七片語各 1 次；新增行不含 `Medium`、`verdict`、`token`、`REASON_CODES`；錨點 `If not, mark …` 仍 1 次；FORBIDDEN 各檔 diff 為空。
- 走查 W1–W8 對照 `SKILL.md` Step 3 新增段（文字級，非 `[行為性]`）：缺紀錄→Missing Contract Expansion；有紀錄且指向核定規則→退出；紀錄存在但無核定決策可定→Decision-Fork；無推導之票不觸發；與核定目標一致之 fail-closed 阻塞不判缺陷；真等價列邊界狀態即合格；與核定目標相悖之阻塞→Missing Contract Expansion；審查者自行發現未記錄狀態→Missing Contract Expansion（Decision-Fork 小節獨立適用）。

### 4. 限制與如實揭露

模型遵循度未驗；隔離成對評測未授權、未執行；未宣稱 Traycer 通過率、token 效率或品質提升。風險分級（純 `.md`、可 revert）省略 `/qa`、`/sv`、Cross-Review，`reviews` 四欄 `N_A`。提交前 `check commit` exit 0。
