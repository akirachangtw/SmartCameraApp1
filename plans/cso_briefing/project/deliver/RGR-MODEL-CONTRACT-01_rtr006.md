# RGR-MODEL-CONTRACT-01: 移除以模型 self-report 推定 provider identity 的 RTR-006 gate

檔案治理／來源說明：本票依據 SmartCameraApp1 本機 live scan（2026-09-16 22:27 CST，`grep_files` 直查 `repair_runner.sh` L500–510、L725–728、L39）、`RGR-CODEX-TRANSPORT-01` 已提交之 commit `0736116`／`f151eea`、以及 2026-09-16 runner-owned event-stream Probe A／B 之本地結果建立，並依 `Unified_Ticket_Template_v3.md` v3.11 產出。外部來源僅作交叉參考，不作承重依據。

**簽發日期**: 2026-09-16
**簽發者**: Claude（SKAD）
**來源議題**: RGR
**Mode**: normal  ||  **Effort**: Low
**Traycer Workflow**: N/A
**前置依賴（依賴三分）**：
- `execution_dependency`: 無
- `evidence_dependency`: `RGR-CODEX-TRANSPORT-01` —— commit `0736116f0e4332d3d576ace6dad11e8c3891591b`（stdin transport ＋ task-integrity guards）、commit `f151eea8b1e0d4cae6f09acb1de27ebca11ca1a4`（true-call RTR-006 rejected evidence）；2026-09-16 runner-owned event-stream Probe A／B
- `coordination_dependency`: `RGR-CODEX-TRANSPORT-01` —— 本票提交後，該票僅重跑 AI-3 bounded true-call；成功後方回填 `RGR-RESP-SCHEMA-01` 與 `RGR-P0A-01` 之 true-call 證據

**協調註記（本票事實性註記，非 `concurrency.md` 一般規則之改寫）**：`RGR-CODEX-TRANSPORT-01` 之 AI-1／AI-2 程式變更與 AI-3 rejected evidence 已分別固定於上列二 commit；本票僅修改 `repair_runner.sh` 之 RTR-006 self-report gate，不得修改該票已提交之 stdin transport 或 task-integrity guards。AI-0 MUST 以 `git status` 與 scoped diff 確認無未提交之 TRANSPORT-01 持有變更與本票重疊；若存在重疊、歸屬不明或同檔有其他未提交改動 → 停止回報。**MUST NOT** 僅以 `RGR-CODEX-TRANSPORT-01` 之 `lifecycle=IN_PROGRESS` 自行停止。

**問題陳述**：`validate_model_contract()` 以模型自述之 `run_summary.model`／`run_summary.reasoning_effort` 比對宣告 profile（`$MODEL`／`$EFFORT`），將模型輸出充當 provider 執行環境 metadata。2026-09-16 Probe A 已證實：runner 自有之 `$EVENT_STREAM`（`codex exec --json` stdout）僅含 `thread.started`／`turn.started`／`item.completed`／`turn.completed` 四類事件，**無 `turn_context`**，故無 runner-owned provider metadata producer 可供替換。既無可信 producer，此 gate 之通過與否不承載 provider identity 語義，且已對已具備正確 `ticket_id` 與非空 `finding_results` 之合格回應造成 false reject。

**已駁回之替代方案（禁重開）**：①換 producer 為 `turn_context` —— Probe A 失敗，不可執行；②保留 function 改為 audit log ＋ `return 0` —— 屬新增行為，且使 self-report 更像可信 metadata，不採；③由 prompt 要求模型回填 expected model／effort —— 等同回填，違 `RGR-CODEX-TRANSPORT-01` Hard Constraint 5；④讀 `.codex/sessions/` 家目錄 rollout —— 落點不穩定、repo 外、未版控，不採。

## Verify Config
TICKET_ID=RGR-MODEL-CONTRACT-01
CREATED=2026-09-16
PRESET=bugfix
ALLOWED=.claude/skills/ticket-rigor-gate/runner/repair_runner.sh
FORBIDDEN=.claude/skills/ticket-rigor-gate/runner/schemas/,.claude/skills/ticket-rigor-gate/runner/prompts/,.claude/skills/ticket-rigor-gate/runner/profiles/,.claude/skills/ticket-rigor-gate/runner/fixtures/,.claude/skills/ticket-rigor-gate/runner/housekeeping.sh,.claude/skills/ticket-rigor-gate/runner/README.md,.claude/PROGRESS.md,.claude/rules/ssot-governance.md,.claude/rules/concurrency.md,app/,plans/cso_briefing/project/Pending/RGR-CODEX-TRANSPORT-01_transport.md
MAX_LINES=N/A
DIFF_REF=716a3ec1a558d809c8a6ff5bf453774824f302d4
ANCHOR_PROBED=2026-09-16 22:27
BASE_COMMIT=716a3ec1a558d809c8a6ff5bf453774824f302d4
TARGET_BLOB=N/A
INTRODUCED_CAPABILITY=NONE
DEPENDENCY_DELTA=NONE

**`MAX_LINES=N/A` 依據**：TPL v3.5 R3——本票標的為守門／fail-closed 判定類安全不變量，失敗形狀枚舉為審查發現式，不可先驗估算。

**`TARGET_BLOB=N/A` 依據**：TPL v3.11「Desktop fallback 正式化」——開票端（Claude Desktop）之 MCP 唯讀 git 原語不回傳 blob hash，shell 通道未授權，故標 `N/A`；`BASE_COMMIT` 已填，AI-0 MUST 逐項讀取現值錨點。`N/A` **MUST NOT** 解讀為豁免 AI-0、scope 或 evidence 驗證。

**開票時工作樹狀態**：`git status --porcelain` 輸出為空（clean），HEAD＝`716a3ec`。

## Action Items

### AI-0: 錨點復驗與 scope 預檢（派工後、修改前）

- **檔案**: `.claude/skills/ticket-rigor-gate/runner/repair_runner.sh`
- **動作 A（function 邊界）**: 逐字確認 `validate_model_contract()` 之範圍為 L500–510 共 11 行，其內僅一個 if 分支，且該分支之條件唯一取自 `run_summary.model`／`run_summary.reasoning_effort`：
  - L500 `validate_model_contract() {`（全檔唯一）
  - L501 `  local response="$1"`
  - L502 `  local resp_model resp_effort`
  - L503 `  resp_model="$(jq -r '.run_summary.model // ""' "$response")"`
  - L504 `  resp_effort="$(jq -r '.run_summary.reasoning_effort // ""' "$response")"`
  - L505 條件行（`$resp_model` 與 `$MODEL`、`$resp_effort` 與 `$EFFORT` 之字面不等）
  - L506 `    echo "❌ RTR-006: run_summary.model=$resp_model reasoning_effort=$resp_effort 不符宣告 profile（$PROFILE → $MODEL/$EFFORT）" >&2`
  - L507 `    return 6`
  - L508 `  fi`
  - L509 `  return 0`
  - L510 `}`
- **動作 B（唯一 caller）**: 確認全檔 `validate_model_contract` 之呼叫僅一處，範圍 L725–728：
  - L725 `if ! validate_model_contract "$RAW_RESPONSE"; then`
  - L726 `  fail_rtr RTR-006 "$CURRENT_FINDING_ID" "model contract 不符"`
  - L727 `  exit 1`
  - L728 `fi`
- **動作 C（RTR-006 全檔清點）**: 確認 `RTR-006` 字面全檔恰 3 處＝L39（`RTR_RETRYABLE` 靜態表項）、L506、L726。L39 屬 FORBIDDEN 語義之 retry table，**本票不動**。
- **動作 D（相鄰不動點）**: 確認 L719–723 之 `validate_response_structure` 呼叫區塊、L730–731 之 `VALIDATED_RESPONSE` 賦值與 `cp` 於本票後仍原樣存在。
- **動作 E（標的檔 worktree 狀態）**: `git rev-parse HEAD`、`git status --short`、`git diff --name-only`。**判定範圍限 `repair_runner.sh` 一檔**：該檔有未提交變動且無法確認歸屬，或有他票／他 session 持有之 diff → 停止回報。**其他無關檔案之 dirty 狀態僅記錄於回報，MUST NOT 阻擋本票執行**（Git 歷史非 gate 律；本票 ALLOWED 僅一檔，他檔 dirty 不影響 scope 判定）。`git check-ignore -v .claude/skills/ticket-rigor-gate/runner/repair_runner.sh`（命中 → 停止回報）。
- **動作 F（regression entrypoint 與輸入現況）**: ①確認 runner 目錄**現無**持久化測試 entrypoint（開票時實測：僅 `repair_runner.sh`／`housekeeping.sh`／`README.md` ＋ `fixtures/`／`profiles/`／`prompts/`／`schemas/` 四目錄）；②確認 `.tmp/rigor-gate/ticket-repair/RGR-P0A-01-FIXTURE/truecall-addendum-01/` 下 `raw_response.json` 與 `input_payload.json` 是否仍存在（housekeeping 可能已清掃）。二項結果決定 AI-2 之執行分支。
- **任一項不符 → 停止回報，禁自行推定等價位置。**
- [x] 完成（2026-09-16 執行：動作 A–F 逐項核對，票面所載與現值全數相符；動作 E `git status --short` 空、`check-ignore` 無命中；動作 F 確認無持久化測試 entrypoint，`.tmp/rigor-gate/ticket-repair/RGR-P0A-01-FIXTURE/truecall-addendum-01/` 之 `raw_response.json`／`input_payload.json` 仍存）

### AI-1: 刪除 `validate_model_contract()` 與其唯一 caller

- **檔案**: `.claude/skills/ticket-rigor-gate/runner/repair_runner.sh`
- **現值**: L500–510 之 `validate_model_contract()` 完整定義（內容見 AI-0 動作 A）；L725–728 之 caller 區塊（內容見 AI-0 動作 B）
- **目標值**: 二段**整段刪除**。不留 stub、不留 `return 0` 空 validator、不新增 replacement validator、不新增 stderr audit 輸出、不新增註解式 placeholder。
- **刪除後之主流程相鄰關係（語義描述，非行號）**: `$RAW_RESPONSE` 存在性檢查 → `validate_response_structure` 呼叫區塊 → **（原 model-contract 區塊位置，現無任何語句）** → `VALIDATED_RESPONSE` 賦值與 `cp "$RAW_RESPONSE" "$VALIDATED_RESPONSE"`。即：`validate_response_structure` 通過後，控制流 MUST 直接進入 validated response 之產出，二者之間 MUST NOT 殘留任何 model-contract 相關語句、空行群或註解。
- **不動點**: L39 `RTR_RETRYABLE[RTR-006]=false` 靜態表項**刻意保留**（未被引用之死條目，不恢復 rejection 行為；為其清理而擴 scope 屬過度治理，本票不為）。`validate_response_structure()`（L467–498，含 RTR-005 之 `ticket_id` 比對與非空 `finding_results` 守門）零改動。
- [x] 完成（2026-09-16 執行：`validate_model_contract()` 原 L500–510 與唯一 caller 原 L725–728 整段刪除，無 stub／無 audit log／無 replacement；`validate_response_structure` 通過後直接進入 `VALIDATED_RESPONSE` 賦值，無殘留語句；L39 靜態表項未動）

### AI-2: 最小 function-level regression（無模型呼叫）

- **檔案**: `.claude/skills/ticket-rigor-gate/runner/repair_runner.sh`（唯讀驗證，不得因驗證而改檔）
- **entrypoint 明示**: runner 現無持久化測試 entrypoint（AI-0 動作 F 實測）。本 AI 之唯一許可手法為 **throwaway `source` 函式隔離抽取**——於暫存 shell 中 source `repair_runner.sh` 之目標函式並直接呼叫，執行完畢不留任何檔案。此即 `RGR-CODEX-TRANSPORT-01` AI-2 之既有先例（該票 L77：三案例函式隔離抽取測試通過）。**MUST NOT** 新建測試腳本、harness、fixture 檔或任何持久化 entrypoint；若抽取需落任何持久檔方能執行 → 停止回報，並將本 AI 之全部行為性驗收移交 `RGR-CODEX-TRANSPORT-01` AI-3 之 true-call 承接。
- **驗證案例（限 `validate_response_structure` 未回歸；本票未觸該函式）**:
  1. response `ticket_id` 不符或為 `unknown` → RTR-005 仍拒絕
  2. payload 含 finding 而 response `finding_results` 為空陣列 → RTR-005 仍拒絕
  3. ticket_id 相符且 payload／response findings 均非空 → 通過 task-integrity guards
- **輸入**: 優先取 AI-0 動作 F 所確認之既有 `raw_response.json` ＋ `input_payload.json`；已被 housekeeping 清掃時，得以暫存最小 inline JSON 替代（不落於 repo 內、不新增 fixture）。
- **不納入本 AI 之驗收**: 「self-report 不符時不再拒絕」之正向行為，於本票刪除後**結構上無可驅動之被測路徑**（被測程式碼已不存在），MUST NOT 以任何方式重建以求驗證；該行為之證明由 `RGR-CODEX-TRANSPORT-01` AI-3 之 bounded true-call（該票既有 AC：`exit 0` ＋ `finding_results` 非空）承接，**無須為此對該票開 Rev**。
- **語法檢查**: `bash -n repair_runner.sh` exit 0。
- **停止條件**: 驗證需新增 fixture、測試腳本、dependency、prompt、schema，或需調整 transport → 停止回報，不擴 scope。
- [x] 完成（2026-09-16 執行：`bash -n` exit 0；throwaway `sed` 抽取＋`source` 隔離呼叫 `validate_response_structure()`，未落任何持久檔（`mktemp` 暫存已 `rm -f`），沿用既有 fixture `raw_response.json`／`input_payload.json`：案例1 ticket_id 不符→RTR-005 拒絕 exit 5；案例2 `finding_results` 清空但 payload 含 finding→RTR-005 拒絕 exit 5；案例3 ticket_id 相符且均非空→通過 exit 0）

## Hard Constraints

1. `run_summary.model` 與 `run_summary.reasoning_effort` 得保留為模型輸出之 self-report 資料，但**不得**再被用以推定、驗證、要求或拒絕 provider 實際執行之 model／effort identity。
2. **不得**導入任何 provider metadata producer、repo 外 lookup、`thread_id` rollout lookup、home-directory parser、sidecar cache、models cache 或跨 session state。
3. **不得**建立 model alias、prefix matching、regex matching、allowlist 或 normalization mapping；**不得**於 prompt 中要求模型回填 expected model／effort。
4. **不得**修改既有 stdin prompt transport，亦不得修改 `response.ticket_id == payload.ticket_id`、payload findings 非空時 `finding_results` 非空之 task-integrity guards——該二守門已由 `RGR-CODEX-TRANSPORT-01` AI-2 承接落地，本票僅驗證其未回歸，**禁重新實作、禁納入本票 scope**。
5. **不得**重跑 true-call；本票完成後之 true-call rerun 歸 `RGR-CODEX-TRANSPORT-01` AI-3，且必須以已提交之 transport baseline 原樣執行。
6. **不得**將 `.claude/PROGRESS.md`、`.claude/rules/ssot-governance.md` 或任何他 session／他票之異動 stage、commit 或歸屬為本票內容。

## Error Handling

- AI-0 找不到唯一之 `validate_model_contract()` 定義或唯一 caller，或現值與票面所載不符 → 停止回報，禁自行推定等價位置
- `repair_runner.sh` 有未提交變動且無法確認是否屬本票 → 停止回報
- `RGR-CODEX-TRANSPORT-01` 或其他工作於同一檔案持有未提交 diff → 停止回報
- 刪除 function／caller 會連帶觸及 schema、prompt、profile、transport、`blocker_preflight()`、CB 區、`RTR_RETRYABLE`、`report_records()` 或 `.codex/sessions/` → 停止回報
- regression 顯示 RTR-005 之 ticket ID 比對或非空 `finding_results` 守門被破壞 → 停止回報，**不得**以「RTR-006 已移除」為由略過
- 驗證需呼叫 Codex、重跑 true-call 或 mutate target → 停止回報，轉回 `RGR-CODEX-TRANSPORT-01` AI-3
- **[標配]** `git check-ignore -v .claude/skills/ticket-rigor-gate/runner/repair_runner.sh` 命中 → 停止回報

## Acceptance Criteria

- [x] **[存在性]** `repair_runner.sh` 不再存在任何以 `run_summary.model`／`run_summary.reasoning_effort` 與 `$MODEL`／`$EFFORT` 字面不等觸發 `RTR-006`／`return 6`／`fail_rtr RTR-006` 之可執行路徑；`RTR-006` 字面全檔僅餘 `RTR_RETRYABLE` 靜態表項一處（grep 驗證，實際行號：**L39**）
- [x] **[存在性]** 未新增 `.codex/sessions/`、`thread_id` lookup、home-directory path、sidecar cache、models cache、alias／prefix／regex／allowlist／mapping 之任何讀取或比對路徑（scoped diff 逐 hunk 檢視：diff 僅兩處純刪除 hunk，無新增讀取／比對路徑）
- [x] **[可達性]** 主流程自 `"$CODEX_BIN" exec --json` 成功產出 `$RAW_RESPONSE` 後，仍可達既有 `validate_response_structure()`；producer 與 consumer 行號——`validate_response_structure()` 定義行 **L467**、主路徑呼叫行 **L707**、`VALIDATED_RESPONSE` 賦值行 **L713**
- [x] ~~**[行為性]** Given self-report `model="gpt-5"` 而 requested profile 為 `gpt-5.6-terra`，expect 不再拒絕~~ **（移交 `RGR-CODEX-TRANSPORT-01` AI-3）**：本票刪除被測程式碼後結構上無可驅動路徑，MUST NOT 重建以求驗證；由該票既有 true-call AC（`exit 0` ＋ `finding_results` 非空）承接，無須對該票開 Rev。顯式劃記，禁靜默刪除。
- [x] **[行為性]** Given response `ticket_id` 與 payload 不同或為 `unknown`，expect RTR-005 仍拒絕（throwaway regression 案例1 實測：exit 5，`❌ RTR-005: response ticket_id（unknown）不符 payload ticket_id（RGR-P0A-01-FIXTURE）`）
- [x] **[行為性]** Given payload 含至少一個 finding ID 而 response `finding_results` 為空陣列，expect RTR-005 仍拒絕（throwaway regression 案例2 實測：exit 5，`❌ RTR-005: finding_results 為空但 payload 含 1 個 finding`）
- [x] **[行為性]** Given ticket_id 相符且 payload／response findings 均非空，expect response 通過 task-integrity guards，且 generic self-report **不得**單獨決定 failure（throwaway regression 案例3 實測：exit 0，PASS；`validate_model_contract` 已刪除，控制流不再存在任何 self-report 判定分支）
- [x] **[存在性]** `bash -n .claude/skills/ticket-rigor-gate/runner/repair_runner.sh` exit 0 且 `git diff --check` exit 0（雙項實測皆 exit 0，無 whitespace 錯誤）
- [x] ~~`assembleDebug` PASS~~ (N/A: 非 Android 產物，本票標的為 shell runner，無編譯面)
- [x] `git diff --stat` 僅涉及 ALLOWED 檔案（`.claude/PROGRESS.md`／`ssot-governance.md` 未被 stage 或 commit 為本票內容；實測 `git diff --stat` 僅 `.claude/skills/ticket-rigor-gate/runner/repair_runner.sh` 一檔，17 行純刪除）
