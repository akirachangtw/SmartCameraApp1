# Headroom Compression Payload Rules

**文件範圍**: 規範 Pilot 階段 Headroom Proxy 的內容壓縮攔截對象與 Fallback 策略 (F1/F2/F3)。

## 1. 壓縮對象收斂 (Target Selection)

Headroom 在 Week 3 的實作中，**僅允許**對以下三類高冗餘的 payload 進行壓縮：

1. **Logs**: 如編譯錯誤輸出、Server Runtime Stacktrace。
2. **Tool Outputs**: 各類腳本執行結果、大部頭系統資訊傾印。
3. **Search Results**: `ripgrep` 或 `codegraph` 回傳的大量搜尋匹配片段。

### 絕對排除對象 (Forbidden Targets)
為確保模型的 Code Reasoning 邏輯推理能力不受損，以下內容**嚴禁壓縮**，必須直接透傳 (Bypass)：
- Conversation History (對話歷史紀錄)。
- 完整的 Code Context (原始碼原文)。
- 用於精密差異比對 (Diff) 審查的完整 Patch 內容。

## 2. 壓縮策略與 Fallback 分級 (F1 / F2 / F3)

當壓縮流程遭遇品質下降或系統異常時，依照以下規則進行退回與降級，確保 Agent 的主工作流不被阻斷。

### F1: 降低壓縮比 (Lower Compression Ratio)
- **觸發情境**: 壓縮後的資訊丟失關鍵上下文，導致 Agent 無法正確判斷出錯行號，出現反覆追問。
- **對應行為**: Headroom Proxy 調低壓縮力度，保留更多原文邊界 (例如保留前後文的各 10 行 Context，不再只截取 Error 行)。

### F2: 局部壓縮放行 (Selective Bypass)
- **觸發情境**: 任務混雜了 Search Results 與 Code Snippets，導致 Headroom 難以精準識別，引發誤壓 Code Block 的風險。
- **對應行為**: 僅對具有強烈特徵的純 Logs / Search Results 進行壓縮，對於任何可能夾雜 Code-related 特徵的區塊一律放棄壓縮，直接放行原文。

### F3: 完全直通上游 (Full Bypass)
- **觸發情境**: 
  - 任務本身極度依賴完整原文 (例如：脆弱 Bug 重現、精密的 AST Diff 審查)。
  - Headroom Proxy 發生 Timeout、OOM 異常，或回報 Stats Degraded。
- **對應行為**: 完全停止文字內容攔截與壓縮，所有 Request 全文直通上游模型。Headroom 僅維持 `/health` 探針檢查。

## 3. Proxy 接線原則 (Routing Principles)
1. VSCode Claude Code / Codex 送出請求。
2. Prompt Assembly Layer 解析請求，僅抽取符合 Targets (Logs/Tool Outputs/Search Results) 的區塊。
3. 將該區塊投遞給 Headroom Proxy。
4. Proxy 傳回壓縮後字串，再組合回 Prompt 送往模型端。
5. **安全網**: 只要 Proxy 的回應超過 2000ms 或發生 HTTP Error，立即無條件觸發 **F3**。

## 4. Fallback Reason Naming

compression event 統一使用以下 `compression_fallback_reason`：

- `none`
- `quality_drop`
- `timeout`
- `proxy_error`
- `full_text_required`

Pilot 期間不應自行擴充近義名稱，避免 Week 5 匯總時產生 naming drift。

## 5. Draft Event Field Naming

若 compression event 尚未完成正式 schema promotion，仍應以以下 draft 欄位命名記錄。這些欄位不得寫入 active `headroom_only` dashboard schema，也不得作為 active dashboard KPI。

```json
{
  "used_headroom": true,
  "compression_target": "logs",
  "raw_input_tokens_est": 12400,
  "compressed_input_tokens_est": 2800,
  "compression_ratio": 0.226,
  "compression_fallback_level": "none",
  "compression_fallback_reason": "none"
}
```

### Required Draft Fields

- `used_headroom`
- `compression_target`
- `raw_input_tokens_est`
- `compressed_input_tokens_est`
- `compression_ratio`
- `compression_fallback_level`
- `compression_fallback_reason`

### Optional Draft Fields

- `request_id`
- `agent_entry`
- `task_type`
- `quality_score`
- `followup_count`
- `notes`

## 6. Manual Logging Template

若尚未有正式 sidecar ingest，可先用以下結構化格式記錄：

```text
[compression]
request_id=req_123
used_headroom=true
compression_target=logs
raw_input_tokens_est=12400
compressed_input_tokens_est=2800
compression_ratio=0.226
compression_fallback_level=none
compression_fallback_reason=none
note=stack trace successfully compressed
```

## 7. Practical Examples

### Compress

- 長篇 stack trace，只需摘要主要 error path。
- 大量 search result 候選，只需濃縮 top matches。
- 冗長 command output，但內容重複且可摘要。

### Do Not Compress

- 逐行 diff review。
- 使用者要求完整 log。
- fragile reproduction 必須保留完整 payload。
- 需要逐行精讀的大段 code。

## 8. Related Pilot Files

- `.agents/pilot/headroom-sampling.md`
  - Headroom sampling and quality evaluation rules.
- `.agents/pilot/codegraph-queries.md`
  - CodeGraph query patterns and retrieval fallback notes.
- `.agents/pilot/pilot.config.json`
  - Active pilot endpoint and dashboard wiring config.
