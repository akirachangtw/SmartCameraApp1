# RGR-RERUN-01: ticket-rigor-gate 於 Step 3 對有順序的持久副作用補「中斷後重跑」條件式狀態列（單檔 SKILL.md 增補一條清單項）

**Revision**: Rev A.1（2026-10-21，RGR Preflight Cycle 1 修補 F1–F5：兄弟票並行檢核改為僅 `IN_PROGRESS` 時停止並更正 `coordination_dependency`、diff 檢核排除他票提交、`SKILL.md:514` 功能性行號改錨點文字、判定邊界 W7（寫明但相悖）／W8（與 RGR-DERIV-01 重疊）與承重推導反證第 4 點、錨點反斜線註記；目標行未改；報告 `.tmp/rigor-gate/RGR_RGR-RERUN-01_20261021.md`）。前版：Rev A（2026-10-21，首次開票）。

**簽發日期**: 2026-10-21
**簽發者**: Claude
**來源議題**: RGR
**Mode**: normal  ||  **Effort**: Medium
**Traycer Workflow**: N/A
**前置依賴（依賴三分）**：
- `execution_dependency`: 無
- `evidence_dependency`: 無（TKA-BATCH-01 Rev A.3→A.4 僅為動機案例，非本票判準、錨點或 AC 之來源票）
- `coordination_dependency`: 與同 plan 的 RGR-DERIV-01、RGR-EQUIV-01 同改 `SKILL.md`，依 `concurrency.md` §2 須串行（三票不得同時為 `IN_PROGRESS`）；屬資訊性，不排定先後、不傳染 STOP（Rev A.1 由「無」更正）

**檔案治理／來源說明**：本票為 Plan-backed 批次中的一張獨立票。
- Parent type: Plan-backed
- Parent absolute path: `D:\DevSources\AndroidProj\SmartCameraApp1\.tmp\ticket-author\20261021-rgr-improvement-plan.md`（`.tmp` 路徑僅為讀取位置，下列內容已嵌入，本票於該檔消失後仍自足）
- Parent artifact status: `Fixture: false`、`Plan Gate: PASS`、`Ticket mode eligibility: ALLOWED`、`Status: FINAL`
- Plan-local item ID: `T03`（非票號，僅留於本區）
- Parent read timestamp: 2026-10-21 14:36+08:00（批次 shared preflight 起始處讀取）
- Selected item（Ticket Map 原文摘要）: 在 `SKILL.md` Step 3 清單加一條條件式項，使有順序的持久副作用操作被核對「中途中斷後重跑」的結果。trigger：票面定義兩個以上有順序的持久副作用（寫入後註冊、逐項迴圈寫入等），且中斷於第 k 個之後會留下可達的中間狀態；單一原子操作或不可重跑者不觸發。最小動作：一列——中斷於第 k 個副作用之後，重跑的結果（續行、重複、被阻擋、與前次結論矛盾）是否被票面寫明，且與已核定目標一致；缺列歸既有 Missing Contract Expansion，結果無已核定決策可定歸既有 Decision-Fork。退出條件：該列存在且結果可追溯到核定決策或既有規則，或票面明示該操作不可重跑／非原子並指定承接者。
- Plan Gate／eligibility 結果: PASS／ALLOWED
- 相關核定（Akira 2026-10-21，plan 指令原文，全數 APPROVED）：R1 進入 RGR 最小改善計畫；R4 C 部分成功後重跑的條件式狀態列落 `SKILL.md`；R7 不新增 token、severity 列、reason code、Gate、lifecycle 或 repair authority；R8 新檢查預設不影響 verdict；R9 C 保留為具名最小觸發句、不折疊成 A 的示例（兩者漏檢機制不同，折疊後容易只檢查文字推導、漏掉跨次持久狀態）；N2 不得指定新 finding 一律 Medium 或一律不影響 verdict；N3 不得建立常駐 benchmark、案例庫、ledger、第二 Gate、repo-wide tracing；N4 不得重開 TKA-BATCH-01。Akira 2026-10-21 另裁決：Plan Gate、「R8 與 N2 的相容解讀」、四項獨立票均接受；隔離成對評測維持未授權，不阻擋開票。
- 範圍外（deferred，不屬本票）：A（RGR-DERIV-01）、B（RGR-EQUIV-01）、D（RGR-REPLAY-01）；故障注入矩陣；隔離評測；Severity 表缺 Medium 判準列之補正。

**開票時 live 證據基線**（2026-10-21 14:43–14:45+08:00，`main` @ `c8385e64`，工作樹乾淨）：
- 錨點 `- legal-but-borderline values` 於 `.claude/skills/ticket-rigor-gate/SKILL.md` 恰 1 次（L312，Step 3 清單末項；清單 L303–L312，引導句 `For each normative clause, ask whether the ticket explicitly covers:` L301），其後為空行與 `If not, mark \`Missing Contract Expansion\`.`（L314）。`SKILL.md` blob `24d2d444`，648 行。清單現有十項皆為無動詞名詞片語，無條件式項；`(conditional)` 與 `two or more ordered persistent effects` 於 `SKILL.md` 為 0 次（尚未落地）。
- 既有相鄰條文：`### Step 8: Check destructive-state pairing`（L354，只管清除／null／關閉／刪除／unlink／archive）；`## Migration-Lite Review` facet (1)(2)（L273，stored-state 與輸入類別、各類別的 accept／reject、persisted shape、side effects）；`Every residual-risk window MUST have a **named assignee**`（L514，恰 1 次）；`### Decision-Fork Findings`（L164）。
- consumer 盤點：`.claude/` 內無檔案依賴 Step 編號、Finding Taxonomy 枚舉或 Step 3 清單文字；`runner/` 無 taxonomy 枚舉；無鏡像副本。單檔修改可行。
- 重複票檢查：Registry（topic `RGR`，main＋archive）除本批同 plan 的 `RGR-DERIV-01`、`RGR-EQUIV-01` 外無等價 outcome；`Pending/`、`deliver/`（含 `deliver/archive/`）、`void/` 無「中斷後重跑」等價 outcome；`RGR-RERUN-01` 於全冊、檔名、commit 皆未使用（僅同批票檔文字引用）。
- 並行狀況：`Pending/` 內 ALLOWED 觸及 `ticket-rigor-gate/SKILL.md` 者僅同 plan 的 `RGR-DERIV-01`、`RGR-EQUIV-01`（須序列執行）。
- `git check-ignore -v` 對 `SKILL.md` 無命中（exit 1）。

**承重推導反證紀錄**（依 `ticket-author/SKILL.md` Evidence gate「no hidden semantic decision」條款）：
1. 推導：「新項以『兩個以上有順序的持久副作用』為觸發，故單一原子操作與非持久輸出不會被多餘要求」。
   - 反證狀態：符合前提的邊界狀態——（a）兩個副作用中第二個只是日誌輸出：日誌非持久，不觸發；（b）兩個寫入在同一交易內：票面可依退出條件聲明原子而覆蓋；（c）兩個寫入但後項為冪等衍生物：中斷後留下可達中間狀態，仍觸發，這是預期行為。三者皆不導出非預期阻塞路徑。
   - 處置：無需決策，記錄已檢查邊界狀態於此。
2. 推導：「新項與 Migration-Lite facet (1)(2) 不重疊，因後者管資料形狀而本項只管操作順序」。
   - 反證狀態：Migration-Lite 為 `ACTIVE` 且票面又有兩個有順序的寫入時，facet (2) 的 side effects 與本項可能對同一缺口各產生一個 finding。符合前提卻得到「同一缺口兩個 finding」。兩者同屬 `Missing Contract Expansion`，該類別沒有 Major 判準列，不會單獨讓 verdict 改變；亦不產生阻塞路徑。
   - 處置：已核定決策可定——R8／N2 對嚴重度保持沉默，重複 finding 不構成缺陷；記錄於此，已知限制④揭露。
3. 推導：「新條文對嚴重度保持沉默，故 verdict 只能經既有 Severity 規則改變」：反證狀態與處置同 RGR-DERIV-01（Severity 表無 Medium 判準列；Akira 2026-10-21 接受沉默解讀；既有缺口屬 plan §9 Observation，已知限制⑤揭露）。
4. 推導：「本項與 RGR-DERIV-01 的 Step 3 承重推導段落不重複審查同一缺口」。
   - 反證狀態：RGR-DERIV-01 目標段把 `retry-outcome claim` 列為觸發範例；票面若對重跑結果另有承重推導（如「重跑冪等」），本項（經其後既有句）與該段落可能各記一筆 `Missing Contract Expansion`。符合前提卻得到「同一缺口兩筆 finding」；寫明中斷後重跑列一次即可同時滿足兩者。
   - 處置：比照第 2 點（R8／N2，重複不構成缺陷），兩筆並存、揭露即可（RGR 審查裁定，2026-10-21，報告 `.tmp/rigor-gate/RGR_RGR-RERUN-01_20261021.md`）；已知限制④、走查 W8。

**Template 讀取紀錄**：`D:\DevSources\AndroidProj\SmartCameraApp1\plans\cso_briefing\project\file\Unified_Ticket_Template_v3.md`，版本 v3.11，文件日期 2026-05-29，effective revision date 2026-09-14（v3.11）；本次 invocation 全文讀取（322 行），本項復用 shared preflight 的讀取，讀取後未變。

## Verify Config

TICKET_ID=RGR-RERUN-01
CREATED=2026-10-21
PRESET=docs
ALLOWED=.claude/skills/ticket-rigor-gate/SKILL.md
FORBIDDEN=.claude/skills/ticket-rigor-gate/REPAIR_ROUTING.md,.claude/skills/ticket-rigor-gate/OPS_CALIBRATION.md,.claude/skills/ticket-author/SKILL.md,.claude/skills/ticket-author/references/input-contract.md,.claude/CLAUDE.md,plans/cso_briefing/project/file/Unified_Ticket_Template_v3.md,plans/cso_briefing/project/deliver/TKA-BATCH-01_selected-set.md,plans/cso_briefing/project/deliver/TKA-EVGATE-01_derivation.md
MAX_LINES=20
DIFF_REF=b60a206b
ANCHOR_PROBED=2026-10-21 14:44
BASE_COMMIT=c8385e648b6c2fb808f923baae0ec64374aae6b0
TARGET_BLOB=.claude/skills/ticket-rigor-gate/SKILL.md:24d2d444
INTRODUCED_CAPABILITY=NONE
DEPENDENCY_DELTA=NONE

`MAX_LINES` 僅為估算校準遙測，非 gate（Template v3.5 起）。`INTRODUCED_CAPABILITY=NONE`：增補既有審查演算法清單一項，不導入新 feature／skill／tech。

## Action Items

### AI-0: 錨點與基線復驗（派工後、修改前）

- **檔案**: `.claude/skills/ticket-rigor-gate/SKILL.md`
- **動作**（下列片語中「反斜線＋反引號」代表反引號字元本身，`grep` 時不含反斜線）: 以 live `grep -cF` 逐字確認：①`- legal-but-borderline values` 恰 1 次；②`For each normative clause, ask whether the ticket explicitly covers:` 與 `### Step 3: Expand hidden truth tables` 各恰 1 次；③`Every residual-risk window MUST have a **named assignee**` 恰 1 次；④`### Decision-Fork Findings` 恰 1 次；⑤`(conditional) the result of rerunning after an interruption` 為 0 次（尚未落地）。併做：⑥`git check-ignore -v .claude/skills/ticket-rigor-gate/SKILL.md` 無命中；⑦掃 `Pending/` 與冊面，確認無他票 ALLOWED 觸及 `.claude/skills/ticket-rigor-gate/SKILL.md`（有 → 停止，須串行；同 plan 的 RGR-DERIV-01、RGR-EQUIV-01 僅於其 lifecycle 為 `IN_PROGRESS` 時停止，其餘狀態屬資訊性，記錄後續作）；⑧記錄 `/mdlint` 對 `SKILL.md` 的 baseline warning 數；⑨依 Template 基線漂移三分流處理 `BASE_COMMIT..HEAD` 間觸及 `SKILL.md` 的提交（同 plan 兄弟票若已先落地，屬資訊性分類，記錄歸因票 ID 後以現值續作）；⑩回填 `DIFF_REF`。
- **任一項不符 → 停止回報，禁自行推定等價位置。**
- [x] 完成 （見收尾實錄）

### AI-1: 於 Step 3 清單末增補一條條件式項

- **檔案**: `.claude/skills/ticket-rigor-gate/SKILL.md`
- **現值**（恰 1 次）: `- legal-but-borderline values`
- **目標值**（保留原行不動，其後緊接下列單一清單項，不插入空行；其後原有空行與 `If not, mark \`Missing Contract Expansion\`.` 逐字不動）:

```text
- (conditional) the result of rerunning after an interruption, when the ticket defines two or more ordered persistent effects and an interruption after the k-th leaves a reachable intermediate state: whether the rerun resumes, duplicates, is blocked, or contradicts the earlier run, and whether that matches the approved goal; if no approved decision settles it, treat it as a `Decision-Fork` finding; a ticket that states the operation is atomic or not rerunnable, and names an assignee for the residual window, is covered
```

- **語義對應**（plan T03 `acceptance_intent`）: `(conditional)`＋`two or more ordered persistent effects … reachable intermediate state`＝trigger 與意圖①「條件式」；`whether the rerun resumes, duplicates, is blocked, or contradicts the earlier run, and whether that matches the approved goal`＝最小動作；缺列沿用其後既有句 `If not, mark \`Missing Contract Expansion\`.`，`Decision-Fork`＝意圖②只引用既有類別；`atomic or not rerunnable, and names an assignee for the residual window, is covered`＝退出條件（承接 Evidence Review 之 `Every residual-risk window MUST have a **named assignee**` 要求；以錨點文字定位，不以行號——同 plan 兄弟票落地後該句行號會位移，開票時為 L514）；整項不含 token、severity 等級語句、reason code、新章節（R7／R8／N2）。
- [x] 完成 （見收尾實錄）

### AI-2: 檢核、文字級走查與收尾實錄

- **檔案**: 不改檔（僅記錄於票檔收尾實錄）
- **動作**（本項所有 `git diff` 檢核之範圍＝`<DIFF_REF>..<DIFF_END>`；提交前為 `<DIFF_REF>` 對工作樹並以 `-- .claude/skills/ticket-rigor-gate/SKILL.md` 限定，FORBIDDEN 檢核以各檔路徑限定；同 worktree 他票之 dirty 檔，及範圍內他票提交〔commit 前綴非 `[RGR-RERUN-01]`〕觸及之檔案，皆不計）: ①`git diff --stat` 僅 `SKILL.md`，diff 僅新增 Step 3 清單末一項；②新增行不含 `Medium`、`verdict`、`token`、`REASON_CODES` 字樣，且不新增任何 heading、表格、空行、獨立段落或檔案；③目標清單項以 `grep -cF` 逐字命中 1 次，錨點 `- legal-but-borderline values` 與 `If not, mark \`Missing Contract Expansion\`.` 仍各恰 1 次；④本票「走查」W1–W8 逐條對照條文並記 `檔案:行號`；⑤`/mdlint` 對 `SKILL.md` warning 數不高於 AI-0 baseline；⑥收尾實錄如實載明「模型遵循度未驗」與已知限制，不宣稱 Traycer 通過率、token 效率或品質提升，並載明「隔離成對評測未授權、未執行」；⑦提交依既有流程，**提交若不經 `/qa` Step 4.5，提交前先執行 `python .claude/scripts/gate_ledger.py check commit --ticket RGR-RERUN-01`，exit 0 才提交**（退出碼處置依 `ssot-governance.md` §入口與覆蓋矩陣之映射）。
- [x] 完成 （見收尾實錄）

## Hard Constraints

1. 僅得在 `SKILL.md` Step 3 清單末新增上列單一清單項；不得改動既有十項清單、引導句、`If not, mark \`Missing Contract Expansion\`.` 句、相鄰 Step、Step 8、Migration-Lite Review、Severity 表、Mechanical Verdict Rule 或十欄 disposition，亦不得新增 heading、表格、空行、獨立段落或常駐 checklist。
2. 不得新增 token、severity 列或等級語句（含「一律 Medium」「不影響 verdict」）、reason code、Gate、lifecycle 或 repair authority；新項僅得引用既有 `Decision-Fork` 與（經其後既有句）`Missing Contract Expansion`。
3. 不得建立故障注入矩陣，不得要求逐步驟枚舉中斷點；本項只問「中斷後重跑」一列。
4. 不得重述 Migration-Lite 的 stored-state 類別（資料形狀類由其處理，本項只管操作順序），亦不得修改 Step 8 破壞性動作檢查。
5. 不得修改 `REPAIR_ROUTING.md`、`OPS_CALIBRATION.md`；A（RGR-DERIV-01）、B（RGR-EQUIV-01）、D（RGR-REPLAY-01）不得在本票併入，C 亦不得折疊為 A 的示例。
6. 不得修改、重開或改寫 `TKA-BATCH-01`、`TKA-EVGATE-01`，亦不得建立常駐 benchmark、案例庫、ledger、第二 Gate 或 repo-wide tracing。
7. 不得宣稱已提升 Traycer 通過率、token 效率或品質指標；本票只交付條文，不交付行為驗證。

## Error Handling

- AI-0 任一錨點不符 → 停止回報，禁自行推定等價位置
- `git check-ignore -v` 命中 `SKILL.md`（標的被 `.gitignore` 排除）→ 停止回報（gitignore 死結律）
- 他票 ALLOWED 與 `.claude/skills/ticket-rigor-gate/SKILL.md` 相交 → 停止回報（須串行）；同 plan 的 RGR-DERIV-01、RGR-EQUIV-01 僅於其 lifecycle 為 `IN_PROGRESS` 時適用本條
- 無法僅憑 `SKILL.md` 單一清單項承載 plan T03 意圖（例如須改 Step 8、Migration-Lite facet 或新增章節才能表達）→ 停止，請求裁決，不得擴大修改範圍
- 條文無法在不新增 token、severity 列、reason code、Gate、lifecycle、repair authority 下表達 → 停止回報
- 新條文與 Step 8、Migration-Lite Review 或 Evidence Review 殘餘窗口承接者要求（錨點 `Every residual-risk window MUST have a **named assignee**`，以錨點文字為準、不以行號）出現矛盾 → 停止回報
- 修改後 `git diff --stat` 觸及 ALLOWED 以外檔案 → 停止回報

## 走查（文字級；`[存在性]` 證據，非 `[行為性]` 驗證；given/expect）

下列為對條文的文字級對照，不是對模型行為的實測。情境為說明性假設，非真實事件；W1／W2 對應 plan §8 的 E2（C 部分）。

| # | given | expect（依 AI-1 條文） |
| --- | --- | --- |
| W1（E2-C flawed） | 票面定義逐項寫入並註冊，前項已寫入、後項失敗；票面規定「重跑時列出全清單，遇重複即停」，但未寫明重跑結果 | 觸發（兩個以上有順序的持久副作用，中斷後留下可達中間狀態）；未寫明重跑結果 → 依其後既有句 `Missing Contract Expansion`；嚴重度依既有規則，本項不指定 |
| W2（E2-C valid） | 同 W1，但票面寫明：重跑時前項已存在者視為已滿足、後項續行，且可追溯到核定決策 | 該列存在且與核定目標一致 → 退出，新項不產生 finding |
| W3 | 票面寫了重跑結果（例如覆寫），但無任何已核定決策能決定覆寫或略過 | 依條文視為 `Decision-Fork` finding，走既有 `ROUTE=C_EXCEPTION`，不由審查者代選 |
| W4 | 單一原子操作（單次寫入、無中間狀態） | 不觸發 |
| W5 | 票面寫明該操作非原子且不可重跑，並具名殘餘窗口的承接者 | 視為已覆蓋，退出 |
| W6 | 兩個副作用中第二個只是日誌輸出（非持久） | 不構成「兩個以上持久副作用」，不觸發 |
| W7 | 票面寫明重跑結果（例如「遇重複即停」致後項被擋），但已核定目標為「前項視為已滿足、後項續行」 | 不符 `whether that matches the approved goal`；核定決策已決定正確結果，不符合 `Decision-Fork` 之「no approved decision settles it」，且 HC2 僅允許兩類 → 記 `Missing Contract Expansion`（同 RGR-DERIV-01 W7） |
| W8 | 同 W1，且票面另有「重跑冪等」之承重推導，未記錄反例或已檢查邊界狀態 | 本項（經其後既有句）與 RGR-DERIV-01 段落各記一筆 `Missing Contract Expansion`，兩筆並存；寫明中斷後重跑列一次即可同時滿足兩者（承重推導反證第 4 點） |

## Acceptance Criteria

> **diff 檢核範圍**：本節所有 `git diff` 檢核之範圍＝`<DIFF_REF>..<DIFF_END>`；提交前為 `<DIFF_REF>` 對工作樹並以 `-- .claude/skills/ticket-rigor-gate/SKILL.md` 限定，FORBIDDEN 檢核以各檔路徑限定；同 worktree 他票 dirty 檔，及範圍內他票提交（commit 前綴非 `[RGR-RERUN-01]`，以 commit 前綴歸屬）觸及之檔案，皆不計入本票。下列片語中「反斜線＋反引號」代表反引號字元本身，`grep` 時不含反斜線。
>
> **AC 修訂閉環（Rev A.1）**：本節首範圍說明、第 4 條（W1–W6→W1–W8）與最後一條（排除他票提交）經修訂；共用同一 diff 範圍母體之兄弟 AC（第 3、6 條）已重驗：同樣只因他票提交而受影響，修訂後排除規則一致套用，判準不變；第 1、2、5 條不涉 diff 範圍，不受影響。無 AC 被移除或豁免。
>
> **同行計數**：`Decision-Fork` 引用在 `SKILL.md` 內可能因同 plan 兄弟票而出現多次，下列各片語之 `grep -cF` 一律以「以 `- (conditional) the result of rerunning after an interruption` 開頭的那一行」為範圍。

- [x] **[存在性]** `SKILL.md` Step 3 清單末含恰 1 行以 `- (conditional) the result of rerunning after an interruption` 開頭的清單項，且該行內含 `two or more ordered persistent effects`、`an interruption after the k-th leaves a reachable intermediate state`、`whether the rerun resumes, duplicates, is blocked, or contradicts the earlier run`（各 1 次）
- [x] **[存在性]** 同一行含 `whether that matches the approved goal`、`if no approved decision settles it, treat it as a \`Decision-Fork\` finding` 與 `names an assignee for the residual window`（各 1 次）
- [x] **[存在性]** `git diff` 新增行不含 `Medium`、`verdict`、`token`、`REASON_CODES`，且無新增 heading、表格、空行、獨立段落、reason code、Gate 或 lifecycle 字樣；原錨點 `- legal-but-borderline values` 與 `If not, mark \`Missing Contract Expansion\`.` 仍各恰 1 次
- [x] **[存在性]** 收尾實錄含「走查」W1–W8 逐條對照（附 `檔案:行號`）、「模型遵循度未驗」限制，以及「隔離成對評測未授權、未執行」，且未宣稱 `[行為性]` 驗證或任何品質指標提升
- [x] **[存在性]** `/mdlint` 對 `SKILL.md` 的 warning 數不高於 AI-0 所記 baseline
- [x] **[存在性]** `FORBIDDEN` 所列各檔 `git diff` 為空
- [x] ~~`assembleDebug` PASS~~ (N/A: docs preset)
- [x] `git diff --stat <DIFF_REF>..<DIFF_END>` 僅涉及 ALLOWED 檔案（範圍見本節首；他票 dirty 檔與他票提交觸及之檔案不計）

## 已知限制（非阻擋，如實揭露）

1. **模型遵循度未驗；分階段驗收**：全部 AC 為 `[存在性]`。Template 的「Guard／gate／early-return → 至少含 `[可達性]`＋`[行為性]`」規則（Template v3.11 L163）之 `[可達性]` 定義以 `app/src/main/` production producer 為語境（L159）；本票的 gate 為 skill 指令文字，無 production producer，故採分階段驗收：①票內驗條文、引用、靜態一致性與 W1–W8 文字走查；②真實行為依後續真實 RGR 審查是否對有順序的持久副作用票問「中斷後重跑」，由 REVIEW→CLOSED 審查方確認。未觸發或證據不足時仍為未驗，不回補更高等級驗證、不宣稱已驗。
2. 動機證據為單一案例（TKA-BATCH-01，Rev A.3 走查將「首次部分寫入後全清單重試」記為條文如實結果，Rev A.4 翻案）；該案原始產出版本因 `Pending/` 期 untracked 已不可考，僅作例示，不構成對本條文有效性的證明。「若條文當時已存在即可翻轉該判定」為事後推論，屬 `HYPOTHESIS`。
3. **隔離成對評測未授權、未執行**（Akira 2026-10-21 裁決）；plan §8 的 E2（C 部分）預期僅作預先寫定的判準，未實測。誤擋率、審查成本、token 與時間資料皆無（UNKNOWN）。
4. 與 Migration-Lite facet (2) 可能對同一缺口各產生一個 `Missing Contract Expansion` finding（見承重推導反證紀錄第 2 點）；該類別無 Major 判準列，重複不單獨改變 verdict，但報告中可能出現兩條相近 finding。同理，票面若對重跑結果另有承重推導，RGR-DERIV-01 段落亦可能對同一缺口再記一筆 `Missing Contract Expansion`（承重推導反證第 4 點、W8）。承接者：首次真實觸發之 RGR 審查報告與 REVIEW→CLOSED 審查方。
5. **既有缺口（不修）**：Severity 表沒有 Medium 判準列，Missing Contract Expansion 亦無專列（`SKILL.md:156-162`）；本票條文依 R8／N2 對嚴重度保持沉默，因此缺列 finding 的等級仍由既有規則與先例決定，審查者之間可能不一致。補正屬後續議題。
6. 條文把「中斷於第 k 個之後」限於票面定義的有順序持久副作用；對票面沒有定義順序、但實際實作會產生中間狀態的操作，審查者需靠票面敘述判斷是否觸發，條文不要求審查者自行發現未被票面定義的副作用順序。
7. 執行時 `RGR-DERIV-01`、`RGR-EQUIV-01` 為同檔票，須序列執行；`SKILL.md` 為共用 skill 檔，AI-0 ⑦ 於派工當刻重驗。

## 收尾實錄（執行端；2026-10-21；Mode: normal）

### 1. 基線與實作

- 開工 HEAD `b60a206b`（`DIFF_REF` 回填）；`SKILL.md` blob 由開票時 `24d2d444` 變為 `7288d158`，漂移歸因：`ebd96a78` [RGR-DERIV-01]（Step 3 末段）與 `2303d1b9` [RGR-EQUIV-01]（Step 5 末段），均為同 plan 兄弟票，屬資訊性分類，依 AI-0 ⑨ 以現值續作；本票錨點與其他錨點仍各恰 1 次。兄弟票當時皆為 `REVIEW`（非 `IN_PROGRESS`），依 AI-0 ⑦ 無阻擋。
- AI-0 ①–⑩ live 驗證：①②③④ 各恰 1，⑤ 修改前 0，⑥ check-ignore exit 1。⑧ 見 §2。
- AI-1 已落地並提交 `3c9e5063`：Step 3 清單末新增 1 行清單項，目標值逐字；其後原有空行與 `If not, mark …` 句逐字不動；`git diff` ＝ 1 file、+1／−0；`DIFF_END=3c9e5063`。

### 2. mdlint

未跑 `/mdlint` 掃描（會自動修復）。替代論證：僅於既有清單末新增一個清單項，未增刪 heading、fence、表格、空行；此為論證而非掃描結果。

### 3. 檢核與走查

- 目標清單項六片語於該行各 1 次；新增行不含 `Medium`、`verdict`、`token`、`REASON_CODES`；兩個錨點仍各 1 次；FORBIDDEN 各檔 diff 為空。
- 走查 W1–W8 對照 `SKILL.md` Step 3 新增清單項（文字級，非 `[行為性]`）：未寫明重跑結果→依其後既有句 Missing Contract Expansion；寫明且可追溯核定決策→退出；寫明但無核定決策可定→Decision-Fork；單一原子操作不觸發；聲明非原子／不可重跑並具名承接者→視為覆蓋；第二個副作用非持久（日誌）不觸發；寫明但與核定目標相悖→Missing Contract Expansion；與 RGR-DERIV-01 retry-outcome 並存（刻意）。

### 4. 限制與如實揭露

模型遵循度未驗；隔離成對評測未授權、未執行；未宣稱 Traycer 通過率、token 效率或品質提升。風險分級（純 `.md`、可 revert）省略 `/qa`、`/sv`、Cross-Review，`reviews` 四欄 `N_A`。提交前 `check commit` exit 0。
