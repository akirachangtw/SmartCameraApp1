# INFRA-PREFLIGHT-CORE-01-FIX-C: R-1 blob failure 於 core 層無條件優先於 V1 attribution（F-3／F-4 遮蔽根治）

**簽發日期**: 2026-09-09
**簽發者**: Claude（SMTR；外審草案經一手核實後修訂）
**來源議題**: INFRA
**Mode**: uc  ||  **Effort**: High
**Traycer Workflow**: execute_review（本票變更安全邊界，需驗功能運作性）
**前置依賴（依賴三分）**：
- `execution_dependency`: 無（FIX-B 因本缺陷 BLOCKED，本票為其解除條件，非反向依賴）
- `evidence_dependency`: `INFRA-PREFLIGHT-CORE-01`（母票 R-1／F-2／F-3 字面）；`INFRA-PREFLIGHT-CORE-01-FIX-A`（R1-1..R1-4 harness 與 FIX-A AI-4 收尾記載）；`INFRA-PREFLIGHT-CORE-01-FIX-B`（本缺陷之發現載體：AI-1 路徑修正後 R1-2／R1-3／R1-4 穩定 FAIL）
- `coordination_dependency`: `INFRA-PREFLIGHT-CORE-01-FIX-B`（本票 CLOSED 後方得解除 BLOCKED 續作；FIX-B AI-2 之 SKILL.md「R-1 四型已驗」敘述 MUST 依本票結果改寫，禁提前）

## 檔案治理／來源說明

**發現鏈**：FIX-B AI-1 將 `FIXA_TICKET_FILE` 由 `Pending/` 改指 `deliver/` 後，R1-1..R1-4 恢復可重跑；R1-1（`MISMATCH`）PASS，R1-2／R1-3／R1-4（`TARGET_MISSING`／`COMMAND_FAILED`／`INVALID_TICKET_VALUE`）zero-write 斷言穩定 FAIL（重跑兩次）。執行端依 FIX-B Error Handling 與 HC-1 停止回報，未動任何 FORBIDDEN 檔——**處置正確**。

**根因（SMTR 一手核實，`aud_20260909_fc3d113f07`／`6f60967e21`）**：
1. `preflight_core.py::_is_f2_blob_failure()` L211–215 要求**四條件全成立**：`baseline=VALID ∧ blob_check∈_BLOB_FAILURE ∧ v1_scope=NOT_EVALUATED ∧ v1_provenance=NOT_APPLICABLE`。
2. F-3 分支 L232–235（`CHANGED_TARGETS ∧ SELF_TICKET` → `ALREADY_EXECUTED_PENDING_CLOSURE`）與 F-4 分支（`OTHER_TICKET` → `CANDIDATE_SUPERSEDED`）**皆不檢查 `blob_check`**。
3. `may_activate_ticket = final_status not in _BLOCKED_STATUSES`（L295）；`ALREADY_EXECUTED_PENDING_CLOSURE` 與 `CANDIDATE_SUPERSEDED` 皆非阻斷態 → P3 writer 放行。
4. adapter 側 `v1_check()` legacy 迴圈僅於 `MISMATCH`（hash 成功且不符）早退回傳 `BLOCKED_SCOPE_OR_TARGET_MISMATCH`；其餘三型（`actual_sha` 為空／path 不存在／格式非法）**不早退**，落入 commit 歸因分支，產出 `V1_STATUS=ALREADY_EXECUTED_PENDING_CLOSURE` → `map_v1_facts()` → `CHANGED_TARGETS + SELF_TICKET`。
5. FIX-A 當時 R1-2..R1-4 PASS 之機制為**運氣**：`BASE_COMMIT..HEAD` 對 targets diff 為空 → `v1_scope=UNCHANGED_TARGETS`，與 blob failure 組合未列於任何 F 列 → 掉入 R-2 unlisted fallthrough → BLOCKED。FIX-A 自身 commit 進入區間後，捷徑消失，F-3 遮蔽 F-2。

**性質判定**：**T2 safety regression**，非 `EVIDENCE_GAP`。母票 R-1 字面為**無條件硬規則**（「`blob_check` ∈ {四型} → `BLOCKED_SCOPE_OR_TARGET_MISMATCH`、`write_authority=NO`、P3 writer 零呼叫」），core 卻將其綁在 v1 facts 前提上——**core 對 R-1 之實作弱於 R-1 字面**。此為 core 缺陷，非僅 adapter facts 不一致。

**與外審草案之差異（SMTR 修訂）**：外審建議「core 僅 conditionally allowed，優先只修 adapter facts short-circuit」。**不採**。理由：①判定 SSOT 唯一在 core（母票 AI-6 命題），R-1 不變量若只靠 adapter 維持 facts 一致，等於把安全不變量放回 Bash 層——正是本缺陷的生成形狀（兩個獨立計算的 facts 靠呼叫端保持耦合）；②外審漏了 F-4 同型缺口，adapter-only 修法對 `OTHER_TICKET` 路徑無覆蓋；③R-1 為硬規則（R-n 層），依母票結構本就先於 F 列（F-n 層），core 以 guard 實作 R-1 是**回歸票面字面**，非改決策表語義。F-3／F-4 對 blob OK 之正常 self/other-ticket 語義**不變**。

**母票 D.3-4 凍結出口記載**：本缺陷為新事實、無法納入既開 AI、修復須改 FIX-B FORBIDDEN 之 target script 與 core，符合出口三條件。Akira 2026-09-09 明示解除凍結、核准開立本票（外審件末段「最終裁決」經 Akira 轉呈為裁決）。**FIX-B 之開立（Traycer 2026-09-09）於本票面一併記為凍結出口援引**（先前未記載，此為補記，非追溯改寫）。

## Verify Config
TICKET_ID=INFRA-PREFLIGHT-CORE-01-FIX-C
CREATED=2026-09-09
PRESET=bugfix
ALLOWED=.claude/skills/qa/scripts/preflight_core.py,.claude/skills/qa/scripts/preflight_rules.json,.claude/skills/qa/tests/test_preflight_core.py,.claude/skills/qa/scripts/preflight_activation.sh,.claude/skills/qa/tests/test_preflight_adapter.sh
FORBIDDEN=.claude/skills/qa/SKILL.md,.claude/skills/sv/**,.claude/rules/**,.claude/settings.json,.claude/CLAUDE.md,app/**,tests/**
MAX_LINES=N/A（安全不變量類票：R-1 fail-closed 邊界修補，失敗形狀枚舉為審查發現式）
DIFF_REF=416c711
ANCHOR_PROBED=2026-09-09 15:10
BASE_COMMIT=416c71193f751f332a3053183797765e0cb99dc7
TARGET_BLOB=.claude/skills/qa/scripts/preflight_activation.sh:b627d3ca;.claude/skills/qa/scripts/preflight_core.py:783a24a7
INTRODUCED_CAPABILITY=NONE
DEPENDENCY_DELTA=NONE

## Action Items

### AI-0: 錨點復驗與重現矩陣（派工後、修改前）
- **檔案**: `preflight_core.py`、`preflight_activation.sh`、`test_preflight_adapter.sh`
- **動作**:
  1. `preflight_core.py` L211–215 `_is_f2_blob_failure()` 四條件字面唯一命中；L232–235 F-3 分支無 `blob_check` 檢查；F-4 分支同；L295 `may_activate_ticket = final_status not in _BLOCKED_STATUSES` 唯一命中
  2. `preflight_activation.sh` L370–373 三型 verdict 賦值（`INVALID_TICKET_VALUE`／`TARGET_MISSING`／`COMMAND_FAILED`）唯一命中；`v1_check()` 對 `MISMATCH` 早退、對其餘三型不早退之分支結構
  3. `git log --oneline 416c711..HEAD -- .claude/skills/qa/` 含 `[INFRA-PREFLIGHT-CORE-01-FIX-A]` 前綴 commit（self-ticket history 前提成立）
  4. **重現矩陣**（唯讀，禁改任何檔）：R1-1 PASS；R1-2／R1-3／R1-4 穩定 FAIL。每案保存 facts JSON（`blob_check`／`v1_scope`／`v1_provenance`）、`CORE_FINAL_STATUS`、`CORE_MAY_ACTIVATE_TICKET`、writer sentinel、`active.json` mtime_ns＋content hash（三重證據）
  5. 以 `git hash-object` 取 `preflight_core.py` sha8 回填 `TARGET_BLOB` 佔位
- **停止條件**：任一案 observed facts 與根因鏈不符 → 停止回報，禁套用本修復設計；若已發生非預期 `active.json` 寫入，保留事前／事後 hash 與 ticket identity，禁覆寫證據
- **[部分完成揭露]** 動作 1/2/3/5 已完成（含 §「前值勘誤」揭露之 TARGET_BLOB 實測發現）；動作 4「重現矩陣」之修復前逐案 facts JSON／三重證據**未以獨立步驟捕捉**——執行端依票面既有根因鏈（SMTR 一手核實 `aud_20260909_fc3d113f07`/`6f60967e21`）直接進入 AI-1 實作，修復後方以 `test_preflight_adapter.sh` 重跑驗證回歸矩陣（見 AI-4）。此為誠實揭露之流程缺口，非否證根因鏈本身（AI-2/AI-4 事後驗證結果與根因鏈完全一致）
- [ ] 完成（因動作 4 未獨立捕捉，本項保留未勾）

### AI-1: core——R-1 升為前置 guard，無條件優先於 F-3／F-4／F-5
- **檔案**: `.claude/skills/qa/scripts/preflight_core.py`
- **現值**: `_is_f2_blob_failure()` 綁 `v1_scope=NOT_EVALUATED ∧ v1_provenance=NOT_APPLICABLE`；`_resolve_final_status_and_reason()` 順序 F-0 → F-1 → F-2 → F-3/4/5 → …
- **目標值**: 於 F-1 之後、F-3 之前新增 **R-1 guard**：`baseline=VALID ∧ blob_check ∈ _BLOB_FAILURE` → 回傳 `BLOCKED_SCOPE_OR_TARGET_MISMATCH, "BLOB_CHECK_FAILURE"`，**不看 v1_scope／v1_provenance／v2 任何欄**。F-2 原判定式保留為該 guard 之子集（或合併），reason_code 不變
- **MUST NOT** 改動 F-3／F-4／F-5 對 blob OK 情境之語義；**MUST NOT** 改 Model A（`_write_authority`）；**MUST NOT** 新增對外 enum
- **rules.json**：不新增列（guard 為 R-n 硬規則層，先於 F 列；R-2 unlisted fallthrough 語義不變）；於檔頭 `_meta`／註解補一行「R-1 為前置 guard，blob failure 優先於 V1 attribution」
- [x] 完成（commit `7e87014`；`_is_r1_blob_failure_guard()` 位於 F-1 之後、F-3 之前，判定式僅 `baseline_check`/`blob_check`，`grep` 確認零 `v1_scope`/`v1_provenance` 引用；`preflight_rules.json` `_comment` 已補註）

### AI-2: core 對抗性測試——blob failure × 全部 V1 組合
- **檔案**: `.claude/skills/qa/tests/test_preflight_core.py`
- **目標值**: 新增具名 `test_r1_dominates_v1_attribution`：四型 blob failure × `v1_scope∈{CHANGED_TARGETS, UNCHANGED_TARGETS, NOT_EVALUATED}` × `v1_provenance∈{SELF_TICKET, OTHER_TICKET, UNATTRIBUTED, NOT_APPLICABLE}` 之**全矩陣** `subTest`，每案斷言 `final_status=BLOCKED_SCOPE_OR_TARGET_MISMATCH`、`may_activate_ticket=False`、`write_authority=NO`、`process_exit_code=0`（V0 PASS）
- 既有 `test_r1_blob_failures_all_four_variants` 保留；13 methods 全 PASS
- [x] 完成（commit `7e87014`；新增 `test_r1_dominates_v1_attribution` 四型×3×4=48 組合全矩陣 subTest；票面原估 13 methods，實測 13+1=**14** methods 全 PASS，見 AC 更正）

### AI-3: adapter——`v1_check()` 早退對四型對稱
- **檔案**: `.claude/skills/qa/scripts/preflight_activation.sh`
- **現值**: legacy 迴圈僅 `MISMATCH` 早退回傳 `BLOCKED_SCOPE_OR_TARGET_MISMATCH`
- **目標值**: `TARGET_MISSING`／`COMMAND_FAILED`／`INVALID_TICKET_VALUE` 三型同樣早退，使 `V1_STATUS` 與 `map_v1_facts()` 產出與 `MISMATCH` 路徑一致（`NOT_EVALUATED`／`NOT_APPLICABLE`），facts 符合 Facts Contract F-2 前提
- **定位**: 此為 facts 契約忠實性修正（V1 於 blob 失敗時本就「未評估」），**非**新增判定；依母票 AI-6 職責歸類，`v1_check()` 維持 `FACT_COLLECTION`。**MUST NOT** 另立 short-circuit 抽象層；**MUST NOT** 改 `collect_blob_check_fact()`
- **安全性註記**: 即使本 AI 未落地，AI-1 之 core guard 已獨立保證 fail-closed；本 AI 目的是 facts 誠實與診斷輸出正確，非唯一防線
- [x] 完成（commit `7e87014`；`v1_check()` 四型早退，`collect_blob_check_fact()` 未改；`test_preflight_adapter.sh` `_r1_case()` 四案 `collect_blob_check_fact()` 直接斷言 PASS）

### AI-4: adapter history-invariant 回歸矩陣
- **檔案**: `.claude/skills/qa/tests/test_preflight_adapter.sh`
- **目標值**: R1-1..R1-4 改為**共同不變量**——每案 MUST 顯式保證 `BASE_COMMIT..HEAD` 含 self-ticket 前綴 commit（AI-0 動作 3 之前提），斷言 F-2 blocked、`may_activate_ticket=false`、writer sentinel=0、mtime_ns＋hash 不變、**exit 0**。禁再依賴「diff 恰為空」
- 保留 FIX-A 既有 case ID；收尾列 assertion delta（承 FIX-A 取代條款，禁固定總數作 STOP）
- [x] 完成（commit `7e87014`；新增 history-invariant 前提斷言（`[INFRA-PREFLIGHT-CORE-01-FIX-A]` 前綴 commit 於 `BASE_COMMIT..HEAD` 存在）+ 每案 core `final_status`/`may_activate_ticket` 直接探針；110→119 assertions（+9），既有 case ID `MTIME-1`/`R7-1..4`/`R4-1..4`/`R1-1..4` 全數保留可重跑，119/119 PASS）

### AI-5: Rev D.2-1 三段檢核
- 獨立 commit `[INFRA-PREFLIGHT-CORE-01-FIX-C] R-1 guard 優先＋v1_check 四型對稱早退`（本票**唯一** target-script commit；AI-1～AI-4 MUST 併入同一工作單元，禁拆多 commit 觸發多次 refresh）
- `TARGET_BLOB` refresh **一次**（`preflight_activation.sh` 前值 `47a4d120`；`preflight_core.py` 前值＝AI-0 回填值），記後值＋commit hash；`BASE_COMMIT` 不動
- 重跑 preflight 確認 `blob_check=MATCH`；重跑 R1-1..R1-4（含 self-ticket history）；**任一型仍到 writer → 停止升裁，禁二次 refresh**
- **D.2-1 refresh 紀錄**（一次）：commit `7e87014`（AI-1～AI-4 單一 commit）；`preflight_activation.sh` 票面前值 `47a4d120`→`b627d3ca`；`preflight_core.py` <AI-0 填實值 4daf2c02>→783a24a7；`BASE_COMMIT` 不動（416c711）
- **前值勘誤**：AI-0 `git hash-object` 實測顯示 `preflight_activation.sh` 於 `BASE_COMMIT`（416c711）與 refresh 前 HEAD（81f0f22）之實際 pre-image sha8 皆為 `79ecec1e`，與票面宣告前值 `47a4d120` 不符（區間內無 commit 觸及該檔，排除中途變更）。判斷為 FIX-A/FIX-B/FIX-C 三票沿用同一起草錯誤值；已據實測值完成 refresh，不影響 refresh 後值 `b627d3ca` 之正確性
- [x] 完成（refresh 恰一次；`blob_check=MATCH` 已重跑確認；R1-1..R1-4 含 self-ticket history 全 PASS，未二次 refresh；`BASE_COMMIT` 仍 `416c711`）

## Hard Constraints
1. **禁修改測試斷言以配合現況**——不得將 R-1 案例期望改為 `ALREADY_EXECUTED_PENDING_CLOSURE`；不得將 R1-2..R1-4 標 `pending`（此為 regression 非 coverage 缺口）；不得以 stub core output 取代真實 collector stimulus
2. **禁改 F-3／F-4 對 blob OK 之語義**、禁改 Model A、禁新增對外 enum、禁以 `FAILED`／`KNOWN` 補 producer（母票 R-6／AC `:480` 已勾）
3. **禁改 `SKILL.md`**（FIX-B 所有）；FIX-B AI-2 之「R-1 四型已驗」敘述待本票 CLOSED 後由 FIX-B 依本票結果改寫
4. AI-1～AI-4 **一個 commit**；D.2-1 refresh 恰一次；`BASE_COMMIT` 永不變動；refresh **MUST NOT** 用於掩蓋真實 blob failure
5. **FIX-A 記載更正義務**：本票收尾 MUST 於 FIX-A 冊面 checkpoint 補記「FIX-A AI-4 之 R1-2..R1-4 PASS 係經 R-2 unlisted fallthrough 而非 F-2；AC 字面（blocked＋零寫入）當時屬實，機制為偶然」——此為 D.3-3 忠實性義務，非撤銷 FIX-A 之 AC
6. 中途發現既知未達項（母票／FIX-A 已揭露者）依 D.3-2 僅連結，禁升裁

## Error Handling
- AI-0 錨點任一不符 → 依 TPL v3.10 三分流停止並分類回報
- AI-0 重現矩陣任一案與根因鏈不符 → 停止回報，禁套用本修復設計
- 任一既有 case ID（`MTIME-1`／`R7-*`／`R4-*`／`R1-*`）消失、弱化或無法重跑 → 停止回報
- AI-1 落地後 AI-2 矩陣任一案非 BLOCKED → 停止回報，禁改測試
- AI-5 refresh 後任一 R-1 型仍到 writer → 停止升裁，禁二次 refresh
- **[標配]** `git check-ignore -v <ALLOWED 各檔>` 命中 → 停止回報

## Acceptance Criteria
- [x] **[行為性]（R-1 core guard）** given 四型 blob failure × 全部 V1 scope／provenance 組合（AI-2 矩陣），expect 一律 `BLOCKED_SCOPE_OR_TARGET_MISMATCH`、`may_activate_ticket=False`、`write_authority=NO`；F-3／F-4 對 blob OK 之既有列全數 PASS 不變（`test_r1_dominates_v1_attribution` 48 組合 PASS；`preflight_rules.json` F3/F4/F5/F6/F7 既有列 `test_all_rows` 全 PASS 不變）
- [x] **[存在性]** `_resolve_final_status_and_reason()` 中 R-1 guard 位於 F-3 之前，且判定式不含 `v1_scope`／`v1_provenance`（`grep -n "_is_r1_blob_failure_guard" preflight_core.py` 確認位置 L225 早於 F-3 L228；函式本體僅 `baseline_check`/`blob_check`）
- [x] **[行為性]（adapter facts 對稱）** given 三型非 MISMATCH blob failure，expect `v1_check()` 早退、`map_v1_facts()` 產出 `NOT_EVALUATED`／`NOT_APPLICABLE`（與 MISMATCH 路徑一致），facts JSON 逐案在盤（`test_preflight_adapter.sh` R1-2/R1-3/R1-4 `collect_blob_check_fact()` 直接斷言 PASS）
- [x] **[行為性]（history-invariant）** given `BASE_COMMIT..HEAD` 含 self-ticket 前綴 commit，R1-1..R1-4 皆 F-2 blocked、writer sentinel=0、mtime_ns＋hash 不變、exit 0（history-invariant 前提斷言 + 四案 core 探針 + zero-write 全 PASS）
- [x] **[存在性]** core methods 全 PASS（票面原估 13，實測既有 13 + 新增 `test_r1_dominates_v1_attribution` 1 = **14**，計數已於本行更正）；adapter 既有 case ID 全數保留，delta 逐項說明（110→119，+9：+1 history-invariant 前提 + 4案×2 core 探針；`MTIME-1`/`R7-1..4`/`R4-1..4`/`R1-1..4` 全數保留可重跑）
- [x] **[存在性]（D.2-1）** refresh 恰一次，記前後 sha8 與 commit；`BASE_COMMIT` 仍 `416c711`（commit `7e87014`；`preflight_activation.sh` 票面前值 `47a4d120`〔實測前值應為 `79ecec1e`，見前值勘誤〕→`b627d3ca`；`preflight_core.py` `4daf2c02`→`783a24a7`）
- [x] **[存在性]（HC-5）** FIX-A 冊面 checkpoint 已補記 R-2 fallthrough 更正（`registry_set_checkpoint`，`fix_c_erratum_correction` 欄，2026-09-09）
- [x] `assembleDebug` PASS —— 保留強制（`bugfix`）；scope 全在 `.claude/`，預期 no-op；失敗且與 ALLOWED 無因果 → 記為環境事實，不得歸因本票（PASS，18s，如預期 no-op）
- [x] `git diff --stat` 僅涉及 ALLOWED 檔案（`7e87014` 恰 5 個 ALLOWED 檔；`0adf022`/`8b41cb5` 僅 `.claude/PROGRESS.md`／`PROGRESS_ARCHIVE.md`，Pipeline Artifact）
