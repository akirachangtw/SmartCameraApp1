---
description: Cross-Review pipeline 行為規範 — FAIL 處理 / Checkpoint / 路由
globs:
  - "app/src/**/*.kt"
  - ".claude/skills/**"
  - ".agents/skills/**"
  - "tmp/**"
---

> RFC 2119 Keywords: **MUST** = 違反即 FAIL; **SHOULD** = 強烈建議，可附理由跳過; **MAY** = 純可選。

# Cross-Review 行為規範

## FAIL 硬規範（不可違反）

**立即停 + Checkpoint 觸發條件**（任一觸發 → **MUST** 輸出 Checkpoint 格式，等待裁決）：

- 修正涉及：PROGRESS.md / sv_report*.md / ssot_context.md / CLAUDE.md / SSOT*.md / *_Contract*.md
- 修正含 token：FeatureFlags / gate / Gate / cooldown / @Inject / @Module / @Provides
- 第 2 次 FAIL（同票任何維度）

**次數限制**：第 1 次 FAIL 純 .kt fix **MAY** inline，完成後 **MUST** 立即回報等 ack。第 2 次 → **MUST** 停。

**Checkpoint 格式**（**MUST** 使用）：

```text
📍 CROSS_REVIEW CHECKPOINT | Ticket: [ID] | Round: R[N]
Codex: [判定原文] | 選項: A.[方案A] / B.[方案B] / D.[Codex誤判理由]
⏸️ 等待 Akira 裁決
```

**退出機制**：無 Akira ack **MUST** 不得連續執行 2 次以上 cross_review.sh。

## Scope Ban BLOCKER 處置 + Payload 一致性

- **Scope Ban BLOCKER + Contract 明文授權** → **MUST** 直接走 Option D（CSO 誤判申訴），**MUST** 不走 Option A（sv_report 補文字）。Layer 3.4 機械閘門不讀推理文字，歷史兩次實證（P05-R1、P08-E1）。（K4-3）
- **Payload scope 一致性**：`cross_review.sh` payload **MUST** 驗證 SV scope ↔ diff scope 一致（防 scope 錯配漏審）。（K3-6）

## Cross-Review 路由

diff 跨三層 or 涉及 CameraViewModel.kt → `xhigh`。
純 domain 新建 → `high`（預設）。

## R0 預審路由

- **R0 路由**：`cross_review.sh` 產生 payload 後、`codex exec` 之前，**MUST** 先委派 `cross-reviewer` subagent（附 payload 路徑 `.tmp/cross_review_payload.md`）；R0 回報 FAIL / CHECKPOINT-REQUIRED → **MUST NOT** 送 Codex，直接進修正或 Checkpoint 裁決流程。
- **護欄一（無放行權）**：R0 任何結果（含 FORWARD）**MUST NOT** 計為正式 cross-review PASS；正式審查主體唯 Codex（gstack 跨家族紀律）。
- **護欄二（否決申訴）**：Akira **MAY** override R0 誤攔直送 Codex；R0 誤攔事件 **MUST** 記錄（校準預過濾器）。
- **護欄三（Budget 隔離）**：R0 攔截 **MUST NOT** 計入 `fail_budget_state`（僅 Codex FAIL 計入）；budget reset 僅限 CSO 顯式授權且記錄理由 + 翻盤條件；受審方 **MUST NOT** 於同票內自行提案規則修訂後自引為 reset 依據。
- **Checkpoint 格式增列機械狀態行**（由 `cross_review.sh` 自動填入）：`📊 Budget: R[N]/[MAX] | state: .tmp/cross_review_state/[TICKET] | mtime: [ts]`
