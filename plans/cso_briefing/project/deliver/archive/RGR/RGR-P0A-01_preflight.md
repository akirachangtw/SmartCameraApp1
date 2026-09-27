# RGR-P0A-01: 機械 preflight 前移至 Codex 呼叫前並產出 blocker receipt（APP 端）

> **狀態（2026-09-13，Akira 裁決）**: `BLOCKED` — AI-0~AI-2 completed; AI-3 no-call PASS; AI-3 true-call blocked by pre-existing response-schema Structured Outputs incompatibility. Awaiting `RGR-RESP-SCHEMA-01`.
> 已驗證且不依賴 schema 修復之工作已原子提交 `4af7260`。AI-3 checkbox **不勾**、true-call 相關 AC **不勾**、**不關票**。承接票落地並回填 true-call 證據後，本票方進入 /qa → /sv → Cross-Review。
>
> **狀態更新（2026-09-19，Akira 授權之證據式解阻）**: `BLOCKED` → `REVIEW`。三張承接票（`RGR-RESP-SCHEMA-01` `c9d1870`／`RGR-CODEX-TRANSPORT-01` `0736116`＋`f151eea`／`RGR-MODEL-CONTRACT-01` `ab23c62`）依序消解 schema、transport、model-contract 三層阻塞；true-call 證據已由 TRANSPORT-01 post-fix run 回填並經逐條映射（見 AI-3 下方 Closure Note）。AI-3 checkbox 與 true-call AC 已勾。`blocked_by` 解除。本票現進入 /qa → /sv → Cross-Review。

**簽發日期**: 2026-09-12
**簽發者**: Claude (SKAD)
**來源議題**: RGR
**Mode**: uc  ||  **Effort**: Medium
**Traycer Workflow**: N/A（APP 端審查路徑改走 `/qa` → `/sv` → Cross-Review；rigor gate 對 APP 為選用非強制，本票即其強化實作，不得以自身為審查閘）
**前置依賴（依賴三分）**：
- `execution_dependency`: 無（PLT `RGR-ALIGN-01` 已 CLOSED，兩端 runner 基線同構；`repair_runner.sh` 於 APP 無在途票）
- `evidence_dependency`: vault `2026-09-11_RigorGate_Autonomous_Boundary_Mechanical_Preflight_Adjudication.md`（九項 KD，本票全部設計判準之裁決來源）；`akirachangtw/SmartCameraApp1#2`（design parent／scope lock，**祖父條款**）；PLT `RGR-P0A-01`（VOID，其票面為本票之結構原型，行號**不得沿用**）
- `coordination_dependency`: 無

## 檔案治理／來源說明

**移線背景**：Akira 2026-09-11 裁決——RGR skill 之目的為提升票品質並順利通過 QA/SV，後續強化在 APP 端實作以驗證是否符合真實需求；PLT 同名票已 VOID，PLT 基線已反向對齊 APP（`RGR-ALIGN-01`）。本票為 APP 端首張 RGR 強化票。

**祖父條款（Issue #2）**：舊基線 `9895b3f…`、fingerprint 改制、CB-1 改動一律 **MUST NOT** 複製為 implementation contract；基線、artifact root、parser vocabulary 一律以 AI-0 live reconfirmation 為準。

**核心設計（裁決 KD-1／KD-2／KD-3）**：preflight 僅收具**可指認機械 producer** 之 reason；語義類（SSOT 衝突、架構決策、anti-mock、decision semantics）**留在既有 post-response、pre-mutation 八層**（`L620`–`L635`），本票不動。兩層命名分離：`runner preflight reason` vs `review disposition`。preflight 為 **run 級停止**（一次 payload 一次 Codex 呼叫），已知聚合限制，分割執行列 `RGR-P0A-04` 條件票。

**APP 端 live 取證帶來的兩項設計修正（相對 PLT 原型）**：

1. **CB-1～CB-4 已在模型呼叫前**。`circuit_breaker_precheck()`（`L235`，§14 註解「每次 codex 呼叫前」）位於 `CODEX_CALL_COUNT` 遞增（`L576`）之前。故 PLT 原型將 `CB-2`／`CB-3` 列入 preflight reason 清單屬**重複判定**——本票 reason 清單縮為**三項新增**（見 Rev A），CB 停止維持既有 report-only 行為，不另出 receipt（receipt 擴及 CB 停止列條件票 `RGR-P0A-05`）。
2. **Notes 已被 CB-4 predicate 消費**——`L242`：`[[ "$r_as" == "escalated" ]] && [[ "$r_notes" == *"CB-4"* ]]`。裁決 KD-7 之「AI-0 未證明無 CB 消費前不得寫 Notes K=V」於此**已有答案：有消費，但為子字串比對**。故 Notes K=V 方案**有條件可行**，條件為硬性不變量：**preflight 寫入之 Notes 字面 MUST NOT 含子字串 `CB-4`**（含 receipt 路徑、reason 名、producer evidence 摘要）；否則 preflight 之 `escalated` 記錄會被 CB-4 latch 誤讀為曾觸發 RTR-012，該 finding 永久凍結。此不變量入 HC#7 與存在性 AC。

**Rev A（2026-09-12，`ticket-rigor-gate` Preflight Mode 審查，報告 `.tmp/rigor-gate/RGR_RGR-P0A-01_20260912.md`，Akira 同意修正）**：原五項 reason 清單中 `SCOPE_UNVERIFIABLE_OR_DENIED` 與 `EXTERNAL_EVIDENCE_DECLARED` 之機械 producer 經逐字比對 `runner/schemas/ticket-repair-payload.v1.json`／`ticket-repair-response.v1.json` 後確認不存在或時序不可達（前者聲稱複用之 `validate_target()` 並無 scope 比對邏輯；後者引用欄位 `evidence.kind` 在兩份 schema 中均不存在）；`ANCHOR_MISSING_OR_UNREADABLE` 原引用之 `source_refs_used` 僅存在於 response schema（Codex 呼叫後），preflight 依設計早於呼叫故不可達。三者原判 Critical，逐字實作將恆假或不可達。處置：`ANCHOR_MISSING_OR_UNREADABLE` producer 改指 payload 頂層既有必填欄位 `evidence[].path`；`SCOPE_UNVERIFIABLE_OR_DENIED`／`EXTERNAL_EVIDENCE_DECLARED` 移出封閉清單、併入「不在射程」，因無可即刻機械化之來源且新增比對邏輯將違反 HC#4（禁擴充 schema）。reason 封閉清單自此為**三項**。

**Receipt 落點（KD-7 於 APP 端之重新界定）**：`TELEMETRY_ROOT`＝`.tmp/rigor-gate/ticket-repair`（`L32`），為 `.gitignore` 覆蓋之非版控區（APP `.gitignore` L52/L55）。本票採**最小方案**：receipt 落 run 目錄（隨 housekeeping N=10 清掃），**quality report 索引行自足**——receipt 被清掃後，僅憑 report 一行仍 MUST 能指出何種 blocker、哪個 run、是否未呼叫模型／未 mutate target（AC 承載）。quality report 落點由 payload `quality_report.report_path` 指定（`L559`），可指向版控區，不受 `.tmp/` 拘束。receipt 另闢版控落點之方案列 `RGR-P0A-05` 條件票，觸發條件：出現「report 索引不足以裁決、須回溯已清掃 receipt」之可重驗事件。

**本票引入之新規則限二（O5 預算律）**：①preflight 僅收具機械 producer 之 reason，語義類留 post-response；②report 索引沿用既有 CB-stop 五元組，Notes K=V 受 `CB-4` 子字串禁令拘束。

**不在射程**：fingerprint（`RGR-P0A-02`）、CB-1 policy change（`RGR-P0A-03`）、分割執行（`-04`）、receipt 擴及 CB 停止／版控落點（`-05`）、payload schema 擴欄、新 failure code namespace、`SCOPE_UNVERIFIABLE_OR_DENIED`（scope 授權比對，需先定義機械來源，列未來條件票）、`EXTERNAL_EVIDENCE_DECLARED`（依賴不存在之 `evidence.kind` 欄位，需先擴 schema 方可實作，本票 HC#4 禁止，列未來條件票）。

## Verify Config
TICKET_ID=RGR-P0A-01
CREATED=2026-09-12
PRESET=feature
ALLOWED=.claude/skills/ticket-rigor-gate/runner/repair_runner.sh,.claude/skills/ticket-rigor-gate/runner/fixtures/
FORBIDDEN=.claude/skills/ticket-rigor-gate/runner/schemas/,.claude/skills/ticket-rigor-gate/runner/prompts/,.claude/skills/ticket-rigor-gate/runner/profiles/,.claude/skills/ticket-rigor-gate/runner/housekeeping.sh,.claude/skills/ticket-rigor-gate/SKILL.md,.claude/skills/ticket-rigor-gate/REPAIR_ROUTING.md,.claude/skills/ticket-rigor-gate/OPS_CALIBRATION.md,.claude/skills/ticket-rigor-gate/EXAMPLE.md,app/src/
MAX_LINES=N/A
DIFF_REF=f9e19bf
ANCHOR_PROBED=2026-09-12 14:30
BASE_COMMIT=f9e19bf
TARGET_BLOB=（AI-0 動作 A 填入：`git hash-object .claude/skills/ticket-rigor-gate/runner/repair_runner.sh` 取 sha8——Desktop 端無 hash-object 通道，屬通道歸屬說明非欄位豁免，未填即視同 AI-0 未完成）
INTRODUCED_CAPABILITY=feature:blocker_preflight
DEPENDENCY_DELTA=NONE

**MAX_LINES=N/A 附註**（TPL v3.5 R3）：AI-1／AI-2 屬 fail-closed 守門與授權判定類安全不變量，失敗形狀枚舉為審查發現式，不可先驗估算。

**外審條件 6 判定**：`feature:blocker_preflight` 已於 2026-09-11 裁決入 corpus（vault 決策檔字面命中），6a 不觸發；`ALLOWED` 首層 segment distinct＝1、AI 標題數 4、`DEPENDENCY_DELTA=NONE`，6b 各款未達。

## Action Items

### AI-0: 錨點復驗與基線 reconfirmation（派工後、修改前）
- **檔案**: `.claude/skills/ticket-rigor-gate/runner/repair_runner.sh`
- **動作 A（基線與 blob）**: `git rev-parse HEAD` 前 7 碼為 `f9e19bf`；`git status` clean（**非 clean 時**依 `RGR-ALIGN-01` 先例：清點未提交異動是否與本票標的重疊，重疊即停止回報）；`git hash-object` 取 sha8 回填 `TARGET_BLOB`。HEAD 已越過 → TPL v3.8 基線漂移三分流。
- **動作 B（行號與字面，APP 座標）**: 逐字確認（標「唯一」者恰 1 次）：
  - `L19` `set -euo pipefail`；`L32` `TELEMETRY_ROOT="$REPO_ROOT/.tmp/rigor-gate/ticket-repair"`
  - `L134` `append_report_entry() {`；`L137` mkdir `|| _report_write_failed`；`L153` `>>` `|| _report_write_failed`
  - `L159` `report_records() {`；`L172` `/^- Notes: / { notes=$0; ...}`
  - `L235` `circuit_breaker_precheck() {`；`L242` CB-4 predicate 含 `[[ "$r_notes" == *"CB-4"* ]]`；`L249`／`L264`／`L290`／`L297` 四處 CB 停止之 `append_report_entry "$CURRENT_FINDING_ID" "n/a" "escalated" "n/a" "..."` 五元組
  - `L308` `render_prompt() {`；`L312` findings_block 建構；`L324`–`L325` `allow_scope`／`allow_decision` 讀取；`L348`–`L349` gsub
  - `L413` `validate_evidence() {`；`L559` `QUALITY_REPORT_PATH="$(jq -r '.quality_report.report_path' ...)"`
  - `L576` `CODEX_CALL_COUNT=$((CODEX_CALL_COUNT + 1))`；`L582` `if ! "$CODEX_BIN" exec --json \`
  - `L620`–`L635` 八層驗證呼叫序（含 `L633` decision_leakage → `L635` CB-4 記錄）
- **動作 C（Notes consumer 全檔清點，正向取證）**: `grep -n 'notes' repair_runner.sh`，逐一列出 `report_records()` 擷取之 `notes` 變數之所有 predicate 消費點；**預期恰 1 處（`L242` CB-4 子字串比對）**；若另有其他 predicate 消費（尤其等值比對或 regex）→ 停止回報。
- **動作 D（`circuit_breaker_precheck` 呼叫點位置）**: 確認其主路徑呼叫點行號**小於** `L576`，且 `CURRENT_FINDING_ID` 於該呼叫前已賦值——此為「CB 已在模型前」之機械證明，決定本票 reason 清單不含 CB-2／CB-3 之正當性；若呼叫點在 `L576` 之後 → 停止回報（清單須回補）。
- **任一項不符 → 停止回報，禁自行推定等價位置。**
- **實測結果**：`TARGET_BLOB=5e3398ed`；動作 B 全數逐字命中；動作 C `notes` predicate 消費點恰 1 處（`L242`）；動作 D `circuit_breaker_precheck` 呼叫點 `L676` < `blocker_preflight` 呼叫點 `L680` < `CODEX_CALL_COUNT` 遞增行 `L688`（AI-1 完成後行號，AI-0 當下為 `L568`/`L576`，插入後位移）
- [x] 完成

### AI-1: 新增 `blocker_preflight()` 並布線於 `circuit_breaker_precheck` 之後、`render_prompt` 之前
- **檔案**: `.claude/skills/ticket-rigor-gate/runner/repair_runner.sh`
- **現值**: 無此函式；`circuit_breaker_precheck` 通過後直接 `render_prompt` → `CODEX_CALL_COUNT` 遞增（`L576`）→ `codex.cmd exec`（`L582`）
- **目標值**: 新增 `blocker_preflight()`，布線 MUST 在 `circuit_breaker_precheck` 呼叫之後、`render_prompt` 之前（payload 驗證、ticket／run／report／target／artifacts 初始化均已完成）。
  - **reason 封閉清單（三項，新增須 Rev；原五項中 2 項因 producer 不存在或時序不可達已移出，見票頭 Rev A）**：

    | reason | 機械 producer（既有欄位／函式，**零 schema 擴欄**） |
    | --- | --- |
    | `ANCHOR_MISSING_OR_UNREADABLE` | `evidence[].path`（payload 頂層既有必填欄位，`validate_payload()` 已強制存在）——任一 path 不可解析／不可讀（收窄：僅證 existence／readability，MUST NOT 聲稱語意充分性或 identity；**MUST NOT** 引用 `finding_results[].source_refs_used`——該欄位屬 response schema，preflight 執行當下尚未產生，不可達） |
    | `WRITE_MODE_FORBIDDEN` | `.target.write_mode` ≠ `addendum_only` |
    | `IRREVERSIBLE_TARGET_DENIED` | target 路徑對不可逆路徑模式之比對（**僅限路徑規則可識別者**，本票模式集：`app/src/**`、`*.gradle*`、`gradle/**`、`.github/**`、`**/migration*/**`；MUST NOT 聲稱可從 replacement text 判讀意圖） |

  - **停止粒度**：run 級——任一 finding 命中任一 reason → 整次停止。
  - **命中時 MUST NOT**：render prompt、遞增 `CODEX_CALL_COUNT`、呼叫 `codex.cmd`、mutate target、宣告 PASS。**MUST**：寫 receipt（AI-2）、追加 report 索引（AI-2）、`exit 1`。
  - `allow_scope_expansion`／`allow_decision_making` **MUST NOT** 作放行權；於 preflight 僅得記錄為 legacy diagnostic。
- **實測結果**：`blocker_preflight()` 定義於 `L365`，布線於 `circuit_breaker_precheck` 呼叫（`L676`）之後、`render_prompt` 呼叫之前；no-call fixture（`fixtures/run_no_call_tests.sh`）驗證 `allow_scope_expansion=true`＋`allow_decision_making=true` 之 payload 命中 `WRITE_MODE_FORBIDDEN` 時仍 `exit 1`
- [x] 完成

### AI-2: blocker receipt 與 parser 相容之 report 索引
- **檔案**: `.claude/skills/ticket-rigor-gate/runner/repair_runner.sh`
- **目標值**:
  - receipt 路徑：`<TELEMETRY_ROOT>/<ticket_id>/<source_run_id>/blocker-receipt.v1.json`（`TELEMETRY_ROOT` 取 `L32` 變數，禁硬編）。
  - receipt 欄位：`schema_version`＝`blocker-receipt/v1`、`ticket_id`、`source_run_id`、`created_at`（UTC ISO-8601）、`status`（`CHECKPOINT_REQUIRED` | `PENDING_EVIDENCE`）、`reasons[]`（每項 `reason`／`finding_id`／`producer_evidence{source,read_result,match_result}`）、`automation_boundary`（`model_invoked`／`prompt_rendered`／`patch_attempted`／`target_mutated`／`external_side_effect`／`final_pass_claimed` 六旗標全 `false`）、`resume_condition[]`。
  - report 索引：**沿用既有 CB-stop 五元組**——`append_report_entry "<finding_id>" "n/a" "escalated" "n/a" "<notes>"`（`L249`／`L264`／`L290`／`L297` 同形）；每一命中 finding 一行。**禁新增** `Apply Status` 值、`Failure Code` namespace、`report_records()` awk 行。
  - Notes 字面：`P0A_PRECHECK_REASON=<reason>; BLOCKER_RECEIPT=<run-relative path>`。**硬性不變量：Notes 全文 MUST NOT 含子字串 `CB-4`**（`L242` CB-4 latch 以 `escalated && notes contains "CB-4"` 判定；違反即該 finding 永久誤凍結）。
  - report 索引自足：receipt 被清掃後，僅憑 report 該行 MUST 能指出 reason、run id、`model_invoked=false`——故 Notes 另附 `MODEL_INVOKED=false`。
- **實測結果**：`write_blocker_receipt()`（含於新增區段）落 `<TELEMETRY_ROOT>/<ticket_id>/<source_run_id>/blocker-receipt.v1.json`，六旗標全 `false`；no-call fixture 三例逐一驗證 receipt 欄位齊全；`fixtures/sandbox/reports/anchor_missing.report.md` 手動清除對應 receipt 後 Notes 行仍可讀出 `P0A_PRECHECK_REASON`／`BLOCKER_RECEIPT`／`MODEL_INVOKED=false`；全檔 Notes 無 `CB-4` 子字串（`grep -c 'CB-4'` 於 preflight 新增區段＝0）
- [x] 完成

### AI-3: fixture 雙組（no-call ＋ true-call regression）
- **檔案**: `.claude/skills/ticket-rigor-gate/runner/fixtures/`（新增；僅 workflow sandbox／test-only，**禁觸 `app/src/`、真機、帳號、外部服務**）
- **目標值**:
  - **no-call 組**：以可偵測之 `codex.cmd` stub 替換（被呼叫即 fixture FAIL）。案例至少涵蓋三 reason 各一。每例驗：stub 零命中、`CODEX_CALL_COUNT` 不變、target hash 與 pre-run 一致、run 目錄**不存在** `rendered-prompt.txt` 與 `raw-response.json`、receipt 存在且六旗標全 `false`、report 索引存在且 Notes 不含 `CB-4`、`exit 1`。
  - **true-call 組**：一個 `addendum_only`、可回滾之低風險案例，target 限 `.claude/skills/ticket-rigor-gate/runner/fixtures/true_call/` 下之 sandbox 文件（**非** `app/src/`、非治理文件樹）〔**Rev B 2026-09-15 更正**：原字面為「`plans/cso_briefing/project/` 下之 sandbox 文件」，與本 AI 之 L105 自身宣告（`fixtures/` 即 workflow sandbox／test-only）矛盾，屬開票方過度指定；約束目的為「非生產碼、非 `app/src/`、可回滾、test-only」，`fixtures/true_call/` 全數滿足且優於 `plans/`——後者為真實票檔與交付物所在之治理文件樹，令 runner 於其中 mutate 將使測試寫入面與票檔異動於 `git diff` 混雜。此為**字面更正非約束放寬**，安全性質未降〕，於 **Windows ＋ git-bash** 實際路徑跑通——`codex exec` 真正啟動（附啟動證據）、八層驗證／rollback／report 路徑無回歸、`exit 0`。
- **🔴 STOP（AI-3 Error Handling 條款觸發）**：no-call 組 5/5 PASS（`fixtures/run_no_call_tests.sh`：三 reason 各一＋allow_* 旗標不解除＋CB-4 rerun 不誤凍結）。true-call 組以 `fixtures/true_call/payload_addendum.json` 對真實 `codex.cmd`（`codex-cli 0.145.0`，`command -v` 可解析，非 openai/codex #28278 型無法啟動）實跑，`codex exec --json` 確實啟動並建立 thread，但 API 端以 `400 invalid_json_schema` 拒絕：`schemas/ticket-repair-response.v1.json` 未於每層滿足 OpenAI Structured Outputs 嚴格模式之 `additionalProperties: false` 要求（events.jsonl 逐字：`"code":"invalid_json_schema"...'additionalProperties' is required to be supplied and to be false`）。修復需改動 `.claude/skills/ticket-rigor-gate/runner/schemas/`，該路徑列本票 **FORBIDDEN**，禁改。依 AI-3 Error Handling「codex.cmd 於本機無法啟動 → 停止回報，禁以 no-call 組單獨結案」比照適用（此為呼叫已啟動但因既有 schema 缺陷於 API 層 400，效果同為「true-call 組無法完成」，非 blocker_preflight／AI-1／AI-2 標的之缺陷）。**停止回報，AI-3 不勾選完成，不以 no-call 組單獨結案**；schema 修復另需承接票（`schemas/ticket-repair-response.v1.json` 全層補 `additionalProperties: false`），待 CSO 裁決是否併入 `RGR-P0A-04`／另立新票。
- [x] 完成（no-call 組 5/5 PASS；true-call 組 PASS，2026-09-17 收口，見下方 Closure Note）

**Closure Note（2026-09-19，SKAD；Akira 授權之證據式解阻）**

AI-3 之 true-call 組非於本票重跑，而係由三張承接票依序消解阻塞後，於 `RGR-CODEX-TRANSPORT-01` AI-3 之 post-fix run 完成；本票之 AC 逐條對照如下，無一項以「上游票 PASS」推定：

| 本票 AI-3／AC 要求 | 收口證據 | 判定 |
|---|---|---|
| 實際 true-call（非 stub、非 no-call） | `thread=01a0adec-b8e8-78c2-ac69-18cf52ad6694`，`codex exec --json` 實際啟動，artifact 落 `.tmp/rigor-gate/ticket-repair/RGR-P0A-01-FIXTURE-POSTFIX/truecall-postfix-01/` | ✅ |
| target 限 `fixtures/true_call/` 下 sandbox 文件（Rev B） | `target_true_call.md`，非 `app/src/`、非治理文件樹 | ✅ |
| `addendum_only` 可回滾 | `e74cb5f9…`→`693c701…`，純新增 3 行（`Addendum: FIXTURE_CONSTANT = 42`），既有內容未動 | ✅ |
| schema 可接受（無 `invalid_json_schema`） | `RGR-RESP-SCHEMA-01`（`c9d1870`）落地後，response 通過結構驗證 | ✅ |
| prompt 完整送達（非僅首行空跑） | `RGR-CODEX-TRANSPORT-01` AI-1 stdin 修復（`0736116`）；`prompt_used.txt`＝`rendered_prompt.txt`；`F-TRUECALL-1` 有實質 `disposition=propose_repair`、`reason` 引用 `evidence_true_call.md:1-3`、具體 `patch` | ✅ |
| task integrity | `response.ticket_id == payload.ticket_id`；payload 有 findings 時 `finding_results` 非空——二守門由 TRANSPORT-01 AI-2 落地並於本輪回歸仍生效 | ✅ |
| model contract 不再阻斷 | `RGR-MODEL-CONTRACT-01`（`ab23c62`）移除 `validate_model_contract()` 與唯一 caller；全檔無 active caller | ✅ |
| 驗證層／rollback／report 無回歸、`exit 0` | 現行七層＋RTR-005 二守門全過；report append-only 追加一筆，歷史未動；`exit 0` | ✅（層數更正見對應 AC） |
| 本票本體行為（preflight 在模型呼叫前、blocker 命中零 render／零 call／零 mutation／非 PASS receipt） | AI-1 實測 `blocker_preflight()` `L365` 布線於 `render_prompt` 之前；no-call 5/5 receipt 六旗標全 `false` | ✅ |
| no-call 回歸 | post-fix run 之後重跑 `run_no_call_tests.sh`：`PASS=5 FAIL=0`，含 `cb4_not_latched_on_rerun` | ✅ |

**fixture identity 說明**：收口 run 之 `ticket_id` 為 `RGR-P0A-01-FIXTURE-POSTFIX`。原 `RGR-P0A-01-FIXTURE` 因兩筆歷史 RTR-006 記錄而受 CB-1 凍結，該凍結與 CB-1 邏輯、report 歷史均**未經編輯或繞過**，至今維持有效；post-fix 為獨立驗證情境，payload 除 `ticket_id`／`source_run_id` 二欄外與原 payload 逐字相同（`diff -u <(jq del OLD) <(jq del NEW)` 空）。ticket_id 輪換之授權**僅限 runner fixture 情境**，對真實票於 CB-1 觸發後輪換身分以規避視窗**恆禁**。

**本票未因本次解阻而變更**：歷史實作、原始 evidence、CB identity、票面既有文字（除本 Note 與二處 checkbox／AC 層數更正外）一律未動。

## Hard Constraints
1. 禁改動 `RTR_RETRYABLE` 表與任何既有 RTR 失敗碼語義
2. 禁改動 CB-1～CB-4 之 enforcement、門檻、key、凍結行為與其呼叫位置（裁決 KD-6）
3. 禁改動六驗證函式本體；preflight 得複用其純 payload 判定，MUST NOT 移除或改寫既有 post-response 呼叫（`L620`–`L635`）
4. 禁擴充 payload／response schema；禁改 prompt 模板與 profiles
5. 禁實作 fingerprint、per-fingerprint budget 或 durable store；禁於 AC 出現「budget 已實作」
6. 禁新增 `Apply Status` 值、`Failure Code` namespace，禁改 `report_records()` awk 擷取行
7. **preflight 寫入之任何 report Notes 字面 MUST NOT 含子字串 `CB-4`**（CB-4 latch 誤觸發防護）
8. 禁以 `allow_scope_expansion`／`allow_decision_making` 作放行權
9. 禁將語義類判定前移至 preflight
10. 禁以本票自身之 rigor gate 作為本票審查閘（審查走 `/qa` → `/sv` → Cross-Review）
11. ⚠️ DEFENSIVE WARNING 承接：Do NOT refactor outside this scope. Do NOT rename variables in unrelated files. Do NOT add TODO() or placeholder implementations. Once completed and successfully built, STOP and wait for Quality Gate verification.

## Error Handling
- AI-0 動作 A：HEAD 越過 `f9e19bf` → 基線漂移三分流，禁自行重錨
- AI-0 動作 B：任一行號／字面／命中數不符 → 停止回報
- AI-0 動作 C：`notes` 之 predicate 消費點**不止 `L242`**、或 `L242` 非子字串比對 → 停止回報（Notes 方案須改設計）
- AI-0 動作 D：`circuit_breaker_precheck` 呼叫點在 `L576` 之後 → 停止回報（reason 清單須回補 CB-2／CB-3）
- AI-1：既有流程中發現等價 preflight 檢查（雙重判定）→ 停止回報
- AI-3 true-call：`codex.cmd` 於本機無法啟動（參 openai/codex #28278）→ 停止回報，**禁以 no-call 組單獨結案**
- **[標配]** `git check-ignore -v .claude/skills/ticket-rigor-gate/runner/repair_runner.sh` 命中 → 停止回報

## Acceptance Criteria
- [x] **[行為性]** no-call 組三 reason 各一例：given 對應缺陷 payload，expect `codex.cmd` stub 零命中、`CODEX_CALL_COUNT` 不變、target hash 逐字一致、run 目錄無 `rendered-prompt.txt`／`raw-response.json`、`exit 1`——`fixtures/run_no_call_tests.sh` 3/3 PASS
- [x] **[行為性]** 每例 receipt 存在，`automation_boundary` 六旗標全 `false`，`reasons[].producer_evidence` 三欄非空——同上 harness 驗證
- [x] **[行為性]** given `allow_scope_expansion=true` 且 `allow_decision_making=true` 之 payload 命中任一 reason，expect 仍停止（旗標不解除 hard stop）——`payload_write_mode_forbidden_flags.json` PASS
- [x] **[行為性]** given no-call 停止後**再次**對同 finding 執行 runner，expect CB-4 **不**因前次 preflight 記錄而誤凍結（Notes 不含 `CB-4` 之行為性證明）——rerun PASS，stderr 無 `CB-4 tripped`
- [x] **[行為性]** true-call 回歸：Windows ＋ git-bash，`fixtures/true_call/` sandbox 文件之 `addendum_only` 案例跑通〔Rev B 2026-09-15 更正，見 AI-3〕，`codex.cmd exec` 實際啟動（附證據），驗證層／rollback／report 無回歸、`exit 0` — **PASS（2026-09-17，經三張承接票回填）**：原 STOP 之 `invalid_json_schema` 由 `RGR-RESP-SCHEMA-01`（`c9d1870`）消解；prompt 傳輸截斷由 `RGR-CODEX-TRANSPORT-01` AI-1 stdin 修復（`0736116`）；RTR-006 false reject 由 `RGR-MODEL-CONTRACT-01`（`ab23c62`）移除。收口 run：`ticket_id=RGR-P0A-01-FIXTURE-POSTFIX`／`source_run_id=truecall-postfix-01`／`thread=01a0adec-b8e8-78c2-ac69-18cf52ad6694`，target `target_true_call.md` `e74cb5f9…`→`693c701…`（addendum-only 新增 3 行），`exit 0`。**驗證層數更正**：本 AC 原文之「八層」含 §7.2 model contract，該層已由 `RGR-MODEL-CONTRACT-01` 正式退役；收口 run 通過之現行**七層**（schema／eligibility／evidence／target hash／diff budget／frozen sentence／decision leakage）＋`validate_response_structure` 之 RTR-005 二守門，即為本 AC 所指驗證堆疊之當前全量，非降低標準
- [x] **[可達性]** producer 證據欄（執行後回填）：`blocker_preflight()` 定義 `L365`、主路徑呼叫點 `L680`、`circuit_breaker_precheck` 呼叫點 `L676`、`CODEX_CALL_COUNT` 遞增行 `L688`；CB 呼叫點(676) < preflight 呼叫點(680) < 遞增行(688) 已證
- [x] **[可達性]** Notes consumer 清點結果欄（執行後回填）：`report_records()` `notes` 之 predicate 消費點 `L242`（唯一一處，`grep -n 'notes'` 全檔複驗，L240/255/271 之 `r_notes` 讀入但未消費）
- [x] **[存在性]** `git diff` 逐 hunk：CB-1～CB-4 判定區（`L235`–`L300`）、六驗證函式本體、`report_records()` awk 行、`RTR_RETRYABLE` 表**均零改動**——`git diff --stat` 顯示純新增（112 insertions, 0 deletions）
- [x] **[存在性]** 全檔 `grep -c 'fingerprint'` ＝ 0；preflight 區段 `grep -c 'CB-4'` ＝ 0——已驗證
- [x] **[存在性]** receipt 被手動清除後，僅憑 quality report 該行可讀出 `P0A_PRECHECK_REASON`、`BLOCKER_RECEIPT`、`MODEL_INVOKED=false`（report 索引自足）——已對 `anchor_missing.report.md` 實測
- [x] `bash -n .claude/skills/ticket-rigor-gate/runner/repair_runner.sh` PASS
- [ ] ~~`assembleDebug` PASS~~ (N/A: bash runner 無編譯產物；`app/src/` 在 FORBIDDEN，Android 建置不受影響)
- [x] `git diff --stat` 僅涉及 ALLOWED 檔案（`repair_runner.sh` 修改＋`fixtures/` 新增，均在 ALLOWED）
