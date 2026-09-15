# RGR-RESP-SCHEMA-01: 使 repair response schema 相容於現行 Codex Structured Outputs strict mode

> **Rev A（2026-09-13，Akira 裁決：乙案 收窄後結案）**：原 AC101 之 `exit 0` 與完整八層驗證／rollback 收口**移交 `RGR-CODEX-TRANSPORT-01`**。理由：AI-2 probe 證實 schema 層目標已達成（provider 接受 schema、thread `01a0999c` turn.completed、response 通過 `validate_response_structure`、`invalid_json_schema` 不再出現），止步於 RTR-006 之真因為 **prompt 傳輸截斷**（`rendered_prompt.txt`／`prompt_used.txt` SHA 一致且含完整任務，但 session 實際 user 訊息僅首行），修復點在 runner L699，落於本票 FORBIDDEN，本票不可達。**駁回「profile 缺陷」歸因**——session `turn_context` 為 `gpt-5.6-terra/medium` 與 profile 一致，`gpt-5/unknown` 係模型生成 JSON 之自述，非 provider 識別證據；禁開 PROFILE-FIX、禁改 model 名稱。收窄為**本票明示記載之 scope 變更，非靜默改 AC**；證據來源：`Skill-Advisor/deliver/ticket-rigor-gate-skill/RGR-RESP-SCHEMA-01_AI2_Evidence_Adjudication_20260913.md`（SHA 指紋表齊備）。

**簽發日期**: 2026-09-13
**簽發者**: Claude (SKAD)（草案來源：Perplexity 2026-09-13，經 SKAD 審查修訂）
**來源議題**: RGR
**Mode**: uc  ||  **Effort**: Medium
**Traycer Workflow**: N/A（APP 端審查路徑走 `/qa` → `/sv` → Cross-Review；本票修的是 rigor gate 自身之 provider 契約，禁以 rigor gate 為本票審查閘）
**前置依賴（依賴三分）**：
- `execution_dependency`: 無（`RGR-P0A-01` checkpoint 已原子提交 `4af7260`，工作樹 clean；本票 schema diff 不與其混雜）
- `evidence_dependency`: `RGR-P0A-01`（AI-3 STOP 記錄，票面 L106；`events.jsonl` 400 逐字證據）；`akirachangtw/SmartCameraApp1#2`；OpenAI Structured Outputs 官方文件（**外部承重，執行端 MUST 附 URL 與頁面日期回填 AI-0，否則僅為 `SUPPORTED`，由 AI-2 probe 升 `CONFIRMED`**）
- `coordination_dependency`: 無（**APP 端單線**——Akira 2026-09-13 裁決：本票禁對 PLT 做任何比對、驗證或同步，避免開發面複雜化；PLT 之對應 schema 是否同病由該端自行另案處置，不由本票承載）

## 檔案治理／來源說明

**承接**：`RGR-P0A-01` AI-3 true-call 於 Windows ＋ Git Bash ＋ 真實 `codex.cmd`（codex-cli 0.145.0）執行 `codex exec --json --output-schema` 時，程序啟動並建立 thread，provider 回 `400 invalid_json_schema`。缺陷為 **RUNNER-01 原始 schema 之既有缺陷**（08-20 至今未動；FIX-A 當時 live 驗證可過，代表當時工具鏈未走 strict 模式），經 P0A-01 之 true-call AC 首次暴露。修復落於 P0A-01 之 FORBIDDEN 路徑，執行端正確停止，本票承接。

**缺陷形狀（SKAD live 取證，APP schema）**：
- 五層 object（L4 root／L12 finding_result／L30 source_ref／L39 replacement／L57 run_summary）`additionalProperties` **零命中**。
- 四處 `required`（L5／L13／L31／L58）均為**部分清單**——strict 模式要求列舉全部 properties，故僅補 `additionalProperties:false` 會換一個 400。
- **草案未列之第二類風險**：schema 使用 `minLength`（L8／L15／L60／L61）、`minimum`（L47）、`const`（L7）、`enum`（L18）。strict 模式對非核心關鍵字之支援**隨模型與 codex-cli 版本變動**（時效敏感，本地無法 grep 消解），故 AI-0 MUST 列全關鍵字清單，AI-2 probe 若於 required/additionalProperties 修正後仍 400 且錯誤指名關鍵字 → **停止回報升裁**，禁自行剝除約束（剝除即放寬契約，且 runner 為 FORBIDDEN 無法補償）。

**本票唯一目的**：修復既有 response contract，使 P0A-01 AI-3 恢復可驗證。不重構 preflight、不動 runner。

## Verify Config
TICKET_ID=RGR-RESP-SCHEMA-01
CREATED=2026-09-13
PRESET=hotfix
ALLOWED=.claude/skills/ticket-rigor-gate/runner/schemas/ticket-repair-response.v1.json,.claude/skills/ticket-rigor-gate/runner/fixtures/true_call/
FORBIDDEN=.claude/skills/ticket-rigor-gate/runner/repair_runner.sh,.claude/skills/ticket-rigor-gate/runner/schemas/ticket-repair-payload.v1.json,.claude/skills/ticket-rigor-gate/runner/prompts/,.claude/skills/ticket-rigor-gate/runner/profiles/,.claude/skills/ticket-rigor-gate/runner/housekeeping.sh,.claude/skills/ticket-rigor-gate/runner/fixtures/no_call/,.claude/skills/ticket-rigor-gate/SKILL.md,.claude/skills/ticket-rigor-gate/REPAIR_ROUTING.md,.claude/skills/ticket-rigor-gate/OPS_CALIBRATION.md,app/src/,build.gradle,settings.gradle
MAX_LINES=N/A
DIFF_REF=6ce95ec
ANCHOR_PROBED=2026-09-13 10:40
BASE_COMMIT=6ce95ec
TARGET_BLOB=.claude/skills/ticket-rigor-gate/runner/schemas/ticket-repair-response.v1.json:5bfd5350
INTRODUCED_CAPABILITY=NONE
DEPENDENCY_DELTA=NONE

**MAX_LINES=N/A 附註**（TPL v3.5 R3）：本票修 provider strict contract 與 fail-closed 真呼叫路徑，變更形狀由 schema object tree 與 downstream null-consumer audit 決定。

**DEPENDENCY_DELTA=NONE 說明**：codex-cli 0.145.0 為既有工具之版本漂移事實（記載於 P0A-01 L106），本票不變更任何依賴座標。

## Action Items

### AI-0: schema、基線、400 證據與 consumer 之 live reconfirmation（派工後、修改前）
- **檔案**: `schemas/ticket-repair-response.v1.json`（標的）；`repair_runner.sh`、`prompts/`、`fixtures/true_call/`（**唯讀**）
- **動作 A（基線）**: `git rev-parse HEAD` 前 7 碼為 `6ce95ec`、`git status` clean、`git hash-object` 回填 `TARGET_BLOB`。HEAD 已越過 → TPL v3.8 基線漂移三分流。
- **動作 B（object tree 機械列舉）**: 列出全部 object 節點（root、巢狀 properties、array `items` object、`$defs`／`$ref` 展開後所指）；每節點記錄 `properties` key 集合、`required` 集合、`additionalProperties` 現值。預期五節點 L4／L12／L30／L39／L57，均無 `additionalProperties`，四處 `required` 為部分清單。
- **動作 C（關鍵字清單）**: 列出 schema 全部 JSON Schema 關鍵字及出現處，預期：`const`×1（L7）、`enum`×1（L18）、`minLength`×4（L8／L15／L60／L61）、`minimum`×1（L47）。**逐一標記**是否屬 strict 模式核心集（`type`／`enum`／`const`／`properties`／`required`／`additionalProperties`／`items`／`anyOf`／`$ref`／`$defs`）。
- **動作 D（400 證據重播）**: 取 P0A-01 AI-3 之 `events.jsonl` 中 `invalid_json_schema` 原文，確認其指向之 schema 路徑與現行 schema 一致；證據遺失、非 schema 拒絕、或與現行 schema 不符 → 停止回報。
- **動作 E（consumer audit）**: 對 `repair_runner.sh`／prompts／fixtures 中所有 response 欄位 consumer，逐處標 `null-safe`／`null-sensitive`／`absence-sensitive`；凡規劃由 optional 改為 nullable 之欄位，其 consumer 若為 null-sensitive 或 absence-sensitive（直接迭代、regex、字串運算、以缺席表狀態）且 runner 為 FORBIDDEN 無法補償 → 停止回報。
- **動作 F（外部承重回填）**: 附 OpenAI Structured Outputs 文件 URL 與頁面日期；缺任一即標 `SUPPORTED`，由 AI-2 probe 決定升級。
- **任一項不符 → 停止回報，禁自行推定等價位置。**
- [x] 完成

### AI-1: 封閉每層 object schema 並保留原有 optional 語意
- **檔案**: `.claude/skills/ticket-rigor-gate/runner/schemas/ticket-repair-response.v1.json`
- **現值**: 五層 object 無 `additionalProperties`；四處 `required` 為部分清單
- **目標值**:
  - 每個 object 顯式 `"additionalProperties": false`。
  - 每個 object 之 `required` 列出其**全部** `properties` key，不多列不存在者。
  - 原可缺席之 property 改 required-but-nullable：型別允許 `null`（`["<原型別>","null"]` 或 `anyOf`），保留既有值型別；**MUST NOT** 以空字串／空陣列／0／false 偽造存在。
  - 原業務上不可為空之 required property **MUST NOT** 改 nullable。
  - **本 AI 不觸碰** `minLength`／`minimum`／`const`／`enum`——關鍵字相容性由 AI-2 probe 判定，非本 AI 先驗剝除。
  - 不改 property 名稱、巢狀結構、語意說明。
- [x] 完成

### AI-2: bounded true-call probe 與 P0A-01 AI-3 回歸驗證
- **檔案**: `fixtures/true_call/`；執行期 `.tmp/rigor-gate/ticket-repair/` artifact
- **動作**:
  - JSON parser 驗 schema 可解析；機械檢查全部 object 節點 `additionalProperties=false` 且 `required` 與 `properties` 完全覆蓋。
  - 以既有 `fixtures/true_call/payload_addendum.json`（`addendum_only`、可回滾、target 限 `plans/cso_briefing/project/` sandbox），Windows ＋ Git Bash ＋ 真實 `codex.cmd exec --json` **一次** bounded probe（必要之最小重試每次 MUST 有新證據，禁反覆消耗 provider call）。
  - 保留與本票 ID／worktree／HEAD／DIFF_REF 綁定之 evidence：Codex 啟動、thread 建立、provider 接受 schema、response 解析、八層驗證、rollback／report、exit status。
  - 重跑 P0A-01 `fixtures/run_no_call_tests.sh`，確認 5/5 fail-closed 不變。
- **停止條件**: provider 400 由 `invalid_json_schema` 轉為其他 4xx（**含指名 `minLength`／`minimum` 等關鍵字者**）→ 停止回報附完整錯誤原文，**禁自行剝除關鍵字**；response parse／null handling／八層驗證／rollback／report 任一回歸或非預期 target mutation → 停止回報；true-call 需改 runner／prompt／profile／payload schema 方能續行 → 停止回報。
- [x] 完成

## Hard Constraints
1. 唯一 production schema 落點為 `ticket-repair-response.v1.json`；禁改 payload schema、runner、prompt、profile、housekeeping、Android／Gradle
2. 每個 object 獨立符合 strict contract；禁只在 root 補 `additionalProperties:false`
3. required 完整化不得改變值語意：optional 僅可轉 required-but-nullable，禁 fabricated default
4. nullability 規劃 MUST 先過 AI-0 動作 E 之 consumer audit；禁以推測視為相容
5. 禁重構、改名或調整 `blocker_preflight()`、CB-1～CB-4、`RTR_RETRYABLE`、六驗證函式、`report_records()` 或既有 failure-code 語義
6. 禁剝除 `minLength`／`minimum`／`const`／`enum` 任一——關鍵字不相容屬升裁事項（剝除即放寬契約且 runner 無法補償）
7. **禁觸 PLT（`mcp-ticket-platform`）任何檔案**：禁比對、禁 hash 驗證、禁同步、禁以 PLT 現況作為本票任一判準或證據來源（Akira 2026-09-13 裁決：APP 端單線）
8. 本票成功後只回填 P0A-01 AI-3 之真呼叫證據；禁追溯將 P0A-01 改寫為 schema 修復票
9. ⚠️ DEFENSIVE WARNING 承接：Do NOT refactor outside this scope. Do NOT rename variables in unrelated files. Do NOT add TODO() or placeholder implementations. Once completed and successfully built, STOP and wait for Quality Gate verification.

## Error Handling
- AI-0 基線／`TARGET_BLOB` 不符 → 基線漂移三分流停止分類回報
- object 未完整枚舉、`$ref` 解析不明、properties／required 集合無法機械比對 → 停止回報
- AI-0 動作 E 任一 nullable 規劃欄位之 consumer 非 null-safe → 停止回報，禁改 FORBIDDEN 路徑強行續作
- API 400 由 `invalid_json_schema` 轉為其他 schema／request 4xx、或指名關鍵字 → 停止回報附原文，禁宣告修復成功
- true-call 發生 target mutation、rollback／report／八層驗證回歸、或非零 exit → 停止回報，保留 evidence，P0A-01 AI-3 維持未完成
- **[標配]** `git check-ignore -v .claude/skills/ticket-rigor-gate/runner/schemas/ticket-repair-response.v1.json` 命中 → 停止回報
- 工作樹含非本票檔案異動且無明確歸屬 → 停止回報

## Acceptance Criteria
- [x] **[存在性]** schema object tree 完整列出（預期五節點）；每節點 `additionalProperties` 明確為 `false`，`required` 集合與 `properties` key 集合完全一致
- [x] **[存在性]** AI-0 動作 C 之關鍵字清單回填，逐一標核心集／非核心集；非核心關鍵字（`minLength`×4／`minimum`×1）於修後**逐字保留**（禁剝除證明）
- [x] **[可達性]** AI-0 動作 E 回填每個 optional→nullable property 之 consumer 位置（`repair_runner.sh:行號`）與判定；全部 null-safe，或該 property 未改 nullable
- [x] **[行為性]**（Rev A 收窄）given `fixtures/true_call/payload_addendum.json` addendum-only case，expect 真實 `codex.cmd exec --json` 啟動並建立 thread、**provider 不再回 `invalid_json_schema`**、response 通過 `validate_response_structure`（`repair_runner.sh:467`）。證據：thread `01a0999c-f3fb-7061-8a22-5fc999a12b42`、`events.jsonl` L1/L2/L8（`thread.started`／`turn.started`／`turn.completed`）、`raw_response.json` 為合規 JSON、schema commit `c9d1870`
- [ ] ~~`exit 0` 與完整八層驗證／rollback／report 收口~~ **（Rev A 移交 `RGR-CODEX-TRANSPORT-01`，本票不承載）**——本次止於 `validate_model_contract`（`repair_runner.sh:487`）RTR-006、`exit 1`；target 未 mutate（blob `e74cb5f9…` 前後同值）、report 已寫入 RTR-003／RTR-006 兩筆（Ratification Status 均 pending，**禁回填為 repair accepted**）
- [x] **[行為性]** given P0A-01 五組 no-call fixtures，expect 全數維持 stub 零命中、模型未呼叫、target hash 不變、receipt 六旗標全 `false`、`exit 1`（`run_no_call_tests.sh` 5/5）
- [x] **[存在性]** true-call evidence record 含 ticket ID、worktree、commit SHA、DIFF_REF、Codex 啟動、thread、provider response、exit status、report／rollback 結果
- [x] **[存在性]** `git diff --name-only` 僅涉 ALLOWED；`repair_runner.sh`、payload schema、prompts、profiles、`no_call/`、CB 區、RTR 表、六驗證函式、Android 路徑零改動
- [x] **[存在性]**（Rev A 新增）基線漂移已重分類並記錄：執行端原稱「`52baa51`／`d1040c2` 未觸碰 `fixtures/true_call/`」**不實**——`52baa51` 新增三個 true_call fixture（`evidence_true_call.md`／`payload_addendum.json`／`target_true_call.md`）；response schema 於該區間確未變更。實作比較基線 `d1040c2` 之採用理由須依 TPL v3.8 三分流明文分類，禁以「正交漂移」概括
- [x] **[存在性]**（Rev A 新增）變更量回填更正：`d1040c2..c9d1870` 為 **2 files、+49 −15**（schema ＋ `report_true_call.report.md`），非執行端回報之「1F +1L −1L」
- [x] ~~`assembleDebug` PASS~~ (N/A: schema／runner toolchain hotfix，`app/src/` 為 FORBIDDEN)
- [x] `git diff --stat` 僅涉及 ALLOWED 檔案

## Addendum：收尾前 live 復驗（2026-09-15，Akira 裁決走 SOP 收尾，方案 1）

**AI-0 動作 A（基線）**：HEAD 已越過 `6ce95ec`；`git diff --name-only 6ce95ec..HEAD -- <ALLOWED>` 非空 → v3.8 三分流 R2：`git log --oneline 6ce95ec..HEAD -- <ALLOWED>` 命中 `c9d1870 [RGR-RESP-SCHEMA-01] ...`（含本票 ID）→ `ALREADY_EXECUTED_PENDING_CLOSURE`。Akira 2026-09-15 對話裁決：票面內容視為已定案（Rev A 乙案），走標準 SOP 收尾，不重新施作。

**AI-0 動作 C（關鍵字清單回填）**：live 讀取 `ticket-repair-response.v1.json` 現值——核心集：`type`×多處、`properties`×5 節點、`required`×5 節點、`additionalProperties:false`×5 節點、`items`×2（`finding_results`／`source_refs_used`）；非核心集：`const`×1（L8 `schema_version`）、`enum`×1（L18-26 `disposition`）、`minLength`×4（L9 `ticket_id`／L17 `finding_id`／L66 `model`／L67 `reasoning_effort`）、`minimum`×1（L52 `changed_line_estimate`）。四類非核心關鍵字逐字保留，未被剝除。

**AI-0 動作 E／AC102（consumer audit）**：live 讀取 `repair_runner.sh:521-616`（`validate_evidence`／`validate_target`／`validate_diff_budget`／`validate_frozen_sentence`／`validate_decision_leakage`）——全數 optional→nullable 欄位（`source_refs_used`／`patch`／`target_file`／`target_section`／`expected_old_text_hash`／`replacement_text`／`changed_line_estimate`／`requires_human_decision`／`verification_hints`）之消費點均採 `jq '.field // <default>'` fallback 慣用法，null-safe 確認。

**AI-1／AC100（schema object tree）**：live 逐節點核對——root（L4-6）／`finding_results.items`（L12-15）／`source_refs_used.items`（L31-34）／`patch`（L41-44）／`run_summary`（L61-64）五節點，`additionalProperties:false` 與 `required`＝`properties` key 集合完全一致，逐一核實。

**AC105**：`bash fixtures/run_no_call_tests.sh` 重跑，5/5 PASS（含 CB-4 latch rerun 檢查）。

**AC107／AC109**：`git show --stat c9d1870` 確認僅 2 檔異動（`schemas/ticket-repair-response.v1.json`＋`fixtures/true_call/report_true_call.report.md`），+49 −15，與票面 AC109 記載一致；`repair_runner.sh`／`payload schema`／`prompts/`／`profiles/`／`no_call/`／Android 路徑零命中。

**AC108**：`git log --oneline 6ce95ec..HEAD -- fixtures/true_call/` 確認 `52baa51 [RGR-P0A-01]` 新增三個 fixture（`evidence_true_call.md`／`payload_addendum.json`／`target_true_call.md`），schema 本身於該區間未變更，與票面記載一致。

以上 live 復驗全數與票面既有記載相符，無新增矛盾。依 Akira 裁決轉入標準 SOP：`/qa` → `/sv` → Cross-Review → closeout 至 `REVIEW`。
