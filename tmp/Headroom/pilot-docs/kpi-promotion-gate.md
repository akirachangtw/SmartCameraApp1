# KPI Schema Promotion Gate

**版本**: v1.0
**狀態**: Draft (Active Contract 仍為 Headroom-only `/health` + `/stats`)
**關聯文件**: `retrieval-compression-kpi-schema.json`, `sidecar-data-flow.md`

## 1. 目的
本文件定義從現行「Headroom-only 營運面板 (`/health` + `/stats`)」升級（Promote）至「全量指標觀測面板（Section 4 UI）」的嚴格條件。在達成這些門檻之前，禁止在 Active VS Code Dashboard 內放入假指標 (Fake KPI)。

## 2. Promotion Gate 升級條件
要將 Section 4 UI 帶入 `vscode-headroom-dashboard`，必須先滿足以下條件：

1. **Sidecar Supervisor 上線**：本地 Sidecar 必須實作並部署 `/events/ingest` 端點，且具備將接收到的事件轉寫入 `JSONL` 或 `SQLite` 的 Metrics Store 機制。
2. **Promoted Endpoint 確立**：Sidecar 必須實作提供聚合運算的端點，建議命名為 `/summary_v2` 或於 OpenAPI 規範中將 `/summary` 標記為 Promoted。該端點的回傳值必須能產出 `retrieval_hit_rate`、`compression_ratio`、`fallback_rate`、`end_to_end_latency` 四大核心 KPI。
3. **資料正確性驗證**：產生的指標必須是從 Metrics Store 動態聚合而來，並經過整合測試驗證。
4. **文件更新**：升級時必須同票更新 `.agents/pilot/pilot.config.json` 與 `tmp/Headroom/README.md`，將 Active Scope 正式從 `headroom_only` 切換為 `full_metrics`。

## 3. 降級分級標準 (Fallback R0-R3)
為確保 Dashboard 的 Fallback 指標具備真實意義，定義 Sidecar 處理過程的 Fallback R0-R3 等級，並對應至不同的系統行為：

| 等級 | 名稱 | 描述與觸發條件 | Dashboard / Sidecar 對應行為 |
|---|---|---|---|
| **R0** | Healthy (Active) | CodeGraph 與 Headroom 皆正常運作。 | 走正常 Retrieval + Compression 流程，紀錄完整的 Hit Rate 與 Compression Ratio。 |
| **R1** | Degraded / Bypass | Compression 引擎無法啟動、遭遇例外或使用者手動 Bypass。 | 直接透傳 (Pass-through) 原生 Prompt。Compression KPI 不會更新，事件紀錄為 `type: fallback, level: R1`。 |
| **R2** | Transient Error | Headroom Local 代理 Timeout 或服務崩潰。 | 退回至原生 `ripgrep` 搜尋機制，停用 CodeGraph 擴充。觸發 Extension 重新發起請求，紀錄 `level: R2`。 |
| **R3** | Offline | Sidecar 完全斷線，連 `/health` 都無法連線。 | Dashboard Reachability 亮紅燈 (Offline)。使用者必須依賴單純的雲端對話，失去所有本地端攔截代理與監測能力。 |

## 4. UI 顯示規範
在 Promotion Gate 尚未完全通過 (Pass) 之前，VS Code Dashboard 的 UI 呈現必須遵守以下規範：
1. **禁止假指標 (No Fake KPI)**：未取得實際資料的面板 (Panel B, C, E, F) 不得寫死假的數據或以綠燈顯示。
2. **預覽標籤 (Prototype/Preview Badge)**：未來指標 (Future KPIs) 包括檢索率、壓縮比、品質分數等，必須明確標記為 `PROTOTYPE`、`BLOCKED` 或 `UNAVAILABLE`，不可偽裝成啟用狀態。
3. **安全降級 (Safe Downgrade)**：即使這些未來指標因為後端 Sidecar 未啟動或無資料而無法取得，面板必須安靜降級為未啟用/無資料狀態，且絕不能影響 `/health` 與 `/stats` 所維護的 Headroom 核心 reachability (亮紅燈/離線) 判定。

