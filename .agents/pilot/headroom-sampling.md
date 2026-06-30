# Headroom Compression Quality Sampling

**文件範圍**: 制定 Week 3 到 Week 5 期間，對 Headroom 壓縮成效進行主客觀抽樣評鑑的標準與紀錄格式。

## 1. 抽樣目標 (Sampling Objectives)
確保壓縮技術能在大幅降低 Token 消耗的情況下，不引起 Agent 回覆延遲、不增加無效的追問 (Followup) 頻率，並維持解答的準確度。以利於 Week 5 決定是否推廣此機制 (Go/Hold/Rollback)。

## 2. 抽樣對象與頻率
- **對象情境**:
  - Bug Fix (大量 Stacktrace Log)。
  - Tracing & Refactoring (包含廣泛的 Search Results)。
  - Script Execution (龐大的 Tool Outputs)。
- **頻率要求**: 每日隨機抽取至少 5 筆上述情境的請求紀錄，在 Week 5 前需至少累積 20 筆有效驗證樣本。

## 3. 比較與觀測維度
每筆樣本需包含以下量化與質化數據：
1. **Token 節省度估算**: 壓縮前原始 Token (`raw_input_tokens_est`) vs. 壓縮後 Token (`compressed_input_tokens_est`)，計算出 `compression_ratio`。
2. **延遲與互動次數**: 觀察首次回覆的秒數 (Latency)，以及完成該任務所需的額外 Followup 次數。
3. **Fallback 觸發狀況**: 記錄本次請求是否有觸發 `F1` / `F2` / `F3` 退回機制及其原因。
4. **主觀品質評分 (1-5)**:
   - 5分: Agent 一次就精準定位問題。
   - 3分: Agent 需要多問一兩次來補齊被壓縮掉的上下文，但最終順利解決。
   - 1分: Agent 完全迷失方向，被迫手動關閉 Headroom (觸發 F3) 才能解決。

## 4. 人工 / 半自動紀錄模板 (Draft Schema)

這些紀錄可使用 JSONL 或 Markdown 表格記錄；在 schema promotion 前，所有欄位都只屬於 pilot draft，不得寫入 active `headroom_only` dashboard schema。

| Task ID | 情境 | Raw Tokens | Comp. Tokens | Ratio | Fallback | 品質評分 | Followups | 備註 |
|---|---|---|---|---|---|---|---|---|
| `req-001` | Bug Fix (Log) | 12400 | 2800 | 77.4% | None | 5 | 0 | Error stack 壓縮精準，秒抓 Bug。 |
| `req-002` | Search Tracing | 8000 | 4500 | 43.7% | F2 | 3 | 2 | 誤壓到部分 Source Code，退回 F2 才解決。 |
| `req-003` | Tool Output | 25000 | 25000 | 0.0% | F3 | 1 | 1 | Proxy Timeout，觸發 F3 全文直通。 |

## 5. Required Fields

最小抽樣欄位如下：

| 欄位 | 說明 |
|---|---|
| date | 抽樣日期 |
| request_id | 任務識別碼 |
| repo | 專案名稱 |
| agent_entry | claude-code / codex / desktop |
| task_type | bug_fix / debugging / tracing / review |
| compression_target | logs / tool_outputs / search_results |
| used_headroom | 是否經 Headroom |
| raw_input_tokens_est | 原始 Token 粗估 |
| compressed_input_tokens_est | 壓縮後 Token 粗估 |
| compression_ratio | 壓縮比例 |
| first_response_latency_ms | 首次回覆延遲 |
| followup_count | 二次補料次數 |
| compression_fallback_level | none / F1 / F2 / F3 |
| compression_fallback_reason | none / quality_drop / timeout / proxy_error / full_text_required |
| quality_score | 1-5 主觀品質評分 |
| notes | 特殊觀察 |

## 6. Quality Scoring

統一使用 1-5 分制：

- **5**：答案完整、定位準確、幾乎不需補料。
- **4**：答案大致正確，少量補充即可完成。
- **3**：可用，但需要明顯追問或手動查證。
- **2**：遺漏關鍵資訊，造成顯著額外工作。
- **1**：答案失真或誤導，應視為不可接受樣本。

## 7. Priority Review Triggers

以下樣本應優先被人工審查：

- `compression_fallback_level != none`
- `quality_score <= 3`
- `followup_count >= 2`
- latency 明顯高於平時
- 使用者主觀反饋為「漏資訊」或「變笨」

## 8. JSONL Template

```json
{"date":"2026-06-29","request_id":"req_001","task_type":"debugging","compression_target":"logs","used_headroom":true,"raw_input_tokens_est":12400,"compressed_input_tokens_est":2800,"compression_ratio":0.226,"followup_count":0,"compression_fallback_level":"none","compression_fallback_reason":"none","quality_score":5,"notes":"stack trace summary was sufficient"}
```

## 9. Daily Review Workflow

建議每日按以下流程操作：

1. 從當日請求中挑出 5 筆樣本。
2. 補齊 token、latency、fallback、quality_score。
3. 檢查是否出現：
   - 壓縮比例高，但 quality_score 降低
   - followup_count 上升
   - F2 / F3 次數增加
4. 若出現異常，回查 `.agents/pilot/headroom-payload-rules.md` 是否需要調整壓縮範圍或 fallback 條件。

## 10. Week 5 Decision Input

到 Week 5 前，至少要能回答：

- 平均壓縮比例是否有效。
- 是否主要只有 `logs` / `search_results` 值得壓縮。
- quality_score 是否維持在可接受範圍。
- F1 / F2 / F3 是否少數，還是已成常態。
- Headroom 是否真正提升主工作流，而非帶來更多人工補料。

## 11. Related Pilot Files

- `.agents/pilot/headroom-payload-rules.md`
  - Headroom compression scope and fallback rules.
- `.agents/pilot/codegraph-queries.md`
  - Retrieval query patterns and fallback guidance.
- `.agents/pilot/pilot.config.json`
  - Active pilot endpoint and dashboard wiring config.
