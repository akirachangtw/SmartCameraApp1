# Pilot Runbook

本 runbook 定義 Headroom dashboard pilot 的每日檢查、incident handling、fallback 判斷與 rollout checklist。其目的不是取代 repo 治理文件，而是提供操作層的固定節奏，讓自己或未來協作者在 sidecar、dashboard、config wiring 出狀況時有統一處置方式。

## 使用範圍

本 runbook 適用於以下範圍：

- VS Code 內建 Webview API 的 sidebar dashboard。
- `.agents/pilot/pilot.config.json` 作為 pilot wiring SSOT。
- Headroom sidecar 的 `/health`、`/stats` 可視化與 fallback 狀態燈號。
- 未來可擴充到 CodeGraph health card，但目前不把 CodeGraph 當 P0 值班項目。

## Endpoint 決策

`pilot-integration.md` 早期草案提到 `/summary` 作為聚合摘要端點；本輪 repo-decision 已將 Headroom dashboard 的正式輪詢面收斂為 `/health` 與 `/stats`。因此 Phase 0/1 runbook、dashboard schema 與 extension wiring 一律以 `/stats` 表示統計摘要，不再把 `/summary` 當作驗收條件。

## KPI Schema 擴展門檻

`dashboard-metrics-schema.json` 是目前 active contract，只驗證 Headroom `/health` 與 `/stats` 的操作狀態。`retrieval-compression-kpi-schema.json` 是下一階段 draft companion schema，用來定義 `retrieval_hit_rate`、`compression_ratio`、`fallback_rate`、`end_to_end_p95_latency_ms` 的事件結構。

擴展到 KPI dashboard 前，必須先完成：

- Headroom-only dashboard 在 `.agents/pilot/pilot.config.json` 存在、缺失、JSON 壞掉三種情境下都能正常顯示狀態。
- `/health` 與 `/stats` 的綠/黃/紅/灰燈規則已被手動或自動測過。
- extension 仍只負責展示與最小控制，不把 retrieval/compression 判定邏輯塞進 VS Code extension。
- sidecar 或 metrics aggregator 已能穩定產出 KPI snapshot，再把 draft schema 提升為 active schema。

## 每日檢查清單

每天開始工作前，依序檢查：

- `AGENTS.md` 是否仍是 repo-level prompt contract，沒有被錯誤搬移或替換。
- `.agents/pilot/pilot.config.json` 是否存在，且 JSON 可解析。
- dashboard 是否顯示正確 `Config Path`、`Endpoint Base`、`Health Path`、`Stats Path`。
- Headroom `/health` 是否可達，狀態燈號是否為綠燈。
- `/stats` 是否可達；若失敗但 `/health` 正常，標記為黃燈而非紅燈。
- `fallback_active` 是否為 `false`；若為 `true`，需記錄原因。
- extension 是否仍從 workspace 讀 config，而非退回內建硬編碼值。

## 狀態燈號規則

狀態燈號僅用於操作判斷，不用於商業 KPI。

| 燈號 | 條件 | 意義 | 動作 |
|---|---|---|---|
| 綠燈 | `/health` 200 且 `/stats` 可達，`fallback_active=false` | sidecar 與 dashboard wiring 正常。  | 正常使用。 |
| 黃燈 | `/health` 正常，但 `/stats` 失敗；或 config 存在警告但已 fallback | 核心可用，觀測不完整。  | 可繼續使用，但需記錄。 |
| 紅燈 | `/health` 不可達、config 致命錯誤、連續失敗超過閾值 | 核心 sidecar 無法信任。  | 立即切回不依賴 dashboard 的工作流。 |
| 灰燈 | dashboard 尚未初始化或未開始輪詢 | 尚未有有效狀態。 | 先檢查 extension 與 config。 |

## Sidecar Health Flow

1. Extension 啟動。
2. 嘗試讀取 `.agents/pilot/pilot.config.json`。
3. 若 config 存在且合法，採用 `headroom.proxy_url`、`health_path`、`stats_path`、`poll_interval_ms`。
4. 若 config 缺失或 JSON 壞掉，寫入 `configError`，並 fallback 到 built-in defaults。
5. 先檢查 `/health`。
6. 若 `/health` 失敗，直接標紅燈並停止信任 `/stats` 結果。
7. 若 `/health` 正常，再檢查 `/stats`。
8. `/stats` 失敗時標黃燈，不升級成紅燈，除非已連續失敗且伴隨 `/health` 波動。

## Incident 處置

### Incident A：Dashboard 顯示灰燈

檢查順序：

- Extension Development Host 或安裝版 `.vsix` 是否已正確載入。
- sidebar view id 是否仍為 `headroomDashboard.view`。
- `pilot.config.json` 是否在目前 workspace root 下的 `.agents/pilot/`。

若只是 view 沒註冊，先修 extension。若 view 已註冊但 state 沒推送，先看 provider 是否啟動輪詢。

### Incident B：Config Error

典型原因：

- `pilot.config.json` 路徑錯誤。
- JSON 語法錯誤。
- 缺少 `headroom.proxy_url` 等必要欄位。

處置方式：

- 修正 JSON 格式。
- 保留 fallback，但把 `configError` 記錄進 metrics event。
- 不要把 config 移到 `.claude/skills` 或 `.codex/skills` 企圖繞過問題。

### Incident C：`/health` 失敗

這是 P0 incident，因為 Headroom sidecar 核心不可達。

處置方式：

- 先檢查本地 sidecar process 是否啟動。
- 若你使用 bash 腳本啟動，重新跑 sidecar 啟動命令。
- 若仍失敗，將 dashboard 標記為紅燈並切回不依賴 sidecar 的原始工作流。

### Incident D：`/stats` 失敗但 `/health` 正常

這是 P1 incident，不是 P0。因為 sidecar 核心仍存活，只是觀測資訊不完整。

處置方式：

- 標黃燈。
- 保留現有工作流。
- 記錄失敗次數，若與 payload parsing 錯誤有關，優先檢查 extension parser 而不是 sidecar process。

## 每日收工前檢查

- 當日是否發生 `fallback_active=true` 事件。
- 是否有 config 解析錯誤。
- 是否有 `/health` 紅燈事件。
- 是否有 `/stats` 黃燈事件累積。
- 是否需要調整 `poll_interval_ms`，避免過度輪詢。

## Rollout Checklist

### Phase 0：本地單人驗證

- `pilot.config.json` 已建立於 `.agents/pilot/`。
- dashboard 可讀 config。
- dashboard 在 config 缺失時可 fallback。
- dashboard 在 config JSON 壞掉時可顯示 `configError` 而不崩潰。
- `/health`、`/stats` 可正確反映燈號。

### Phase 1：本地 `.vsix` 使用

- extension 已打包成 `.vsix`，不依賴 Marketplace 發布。
- 安裝後仍能從 workspace 讀 `pilot.config.json`。
- 未把 repo config 打包成 extension 內硬編碼。
- 日常 coding loop 不因 dashboard 壞掉而中斷。

### Phase 2：協作者試用

- 提供本 runbook 與 schema。
- 協作者知道如何判斷綠/黃/紅/灰燈。
- 協作者知道 P0 與 P1 的差別。
- 協作者知道 fallback 啟動後如何繼續工作。

## 不做事項

目前不做：

- 不將 sidecar start/stop lifecycle 綁進 extension。
- 不把 CodeGraph health card 列為本輪強制交付。
- 不把 pilot config 存進 runtime adapter layer。
- 不把 dashboard 當成唯一可觀測入口。
