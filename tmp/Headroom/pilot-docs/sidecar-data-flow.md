# Sidecar Data Flow & Architecture

**文件狀態**: Draft (Prototype Phase)

## 1. 架構定位
為滿足全量 KPI 儀表板的資料精確度要求，我們確立「Sidecar Supervisor」的最小資料責任，確保資料具備單一真實來源 (SSOT) 並可追溯，不依賴 Webview UI 端的不穩定邏輯。

## 2. 最小資料責任 (Responsibilities)
Sidecar 必須涵蓋以下五個核心模組：
1. **Event Ingest**: 提供 `POST /events/ingest` HTTP 介面，供 VSCode Extension 或 CLI Agent (Claude/Codex) 發送原始任務執行與 Fallback 事件。
2. **Metrics Store**: 負責將收到的 Events 落地。初期為降低門檻與保持輕量，採用 **JSONL (JSON Lines)** 檔案系統追加寫入；後續流量增加可平滑轉移至 **SQLite**。
3. **Health Evaluator**: 定期戳刺本地其他依賴進程 (如 Python-based CodeGraph 或 Inference 引擎)，提供給 Dashboard `/health` 與 `/stats` 端點。
4. **Headroom Adapter**: 攔截、壓縮 Prompt 並提供與遠端 Inference Provider 之間的通訊重試/降級機制。
5. **CodeGraph Adapter**: 與 Repository 的 AST 圖譜服務交握，執行跨檔案 Retrieval。

## 3. 真實 Event Flow 流程
從事件發起到呈現在 Dashboard 上的完整流轉程序如下：

### Phase 1: 觸發與採集 (VSCode / Agent 側)
- 使用者送出 Agent Prompt，或 Extension 偵測到手動切換 (Toggle Bypass) 時，主動呼叫 `provider.ingestEvent({type: '...', ...})`。
- 如果底層執行 `curl` 或 `cross_review.sh` 時遭遇 R2 Timeout，Shell script 會向 `http://127.0.0.1:8787/events/ingest` 推送失敗事件。

### Phase 2: 補齊與落地 (Sidecar Supervisor 側)
- Sidecar 收到 `/events/ingest` 請求後，會透過 Header 與內建 Config 自動補齊下列資訊：
  - `repo_context`: 自動補上 `SmartCameraApp1` 與 `pilot_config_path`。
  - `agent`: 判斷是 Claude 或 Codex。
  - `timestamp`: 精確校準時間。
- Sidecar 將補齊後的完整物件以一行的形式 (JSON serialization) 寫入 `events.jsonl` (或插入 SQLite `events` 表)。

### Phase 3: 聚合與呈現 (Dashboard 側)
- VSCode Dashboard 定時向 Sidecar 發出 `GET /summary` 與 `GET /events/recent` 請求。
- Sidecar 透過 `lib/kpiAggregator.js` 模組讀取 `events.jsonl`，在記憶體內執行聚合運算：
  - 計算 `retrieval_hit_rate` (成功筆數 / 總查詢)。
  - 計算 `compression_ratio` (已節省 token / 總輸入 token)。
  - 計算 `fallback_rate` 降級率與 p95 延遲。
- `/summary` 端點回傳原始數值與 `source_status: 'prototype'`，由 Dashboard 進行格式化與純視覺 UI 渲染。
- `/events/recent` 回傳最後 20 筆事件，供面板展示。

