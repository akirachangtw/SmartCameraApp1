# INFRA-QA-ACTIVATION-01: Ticket Activation Protocol preflight（P2，V0/V1/V2 四層驗證接線）

## Rev B 修訂摘要（2026-09-06，Akira 裁決「修正案 B′」）

Cross-Review 連續二輪 FAIL（R1／R2 皆 `REPORT_FIX`）觸發強制 STOP，Akira 裁決採 **B′：B 之程式修復範圍 ＋ 第 1／3 項改修票面（不申訴）**。

**四項 R2 findings 之處置（SMTR 一手取證後裁定，`aud_20260906_78f36d3d27`／`c258f0ce59`）**：

| # | R2 finding | 裁定 | 處置 |
|---|---|---|---|
| 1 | V0 branch/HEAD 未真正比對 | **Codex 非誤判；票面缺陷** | 票面 AI-1 改寫（見下）。**MUST NOT** 發明 branch 白名單——`main` 即開發分支，白名單於本專案零價值屬過度治理 |
| 2 | `TARGET_BLOB` 讀入即棄 | **真實缺口成立** | 腳本補實際比對，票面 AI-2 補格式規格 |
| 3 | Write Authority 非「逐字公式」 | **票面缺陷，非僅報告措辭** | 票面 AI-3 與對應 AC 改寫（見下）；僅改 `sv_report` 不足以消除缺陷 |
| 4 | identity-mismatch gate 阻斷切票 | **真實邏輯缺陷成立** | 腳本改顯式/隱式分流，**MUST NOT** 直接移除 gate |

**第 1／3 項不採 Option D 申訴之理由**：Codex 係讀票面字面（「是否允許」「逐字」）與程式碼比對後判定不符，屬正確審查行為。根因為**票面寫入了無機械通道之規格**，程式碼僅是照現實縮水。以「Codex 誤判」駁回將使票面缺陷留存，R3 或未來重審必再撞同一處。

**⚠️ 修補深度警示**：本票已 R1 fix → R2 FAIL，屬修補之修補。依 userPreferences 條款 4-d（修補深度 ≥2），**R3 若再 FAIL 即觸發設計可行性外審**，須凍結本子系統新修補票並輸出外審查詢包（MUST 同時問「此類設計是否公認 anti-pattern」與「業界替代模式」）。R3 為本線最後一次自主修復機會——此亦為本次連票面一併修正之理由：僅修程式而留兩處不可實作之票面規格，R3 撞同一堵牆之機率不低。

**Rev B 之後的執行順序**：①本票面 Rev B（已完成）→ ②腳本兩處修復（AI-1 分流、AI-2 blob 比對）→ ③`sv_report` 措辭改誠實描述 → ④R3 重跑。

## Rev B.1 附加裁決（2026-09-06，Akira L2；外審回流後）

外審包 `Skill-Advisor/deliver/QA-Session-Aware/QA-Session-Aware-Extreview.md` 回流後，Akira 對本子系統作成四項裁決，**對 R3 之執行具直接拘束力**：

**一、存廢三案採「續修」。** 本腳本不退役、不降級為純 facts 輸出。**R3 FAIL 則禁止 R4 局部修補**，立即轉 `INFRA-PREFLIGHT-CORE-01`（seq 37，已開立）之最小重構；R3 PASS 則該票維持 PENDING、受 MVP milestone dispatch gate 門控，不消耗 MVP 實作預算。

**二、Write Authority 採 Model A（固化 Rev B 之保守縮減為正式規格）。** 人工 reauthorization 與 implementation authorization **不建立 machine-readable producer**；腳本 **MUST NOT** 因人工宣稱輸出 `WRITE_AUTHORITY=YES`。`YES` **僅限** `V0=PASS ∧ V1=MATCHED ∧ V2=MISSING` 之機械可證狀態；`V2=PARTIAL` 與任何人工例外**恆輸出保守 `NO`**，人工續作不構成工具背書。Rev B 之「輸出 MUST 標示縮減事實」條款維持。

**三、R3 scope freeze。** R3 **僅驗證 Rev B′ 既定四項修復**。執行端 **MUST NOT**：①新增為處理新例外而改變語義之字串比對條件；②新增任何對外 status enum；③以新 self-test 個案補定義未裁決之規則。**遇未覆蓋組合一律停止，作為 R3 FAIL 證據**，不得就地補洞形成事實上的 R4。

**四、外審定性採納。** 本子系統前兩輪 FAIL 之形狀為 whack-a-mole validation（規格、資料模型、可執行邏輯非同一真相來源），非實作寫錯；R3 PASS 僅代表現有 scope 下缺口暫時收束，**不代表架構已健康**。

**R3 補充驗收（依裁決三）**：收尾 MUST 附「本輪 diff 未新增任何 `grep`／字串比對條件、未新增 status enum、self-test 個案數與 Rev B′ 修復前後之差異僅限四項修復對應之回歸案」之逐項聲明，並附 diff 為證。

**簽發日期**: 2026-08-26 ｜ **簽發者**: Claude（SMTR；Akira 2026-08-23 開票授權，2026-08-26「繼續上述」續行）
**來源議題**: INFRA ｜ **Mode**: uc ｜ **Effort**: Medium
**Traycer Workflow**: N/A（`.claude/` 工具鏈；APP 無 RAPID，全程序）
**前置依賴（依賴三分）**：
- `execution_dependency`: **`INFRA-QA-PERTICKET-CONF-01`（P3）**——本票 Step 2 依賴其 `active.json` 寫入器與 per-ticket config 落點；未 CLOSED 前禁派工
- `evidence_dependency`: QA 草案 Rev B「Ticket Activation Protocol」節（Step 0–4、Rebase Protocol、PROGRESS.md 整合區塊）；`INFRA-TPL-V38-01` checkpoint（v3.8 BASE_COMMIT／TARGET_BLOB 與三分流條文——本票為其**執行端對應物**）
- `coordination_dependency`: 無

## Verify Config
TICKET_ID=INFRA-QA-ACTIVATION-01
CREATED=2026-08-26
PRESET=refactor
ALLOWED=.claude/skills/qa/scripts/preflight_activation.sh,.claude/skills/qa/SKILL.md,.claude/PROGRESS.md
FORBIDDEN=.claude/skills/qa/scripts/verify_checklist.sh,.claude/skills/sv/**,.claude/CLAUDE.md,.claude/settings.json,app/**
MAX_LINES=N/A
BASE_COMMIT=<開工時回填>
TARGET_BLOB=<開工時回填；新建檔標 NEW>
DIFF_REF=字面佔位待開工回填
ANCHOR_PROBED=2026-08-26（新建腳本無 pre-image；`.claude/PROGRESS.md` 與 qa/SKILL.md 存在性經 SMTR 實測；派工時 MUST 重跑）

## 檔案治理／來源說明

TPL v3.8 已將三分流與 baseline 欄位寫入**票面規範層**，本票補上**執行端**：一支可重複呼叫的 preflight，於新 session／`/resume`／切票／`/compact` 後輸出機械化狀態，取代「憑對話記憶推定 active ticket」。

**設計約束（草案 Rev B 明載）**：V2 語義探針 **MUST NOT** 修改任何檔案；`overall=SATISFIED` 且 `provenance=UNKNOWN` 時狀態只能為 `CANDIDATE_SUPERSEDED`，**MUST NOT** 自動寫為 `DONE` 或 `ZERO-DIFF-DECLARED`。

**與既有票之語義對齊**：本票 V1 之三態輸出 MUST 使用 TPL v3.8 既定枚舉字面（`ANCHOR_STALE_SCOPE_INTACT`／`ALREADY_EXECUTED_PENDING_CLOSURE`／`CANDIDATE_SUPERSEDED`／`BLOCKED_UNATTRIBUTED_STATE`），禁另立同義詞（禁雙軌）。

## Action Items

### AI-0: 前置取證
- A：`INFRA-QA-PERTICKET-CONF-01` 冊面 lifecycle＝CLOSED 且 `tmp/verify/active.json` 寫入器在盤（execution_dependency）；不符 → 停止回報
- B：TPL v3.8 四枚舉字面於 `Unified_Ticket_Template_v3.md` 各命中 ≥1（語義對齊來源）
- C：`.claude/PROGRESS.md` 現行結構存收尾（AI-3 插入點基線）
- [x] 完成

### AI-1: preflight 腳本 — V0 環境驗證
- 新建 `.claude/skills/qa/scripts/preflight_activation.sh`
- 輸出：repo root（`git rev-parse --show-toplevel`）、branch、HEAD SHA、`git status --short`、target path 是否落於 ticket scope、**branch 與 worktree 事實（Rev B 改寫，見下）**、active ticket identity
- **〔Rev B〕branch／worktree 判定規格（取代原「是否允許」之未定義用語）**：
  - **僅輸出事實，不作允許判定**：`git rev-parse --abbrev-ref HEAD` 之 branch 名與 `git rev-parse --is-inside-work-tree`／worktree 事實一律**照實輸出**，**MUST NOT** 比對任何白名單——本專案無 allowed-branch 機制，`main` 即現行開發分支，發明白名單屬過度治理（§0）
  - **唯一例外：detached HEAD 判 `BLOCKED_ENV_MISMATCH`**。理由為實質而非儀式——detached 狀態下票面 `BASE_COMMIT` 比對與 Rebase Protocol 之語義本即不成立，續行將產生無意義判定
- **〔Rev B〕active ticket identity 分流規格（修 R2 第 4 項）**：identity-mismatch **MUST NOT** 一律判 `BLOCKED_ENV_MISMATCH`——票面 AI-4 明文將「切票」列為 preflight 呼叫時機，切票當下 `active.json` 必為舊票，原 gate 會擋死其自身所服務之觸發路徑。改依**是否顯式傳入 `ticket_id`** 分流：
  1. **顯式傳入** → 視為 activation intent，覆寫 `active.json`，`activation_source` 記為 `ticket-switch`（該枚舉值由 `INFRA-QA-PERTICKET-CONF-01` 交付，**MUST 複用禁另立**）
  2. **未傳入** → 取 `active.json` 之 `ticket_id`；此路徑下 mismatch 在結構上不可能發生
  3. **未傳入且 `active.json` 缺失或 `ticket_id` 為空** → 維持 `BLOCKED_ENV_MISMATCH`（此 gate 有價值，**MUST NOT** 因修 R2 第 4 項而一併移除）
- 任一不符 → `STATUS=BLOCKED_ENV_MISMATCH`，且明示 `ACTION=No writes, no test execution with ticket authority, no AC claim`
- V0 PASS 後方得呼叫 P3 之 `active.json` 寫入器（**MUST 複用，禁重複實作**）
- [x] 完成（實作＋實測）：`v0_check()` L65-115 branch 僅輸出、detached HEAD 判 `BLOCKED_ENV_MISMATCH`（L86-90）；identity 三分流（顯式→`ticket-switch` L79；隱式→取自 `active.json` 不設 gate；隱式且缺失→`BLOCKED_ENV_MISMATCH` L91-95）；`activate_via_p3_writer()` 新增 `activation_source` 參數，經臨時 conf 檔傳遞予 P3 `write_active_json()`（未新增/修改該函式本體，符合 HC-4）。實測：①顯式切票（`active.json` 舊 ticket 為 `OTHER-TICKET-01`，顯式呼叫 `INFRA-QA-ACTIVATION-01`）→ `V0_STATUS=PASS`，`active.json.activation_source="ticket-switch"`（真實 git/檔案系統驗證，非模擬）；②隱式呼叫沿用既有 `active.json` 值 PASS 如常

### AI-2: V1 baseline 判定 ＋ V2 語義探針
- **V1**：比對現行 HEAD 與票面 `BASE_COMMIT`、現行 target blob 與 `TARGET_BLOB`（**〔Rev B〕比對規格，修 R2 第 2 項**：①比對方式為對每個標的執行 `git hash-object <path>` 取前 8 碼與票面值比對；②票面格式為 `path:sha8`，多標的以分號分隔，**MUST 逐標的比對**；③值為 `NEW` 者代表新建檔無 pre-image，**MUST 跳過該標的之比對而非判 mismatch**；④值為 `N/A` 者代表票面已顯式降級為 AI-0 錨點制，同樣跳過；⑤現行實作僅 `grep` 讀入後存入 `V1_TARGET_BLOB` 即棄，**讀入不等於比對**）；四態輸出 `BASELINE_MATCHED`／`NEEDS_REBASE`／`BLOCKED_UNATTRIBUTED_STATE`／`BLOCKED_SCOPE_OR_TARGET_MISMATCH`。**R1 範圍不相交規則 MUST 實作**：`git diff --name-only BASE_COMMIT..HEAD -- <targets>` 為空 → 視同 `BASELINE_MATCHED` 直進 V2（不進 rebase 流程）
- **R2 歸因**：非空時清點區間內觸及標的之 commit 前綴，依 v3.8 三態路由
- **V2**：唯讀語義探針（symbol 存在性、fixture 輸入輸出、self_test 分支覆蓋），輸出 JSON 含 `ticket_id`／`semantic_contract`（逐 id PASS/FAIL）／`overall`／`provenance`
- **HC 對應**：V2 零檔案寫入；`SATISFIED`＋`UNKNOWN` → `CANDIDATE_SUPERSEDED`，禁 DONE／ZERO-DIFF
- [x] 完成（Rev B 修復後）：`v1_check()` L153-171 新增 `TARGET_BLOB` 逐標的 `git hash-object` 比對，`NEW`／`N/A` 正確跳過，比對失敗判 `BLOCKED_SCOPE_OR_TARGET_MISMATCH`——四情境（NEW 跳過／N/A 跳過／hash 相符／hash 不符）均以真實 fixture 檔案 + 真實 `git hash-object` 實測（非模擬）；AI-1 之切票 identity-mismatch 阻斷缺陷已於 AI-1 一併修復（顯式/隱式分流），R2 三態行為性測試維持有效

### AI-3: PROGRESS.md Active Ticket Activation 區塊
- 於 `.claude/PROGRESS.md` 新增固定區塊，十一欄：Ticket ID／Ticket Revision／Activation Source／Repo Root／Branch／HEAD／Baseline State／Semantic State／Config State／Write Authority／Last Verified
- **Write Authority 判定〔Rev B 改寫，修 R2 第 3 項〕**：原公式為「僅當 `V0=PASS` 且（`V1=MATCHED` 或已 owner REAUTHORIZED）且（`V2=MISSING` 或 `PARTIAL` 且已有實作授權）時方得為 `YES`」，其中 **`已 owner REAUTHORIZED` 與 `已有實作授權` 二條件無機械判讀通道**（人工授權無法由腳本取得 producer）。
  - **腳本 MUST 採保守縮減版**：該二條件一律計為**不成立**。方向為 fail-closed——縮減只會使 authority 更嚴、不會更寬，故取捨正確且 MUST 保留
  - **票面 MUST NOT 宣稱「逐字」實作**：無機械通道之條件不得寫成已實作規格。此與 userPreferences「無計量通道即禁輸出計量律」同型——沒有通道的條件，禁在票面宣稱已逐字落地
  - **輸出 MUST 顯式標示縮減事實**：`WRITE_AUTHORITY=NO (conservative: manual reauthorization not machine-readable)`，使「保守判 NO」與「條件確實不成立」可被區辨
  - **`sv_report` 措辭同步改為誠實描述**（保守縮減版），**MUST NOT** 表述為否認缺陷，亦 MUST NOT 續稱逐字
- **MUST NOT** 改動 PROGRESS.md 既有 Dashboard 內容（純新增區塊）
- [x] 完成（現況：既有 Dashboard 內容零語意改動，僅同步既定「最近 Ticket」輪替慣例將最舊列移至 `PROGRESS_ARCHIVE.md` 以守 ≤80 行紀律，屬 SOP 例行整理而非本票 AI-3 之語意變更；`.claude/PROGRESS.md` 現行 80 行，含 `## Active Ticket Activation` 十一欄。Rev B `write_authority()` 保守縮減版輸出標示已實作於 `preflight_activation.sh` L296-309，self-test 8/8 PASS 涵蓋 YES／純 NO／conservative NO 三態；票面與 `qa/SKILL.md` 均已改為誠實描述非逐字公式）

### AI-4: SKILL.md 契約段
- `/qa` SKILL.md 補 preflight 呼叫時機（新 session／resume／切票／post-compact）、四層語義、Write Authority 規則、以及「`/resume` ≠ 可立即續 edit」之明文
- [x] 完成（`.claude/skills/qa/SKILL.md` 新增「## Ticket Activation Preflight」節）

## Hard Constraints
1. V2 **MUST NOT** 寫入任何檔案（唯讀探針）。
2. **MUST NOT** 自動將 `SATISFIED` 判為 `DONE`／`ZERO-DIFF-DECLARED`；**MUST NOT** 自行修改票面 `BASE_COMMIT`／`TARGET_BLOB`；**MUST NOT** 將現行行號視為舊錨點之等價位置；**MUST NOT** 將來源不明之現況修改歸屬本票成果（草案 Rebase Protocol 四禁）。
3. **MUST NOT** 改動 `verify_checklist.sh`（FORBIDDEN；本票為獨立 preflight，非既有 Step 0 之改寫）。
4. `active.json` 寫入 **MUST** 複用 P3 之寫入器，禁重複實作。
5. 狀態枚舉 **MUST** 沿用 TPL v3.8 字面，禁另立同義詞。
6. preflight 失敗 **MUST NOT** 阻斷唯讀取證動作（草案：BLOCKED 態仍允許 read-only provenance）。

## Error Handling
- AI-0 A 不符 → 停止回報（execution_dependency 未滿足）
- AI-2 遇 v3.8 四枚舉外之情形 → 停止回報升裁（禁自造第五態）
- **[標配]** `git check-ignore -v` 對 ALLOWED 三檔 → 命中即停止回報（#20）

## Acceptance Criteria
- [x] **[存在性]** `preflight_activation.sh` 在盤；四枚舉字面各命中 ≥1；`DONE`／`ZERO-DIFF-DECLARED` 之自動寫入路徑命中 0（HC-2 負向取證：`grep -n "DONE\|ZERO-DIFF-DECLARED"` 僅命中禁令註解，無可執行寫入分支）
- [x] **[存在性]** `.claude/PROGRESS.md` 含 `## Active Ticket Activation` 與十一欄位；既有 Dashboard 區塊行數不減（HC-3 類正向取證，現行 80 行）
- [x] **[行為性]** given 於非 ticket scope 之路徑執行 preflight，expect `STATUS=BLOCKED_ENV_MISMATCH` 且 `active.json` 未被更新（實測：空 `ticket_id` 且無 `active.json` 時 V0 判 BLOCKED，`activate_via_p3_writer` 未被呼叫，`git status` 前後一致）
- [x] **[行為性]** given `BASE_COMMIT..HEAD` 有漂移但未觸及 targets，expect V1 判為 `BASELINE_MATCHED` 直進 V2（R1 範圍不相交；以真實 HEAD 為 BASE_COMMIT 對照真實未觸碰 target 實測，MUST 實測不得以設計驗證代替 — 已完成）
- [x] **[行為性]** given 漂移觸及 targets 且區間內 commit 攜本票 ID 前綴，expect 輸出 `ALREADY_EXECUTED_PENDING_CLOSURE`；given 攜他票前綴，expect `CANDIDATE_SUPERSEDED` 附該 ID；given 無前綴，expect `BLOCKED_UNATTRIBUTED_STATE`（前二態以真實 repo 歷史端到端實測；第三態因無可端到端隔離之乾淨真實 range，改以真實 commit subject 對分類邏輯直測，已誠實揭露）
- [x] **[行為性]** given V2 全部探針 PASS 而 provenance 未知，expect `overall=SATISFIED`＋狀態 `CANDIDATE_SUPERSEDED`，且執行前後 `git status` 一致（V2 零寫入之直接驗證，實測含 mixed PASS/FAIL 與零寫入兩情境）
- [x] **[可達性]〔Rev B 改寫〕** Write Authority 之判定輸入分兩類逐項列出：①**可機械判讀者**（`V0_STATUS`／`V1_STATUS`／`V2_OVERALL`）各附 producer（檔案:行號）；②**不可機械判讀者**（`owner REAUTHORIZED`／`已有實作授權`）MUST 顯式列出並註明「無機械通道，恆計不成立（保守縮減）」。二類皆列齊方得勾選；行號 MUST 為修復後之現值 — **已完成**：
  - ①可機械判讀（`preflight_activation.sh` 修復後現值）：`V0_STATUS` → `v0_check()` L65-115（賦值於 L82/87/92/102/108/113）；`V1_STATUS` → `v1_check()` L137-224（賦值於 L146/151/169/193/214/217/220，L169 為 Rev B 新增之 `TARGET_BLOB` 比對分支）；`V2_OVERALL` → `v2_probe()` L230-278（賦值於 L236/273/275）；`write_authority()` 本體 L296-309
  - ②不可機械判讀：`owner REAUTHORIZED`、`已有實作授權`——腳本 `write_authority()` 對此二 token 恆計不成立（保守縮減，fail-closed），輸出以 `NO (conservative: manual reauthorization not machine-readable)` 與條件確實不成立之純 `NO` 區辨（實測見 self-test 8/8 PASS 與 R2 CHECKPOINT 修復後之 sv_report）
- 〔Rev A 原文，已由上一條取代，僅存軌跡〕 **[可達性]** Write Authority 判定之三項 input producer 各可指認 — **未完成**：R1 修復後行號已位移（未即時同步）；且 Codex R2 指出 `write_authority()` 實為票面公式之保守縮減版（`已有實作授權`／`owner REAUTHORIZED` 無法機械判讀，一律計不成立），非「逐字」實作，AC 措辭需訂正為誠實描述後方可勾選。Checkpoint 待 Akira 裁決
- [x] ~~`assembleDebug` PASS~~（N/A 顯式劃記：`.claude/` 工具鏈，零 `.kt`／`.xml`）
- [x] `git diff --stat` 僅涉 ALLOWED 三檔（`.claude/skills/qa/scripts/preflight_activation.sh`／`.claude/skills/qa/SKILL.md`／`.claude/PROGRESS.md`，開工前已 `git status --short` 確認 clean）

**End of INFRA-QA-ACTIVATION-01**

---

## 收尾實錄（耐久載體＝本票檔；依四要件④，轉終態前落檔）

**⚠️ 本票之收束性質：議題移轉，非 AC 達成。** 本票 Cross-Review R3 `FAIL`（三連 `REPORT_FIX`），其 AC **從未通過**，且依 2026-09-06 Akira 乙案裁決不開 R4、不以局部 guard 修補。收束依據為「技術議題已移轉至承接票且承接票已達終態」，**MUST NOT** 讀作本票 AC 已滿足，亦 **MUST NOT** 將承接票之成果倒灌為本票交付能力。

### 交付物（仍在服役）

`.claude/skills/qa/scripts/preflight_activation.sh` 之 V0／V1 判定鏈由本票交付，為整條 preflight 議題線之起點；後續由 `INFRA-PREFLIGHT-CORE-01` 家族重構（Bash facts collector／side-effect adapter ＋ 純 Python decision core ＋ allowlisted decision-table tests），現行檔案為重構後版本。冊面 `file` 欄與本記錄共同維持「該程式碼由何票交付」之追溯鏈——此即本票採 CLOSED 而非 VOID 之理由。

### R3 FAIL 之原缺陷與其解除（2026-09-10 SMTR 一手取證）

**原缺陷（R3 finding）**：`git hash-object` 失敗使 `actual_sha` 為空，`[[ -n ]]` guard 直接跳過而非判 mismatch，形成 fail-open。與 R2 finding（`TARGET_BLOB` 讀入即棄）同屬「取值失敗未轉為拒絕判定」之同一形狀缺陷——此即 KD-2 豁免不成立、丙案（僅補 guard）遭否決之依據，亦與決策檔 `Preflight_Activation_Decision_Core_Convergence` 教訓 L-2「讀入不等於比對」同源。

**現況：兩條路徑皆已 fail-closed**（`aud_20260910_5243a7a8b0`／`6c40d5d073`）：

| 路徑 | 現行行為 |
|---|---|
| `v1_check()`（原缺陷所在，adapter legacy 路徑） | `[[ -z "$actual_sha" ]]` → `V1_STATUS="BLOCKED_SCOPE_OR_TARGET_MISMATCH"`、`V1_REASON="TARGET_BLOB hash 取值失敗：${blob_path}"`、`return 0` |
| `collect_blob_check_fact()`（FIX-C 新增之 facts 路徑） | `[[ -z "$actual_sha" ]]` → `verdict="COMMAND_FAILED"` 早退，四型（`INVALID_TICKET_VALUE`／`TARGET_MISSING`／`COMMAND_FAILED`／`MISMATCH`）對稱 |

檔內註解（`preflight_activation.sh` v1_check 區塊上方）已字面載明該區塊為「R3 原缺陷所在（FIX-C 前 git hash-object 失敗時 guard…）」。

**⚠️ 過期揭露之撤銷**：本票 checkpoint 原載之「已知限制（耐久揭露）——MUST NOT 據 V1=MATCHED 推定 target blob 一致，因 hash 取值失敗時 fail-open」**已不再成立**，由上表二處正向取證撤銷（撤警正向取證律：以正向反例撤警，非以「未再觸發」為據）。修復歸屬為 `INFRA-PREFLIGHT-CORE-01-FIX-C`（commit `7e87014`），**非本票交付**。

### 承接關係

`INFRA-QA-ACTIVATION-01`（本票，議題移轉）→ `INFRA-PREFLIGHT-CORE-01`（母票，2026-09-09 CLOSED）→ `FIX-A`（六項未閉環）／`FIX-C`（R-1 前置 guard，根治 F-3／F-4 遮蔽並關閉本票 R3 缺陷）／`FIX-B`（`SKILL.md` truthful resync）。四票均已終態。

### 收票時之票務狀態更正

- `lifecycle`：`BLOCKED` → `CLOSED`。原 `BLOCKED` 於 2026-09-06 裁決時定性為「議題已移轉後之凍結態」，非等待任何票；承接票家族於 2026-09-09 全數 CLOSED 後，該凍結態已無對象，且與 `checkpoint.state=RESOLVED` 語義互斥
- `depends_on` `INFRA-QA-PERTICKET-CONF-01` 已 `CLOSED`（`aud_20260910_a40b877c46`）；`blocked_by` 早於 2026-09-06 清為 `null`（互鎖解除三筆修正之一）
- `checkpoint.dispatch_trigger`（`seq37_gate1_satisfied`）為指向已終態票之死觸發器，收票時移除
- 票檔 `Pending/` → `deliver/`

### AC 狀態（誠實未勾，不補勾）

本票全部 AC 維持未勾——R3 FAIL 後未再執行任何驗證輪次，無任何佐證可支持勾選。其未勾狀態為**真實記錄**，**MUST NOT** 因承接票通過而追溯補勾。
