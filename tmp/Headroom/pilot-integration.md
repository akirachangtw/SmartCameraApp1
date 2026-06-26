# Pilot Integration：VSCode 插件 + 本地 Sidecar 觀測面板與部署 / 回退策略範本

> 檔案治理 / 來源說明：本文件結合既有工作流脈絡、VS Code Webview / extension 測試官方文件、OpenTelemetry semantic conventions，以及先前確立的 Headroom / CodeGraph Phase 1 架構原則整理而成。

## 1. 文件目的

本文件定義 Phase 1 pilot 的兩個交付重點：

1. VSCode 插件與本地 Sidecar 的觀測效能面板（observability dashboard）應該呈現哪些指標。
2. pilot 導入時的部署步驟、切流方式、回退規則，以及最小驗收條件。

本文件的定位是實作範本，可直接作為 repo 內 `pilot-integration.md` 初稿，再依實際端點、port、腳本名稱與作業系統差異微調。

## 2. 設計原則

觀測面板的設計不應追求「全量 APM」，而應聚焦回答四個工程問題：

- 找檔有沒有更準。
- 上下文有沒有變短。
- 回答有沒有變慢。
- 系統壞掉時能不能立刻回退。

依此原則，pilot 儀表板以 golden signals 風格為核心：吞吐、延遲、錯誤率、品質代理指標，再補上 Token / compression / retrieval 專屬欄位。OpenTelemetry 強調語意一致的 naming 與 attributes，適合拿來約束 extension host、webview、sidecar、proxy 間的同名指標。

## 3. 觀測架構

### 3.1 元件分層

```text
VSCode Extension Host
  ├─ command hooks
  ├─ status bar summary
  ├─ output channel logs
  └─ WebviewViewProvider dashboard

Local Sidecar Supervisor
  ├─ CodeGraph client adapter
  ├─ Headroom proxy adapter
  ├─ health checker
  ├─ metrics aggregator
  └─ local event store (jsonl/sqlite)

Optional Export Layer
  ├─ OpenTelemetry exporter
  └─ CSV / JSON snapshot exporter
```

VSCode extension 負責「展示」與「最小控制」，sidecar 負責「聚合」與「判定」。這樣可以避免大量 network / parser / health logic 塞進 extension host，降低 UI 卡頓與維護複雜度。

### 3.2 資料流

1. Extension 在命令啟動、任務送出、回應完成、fallback 發生時送事件給 sidecar。
2. Sidecar 統一補齊 request_id、repo、agent、task_type、latency、compression、fallback 欄位。
3. Dashboard Webview 每 2 到 5 秒拉一次聚合摘要，或透過 postMessage 接收增量更新。VS Code Webview 官方支援用 `WebviewViewProvider` 建構自訂檢視，適合做常駐側邊面板。
4. 若需要長期分析，再由 sidecar 匯出到 JSONL、SQLite 或 OpenTelemetry backend。

## 4. VSCode 插件面板指標設計

### 4.1 面板區塊

建議 Webview dashboard 分為 5 塊，避免一上來做成複雜 Grafana 替代品。

#### A. Session 概況

顯示目前 repo、目前 agent、最近 30 分鐘任務量、成功率與粗估 Token 狀態。這一塊主要給你快速判斷「現在這條工作流是健康還是該切回原生模式」。

建議指標：

- active_repo
- active_agent_entry (`claude-code` / `codex` / `desktop`)
- session_requests_30m
- success_rate_30m
- fallback_rate_30m
- token_saved_est_30m
- current_mode (`native` / `pilot` / `degraded` / `bypass`)

#### B. Retrieval 品質

這一塊專看 CodeGraph 是否真的幫助定位。若 retrieval 本身不準，再高壓縮比都沒有意義。

建議指標：

- retrieval_queries_total
- retrieval_hit_rate
- retrieval_topk_avg
- retrieval_latency_p50_ms
- retrieval_latency_p95_ms
- retrieval_fallback_count
- retrieval_zero_result_count
- retrieval_manual_override_count

#### C. Compression / Token 效益

這一塊評估 Headroom 是否值得保留在主路徑。

建議指標：

- raw_input_tokens_est_total
- compressed_input_tokens_est_total
- compression_ratio_avg
- compression_ratio_p50
- compression_ratio_p95
- cacheable_prefix_ratio
- cache_hit_observed_rate
- compression_bypass_count
- compression_fallback_f1_count
- compression_fallback_f2_count
- compression_fallback_f3_count

#### D. 回應效率

這一塊回答「有沒有因多一層 sidecar / proxy 而變慢」。

建議指標：

- first_token_latency_p50_ms
- first_token_latency_p95_ms
- end_to_end_latency_p50_ms
- end_to_end_latency_p95_ms
- sidecar_overhead_p50_ms
- sidecar_overhead_p95_ms
- proxy_timeout_count
- upstream_error_rate

#### E. 穩定性與品質代理

Pilot 階段無法完全自動量化答案品質，所以需要以代理指標近似。

建議指標：

- followup_count_avg
- repeated_question_rate
- answer_rejected_count
- user_manual_retry_count
- quality_score_avg_1_5
- incident_open_count
- last_fallback_reason
- sidecar_health_status
- codegraph_health_status
- headroom_health_status

### 4.2 面板視覺層級

建議採三層顯示：

- 第一層：4 個 KPI 大卡，`retrieval hit rate`、`compression ratio`、`fallback rate`、`end-to-end p95 latency`。
- 第二層：趨勢圖，顯示最近 30 分鐘與最近 24 小時。
- 第三層：事件流與 fallback feed，列出最後 20 筆異常或切換事件。

這樣的資訊密度對側邊面板較合理，也符合你偏好的高擬真互動視圖風格，但不會因圖表過多造成 Webview 過重。

## 5. 指標命名與事件模型

### 5.1 指標命名建議

建議全部採 snake_case 或 dot-separated 慣例，並維持 extension / sidecar / proxy 一致。OpenTelemetry 文件強調 semantic conventions 的價值在於跨元件的一致命名與可比對性。

建議前綴：

- `pilot.request.*`
- `pilot.retrieval.*`
- `pilot.compression.*`
- `pilot.fallback.*`
- `pilot.health.*`
- `pilot.quality.*`

### 5.2 Event Schema 範本

```json
{
  "ts": "2026-06-26T13:58:00+08:00",
  "request_id": "req_01J...",
  "repo": "my-repo",
  "agent_entry": "claude-code",
  "task_type": "bug_fix",
  "mode": "pilot",
  "used_codegraph": true,
  "used_headroom": true,
  "retrieval_latency_ms": 142,
  "retrieval_result_count": 6,
  "raw_input_tokens_est": 12400,
  "compressed_input_tokens_est": 2800,
  "compression_ratio": 0.226,
  "first_token_latency_ms": 2100,
  "end_to_end_latency_ms": 12800,
  "followup_count": 1,
  "fallback_level": "none",
  "fallback_reason": null,
  "quality_score": 4,
  "cache_prefix_stable": true,
  "cache_hit_observed": true
}
```

### 5.3 必備維度（attributes）

- repo
- branch
- agent_entry
- task_type
- mode
- os
- sidecar_version
- codegraph_version
- headroom_version
- fallback_level
- fallback_reason

這些 attributes 會決定你之後能不能分析「只有某個 repo / 某個 agent / 某個 sidecar 版本出問題」的模式。

## 6. VSCode 插件實作建議

### 6.1 UI 元件

建議插件至少提供 4 個 UI 入口：

- Status Bar：顯示目前模式與健康度，例如 `Pilot: Healthy`、`Pilot: Degraded`。
- Command Palette：提供 `Pilot: Open Dashboard`、`Pilot: Toggle Bypass`、`Pilot: Recheck Health`。
- Output Channel：保留文字事件流，便於排障。
- WebviewViewProvider：常駐側欄 dashboard。

### 6.2 插件與 Sidecar 邊界

插件只做這些事：

- 收集 IDE 內可得事件。
- 發送 command / request lifecycle event 到 sidecar。
- 呈現 sidecar 聚合結果。
- 接收使用者手動切換 bypass / degraded mode 指令。

插件不要做這些事：

- 自己維護大量歷史統計。
- 自己判斷 retrieval / compression 健康。
- 自己直接串多個 sidecar service。
- 自己寫複雜 Token 推估核心邏輯。

### 6.3 測試策略

VS Code 官方建議使用專用的 extension test 環境來跑擴充測試；Webview 測試則應把邏輯拆開，讓大部分邏輯能在不依賴 VS Code API 的情況下單元測試。

Pilot 階段建議測試分三層：

- Unit：metrics reducer、health decision、fallback rule matcher。
- Integration：extension host ↔ sidecar message flow。
- UI smoke：webview 是否能載入、刷新、接收更新。

## 7. 本地 Sidecar 設計

### 7.1 Sidecar 模組

本地 sidecar 建議拆成 5 個模組：

- `event-ingest`：收 extension 事件。
- `metrics-store`：保存最近 N 小時事件，可先用 JSONL 或 SQLite。
- `health-evaluator`：判定 `healthy / degraded / bypass-recommended`。
- `adapter-codegraph`：封裝 CodeGraph 查詢與 timeout。
- `adapter-headroom`：封裝 proxy health、compression 統計與 bypass。

### 7.2 健康判定規則

最小健康模型建議如下：

- `healthy`：fallback_rate < 5%，p95 latency 在門檻內，health check 全綠。
- `degraded`：fallback_rate 介於 5% 到 20%，或單一元件 latency 異常。
- `bypass-recommended`：fallback_rate > 20%，或 proxy / retrieval 連續失敗。
- `offline`：sidecar 無法聚合或主要依賴不可用。

Sidecar 應只提供「建議模式」，最終是否切換由使用者或插件設定決定，避免 pilot 階段自動切流造成過度驚訝。

## 8. 部署策略範本

### 8.1 前置條件

- 已有至少 1 個 pilot repo。
- repo 內已有 `AGENTS.md` 或等效 prompt contract。
- CodeGraph 可以完成首次 indexing。
- Headroom proxy 可健康回應。
- VSCode 插件能開啟 dashboard 並顯示本地 sidecar health。

### 8.2 部署步驟

#### Step 1：建立本地設定檔

建立 `pilot.config.json`：

```json
{
  "mode": "pilot",
  "repoAllowlist": ["my-repo"],
  "sidecarBaseUrl": "http://127.0.0.1:8765",
  "codegraphBaseUrl": "http://127.0.0.1:8781",
  "headroomProxyBaseUrl": "http://127.0.0.1:8787",
  "dashboardRefreshMs": 3000,
  "healthThresholds": {
    "fallbackRateWarn": 0.05,
    "fallbackRateBypass": 0.20,
    "p95LatencyWarnMs": 8000,
    "p95LatencyBypassMs": 15000
  }
}
```

#### Step 2：啟動 Sidecar Supervisor

先啟 sidecar，再檢查其依賴：

1. 啟動 metrics store。
2. 啟動 CodeGraph adapter health check。
3. 啟動 Headroom adapter health check。
4. 驗證 sidecar `/health` 與 `/summary`。

#### Step 3：安裝 / 啟用 VSCode 插件

- 插件啟動後，先只顯示 health 與原生模式。
- 不立即切流。
- 使用 `Pilot: Recheck Health` 確認三個狀態：sidecar、CodeGraph、Headroom。

#### Step 4：以單 repo 切入

- 僅對 allowlist repo 啟用 pilot。
- 初始只對 `code search` 與 `log-heavy debugging` 任務開啟新路徑。
- 單檔小修改先不強制走 retrieval + compression。

#### Step 5：觀察 3 至 5 天

- 每天看 fallback feed。
- 抽樣核對 10 筆請求的 retrieval relevance 與 compression 後內容完整度。
- 不要在未完成抽樣前擴到第二個 repo。

## 9. 回退策略範本

### 9.1 回退層級

#### R0：僅視覺告警

條件：短暫高延遲，但成功率與品質尚可。
行為：dashboard 黃燈、status bar 顯示 warning，不切模式。

#### R1：功能降級

條件：CodeGraph 或 Headroom 單一路徑不穩，但另一條仍可用。
行為：

- 關閉 retrieval，保留 compression；或
- 關閉 compression，保留 retrieval。

#### R2：Pilot bypass

條件：fallback_rate 持續偏高、p95 latency 失控、sidecar 健康異常。
行為：

- 所有請求回到原生工作流。
- extension 只保留觀測與告警。
- sidecar 繼續收集 health，不參與主請求路徑。

#### R3：整體停用

條件：sidecar 造成明顯 IDE 擾動，或本地資源消耗過高。
行為：

- 停用插件整合。
- 關閉 sidecar。
- 僅保留 log 與 incident note。

### 9.2 回退觸發條件

| 條件 | 建議回退 |
|---|---|
| retrieval p95 latency > 門檻且連續 10 次 | R1 |
| compression fallback F3 連續發生 | R1 或 R2 |
| overall fallback_rate > 20% | R2 |
| proxy timeout burst | R2 |
| dashboard 顯示 sidecar unhealthy > 5 分鐘 | R2 |
| VSCode extension host 明顯卡頓 | R3 |

### 9.3 回退操作命令範本

```bash
# 僅停用 compression
pilotctl mode set --compression=off --retrieval=on

# 僅停用 retrieval
pilotctl mode set --compression=on --retrieval=off

# 全面 bypass
pilotctl mode set --pilot=bypass

# 回到健康檢查模式
pilotctl health recheck
```

## 10. 驗收清單

### 10.1 儀表板驗收

- 可以在 Webview 正常開啟。
- 5 大區塊資料可刷新。
- status bar 會反映 `healthy / degraded / bypass`。
- fallback feed 有最近事件。
- 切換 repo / agent 後數據不混淆。

### 10.2 Sidecar 驗收

- `/health`、`/summary`、`/events/recent` 可用。
- CodeGraph adapter timeout 可被正確標示。
- Headroom proxy bypass 可人工切換。
- metrics store 重啟後不會毀損既有觀測資料。

### 10.3 Pilot 驗收

- 單 repo pilot 可跑滿 3 天。
- 至少 20 筆請求可供比較 before / after。
- 有明確 fallback 記錄。
- 有至少一次人工 bypass 驗證成功。

## 11. 建議的後續附件

本文件完成後，建議再補兩份附件：

- `dashboard-metrics-schema.json`：定義所有 event / metrics 欄位型別。
- `pilot-runbook.md`：給自己或未來協作者的 incident 與每日檢查清單。

若你要把它真正變成開發 SSOT，下一步最值得做的是再產一版互動式 HTML 規格視圖，將 dashboard 區塊、fallback 狀態燈號、sidecar health flow 與 rollout checklist 可視化。
