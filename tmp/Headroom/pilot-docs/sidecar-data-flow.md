# Headroom Observability Supervisor Data Flow

**狀態**：Prototype / Preview。此 Supervisor 不代理 Claude、Headroom 或 CodeGraph 請求，也不產生模擬 LLM 回應。

## Runtime surface

- `GET /health`：Supervisor 與 evidence store 健康度。
- `POST /events/ingest`：接收符合 `retrieval-compression-kpi-schema.json` v0.2.0 的事件。
- `GET /summary`：分流 real/fixture 的聚合摘要。
- `GET /events/recent`：只回傳最近 20 筆 metadata。

`/v1/proxy/completions`、假 CodeGraph `/stats` 與隨機 F1/F2/F3 producer 不屬於 Supervisor。

## Trusted event flow

1. Producer 送出 `source_kind=real|fixture` 的完整 event。
2. `POST /events/ingest` 先受 128 KiB body limit 保護。
3. Server 以自身 UTC timestamp 覆寫 client timestamp，再呼叫 Schema validator 純函式。
4. 驗證失敗回 `422` 且不落盤；成功才以單行 JSON 追加至 JSONL store。
5. 讀取 store 時，壞 JSON 或不合 Schema 的舊資料會形成 `degraded` evidence，不會被靜默忽略，也不會使 endpoint crash。
6. Aggregator 分別輸出 `real_event_count`、`fixture_event_count`；只有 `promotion_real_event_count` 可作 promotion count，且只計 `source_kind=real`。

## Data minimization

`/events/recent` 僅允許 timestamp、event type、source kind、request id、agent/task type、fallback flag 與 token counts。不得回傳 prompt、完整 message、程式碼、API key、tool content 或 raw payload。

## Verification

```powershell
node tmp/Headroom/prototype-sidecar/scripts/verify-kpi-aggregation.js
node tmp/Headroom/prototype-sidecar/scripts/smoke-summary-endpoints.js
```

Smoke 會在 OS temp 建立隔離 JSONL store，透過真實 HTTP 驗證 ingest→store→summary、非法事件零寫入、fixture promotion count=0、壞 JSONL degraded，以及 recent metadata 白名單。
