---
description: Cross-Review pipeline 行為規範 — FAIL 處理 / Checkpoint / 路由
---

> RFC 2119 Keywords: **MUST** = 違反即 FAIL; **SHOULD** = 強烈建議，可附理由跳過; **MAY** = 純可選。

# Cross-Review 行為規範

## FAIL 硬規範（不可違反）

**立即停 + Checkpoint 觸發條件**（任一觸發 → **MUST** 輸出 Checkpoint 格式，等待裁決）：

- 修正涉及：PROGRESS.md / sv_report*.md / ssot_context.md / CLAUDE.md / SSOT*.md / *_Contract*.md，且該檔未列於票面 `ALLOWED`，或該修改行為未被票面 Action Item／AC 明確授權（屬新 authority）→ MUST 經 Gate 本地取證＋Advisor 確認後 `HUMAN_REQUIRED(SCOPE)` 並輸出 Checkpoint；**`ALLOWED` 僅界定可觸碰檔案，不是行為授權**。該檔已列 `ALLOWED` **且**行為已被 Action Item／AC 明確授權（可指出對應 AI／AC 編號或具名裁決）者走 Gate 續作，不因檔名 Checkpoint（**例外**：符合 `ssot-governance.md` Transition Table「SV／Codex 判 REPORT_FIX」七條件、僅修改 `.tmp/sv_report*.md` 之 report-only 閉環不觸發本款）
- 修正涉及既定 feature flag 預設值或 DI 綁定（FeatureFlags／@Inject／@Module／@Provides 的綁定與 scope）之變更，且票面未明確授權：屬新 authority → MUST 立即 Checkpoint／升報（HUMAN_REQUIRED，類別依 Gate，多為 SCOPE）。關鍵詞（FeatureFlags / gate / Gate / cooldown / @Inject / @Module / @Provides）僅作取證提示：命中後須讀 hunk 判定，不獨立觸發 Checkpoint；票面已明確授權、屬已授權 scope 內之契約修復者走 Gate，不因此再找人工。

**次數限制**：第 2 次 FAIL（同票任何維度）屬步驟級事件，記 `step_limit` → 依 `ssot-governance.md` §Autonomous Engineering Gate 處置；第 1 次 FAIL 純 .kt fix **MAY** inline。report-only REPORT_FIX 閉環之重跑不計入，其上限由腳本於第 3 次輸出 `CHECKPOINT_REPORT_FIX_LIMIT`（exit 2）管制。

**Checkpoint 格式**（**MUST** 使用；僅於 `HUMAN_REQUIRED` 成立時輸出）：

```text
📍 CROSS_REVIEW CHECKPOINT | Ticket: [ID] | Round: R[N]
Codex: [判定原文] | 選項: A.[方案A] / B.[方案B] / D.[Codex誤判理由]
⏸️ 等待 Akira 裁決
```

**退出機制**：步驟級預算用盡後再重跑 `cross_review.sh` 不是已授權策略（report-only REPORT_FIX 閉環於腳本本次回 exit 3 之重跑除外；第 3 次 → `CHECKPOINT_REPORT_FIX_LIMIT` 為步驟級上限，依 `ssot-governance.md` §Autonomous Engineering Gate 處置）；Akira ack 僅作為 `EXHAUSTED` 後續跑之恢復門（`HUMAN_REQUIRED(COST)`），例行 ack 不構成「無已授權策略」之理由。

## Scope Ban BLOCKER 處置 + Payload 一致性

- **Scope Ban BLOCKER + Contract 明文授權** → **MUST** 直接走 Option D（CSO 誤判申訴），**MUST** 不走 Option A（sv_report 補文字）。Layer 3.4 機械閘門不讀推理文字，歷史兩次實證（P05-R1、P08-E1）。（K4-3）
- **Payload scope 一致性**：`cross_review.sh` payload **MUST** 驗證 SV scope ↔ diff scope 一致（防 scope 錯配漏審）。（K3-6）

## Cross-Review 路由

diff 跨三層 or 涉及 CameraViewModel.kt → `xhigh`。
純 domain 新建 → `high`（預設）。

## R0 預審路由

- **管線標準觸碰面登記表**（新管線步驟觸碰 repo-tracked 檔案時 MUST 同步維護）：

  | 管線步驟 | 標準觸碰面 |
  |---------|-----------|
  | /qa Step 4 | `.claude/PROGRESS.md` |
  | /qa Step 4.5 | `PROGRESS_ARCHIVE.md`（歸檔輪替） |
  | /handoff | `Handoff/` |

- **Pre-flight FORBIDDEN 比對維度**：票面 FORBIDDEN 清單 × 上表觸碰面——命中時票面 **MUST** 內建處置條款（`DEFERRED-TO-DOCSYNC` 或明示例外授權），留白即 Pre-flight FAIL（開票 lint，衝突消滅於起草時）。
- **R0 路由**：`cross_review.sh` 產生 payload 後、`codex exec` 之前，**MUST** 先委派 `cross-reviewer` subagent（附 payload 路徑 `.tmp/cross_review_payload.md`）；R0 回報 FAIL / CHECKPOINT-REQUIRED → **MUST NOT** 送 Codex；**主 agent** 為 R0 回件唯一承接者，於 R0 記錄落盤後 **MUST** 逐列依 R0 分類欄處置，**MUST NOT** 僅因 verdict 字樣即停車輸出 Checkpoint：
  - `ACTIONABLE`（ALLOWED 內、修法可本地判定）：記 `gate_ledger.py consult --kind r0`（`--note` 填 R0 記錄檔名）→ 以該修法 `round start --layer r0` → 修復 → 局部驗證＝重跑 R0 並確認該列已消除 → `round end`；重跑後同列仍在＝該輪 fail（逐層計輪，每層最多 3 次）。r0 層三次用盡，或本層已無可行修法可提早升級 → 轉 `UNDECIDABLE` 路徑（本地取證→真實 Advisor），不停等 Akira；升級時攜帶同 incident 已試策略、結果與未解原因，Advisor 層不原樣重播。
  - `UNDECIDABLE`（判準衝突、授權或語境無法判定）：先本地取證（`consult --kind local`）；仍不能判定 → 主 agent 依 Gate「Advisor 與 PPSS」條文取得 Advisor 並經入口記帳（R0 為唯讀 subagent，**不是** Advisor，不得代記 advisor consult）→ `round start --layer advisor`；Advisor 確認之事實與一手依據附入重跑 R0 之委派 prompt。
  - `AUTHORITY`（未獲票面授權之 feature flag 預設值／DI 綁定變更，或 file-class 命中而不具「ALLOWED 已列＋行為已被 AI／AC 明確授權」）：先本地取證，再依 Gate「Advisor 與 PPSS」條文取得 Advisor 確認（含查證票面是否實有授權依據）；確認仍屬授權缺口 → `human --category SCOPE`，輸出 `HUMAN_REQUIRED` 單行，**僅此時**使用下方 Checkpoint 格式；確認已有授權依據 → 轉 `ACTIONABLE`／`FORWARD` 路徑並於 R0 記錄註明依據，不重問已裁決政策。
  - 無分類欄之列（舊格式回件）一律視為 `UNDECIDABLE`；R0 重跑後仍判 FAIL 之列，主 agent **不得自判放行**（護欄二：僅 Akira 可 override R0 誤攔）；incident／round／consult／human 事件與前置條件以 `ssot-governance.md` §Autonomous Engineering Gate 為準，本條不複製。
- **護欄一（無放行權）**：R0 任何結果（含 FORWARD）**MUST NOT** 計為正式 cross-review PASS；正式審查主體唯 Codex（gstack 跨家族紀律）。
- **護欄二（否決申訴）**：Akira **MAY** override R0 誤攔直送 Codex；R0 誤攔事件 **MUST** 記錄（校準預過濾器）。
- **事件三分類**：R0 事件分三類——`NORMAL`（無 override）／`_OVERRIDE`（誤攔，Akira 行使護欄二否決）／**`TRUE-POSITIVE-RULE-GAP`**（攔截忠實於現行規格、但規格本身缺分支之事件）。`TRUE-POSITIVE-RULE-GAP` **MUST NOT** 計入 `_OVERRIDE`（不消耗 CR-03 誤攔預算）；**MUST** 以承接票號記錄規則校準債。
- **護欄三（Budget 隔離）**：R0 攔截 **MUST NOT** 計入 `fail_budget_state`（僅 Codex FAIL 計入）；budget reset 僅限 CSO 顯式授權且記錄理由 + 翻盤條件；受審方 **MUST NOT** 於同票內自行提案規則修訂後自引為 reset 依據。
- **Checkpoint 格式增列機械狀態行**（由 `cross_review.sh` 自動填入）：`📊 Budget: R[N]/[MAX] | state: .tmp/cross_review_state/[TICKET] | mtime: [ts]`
- **R0 記錄落盤**：主 session 收到 R0 報告後、決定送 Codex 或進修正之**前**，**MUST** 將 R0 完整報告寫入 `.tmp/fail-advisor/R0_[TICKET]_[YYYYMMDD_HHMMSS].md`；目錄不存在則建立。
- **記錄格式**：固定七欄位——Ticket / Payload 路徑 + `git rev-parse` diff 端點 / R0 裁決（FORWARD / FAIL / CHECKPOINT-REQUIRED）/ 分級表全文 / 後續實際走向（送 Codex + Codex 裁決 / 修正 / Checkpoint）/ Override 標記（Akira 行使護欄二 override 時檔名加 `_OVERRIDE` 後綴）/ `CLASSIFICATION: NORMAL | MISSED | OVERRIDE | TRUE-POSITIVE-RULE-GAP`（`TRUE-POSITIVE-RULE-GAP` 記錄 **MUST** 附承接票號欄）。
- **Housekeeping**：每次寫入後保留最近 `RETAIN_N=20` 筆（mtime 排序 FIFO 刪除超額）；`_OVERRIDE` 記錄遭 FIFO 淘汰前 **MUST** 提示主對話。
- **`.tmp/` 非版控紀律**：記錄升格為治理證據（如 KAIROS 事件支撐）時，**MUST** 於歸檔時複製至對應決策文件，不得僅以 `.tmp/` 路徑引用。
