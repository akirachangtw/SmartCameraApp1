# RGR-EQUIV-01: ticket-rigor-gate 於 Step 5 對非資料類的等價宣稱，在票面宣告範圍內以一個足以改變分支結果的條件確認兩變體結果（單檔 SKILL.md 增補一段）

**Revision**: Rev A.1（2026-10-21，RGR Preflight Cycle 1 修補 F1–F5：兄弟票並行檢核改為僅 `IN_PROGRESS` 時停止並更正 `coordination_dependency`、diff 檢核排除他票提交、確認對象＝票面證據與確認失敗處置（W7／W8）、與 RGR-DERIV-01 重疊觸發刻意並存（W9、已知限制⑦、承重推導反證第 3 點）、錨點反斜線註記；目標段未改；報告 `.tmp/rigor-gate/RGR_RGR-EQUIV-01_20261021.md`）。前版：Rev A（2026-10-21，首次開票）。

**簽發日期**: 2026-10-21
**簽發者**: Claude
**來源議題**: RGR
**Mode**: normal  ||  **Effort**: Medium
**Traycer Workflow**: N/A
**前置依賴（依賴三分）**：
- `execution_dependency`: 無
- `evidence_dependency`: 無（TKA-BATCH-01 RGR 報告 F4〔母體等同宣稱〕僅為動機案例，非本票判準、錨點或 AC 之來源票）
- `coordination_dependency`: 與同 plan 的 RGR-DERIV-01、RGR-RERUN-01 同改 `SKILL.md`，依 `concurrency.md` §2 須串行（三票不得同時為 `IN_PROGRESS`）；屬資訊性，不排定先後、不傳染 STOP（Rev A.1 由「無」更正）

**檔案治理／來源說明**：本票為 Plan-backed 批次中的一張獨立票。
- Parent type: Plan-backed
- Parent absolute path: `D:\DevSources\AndroidProj\SmartCameraApp1\.tmp\ticket-author\20261021-rgr-improvement-plan.md`（`.tmp` 路徑僅為讀取位置，下列內容已嵌入，本票於該檔消失後仍自足）
- Parent artifact status: `Fixture: false`、`Plan Gate: PASS`、`Ticket mode eligibility: ALLOWED`、`Status: FINAL`
- Plan-local item ID: `T02`（非票號，僅留於本區）
- Parent read timestamp: 2026-10-21 14:36+08:00（批次 shared preflight 起始處讀取）
- Selected item（Ticket Map 原文摘要，含 Akira 2026-10-21 收窄文字）: 在 `SKILL.md` Step 5 補一個觸發句：在票面已宣告的入口至觀察者範圍內，選擇一個足以改變分支結果的條件，確認兩個變體仍導出所承諾的可觀察結果。審查者不必逐一比較各變體、追蹤未點名路徑、consumer、metadata 或內部實作細節。不屬 Migration-Lite 資料類才觸發；僅比較變更點者歸既有 Evidence Gap；超出票面宣告的 repo 範圍不追。重用既有 `[可達性]` 軌跡標準，不新增軌跡定義。trigger：票面宣稱兩個變體（新舊邏輯、單數與複數形式、同一母體的兩個查詢）等價、相容或行為不變。退出條件：已選一個有判別力的條件並確認兩變體結果，或票面明示不宣稱等價。
- Plan Gate／eligibility 結果: PASS／ALLOWED
- 相關核定（Akira 2026-10-21，plan 指令原文，全數 APPROVED）：R1 進入 RGR 最小改善計畫；R3 B 完整路徑相容的觸發句落 `SKILL.md`、重用既有可達性方法；R7 不新增 token、severity 列、reason code、Gate、lifecycle 或 repair authority；R8 新檢查預設不影響 verdict；R9 B 保留為具名最小觸發句、不折疊成 A 的示例；N2 不得指定新 finding 一律 Medium 或一律不影響 verdict；N3 不得建立常駐 benchmark、案例庫、ledger、第二 Gate、repo-wide tracing；N4 不得重開 TKA-BATCH-01。Akira 2026-10-21 另裁決：Plan Gate、「R8 與 N2 的相容解讀」、四項獨立票均接受，並提供 T02 收窄文字；隔離成對評測維持未授權，不阻擋開票。
- 範圍外（deferred，不屬本票）：A（RGR-DERIV-01）、C（RGR-RERUN-01）、D（RGR-REPLAY-01）；隔離評測；Severity 表缺 Medium 判準列之補正。

**開票時 live 證據基線**（2026-10-21 14:40–14:42+08:00，`main` @ `c8385e64`，工作樹乾淨）：
- 錨點 `If the evidence only covers helper code or grep-level existence, mark \`Evidence Gap\`.` 於 `.claude/skills/ticket-rigor-gate/SKILL.md` 恰 1 次（L331，Step 5 末句；`### Step 5: Trace the producer path` L322），其後為 `### Step 6: Check ticket atomicity`（L333）。`SKILL.md` blob `24d2d444`，648 行。
- 既有方法錨點：`## Acceptance-Criteria Typology`（L212）之 `[可達性]` 軌跡標準（L223）、`## Migration-Lite Review` 觸發（L267／L269）均恰 1 次。`one condition that would change a branch outcome` 與 `is not covered by Migration-Lite Review` 於 `SKILL.md` 為 0 次（尚未落地）。
- consumer 盤點：`.claude/` 內無檔案依賴 Step 編號、Finding Taxonomy 枚舉或 Step 5 文字；`runner/` 無 taxonomy 枚舉；無鏡像副本。單檔修改可行。
- 重複票檢查：Registry（topic `RGR`，main＋archive）除本批同 plan 的 `RGR-DERIV-01` 外無等價 outcome；`Pending/`、`deliver/`（含 `deliver/archive/`）、`void/` 無等價 outcome；`RGR-EQUIV-01` 於全冊、檔名、commit 皆未使用（僅同批票檔文字引用）。
- 並行狀況：`Pending/` 內唯一 ALLOWED 觸及 `ticket-rigor-gate/SKILL.md` 者為同 plan 的 `RGR-DERIV-01`（須序列執行）；`TKA-CLOSE-01` 的 ALLOWED 為 `ticket-author/`。
- `git check-ignore -v` 對 `SKILL.md` 無命中（exit 1）。

**承重推導反證紀錄**（依 `ticket-author/SKILL.md` Evidence gate「no hidden semantic decision」條款）：
1. 推導：「新段落以『不屬 Migration-Lite Review 涵蓋』為排除條件，故不與 Migration-Lite 重複審查同一宣稱」。
   - 反證狀態：一條宣稱同時涉及「舊記錄形狀與新形狀行為一致」（資料類）與「路由分支一致」（非資料類）。符合推導前提的狀態有兩種：若整條宣稱被 Migration-Lite 涵蓋，B 不觸發，無重複；若宣稱可拆為資料類與路由類兩條，B 只對路由類那條觸發，兩者各管各的，不重複同一宣稱。已檢查的邊界狀態皆不導出「同一宣稱被兩邊各產生 finding」或阻塞路徑。
   - 處置：無需決策，記錄於此。
2. 推導：「新段落對嚴重度保持沉默，故 verdict 只能經既有 Severity 規則改變」。
   - 反證狀態：同 RGR-DERIV-01——Severity 表無 Medium 判準列；「僅比較變更點」的 `Evidence Gap` 可能被某審查者判為 Major（既有列「evidence proves existence only where reachability is required」），也可能判為較低等級。
   - 處置：已核定決策可定——Akira 2026-10-21 接受「新條文對嚴重度保持沉默」，並要求不指定固定等級（N2）；既有缺口為 plan §9 Observation，不在本票修補，已知限制⑥揭露。
3. 推導：「本段與 RGR-DERIV-01 的 Step 3 承重推導段落各管各的，不重複審查同一宣稱」。
   - 反證狀態：一條等價宣稱若同為承重推導，會同時觸發兩段。四種組合——T01 紀錄有／無 × 本段分支證據有／無：有／有 → 兩段皆不產生 finding；有／無 → 僅本段 `Evidence Gap`；無／有 → 僅 T01 段 `Missing Contract Expansion`；無／無 → 兩筆並存。符合前提卻得到「同一宣稱兩筆 finding」。
   - 處置：兩筆指向可各自修補的不同缺口（推導紀錄 vs 路徑證據：補紀錄不補證據、補證據不補紀錄），刻意並存、不去重，嚴重度依既有規則；可能推向 PARTIAL 之風險由已知限制⑦揭露並具名承接者（RGR 審查裁定，2026-10-21，報告 `.tmp/rigor-gate/RGR_RGR-EQUIV-01_20261021.md`）。走查 W9。

**Template 讀取紀錄**：`D:\DevSources\AndroidProj\SmartCameraApp1\plans\cso_briefing\project\file\Unified_Ticket_Template_v3.md`，版本 v3.11，文件日期 2026-05-29，effective revision date 2026-09-14（v3.11）；本次 invocation 全文讀取（322 行），本項復用 shared preflight 的讀取，讀取後未變。

## Verify Config

TICKET_ID=RGR-EQUIV-01
CREATED=2026-10-21
PRESET=docs
ALLOWED=.claude/skills/ticket-rigor-gate/SKILL.md
FORBIDDEN=.claude/skills/ticket-rigor-gate/REPAIR_ROUTING.md,.claude/skills/ticket-rigor-gate/OPS_CALIBRATION.md,.claude/skills/ticket-author/SKILL.md,.claude/skills/ticket-author/references/input-contract.md,.claude/CLAUDE.md,plans/cso_briefing/project/file/Unified_Ticket_Template_v3.md,plans/cso_briefing/project/deliver/TKA-BATCH-01_selected-set.md,plans/cso_briefing/project/deliver/TKA-EVGATE-01_derivation.md
MAX_LINES=20
DIFF_REF=c74bfcd5
ANCHOR_PROBED=2026-10-21 14:41
BASE_COMMIT=c8385e648b6c2fb808f923baae0ec64374aae6b0
TARGET_BLOB=.claude/skills/ticket-rigor-gate/SKILL.md:24d2d444
INTRODUCED_CAPABILITY=NONE
DEPENDENCY_DELTA=NONE

`MAX_LINES` 僅為估算校準遙測，非 gate（Template v3.5 起）。`INTRODUCED_CAPABILITY=NONE`：增補既有審查演算法一段文字，不導入新 feature／skill／tech。

## Action Items

### AI-0: 錨點與基線復驗（派工後、修改前）

- **檔案**: `.claude/skills/ticket-rigor-gate/SKILL.md`
- **動作**（下列片語中「反斜線＋反引號」代表反引號字元本身，`grep` 時不含反斜線）: 以 live `grep -cF` 逐字確認：①`If the evidence only covers helper code or grep-level existence, mark \`Evidence Gap\`.` 恰 1 次；②`### Step 5: Trace the producer path` 與 `### Step 6: Check ticket atomicity` 各恰 1 次；③`## Acceptance-Criteria Typology` 恰 1 次，且 `A \`[可達性]\` AC MUST exercise or trace a production runtime or workflow-control path` 恰 1 次；④`Trigger: a ticket that introduces or changes field aliasing` 恰 1 次；⑤`one condition that would change a branch outcome` 為 0 次（尚未落地）。併做：⑥`git check-ignore -v .claude/skills/ticket-rigor-gate/SKILL.md` 無命中；⑦掃 `Pending/` 與冊面，確認無他票 ALLOWED 觸及 `.claude/skills/ticket-rigor-gate/SKILL.md`（有 → 停止，須串行；同 plan 的 RGR-DERIV-01、RGR-RERUN-01 僅於其 lifecycle 為 `IN_PROGRESS` 時停止，其餘狀態屬資訊性，記錄後續作）；⑧記錄 `/mdlint` 對 `SKILL.md` 的 baseline warning 數；⑨依 Template 基線漂移三分流處理 `BASE_COMMIT..HEAD` 間觸及 `SKILL.md` 的提交（同 plan 兄弟票若已先落地，屬資訊性分類，記錄歸因票 ID 後以現值續作）；⑩回填 `DIFF_REF`。
- **任一項不符 → 停止回報，禁自行推定等價位置。**
- [x] 完成 （見收尾實錄）

### AI-1: 於 Step 5 末段增補一段等價宣稱的最小核對

- **檔案**: `.claude/skills/ticket-rigor-gate/SKILL.md`
- **現值**（恰 1 次）: `If the evidence only covers helper code or grep-level existence, mark \`Evidence Gap\`.`
- **目標值**（保留原行不動，其後空一行接下列單一段落；再空一行接原有 `### Step 6: Check ticket atomicity`）:

```text
When the ticket claims that two variants (for example old and new logic, or two forms of the same input) are equivalent, compatible, or behave unchanged, and the claim is not covered by Migration-Lite Review, choose, within the entry-to-observer range the ticket already declares, one condition that would change a branch outcome, and confirm that both variants still yield the promised observable result, using the trace standard defined under Acceptance-Criteria Typology. The reviewer need not compare every variant one by one, trace undeclared paths, consumers, metadata, or internal implementation details; a comparison limited to the changed point is an `Evidence Gap`, and nothing beyond the ticket's declared range is traced.
```

- **語義對應**（plan T02 `acceptance_intent` 與 Akira 收窄文字）: 觸發與「一個足以改變分支結果的條件」＝收窄文字第一句；`need not compare every variant one by one, trace undeclared paths, consumers, metadata, or internal implementation details`＝收窄文字第二句；`not covered by Migration-Lite Review`＝「不屬 Migration-Lite 資料類才觸發」；`a comparison limited to the changed point is an \`Evidence Gap\``＝「僅比較變更點者歸既有 Evidence Gap」；`nothing beyond the ticket's declared range is traced`＝「超出票面宣告的 repo 範圍不追」；`using the trace standard defined under Acceptance-Criteria Typology`＝R3 重用既有 `[可達性]` 方法而不重述（`SKILL.md:223`）。整段不含 token、severity 等級語句、reason code、新章節（R7／R8／N2）。**確認對象**：`confirm` 的對象是票面對所選條件的證據（AC 或走查）是否達到 trace standard，審查者核對、不自行執行；理由：Preflight 時新變體尚不存在，審查者無法產出 trace standard 要求的執行結果，若讀成審查者自行執行，每條等價宣稱都無法確認、valid 案例亦被擋（自我歸零），且與 RGR-DERIV-01 段落「reviewer is not required to produce」相悖；本讀法與 Step 5 既有對象（the tests that prove that path）及 `Evidence Gap` 類別一致（見 W7／W8）。
- [x] 完成 （見收尾實錄）

### AI-2: 檢核、文字級走查與收尾實錄

- **檔案**: 不改檔（僅記錄於票檔收尾實錄）
- **動作**（本項所有 `git diff` 檢核之範圍＝`<DIFF_REF>..<DIFF_END>`；提交前為 `<DIFF_REF>` 對工作樹並以 `-- .claude/skills/ticket-rigor-gate/SKILL.md` 限定，FORBIDDEN 檢核以各檔路徑限定；同 worktree 他票之 dirty 檔，及範圍內他票提交〔commit 前綴非 `[RGR-EQUIV-01]`〕觸及之檔案，皆不計）: ①`git diff --stat` 僅 `SKILL.md`，diff 僅在 Step 5 末段新增該段；②新增行不含 `Medium`、`verdict`、`token`、`REASON_CODES` 字樣，且不新增任何 heading、表格、清單層級或檔案；③目標段落以 `grep -cF` 逐字命中 1 次，錨點 `If the evidence only covers helper code or grep-level existence, mark \`Evidence Gap\`.` 仍恰 1 次，`SKILL.md:223` 之 `[可達性]` 定義原文仍恰 1 次；④本票「走查」W1–W9 逐條對照條文並記 `檔案:行號`；⑤`/mdlint` 對 `SKILL.md` warning 數不高於 AI-0 baseline；⑥收尾實錄如實載明「模型遵循度未驗」與已知限制，不宣稱 Traycer 通過率、token 效率或品質提升，並載明「隔離成對評測未授權、未執行」；⑦提交依既有流程，**提交若不經 `/qa` Step 4.5，提交前先執行 `python .claude/scripts/gate_ledger.py check commit --ticket RGR-EQUIV-01`，exit 0 才提交**（退出碼處置依 `ssot-governance.md` §入口與覆蓋矩陣之映射）。
- [x] 完成 （見收尾實錄）

## Hard Constraints

1. 僅得在 `SKILL.md` Step 5 末段新增上列單一段落；不得改動 Step 5 既有四項清單與末句、相鄰 Step、Acceptance-Criteria Typology（含 `SKILL.md:223`）、Migration-Lite Review、Severity 表、Mechanical Verdict Rule 或十欄 disposition，亦不得新增 heading、表格、清單層級或常駐 checklist。
2. 不得新增 token、severity 列或等級語句（含「一律 Medium」「不影響 verdict」）、reason code、Gate、lifecycle 或 repair authority；新段落僅得引用既有 `Evidence Gap`、Migration-Lite Review 與 Acceptance-Criteria Typology。
3. 不得要求審查者逐變體逐路徑追蹤，不得追蹤未點名路徑、consumer、metadata 或內部實作細節，不得做 repo-wide tracing，不得新增軌跡定義。
4. 不得修改 `REPAIR_ROUTING.md`、`OPS_CALIBRATION.md`；A（RGR-DERIV-01）、C（RGR-RERUN-01）、D（RGR-REPLAY-01）不得在本票併入，B 亦不得折疊為 A 的示例。
5. 不得修改、重開或改寫 `TKA-BATCH-01`、`TKA-EVGATE-01`，亦不得建立常駐 benchmark、案例庫、ledger 或第二 Gate。
6. 不得宣稱已提升 Traycer 通過率、token 效率或品質指標；本票只交付條文，不交付行為驗證。

## Error Handling

- AI-0 任一錨點不符 → 停止回報，禁自行推定等價位置
- `git check-ignore -v` 命中 `SKILL.md`（標的被 `.gitignore` 排除）→ 停止回報（gitignore 死結律）
- 他票 ALLOWED 與 `.claude/skills/ticket-rigor-gate/SKILL.md` 相交 → 停止回報（須串行）；同 plan 的 RGR-DERIV-01、RGR-RERUN-01 僅於其 lifecycle 為 `IN_PROGRESS` 時適用本條
- 無法僅憑 `SKILL.md` 單段承載 plan T02 意圖（例如須改 `[可達性]` 定義或新增章節才能表達）→ 停止，請求裁決，不得擴大修改範圍
- 條文無法在不新增 token、severity 列、reason code、Gate、lifecycle、repair authority 下表達 → 停止回報
- 新條文與 Step 10（reverse scan）、Migration-Lite Review 或 `[可達性]` 定義出現矛盾 → 停止回報
- 修改後 `git diff --stat` 觸及 ALLOWED 以外檔案 → 停止回報

## 走查（文字級；`[存在性]` 證據，非 `[行為性]` 驗證；given/expect）

下列為對條文的文字級對照，不是對模型行為的實測。情境為說明性假設，非真實事件；W1／W2 對應 plan §8 的 E2（B 部分）。

| # | given | expect（依 AI-1 條文） |
| --- | --- | --- |
| W1（E2-B flawed） | 票面宣稱「新的查找母體等同重複檢查母體」，只比對了母體清單的文字，沒有選任何會改變分支結果的條件，也沒有比對可觀察結果 | 僅比較變更點 → 歸既有 `Evidence Gap`；嚴重度依既有規則，本段不指定 |
| W2（E2-B valid） | 同 W1，但票面選了「票只存在於 `deliver/archive/`」這個會改變分支結果的條件，並確認兩個查詢仍導出所承諾的結果 | 審查者核對票面所選條件確有判別力，且票面證據確認兩變體結果 → 退出，新段落不產生 finding |
| W3 | 宣稱屬欄位別名正規化，Migration-Lite Review 為 `ACTIVE` | 屬 Migration-Lite Review 涵蓋，B 不觸發；以其五 facet 審查，不重複 |
| W4 | 票面沒有任何兩變體等價／相容／不變的宣稱 | 不觸發 |
| W5 | 審查者懷疑票面未點名的 consumer 在兩變體下行為不同 | 超出票面宣告範圍 → 不追蹤；若認為遺漏屬根因掃描，依既有 Step 10 另行處理，不在本段 |
| W6 | 票面只描述單一邏輯，沒有第二個變體 | 不觸發 |
| W7 | Preflight，新變體尚未實作；票面以 AC 或走查宣告所選條件下兩變體的可觀察結果 | 審查者核對該證據是否達 trace standard（含觀察者），不自行執行或產出 trace；票面未提供此類證據 → `Evidence Gap` |
| W8 | 依票面證據，所選條件下兩變體導出不同結果 | 等價宣稱未獲支持 → `Evidence Gap`；分歧結果若無核定決策可定，既有 `### Decision-Fork Findings` 獨立適用，新段落不排除也不取代它 |
| W9 | 等價宣稱同為承重推導，票面既無 RGR-DERIV-01 段落要求之紀錄，也無分支證據 | 兩段各記一筆（`Missing Contract Expansion`＋`Evidence Gap`），指向不同缺口，刻意並存、不去重；嚴重度依既有規則 |

## Acceptance Criteria

> **diff 檢核範圍**：本節所有 `git diff` 檢核之範圍＝`<DIFF_REF>..<DIFF_END>`；提交前為 `<DIFF_REF>` 對工作樹並以 `-- .claude/skills/ticket-rigor-gate/SKILL.md` 限定，FORBIDDEN 檢核以各檔路徑限定；同 worktree 他票 dirty 檔，及範圍內他票提交（commit 前綴非 `[RGR-EQUIV-01]`，以 commit 前綴歸屬）觸及之檔案，皆不計入本票。下列片語中「反斜線＋反引號」代表反引號字元本身，`grep` 時不含反斜線。
>
> **AC 修訂閉環（Rev A.1）**：本節首範圍說明、第 4 條（W1–W6→W1–W9）與最後一條（排除他票提交）經修訂；共用同一 diff 範圍母體之兄弟 AC（第 3、6 條）已重驗：同樣只因他票提交而受影響，修訂後排除規則一致套用，判準不變；第 1、2、5 條不涉 diff 範圍，不受影響。無 AC 被移除或豁免。

- [x] **[存在性]** `SKILL.md` Step 5 末段含一個以 `When the ticket claims that two variants` 開頭的段落，且同段內含 `one condition that would change a branch outcome`、`both variants still yield the promised observable result`、`the claim is not covered by Migration-Lite Review`（`grep -cF` 各為 1）
- [x] **[存在性]** 同段含 `using the trace standard defined under Acceptance-Criteria Typology`、`The reviewer need not compare every variant one by one`、`a comparison limited to the changed point is an \`Evidence Gap\`` 與 `nothing beyond the ticket's declared range is traced`（`grep -cF` 各為 1）
- [x] **[存在性]** `git diff` 新增行不含 `Medium`、`verdict`、`token`、`REASON_CODES`，且無新增 heading、表格、清單層級、reason code、Gate 或 lifecycle 字樣；原錨點 `If the evidence only covers helper code or grep-level existence, mark \`Evidence Gap\`.` 仍恰 1 次
- [x] **[存在性]** 收尾實錄含「走查」W1–W9 逐條對照（附 `檔案:行號`）、「模型遵循度未驗」限制，以及「隔離成對評測未授權、未執行」，且未宣稱 `[行為性]` 驗證或任何品質指標提升
- [x] **[存在性]** `/mdlint` 對 `SKILL.md` 的 warning 數不高於 AI-0 所記 baseline
- [x] **[存在性]** `FORBIDDEN` 所列各檔 `git diff` 為空
- [x] ~~`assembleDebug` PASS~~ (N/A: docs preset)
- [x] `git diff --stat <DIFF_REF>..<DIFF_END>` 僅涉及 ALLOWED 檔案（範圍見本節首；他票 dirty 檔與他票提交觸及之檔案不計）

## 已知限制（非阻擋，如實揭露）

1. **模型遵循度未驗；分階段驗收**：全部 AC 為 `[存在性]`。Template 的「Guard／gate／early-return → 至少含 `[可達性]`＋`[行為性]`」規則（Template v3.11 L163）之 `[可達性]` 定義以 `app/src/main/` production producer 為語境（L159）；本票的 gate 為 skill 指令文字，無 production producer，故採分階段驗收：①票內驗條文、引用、靜態一致性與 W1–W9 文字走查；②真實行為依後續真實 RGR 審查是否於等價宣稱處選條件並確認結果，由 REVIEW→CLOSED 審查方確認。未觸發或證據不足時仍為未驗，不回補更高等級驗證、不宣稱已驗。
2. 動機證據為少數案例（TKA-BATCH-01 RGR 報告 F4「母體等同宣稱」），「現行文字不要求」不等於「審查從不抓到」；缺口判斷屬現行文字比對，不構成對本條文有效性的證明。
3. **隔離成對評測未授權、未執行**（Akira 2026-10-21 裁決）；plan §8 的 E2（B 部分）預期僅作預先寫定的判準，未實測。誤擋率、審查成本、token 與時間資料皆無（UNKNOWN）。
4. 「選擇一個足以改變分支結果的條件」仰賴審查者判斷；審查者可能選到實際上不改變分支結果的條件而形式上通過。條文不要求報告載明所選條件（收窄文字未列），此為已知的敷衍風險，觀察物為該次 RGR 報告與 Cross-Review／T2 審查，本票不增設機械檢查。
5. 執行時 `RGR-DERIV-01`、`RGR-RERUN-01` 為同檔票，須序列執行；`SKILL.md` 為共用 skill 檔，AI-0 ⑦ 於派工當刻重驗。
6. **既有缺口（不修）**：Severity 表沒有 Medium 判準列，`Evidence Gap` 亦只有部分情形對應 Major 列（`SKILL.md:156-162`）；本票條文依 R8／N2 對嚴重度保持沉默，因此「僅比較變更點」finding 的等級仍由既有規則與先例決定，審查者之間可能不一致。補正屬後續議題。
7. **與 RGR-DERIV-01 重疊觸發**：同一條等價宣稱若同為承重推導，可能同時產生 `Missing Contract Expansion`（RGR-DERIV-01 段落）與 `Evidence Gap`（本段）兩筆 finding；兩者刻意並存（承重推導反證紀錄第 3 點）。若 `Evidence Gap` 落在既有 Major 列，一個宣稱可能使 verdict 偏向 PARTIAL。承接者：首次真實觸發兩段之 RGR 審查報告，以及 REVIEW→CLOSED 審查方。

## 收尾實錄（執行端；2026-10-21；Mode: normal）

### 1. 基線與實作

- 開工 HEAD `c74bfcd5`（`DIFF_REF` 回填）；`SKILL.md` blob 由開票時 `24d2d444` 變為 `d099b657`，漂移歸因：`ebd96a78` [RGR-DERIV-01]（同 plan 兄弟票，已先落地於 Step 3），屬資訊性分類，依 AI-0 ⑨ 以現值續作；本票 Step 5 錨點與其他錨點仍各恰 1 次。RGR-RERUN-01 當時為 `TICKET_RIGOR`（非 `IN_PROGRESS`），依 AI-0 ⑦ 屬資訊性。
- AI-0 ①–⑩ live 驗證：①②③④ 各恰 1，⑤ 修改前 0，⑥ check-ignore exit 1。⑧ 見 §2。
- AI-1 已落地並提交 `2303d1b9`：Step 5 末句之後新增 1 空行＋1 段落，目標值逐字；`git diff` ＝ 1 file、+2／−0；`DIFF_END=2303d1b9`。

### 2. mdlint

未跑 `/mdlint` 掃描（會自動修復）。替代論證：僅新增一個以空行分隔的段落，未增刪 heading、fence、表格或清單層級；此為論證而非掃描結果。

### 3. 檢核與走查

- 目標段落八片語各 1 次；新增行不含 `Medium`、`verdict`、`token`、`REASON_CODES`；Step 5 錨點與 `[可達性]` 定義行仍各 1 次；FORBIDDEN 各檔 diff 為空。
- 走查 W1–W9 對照 `SKILL.md` Step 5 新增段（文字級，非 `[行為性]`）：僅比較變更點→Evidence Gap；選了足以改變分支的條件且證據達 trace standard→退出；Migration-Lite 涵蓋之宣稱不觸發；無等價宣稱不觸發；未宣告範圍不追；單一邏輯不觸發；證據未達 trace standard→Evidence Gap；確認對象為票面證據、審查者不自行執行；與 RGR-DERIV-01 同一宣稱並存（刻意）。

### 4. 限制與如實揭露

模型遵循度未驗；隔離成對評測未授權、未執行；未宣稱 Traycer 通過率、token 效率或品質提升。風險分級（純 `.md`、可 revert）省略 `/qa`、`/sv`、Cross-Review，`reviews` 四欄 `N_A`。提交前 `check commit` exit 0。
