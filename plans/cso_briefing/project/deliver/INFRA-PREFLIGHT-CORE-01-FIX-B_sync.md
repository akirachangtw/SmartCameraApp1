# INFRA-PREFLIGHT-CORE-01-FIX-B: 同步 preflight adapter 測試載體與 SKILL.md 決策核心契約 pending 敘述

**簽發日期**: 2026-09-09
**簽發者**: Traycer（草案）／SMTR（Rev A 修訂，2026-09-09）

**Rev A（2026-09-09，SMTR 派工前修訂，Akira 核准）**：Traycer 草案之核心發現（`FIXA_TICKET_FILE` 路徑失效、`SKILL.md` 敘述落差）經 SMTR 一手核實**成立**（`aud_20260909_fdfa2a1e71`：`test_preflight_adapter.sh:33` 確指 `Pending/`，該檔已隨 FIX-A 收尾搬 `deliver/`；L258 `cp`／L276 `sed -i`／L315 還原皆依賴此路徑，失效將使 R1-1..R1-4 共 17 項斷言直接爆掉）。惟票面有三處格式與先例問題，派工前修正：
- **①AC 區塊格式崩壞（阻斷級）**：六項 AC 擠於同一段落、無換行、無 `- [ ]` checkbox，無法逐項勾選、`verify_checklist.sh` 亦讀不到。已依 TPL v3.10 重排為逐行 `- [ ] **[類型]**`；Action Items 之「完成」同補 `- [ ]`。
- **②`PRESET=docs` 用錯**：AI-1 改動可執行測試腳本且票面自帶「重跑全部 case ID 全綠」之行為性 AC，非 TPL v3.10 之 docs 定義（契約增補／治理檔同步／歸檔建檔／模板修訂）。且 `assembleDebug` 之 `N/A` 豁免**僅適用 docs preset**——FIX-A 為 `bugfix` 且保留該 AC，FIX-B 同一 `.claude/skills/qa/` 範圍以 docs 豁免即 preset shopping 並製造先例漂移。改 `PRESET=bugfix`，`assembleDebug` 恢復保留並沿用 FIX-A 同一句歸因規則。
- **③檔名違反短檔名律**：原 `INFRA-PREFLIGHT-CORE-01-FIX-B.md` 缺 slug（TPL v3.9 要求 `<TICKET_ID>_<slug>.md`）。已更名 `INFRA-PREFLIGHT-CORE-01-FIX-B_sync.md`（37 字元，未逾 40；本票為新開，不受「既有檔名不追溯改名」豁免）。檔頭重複標題已刪。
- **新增 AI-3（Akira 核准，清母票欠帳）**：母票 `INFRA-PREFLIGHT-CORE-01` 甲案裁決之 `SKILL.md` 回鏈更正（`md:410`→`md:413`）因 FIX-A 之 ALLOWED 不含 `SKILL.md` 而未清；本票 ALLOWED 恰含該檔，為最省落點。再不清即成為已 CLOSED 母票之永久殘留。
- **KAIROS 候選（不入本票）**：以「真實既存票檔」作 R-1 stimulus 天生脆弱——任何票檔生命週期轉態皆會打斷測試。候選改進為改用專用 fixture 票檔（置 `.claude/skills/qa/tests/fixtures/`，仍為真實檔案與真實 `git hash-object` 路徑，滿足「真實 collector stimulus」語義，但不隨票務流轉）。HC-3 禁重構於本票正確，此項另議。
**Rev A.1（2026-09-09，SMTR 派工前補正，Akira 甲案裁決）**：Traycer 於同日另提 `INFRA-PREFLIGHT-CORE-01-FIX-D`（QA SKILL 決策核心契約 truthful resync）之 `execute_review`，判 🔴 Approach Drift／T1，並建議「將 fix 指令併入下一批 execute Handoff」。**該審查經 SMTR 一手核實後作廢**，理由三項：①FIX-D 已於 2026-09-09 裁決**不核准開立**（前提造假、與本票 AI-2 完全重疊、D.3-4 凍結出口不成立、缺 Verify Config 與 AC 類型學），冊面 `registry_list` topic=INFRA 全量 16 筆**無此票**（`aud_20260909_4ccf42a5d7`）——審查對象非冊面在案票，依票務治理不生 Tier 裁決效力；②其「零 diff」認定屬實（`git_log` `2568333..6233375` 對 `SKILL.md` 零異動，`aud_20260909_b2652355d7`；`SKILL.md:83` 六欄、`:90` R-1 四型 `pending` 字面仍在，`aud_20260909_b1841914d1`），但零 diff 係**否決票之正確結果**，非執行偏移；③其建議之修改內容已由本票 AI-2 完整承載（含「不得改動第 2／3／4 列」＝本票 HC-2／AC 既有字面），且本票另有 AI-1、AI-3 為其所無。**唯一採納增量**：其措辭指出 SKILL.md 應同時反映 FIX-C 之 R-1 前置 guard 成果，與母票 D.3-4 出口裁決一致 → 落為本 Rev 之 HC-5 與 AI-2 目標值補強句。本 Rev **未新增 AI、未改任何既有 `R-n`**，僅增 HC-5 與對應 AC 一條。
**〔Rev A.1 補記，2026-09-09，Akira 回流裁決第 4 點〕**：AI-2 補強句原僅要求回鏈 FIX-C 票檔，依裁決加嚴為**回鏈實作與 history-invariant regression 之具體證據**（見 AI-2）。本補記於 Rev A.1 派工前完成、票面 Rev 字面未新增（`Rev A`／`Rev A.1` 二筆，計數＝2，4a 門檻 ≥3 未達），非新一輪 dotted 補正；原 Rev A.1 文字保留不刪，僅此加註。
**來源議題**: INFRA
**Mode**: normal → uc  ||  **Effort**: Low
**Traycer Workflow**: execute_review（**v2.1 前置聲明**：本票 AI-1 需重跑 `test_preflight_adapter.sh` 驗證行為性 AC，屬需驗功能運作性之票，開票時即明載需 `execute_review` 審查）
**前置依賴（依賴三分）**：

- `execution_dependency`: 無
- `evidence_dependency`: `INFRA-PREFLIGHT-CORE-01`（母票，`plans/cso_briefing/project/deliver/INFRA-PREFLIGHT-CORE-01_Preflight_Decision_Core_Extraction.md`）／`INFRA-PREFLIGHT-CORE-01-FIX-A`（承接票，`plans/cso_briefing/project/deliver/INFRA-PREFLIGHT-CORE-01-FIX-A_gaps.md`）——本票兩項修補之判準與現值皆源自此二票之收尾證據與 Traycer `execute_review` 一手比對
- `coordination_dependency`: 無

## 檔案治理／來源說明

上一輪 Traycer `execute_review`（對 `INFRA-PREFLIGHT-CORE-01`）以一手比對發現：`INFRA-PREFLIGHT-CORE-01-FIX-A` 已於 `deliver/` 結案（`.claude/PROGRESS.md` 記為 REVIEW，110/110 assertions PASS），但兩處驗證/契約載體仍鎖定舊生命週期狀態，未隨結案同步：

1. `test_preflight_adapter.sh` 之 `FIXA_TICKET_FILE` 仍硬指向 `plans/cso_briefing/project/Pending/INFRA-PREFLIGHT-CORE-01-FIX-A_gaps.md`——該路徑現況已不存在（票檔已搬至 `deliver/`），破壞 R1-1..R1-4 案例之可重跑性。
2. `.claude/skills/qa/SKILL.md`「## 決策核心契約」節之「已知未達項」表與第 4 點如實揭露，仍描述「僅六欄檢查」「R-1 僅 MISMATCH 一型於 adapter 層驗證」等已被 FIX-A 覆蓋之舊狀態，構成活契約文件與現況真相分歧。

本票**僅**修這兩處驗證/文檔同步落差，**不**變更任何決策邏輯、規則表或 Bash 判定路徑。

## Verify Config

```
TICKET_ID=INFRA-PREFLIGHT-CORE-01-FIX-B
CREATED=2026-09-09
PRESET=bugfix
ALLOWED=.claude/skills/qa/tests/test_preflight_adapter.sh,.claude/skills/qa/SKILL.md
FORBIDDEN=.claude/skills/qa/scripts/preflight_core.py,.claude/skills/qa/scripts/preflight_rules.json,.claude/skills/qa/scripts/preflight_activation.sh,.claude/skills/qa/tests/test_preflight_core.py,.claude/skills/sv/**,.claude/rules/**,.claude/settings.json,.claude/CLAUDE.md,app/**,tests/**
MAX_LINES=N/A（純 docsync／testsync 票，失敗形狀為審查發現式，不可先驗估算）
DIFF_REF=416c711（開票時 HEAD short-sha）
ANCHOR_PROBED=2026-09-09（依本次 session 內 live read_partial_file 直查取樣，非記憶或前次回報）
BASE_COMMIT=416c71193f751f332a3053183797765e0cb99dc7
TARGET_BLOB=.claude/skills/qa/tests/test_preflight_adapter.sh:3c963f36;.claude/skills/qa/SKILL.md:6ed09114
INTRODUCED_CAPABILITY=NONE
DEPENDENCY_DELTA=NONE
```

## Action Items

### AI-0: 錨點復驗（派工後、修改前）

- **檔案**: `.claude/skills/qa/tests/test_preflight_adapter.sh`、`.claude/skills/qa/SKILL.md`
- **動作**: 逐字確認下列「現值」於檔內命中數與本票所載相符：
  1. `test_preflight_adapter.sh` 第 32-33 行恰為：
         ```
          FIXA_TICKET_ID="INFRA-PREFLIGHT-CORE-01-FIX-A"
          FIXA_TICKET_FILE="plans/cso_briefing/project/Pending/INFRA-PREFLIGHT-CORE-01-FIX-A_gaps.md"
         ```
  2. `plans/cso_briefing/project/Pending/INFRA-PREFLIGHT-CORE-01-FIX-A_gaps.md` **不存在**於工作樹；`plans/cso_briefing/project/deliver/INFRA-PREFLIGHT-CORE-01-FIX-A_gaps.md` **存在**
  3. `SKILL.md` 第 82-93 行仍含「僅涵蓋...六欄」「僅 `MISMATCH` 一型...其餘三型...為 `pending`」字面
  4. **〔Rev A 新增〕** `SKILL.md` 第 97 行含 `INFRA-PREFLIGHT-CORE-01_...md:410` 字面（SMTR 已驗，`aud_20260909_fdfa2a1e71`）
- **〔Rev A 新增〕** 以 `git hash-object` 取 ALLOWED 二檔 sha8，回填 Verify Config `TARGET_BLOB` 兩個 `<AI-0 填實值>` 佔位；未回填不得續作
- **任一項不符 → 停止回報，禁自行推定等價位置。**
- [x] 完成：四項現值逐字相符；`TARGET_BLOB` 已以 `git hash-object` 實測回填（`test_preflight_adapter.sh:3c963f36`／`SKILL.md:6ed09114`）
- [x] **開工復驗（2026-09-09 二次 AI-0，SMTR 派工令）**：`test_preflight_adapter.sh` 自起草值已 +38/-4（`aud_20260909_fc0184c5d1`，FIX-C 貢獻），`TARGET_BLOB` 重取實測為 `b6fce448`（範圍不相交自動通過，異動與本票 AI-1 目標行無關——該行已由 FIX-C 提前修正）；`SKILL.md` 重取仍為 `6ed09114`，零異動確認非推定。AI-1 判定 `ALREADY_EXECUTED_PENDING_CLOSURE`（見下）

### AI-1: 修正 `test_preflight_adapter.sh` 之 FIX-A 票檔來源路徑

- **檔案**: `.claude/skills/qa/tests/test_preflight_adapter.sh`
- **現值**（第 33 行）：
    ```
    FIXA_TICKET_FILE="plans/cso_briefing/project/Pending/INFRA-PREFLIGHT-CORE-01-FIX-A_gaps.md"
    ```
- **目標值**：改為指向現況實存路徑：
    ```
    FIXA_TICKET_FILE="plans/cso_briefing/project/deliver/INFRA-PREFLIGHT-CORE-01-FIX-A_gaps.md"
    ```
- **理由**：R1-1..R1-4 案例（`_r1_case()`）以 `sed -i` 就地覆寫此檔之 `TARGET_BLOB` 欄位再還原，作為「真實 collector stimulus」；票檔生命週期一旦轉態離開 `Pending/`，硬編碼路徑即失效，導致該組 17 項斷言（含 `diff -q` 位元組級還原驗證）無法重跑。
- **MUST NOT** 改用其他票檔或 stub 檔案取代——語意需為「真實既存票檔」，僅路徑更新為現況落點。
- [x] **ALREADY_EXECUTED_PENDING_CLOSURE**：目標值已於 `INFRA-PREFLIGHT-CORE-01-FIX-C`（commit `7e87014`）實現，`git diff --name-only` 對本票 `git_diff` 零命中此行異動。唯讀驗證：`grep -n FIXA_TICKET_FILE test_preflight_adapter.sh:33` 現值恰為目標值，`test -f plans/cso_briefing/project/deliver/INFRA-PREFLIGHT-CORE-01-FIX-A_gaps.md` 存在、`Pending/` 同名檔不存在。**MUST NOT** 記為本票交付，歸因 FIX-C

### AI-2: 更正 `SKILL.md` 決策核心契約段之已結案 pending 敘述

- **檔案**: `.claude/skills/qa/SKILL.md`
- **現值**（第 82-83 行，R-7 如實揭露段）：
    ```
    「必要欄位缺漏」現行僅涵蓋 invoke_core_decision() protocol 完備性檢查之六欄...不含
    CORE_WRITE_AUTHORITY／CORE_TICKET_ID／CORE_CONTEXT_ID 三欄...MUST NOT 讀作「任一必要欄位
    缺漏皆已 fail-closed」；此為 AI-4/AI-5.1 既有程式碼缺口，非本票 AI-6/AI-7 新增，修補承接
    INFRA-PREFLIGHT-CORE-01-FIX-A AI-1/AI-2。
    ```
- **目標值**：改寫為反映 FIX-A 已結案之現況，逐字回鏈 FIX-A 完成證據（九欄 `CORE_REQUIRED_FIELDS`、R7-4 全覆蓋），**MUST** 保留原句「人工續作不構成工具背書」等既有規範性語句不動，僅更正事實敘述段落。
- **現值**（第 90 行，已知未達項表第 1 列）：
    ```
    | 1 | R-1 四型阻斷 | pending（adapter 層） | ...adapter 層以真實 active.json mtime 不變驗證者僅
    MISMATCH 一型，其餘三型之 adapter 全流程重跑驗證為 pending。（AC ...:475，未勾） |
    ```
- **目標值**：改寫為反映 FIX-A AI-4（R1-1..R1-4 四型皆以真實 collector stimulus 於 adapter 全流程驗證）已完成之現況，回鏈 `plans/cso_briefing/project/deliver/INFRA-PREFLIGHT-CORE-01-FIX-A_gaps.md` 對應行號。
- **〔Rev A.1 補強〕** 該列並 **MUST** 一併載明 `INFRA-PREFLIGHT-CORE-01-FIX-C` 之成果——core 層 R-1 前置 guard 與 history-invariant 回歸——並回鏈 `plans/cso_briefing/project/deliver/INFRA-PREFLIGHT-CORE-01-FIX-C_r1-guard.md`。此敘述之前提為 FIX-C 已達終態；本票 `blocked_by=INFRA-PREFLIGHT-CORE-01-FIX-C`，派工時該前提即已成立（若開工時 FIX-C 仍未達終態 → 停止回報升裁，禁先行寫入「已完成」字面，D.3-3 文件忠實性）。
- **〔Rev A.1 補記〕證據回鏈 MUST 具體到實作與回歸兩層**，不得僅指票檔檔名：
  1. **實作層**：commit `7e87014`；`preflight_core.py::_is_r1_blob_failure_guard()` 位於 F-3 之前（FIX-C 票面 AC 實測 `L225` 早於 F-3 `L228`），判定式僅含 `baseline_check`／`blob_check`，零 `v1_scope`／`v1_provenance` 引用
  2. **回歸層**：`test_preflight_adapter.sh` history-invariant 前提斷言（`BASE_COMMIT..HEAD` 存在 `[INFRA-PREFLIGHT-CORE-01-FIX-A]` 前綴 commit）＋每案 core `final_status`／`may_activate_ticket` 直接探針；assertions 110→119（+9），既有 case ID 全數保留可重跑，119/119 PASS
  3. 上列數值 MUST 於 AI-0 以 FIX-C 交付票檔（`deliver/INFRA-PREFLIGHT-CORE-01-FIX-C_r1-guard.md`）現值復驗後方得引用；與現值不符 → 停止回報，禁沿用本票面所載舊值
- **MUST NOT** 更動表格其餘三列（多 worktree 並行、四入口實機、新增規則演練）——此三項 FIX-A 亦未完全達成（W2 選作跳過、四入口未驗、規則演練未做），**MUST** 維持 `pending`／`planned` 標記不變，不得因本票順帶美化。
- [x] 完成 — `SKILL.md` R-7 揭露段（原第 82-83 行）已改寫，回鏈 FIX-A AI-1/AI-2（九欄 `CORE_REQUIRED_FIELDS` 陣列驅動、R7-4 九子案 55 項斷言）；已知未達項表第 1 列改寫為「已完成」，回鏈 FIX-A AI-4（core+adapter 全流程驗證）＋FIX-C（`_is_r1_blob_failure_guard()` L225 早於 F-3 L228、history-invariant 回歸 assertions 110→119）；第 2／3／4 列 `pending`／`planned` 標記逐字未動（`git diff` 佐證）；`workaround`／`僅測試層`／`暫時性繞道`／`測試輔助` 四類降格字面命中＝0，guard 定性句含「core」＋「前置」語義

### AI-3: 清母票欠帳——`SKILL.md` `route_v2_state` 職責歸類回鏈行號更正〔Rev A 新增〕

- **檔案**: `.claude/skills/qa/SKILL.md`
- **現值**（第 97 行）：`（AI-6 職責歸類表 `INFRA-PREFLIGHT-CORE-01_...md:410`，唯一消費點為診斷 echo）`
- **目標值**：`md:410` → `md:413`
- **理由**：母票 `INFRA-PREFLIGHT-CORE-01` 職責歸類表中 `route_v2_state()` 實列於 `:413`；`:410` 為 `normalize_ticket_path()`。此更正為母票甲案裁決（Akira 2026-09-08）落於母票 L444 之 TODO，因 FIX-A 之 ALLOWED 不含 `SKILL.md` 而未清。母票已 REVIEW／交付，不再重開，本票 ALLOWED 恰含該檔為最省落點。
- **MUST** 僅改該三位數字面，**MUST NOT** 更動同行其餘文字或其他行之任何回鏈行號
- [x] 完成 — `md:410`→`md:413`（母票 L413 確認為 `route_v2_state()` 職責歸類表行，非 `normalize_ticket_path()`）；全檔 `md:410` 命中＝0；同行其餘文字未動（`git diff` 僅該三位數字面異動）

## Hard Constraints

1. **禁觸碰任何決策邏輯**：`preflight_core.py`／`preflight_rules.json`／`preflight_activation.sh` 為 FORBIDDEN，本票純屬測試載體與契約文檔之現況同步，不得動任何 `if`／`case`／規則列。
2. **禁美化未達成項**：SKILL.md「已知未達項」表僅第 1 列（R-1 四型阻斷）因 FIX-A AI-4 而更正；其餘三列（多 worktree 並行、四入口實機、新增規則演練）之 `pending`／`planned` 標記 **MUST** 逐字保留不動。
3. **禁引入第二份路徑集合**：AI-1 修正僅改路徑字面值，不得順帶重構 `_r1_case()` 函式或新增抽象層。
4. **禁靜默刪除揭露段**：AI-2 之改寫 **MUST** 保留「MUST NOT 讀作...」等既有規範性防護句式，僅更正描述現況的事實子句。
5. **〔Rev A.1〕禁降格 FIX-C 之 R-1 guard 定性**：AI-2 改寫 **MUST NOT** 將 FIX-C 所落地之 core 層 R-1 前置 guard 描述為「僅測試層 workaround」「暫時性繞道」「測試輔助」或任何等義降格措辭。該 guard 為 core 判定路徑之無條件安全不變量（FIX-C 裁決明示不採 adapter-only 修法），降格敘述將使日後讀者誤判其可被移除。

## Error Handling

- AI-0 任一現值不符 → 停止回報，禁自行推定等價位置
- `test_preflight_adapter.sh` 修正後任一斷言 FAIL（含既有 R7-1/R7-2/R7-3/R4-1..4/R7-4/MTIME-1/R1-1..4 case ID）→ 停止回報，禁改動生產邏輯讓測試過
- FIX-A 交付票（`plans/cso_briefing/project/deliver/INFRA-PREFLIGHT-CORE-01-FIX-A_gaps.md`）於本票開工前已再次搬離 `deliver/` → 停止回報升裁，禁自行猜測新路徑
- **[標配]** 令含 commit／版控標的時，`git check-ignore -v <標的路徑>` 命中 → 停止回報（gitignore 死結律）

## Acceptance Criteria

- [x] **[存在性]** `test_preflight_adapter.sh` 之 `FIXA_TICKET_FILE` 指向 `plans/cso_briefing/project/deliver/INFRA-PREFLIGHT-CORE-01-FIX-A_gaps.md`，且該路徑於工作樹實際存在（`test -f` 或等效檢查） —— 已由 FIX-C（`7e87014`）達成，`test -f` 實測存在，`Pending/` 同名檔不存在
- [x] **[行為性]** given 修正後之路徑，when 執行 `bash .claude/skills/qa/tests/test_preflight_adapter.sh`，expect 全數既有 case ID（`MTIME-1`／`R7-1`／`R7-2`／`R7-3`／`R4-1..4`／`R7-4`／`R1-1..4`）逐一可重跑且全綠；斷言總數對 FIX-A 收尾基線（110）之 delta MUST 逐項附差異說明。**MUST NOT** 以固定歷史 assertion 總數作為 STOP 判準（承 FIX-A Error Handling 取代條款）；若既有 case ID 消失、被弱化或無法重跑 → 停止回報 —— 實跑：`test_preflight_adapter.sh: 119 passed, 0 failed`；全部具名 case ID 於輸出中可見（R7-4 九子案、R1-1..4 四型、history-invariant）；delta 110→119（+9）歸因 FIX-C，非本票引入（本票對此檔零異動）
- [x] **[行為性]** given `SKILL.md` 決策核心契約段第 1 列已改寫，when 對照 `deliver/INFRA-PREFLIGHT-CORE-01-FIX-A_gaps.md` AI-4 完成記載，expect 兩者描述之 R-1 四型 adapter 驗證狀態一致（皆為已完成，非 `pending`） —— 已改寫為「已完成」並回鏈 FIX-A AI-4 與 FIX-C
- [x] **[存在性]** `SKILL.md` 已知未達項表第 2／3／4 列（多 worktree 並行／四入口實機／新增規則演練）之 `pending`／`planned` 標記逐字保留，`git diff` 佐證此三列文字未被觸碰 —— 三列文字逐字未動，`git diff` 僅第 1 列與相鄰兩處異動
- [x] **[存在性]〔Rev A 新增，AI-3〕** `SKILL.md` 第 97 行回鏈為 `md:413`；全檔 `md:410` 字面命中＝0；同行其餘文字未變（`git diff` 佐證該行僅三位數字面異動） —— 實測命中＝0
- [x] **[存在性]〔Rev A.1，HC-5〕** `SKILL.md` 全檔中描述 FIX-C R-1 guard 之語句，`workaround`／`僅測試層`／`暫時性繞道`／`測試輔助` 四類降格字面命中＝0；且該 guard 之定性字面含「core」與「前置」語義（`git diff` 佐證） —— 四類降格字面命中＝0；「core 層新增無條件前置 guard」字面在盤
- [x] `assembleDebug` PASS —— **保留強制**（Rev A：preset 改 `bugfix`，TPL v3.10 之 `N/A` 豁免僅適用 `docs`）。本票 scope 全在 `.claude/`，預期 no-op；**若失敗且與 ALLOWED 二檔無因果關係 → 記為環境事實，不得歸因本票、不得升裁**，另循 housekeeping 處理（沿用 FIX-A 同一歸因規則）
- [x] `git diff --stat` 僅涉及 ALLOWED 檔案（`.claude/skills/qa/tests/test_preflight_adapter.sh`、`.claude/skills/qa/SKILL.md`） —— 實測僅 `SKILL.md` 有異動（+3/-3），`test_preflight_adapter.sh` 零異動（AI-1 已由 FIX-C 提前達成）

---

## 收尾實錄（耐久載體＝本票檔；依四要件④，轉終態前落檔）

**碼閉環（2026-09-09）**：AI-0～AI-3 全數完成，AC 8/8 全勾。AI-1 為 `ALREADY_EXECUTED_PENDING_CLOSURE`（歸因 `INFRA-PREFLIGHT-CORE-01-FIX-C` commit `7e87014`，本票僅唯讀復驗，未新增交付）。

**Commit**：`1dfddd4`（AI-2/AI-3，`SKILL.md` 單檔，+3/-3，唯一 Implementation Scope 異動）＋`46dd4bb`（`PROGRESS.md` Dashboard 同步，Pipeline Artifact）。

**審查鏈**：`/qa` PASS（`assembleDebug` BUILD SUCCESSFUL、`testDeviceDebugUnitTest` 全綠、Lint 0 new warnings、`test_preflight_adapter.sh` 119/119 PASS）→ `/sv` 階段 A-C PASS → Codex Cross-Review R1 REPORT_FIX（sv_report.md AC 措辭精確化：Implementation Scope diff vs 全量 diff）→ R2 **PASS**（`REACHABILITY: NONE`）。

**票面聲明之 `execute_review`（Traycer）**：票頭明載「本票 AI-1 需重跑 `test_preflight_adapter.sh` 驗證行為性 AC，屬需驗功能運作性之票」。AI-1 實際已由 FIX-C 提前達成、本票僅唯讀復驗（非新增功能變更），惟票面既有聲明不因此追溯撤銷；`reviews.traycer` 依既有五段語義維持 `PENDING`，待 Akira 於 VS Code 執行 `execute_review` 人工複審，lifecycle 不因此停於 `EXECUTED`（依 ssot-governance.md「Traycer T2 執行邊界」條款）。

**D-2 一次性收束提醒**：依票面 Rev A.1 註記，母票／FIX-A／FIX-B 之 metadata 於本票收尾後一次性收束；FIX-C／FIX-A 之成果（core R-1 guard、九欄 CORE_REQUIRED_FIELDS）**MUST NOT** 倒灌為母票原始交付能力之描述，僅得以回鏈引用方式呈現（已於 `SKILL.md` AI-2/AI-3 段落落實）。