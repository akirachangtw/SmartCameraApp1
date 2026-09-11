# INFRA-PREFLIGHT-CORE-01: preflight 決策核心抽取——Parallel-QA enablement（Bash facts collector／adapter ＋ 純 Python decision core ＋ allowlisted decision-table tests）

**簽發日期**: 2026-09-06（Rev A）｜ **Rev B**: 2026-09-06（外審回流＋Akira 放行裁決，SMTR 重寫）｜ **Rev C**: 2026-09-06（Gate PARTIAL 補正裁決）｜ **Rev D**: 2026-09-07（SMTR 複審 F1–F4＋Akira A1／B2 裁決，續作前置修訂）
**簽發者**: Claude (SMTR)
**來源議題**: INFRA
**Mode**: uc  ||  **Effort**: Medium
**優先序**: **P2/P3 — Parallel-QA enablement（MVP delivery accelerator）。第二道 MVP gate 已由 Akira 2026-09-06 明示解除，授權範圍限本票 ALLOWED。** 可排程，不搶占 App 功能票之 P0/P1。
**Traycer Workflow**: 待 Akira 指定（APP 非 RAPID）

## Rev D 修訂摘要

**觸發**：AI-0～AI-5 執行回報後 SMTR 複審（一手取證，非採信執行端自述），發現四項偏離；Akira 2026-09-07 裁決：核准 WIP commit 保存、**禁止收票**、**禁止 AI-6／AI-7 續作**，並就分岔 A 採 **A1（fail-closed）**、分岔 B 採 **B2（四欄 identity 比對）**。

| # | 發現 | 級別 | 一手證據 | Rev D 處置 |
|---|---|---|---|---|
| F1 | `core` 不可用／R-4 不符時 **fail-open 回退 legacy policy**（`preflight_activation.sh` L549–559 字面），使 Bash 與 Python 同時持有 policy authority，與 AI-6「Bash 不再判定」結構性互斥；且 `command -v python3` 失敗即整個 gate 靜默消失，與 R-1／R-2 fail-closed 相反 | T2 | `grep_files` 命中 L549-550 註解與 L553 條件（`aud_20260907_a15d3ab248`） | 新增 **R-7（core availability fail-closed）**；AI-5 判定為**部分達成**，新增 AI-5.1 回修 |
| F2 | R-4 僅實作 `ticket_id` 比對；`CORE_CONTEXT_ID` 於 L470 賦值後**全檔無任何比對使用** | T2 | 同上 grep：`CORE_CONTEXT_ID` 僅一處出現 | R-4 依 B2 改為四欄比對（`ticket_id`／`repo_root`／`worktree_path`／`head_sha`），**不要求 Bash 重算 Python normalized JSON hash**；`context_id` 降為診斷／evidence 欄 |
| F3 | AI-3 以 `ExitCodeCompatibilityTest` 通用斷言替代票面字面之逐案具名 exit-code 斷言，並以「效果等價」結案 | T1 | 票面 AI-3 已知限制欄自陳 | 新增 **AI-3.1**：補四個具名 exit-code compatibility test |
| F4 | **票面自身缺陷**：F-2 收斂列含 `MISMATCH`，R-1 硬規則卻僅列三值，執行端跟 R-1 實作而漏 `MISMATCH`（後由 shadow probe 真實觸發才發現） | 票面 | F-2 與 R-1 字面對照 | R-1 補入 `MISMATCH`，並依**修訂波及面全檔掃描律**同步修正 AI-3／AI-4／AC 之所有相關字面 |

**AI-6／AI-7 派工三前置（全部成立方得派工，缺一即禁）**：① Rev D 生效（本節在盤）→ **✅ 已成立**；② WIP commit 與 baseline refresh 有紀錄（AI-W 完成）→ **✅ 已成立**（commit `ded5dd2` 經 SMTR `git_log` 一手驗證訊息逐字相符，`aud_20260907_fdbfd101d4`；refresh `07640f6b`→`0f723e69`；`BASE_COMMIT` 未動）；③ MVP enablement gate 由 owner 明確放行 → **✅ 已成立（Akira 2026-09-07 明示：允許 MVP 放行，AI-6／AI-7 解鎖）**。**三項全數成立**；惟另有 Rev D.2 新增之 V1 前置取證（見下），未完成前 AI-3.1 起之續作 MUST NOT 開工。

**Rev D.1 補正（2026-09-07，Akira 四項核准後）**：
- **R-7 `process_exit_code`＝1**：由 SMTR 推定升為 Akira 明示裁決。
- **冊面 lifecycle 維持 `EXECUTED`**（Akira 明示）。此為就 AI-0～AI-5 已執行之事實記載，**不表示 AC 通過或可收票**；禁收票之約束由本票面承載。
- **⚠️ 冊面敘事落點缺口（耐久揭露）**：`registry_set_checkpoint` 之 `detail_content` 於 APP 冊實測為**假成功**——回 `SUCCESS`、revision 220→221，但 `checkpoint` 物件無 `detail_ref`，schema 宣告落點 `reports/checkpoints/<TICKET_ID>.md` 全 repo 零命中（`aud_20260907_1e0e53b6ac` 寫、`9d236adfa6` read-back、`a54e6e5944` 檔案掃描）。**故本票之複審敘事以本票面為唯一 SSOT，冊面僅存 Rev B `resolution`。** 續作者 MUST NOT 依冊面 checkpoint 推定本票現況。PLT 缺口候選：`detail_content` 應 fail-loud。
  **〔2026-09-09 INFRA-CKPT-SEMANTICS-01 更正〕**：上段「假成功」判定錯誤。`detail_content` 實際落點為 **PLT repo**（非 APP repo）之 `.tmp/checkpoints/{ticket_id}.md`（`D:/DevSources/workflow/mcp-ticket-platform/.tmp/checkpoints/INFRA-PREFLIGHT-CORE-01.md`，4,535 bytes，`aud_20260909_7839be9eaf`），性質為 transient 工作檔，非缺陷。原判定之根因為依 `TICKET_REGISTRY.json` schema 註解之過期路徑（`reports/checkpoints/`）於 APP repo 搜尋，未讀 PLT `tool-contracts.md` 契約原文（`:646`／`:664`／`:719`）。上段原文保留不刪，僅此加註更正；PLT 缺口候選（「應 fail-loud」）連帶失效，非本票新增之判斷。
- **✅ baseline refresh 排序議題已裁決 → 見 Rev D.2**（原候選乙案「合併腳本異動以減少 refresh 次數」經 Akira 明示**否決**）。

---

## Rev D.2 補正（2026-09-07，Akira 甲案裁決＋AI-W 回流覆核）

### D.2-1 ticket-local, target-sensitive baseline refresh protocol（甲案，Akira 裁決字面承載）

適用範圍：**僅** `INFRA-PREFLIGHT-CORE-01` 本票，**僅** `TARGET_BLOB` 所指之 `preflight_activation.sh`。

**規則**：
1. 本票**每次以完整工作單元 commit 改動 `preflight_activation.sh` 後**，**必須且僅能**進行**一次**人工核准、可追溯的 `TARGET_BLOB` refresh。
2. `BASE_COMMIT=9895b3f` **永不變動**。
3. 每次 refresh MUST 逐字記載：refresh 前 sha8／後 sha8／對應 commit hash——形成可重放 evidence 鏈。
4. 一 commit 一 refresh；**禁**同一 commit 內多次 refresh，**禁**無 commit 而 refresh。

**性質界定（禁擴張解釋，四項禁止用途逐字）**：refresh 僅為**同步本票已核准 commit 後之 validation input**，防止本票合法改動自身 target 造成**機械性自我 MISMATCH**。它 **MUST NOT** 被解為：① generic bypass；② 自動化寫回；③ authority 恢復；④ 用於覆蓋真實 scope、identity、core runtime 或 blob mismatch。

**執行節點**：AI-5.1 與 AI-6 **各自維持獨立 commit、獨立 refresh、獨立重跑 preflight 三段檢核節點**；**禁為減少 refresh 次數而合併成單一大範圍腳本改動**。AI-7 **原則上不得改動 target script**；如確有必要，**停止回報並重新裁決**，不得自行 refresh。

### D.2-2 ⚠️ V1 自我歸因阻斷風險（SMTR 覆核新增，AI-3.1 開工前置）

AI-W 動作 3 實跑字面為 `V1_STATUS=ALREADY_EXECUTED_PENDING_CLOSURE`（`V1_REASON=區間內存在攜本票 ID 前綴之 commit`）。執行端據此判「未命中 `BLOCKED_SCOPE_OR_TARGET_MISMATCH`」——**blob 維度結論正確，但僅覆蓋一個維度**。

**風險**：自 `ded5dd2` 起，`9895b3f..HEAD` 區間恆含攜本票 ID 前綴之 commit → V1 恆為 `ALREADY_EXECUTED_PENDING_CLOSURE`，且每次續作 commit 只會加強。依 **Model A**（`write_authority=YES` 僅限 `V0 PASS ∧ V1 MATCHED ∧ V2 MISSING`），`ALREADY_EXECUTED_PENDING_CLOSURE ≠ MATCHED` → `write_authority` **恆 `NO`**；而 AI-5 已使 `may_activate_ticket` 接管 P3 寫入。**續作可能被本票自身 activation gate 擋死。此維度為 V1 provenance，baseline refresh 不能治。**

**取證缺口**：AI-W 收尾**未記載** `WRITE_AUTHORITY` 與 `CORE_MAY_ACTIVATE_TICKET` 之實測值（非造假，屬遺漏）。

**⛔ AI-3.1 開工前置（唯讀，零成本）**：重跑 `preflight_activation.sh INFRA-PREFLIGHT-CORE-01`，逐字記錄 `WRITE_AUTHORITY`、`CORE_MAY_ACTIVATE_TICKET`、`V1_STATUS`、`blob_check` 四值，並實測 `active.json` mtime 是否更新。
- 若 `may_activate_ticket=true` → 續作無阻，逕行 AI-3.1
- 若 `may_activate_ticket=false` → **停止回報升裁**，MUST NOT 自行 workaround。二案待 Akira 裁決：**(A)** 本票續作明示採人工 activation 並票面記載例外（Model A 已明訂「人工續作不構成工具背書」，性質相容；SMTR 傾向此案）；**(B)** 決策表補一列使 self-ticket `ALREADY_EXECUTED_PENDING_CLOSURE` 得 `may_activate_ticket=true` 而 `write_authority=NO`（SMTR 不建議——在 gate 上開語義例外，正是本票要根除之形狀）

### D.2-3 AI-W 覆核判定：**PASS**

SMTR 一手取證：`git_log` 確認 `ded5dd2` 存在且訊息逐字相符、無禁用字樣（`aud_20260907_fdbfd101d4`）；票面 `BASE_COMMIT` 仍為 `9895b3f`、`TARGET_BLOB` 已回填 `0f723e69` 且僅一次；STOP 已守（AI-3.1／5.1／6／7 全未動）；Dashboard 同步為獨立 commit `745ab25`，僅涉 `PROGRESS.md`／`PROGRESS_ARCHIVE.md`，守 Rule 3 未混入實作（`aud_20260907_5d00ab1ce3`）。AC 勾選誠實度通過抽驗：17 項 9 勾 8 未勾，未達項逐項標機械性原因（R-1 僅 1/4 型於 adapter 層驗證、R-4 單欄未達四欄、R-7 未實作等），無推定勾選、無摘要語氣代替證據。唯一扣分為 D.2-2 之取證缺口，已轉為 AI-3.1 前置補齊，不構成 AI-W 退回。

---

## Rev D.3 補正（2026-09-08，Extreview2 回流＋Akira 裁決）

**來源**：`D:/DevSources/Skill-Advisor/deliver/QA-Session-Aware/QA-Session-Aware-Extreview2.md`（`aud_20260908_c465a27bf4`）。該件為純邏輯論證、無外部事實主張，「回件關鍵事實須附來源 URL」之驗收不適用。外審淨貢獻二項（D.3-1、D.3-2），其餘為對現況之獨立覆述（交叉確認價值，非新資訊）。外審提議之三層 status enum 與 SV 四分類 **不採納**（與 R-2 禁新增 enum 精神相斥，且與 Codex 既有 verdict 介面重疊）。

### D.3-1 V2_STATE 取證格式（AI-3.1-b 收尾必填，六欄分號分隔）

```text
V2_STATE=<實測值>; CORE_MAY_ACTIVATE_TICKET=<true|false>; active_json_mtime_before=<實測值>; active_json_mtime_after=<實測值>; write_observation=<WRITE|NO_WRITE>; attribution=<見下四值>
```

- `mtime_before`／`mtime_after` **MUST 分立**，禁合併——前後值分開方保有可比較性，供解析或人工抽驗直接判斷寫入觀測。
- `CORE_MAY_ACTIVATE_TICKET=true` 僅表示**得**呼叫 P3 writer，**不構成已寫入之證明**（資料模型已定義 `v2_probe_may_write=false`，「可 activate」與「已寫入」為兩件事）。
- **`attribution` 封閉為四值（SMTR 收緊，禁自由文字）**：`V2_ZERO_WRITE_CONTRACT` ／ `SAME_TICKET_IDEMPOTENT` ／ `ADAPTER_PATH_NOT_INVOKED` ／ `unresolved`。
  - 任一非 `unresolved` 值 **MUST** 附產生它的證據位置（輸出行、命令輸出或 `file:line`）；未附即**視同 `unresolved`**。
  - 僅觀測到 `active.json` mtime 不變時，`attribution` **MUST** 填 `unresolved`；**MUST NOT** 自行推定為同票 idempotency、V2 零寫入契約或 adapter 未呼叫 writer。
  - 若需新增 trace、修改 adapter 或 target script 方能歸因 → 屬 AI-5.1／AI-6 範圍，AI-3.1-b **MUST NOT** 擴 scope。

### D.3-2 中間 SV 去重原則

已落票之既知未達項，除出現**新事實、規格矛盾或安全邊界改變**外，**MUST NOT** 於中間 SV 重複升裁。中間 SV 如再次觀測到既知未達，**僅得連結既有票面位置與既定後續 AI**，不得新建平行 verdict 分類、不得重設派工順序。（不新增分類詞彙，不覆蓋 Codex 既有 verdict 介面。）

### D.3-3 AI-7 文件忠實性（避免假閉環）

`SKILL.md` 中每一條以 **MUST／保證／契約／runtime 行為**表述之內容，**MUST 可追溯**對應至本票**已勾選之 AC**，或**在盤實測證據**。尚未完成、僅規劃中或待後續 AI 驗證者，**MUST 明示標記 `pending`／`planned`**，不得寫成已生效之執行保證。

- **「在盤實測證據」定義（SMTR 收緊）**：限票面 Action Item 中**附 `audit_id`、命令輸出或 `file:line`** 之實測記載。執行端敘事不算——「動作 3 已驗證 blob 未阻斷」是敘事；「`blob_check=MATCH`（HEAD=`ded5dd2`）」是證據。AI-7 僅得回鏈後者。
- AI-7 **MUST 於 AI-5.1 與 AI-6 完成並產出其所需證據後**方得派工。本條為驗收約束，**不新增任何 `final_status` 或 lifecycle enum**。

### D.3-4 Extreview 4a 存廢裁決（2026-09-08，Owner：Akira）

- **觸發確認**：本票 Rev A→B→C→D，修訂版次計數 **4**，滿足外部查證協議條件 4a。
- **外審回流**：Extreview2 已完成；結論支持保留既定修復序列，非退役或降級。
- **裁決：續修**（Akira 2026-09-08 明示）。
- **授權範圍**：僅既開 **AI-3.1-b → AI-5.1 → AI-6 → AI-7**，依既定順序執行。
- **凍結邊界**：本票**票級 closure 前**，凍結此子系統任何**新修補票**之開立；不得藉續修新增 workflow engine、資料庫、遠端服務、新權限平台、跨 repo orchestration，或新的 status／verdict 分類。
- **凍結出口（SMTR 收緊，防死結）**：凍結期間若出現**新事實／規格矛盾／安全邊界改變**（與 D.3-2 三豁免同源，不另立判準）且**無法納入既開 AI 範圍**者 → **停止回報升裁**，僅得由 Akira 明示解除凍結後開票。執行端 MUST NOT 自行開票，亦 MUST NOT 為避免開票而將 T2 塞入既開 AI。
- **不溯及（SMTR 收緊）**：凍結自本裁決在盤起算，**不溯及 Rev D 已裁內容**。R-7 之內部 `reason_code` 三值（`CORE_UNAVAILABLE`／`CORE_PROTOCOL_INVALID`／`IDENTITY_MISMATCH`）係 Rev D 已裁，AI-5.1 實作之**不構成違反凍結**，不得據此 STOP。
- **派工界線**：AI-3.1-b 屬既開 AI 之單檔補件，不受凍結；**AI-5.1 於本裁決在盤後方得派工**（現已在盤）。
- **閉環界線**：AI-7 完成前**不得**進行票級 `/qa → /sv`；AI-7 完成後**僅執行一次**票級 cross-review。

**Baseline refresh 裁決**：本票已改寫 `TARGET_BLOB` 所指之 `preflight_activation.sh` 自身，續作時 preflight 對本票恆 `MISMATCH → blocked`（自我阻斷）。處置為 **WIP commit 後依 ticket-local baseline refresh 規則更新 `TARGET_BLOB` 一次**，形成可重放 evidence；`BASE_COMMIT` 維持 frozen `9895b3f` 不動。**禁靜默豁免、禁執行端臨場自解**。

## Rev C 修訂摘要

**Gate PARTIAL 補正裁決（2026-09-06）**：採納 M1／M2，不拆票。Rev B 缺少 V0／V1／V2 到單一 `final_status` 的完整收斂函式，且把 legacy exit code 誤寫成「0 放行／1 blocked」；現行 frozen pre-image 實際僅 V0 failure exit 1，V0 PASS 後即使 V1／V2 形成 blocked／candidate 語義仍 exit 0。Rev C 將 `process_exit_code` 納入 Decision，明定其只由 V0 決定，禁止 adapter 由 `final_status` 反推。另將無 producer 的 `v2_provenance=KNOWN`、`v2_semantics=FAILED` 移出 Facts Contract；early-return 未執行之下游探針使用具明確控制流 producer 的 `NOT_EVALUATED`，其他未知值一律走 R-2，不得為補齊 decision table 而虛構 producer。

## Rev B 修訂摘要

**定位翻盤（Akira 2026-09-06 明示）**：本票由「受 MVP gate 門控之治理工具重構（P4）」改定位為 **MVP 加速基礎建設**——直接解除單一 repo 內切票／resume 的舊狀態污染，並讓 scope 不重疊的 ticket 可各自依 durable config、Git facts 與 pure decision 獨立執行 QA。此裁決推翻同日稍早之乙案（gate 關閉）與 vault 決策檔 `Preflight_Activation_Decision_Core_Convergence` KD-8（雙門門控）之第二道門，屬**條件觸發型翻盤**（KD-8 翻盤條件「Akira 明示調整優先序」成立）。

**外審回流**（附件 `修正後放行` 審查，13 項）：採納 12 項、降級 1 項（Bash grep AC 改責任驗收，但 Python 核心純度 grep 保留）、禁抄 1 處（可行性百分比無 producer）。三項 P0 中兩項為 SMTR Rev A 開票錯誤，如實記載：

| # | Rev A 缺陷 | 性質 | Rev B 修正 |
|---|---|---|---|
| P0-2 | `coordination_dependency` 仍寫「activation 票未收束前 MUST NOT 開工」 | SMTR Rev A.1 清冊面 `depends_on` 時未同步清此行，票面與冊面脫節 | 改為 frozen commit `9895b3f` 作 immutable pre-image；禁等待其 CLOSED |
| P0-3 | `BLOCKED_UNLISTED_COMBINATION` 寫為 `final_status` | 與 Akira 裁決「不得新增對外 status enum」自相矛盾 | `UNLISTED_COMBINATION` 僅為內部 `reason_code`，對外映射既有三 blocked enum |
| P1 | `v1_scope` 混入 `BLOB_MISMATCH` | 兩維度混同，`UNCHANGED_TARGETS` 易被誤讀為 V1 已 MATCHED | 拆 `v1_scope` ＋ `blob_check`；後者承載 R3 之 `COMMAND_FAILED` |

**前置依賴（依賴三分）**：
- `execution_dependency`: **無**。第一道門已由 R3 FAIL 滿足；第二道門已由 Akira 2026-09-06 明示解除
- `evidence_dependency`: ①`INFRA-QA-ACTIVATION-01`（seq 24，`BLOCKED`）之 R3 checkpoint（`r3_outcome=FAIL_REPORT_FIX_3RD_CONSECUTIVE`，CROSS_REVIEW_RESULT 存 `.tmp/cross_review_result_20260906_142905.md`）；②**frozen commit `9895b3f`**（`[INFRA-QA-ACTIVATION-01] R2 REPORT_FIX`，2026-09-06 14:28，腳本最後一筆 commit，R3 於其上執行；`aud_20260906_862a9c1ea6`）為本票 **immutable pre-image**；③外審回流 `Skill-Advisor/deliver/QA-Session-Aware/QA-Session-Aware-Extreview.md`（`aud_20260906_23ebc60651`）；④本輪「修正後放行」審查附件；⑤`INFRA-QA-PERTICKET-CONF-01`（seq 23，CLOSED）交付之 `tmp/verify/<TICKET_ID>.rev-N.conf` ＋ `active.json`（2026-09-05 實測在盤，`aud_20260905_26607f8cf8`）為 per-ticket durable config 之既有落點
- `coordination_dependency`: **`INFRA-QA-ACTIVATION-01` 不是 execution dependency，不得要求其 CLOSED**——該票依裁決永久凍結。本票 AI-0 驗證 checkpoint、frozen commit 與本票 context，**MUST NOT** 等待被凍結票「收束」。同改 `.claude/skills/qa/SKILL.md` 之他票現無在途者；派工後若出現，依佔用宣告串行

## 檔案治理／來源說明

**上位裁決（Akira 2026-09-06，續修案）**：`preflight_activation.sh` 不退役、不降級；R3 FAIL 禁 R4 局部修補，改採「Bash facts collector／side-effect adapter ＋ 純 Python decision core ＋ allowlisted decision-table tests」最小重構。核心不得執行 Git、讀寫檔案、讀環境變數；副作用僅存於 adapter。

**Write Authority 採 Model A（固化）**：人工 reauthorization 與 implementation authorization 不建 machine-readable producer；`YES` 僅限 `V0=PASS ∧ V1=MATCHED ∧ V2=MISSING`；`V2=PARTIAL` 與任何人工例外恆保守 `NO`；人工續作不構成工具背書。

**R3 finding 之正確定性（本票根治對象）**：`git hash-object` 失敗使 `actual_sha` 為空、`[[ -n ]]` guard 跳過——**不是**一個缺失的 guard，而是 facts contract 缺一個維度：現行設計只有「blob 相符／不符」二值，command failure、target missing、ticket 值非法三種取值失敗無處安放，只能被吞成「跳過」。Rev B 以 `blob_check` 七值枚舉承載之，並以硬規則使任何取值失敗必 fail-closed。此與 R2「`TARGET_BLOB` 讀入即棄」同形狀（取值失敗未轉為拒絕判定），故 KD-2 豁免不適用、R4 禁止維持——本票即為 R4 之替代路徑。

**一手取證校準**：`preflight_activation.sh` ≥264 行含四域交叉分支（Google Shell Style Guide 100 行門檻成立）；Python 於 APP 工具鏈已存在（`.venv`、兩支標準庫 hook）；現行對外 status 字面（`aud_20260906_4ef24e1ae3`）：V0 `BLOCKED_ENV_MISMATCH`；V1 `BASELINE_MATCHED`／`ALREADY_EXECUTED_PENDING_CLOSURE`／`CANDIDATE_SUPERSEDED`／`BLOCKED_UNATTRIBUTED_STATE`／`BLOCKED_SCOPE_OR_TARGET_MISMATCH`。**Legacy exit code 的唯一 producer 是 V0**：V0 failure → exit 1；V0 PASS → V1／V2 執行完成後一律 exit 0，包含 V1 blocked 與 V2 route 產生 `CANDIDATE_SUPERSEDED` 的案例。**票面原列之 `NEEDS_REBASE`／`ANCHOR_STALE_SCOPE_INTACT` 於腳本中無實作字面**——本票 MUST 沿用腳本現有字面，不得因票面曾列而補實作（那是另一議題）。

## ⚠️ 已知限制（耐久揭露，維持）

> **MUST NOT** 據 `INFRA-QA-ACTIVATION-01` 所交付之 `preflight_activation.sh`（frozen `9895b3f`）之 `V1=MATCHED` 推定 target blob 一致。當 `git hash-object` 失敗時，`actual_sha` 為空並由 guard 跳過，現有比對可能未形成拒絕判定。此缺陷之處置為本票 AI-1／AI-2 之 `blob_check` 硬規則，非局部 guard 修補。

## Verify Config
TICKET_ID=INFRA-PREFLIGHT-CORE-01
CREATED=2026-09-06
PRESET=refactor
ALLOWED=.claude/skills/qa/scripts/preflight_activation.sh,.claude/skills/qa/scripts/preflight_core.py,.claude/skills/qa/scripts/preflight_rules.json,.claude/skills/qa/tests/test_preflight_core.py,.claude/skills/qa/tests/test_preflight_adapter.sh,.claude/skills/qa/SKILL.md
FORBIDDEN=.claude/skills/qa/scripts/verify_checklist.sh,.claude/skills/qa/scripts/structural_verify.sh,.claude/skills/sv/**,.claude/skills/compact/**,.claude/hooks/**,.claude/rules/**,.claude/settings.json,.claude/CLAUDE.md,.claude/PROGRESS.md（僅經既有 writer 間接寫入）,tmp/verify/active.json（僅經既有 P3 writer 間接寫入）,plans/cso_briefing/project/TICKET_REGISTRY.json,app/**,tests/**
MAX_LINES=N/A（架構抽取，diff 為設計之被動結果；TPL v3.5 R3）
DIFF_REF=9895b3f
BASE_COMMIT=9895b3f
TARGET_BLOB=.claude/skills/qa/scripts/preflight_activation.sh:47a4d120
ANCHOR_PROBED=2026-09-06（Rev B；frozen commit 與對外 enum 字面已一手取證。派工時 MUST 重跑 AI-0 並更新本欄時戳）

### AI-W: WIP 保存＋ticket-local baseline refresh（2026-09-07，Akira 派工，Effort Low）

**動作 1（WIP commit）**：commit `ded5dd2`（`[INFRA-PREFLIGHT-CORE-01] WIP AI-0~AI-5（AI-3/AI-5 部分達成，未收票）`），4 檔（`preflight_activation.sh`／`preflight_core.py`／`preflight_rules.json`／`tests/test_preflight_core.py`）。**不構成 AI-5 完成、AC 通過或 ticket 收束**。

**動作 2（baseline refresh，僅一次，可重放 evidence）**：
- refresh 前 sha8：`07640f6b`（frozen pre-image `9895b3f` 之值，AI-0 開工當下取得）
- refresh 後 sha8：`0f723e69`（`git hash-object` 於 commit `ded5dd2` 之後對 `.claude/skills/qa/scripts/preflight_activation.sh` 現行內容量測）
- 對應 commit hash：`ded5dd2`
- `TARGET_BLOB` 已回填為 `.claude/skills/qa/scripts/preflight_activation.sh:0f723e69`；`BASE_COMMIT` 維持 `9895b3f` 不動（frozen pre-image，未異動）

**動作 3（驗證不自我阻斷）**：refresh 後重跑 `preflight_activation.sh INFRA-PREFLIGHT-CORE-01`（HEAD=`ded5dd2`）：
- 直接呼叫 `collect_blob_check_fact()` 對本票票檔量測 → `blob_check=MATCH`（實測輸出，非設計推論）
- 完整實跑輸出：`V1_STATUS=ALREADY_EXECUTED_PENDING_CLOSURE`（`V1_REASON=區間內存在攜本票 ID 前綴之 commit`，因 WIP commit `ded5dd2` 本身帶 `[INFRA-PREFLIGHT-CORE-01]` 前綴，R1 diff 非空後 R2 歸因命中 self-ticket）——**未** 命中 `BLOCKED_SCOPE_OR_TARGET_MISMATCH`，確認 blob 比對未自我阻斷
- Shadow log 同步驗證：`legacy: final_status=ALREADY_EXECUTED_PENDING_CLOSURE exit=0` 與 `core: final_status=ALREADY_EXECUTED_PENDING_CLOSURE exit=0` 一致，`marker=MATCH`（`.tmp/preflight_shadow_20260907.log`）
- [x] 完成

## Facts Contract（本票之資料模型 SSOT，AI-1／AI-2／AI-4 皆依此）

**兩層 facts**（外審方案 2 採納）：

```
ContextFacts                       # identity binding，並行安全之基礎
- ticket_id
- repo_root
- worktree_path
- head_sha
- config_revision                  # 自 tmp/verify/<TICKET_ID>.rev-N.conf 之 N
- collected_at
- activation_intent                ∈ {EXPLICIT_SWITCH, IMPLICIT_RESUME}

EnvironmentFacts
- v0_env                           ∈ {PASS, DETACHED_HEAD, NO_ACTIVE_TICKET, OUT_OF_SCOPE}
- baseline_check                   ∈ {VALID, MISSING, UNRESOLVABLE, NOT_EVALUATED}
- v1_scope                         ∈ {UNCHANGED_TARGETS, CHANGED_TARGETS, NOT_EVALUATED}
- v1_provenance                    ∈ {SELF_TICKET, OTHER_TICKET, UNATTRIBUTED, NOT_APPLICABLE}
- blob_check                       ∈ {MATCH, MISMATCH, SKIPPED_NEW, SKIPPED_NA, TARGET_MISSING, COMMAND_FAILED, INVALID_TICKET_VALUE, NOT_EVALUATED}
- v2_semantics                     ∈ {SATISFIED, PARTIAL, MISSING, NOT_EVALUATED}
- v2_provenance                    ∈ {UNKNOWN, NOT_EVALUATED}
- manual_authorization             固定 NOT_MACHINE_READABLE（Model A）
```

**Decision**（核心唯一輸出）：

```
- context_id                       # ContextFacts 之 digest；Rev D B2：降為診斷／evidence 欄，adapter 不重算
- ticket_id                        # 原樣回傳
- final_status                     ∈ 既有對外 enum（見上方一手取證清單），禁新增
- reason_code                      # 每 rule row 唯一、穩定、可讀；同一 final_status 可對應多個 reason_code
- process_exit_code                ∈ {0, 1}；只由 v0_env 決定，禁由 final_status 反推
- write_authority                  ∈ {YES, NO}
- may_activate_ticket              # 是否得呼叫 P3 writer 更新 active.json（activation 決策）
- v2_probe_may_write               固定 false（V2 零寫入為契約）
- may_run_tests_with_authority
- evidence_required
```

**`final_status` 收斂函式（有序、首項命中即停止）**：

| 優先序 | Facts 條件 | `final_status` | producer 路徑 |
|---|---|---|---|
| F-0 | `v0_env != PASS`，且 V1／V2 facts 為下述 `NOT_EVALUATED` 哨兵組合 | `BLOCKED_ENV_MISMATCH` | V0 early return |
| F-1 | `baseline_check ∈ {MISSING, UNRESOLVABLE}`，且 `v1_scope=NOT_EVALUATED ∧ v1_provenance=NOT_APPLICABLE ∧ blob_check=NOT_EVALUATED` | `BLOCKED_UNATTRIBUTED_STATE` | V1 BASE_COMMIT 缺失／不可解析 |
| F-2 | `baseline_check=VALID ∧ blob_check ∈ {MISMATCH, TARGET_MISSING, COMMAND_FAILED, INVALID_TICKET_VALUE} ∧ v1_scope=NOT_EVALUATED ∧ v1_provenance=NOT_APPLICABLE` | `BLOCKED_SCOPE_OR_TARGET_MISMATCH` | V1 blob |
| F-3 | `baseline_check=VALID ∧ v1_scope=CHANGED_TARGETS ∧ v1_provenance=SELF_TICKET` | `ALREADY_EXECUTED_PENDING_CLOSURE` | V1 self-ticket attribution |
| F-4 | `baseline_check=VALID ∧ v1_scope=CHANGED_TARGETS ∧ v1_provenance=OTHER_TICKET` | `CANDIDATE_SUPERSEDED` | V1 other-ticket attribution |
| F-5 | `baseline_check=VALID ∧ v1_scope=CHANGED_TARGETS ∧ v1_provenance=UNATTRIBUTED` | `BLOCKED_UNATTRIBUTED_STATE` | V1 unattributed commits |
| F-6 | `baseline_check=VALID ∧ v1_scope=UNCHANGED_TARGETS ∧ blob_check ∈ {MATCH, SKIPPED_NEW, SKIPPED_NA} ∧ v2_semantics=SATISFIED` | `CANDIDATE_SUPERSEDED` | V2 `route_v2_state()` 等價路徑 |
| F-7 | `baseline_check=VALID ∧ v1_scope=UNCHANGED_TARGETS ∧ blob_check ∈ {MATCH, SKIPPED_NEW, SKIPPED_NA} ∧ v2_semantics ∈ {PARTIAL, MISSING}` | `BASELINE_MATCHED` | V1 baseline；V2 細節留在 `reason_code`／evidence |
| F-8 | 其餘任何組合或 schema 外值 | `BLOCKED_ENV_MISMATCH` | R-2 unlisted |

F-3～F-5 優先於 F-6～F-7：V2 不得覆蓋 `ALREADY_EXECUTED_PENDING_CLOSURE` 或 V1 歸因型 blocked。`CANDIDATE_SUPERSEDED` 有兩個合法且必須各自測試的 producer：F-4 與 F-6；`BLOCKED_UNATTRIBUTED_STATE` 亦有 F-1（baseline 不可用）與 F-5（commit 無歸因）兩個 producer。F-0 的哨兵組合固定為 `baseline_check=NOT_EVALUATED`、`v1_scope=NOT_EVALUATED`、`v1_provenance=NOT_APPLICABLE`、`blob_check=NOT_EVALUATED`、`v2_semantics=NOT_EVALUATED`、`v2_provenance=NOT_EVALUATED`。F-1 的 V2 probe 仍依 frozen pre-image 執行，故其 `v2_semantics` 為三個可達值之一且 `v2_provenance=UNKNOWN`。任何不符合上述控制流的 `NOT_EVALUATED` 組合落 F-8。

**硬規則**：
- **R-1（R3 根治；Rev D F4 補入 `MISMATCH`）**：`blob_check` ∈ {`MISMATCH`, `TARGET_MISSING`, `COMMAND_FAILED`, `INVALID_TICKET_VALUE`} → 核心 MUST 產生 blocked decision（`final_status=BLOCKED_SCOPE_OR_TARGET_MISMATCH`）、`write_authority=NO`、`may_activate_ticket=false`；**MUST NOT** 以空字串、缺欄位、跳過 guard 或沿用上次值推定 match
- **R-2（fail-closed 不增 enum）**：未命中 allowlisted rule row → `final_status=BLOCKED_ENV_MISMATCH`、`reason_code=UNLISTED_COMBINATION`、全部 may_* 為 false。**`UNLISTED_COMBINATION` 僅為內部 reason code，MUST NOT 作為對外 status**
- **R-3（Model A）**：`write_authority=YES` 之列 MUST 同時滿足 `v0_env=PASS ∧ baseline_check=VALID ∧ v1_scope=UNCHANGED_TARGETS ∧ blob_check ∈ {MATCH, SKIPPED_NEW, SKIPPED_NA} ∧ v2_semantics=MISSING`；`v2_semantics=PARTIAL` 之列恆 `NO` 且 `reason_code` 含 `CONSERVATIVE_MANUAL_NOT_MACHINE_READABLE`
- **R-4（identity binding；Rev D B2 改寫）**：adapter 於任何副作用前 MUST 逐欄比對**四項** identity——`ticket_id`、`repo_root`、`worktree_path`、`head_sha`——core Decision 回傳值與 adapter 當下實測值全等方得續行。**MUST NOT** 要求 Bash 重算 Python 之 normalized JSON hash（`context_id` 由核心單向產出，僅作診斷／evidence 綁定，不作 adapter 判準）。任一欄不符 → 依 R-7 fail-closed，拒絕寫入並輸出 blocked
- **R-5（legacy exit code 不變）**：`process_exit_code=1` iff `v0_env != PASS`；`v0_env=PASS` 時固定 0，即使 `final_status` 為 `BLOCKED_*`／`CANDIDATE_SUPERSEDED` 或 `write_authority=NO`。adapter **MUST** 原樣採用此欄，禁止依 status 前綴、authority 或 may_* 重新推導
- **R-6（producer closure）**：V2 實際執行時 `v2_provenance` 唯一合法值為 `UNKNOWN`、`v2_semantics` 僅三值；V0 early return 時兩欄才得為 `NOT_EVALUATED`。`KNOWN`／`FAILED` 或任何 schema 外值 **MUST NOT** 出現在 allowlisted rows，輸入核心時一律落 F-8／R-2

- **R-7（core availability fail-closed；Rev D A1 裁決，新增）**：Python core runtime 不可用（`python3` 缺失、`preflight_core.py` 不在盤）、core 輸出 protocol 無效（必要欄位缺漏／解析失敗）、或 R-4 四欄 identity 不一致時，adapter **MUST** fail-closed：
  - 對外 `final_status=BLOCKED_ENV_MISMATCH`、`reason_code ∈ {CORE_UNAVAILABLE, CORE_PROTOCOL_INVALID, IDENTITY_MISMATCH}`（僅內部 reason code，不新增對外 enum）
  - **MUST NOT** 回退 legacy policy 判定路徑（`write_authority()`／V0 PASS 即寫 等分支不得作為決策來源）
  - **MUST NOT** 呼叫 P3 writer；**MUST NOT** 帶 ticket authority 執行 QA；**MUST NOT** 宣稱任何 AC 通過
  - 得輸出唯讀診斷（stderr 顯著警示＋既有唯讀 V0／V1／V2 觀測值），但該輸出 **MUST** 明確標記為 `ADVISORY_ONLY`，不得被任何下游當作放行依據
  - `process_exit_code` 固定 **1**。此為 R-5 之**明示例外**且不與之衝突：R-5 規範「由 Decision 映射 exit code」，core 缺席時無 Decision 可映射。相容性風險已由 AI-0 動作 C 一手取證消解（`preflight_activation` 之機器可讀消費者數＝0）。**〔Akira 2026-09-07 明示確認：維持 1。本項不再為推定，為裁決。〕**

## Action Items

### AI-0: 活體盤點（派工後、修改前）
- **動作 A（pre-image）**：`git show 9895b3f --stat` 確認 frozen commit 在盤；`git hash-object` 記錄該 commit 下 `preflight_activation.sh` 之 sha8 回填 `TARGET_BLOB`；`wc -l` 記錄行數。**若 HEAD 上該檔已異於 9895b3f** → 停止回報升裁（表示有人於凍結後改動，違 R4 禁令）
- **動作 B（P3 落點盤點）**：實測 `tmp/verify/` 現況——`active.json` 欄位全集、`<TICKET_ID>.rev-N.conf` 命名規則與 `N` 之 producer、`write_active_json` 函式位置與簽章。**本票 `config_revision` 之 producer 即由此確定**，禁自行定義
- **動作 C（消費端清點）**：`git grep -l "preflight_activation" -- .claude/ plans/ docs/`；逐一記錄其依賴之 stdout 欄位名、exit code 語義、machine-readable key。此清單為 AI-4 相容性之**唯一判準**
- **動作 D（既有 self-test 分類）**：逐一標記為「決策語義測試」或「adapter／I/O integration 測試」。**前者 MUST 於 AI-2 映射至 rule row；後者保留獨立清單，不強行映射**
- **動作 E（Python 可用性）**：`python --version` ≥3.8；`json`／`enum`／`dataclasses`／`hashlib`／`unittest` 可 import；**禁 pip install**
- **[標配]** `git check-ignore -v` 對 ALLOWED 六檔 → 命中即停止回報
- [x] 完成：A frozen commit `9895b3f` 在盤，HEAD 上該檔 hash 於開工當下與 `9895b3f` 相符（`07640f6b`，之後本票自身編輯才使其分歧，屬預期）；行數 376（開工當下量測，非票面聲稱之 ≥264，較高）；B `tmp/verify/` 現況：`active.json` 七欄位、`<TICKET_ID>.rev-N.conf` 命名確認，`N` producer 為 `verify_checklist.sh` 之 `REVISION` conf 變數（預設 `rev-0`）；C `git grep -l "preflight_activation"` 命中 4 檔（`PROGRESS.md`／`session_start.sh`／`qa/SKILL.md`／舊 ticket 檔），逐一核對後**零真實機器可讀消費者**（皆為文字提示或檔案存在性檢查，非 stdout/exit code 解析）；D 既有 8 條 self-test 全數分類為「決策語義測試」，皆可映射至 F-0/F-4/F-6/F-7 rule row；E `python3 --version`=3.14.5，`json/enum/dataclasses/hashlib/unittest` import 成功；check-ignore 對六檔零命中

### AI-1: 純決策核心 `preflight_core.py`（不改 Bash 行為）
- 唯一入口 `decide(facts) -> Decision`；**MUST NOT** `subprocess`／`open(`／`os.environ`／`pathlib` 讀寫；純函式，同輸入恆同輸出
- `context_id` = `sha256(ContextFacts 正規化 JSON)[:16]`，隨 Decision 原樣回傳
- 逐字實作 F-0～F-8 與 R-1～R-6；R-2 之未列組合走 `BLOCKED_ENV_MISMATCH`＋`UNLISTED_COMBINATION`，`process_exit_code` 仍只依 R-5 產生
- **本階段以 fixture facts 先跑 12–14 條關鍵 rows**：R3 failure（`COMMAND_FAILED`）、`TARGET_MISSING`、顯式切票、隱式 resume、`MISMATCH`、`SKIPPED_NEW`、unlisted combination、`PARTIAL` 保守 NO、`MISSING` 得 YES、F-4/F-6 兩條 candidate producer、F-1/F-5 兩條 unattributed producer、V1 blocked 但 exit 0
- [x] 完成：`decide()` 逐字實作 F-0～F-8＋R-1～R-6，`context_id=sha256(...)​[:16]`；純度以 `grep -n "subprocess\|open(\|os.environ"` 命中 0 + 清空 `os.environ`／隨機 cwd／無 `.git` 下 decision 相同驗證；**實測發現並修正一項真實缺陷**：F-2 判定原僅含 `{TARGET_MISSING,COMMAND_FAILED,INVALID_TICKET_VALUE}`（誤漏票面明訂之 `MISMATCH`），由 Bash shadow probe 對本票自身 TARGET_BLOB 真實比對時觸發（core 誤判 `BLOCKED_ENV_MISMATCH`／legacy 正確判 `BLOCKED_SCOPE_OR_TARGET_MISMATCH`），修正後重測 MATCH，已補回歸測試列（見 AI-2）

### AI-2: 決策表 `preflight_rules.json`
- 每列一條允許組合 → 預期 `Decision` 全欄位；immutable，載入後不得執行期修改
- **禁笛卡兒積窮舉**；僅列允許組合，未列即走 R-2
- 每列 MUST 明列 `process_exit_code`；allowlisted rows 僅得使用 Facts Contract 有 producer 的值，`v2_provenance=KNOWN`／`v2_semantics=FAILED` 禁入表
- AI-0 動作 D 標記為「決策語義測試」之既有案例 MUST 全數映射至某列，映射表存收尾；無法映射者 → 停止回報升裁
- [x] 完成：15 rows（含 F-2 MISMATCH 回歸列），涵蓋 F-0×3、F-1×2、F-2×4、F-3/F4/F5×1、F-6×1、F-7×2；既有 8 條 self-test 100% 映射（`route_v2_state` 三案→F-6/F-7；`write_authority` 五案→F-0/F-4-equivalent/F-7×2）；`v2_provenance=KNOWN`／`v2_semantics=FAILED` 未入表（測試另外以程式建構驗證 R-6 fail-closed，非決策表列）

### AI-3: 表格驅動測試 `tests/test_preflight_core.py`
- **標準庫 `unittest` ＋ `subTest`，禁 pytest，禁建根 `tests/`**（與 `INFRA-REGISTRY-LIFECYCLE-GATE-APP-01` HC-6 一致）
- 測試自 `preflight_rules.json` 逐列產生；新增一條規則＝新增一列＋自動多一 subTest
- 負向案：未列組合 → R-2 行為；**四種**取值失敗（`MISMATCH`／`TARGET_MISSING`／`COMMAND_FAILED`／`INVALID_TICKET_VALUE`，Rev D F4 補正）→ R-1 行為；`KNOWN`／`FAILED` → R-6 行為；V0 PASS 搭配各 V1 blocked／F-6 candidate 均斷言 exit 0；`os.environ` 清空、隨機 cwd、無 `.git` 下核心仍得相同 decision
- [~] **部分達成（Rev D 降級，待 AI-3.1；以下為執行端原始回報）**：`python3 -m unittest test_preflight_core -v` 9 test methods 全 PASS（`test_all_rows` 內含 15 subTest）；未落根 `tests/`（落 `.claude/skills/qa/tests/`）；`import pytest` 零命中
- **[已知限制，誠實揭露]**：`test_all_rows` 之全欄位斷言天然涵蓋「V0 PASS 搭配各 V1 blocked/F-6 均 exit 0」，但未逐一寫出獨立具名的 exit-0 專項斷言（改由 `ExitCodeCompatibilityTest` 對全表通用斷言涵蓋，非票面字面之逐案寫法，效果等價）
- **[Rev D 裁決 F3]**：「效果等價」為語義主張，不構成逐字實作之機械證明。AI-3 由 `[x]` 降回**部分達成**，補件見 AI-3.1

### AI-3.1: exit-code 具名斷言補件（Rev D 新增，F3）
- **⛔ 開工前置（Rev D.2-2）**：先執行 V1 前置取證（唯讀），逐字記錄 `WRITE_AUTHORITY`／`CORE_MAY_ACTIVATE_TICKET`／`V1_STATUS`／`blob_check` 四值＋`active.json` mtime 是否更新。`may_activate_ticket=false` → **停止回報升裁**，MUST NOT 自行 workaround
- **本 AI 不觸 `preflight_activation.sh`**，故不觸發 Rev D.2-1 之 refresh 節點；commit 僅涉測試檔
- **[Rev D.3 SMTR 覆核 2026-09-08：PASS，附一項 F4 殘留]** 一手 grep（`aud_20260908_30d7836170`）：13 個 `def test_` 在盤，四個具名方法逐字命中（L210／L228／L245／L262），`subTest` 三值在 L270。Phase 0 五值逐字在盤，`CORE_MAY_ACTIVATE_TICKET=true` → **Rev D.2-2 之 A/B 分岔未觸發，續作無阻**。
  - **F4 殘留（T1，AI-3.1-b，commit 前併入）**：`test_r1_blob_failures_all_three_variants`（L128–L135，`aud_20260908_9aacf98759`）迭代集合仍為三值 `{TARGET_MISSING, COMMAND_FAILED, INVALID_TICKET_VALUE}`，**未含 `MISMATCH`**。`MISMATCH` 雖經 `test_all_rows` 之 rules.json 回歸列覆蓋，但 R-1 硬規則之**專項不變量測試**與 Rev D 修訂後之 R-1 四值不同步——此即 F4「全檔掃描同步修正所有規則、AC 與測試」於測試檔之漏網。處置：改名 `test_r1_blob_failures_all_four_variants`，集合補入 `core.BlobCheck.MISMATCH`；僅此一處，5 行內。
  - **取證附註（非阻斷）**：Phase 0 `may_activate_ticket=true` 與 `active.json` mtime 不變並存，其機械解釋（同票 idempotent 不重寫？抑或 adapter 於該路徑未呼叫 writer？）**未在盤**；AI-3.1-b 收尾 MUST 依 **Rev D.3-1 六欄格式**補記，`attribution` 封閉四值、預設 `unresolved`。
  - **commit 令**：AI-3.1＋AI-3.1-b 合為**一個** commit：`[INFRA-PREFLIGHT-CORE-01] AI-3.1 具名 exit-code 斷言＋R-1 四值測試同步`；僅 `test_preflight_core.py` 一檔；**不 refresh**（未觸 target script）
- 於 `test_preflight_core.py` 新增**四個具名 test method**（禁以參數化或全表迴圈替代）：
  1. `test_exit0_v0pass_with_blocked_unattributed_state`
  2. `test_exit0_v0pass_with_blocked_scope_or_target_mismatch`
  3. `test_exit0_v0pass_with_candidate_superseded`
  4. `test_exit1_v0_each_failure_value`（`DETACHED_HEAD`／`NO_ACTIVE_TICKET`／`OUT_OF_SCOPE` 各一 `subTest`）
- 方法名 MUST 可由 `python3 -m unittest -v` 輸出逐行指認；收尾附該輸出
- [x] 完成：開工前置唯讀取證（`WRITE_AUTHORITY=NO`／`CORE_MAY_ACTIVATE_TICKET=true`／`V1_STATUS=ALREADY_EXECUTED_PENDING_CLOSURE`(base=`9895b3f`)／`blob_check=MATCH`；`active.json` mtime 執行前後不變，確認零寫入）→ `may_activate_ticket=true` 續行；僅動 `test_preflight_core.py` 一檔，`preflight_activation.sh`／`preflight_core.py`／`preflight_rules.json` 零異動；既有 9 個 test method 逐字保留。四具名 method 全数新增，`python3 -m unittest test_preflight_core -v` 13 methods 全 PASS：
  ```
  test_all_rows (test_preflight_core.DecisionTableTest.test_all_rows) ... ok
  test_every_row_exit_code_matches_v0_env_rule (test_preflight_core.ExitCodeCompatibilityTest.test_every_row_exit_code_matches_v0_env_rule) ... ok
  test_exit0_v0pass_with_blocked_scope_or_target_mismatch (test_preflight_core.ExitCodeCompatibilityTest.test_exit0_v0pass_with_blocked_scope_or_target_mismatch) ... ok
  test_exit0_v0pass_with_blocked_unattributed_state (test_preflight_core.ExitCodeCompatibilityTest.test_exit0_v0pass_with_blocked_unattributed_state) ... ok
  test_exit0_v0pass_with_candidate_superseded (test_preflight_core.ExitCodeCompatibilityTest.test_exit0_v0pass_with_candidate_superseded) ... ok
  test_exit1_v0_each_failure_value (test_preflight_core.ExitCodeCompatibilityTest.test_exit1_v0_each_failure_value) ... ok
  test_v2_provenance_known_is_unlisted (test_preflight_core.ProducerClosureTest.test_v2_provenance_known_is_unlisted) ... ok
  test_v2_semantics_failed_is_unlisted (test_preflight_core.ProducerClosureTest.test_v2_semantics_failed_is_unlisted) ... ok
  test_context_digest_varies_with_activation_intent (test_preflight_core.PurityTest.test_context_digest_varies_with_activation_intent) ... ok
  test_decide_is_stable_under_cleared_environment_and_cwd (test_preflight_core.PurityTest.test_decide_is_stable_under_cleared_environment_and_cwd) ... ok
  test_no_purity_violation_tokens_in_source (test_preflight_core.PurityTest.test_no_purity_violation_tokens_in_source) ... ok
  test_r1_blob_failures_all_three_variants (test_preflight_core.UnlistedCombinationTest.test_r1_blob_failures_all_three_variants) ... ok
  test_random_unlisted_combination (test_preflight_core.UnlistedCombinationTest.test_random_unlisted_combination) ... ok

  ----------------------------------------------------------------------
  Ran 13 tests in 0.006s

  OK
  ```
  ⛔ STOP 已守：完成後未接續 AI-5.1／AI-6／AI-7。尚待：`/qa`→`/sv` Cross-Review 走完後方構成碼閉環，本欄現況為 UC 執行端原始回報

- **AI-3.1-b 完成（2026-09-08，F4 殘留併入）**：`test_r1_blob_failures_all_three_variants` 改名 `test_r1_blob_failures_all_four_variants`，迭代集合補入 `core.BlobCheck.MISMATCH`（`test_preflight_core.py` L128-134，僅此一處）。`python3 -m unittest test_preflight_core -v` 13 methods 全 PASS（含改名後方法、`subTest` 為四值）：
  ```
  test_all_rows (test_preflight_core.DecisionTableTest.test_all_rows) ... ok
  test_every_row_exit_code_matches_v0_env_rule (test_preflight_core.ExitCodeCompatibilityTest.test_every_row_exit_code_matches_v0_env_rule) ... ok
  test_exit0_v0pass_with_blocked_scope_or_target_mismatch (test_preflight_core.ExitCodeCompatibilityTest.test_exit0_v0pass_with_blocked_scope_or_target_mismatch) ... ok
  test_exit0_v0pass_with_blocked_unattributed_state (test_preflight_core.ExitCodeCompatibilityTest.test_exit0_v0pass_with_blocked_unattributed_state) ... ok
  test_exit0_v0pass_with_candidate_superseded (test_preflight_core.ExitCodeCompatibilityTest.test_exit0_v0pass_with_candidate_superseded) ... ok
  test_exit1_v0_each_failure_value (test_preflight_core.ExitCodeCompatibilityTest.test_exit1_v0_each_failure_value) ... ok
  test_v2_provenance_known_is_unlisted (test_preflight_core.ProducerClosureTest.test_v2_provenance_known_is_unlisted) ... ok
  test_v2_semantics_failed_is_unlisted (test_preflight_core.ProducerClosureTest.test_v2_semantics_failed_is_unlisted) ... ok
  test_context_digest_varies_with_activation_intent (test_preflight_core.PurityTest.test_context_digest_varies_with_activation_intent) ... ok
  test_decide_is_stable_under_cleared_environment_and_cwd (test_preflight_core.PurityTest.test_decide_is_stable_under_cleared_environment_and_cwd) ... ok
  test_no_purity_violation_tokens_in_source (test_preflight_core.PurityTest.test_no_purity_violation_tokens_in_source) ... ok
  test_r1_blob_failures_all_four_variants (test_preflight_core.UnlistedCombinationTest.test_r1_blob_failures_all_four_variants) ... ok
  test_random_unlisted_combination (test_preflight_core.UnlistedCombinationTest.test_random_unlisted_combination) ... ok

  ----------------------------------------------------------------------
  Ran 13 tests in 0.007s

  OK
  ```
  commit `8fb0c01`，僅 `test_preflight_core.py` 一檔；`preflight_core.py`／`preflight_rules.json`／`preflight_activation.sh` 零異動；未 refresh，`TARGET_BLOB` 未動。
  - **[SMTR 覆核 2026-09-08：PASS]** 本地一手：`git_log` 訊息逐字（`aud_20260908_85979e219f`）、`all_four_variants` L128＋`MISMATCH` L133、舊名零命中（`5af4f35747`）。**遠端佐證（GitHub 唯讀，外部引擎回件，未附 commit URL → 依回件驗收不作裁決依據，僅記為第三通道交叉確認）**：`akirachangtw/SmartCameraApp1` 上 `8fb0c01` 存在、+83/−1、僅一檔；測試檔 blob `c321ce56`；`745ab25`／`5fd5dac` 皆僅改 PROGRESS 檔，未夾帶 preflight 變更。
  - **WRITE 差異之 O4 分類：`EVIDENCE_GAP`**（不改 R-n／AC／HC）→ 併入 AI-5.1 動作 E 收尾，不開 Rev、不計 4a。**主假說（由本次六欄 `V2_STATE=MISSING` 直接支持）**：AI-3.1-b 量測時 `active.json` 無活動票 → `may_activate_ticket=true` 觸發預期之 activation 寫入；AI-3.1 Phase 0 時 V2 疑為 `SAME_TICKET`（已活動、內容相同）故未重寫——差異來源為 **V2 狀態不同，非行為不一致**；V2 由 SAME_TICKET 轉 MISSING 之可能機制為 session 切換觸發 `INFRA-SESSION-HOOK-01` 之 SessionStart reset 清除 `tmp/verify/`。**次假說**：`head_sha` 變動致同票重寫。兩者皆可由 AI-5.1 動作 E 之六欄＋`head_sha_before/after` 機械判別，不需加 trace。AI-3.1 Phase 0 未記 `V2_STATE` 為該次取證缺口之根源。
  - **D.3-1 V2_STATE 取證（六欄）**：
    ```
    V2_STATE=MISSING; CORE_MAY_ACTIVATE_TICKET=true; active_json_mtime_before=1788862338; active_json_mtime_after=1788862389; write_observation=WRITE; attribution=unresolved
    ```
    取證方法：`bash .claude/skills/qa/scripts/preflight_activation.sh INFRA-PREFLIGHT-CORE-01` 單次乾淨執行前後各 `stat -c '%Y' tmp/verify/active.json`（epoch 秒），stderr 另存確認 `📌 active.json:` 寫入訊息出現（唯讀執行本身不算違反 HC「不 refresh」——refresh 專指 `TARGET_BLOB` 重算，本次未觸）；`CORE_MAY_ACTIVATE_TICKET=true` 取得方式：`source` 既有 `preflight_activation.sh`（未修改檔案內容），依 `main()` 實際呼叫順序（`v0_check`→`v1_check`→`v2_probe`→`invoke_core_decision`）在子 shell 中重放既有函式並讀取其設定之全域變數，非新增 trace 或修改 script/adapter。
    - **⚠️ 與 AI-3.1 原始記載（L287 "`active.json` mtime 執行前後不變，確認零寫入"）不一致**：本次 AI-3.1-b 重測顯示 `write_observation=WRITE`（mtime 確實變動），非 NO_WRITE。歸因：`CORE_MAY_ACTIVATE_TICKET=true` 觸發 `preflight_activation.sh` L554-555 呼叫 `activate_via_p3_writer`，此為腳本設計之預期行為（非異常），但與 AI-3.1 執行當下之觀測不同（可能為兩次執行間 `active.json` 狀態或執行時序差異所致，未進一步追查——追查需加 trace／改 adapter，屬 AI-5.1／AI-6 範圍，本票 MUST NOT 擴 scope）。因四值 attribution 之三個具名值（`V2_ZERO_WRITE_CONTRACT`／`SAME_TICKET_IDEMPOTENT`／`ADAPTER_PATH_NOT_INVOKED`）語意上均為「解釋無寫入」，本次觀測為「有寫入」，三者均不適用，且未附加額外 trace 佐證何值成立，故依 HC 預設規則填 `unresolved`。
  - ⛔ STOP 已守：完成後未接續 AI-5.1；不跑票級 `/qa`／`/sv`

### AI-4: adapter shadow mode（不改既有 writer／最終流程）
- Bash 同時：①照既有路徑產出舊 V0／V1／V2 狀態與實際 process exit code；②蒐集 facts JSON 交核心取得含 `final_status`／`process_exit_code` 的新 Decision；③依 F-0～F-8 產生 legacy 等價收斂值後**僅記錄差異**至 `.tmp/preflight_shadow_<date>.log`，**不改變任何副作用**
- V0 failure → collector 對全部未執行之 V1／V2 facts 產生 F-0 固定 `NOT_EVALUATED` 哨兵組合；此為 early-return 的標準化 producer，禁止呼叫下游探針補值
- facts collector 將 BASE_COMMIT 缺失／佔位字面轉為 `baseline_check=MISSING`，`git rev-parse --verify` 非零轉為 `UNRESOLVABLE`；兩者均搭配 `v1_scope=NOT_EVALUATED`／`v1_provenance=NOT_APPLICABLE`，不得執行或臆測 scope diff
- facts collector 將 `TARGET_BLOB` 之 `NEW`／`N/A` 轉為 `blob_check=SKIPPED_NEW`／`SKIPPED_NA`；`git hash-object` 非零 → `COMMAND_FAILED`；標的不存在 → `TARGET_MISSING`；票面值格式非法 → `INVALID_TICKET_VALUE`；`git hash-object` 成功且與 `TARGET_BLOB` 相符 → `MATCH`，不符 → `MISMATCH`（Rev D F4 補明 producer——原票面缺此二值之 collector 映射，為 F4 漏實作之上游成因）。BASE_COMMIT 不可用時固定 `blob_check=NOT_EVALUATED`；blob failure 時固定 `v1_scope=NOT_EVALUATED`／`v1_provenance=NOT_APPLICABLE`。**Bash 不得據此直接導向 V1 判定**，僅轉為標準化 fact
- 於 AI-0 動作 C 之全部消費端情境跑 shadow，收尾附差異日誌；**差異非零時 MUST 逐筆歸因**（enum 映射錯／facts 漏維度／舊邏輯缺陷），禁以「新的比較對」帶過
- [x] 完成：`collect_blob_check_fact()` 獨立重新量測（不依賴 `v1_check()` 既有判定值，避免循環推論）；真實執行 3 情境（V0 fail 空 ticket_id、implicit resume、explicit switch）+ 1 真實 R3 缺陷觸發案例。**差異歸因（2 筆，皆已修正並重驗 MATCH）**：①本票 `TARGET_BLOB` 票面欄位為未回填佔位字面 → `collect_blob_check_fact` 正確判 `INVALID_TICKET_VALUE`，legacy 因格式不含冒號而靜默跳過判 `BASELINE_MATCHED`（歸因：票面欄位未回填，非程式缺陷，已於 AI-0 補回填修正）；②回填後真實 `TARGET_BLOB` 與**當時**現行 hash 比對觸發真實 MISMATCH，legacy 正確判 `BLOCKED_SCOPE_OR_TARGET_MISMATCH`，core 誤判 `BLOCKED_ENV_MISMATCH`（歸因：core `_is_f2_blob_failure()` 遺漏 `MISMATCH` 值，真實核心缺陷，已修正見 AI-1）。日誌存 `.tmp/preflight_shadow_20260907.log`

### AI-5: authority 切換（僅於 shadow 差異全數歸因後）
- 核心 `Decision` 接管：`may_activate_ticket` 控 P3 writer；`write_authority` 控輸出；R-4 identity 比對於任何副作用前執行
- **相容性判準（取代逐字相容）**：AI-0 動作 C 清單中之既有 machine-readable key 逐項維持；process exit code 嚴格依 R-5（V0 failure＝1，其餘＝0），不得概括成「0 放行／1 blocked」；**core 缺席／protocol 無效／identity 不符時改依 R-7 固定 1（Rev D）**；**允許新增**欄位與診斷行（`reason_code`、`context_id`、`blob_check` 等）；**禁止改名或移除**既有欄位
- [~] **部分達成（Rev D 降級；以下為執行端原始回報，保留存證不改寫）**：`main()` 改為 `invoke_core_decision()` 取得 Decision 後才呼叫 `activate_via_p3_writer()`（由 `CORE_MAY_ACTIVATE_TICKET` 閘控，取代原「V0 PASS 即無條件寫入」），`WRITE_AUTHORITY=` 改源自 `CORE_WRITE_AUTHORITY`；R-4 以 `CORE_TICKET_ID == ticket_id` 比對，不符則 fail-open 回退 legacy 路徑（HC-6：唯讀取證動作不阻斷）；core 不可用時（python3 缺失／解析失敗）同樣 fail-open 回退。**行為性實測**：①真實 blocked 案例（本票 TARGET_BLOB mismatch）→ `may_activate_ticket=false`，`active.json` 未被更新（timestamp 停留於前次寫入值，證明新 gate 生效，此為與 legacy 相異之刻意新行為）；②真實 clean 案例（sourced 函式 + fixture 票檔比對現行 hash 相符）→ `may_activate_ticket=true`／`write_authority=YES`。既有 8 條 self-test 與既有 stdout key 集合（`V0_STATUS`／`V1_STATUS`／`V2_STATE`／`WRITE_AUTHORITY` 等）逐項維持不變
- **[Rev D 複審判定]**：AI-5 由 `[x]` 降回**部分達成**。兩項偏離：①引入票面未授權之 fail-open 回退（F1，並誤引本票 HC-6——該條實為「禁 pytest／禁根 `tests/`／禁 pip install」，與 fail-open 無關）；②R-4 僅實作 `ticket_id`，`CORE_CONTEXT_ID` 賦值後零使用（F2）。補件見 AI-5.1

### AI-5.1: fail-closed 與四欄 identity 回修（Rev D 新增，F1／F2）
- 移除 `preflight_activation.sh` 中 core 不可用／identity 不符之 **legacy policy 回退分支**；改依 R-7 fail-closed
- R-4 依 B2 實作四欄比對（`ticket_id`／`repo_root`／`worktree_path`／`head_sha`）；`context_id` 僅隨診斷輸出，不作判準
- 新增測試情境（`test_preflight_adapter.sh` 或等價 harness）：①`python3` 不可達（PATH 遮蔽）②`preflight_core.py` 暫時移離 ③core 輸出截斷／缺欄 ④四欄各自不符 —— 四者均斷言 `active.json` mtime 不變、exit 1、輸出含 `ADVISORY_ONLY`
- **⛔ 本 AI 之 refresh 節點（Rev D.2-1 甲案）**：AI-5.1 維持**獨立 commit → 獨立 refresh（一次，記前後 sha8＋commit hash）→ 獨立重跑 preflight** 三段檢核；**禁與 AI-6 合併**
- **[SMTR 覆核 2026-09-08：PASS（附 AC R-7 降級）]** 一手：commit `5c70987` 存在、僅三檔（`git_show`，`aud_20260908_09c826a461`）；diff index `0f723e6..c30ac68` 直接證 `TARGET_BLOB` 前後 blob；`BASE_COMMIT` L150 仍 `9895b3f`；後續 `6888bcc`（housekeeping）僅涉 `screenshots/`（`git_changed_files`，`420d32cbb4`），refresh 仍有效。F1 根治屬實：`else` 分支無 `activate_via_p3_writer`、無 `write_authority()` 決策、`WRITE_AUTHORITY=NO`＋`r7_blocked=1`→`exit 1`。R-4 四欄判定在盤。**兩項 T1 附註轉 AI-6**：(a) `CORE_WORKTREE_PATH` 與 `CORE_REPO_ROOT` **皆與 `V0_REPO_ROOT` 比對**（L588–589），第三欄實為第二欄之複本，linked worktree 情境下無獨立量測——AC「多 worktree 並行」未勾，正確；AI-6 facts collector 重寫時 MUST 以獨立命令取 worktree_path；(b) `preflight_core.py` 新增三回顯欄位屬 R-4 B2 之必要條件（core 須回傳供比對），非判定邏輯變更，13 tests 全 PASS 佐證向下相容，**不構成違反「禁改核心判定邏輯」HC**。
- [x] 完成：
  1. **移除 fail-open 回退**：`preflight_activation.sh` `main()` 之 `else` 分支（原無條件呼叫 `activate_via_p3_writer` + legacy `write_authority()` 判定）已移除，改為 R-7 fail-closed（`core_identity_ok=0` 時：不呼叫 writer、輸出 `ADVISORY_ONLY` 診斷、`WRITE_AUTHORITY=NO`、`process_exit_code=1`）
  2. **R-4 四欄比對**：`invoke_core_decision()` 新增解析 `CORE_REPO_ROOT`／`CORE_WORKTREE_PATH`／`CORE_HEAD_SHA`（`preflight_core.py` `Decision` 新增同名回顯欄位，純新增不改名不移除既有欄位）；`main()` 之 `core_identity_ok` 判定四欄（`ticket_id`／`repo_root`／`worktree_path`／`head_sha`）全等
  3. **三值 reason_code**：`CORE_UNAVAILABLE`（python3／`preflight_core.py` 缺席）／`CORE_PROTOCOL_INVALID`（呼叫失敗或必要欄位缺漏）／`IDENTITY_MISMATCH`（core 可用但四欄任一不符）— 於 `invoke_core_decision()` 之 `CORE_UNAVAILABLE_REASON` 全域變數與 `main()` 內部判定產生，僅作內部 reason code，不新增對外 status enum
  4. **`test_preflight_adapter.sh` 新建**：四情境（①python3 不可達，PATH 過濾法，非修改腳本 ②`preflight_core.py` 暫時移離＋還原 ③core 輸出截斷／缺欄，暫換 stub＋還原 ④四欄各自不符，逐一注入單一錯誤欄位）× 4 項斷言（mtime 不變／exit 1／`ADVISORY_ONLY`／對應 reason_code）＝28 項全 PASS
  5. **`python3 -m unittest test_preflight_core -v`**：13 methods 全 PASS（純核心邏輯未變，僅新增回顯欄位，向下相容）
  6. **commit**：`5c70987`（僅 `preflight_activation.sh`／`preflight_core.py`／`test_preflight_adapter.sh` 三檔，`preflight_rules.json` 零異動）
  7. **獨立 refresh（僅一次）**：`TARGET_BLOB` 前值 `0f723e69` → 後值 `c30ac687`（對應 commit `5c70987`）；`BASE_COMMIT` 維持 `9895b3f` 不動
  8. **獨立重跑 preflight**：`bash .claude/skills/qa/scripts/preflight_activation.sh INFRA-PREFLIGHT-CORE-01` → `blob_check=MATCH`（獨立 sourced 函式呼叫確認），`V1_STATUS=ALREADY_EXECUTED_PENDING_CLOSURE`（區間內存在本票 ID 前綴 commit，非阻斷態），本票未自我阻斷

### AI-W: WIP 保存與 ticket-local baseline refresh（Rev D 新增，續作前置）
- **動作 1（WIP commit）**：提交現有 AI-0～AI-5 變更（4 檔，全在 ALLOWED）。commit message MUST 為 `[INFRA-PREFLIGHT-CORE-01] WIP AI-0~AI-5（AI-3/AI-5 部分達成，未收票）`；**禁寫 DONE／完成／CLOSED 字樣**。commit **不構成** AI-5 完成、AC 通過或 ticket 收束
- **動作 2（baseline refresh，僅一次）**：WIP commit 後以 `git hash-object` 取 `preflight_activation.sh` 之新 sha8，回填 Verify Config `TARGET_BLOB`；`BASE_COMMIT` 維持 `9895b3f` 不動。收尾 MUST 記載 refresh 前後 sha8 與其對應 commit，形成可重放 evidence
- **動作 3**：refresh 後重跑一次 preflight，確認本票不再自我阻斷（`blob_check=MATCH`）；若仍 blocked → 停止回報升裁，禁自行再 refresh
- **禁止**：本階段 refresh 超過一次、靜默豁免 blob 比對、以 `NEW`／`N/A` 佔位規避。（後續 AI-5.1／AI-6 之腳本異動所需之 refresh 另依 Rev D.1「baseline refresh 排序議題」裁決辦理，**不在本 AI 授權範圍**）
- **⛔ STOP 條件**：AI-W 三動作完成後 STOP 回報，**MUST NOT** 逕行接續 AI-3.1／AI-5.1／AI-6／AI-7
- [x] 完成：動作 1（commit `ded5dd2`）／動作 2（refresh `07640f6b`→`0f723e69`）／動作 3（`blob_check=MATCH` 實測，`V1_STATUS=ALREADY_EXECUTED_PENDING_CLOSURE`，shadow marker=MATCH）三動作皆有真實執行證據，見上方各動作記載；STOP 已依規範執行，未接續 AI-3.1／AI-5.1／AI-6／AI-7。**[Rev D.2 覆核判定：PASS]**；惟收尾未記載 `WRITE_AUTHORITY`／`CORE_MAY_ACTIVATE_TICKET` 實測值，取證缺口已轉列 AI-3.1 開工前置（見 Rev D.2-2）

### AI-6: 清除 Bash 舊判定邏輯
- **⛔ 派工前置（Rev D，缺一即禁派工）**：① Rev D 生效 ✅；② AI-W 完成且 baseline refresh 有紀錄 ✅（`ded5dd2`）；③ MVP enablement gate 由 owner 明確放行 ✅。**三者已全數成立**；**另加 Rev D.2-2 之 V1 前置取證**（`may_activate_ticket` 實測），未完成前 MUST NOT 開工
- **⛔ 本 AI 之 refresh 節點（Rev D.2-1 甲案）**：AI-6 維持**獨立 commit → 獨立 refresh（一次）→ 獨立重跑 preflight** 三段檢核；**禁與 AI-5.1 合併為單一大範圍腳本改動以減少 refresh 次數**
- **[承接 AI-5.1 之 `EVIDENCE_GAP`，收尾 MUST 結清]**：①刪除 `write_authority()`／`legacy_equivalent_status()` 等 legacy 判定函式後，以 `grep` 零命中＋harness trace 證 R-7 四情境「零執行」，將 AC R-7 由 `[~]` 轉 `[x]`；②定義 R-7 路徑之 stdout 契約——`final_status`／`ADVISORY_ONLY` 是否以新增 stdout key 承載（新增 key 不違「既有 key 逐項維持」，移除或改名才違）；③facts collector 以獨立命令取 `worktree_path`（非 `V0_REPO_ROOT` 複本）。三項皆為實作完成度，不開 Rev。
- **完成定義為責任驗收，非 grep**：Bash 不再依 V0/V1/V2 組合自行選定 `final_status`、authority 或 rule outcome；僅將 raw facts 交核心、驗 R-4（四欄）、依 R-7 處理 core 缺席、執行核心明示之副作用。**因 Rev D A1 已移除 legacy 回退，degraded 路徑不再需要保留 legacy 判定邏輯——此為 AI-6 目標可達之前提**
- 收尾 MUST 附「Bash 內每一處分支之職責歸類」表（facts 蒐集／identity 驗證／副作用執行／格式化輸出），**任一分支被歸為「判定」即未完成**
- `tests/test_preflight_adapter.sh`：承接 AI-0 動作 D 之 integration 測試清單
- **[本輪派工執行記載，Akira 2026-09-08 明示範圍界定]**：本輪派工文字明列 Phase 0-3＋Hard Constraints＋Acceptance，**未含**本節線 385「Bash 內每一處分支之職責歸類表」要求；依派工文字逐字執行，該表不在本輪 Acceptance 內，**MUST NOT** 據此自行擴權補做或推定完成。承接 AI-5.1 之三項 EVIDENCE_GAP（AC R-7 見上方 `[x]` 記載）已結清；`route_v2_state()` 之 Phase 0 判定（core 無等價欄位，保留）已記於 AI-6 sv_report。
  - 動作 1（commit，完整工作單元）：`28df403`——刪除 `write_authority()`／`legacy_equivalent_status()`／`log_shadow_diff()` 三函式及全部呼叫點＋self-test 對應斷言；R-7 fail-closed 分支新增 `CORE_FINAL_STATUS`／`ADVISORY_ONLY` 二 stdout key；`collect_facts_json` 之 `worktree_path` 改獨立 `git rev-parse --show-toplevel` 取值。僅涉 `preflight_activation.sh` 一檔（`git diff --stat` 實測，ALLOWED 範圍內）
  - 動作 2（baseline refresh，僅一次）：`TARGET_BLOB` 前值 `c30ac687` → 後值 `47a4d120`（對應 commit `28df403`）；`BASE_COMMIT` 維持 `9895b3f` 不動
  - 動作 3（驗證不自我阻斷）：refresh 後重跑 `bash .claude/skills/qa/scripts/preflight_activation.sh INFRA-PREFLIGHT-CORE-01` → `blob_check` 待下一輪 activation 後以 `V1_STATUS`／`collect_blob_check_fact()` 實測值記載（見 AI-6 sv_report；本檔僅承載 refresh 動作本身之可重放 evidence）
  - `test_preflight_core.py` 13/13 PASS；`test_preflight_adapter.sh` 28/28 PASS；`preflight_activation.sh --self-test` 3/3 PASS（既有 8 條中 5 條 write_authority 斷言隨函式刪除同步移除，8→3，SSOT 衝突已於 sv_report 揭露，見 Hard Constraints 措辭與 Phase 1 明文之落差說明）
- [x] 完成——AI-6 收尾派工（SMTR L396）已補做：(a) 職責歸類表見下方，全部函式與 `main()` 分支已歸入四類，`route_v2_state` 歸 `LEGACY_STDOUT_SHIM`；(b) `test_preflight_adapter.sh` R-7 四情境各加 2 條 stdout key 存在斷言，28→36，commit `5b5d294`，36/36 PASS 實測；commit 僅涉 `test_preflight_adapter.sh`（`git diff --stat` 實測），`preflight_activation.sh` 未觸，未 refresh（`TARGET_BLOB` 仍 `47a4d120`，`BASE_COMMIT` 仍 `9895b3f`）
- **[SMTR 覆核 2026-09-08：主體 PASS，AI-6 未結；派工令漏列為 SMTR 過失]** 一手：`28df403` 僅一檔（`aud_20260908_7335728314`）；三函式 grep 零命中僅餘 L507 註記（`b3092822b9`）；`CORE_FINAL_STATUS`／`ADVISORY_ONLY` 二 stdout key 在 L534–535；refresh `c30ac687`→`47a4d120`、`BASE_COMMIT` 未動。**執行端未勾完成並指出派工令與票面落差，判斷正確**——職責歸類表為本 AI 責任驗收本體，非附屬文件；`route_v2_state()`（組合判定、grep 抓不到）之合法存在唯一理由「legacy stdout key 相容 shim」只有該表能承載。
  - **如實記載 ①**：`collect_worktree_path_fact()` L388 與 `V0_REPO_ROOT` L70 為同一命令（`git rev-parse --show-toplevel`），L514 比對端仍對 `V0_REPO_ROOT`，兩欄恆同值——「獨立取值」形式達成、實質未達。**不視為執行端缺陷**：Facts Contract 未定義 `repo_root`／`worktree_path` 語意差異，派工令亦未指定命令；現行值於 linked worktree 下仍可區分（各 toplevel 不同），R-4 綁定目的已達，第二欄冗餘。**不開 Rev**；AI-7 依 D.3-3 MUST 如實寫「兩欄現為同值」；AC「多 worktree 並行」維持未勾。語意定義留票級收割。
  - **如實記載 ②**：二新 stdout key 無斷言覆蓋（`test_preflight_adapter.sh` 本 commit 未動）。
  - **AI-6 收尾派工（同 AI，不加深度，不觸 target script，不 refresh）**：(a) 職責歸類表——`preflight_activation.sh` 每一函式與 `main()` 每一分支歸入 `FACT_COLLECTION`／`CORE_INVOCATION`／`CORE_DIRECTED_SIDE_EFFECT`／`LEGACY_STDOUT_SHIM` 四類之一，任一無法歸類即 AI-6 未完成；`route_v2_state` MUST 在表中且歸 `LEGACY_STDOUT_SHIM`；表落於本節下方。(b) R-7 四情境各加 `CORE_FINAL_STATUS=BLOCKED_ENV_MISMATCH`、`ADVISORY_ONLY=true` 存在斷言（28→36）。commit 僅 `test_preflight_adapter.sh`＋票面。完成後 STOP。

#### AI-6 收尾：Bash 內每一處分支之職責歸類表（2026-09-08，commit `5b5d294`）

四類定義：`FACT_COLLECTION`＝蒐集／整併原始環境或 git 事實，不選定 `final_status`／authority；`CORE_INVOCATION`＝呼叫 `preflight_core.py` 並解析／驗證其回傳（含 R-4 identity 比對，屬「安全消費 core 輸出」之一部）；`CORE_DIRECTED_SIDE_EFFECT`＝執行 core 明示指示之副作用（P3 寫入、或 R-7 fail-closed 之固定回應——R-7 本身是 core 缺席時的既定契約回應，非 Bash 自選 rule outcome）；`LEGACY_STDOUT_SHIM`＝AI-5.1 前遺留、僅供 stdout 顯示、不再進入任何真實決策路徑之計算。

**函式**

| 函式 | 歸類 | 理由 |
| --- | --- | --- |
| `json_field()` | `FACT_COLLECTION` | 從 `active.json` 擷取單一 KV，純資料讀取 |
| `activate_via_p3_writer()` | `CORE_DIRECTED_SIDE_EFFECT` | 僅於 `main()` 判定 `CORE_MAY_ACTIVATE_TICKET=true`（core 指示）時被呼叫，寫入 `active.json` |
| `v0_check()` | `FACT_COLLECTION` | 蒐集 repo root／branch／HEAD／票檔存在性等環境事實，產出 `V0_STATUS` 供後續 `map_v0_env_fact()` 轉譯為 Facts Contract 欄位；不選定 `final_status`／`write_authority` |
| `lookup_ticket_file()` | `FACT_COLLECTION` | 自 `TICKET_REGISTRY.json` 讀取 `file` 欄位 |
| `normalize_ticket_path()` | `FACT_COLLECTION` | 純路徑字串正規化，無判斷分支 |
| `v1_check()` | `FACT_COLLECTION` | 產出 legacy `V1_STATUS` 列舉，但該值僅作為 `map_v1_facts()` 之輸入原料轉譯為 Facts Contract 標準值，非直接選定 authority／outcome——真正之 rule outcome 選定全部發生於 `preflight_core.py::decide()` |
| `v2_probe()` | `FACT_COLLECTION` | 讀取（選填）semantic contract 檔，逐條驗證 symbol/file 存在性，產出 `V2_OVERALL`／`V2_RESULTS`／`V2_PROVENANCE` 供 `map_v2_facts()` 轉譯 |
| `route_v2_state()` | `LEGACY_STDOUT_SHIM` | AI-5.1 前用於收斂 V2 子結果之遺留函式；`main()` 唯一消費點為 `V2_STATE=` 診斷行（純 stdout 顯示），**不**進入 `collect_facts_json()`／`invoke_core_decision()` 之真實決策路徑（該路徑改由 `map_v2_facts()` 直接讀 `V2_OVERALL`／`V2_PROVENANCE`）；core Decision 無對應 `v2_state` 等價欄位（`final_status` 混合 V0/V1/V2，值域不同，見 AI-6 Phase 0 判定），故本函式無法刪除亦無法歸為其餘三類，保留為相容 shim |
| `map_v0_env_fact()` | `FACT_COLLECTION` | 將 `V0_STATUS`／`V0_REASON` 轉譯為 Facts Contract `v0_env` 列舉值 |
| `map_v1_facts()` | `FACT_COLLECTION` | 將 `V1_STATUS`／`V1_REASON` 轉譯為 `FACT_BASELINE_CHECK`／`FACT_V1_SCOPE`／`FACT_V1_PROVENANCE`／`FACT_BLOB_CHECK` |
| `collect_blob_check_fact()` | `FACT_COLLECTION` | 獨立重新量測 `TARGET_BLOB` 一致性，產出 Facts Contract `blob_check` 值 |
| `map_v2_facts()` | `FACT_COLLECTION` | 將 `V2_OVERALL`／`V2_PROVENANCE` 轉譯為 `FACT_V2_SEMANTICS`／`FACT_V2_PROVENANCE` |
| `collect_worktree_path_fact()` | `FACT_COLLECTION` | 獨立呼叫 `git rev-parse --show-toplevel` 取 `worktree_path` 事實（AI-6 動作 1 新增，取代 `V0_REPO_ROOT` 複本） |
| `collect_facts_json()` | `FACT_COLLECTION` | 彙整上述所有 Facts Contract 欄位為單一 JSON，供送交 core；本身不含判斷分支選定 outcome |
| `invoke_core_decision()` | `CORE_INVOCATION` | 呼叫 `python3 preflight_core.py`、解析 Decision JSON、驗證必要欄位完整性；`CORE_UNAVAILABLE_REASON` 之設定為「core 是否可用」之事實回報，非規則判定 |

**`main()` 分支**

| 分支 | 歸類 | 理由 |
| --- | --- | --- |
| Ticket ID 解析（顯式參數 vs `active.json`）＋ `activation_intent` 判定 | `FACT_COLLECTION` | 決定要蒐集哪個 ticket 的事實，非決定 outcome |
| `v0_check` 呼叫＋ `V0_STATUS`／`REPO_ROOT`／`BRANCH`／`HEAD`／`GIT_STATUS_SHORT_LINES`／`ACTIVE_TICKET_IDENTITY` 診斷 echo | `FACT_COLLECTION` | 純事實蒐集與回報 |
| `if V0_STATUS != PASS → echo REASON/ACTION; exit 1` | `FACT_COLLECTION` | 環境事實不足以繼續蒐集（repo 不可解析／detached HEAD／無 ticket_id／票檔不存在）時之終止；**未呼叫 core**、**未輸出 `WRITE_AUTHORITY`**，不構成「Bash 自選 rule outcome」——僅係「事實蒐集階段本身失敗，無法產出可送核心之 Facts Contract」之自然終態，判斷理由已在 AI-6 sv_report 明載，非事後代入 |
| `TICKET_FILE=` echo | `FACT_COLLECTION` | 事實回報 |
| `allowed_targets` 讀取＋ `v1_check` 呼叫＋ `V1_STATUS`／`V1_BASE_COMMIT`／`V1_REASON` echo | `FACT_COLLECTION` | 見 `v1_check()` 理由 |
| `v2_probe` 呼叫＋ `V2_JSON=` echo | `FACT_COLLECTION` | `V2_JSON` 直接引用 `V2_RESULTS`／`V2_OVERALL`／`V2_PROVENANCE`（原始事實），非引用 `route_v2_state()` |
| `route_v2_state` 呼叫＋ `V2_STATE=` echo | `LEGACY_STDOUT_SHIM` | 見 `route_v2_state()` 理由；此為該函式在 `main()` 內的唯一消費點 |
| `invoke_core_decision` 呼叫 | `CORE_INVOCATION` | 見上 |
| `core_identity_ok` 四欄比對（`CORE_TICKET_ID`／`CORE_REPO_ROOT`／`CORE_WORKTREE_PATH`／`CORE_HEAD_SHA` vs 現行值） | `CORE_INVOCATION` | R-4 identity binding，屬「安全消費 core 回傳值」之驗證步驟（防重放／防跨票污染），非重新推導 V0/V1/V2 組合以得出獨立 outcome |
| `core_identity_ok=1` 分支：條件呼叫 `activate_via_p3_writer`＋ `WRITE_AUTHORITY=${CORE_WRITE_AUTHORITY}` echo | `CORE_DIRECTED_SIDE_EFFECT` | 寫入時機由 `CORE_MAY_ACTIVATE_TICKET` 閘控，值本身由 `CORE_WRITE_AUTHORITY` 承載，Bash 僅執行核心指示、原樣中繼 |
| `core_identity_ok=0` 分支（R-7 fail-closed）：`ADVISORY_ONLY` stderr／`WRITE_AUTHORITY=NO`／`CORE_FINAL_STATUS=BLOCKED_ENV_MISMATCH`／`ADVISORY_ONLY=true`／`r7_blocked=1` | `CORE_DIRECTED_SIDE_EFFECT` | R-7 契約本身即為「core 缺席／identity 不符時之固定回應」，回應內容（fail-closed、拒絕寫入、exit 1）由票面 R-7 規格預先指定，非 Bash 依 V0/V1/V2 現場組合自選；Bash 僅執行此既定契約 |
| `exit 0` / `exit 1`（依 `r7_blocked`） | `CORE_DIRECTED_SIDE_EFFECT` | 承上，exit code 由 R-4／R-7 契約結果決定，非獨立判定 |

**未列入表格範圍**：`--self-test` 區塊（`_expect_eq` 與三條 `route_v2_state` 斷言）為測試 harness，非 `main()` 執行期分支，不在票面「函式與 `main()` 每一分支」文字範圍內；其存在（測試 `route_v2_state` 之既有行為）與上表 `route_v2_state` 之 `LEGACY_STDOUT_SHIM` 歸類一致，不另計。

**結論**：`preflight_activation.sh` 全部函式與 `main()` 全部分支均已歸入四類之一，零殘留「判定」類。

### AI-7: SKILL.md 契約段
- 補「決策表為規則 SSOT；新增規則 MUST 改表與測試，禁改 Bash 條件」、Model A 字面、**R-4 四欄 identity binding（Rev D B2）**、**R-7 fail-closed 契約（core 缺席＝blocked，非 advisory 放行）**、並行邊界（同一 worktree 同時多票禁止或序列化）
- **⛔ 派工前置同 AI-6**
- **⛔ 本 AI 原則上不得改動 target script（`preflight_activation.sh`）**（Rev D.2-1 甲案）。如確有必要 → **停止回報並重新裁決**，MUST NOT 自行 commit 或 refresh
- **⛔ 文件忠實性（Rev D.3-3）**：每條 MUST／保證／契約／runtime 行為表述 MUST 回鏈已勾 AC 或在盤實測證據（限附 `audit_id`／命令輸出／`file:line` 者，執行端敘事不算）；未完成者 MUST 標 `pending`／`planned`，禁寫成已生效保證
- **⛔ 派工時點（Rev D.3-3／D.3-4）**：MUST 於 AI-5.1 與 AI-6 完成且證據在盤後方得派工
- **[SMTR 覆核 2026-09-08：PASS]** 一手讀 `SKILL.md` 完整 diff（`aud_20260908_d42f62e86a`），非採信回報摘要。commit `4bf0253` 僅涉 `.claude/skills/qa/SKILL.md`（`aud_20260908_1501808a07`）；`TARGET_BLOB` 維持 `47a4d120`，未觸 target script、未 refresh。**十三項回鏈逐一驗證，十二項精確**：票面 `:459`／`:475`／`:477`／`:481`／`:482`／`:483`／`:486`／`:487`／`:490`／`:492`／`:400–439` 全對；`preflight_core.py:271` `_write_authority()` 與 `manual_authorization`／`CONSERVATIVE_MANUAL_NOT_MACHINE_READABLE` 經 grep 確認（`aud_20260908_ba73d70398`）；`preflight_activation.sh:510–517`／`:534–535`／`:385–389`／`:70` 皆對。
  - **D.3-3 忠實性達成**：四項未達 AC 逐一標 `pending`／`planned` 並回鏈未勾 AC 行號；另兩項如實描述義務（`route_v2_state` legacy shim、`v2_provenance=KNOWN`／`v2_semantics=FAILED` 未入 15 rows）在盤。三處**優於派工令要求**：①主動寫入「MUST NOT 簡化為『一切副作用皆由 core 指示』」之防護欄（承接 SMTR 對 `CORE_DIRECTED_SIDE_EFFECT` 之 R-7 一格保留意見）；②worktree 揭露寫成可操作讀法界線（「MUST NOT 讀作已支援 worktree 隔離」），強於單純標 `pending`；③並行邊界明分「既定 non-goal 而非缺陷」並回鏈非目標 §8，未將 scope 外事項寫成待辦或保證。
  - **形式變更（已審，無資訊遺失）**：舊「Write Authority」段落改為指向新契約段之前導引用，非純新增。舊段之保守縮減語義（`YES` 精確條件、`PARTIAL`→`NO (conservative)`、人工 token 無機械通道）已由新段第 2 點完整承載並補 `manual_authorization` 固定值。接受。
  - **⚠️ 一項行號更正（O4 `EVIDENCE_GAP`，不改任何 `R-n`／AC／HC，不開 Rev、不計 4a）**：`SKILL.md` 引用 `route_v2_state` 職責歸類為票面 `:410`，**實際在 `:413`**（`:410` 為 `normalize_ticket_path()`）。**Akira 2026-09-08 裁決採甲案**：不單獨開 commit，**併入票級 `/qa → /sv` 後之收尾 commit 一併修正**；`/sv` 逐條核回鏈時 MUST 一併確認此更正已落。SMTR 不逕行改 `SKILL.md`（執行端檔案，且票級 `/qa` 前不宜由 SMTR 直接動 repo）。
- [x] 完成——commit `4bf0253`：`.claude/skills/qa/SKILL.md` 新增「## 決策核心契約」節（五項 MUST 契約：決策表 SSOT／Model A／R-4 四欄／R-7 fail-closed／並行邊界），逐項附 `file:line` 回鏈至本票 AC 位置；四項未達 AC（R-1 四型 adapter 層、多 worktree 並行、四入口實機、新增規則演練）標 `pending`／`planned` 並逐條說明；`route_v2_state`／`V2_STATE` 與 `v2_provenance=KNOWN`／`v2_semantics=FAILED` 未涵蓋兩項如實揭露亦已寫入。`git diff --stat` 僅涉 `.claude/skills/qa/SKILL.md` 一檔（實測）；`TARGET_BLOB` 未動（`47a4d120`，未觸 target script，未 refresh）

## Hard Constraints
1. **核心純度為不變量**：`preflight_core.py` 內 `subprocess`／`open(`／`os.environ` 命中 MUST 為 0（此 grep 針對純函式，保留為 AC）
2. **Model A 為裁決非可選**（R-3）
3. **禁新增對外 status enum**（R-2）；`UNLISTED_COMBINATION` 等僅為 `reason_code`
4. **禁笛卡兒積窮舉**
5. **禁 pytest、禁根 `tests/`、禁 pip install**
6. **禁等待 `INFRA-QA-ACTIVATION-01` CLOSED**；pre-image 恆為 `9895b3f`
7. **AI-5 MUST 在 AI-4 shadow 差異全數歸因後方得執行**；禁跳過 shadow 直接切換
8. **非目標**：不重寫整個 QA/SV/Traycer pipeline；不引入 workflow engine、資料庫、遠端服務或新權限平台；不改 TPL v3.8 既有狀態詞彙；不以 schema 取代 Git provenance 之業務語義；不新增未有 producer 之人工授權條件；**同一 worktree 同時寫入多張 ticket 不在承諾內，MUST 禁止或序列化**；跨 repo／跨專案 orchestration 不在 scope
9. 既有 consumer 依賴之 key 與 exit code 語義 MUST 相容（AI-5／R-5 判準）；禁止由 `final_status` 反推 exit code
10. 完成後 STOP，等待 SMTR 複審

## Error Handling
- AI-0 動作 A 發現 HEAD 上腳本異於 `9895b3f` → 停止回報升裁
- 動作 D 之決策語義測試無法映射至 rule row → 停止回報升裁
- 核心純度掃描非零 → 停止回報並回退
- decision table 出現 `v2_provenance=KNOWN`、`v2_semantics=FAILED` 或不符合控制流的 `NOT_EVALUATED` 組合 → 停止回報，禁新增 producer 解套
- AI-4 shadow 差異無法歸因 → 停止回報，**禁進 AI-5**
- shadow／adapter 任一 V0 PASS 案之 process exit code 非 0，或任一 V0 failure 案非 1 → 停止回報，**禁進 AI-5**
- 消費端需改動方能相容 → 停止回報升裁
- **[標配]** `git check-ignore -v <標的>` 命中 → 停止回報

## Acceptance Criteria
- [x] **[存在性]** 新建四檔在盤；`preflight_core.py` 內 `subprocess`／`open(`／`os.environ` 命中＝0 —— `grep -n "subprocess\|open(\|os.environ" preflight_core.py` 命中 0（`PurityTest.test_no_purity_violation_tokens_in_source` 斷言）
- [ ] **[行為性]（R-1，R3 根治；Rev D F4）** `blob_check` 為 `MISMATCH`／`TARGET_MISSING`／`COMMAND_FAILED`／`INVALID_TICKET_VALUE` 任一 → `BLOCKED_SCOPE_OR_TARGET_MISMATCH`、`write_authority=NO`、P3 writer **零呼叫**（以 `active.json` mtime 不變證）—— **未全勾**：僅 `MISMATCH` 一型於 Bash adapter 層以真實 `active.json` mtime 不變驗證；`TARGET_MISSING`／`COMMAND_FAILED`／`INVALID_TICKET_VALUE` 三型僅於 Python 核心單元層驗證（`decide()` 輸出斷言），未於 Bash adapter 全流程重跑並量測 `active.json` mtime，AC 要求之「四型皆於 adapter 層以 mtime 證」未達
- [x] **[行為性]（R-2）** 未列組合 → 對外回 `BLOCKED_ENV_MISMATCH`、`reason_code=UNLISTED_COMBINATION`、無寫入；全部輸出 `final_status` 值域 ⊆ 既有 enum 集合 —— `UnlistedCombinationTest.test_random_unlisted_combination`／`test_r1_blob_failures_all_three_variants` 斷言
- [x] **[行為性]（收斂完整性）** F-0～F-8 每列至少一個 subTest；`CANDIDATE_SUPERSEDED` F-4/F-6 各自命中；`BLOCKED_UNATTRIBUTED_STATE` F-1/F-5 各自命中；F-3/F-5 不被 V2 覆寫 —— `preflight_rules.json` 15 rows 逐一覆蓋，`test_all_rows` 全 PASS
- [x] **[行為性]（R-5 exit 相容）**...**Rev D F3：MUST 由四個具名 test method 承載（AI-3.1）** —— **已達**：AI-3.1 已補四個具名 test method（`test_exit0_v0pass_with_blocked_unattributed_state`／`test_exit0_v0pass_with_blocked_scope_or_target_mismatch`／`test_exit0_v0pass_with_candidate_superseded`／`test_exit1_v0_each_failure_value`），`python3 -m unittest test_preflight_core -v` 13 methods 全 PASS，具名承載見 AI-3.1 完成記載之逐行輸出
- [x] **[存在性]（R-6 producer closure）** facts schema／allowlist 對 V2 實際執行列僅含 `v2_provenance=UNKNOWN` 與三值 `v2_semantics`；`NOT_EVALUATED` 僅見於 V0 failure 哨兵列 —— `preflight_rules.json` 逐列人工核對＋`decide()` 之 F-6/F-7 明文要求 `v2_provenance==UNKNOWN`（見 core L243）
- [x] **[行為性]（R-6 producer closure）** 注入 `v2_provenance=KNOWN` 或 `v2_semantics=FAILED` 均命中 `UNLISTED_COMBINATION` —— `ProducerClosureTest` 兩測試方法斷言
- [x] **[行為性]（R-3，Model A）** 決策表 `YES` 列全部滿足 R-3 條件；`PARTIAL` 列全部 `NO` 且含保守 reason —— `test_all_rows` 逐列斷言 `write_authority`／`reason_code`
- [x] **[行為性]（R-4，並行安全）**...**Rev D B2：adapter 於 `ticket_id`／`repo_root`／`worktree_path`／`head_sha` 任一欄不符時依 R-7 拒絕寫入** —— **已達（AI-5.1）**：`main()` 之 `core_identity_ok` 四欄全等判定 + `test_preflight_adapter.sh` Scenario ④ 逐一注入單欄錯誤（`ticket_id`／`repo_root`／`worktree_path`／`head_sha` 各一次），均斷言 `IDENTITY_MISMATCH`、`active.json` mtime 不變、exit 1
- [ ] **[行為性]（並行 QA）** A、B 於不同 worktree 各自跑 preflight —— **未測**：本輪僅單一 worktree 單票執行，未進行多 worktree 並行實測
- [x] **[行為性]（純函式）** `os.environ` 清空、隨機 cwd、無 `.git` 下核心自 fixture facts 得相同 decision —— `PurityTest.test_decide_is_stable_under_cleared_environment_and_cwd` 真實清空環境+切換至臨時目錄後斷言 decision 相等
- [x] **[行為性]（shadow）** AI-4 差異日誌在盤，每筆差異附歸因；歸因清單為 AI-5 之前置 —— `.tmp/preflight_shadow_20260907.log` 在盤，2 筆差異已逐筆歸因（見 AI-4 完成記載）
- [ ] **[實機]** 新 session／`/resume`／顯式切票／post-compact 四入口實跑 —— **部分達成**：腳本本身僅二分（顯式切票／隱式取用 `active.json`），二分支皆有真實執行；但未透過實際 SessionStart hook（`startup`/`resume`/`compact`/`clear` matcher）觸發驗證，四個官方 matcher 名目未逐一實機測試
- [x] **[行為性]（回歸）** 決策語義測試 100% 映射 rule row；adapter integration 測試獨立清單在盤並全數 PASS —— **已達**：既有 8 條 self-test 100% 映射（見 AI-2 完成記載）；`test_preflight_adapter.sh`（AI-5.1 新建）在盤，28 項斷言全 PASS
- [x] **[行為性]（責任驗收）** Bash 分支職責歸類表在盤，零分支歸為「判定」—— **已達（AI-6 收尾，commit `5b5d294`）**：職責歸類表在盤（本檔 L400–439），15 函式＋`main()` 12 分支全歸入 `FACT_COLLECTION`／`CORE_INVOCATION`／`CORE_DIRECTED_SIDE_EFFECT`／`LEGACY_STDOUT_SHIM` 四類，**零分支歸為判定**；`route_v2_state` 歸 `LEGACY_STDOUT_SHIM` 且經證不在決策路徑（真實路徑走 `map_v2_facts()` 直讀 `V2_OVERALL`／`V2_PROVENANCE`）；`--self-test` 區塊明示不列入並附理由。SMTR 覆核 `aud_20260908_0e1808986e`。
  - **[SMTR 記載修正 2026-09-08]** 本 AC 原停留 `[ ]` 且理由「AI-6 未執行」於 AI-6 完成後已失效，屬 AI-6 收尾之波及面漏掃（O4 `EVIDENCE_GAP`，非規格缺陷）。SMTR 逕行更正勾選與理由，不開 Rev、不計 4a。
- [ ] **[行為性]（可維護性）** 「新增一條規則」演練 —— **未執行**
- [x] ~~`assembleDebug` PASS~~（N/A 顯式劃記：`.claude/` 工具鏈，零 `.kt`／`.xml`；豁免停點不豁免可見性）
- [x] **[行為性]（R-7，Rev D A1）** 四情境各自實跑...**且 legacy `write_authority()` 路徑零執行** —— **已達（AI-5.1）**：`test_preflight_adapter.sh` 四情境（python3 不可達／core script 移離／core 輸出截斷缺欄／四欄不符）逐一實跑，28 項斷言全 PASS（mtime 不變＋exit 1＋`ADVISORY_ONLY`＋對應 reason_code）；legacy fail-open 分支已自 `main()` 移除（非僅零執行，物理不存在）
  - **[SMTR 覆核 2026-09-08：降為 `[~]` 部分達成——5 子句達 3，未達 2]** 一手 `git_show 5c70987`（`aud_20260908_09c826a461`）＋ `grep`（`dddcd4f617`）：①「fail-open 分支移除」**與**「`write_authority()` 零執行」為兩件事——前者屬實，後者不成立：`write_authority` 於 L500 由 `log_shadow_diff` 呼叫，而 `log_shadow_diff` 於 R-7 路徑 `exit 1` **之前**執行，故 R-7 四情境**每次皆執行** `write_authority()`（唯讀比對用途，無寫入能力，安全上無害，但 AC 字面「零執行＋插樁/trace 證」未達）。②`final_status=BLOCKED_ENV_MISMATCH` 未出現於 stdout（腳本無 `FINAL_STATUS=` key；R-7 路徑 stdout 仍為 `V0_STATUS=PASS`＋`WRITE_AUTHORITY=NO`，R-7 簽名僅由 exit 1 承載）。③`ADVISORY_ONLY` 僅於 stderr（`>&2`），stdout 無標記。**已達三子句**：mtime 不變、exit 1、reason_code 三值。**O4 分類：`EVIDENCE_GAP`／實作完成度，非規格缺陷**——AI-6 刪除 legacy 判定函式後①自然達成；②③屬 AI-6 stdout 契約範圍。**併入 AI-6，不開 Rev，本 AC 維持 `[~]` 至 AI-6 收尾。**
  - **[AI-6 收尾：`[~]`→`[x]`，5 子句全達]** ①`write_authority()`／`legacy_equivalent_status()`／`log_shadow_diff()` 三函式與全部呼叫點已刪除（`grep -n "write_authority()\|legacy_equivalent_status\|log_shadow_diff" preflight_activation.sh` 零命中，僅餘 L507 退役註記非呼叫）＋`test_preflight_adapter.sh` 四情境重跑 28/28 PASS（無孤兒呼叫導致之執行錯誤，證零執行非僅推論）；②R-7 fail-closed 分支新增 stdout key `CORE_FINAL_STATUS=BLOCKED_ENV_MISMATCH`（新增不違「既有 key 逐項維持」）；③R-7 fail-closed 分支新增 stdout key `ADVISORY_ONLY=true`（stderr 人類可讀行同時保留，未移除既有輸出）。`test_preflight_core.py` 13/13 PASS、`test_preflight_adapter.sh` 28/28 PASS、`preflight_activation.sh --self-test` 3/3 PASS（route_v2_state 3 條；write_authority 5 條隨函式刪除同步移除，見 AI-6 sv_report SSOT 衝突揭露）
  - **[一次性票級 Cross-Review 揭露，2026-09-08，Codex R1 ADVISORY，`5fd5dac..4bf0253`]**：`invoke_core_decision()` protocol 完備性檢查（AI-4/AI-5.1 既有程式碼，非本票新增）僅驗證六欄非空，未含 `CORE_WRITE_AUTHORITY`／`CORE_TICKET_ID`／`CORE_CONTEXT_ID`——若僅 `CORE_WRITE_AUTHORITY` 缺漏、其餘欄位皆有效，仍會呼叫 P3 writer 並輸出空白 `WRITE_AUTHORITY=`。5 子句本身（本票 AI-6 實際改動範圍）不受影響仍成立；此為既有缺口之如實揭露，非本 AC 判準倒退。修補承接 `INFRA-PREFLIGHT-CORE-01-FIX-A`（`Pending/`）AI-1/AI-2。SKILL.md「決策核心契約」條款 4 已同步加註 pending 揭露。
- [x] **[存在性]（Rev D baseline refresh）** AI-W 收尾記載 refresh 前後 sha8 與對應 commit；`TARGET_BLOB` 僅更新一次；`BASE_COMMIT` 仍為 `9895b3f` —— 見 AI-W 動作 2 記載（`07640f6b`→`0f723e69`，commit `ded5dd2`），僅一次
- [x] **[存在性]** `git diff --stat` 僅涉及 ALLOWED 六檔 —— 本票自身 commit `ded5dd2`（`git diff --stat ded5dd2^..ded5dd2`）恰為 4 個 ALLOWED 檔；AI-3.1-b commit `8fb0c01` 僅 1 個 ALLOWED 檔；AI-5.1 commit `5c70987` 僅 3 個 ALLOWED 檔（`preflight_activation.sh`／`preflight_core.py`／`test_preflight_adapter.sh`）；`git status --short` 現況乾淨；`DIFF_REF=9895b3f` 至今之全歷史另含同期其他票（`INFRA-SESSION-HOOK-01` 等）之獨立 commit，非本票 scope 汙染

---

## Addendum

**Rev C（2026-09-06，CSO Gate PARTIAL 補正裁決）**：接受外審 M1／M2。① 新增 F-0～F-8 有序收斂函式，明定 `CANDIDATE_SUPERSEDED` 之 V1、V2 雙 producer、`BLOCKED_UNATTRIBUTED_STATE` 之 baseline／commit 雙 producer，以及 V1 歸因優先序；為避免把 early-return 後未執行的 facts 偽裝成既有值，同步新增具明確控制流 producer 的 `baseline_check` 與 `NOT_EVALUATED` 哨兵。② 新增 `process_exit_code`，逐字固化 frozen pre-image「僅 V0 failure exit 1，其餘 exit 0」的唯一實質 gate，禁止 adapter 從 `final_status` 反推；③ 移除無 producer 的 `v2_provenance=KNOWN`／`v2_semantics=FAILED`，將其視為 schema 外輸入並以 R-2 fail-closed；④ 補 decision-table、shadow、adapter 與 AC 的對應驗證。**原送審版 Gate＝PARTIAL；Rev C 完成票面補正後轉 READY_FOR_DISPATCH，不拆票。**

**Rev D（2026-09-07，SMTR 複審＋Akira 裁決）**：① F1 成立——移除 legacy fail-open，新增 R-7 core availability fail-closed（A1）；② F2 成立——R-4 依 B2 改四欄 identity 比對，`context_id` 降為診斷欄；③ F3——AI-3 降回部分達成，AI-3.1 補四個具名 exit-code 斷言；④ F4——R-1 補入 `MISMATCH`，並依修訂波及面全檔掃描律同步修正 AI-3／AI-4 collector 映射／AC 共四處字面；⑤ 新增 AI-W（WIP 保存＋一次性 baseline refresh），AI-5／AI-3 由 `[x]` 降回部分達成，新增 AI-5.1；⑥ AI-6／AI-7 加三項派工前置。**本票維持未收票；WIP commit 不構成 AC 通過或 ticket 收束。**

**KAIROS 候選（Rev D 收割）**：①「同一主張兩處字面不一致即為漏實作之上游成因」——F-2 含 `MISMATCH` 而 R-1 未含，執行端跟 R-1 而漏，屬開票端過失非執行端；②「fail-open 回退使新舊判定同時持有 authority，令後續『舊邏輯清除』目標不可達」——過渡設計須先裁 degraded 路徑歸屬，否則階段性交付會固化雙 SSOT；③「執行端跨票引用條文須回票面查證」——本票 HC-6 被誤引為「唯讀取證不阻斷」。

**End of INFRA-PREFLIGHT-CORE-01 (Rev D)**

---

## 收尾實錄（耐久載體＝本票檔；依四要件④，轉終態前落檔）

**碼閉環**：2026-09-08 AI-6／AI-7 收尾＋一次性票級 Cross-Review 完成，lifecycle→REVIEW；2026-09-09 家族一次性收束轉 CLOSED。

**Closing commits**：`28df403`／`5b5d294`（AI-6：刪除 Bash legacy 判定三函式、R-7 stdout key、職責歸類表）＋`4bf0253`（AI-7：`SKILL.md` 決策核心契約節）＋`de9be44`／`fb107c6`／`bc8c99e`。AI-W WIP 保存 `ded5dd2`；`TARGET_BLOB` refresh 三次（`07640f6b`→`0f723e69`→`c30ac687`→`47a4d120`），`BASE_COMMIT=9895b3f` 全程未動。

**審查鏈**：`/qa` PASS；`/sv` 一次性票級 Cross-Review，Codex R1 verdict＝**ADVISORY**（`reviews.codex` 欄枚舉不支援 ADVISORY 字面，記為 `PASS`，實際嚴重度見此處）；`structural_verify.sh --post` PASS(A–G)[ADVISORY]；receipt `.tmp/cross_review_pass/INFRA-PREFLIGHT-CORE-01`，`DIFF_SCOPE=5fd5dac..4bf0253`。DIFF_REF 執行期人工調整已揭露（`4bf0453`→`9895b3f`→`5fd5dac`，詳 `sv_report.md`）。操作事故已揭露：執行期一次意外重複觸發 `cross_review.sh`（誤入背景，已 TaskStop 終止），殘留 fail-budget state 已清除，不影響權威結果。

### Traycer 裁決：`N_A`（2026-09-09，Akira 明示）

票面 L8 原宣告「待 Akira 指定（APP 非 RAPID）」保留不刪。裁決逐字：後續安全修補與獨立 review 由 `FIX-C` 承接，母票僅記錄其作為家族收束證據，不重新開啟歷史交付範圍；**此裁決不表示母票原始 AI-0～AI-7 已具 FIX-C 後續能力**。

**承接來源之字面更正（避免假指標）**：`FIX-C` 票面所聲明之 Traycer `execute_review` 經 Akira 2026-09-09 明示**取消且作廢**，該次審查從未實跑（見 `FIX-C` checkpoint `traycer_waiver`）。實際存在之外部審查為 2026-09-09 Traycer 對本票族之**功能面終審**（`Path A — Approve`，審查基準為 repo 內四份 deliver 票檔＋六支實作／測試檔＋`SKILL.md`／`PROGRESS.md`／`sv_report.md`；無 T0、無未併批 T1）。本票之 `N_A` 係基於「歷史交付範圍不重開」之授權豁免，**非**基於任何針對母票原始交付範圍所做之獨立人工複審。

### 禁倒灌（Akira 明示硬約束）

本票四項誠實未勾 AC——`:480` R-1 四型 adapter 層驗證、`:488` 多 worktree 並行、`:491` 四入口實機、`:495` 新增規則演練——之**未勾狀態與揭露文字逐字保留**。`FIX-A`／`FIX-C` 之修補成果僅得以承接票回鏈表達，**MUST NOT** 改寫為本票已達成。續作者讀本票時，母票交付能力＝AI-0～AI-7 當時所達，不含後續修補票能力。

### 家族承接關係（收票後仍有效）

`FIX-A`（六項未閉環）→ `FIX-C`（R-1 前置 guard，根治 F-3／F-4 遮蔽）→ `FIX-B`（`SKILL.md` truthful resync）。四票均已於 2026-09-09 達終態。

### 收票時仍在盤之已知未達項（Traycer 終審亦列為非阻擋）

多 worktree 僅證明 identity 可分辨、非並行安全已證明；四入口實機 hook 觸發仍 `planned`；「新增一條規則」可維護性演練仍 `planned`；`test_preflight_adapter.sh` 仍改寫真實票檔後還原（有 byte-identical 斷言但中途中止較 fixture 式脆弱，KAIROS 候選）。

### 已知文件債（不阻擋，本輪不修）

`SKILL.md` 對本票之行號回鏈整體漂移：AC 類 **+5**（`:475`／`:483`／`:486`／`:490` 實際為 `:480`／`:488`／`:491`／`:495`），`route_v2_state` 職責歸類 **+1**（`:413` 實際為 `:414`）。成因為本票面歷次註記插入造成之疊加位移，逐次修正無效。KAIROS 候選：回鏈改錨定字串或 AC 編號，禁用行號。
