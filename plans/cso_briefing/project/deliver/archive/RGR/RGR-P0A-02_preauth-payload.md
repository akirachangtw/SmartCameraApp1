# RGR-P0A-02: 以票面預授權承載自主 apply，解除自報單獨構成停點

檔案治理／來源說明：本票依據 2026-09-19 本機 live scan（`repair_runner.sh` L137／L202／L384–388／L425–428／L507–528／L550–558／L606–612／L629／L722–726／L469–500 區 RTR-005 二守門、`schemas/ticket-repair-payload.v1.json` 全檔無 `additionalProperties`）、`REPAIR_ROUTING.md` L38–55 `Dispatch Authority` 節、`OPS_CALIBRATION.md` L15 Phase 0 例外句建立，依 `Unified_Ticket_Template_v3.md` v3.11 產出。

**簽發日期**: 2026-09-19
**簽發者**: Claude（SKAD）
**來源議題**: RGR
**Mode**: normal  ||  **Effort**: Low
**Traycer Workflow**: N/A
**前置依賴（依賴三分）**：
- `execution_dependency`: `RGR-REPORT-ANCHOR-01`（`780b079`，CWD guard 已落地——預授權之 receipt 與 breaker 歷史須有不受 CWD 影響之穩定落點）、`RGR-DISPATCH-AUTH-01`（`REPAIR_ROUTING.md` L38 `Dispatch Authority` 節已落地——本票之欄位語意以該節六項條件為唯一法源）
- `evidence_dependency`: `RGR-CODEX-TRANSPORT-01` post-fix run（`thread=01a0adec-b8e8-78c2-ac69-18cf52ad6694`）證實 `addendum_only` 自主 apply 閉環可行；`RGR-MODEL-CONTRACT-01`（`ab23c62`）確立「自報不承載授權語意」之處置先例
- `coordination_dependency`: 無

## 問題陳述

`REPAIR_ROUTING.md` `Dispatch Authority` 節已立法「六項放行條件全真 → Host MUST 放行、禁逐案諮詢」，但**payload 無欄位承載該授權**，runner 亦無消費點。缺口具體落在兩處：

1. **無承載**：payload v1 無任何欄位可宣告「本 finding 已預授權」及「required verification」。
2. **自報單獨成停點**：`repair_runner.sh` L611–612 之 **RTR-012**——`requires_human_decision == "true" && disposition == "propose_repair"` 即拒絕並 `return 12`。`requires_human_decision`（L607，`jq -r '.requires_human_decision // false'`）為**模型自報**欄位。預授權命中時仍會被此路徑攔下，與 `Dispatch Authority` 節「Self-report carries no authority」字面直接衝突。

**v1 相容性已確認**：`ticket-repair-payload.v1.json` 全檔無 `additionalProperties`（JSON Schema 預設 `true`），runner 之欄位驗證為手寫 `jq` 缺欄清單（L202 型式，只列缺漏、不拒未知欄）。故兩個 optional 欄位可直接加入 v1，**不建 v2**，既有 `addendum_only` fixture 零影響。

## 範圍聲明（明禁事項）

本票**不做**：五態 `dispatch_status`、evidence class 分類、closed-set stop reason 集、finding routing 欄位表、rerun matrix、schema v2、batch orchestration、`report_patch`／`anchor_refresh`／test patch／production code patch 等新 write mode、CB／RTR 判定或 `RTR_RETRYABLE` 之修改、Phase 0/1 門檻數值或拒絕歸零規則之調整。上列分類學為 KAIROS 候選，經三輪外審一致延後。

## Verify Config
TICKET_ID=RGR-P0A-02
CREATED=2026-09-19
PRESET=feature
ALLOWED=.claude/skills/ticket-rigor-gate/runner/schemas/ticket-repair-payload.v1.json,.claude/skills/ticket-rigor-gate/runner/repair_runner.sh,.claude/skills/ticket-rigor-gate/runner/fixtures/
FORBIDDEN=.claude/skills/ticket-rigor-gate/runner/schemas/ticket-repair-response.v1.json,.claude/skills/ticket-rigor-gate/runner/prompts/,.claude/skills/ticket-rigor-gate/runner/profiles/,.claude/skills/ticket-rigor-gate/runner/housekeeping.sh,.claude/skills/ticket-rigor-gate/SKILL.md,.claude/skills/ticket-rigor-gate/REPAIR_ROUTING.md,.claude/skills/ticket-rigor-gate/OPS_CALIBRATION.md,.claude/rules/ssot-governance.md,.claude/PROGRESS.md,app/,plans/cso_briefing/project/deliver/
MAX_LINES=N/A
DIFF_REF=bb119b74bf3a3962a63459ff3dad38d37ffcd1e4
ANCHOR_PROBED=2026-09-19（AI-0 MUST 以 live read 復驗並回填實際時戳）
BASE_COMMIT=bb119b74bf3a3962a63459ff3dad38d37ffcd1e4
TARGET_BLOB=N/A
INTRODUCED_CAPABILITY=feature:preauthorized_autonomous_apply
DEPENDENCY_DELTA=NONE

**`MAX_LINES=N/A` 依據**：TPL v3.5 R3——標的觸及 fail-closed 守門（RTR-012）之放行條件，屬安全不變量類。
**`TARGET_BLOB=N/A` 依據**：TPL v3.11 Desktop fallback（開票端無 blob-hash producer）；`BASE_COMMIT` 已填，AI-0 MUST 逐項復驗。
**`INTRODUCED_CAPABILITY=feature:preauthorized_autonomous_apply` 依據**：本票新增一項**受限 capability**——runner 可消費票面 `preauthorized_autonomous_apply`。當該旗標為 `true` 且 `required_verification` 為非空 immutable reference 時，模型自報之 `requires_human_decision` **不得單獨**造成 RTR-012 終止；所有既有機械 guard 仍在**原時序、原位置、原語意**下完整執行，並獨立決定 apply 或 reject。此 capability **不**新增 write mode、**不**擴張 scope、**不**改變 CB／RTR 表、Phase 門檻或模型權限。「欄位可相容擴充」與「系統行為無新能力」是兩件事——本票確實改變一條真實 dispatch path，故如實標示為 feature capability，不由執行端臨場重判 6a。

**執行模型（兩階段語意，票面唯一權威表述）**：
1. **授權層**：票面發出預授權且 reference 存在 → 模型自報不具 veto 權。
2. **執行層**：runner 之既有 guard 在實際寫入前完成所有可機械化約束。

本票**不**主張「runner 於 RTR-012 前已完整判明六條件」。**現場核實（Rigor Gate 更正）**：主迴圈實際順序為條件③（`target.old_hash` preimage，`validate_target`）與條件④（mutation shape，`validate_diff_budget`／`validate_frozen_sentence`）之既有 guard **先於** RTR-012（`validate_decision_leakage`）執行——RTR-012 判定當下，③④早已通過驗證，並非「尚未產生」。`preauthorized_autonomous_apply` **不是 bypass token**：它不需要、也不會觸及已經跑完的③④，僅使 `requires_human_decision` 不再單獨擁有終止權；能否實際 apply，仍完全由既有機械驗證鏈在其原位置決定。若日後要讓 runner 於 RTR-012 前即認定六條件全真，須將各 guard 結果改造為可重用狀態或重構控制流——那是另一次有實質 blast radius 的 runner architecture work，不在本票範圍。

## Action Items

### AI-0: 錨點復驗與 scope 預檢
- **動作 A（schema 相容性）**: 確認 `ticket-repair-payload.v1.json` 全檔無 `additionalProperties`；確認 runner 之 payload 欄位驗證為手寫 `jq` 缺欄清單（L202 附近）且不拒未知欄。任一不符 → 停止回報，改判是否需 v2。
- **動作 B（RTR-012 現值）**: 逐字確認 L606–612：`local requires_human disposition replacement marker`／`requires_human="$(jq -r '.requires_human_decision // false' <<<"$finding_result")"`／`disposition="$(jq -r '.disposition' <<<"$finding_result")"`／`if [[ "$requires_human" == "true" && "$disposition" == "propose_repair" ]]; then`／`echo "❌ RTR-012: …（自相矛盾）" >&2`。回填實際行號。
- **動作 C（不動點定位）**: 回填下列之行號並確認本票後皆未改：`blocker_preflight()` 之 `write_mode` 檢查（L384–388）、`validate_response_structure` 之 RTR-005 二守門（`ticket_id` 比對與非空 `finding_results`）、RTR-002（L516–517）、RTR-007（L528）、RTR-009（L557–558）、`apply_patch()`（L629）、主迴圈非 `propose_repair` → `no_write`（L725–726）、CB-1～CB-4 區、`RTR_RETRYABLE` 表。
- **動作 D（fixture 現況）**: 列出 `fixtures/true_call/` 與 `fixtures/` 下現有檔案；確認既有 `payload_addendum.json`、`payload_postfix.json`、`run_no_call_tests.sh` 與 no-call 五案 fixture 皆存在。
- **動作 E（worktree 與 scope）**: `git rev-parse HEAD`、`git status --short`（判定範圍限本票 ALLOWED 三路徑；他檔 dirty 僅記錄不阻擋）、`git check-ignore -v` 各 ALLOWED 檔（命中 → 停止）。
- **動作 F（法源復驗）**: 唯讀 `REPAIR_ROUTING.md` `Dispatch Authority` 節，逐字抄錄其六項放行條件至本票執行記錄。
- **動作 G（guard coverage 與時序）**: 確認 RTR-012 所在函式、主迴圈、`apply_patch()` 呼叫點與各既有 guard 之實際順序。逐項定位六條件所對應之**既有** guard：①authority anchor／evidence 可讀性；②target scope；③`target.old_hash`／preimage；④`write_mode`／`max_lines`／`frozen_terms` 與既有 mutation-shape guard；⑤`required_verification` 之存在性與 non-empty 檢查（**本票唯一新增，純存在性**）；⑥CB／RTR／contract block。**確認除⑤外，①②③④⑥均仍由既有 guard 在實際 target mutation 之前覆蓋**；確認 AI-2 不會搬移、重跑、複製、平行實作或降低任一 guard。**任一條件在 AI-2 之後不再於 target mutation 前被既有機械路徑覆蓋 → 停止回報**；不得以預授權旗標補償、繞過或自行新增替代驗證。回填各 guard 行號與其相對於 `apply_patch()` 之序位。
  **條件②之特別處置（Rigor Gate 發現，MUST 處理）**：`blocker_preflight()` 現行之 `IRREVERSIBLE_TARGET_PATTERNS`（`app/src/*`／`*.gradle*`／`gradle/*`／`.github/*`／`*migration*/*`）為與特定票無關之通用危險路徑黑名單，**非**「target 是否屬簽發本票宣告範圍」之逐票比對；`ticket-repair-payload.v1.json` 本身亦無欄位承載簽發票之 ALLOWED scope 供比對。AI-0 **MUST** 二擇一並如實記錄於執行記錄，禁逕自回填一個語意不符的行號蒙混過關：(a) 主張條件②由「payload 只會為簽發票自己宣告之目標而建構」之**結構性信任**滿足，非 runner 內之獨立動態檢查，並明白寫出這是結構性論證而非既有 guard 行號引用；或 (b) 判定此結構性論證不成立 → 依本節既有停止條件停止回報，改由後續票補一個明確的 scope 比對機制。
- [x] 完成（執行記錄：動作 A-G 全數核實，行號見對話回報；動作 G 條件②既非 (a) 亦非 (b)——Akira 2026-09-19 裁決改採第三案「Fixture-only boundary」：override 能力本身收窄至 `target.file_path` 經 `realpath -m` 解析後位於 `fixtures/` root 之下方得生效，而非放行任意宣告 scope；此舉使「target=另一張已簽發票本文」之風險在 runner 層面技術上不可達（票務 `.md` 從不落於 fixtures root），故一般化逐票 scope 比對機制之需求暫不成立，非結構性信任亦非停止回報）

### AI-1: payload v1 加兩個 optional 欄位
- **檔案**: `.claude/skills/ticket-rigor-gate/runner/schemas/ticket-repair-payload.v1.json`
- **目標值**: 於既有 schema 加入兩個 **optional**（不入任何 `required` 陣列）欄位：
  - `preauthorized_autonomous_apply`：boolean，預設語意為 `false`（缺欄即 `false`）
  - `required_verification`：`type: string`、`minLength: 1`，optional。**它是 immutable verification reference——不是 shell command、不是自由文字指令、不是 runner 任意執行入口。** AI-2 僅於預授權旗標為 `true` 時檢查其存在且非空；**僅於該旗標實際觸發 RTR-012 override 分支時**（見 AI-2 目標值第 3 點，即模型自報 `requires_human_decision=true` 且 `disposition=propose_repair`），才將該 reference 隨 `requires_human_decision` 一併留存於 receipt／Notes 之 audit 資料——若旗標為 `true` 但模型自報 `requires_human_decision=false`（override 分支未觸發，因 RTR-012 本不會因此欄位攔截），**不**另外記錄一筆 audit 資料，此為刻意之最小化設計，非遺漏。**本票不執行其內容**。為 `required_verification` 引入 command execution contract、allowlist、quoting 或 exit-code 語意，屬完全不同的系統設計，不在本票範圍。
- **不動點**: 既有 properties、既有 `required` 陣列、既有型別與 enum、`$schema`／`$id`／version 字串——逐字不改。**不得**新增 `additionalProperties`（新增即破壞既有相容性假設）。
- **禁**: 不建 v2；不動 `ticket-repair-response.v1.json`（FORBIDDEN）。
- [x] 完成（commit `568d139`；`preauthorized_autonomous_apply`／`required_verification` 兩個 optional 欄位加於 L89-90，`required` 陣列與 `additionalProperties` 逐字未動，`payload_addendum.json` 回歸驗證缺欄清單為空）

### AI-2: runner 消費預授權，RTR-012 於命中時不觸發
- **檔案**: `.claude/skills/ticket-rigor-gate/runner/repair_runner.sh`
- **現值**: L611–612 之 RTR-012 為 `requires_human_decision`（模型自報）與 `disposition` 之組合判定，無任何預授權旁路。
- **目標值**:
  1. 自 payload 讀取 `preauthorized_autonomous_apply`（缺欄 → `false`）。
  2. 自 payload 讀取 `required_verification`：其為 immutable reference，**MUST NOT** 執行、`eval`、傳入 shell 或作為任意命令。當 `preauthorized_autonomous_apply=true` 時，`required_verification` 必須存在且非空；缺失或空值 → `preauthorization_override=false`。
  3. **於 RTR-012 原判定處**：若 `requires_human_decision=true` 且 `disposition=propose_repair`，且 `preauthorized_autonomous_apply=true`，且 `required_verification` 為非空 immutable reference → **不得因該模型自報單獨 `return 12`**；將 `requires_human_decision` 與 `required_verification` 寫入既有 receipt／Notes 之 audit 資料，並繼續既有處理流程。
  4. 上述情況以外，RTR-012 行為**逐字維持原狀**。
     **override 之作用面（2026-09-19 收緊，`EVIDENCE_GAP`）**：`validate_decision_leakage()` 內有**兩條**同以 `RTR-012` 為失敗碼、但語意不同之拒絕路徑——①`requires_human_decision=true` 與 `disposition=propose_repair` 之**自報矛盾**分支；②`FROZEN_LEAKAGE_MARKERS` 對 `replacement_text` 之**既有 marker 掃描**。預授權 override **僅**抑制①。②**必須**保留在原位置並照常執行；override **MUST NOT** 以 `return`、`continue`、`break` 或任何控制流使該迴圈不可達。實作形式：override 命中時**僅**設 `PREAUTH_OVERRIDE_NOTE`，**不得**在該處 `return 0`；以 `else` 承載原拒絕分支，令控制流自然落入 marker loop 與函式末尾之唯一成功 `return 0`。
  6. **Fixture-only boundary（2026-09-19 Akira 裁決，`EVIDENCE_GAP` 收緊）**：`preauthorized_autonomous_apply` 之 RTR-012 override **僅可**在 `target.file_path` 經 `realpath -m` 解析後位於 canonical fixture root 之下時生效。
     - fixture root：`$REPO_ROOT/.claude/skills/ticket-rigor-gate/runner/fixtures/`，比較時**必須帶末端目錄分隔符**
     - 判定形式（重用既有原語，非新引入 resolver——`realpath -m --` 已用於 L224 `report_path_abs`）：
       ```bash
       FIXTURE_ROOT="$(realpath -m -- "$REPO_ROOT/.claude/skills/ticket-rigor-gate/runner/fixtures")/"
       target_abs="$(realpath -m -- "$TARGET_FILE")"
       [[ "$target_abs" == "$FIXTURE_ROOT"* ]]
       ```
     - `TARGET_FILE` 若為相對路徑，**MUST** 於與既有 apply path **相同之 repo-root context** 下 canonicalize（`$PWD == $REPO_ROOT` 已由 `RGR-REPORT-ANCHOR-01` L108 guard 固定）。**MUST NOT** 改變工作目錄、**MUST NOT** 自行 `cd`、**MUST NOT** 引入 fallback。
     - **fail-closed**：`target_abs` 不可得、`TARGET_FILE` 為空、`realpath` 失敗，或解析結果不屬於 fixture root → `override=false`，RTR-012 **維持既有行為並 `return 12`**。
     - **MUST NOT** 採裸 substring、`contains`、`grep fixtures`、未 canonicalize 之相對路徑、symlink 可繞過之比對，或對一般 `.md` 路徑放寬（`fixtures-old/`、`my-fixtures/` 等相近前綴必須被排除）。
     - target 位於 fixture root 之外時，**即使** `preauthorized_autonomous_apply=true`、`required_verification` 非空、`requires_human_decision=true`、`disposition=propose_repair` 四者全部成立，RTR-012 仍**逐字維持既有行為**。**MUST NOT** 以 prefix 相近、symlink、路徑正規化歧義或 host 宣告替代此限制。
  5. **preauthorization override 不構成任何既有 guard 之 bypass**：evidence、scope、`target.old_hash`、`write_mode`、`max_lines`、`frozen_terms`、diff budget、frozen sentence、decision leakage、RTR-002／005／007／009、CB-1～CB-4、`RTR_RETRYABLE` 與 `apply_patch()` 皆須在**原順序、原位置、原語意**下繼續執行。**MUST NOT** 重跑、複製、搬移或新寫平行檢查。本 AI **不需要**、亦**不得**嘗試在 RTR-012 前判定六條件全真；真正的保護是每個既有 guard 仍在 mutation 前發揮效力。
- **不動點**: RTR-002／RTR-005（二守門）／RTR-007／RTR-009、`blocker_preflight()`、`apply_patch()` 本體、CB-1～CB-4、`RTR_RETRYABLE` 表、非 `propose_repair` → `no_write`（L725–726）、report parser 與歷史格式——零改動。
- **禁**: 不得新增 CWD fallback；不得以 `preauthorized_autonomous_apply` 旁路 scope、preimage、evidence、diff budget、frozen sentence、decision leakage 或任一 breaker——該旗標**只解除自報型停點，不解除任何機械驗證**。
- [x] 完成（commit `b172441`；`EVIDENCE_GAP` 收緊之 marker-loop 可達性與 fixture-only boundary 均已落地並經 sanity check 驗證）

### AI-3: fixture 驗收（deterministic stub 為主）
- **檔案**: `.claude/skills/ticket-rigor-gate/runner/fixtures/`
- **動作**: 建立 fresh fixture 情境——**fresh `ticket_id`**（不重用 `RGR-P0A-01-FIXTURE` 或 `-POSTFIX`，避免 CB-1 歷史聚合）、**fresh canonical report**、以 deterministic `codex.cmd` stub 產出固定 response。驗證四案：
  1. **成功案例**：`preauthorized_autonomous_apply=true`、`required_verification` 為非空 immutable fixture reference、stub response `requires_human_decision=true` 且 `disposition=propose_repair`、後續所有既有 guard 均通過 → RTR-012 不因 self-report `return 12`；所有既有 guard 照常執行；`addendum_only` apply 成功；`Apply Status: applied`；Notes 同時含 `PREAUTHORIZED_AUTONOMOUS_APPLY=true`、`REQUIRES_HUMAN_SELF_REPORT=true`、`REQUIRED_VERIFICATION_REF=<fixture reference>`；`HUMAN_CHECKPOINT_EMITTED=false`
  2. **preimage 不符**：`target.old_hash` guard（`validate_target`，主迴圈 L749）**先於** RTR-012（`validate_decision_leakage`，唯一呼叫點 L759）執行，於**原位置**拒絕，RTR-012 自報分支從未被觸及；零 target mutation。（本案為第四項修訂之核心證明：預授權旗標不使 preimage 驗證被跳過、延後或弱化。時序敘述於 2026-09-20 依 F1 `EVIDENCE_GAP` 校正，與 §執行模型之 Rigor Gate 更正一致）
  3. **reference 缺失或空值**：`preauthorized_autonomous_apply=true`、`required_verification` 缺失或空字串、response `requires_human_decision=true` 且 `disposition=propose_repair` → `preauthorization_override=false`；RTR-012 維持原行為並 `return 12`；零 target mutation。**另以極小變體確認**：若 response `requires_human_decision=false`，即使 `required_verification` 缺失，**亦不得人為觸發 RTR-012**，流程維持本票前原行為
  4. **舊 payload regression**：既有 `payload_addendum.json`（無預授權欄位）→ runner 行為與本票前一致；不出現 `PREAUTHORIZED_AUTONOMOUS_APPLY` audit note；不新增任何 apply opportunity
  6. **preauthorized fixture with frozen leakage marker**：target 在 canonical fixture root 內、`preauthorized_autonomous_apply=true`、`required_verification` 為非空 immutable fixture reference、stub response `requires_human_decision=true`、`disposition=propose_repair`，且 `replacement_text` **含任一** `FROZEN_LEAKAGE_MARKERS` marker → 自報矛盾分支不單獨 `return 12`；**marker loop 照常掃描**並依既有 decision-leakage 路徑 `return 12`；零 target mutation；**無** `Apply Status: applied`；`HUMAN_CHECKPOINT_EMITTED=false` **不得**被宣告為成功結果；`PREAUTH_OVERRIDE_NOTE` **不得**寫入成功 receipt（既有 failure record 照常留存）；stub 呼叫計數維持預期值且零真實 Codex 呼叫
  5. **non-fixture target negative case**：`target.file_path` 指向 fixture root 之外（deterministic stub），且 `preauthorized_autonomous_apply=true`、`required_verification` 非空、stub response `requires_human_decision=true`、`disposition=propose_repair` → boundary 判定 `false`、override 不生效、RTR-012 依原行為 `return 12`、**零 target mutation**。此案為 fixture-only 限制之唯一正向證明，**不得省略**。
     **5b. sibling-prefix negative sanity assertion（必附）**：另以與 fixture root **同層、名稱含 `fixtures` 之 sibling path**（如 `.../runner/fixtures-old/…` 或 `.../runner/my-fixtures/…`）為 target，其餘四項 override 輸入全真 → assert override **不可達**。此案為「帶末端分隔符之 prefix 判定」與「裸 `contains` matching」之**唯一區辨證明**：裸 `contains` 會誤判 sibling 為命中，帶分隔符之 prefix 不會。完整 symlink suite 成本高一個量級而僅多覆蓋邊角，**本票不做**；本 sanity assertion 即為該取捨之替代證據。回流 MUST 附 canonical resolved path、fixture root 值與 reject 證據。
- **另須**: 既有 no-call 五案 `run_no_call_tests.sh` 維持 `PASS=5 FAIL=0`（含 `cb4_not_latched_on_rerun`）；`bash -n repair_runner.sh` exit 0。
- **禁**: 不得發出真實 Codex 呼叫；不得 mutate `fixtures/true_call/` 以外之任何 target；不得為使測試通過而調整既有 fixture 之斷言。
- [x] 完成（commit `0b8fa8d`；新建 `fixtures/p0a02/` 自足樹，六案+3b/5b 全數 PASS，`run_no_call_tests.sh` PASS=5 FAIL=0，`bash -n`／`git diff --check` exit 0；執行中發現並於 fixtures/** 範圍內解決 Windows `.cmd` 派發問題，未觸碰 runner）

## Hard Constraints
1. `preauthorized_autonomous_apply` **只使模型自報不再單獨擁有終止權（RTR-012 之自報矛盾分支）**，**MUST NOT** 旁路 scope、preimage、evidence 可讀性、mutation shape、diff budget、frozen sentence、decision leakage、RTR-005 二守門或任一 breaker。它**不是 bypass token**。
   其中 decision leakage **包含** `validate_decision_leakage()` 內 `FROZEN_LEAKAGE_MARKERS` 對 `replacement_text` 之既有 marker 掃描；預授權 **MUST NOT** 使該迴圈、其失敗路徑或其於 mutation 前之覆蓋失效。`RTR-012` 為一碼兩用，解除其一不得連帶解除其二。
2. **MUST NOT** 為判定六條件而新寫、重跑、複製或搬移任何既有 guard；AI-2 之改動限於 RTR-012 判定處與 audit 記錄。六條件之實際強制由既有 guard 在其**原位置**完成，本票僅新增第⑤項（`required_verification` 存在性）。
2a. `required_verification` **MUST NOT** 被執行、`eval`、傳入 shell 或作為任意命令；它是 immutable reference，僅供存在性檢查與 receipt 留存。
3. **MUST NOT** 建 payload v2；**MUST NOT** 於 v1 新增 `additionalProperties`；**MUST NOT** 改動既有 `required` 陣列。
4. **MUST NOT** 擴充 write mode——`addendum_only` 維持唯一支援值，RTR-009 零改動。
5. **MUST NOT** 修改 CB-1～CB-4 判定、failure-count predicate、`RTR_RETRYABLE` 表、report parser 或 report 歷史。
6. **MUST NOT** 調整 Phase 0/1 門檻數值或拒絕歸零規則；預授權之 apply 結果**仍計入**該 class 之 calibration counter（`OPS_CALIBRATION.md` L15 已立法）。
7. **MUST NOT** 引入五態 dispatch status、evidence class、closed-set stop reason 集、finding routing 欄位表或 rerun matrix。
8. 驗收 **MUST NOT** 發出真實 Codex 呼叫；fixture 一律 deterministic stub。
9. `preauthorized_autonomous_apply` 之 RTR-012 override **僅限 canonical fixture-root target**。`RGR-SCOPE-BIND-01` 之交付與獨立驗收**不會自動解除**此限制。解除 fixture-only 限制**必須**由另一張明示、已簽發之 follow-up ticket 定義 scope-binding contract、驗收 non-fixture 之 negative／positive cases，並明確授權該解除。**MUST NOT** 將「機械 scope binding 已修好」誤讀為「已獲准擴大自主 apply 範圍」。

## Error Handling
- AI-0 動作 A 顯示 schema 或 parser 會拒未知欄 → 停止回報，重判 v2 必要性，禁逕自建 v2
- AI-0 動作 B 之 RTR-012 現值與票面不符 → 停止回報，禁推定等價位置
- AI-0 動作 F 抄錄之六項條件與本票 AI-2 第 2 點不一致 → 停止回報，以 `REPAIR_ROUTING.md` 為準，禁自行調和
- AI-0 動作 G 顯示任一條件（除⑤外）在 AI-2 後不再於 target mutation 前被既有 guard 覆蓋 → 停止回報；不得以預授權旗標補償或自行新增替代驗證
- 實作需在 RTR-012 前新寫、重跑、複製或搬移任何既有 guard → 停止回報（本票設計明示不需要如此）
- AI-0／AI-2 無法安全取得 canonical target path、無法確認 fixture root boundary，或既有 path handling 不足以區分 fixture 與非 fixture target → 停止回報；**MUST NOT** 退化為 substring、`contains`、未 canonicalize 之相對路徑、host 宣告或 payload 自報替代 fixture-only boundary
- 任一既有守門（RTR-002／005／007／009、CB、blocker_preflight）行為因本票而改變 → 停止回報
- no-call 五案任一由 PASS 轉 FAIL → 停止回報，禁調整 fixture 斷言
- 驗收需發出真實 Codex 呼叫或 mutate 非 fixture target → 停止回報
- 執行環境（auto 模式 Self-Modification 分類器）拒絕寫入 `.claude/skills/` → 改互動核准；仍拒絕 → 停止回報，**MUST NOT** 以第二通道繞過、**MUST NOT** 新增廣泛 allow rule
- **[標配]** `git check-ignore -v` 命中任一 ALLOWED 檔 → 停止回報

## Acceptance Criteria
- [x] **[存在性]** `ticket-repair-payload.v1.json` 含 `preauthorized_autonomous_apply` 與 `required_verification` 二欄，均**不在**任何 `required` 陣列中；既有 properties／`required`／`$id`／version 逐字未改；未新增 `additionalProperties`（行號：`preauthorized_autonomous_apply` L89、`required_verification` L90）
- [x] **[可達性]** runner 消費該二欄之行號與 RTR-012 判定行號可定位（`preauth_flag=` L613、`verification_ref=` L614；RTR-012 函式定義 L604、唯一呼叫點 L759）
- [x] **[存在性]** **未新增**任何平行檢查——AI-2 之改動僅及 RTR-012 判定處與 audit 記錄；scoped diff 確認 `validate_eligibility`／`validate_evidence`／`validate_target`／`validate_diff_budget`／`validate_frozen_sentence` 零改動，未出現新的 scope／hash／evidence／mutation-shape 檢查實作，亦未搬移或重跑既有 guard
- [x] **[存在性]** 於 `preauthorized_autonomous_apply=true` 且 `required_verification` 非空時，RTR-012 **僅**不因模型 self-report 單獨 `return 12`；target mutation 前**所有**既有 guard 仍在原順序覆蓋 evidence、scope、preimage、mutation shape、CB／RTR——主迴圈實測序位（`apply_patch()` L777 之前）：`circuit_breaker_precheck()` L694（條件⑥，run 級前置）→ `blocker_preflight()` L698（含 evidence 可讀性①、write_mode②通用檢查）→ `validate_eligibility` L746(RTR-002) → `validate_evidence` L747(RTR-007，條件①) → `validate_target` L749(RTR-008/009，**條件③ preimage**) → `validate_diff_budget` L757(RTR-010) → `validate_frozen_sentence` L758(RTR-011，**條件④**) → `validate_decision_leakage` L759(RTR-012) → `apply_patch()` L777
- [x] **[安全性]** scoped diff 與逐行人工核對（grep 呼叫遭 Auto Mode 分類器攔截，改用視覺核對）確認未引入 `eval`、`bash -c`、`source`、command substitution 或任何把 `required_verification` 作為可執行內容之路徑——僅出現於存在性判定（L615）與字串賦值（L624）
- [x] **[行為性]** Given `target.file_path` 位於 canonical fixture root 之外，且 `preauthorized_autonomous_apply=true`、`required_verification` 非空、`requires_human_decision=true`、`disposition=propose_repair`，expect override **不生效**、RTR-012 依原行為 `return 12`、零 target mutation — Case 5（`fixtures/p0a02/payloads/payload_c5.json`，target=`.tmp/p0a02_ai3_case5_outside_fixture_target.md`）harness `c5_boundary_outside_fixture_root_fail_closed` PASS，target 從未被建立
- [x] **[行為性]** Given target 位於 fixture root、`preauthorized_autonomous_apply=true`、`required_verification` 非空、`requires_human_decision=true`、`disposition=propose_repair`，且 `replacement_text` 含任一 `FROZEN_LEAKAGE_MARKERS`，expect 自報矛盾分支不單獨 `return 12`、marker scan **仍被執行**、函式依既有 decision-leakage 路徑 `return 12`、零 target mutation、不產生 `Apply Status: applied`、`PREAUTH_OVERRIDE_NOTE` 不寫入成功 receipt — Case 6（`payload_c6.json`，replacement_text 含 "new policy"）harness `c6_override_hit_marker_reject_no_leak` PASS，report 兩筆記錄（rejected+escalated）均不含四項 audit 欄，完整 record 已存執行記錄
- [x] **[可達性]** reachability 證明：override 分支之後**無** `return`／`continue`／`break`；`FROZEN_LEAKAGE_MARKERS` 迴圈於 override 命中時**仍可達**（override 分支起點 L623；`then` 分支〔L624 賦值〕無 `return`，`else` 分支〔L626-627〕方有 `return 12`；外層 `if` 於 L629 以 `fi` 收尾後無條件落入 L631 marker loop；函式末尾唯一成功 `return 0` 於 L637。序關係：L623→L624/626→L629→L631→L637，靜態結構確認無分支可跳過 marker loop）
- [x] **[行為性]** Given target 為與 fixture root 同層、名稱含 `fixtures` 之 sibling path（`fixtures-old/`／`my-fixtures/` 等），且其餘四項 override 輸入全真，expect override **不可達**、RTR-012 依原行為 `return 12`、零 target mutation。此為帶末端分隔符 prefix 判定 vs 裸 `contains` matching 之唯一區辨證明 — Case 5b（`payload_c5b.json`，target=`.claude/skills/ticket-rigor-gate/runner/fixtures-old/sibling_target.md`）harness `c5b_sibling_prefix_boundary_rejected` PASS；fixture root 值＝`$REPO_ROOT/.claude/skills/ticket-rigor-gate/runner/fixtures/`（帶末端 `/`），canonical resolved target 不具此 prefix（`fixtures-old` 於字元 9 即與 `fixtures/` 分岔），`fixtures-old/` 目錄本身從未被建立
- [x] **[存在性]** fixture-only 判定使用 runner 既有 canonical／resolved target path 表示（`realpath -m --`，同既有型式）與帶末端分隔符之 fixture-root 前綴比對；scoped diff 確認**未**引入裸 substring／`contains` matching、symlink bypass、未 canonicalize 之相對路徑歧義，亦未對一般 `.md` 路徑放寬（判定行號 L619-620；fixture root 常數行號 L617；`target_abs` 賦值 L618）
- [x] **[行為性]** Given 預授權命中、`required_verification` 非空、stub response `requires_human_decision=true` 且 `disposition=propose_repair`、後續既有 guard 全過，expect 不觸 RTR-012、apply 成功、`Apply Status: applied`、Notes 含 `PREAUTHORIZED_AUTONOMOUS_APPLY=true`／`REQUIRES_HUMAN_SELF_REPORT=true`／`REQUIRED_VERIFICATION_REF=<ref>`、`HUMAN_CHECKPOINT_EMITTED=false` — Case 1（`payload_c1.json`）harness `c1_success_applied` PASS，四欄逐一 grep 確認齊備，addendum mutation 出現於 `target_c1.md`
- [x] **[行為性]** Given 預授權命中但 `target.old_hash` 與實際 preimage 不符，expect RTR-012 不單獨終止、但 preimage guard（`validate_target` L749，**先於** RTR-012 呼叫點 L759）於原位置拒絕、零 target mutation — Case 2（`payload_c2.json`，`expected_old_text_hash` 為刻意過期值）harness `c2_preimage_mismatch_rejected_before_rtr012` PASS；report 顯示 `Failure Code: RTR-008`，stderr／report 均未見任何 `decision leakage` 字樣，證實 `validate_target()` 先於 `validate_decision_leakage()` 攔截，RTR-012 從未被呼叫
- [x] **[行為性]** Given `preauthorized_autonomous_apply=true` 但 `required_verification` 缺或為空、且 response `requires_human_decision=true`／`disposition=propose_repair`，expect `preauthorization_override=false`、RTR-012 原行為 `return 12`、零 target mutation；**另**：response `requires_human_decision=false` 且 reference 缺失時，expect **不**觸發 RTR-012，行為同本票前 — Case 3（`payload_c3.json`，無 `required_verification`，self-report=true）harness `c3_ref_missing_selfreport_true_override_false` PASS；Case 3b（`payload_c3b.json`，同樣缺 ref，self-report=false）harness `c3b_ref_missing_selfreport_false_not_triggered` PASS，apply 成功且無 RTR-012
- [x] **[行為性]** Given payload 未宣告預授權（缺欄），expect 全流程與本票前逐字相同——以既有 `payload_addendum.json` 之 v1 相容回歸確認 — Case 4（`payload_c4.json`，legacy v1 形制、fresh ticket_id）harness `c4_legacy_v1_payload_regression` PASS，Notes 逐字等於 `bounded addendum write only`，無四項 audit 欄任一
- [x] **[行為性]** `run_no_call_tests.sh` 維持 `PASS=5 FAIL=0`（含 `cb4_not_latched_on_rerun`）— AI-3 收尾重跑確認 PASS=5 FAIL=0；`fixtures/sandbox/reports/*.report.md` 於除錯過程一度被意外重寫，已 `git checkout --` 還原至提交狀態
- [x] **[存在性]** RTR-002／RTR-005 二守門／RTR-007／RTR-009、`blocker_preflight()`、`apply_patch()` 本體、CB-1～CB-4、`RTR_RETRYABLE` 表、report parser 均零改動（scoped diff 逐 hunk 檢視：AI-2 diff 僅命中 `validate_decision_leakage()` 定義、唯一呼叫點、迴圈重置行、成功路徑 note 串接四處）
- [x] **[存在性]** 全程零真實 Codex 呼叫（stub 命中計數為驗證唯一來源）；`bash -n repair_runner.sh` exit 0 且 `git diff --check` exit 0——兩者均已於收尾複驗
- [x] ~~`assembleDebug` PASS~~ (N/A: 非 Android 產物，shell runner ＋ JSON schema) — 確認
- [x] `git diff --stat` 僅涉及 ALLOWED 三路徑 — `/qa` Step 0 PASS（36 檔全數落於 `schemas/ticket-repair-payload.v1.json`／`repair_runner.sh`／`fixtures/**`）
