# MCP-TS-HEADROOM：`headroomViewProvider.js` TypeScript 化與風險治理面板正式實作單

## 2026-07-15 Repair succession and current contract

This file is a historical umbrella and ticket-provenance record. Current runtime authority is Config v2 plus the five `INFRA-HEADROOM-*` repair tickets.

- Active: Headroom 8787 `/health + /stats` only.
- Preview: Supervisor 8788 `/events/ingest + /summary + /events/recent`.
- Promotion requires at least 20 `source_kind=real` Canary events; fixtures contribute zero promotion evidence.
- `source_status=prototype` or `promoted=false` always prevents PASS.

| Existing ticket | Repair successor | Current responsibility |
|---|---|---|
| `MCP-KPI-00` | `INFRA-HEADROOM-SCHEMA-01`, `INFRA-HEADROOM-GATE-01` | Typed Schema and enforceable gate evidence |
| `MCP-KPI-01` | `INFRA-HEADROOM-SCHEMA-01`, `INFRA-HEADROOM-CANARY-01` | Fixture isolation and 20 real events |
| `MCP-KPI-02` | `INFRA-HEADROOM-SUPERVISOR-01` | Real/fixture aggregation |
| `MCP-KPI-03` | `INFRA-HEADROOM-SUPERVISOR-01` | Trusted Supervisor endpoints |
| `MCP-KPI-04` | `INFRA-HEADROOM-GATE-01` | Config v2 and Preview dashboard wiring |
| `MCP-KPI-05` | `INFRA-HEADROOM-DOCSYNC-01` | Documentation truth-source sync |
| `MCP-KPI-06` | `INFRA-HEADROOM-GATE-01`, `INFRA-HEADROOM-DOCSYNC-01` | Optional CodeGraph remains disabled/unpromoted |

Historical acceptance text below is preserved for provenance. Where it allows fixture-driven promotion, ambiguous `compression_ratio`, inferred endpoints, or non-neutral preview warnings, this succession section overrides it.

## Summary
本票目標不是單純把 `headroomViewProvider.js` 由 JavaScript 改名成 TypeScript，而是藉由 TypeScript 化，將 Headroom dashboard 的資料契約、面板分區、風險訊號、risk alert、promotion gate 與 active/future scope 邊界一次收斂成可驗證的正式實作。

本票定位在 Phase 1 的 VSCode Webview 實作層：保留目前「extension 負責展示、sidecar 負責聚合與判定」的架構原則，並將 UI 契約明文化到型別、view model、狀態機與 Webview message protocol，避免後續再出現 mock UI 先行、endpoint 與 schema 漂移的情況。

## 2026-06-30 審查修正版：本票改為 umbrella，不直接執行

本文件原本把 TypeScript 遷移、Panel A–F、risk alert、promotion gate、status bar、packaging 放在同一張實作單，執行風險過高。正式執行時請改用下方小票切分；本文件保留為 umbrella 背景與約束來源。

### Live Repo 不可破壞契約

- 目標 extension：`tmp/Headroom/vscode-headroom-dashboard/`。
- 目前 entry：`package.json.main = ./src/extension.js`；只有 TS build 與 VSIX 載入驗證通過後才能改為 `./out/extension.js`。
- 目前 view id：`headroomDashboard.view`。
- 目前 command id：`headroomDashboard.refresh`、`headroomDashboard.openConfig`。
- 正式設定來源：workspace root `.agents/pilot/pilot.config.json`。
- active endpoint：只允許 `/health + /stats`。
- active schema：`headroom_only` v1；retrieval/compression/quality/CodeGraph KPI 只能是 prototype、blocked 或 unavailable。

### 小票切分

#### MCP-TS-00：Live Baseline Lock

**目的**：在任何 TS 遷移前鎖定目前 JS extension 可用基線。

**Scope IN**
- 讀取並記錄 `tmp/Headroom/vscode-headroom-dashboard/package.json`、`src/extension.js`、`src/headroomViewProvider.js` 的 view id、command id、config path、status bar、OutputChannel、polling/fetch 行為。
- 執行現有 `npm run check`。

**Scope OUT**
- 不新增 TS 檔。
- 不改 UI。
- 不改 `.claude/PROGRESS.md` 或 handoff。

**AC**
- 產出 baseline notes：`main`、view id、commands、config path、active endpoints。
- `npm run check` 結果被記錄；若失敗，先開修復票，不進入 TS 遷移。

#### MCP-TS-01：TS Build Chain Only

**目的**：只建立 TypeScript 編譯鏈，不切 runtime entry。

**Scope IN**
- 新增 `tsconfig.json`。
- `package.json` 新增 `typescript`、`@types/node`、`build`、`check:ts`。
- 保持 `main = ./src/extension.js` 與現有 `check` 可跑。

**Scope OUT**
- 不切 `main`。
- 不替換 provider。
- 不新增 Panel A–F。

**AC**
- `npm run check` PASS。
- `npm run build` PASS。
- 沒有改動 view id 或 command id。

#### MCP-TS-02：Types and Pure Services Skeleton

**目的**：引入可編譯型別與純函式 service，先不接 UI。

**Scope IN**
- 新增 `src/types/*`、`src/services/configLoader.ts`、`dataFetcher.ts`、`dashboardMapper.ts`、`riskEvaluator.ts`、`promotionGate.ts`。
- `FallbackLevel` 拆成 compression fallback `F1–F3` 與 rollback `R0–R3`，不得混在同一型別。
- `dataFetcher` 必須以 `response.ok` 判斷 HTTP 500，且可處理非 JSON payload。

**Scope OUT**
- 不改現行 JS provider render 行為。
- 不讓 future KPI 進 active dashboard。

**AC**
- `npm run build` PASS。
- 型別中 active source 與 future KPI promotion 可區分。
- `/health + /stats` 是唯一 active data contract。

#### MCP-TS-03：Provider TS Parity

**目的**：將 provider 遷移到 TS，但只達成現有功能 parity。

**Scope IN**
- 建立 `src/headroomViewProvider.ts`。
- 保留 workspace root config lookup：`.agents/pilot/pilot.config.json`。
- 保留 `fetchWithTimeout()`、safe JSON parse、status derivation、manual refresh、polling、OutputChannel、status callback。
- 顯示 health/stats 紅黃綠狀態；raw payload 是否顯示仍受 `headroom.dashboard.show_raw_payload` 控制。

**Scope OUT**
- 不啟用 Panel B/C/E KPI。
- 不顯示 Active Schema 區塊。
- 不把 `/summary` 或 `/events/recent` 視為 active。

**AC**
- TS provider 與 JS provider 在 `/health + /stats` 行為一致。
- HTTP 500、timeout、非 JSON response 均有明確 degraded/offline 狀態。
- `headroomDashboard.refresh` 與 `headroomDashboard.openConfig` 仍可用。

#### MCP-TS-04：Extension Entry Cutover

**目的**：在 provider parity 通過後才切換 runtime entry。

**Scope IN**
- 新增 `src/extension.ts`，接回現行 view id、commands、status bar、OutputChannel。
- `package.json.main` 切到 `./out/extension.js`。
- package script 先 build 再 vsce package。

**Scope OUT**
- 不更名 command/view。
- 不新增第二套 status bar。

**AC**
- VSIX 可載入 webview。
- status bar 狀態與 dashboard 狀態一致。
- open config 開啟 workspace root `.agents/pilot/pilot.config.json`。

#### MCP-TS-05：Governance Prototype Panels

**目的**：只在 TS runtime 穩定後，加入治理視圖與 prototype panel。

**Scope IN**
- Panel A/F 可 active：Session/Health/Stats + Governance Gate。
- Panel B/C/E 僅可 prototype、blocked 或 unavailable。
- risk alert 只使用有 evidence 的欄位；無 evidence 不得顯示 critical。

**Scope OUT**
- 不 promotion retrieval/compression/quality KPI。
- 不啟用 CodeGraph dashboard card。

**AC**
- 未達 promotion gate 時，future KPI 不顯示為 active 或 green。
- Panel F 明確列出 blockers：schema note、event count、endpoint readiness、fallback mapping。
- packaged VSIX 在 health-only 情境下不產生 fake KPI。

## KPI Promotion 補件實作票

以下小票用於把目前 preview/prototype 狀態的 Retrieval / Compression / Quality / CodeGraph KPI 推進到可 promotion 的資料基礎。這批票不得改變既有 active contract：在 promotion gate 通過前，active dashboard 仍只以 `/health + /stats` 判斷 Headroom 狀態。

### MCP-KPI-00：Promotion Contract Note 與 Gate Evidence 定義

**目的**：先把 KPI promotion 的最小證據契約寫清楚，避免後續直接把 future KPI 打開成 active。

**Authority Reference**
- `dashboard-metrics-schema.json`：目前 active schema 是 `headroom_only`，刻意排除 retrieval / compression / latency KPI。
- `MCP-TS-HEADROOM-IMPLEMENTATION.md`：`/health + /stats` 是唯一 active data contract；future KPI 未達 gate 不得 active。
- `.agents/pilot/headroom-payload-rules.md`、`.agents/pilot/headroom-sampling.md`：已有 compression / sampling draft 欄位。

**Target Files**
- `tmp/Headroom/pilot-docs/kpi-promotion-gate.md`
- `tmp/Headroom/pilot-docs/retrieval-compression-kpi-schema.json`
- `tmp/Headroom/pilot-docs/MCP-TS-HEADROOM-IMPLEMENTATION.md`

**Absolute Ban List**
- 不修改 Android app。
- 不修改 `.claude/PROGRESS.md` 或 handoff。
- 不把 `dashboard-metrics-schema.json` 的 `scope=headroom_only` 改成 KPI schema。
- 不把 `codegraph.enabled` 或 `enable_codegraph_health` 改成 true。

**Acceptance Criteria**
- `kpi-promotion-gate.md` 明確列出 promotion pass / hold / blocked 條件。
- promotion note 至少定義：schema note、event count、endpoint readiness、fallback mapping、README/config sync。
- `retrieval-compression-kpi-schema.json` 可被引用為 future schema，不覆蓋 active `headroom_only` schema。
- 文件明確寫出：未 promotion 前 UI 只能顯示 preview/prototype，不得顯示 active/green KPI。

**Dependencies**
- 依賴 `MCP-TS-05` 已保留 Governance / Observability preview UI。
- 可與 `MCP-KPI-01` 並行，但不得晚於 `MCP-KPI-04`。

**Defensive Warning**
Do NOT refactor outside this scope. Do NOT rename variables in unrelated files. Do NOT add TODO() or placeholder implementations. Once completed and successfully built, STOP and wait for Quality Gate verification.

### MCP-KPI-01：真實 / Fixture Event Flow 與 20 筆門檻資料

**目的**：補齊 promotion gate 所需的 event evidence，讓 event count 不再依靠硬編碼或手動假值。

**Authority Reference**
- `MCP-TS-HEADROOM-IMPLEMENTATION.md`：promotion gate 至少要求真實 event flow 與 20 筆 `source_kind=real` event；fixture 僅驗證 mechanics。
- `.agents/pilot/headroom-sampling.md`：定義 sampling draft 欄位與品質評分欄位。
- `.agents/pilot/headroom-payload-rules.md`：定義 compression event draft 欄位與 fallback reason。

**Target Files**
- `tmp/Headroom/prototype-sidecar/events.jsonl`
- `tmp/Headroom/prototype-sidecar/fixtures.jsonl`
- `tmp/Headroom/prototype-sidecar/generate_samples.js`
- 新增：`tmp/Headroom/prototype-sidecar/scripts/validate-kpi-events.js`
- 新增：`tmp/Headroom/testing/kpi-events-fixture.jsonl`

**Absolute Ban List**
- 不寫入 `.agents/pilot/logs/` 作為 tracked fixture。
- 不用 synthetic PASS 取代 event schema 檢查。
- 不改 VS Code dashboard UI。
- 不把 fixture event 當成 production telemetry。

**Acceptance Criteria**
- 至少有 20 筆 fixture-driven test event 涵蓋 retrieval、compression、quality、fallback，但 promotion count 必須維持 0。
- 每筆 event 必須可解析為一行 JSONL，不得出現 literal escaped newline 造成 JSONL 失真。
- `validate-kpi-events.js` 可驗證 event count、必要欄位與 JSONL 格式。
- 驗證命令可在 `tmp/Headroom/prototype-sidecar` 下執行並回報 PASS/FAIL。

**Dependencies**
- 建議先完成 `MCP-KPI-00`，但可先建立 fixture 草案。
- `MCP-KPI-02` 依賴本票輸出的 event fixture。

**Defensive Warning**
Do NOT refactor outside this scope. Do NOT rename variables in unrelated files. Do NOT add TODO() or placeholder implementations. Once completed and successfully built, STOP and wait for Quality Gate verification.

### MCP-KPI-02：KPI Aggregator 純函式與 Summary 輸出

**目的**：把 JSONL event 聚合成 dashboard 可讀的 retrieval / compression / quality summary，避免 extension UI 自己推導 KPI。

**Authority Reference**
- `pilot-integration.md`：sidecar / supervisor 負責聚合與判定，extension 負責展示。
- `MCP-TS-HEADROOM-IMPLEMENTATION.md`：extension 不把 retrieval 計算、compression 統計核心邏輯塞回 extension host。
- `.agents/pilot/headroom-sampling.md`：quality_score、followup_count、compression_ratio 等欄位來源。

**Target Files**
- `tmp/Headroom/prototype-sidecar/server.js`
- 新增：`tmp/Headroom/prototype-sidecar/lib/kpiAggregator.js`
- 新增：`tmp/Headroom/prototype-sidecar/scripts/verify-kpi-aggregation.js`
- `tmp/Headroom/prototype-sidecar/fixtures.jsonl`

**Absolute Ban List**
- 不修改 `tmp/Headroom/vscode-headroom-dashboard/src/headroomViewProvider.ts`。
- 不把 aggregation 寫進 VS Code extension host。
- 不新增外部資料庫。
- 不把聚合結果直接寫進 active `dashboard-metrics-schema.json`。

**Acceptance Criteria**
- Aggregator 可從 JSONL 計算：
  - retrieval: `retrieval_queries_total`、`retrieval_hit_rate`、`retrieval_zero_result_count`、`manual_override_count`
  - compression: `raw_input_tokens_est_total`、`compressed_input_tokens_est_total`、`compression_ratio_avg`、`compression_fallback_count`
  - quality: `quality_score_avg_1_5`、`followup_count_avg`、`manual_retry_count`
- 聚合函式對空檔案、壞 JSONL、缺欄位有明確 degraded result。
- `verify-kpi-aggregation.js` 對 fixture 執行並輸出可驗證 summary。
- 不產生 active promotion side effect。

**Dependencies**
- 依賴 `MCP-KPI-01`。
- `MCP-KPI-03` 依賴本票的 aggregation output。

**Defensive Warning**
Do NOT refactor outside this scope. Do NOT rename variables in unrelated files. Do NOT add TODO() or placeholder implementations. Once completed and successfully built, STOP and wait for Quality Gate verification.

### MCP-KPI-03：Sidecar Summary / Events Endpoint 補齊

**目的**：把 KPI 聚合結果透過 sidecar endpoint 暴露，讓 dashboard 能讀取 future KPI 來源，而不是讀本地檔案或硬編碼。

**Authority Reference**
- `pilot-integration.md`：broader supervisor target 包含 `/summary`、`/events/recent`。
- `MCP-TS-HEADROOM-IMPLEMENTATION.md`：`/summary`、`/events/recent` 未 promotion 前不得視為 active，但可作為 future data source。

**Target Files**
- `tmp/Headroom/prototype-sidecar/server.js`
- `tmp/Headroom/prototype-sidecar/lib/kpiAggregator.js`
- 新增：`tmp/Headroom/prototype-sidecar/scripts/smoke-summary-endpoints.js`
- `tmp/Headroom/pilot-docs/sidecar-data-flow.md`

**Absolute Ban List**
- 不更動 Headroom `/health`、`/stats` 的既有語意。
- 不讓 VS Code dashboard 在本票中顯示 KPI active。
- 不啟用 CodeGraph health。
- 不寫 Windows service 或安裝腳本。

**Acceptance Criteria**
- prototype sidecar 提供：
  - `GET /summary`
  - `GET /events/recent`
- `/summary` 回傳 retrieval / compression / quality summary 與 `source_status=prototype`。
- `/events/recent` 回傳最近 N 筆已驗證 event，壞資料不會讓 endpoint crash。
- smoke script 可啟動或連線 sidecar 並驗證 HTTP 200 / payload shape。

**Dependencies**
- 依賴 `MCP-KPI-02`。
- `MCP-KPI-04` 依賴本票 endpoint readiness。

**Defensive Warning**
Do NOT refactor outside this scope. Do NOT rename variables in unrelated files. Do NOT add TODO() or placeholder implementations. Once completed and successfully built, STOP and wait for Quality Gate verification.

### MCP-KPI-04：Dashboard Future KPI Data Source Wiring

**目的**：讓 VS Code dashboard 讀取 `/summary` 作為 future KPI preview 來源，但 promotion gate 未通過前仍不得顯示 active/green KPI。

**Authority Reference**
- `MCP-TS-HEADROOM-IMPLEMENTATION.md`：Panel B/C/E 只能 prototype、blocked 或 unavailable，直到 promotion gate 通過。
- `headroom-ts-merge-plan.md`：future KPI 不得一起切入 active dashboard。
- `dashboard-metrics-schema.json`：active schema 仍只驗證 health/stats。

**Target Files**
- `tmp/Headroom/vscode-headroom-dashboard/src/headroomViewProvider.ts`
- `tmp/Headroom/vscode-headroom-dashboard/src/services/dataFetcher.ts`
- `tmp/Headroom/vscode-headroom-dashboard/src/services/dashboardMapper.ts`
- `tmp/Headroom/vscode-headroom-dashboard/src/services/promotionGate.ts`
- `tmp/Headroom/vscode-headroom-dashboard/package.json`
- 由 build/package 產生：`tmp/Headroom/vscode-headroom-dashboard/out/**`、`vscode-headroom-dashboard-0.0.1.vsix`

**Absolute Ban List**
- 不改 view id：`headroomDashboard.view`。
- 不改 command id：`headroomDashboard.refresh`、`headroomDashboard.openConfig`。
- 不改 active `/health + /stats` 狀態判斷。
- 不把 `/summary` failure 顯示成 Headroom offline。

**Acceptance Criteria**
- Dashboard 可讀 `/summary`，但 panel source 明確標示 `futurePromotedContract` 或 `jsonlPilotContract`。
- `/summary` 不可用時，KPI panels 顯示 unavailable/prototype，不影響 `/health + /stats` 狀態。
- Promotion Gate checklist 不再用「missing」文案當成 runtime error；只列出 readiness evidence。
- `npm.cmd run build`、`npm.cmd run check`、`npm.cmd run package` PASS。
- `.vsix` 內確認包含新 UI，且不含舊 blocker 文案：`Schema promotion note missing`、`Event count below threshold`、`Source not yet promoted to active contract`。

**Dependencies**
- 依賴 `MCP-KPI-03`。
- 可與 `MCP-KPI-05` 並行，但不得在 docs/config promotion 前把 KPI 顯示為 active。

**Defensive Warning**
Do NOT refactor outside this scope. Do NOT rename variables in unrelated files. Do NOT add TODO() or placeholder implementations. Once completed and successfully built, STOP and wait for Quality Gate verification.

### MCP-KPI-05：Docs / Config Promotion Sync

**目的**：當 `/summary`、event evidence、schema note 都可用後，補齊 README / runbook / config 的 promotion 狀態同步，避免 UI 與文件說法漂移。

**Authority Reference**
- `MCP-TS-HEADROOM-IMPLEMENTATION.md`：promotion gate 要求 README / runbook / config 同步。
- `.agents/pilot/pilot.config.json`：目前 CodeGraph disabled，Headroom active endpoint 是 `/health + /stats`。
- `tmp/Headroom/README.md`、`pilot-runbook.md`：pilot workspace entry / operation guide。

**Target Files**
- `.agents/pilot/pilot.config.json`
- `.agents/pilot/README.md`
- `tmp/Headroom/README.md`
- `tmp/Headroom/pilot-docs/pilot-runbook.md`
- `tmp/Headroom/pilot-docs/kpi-promotion-gate.md`

**Absolute Ban List**
- 不在沒有 `MCP-KPI-01` 到 `MCP-KPI-04` 證據前改成 promoted。
- 不改 Android app。
- 不更新 `.claude/PROGRESS.md` 或 handoff。
- 不啟用 CodeGraph KPI；CodeGraph 另由 `MCP-KPI-06` 處理。

**Acceptance Criteria**
- 文件清楚區分：
  - active Headroom status：`/health + /stats`
  - promoted KPI source：`/summary` / `/events/recent`
  - prototype-only KPI：尚未過 gate 的項目
- `pilot.config.json` 若新增 promotion flags，必須保留原 `headroom` endpoint 與 `codegraph.enabled=false`。
- README/runbook 有 rollback 說明：若 KPI source degraded，dashboard 回到 health-only，不影響 Headroom 基本狀態。
- Git diff 不包含 Android app、`.claude/PROGRESS.md`、handoff。

**Dependencies**
- 依賴 `MCP-KPI-00` 到 `MCP-KPI-04`。

**Defensive Warning**
Do NOT refactor outside this scope. Do NOT rename variables in unrelated files. Do NOT add TODO() or placeholder implementations. Once completed and successfully built, STOP and wait for Quality Gate verification.

### MCP-KPI-06：CodeGraph KPI Optional Promotion

**目的**：只有在 Retrieval KPI 需要 CodeGraph health evidence 時，才把 CodeGraph 從 future integration 推進為可檢查的 optional KPI source。

**Authority Reference**
- `.agents/pilot/pilot.config.json`：目前 `codegraph.enabled=false`、`enable_codegraph_health=false`。
- `dashboard-metrics-schema.json`：CodeGraph health metrics 被 active schema 排除，保留待 promotion。
- `.agents/pilot/codegraph-queries.md`：CodeGraph query patterns 與 fallback guidance。

**Target Files**
- `.agents/pilot/pilot.config.json`
- `.agents/pilot/codegraph-queries.md`
- `tmp/Headroom/prototype-sidecar/server.js`
- `tmp/Headroom/vscode-headroom-dashboard/src/headroomViewProvider.ts`
- `tmp/Headroom/vscode-headroom-dashboard/src/services/promotionGate.ts`

**Absolute Ban List**
- 不把 CodeGraph health failure 顯示成 Headroom failure。
- 不預設啟用 `codegraph.enabled=true`；必須由明確 config flag 控制。
- 不修改 Android app。
- 不把單次 demo 成功當成 promotion pass。

**Acceptance Criteria**
- CodeGraph health 是 optional card / evidence，不影響 `/health + /stats` Headroom status。
- 只有 `codegraph.enabled=true` 且 `enable_codegraph_health=true` 時才查詢 CodeGraph health。
- CodeGraph unreachable 時 Retrieval KPI 顯示 hold/degraded evidence，不使 dashboard offline。
- `npm.cmd run build`、`npm.cmd run check`、`npm.cmd run package` PASS。

**Dependencies**
- 依賴 `MCP-KPI-03` 或後續正式 supervisor endpoint。
- 可在 `MCP-KPI-05` 後獨立執行。

**Defensive Warning**
Do NOT refactor outside this scope. Do NOT rename variables in unrelated files. Do NOT add TODO() or placeholder implementations. Once completed and successfully built, STOP and wait for Quality Gate verification.

## 實作順序
1. 先完成 TypeScript 化與基本型別邊界，再處理 panel 分區。
2. 再實作 risk evaluator，確保 panel 有一致資料輸入來源。
3. 最後接上 promotion gate，把 active/future scope 綁回統一治理流程。
4. 打包前以至少三種情境驗證：`health-only`、`jsonl pilot with partial metrics`、`future endpoint blocked`。

## 驗收情境
- **情境 A：/health 成功、/stats 成功，但無 promoted KPI schema** → Panel A/D 可正常，Panel B/C/E 顯示 prototype 或 unavailable，Panel F 為 `hold`。
- **情境 B：JSONL 已有事件，能算出部分 retrieval/compression 指標，但 schema note 尚未 promotion** → Panel B/C 可顯示 `jsonl pilot` 指標，Risk 3 可能提醒 `observability gap`，Panel F 不可 `pass`。
- **情境 C：retrieval hit rate 下降且 manual override 飆升** → Risk 1 觸發，status bar 降級為 degraded，Panel B 顯示 risk alert。
- **情境 D：compression ratio 高但 followup / retry 上升** → Risk 2 觸發，建議 fallback 至 F1/F2 或降低壓縮比例。
- **情境 E：未來 UI 區塊被開啟但 gate 未過** → 相關 panel 自動標示 prototype / blocked，不得顯示為 active。

## 補充說明
這張票的本質是把 dashboard 從「單一 provider 檔案上的 UI 邏輯」提升成「具型別、具治理、具 promotion 邊界」的實作基礎。對 VSCode 介面中的 Claude Code / Codex multi-agent pilot 而言，這比單純畫出更多卡片重要，因為真正要避免的是 extension、sidecar、JSONL、future endpoint 之間各自演化而失去一致性。
