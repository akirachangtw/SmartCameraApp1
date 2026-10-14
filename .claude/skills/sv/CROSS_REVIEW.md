# Cross-Review 指南（Layer 3 指令）

**版本**: v1.6
**所屬 Skill**: /sv（本地 Reviewer）
**觸發時機**: /sv 階段 A-C 全部通過並產出 `.tmp/sv_report.md` 後，自動進入階段 D

---

## 概念

Cross-Review 是 gstack Step 4.5 防線。它使用 Codex CLI 對 Claude Code 的 SV Report 與 git diff 進行獨立跨模型二審，一次呼叫完成三層驗證：

- **Layer 1 — Architectural Review（架構重審）**：SSOT drift、Scope Ban、Clean Architecture 依賴方向、硬體約束
- **Layer 2 — SV Report Cross-Review（報告驗證）**：SV Report 決策相關主張 vs 原始證據比對（檔案清單由 `structural_verify.sh` Check A/B/C 確定性核對）、Orphan Code、Anti-Mock
- **Layer 3 — Blind Scan（盲區掃描）**：物理邊界、全鏈路退場、狀態機時序、治理證據鏈、**Guard Input Reachability（固定項）**

BLOCKER 判準之唯一來源為 `cross_review.sh` PROMPT_HEADER 的「BLOCKER 判準」表；各 Layer 僅作歸類提示。

`REPORT_FIX` 是本專案的正式 verdict 名稱。原始 fail-budget plugin 的 `REPORTFIX` 只作為相容輸入；任何 state、報告、文件輸出都必須使用 `REPORT_FIX`。

## 執行方式

### 自動觸發（SKILL.md 階段 D）

/sv 通過後，Claude Code 自動檢查環境並執行：

```bash
bash .claude/skills/sv/scripts/cross_review.sh .tmp/sv_report.md [diff_scope] [extra_exclude] [ssot_context]
```

參數說明：

| 參數 | 預設值 | 說明 |
|------|--------|------|
| `.tmp/sv_report.md` | 必填 | /sv 產出的 SV Report 檔案路徑 |
| `diff_scope` | `tmp/verify_cmd.conf` 的 `DIFF_REF..DIFF_END`，無 conf 時才 fallback `HEAD~1..HEAD` | Git diff 範圍，可指定如 `HEAD~3..HEAD` |
| `extra_exclude` | 空 | 逗號分隔 pathspec；亦會合併 `.tmp/sv_report.md ## Excluded Payload` |
| `ssot_context` | `.claude/skills/sv/ssot_context.md` | SSOT 精簡上下文路徑 |

### 手動執行

```bash
# 基本用法
bash .claude/skills/sv/scripts/cross_review.sh .tmp/sv_report.md

# 指定 diff 範圍
bash .claude/skills/sv/scripts/cross_review.sh .tmp/sv_report.md HEAD~3..HEAD

# 指定 diff 範圍 + 授權排除
bash .claude/skills/sv/scripts/cross_review.sh .tmp/sv_report.md "a9558fc..c65482f" "*/CameraScreen.kt"

# 完整四參數
bash .claude/skills/sv/scripts/cross_review.sh .tmp/sv_report.md "a9558fc..c65482f" "*/CameraScreen.kt" .claude/skills/sv/ssot_context.md
```

## Verdict 與退出碼

第一行 verdict 必須是以下四種之一：

| Verdict | Exit | 意義 | 下一步 |
|---------|------|------|--------|
| `PASS` | 0 | Cross-Review 通過 | 進入 Traycer 終審 |
| `ADVISORY` | 0 | 有非阻斷觀察（含輕微措辭、無品質影響連結、含糊完備性；每項在回件列一行） | 記錄後可進入 Traycer 終審 |
| `BLOCKER` | 1 | BLOCKER 判準表所列之真實契約、API、接線或行為違反（單一來源：`cross_review.sh`） | 修程式碼後重新 /qa → /sv |
| `REPORT_FIX` | 3 | `.tmp/sv_report.md` 的決策相關主張與原始證據矛盾（輕微措辭、無品質影響連結、含糊完備性屬 ADVISORY；檔案清單由 Check A/B/C 確定性核對） | 符合 report-only 閉環條件者依 `sv/SKILL.md` 閉環（修報告→preflight→重跑）；否則修報告後停止回報，不需重跑 /qa |
| ENV / FAIL BUDGET / REPORT_FIX 閉環上限 | 2 | codex、Git、輸入、fail budget 錯誤；或 `CHECKPOINT_REPORT_FIX_LIMIT`／`REPORT_FIX_ESCALATE` | 人工介入（LIMIT 為人工裁決 checkpoint，非 BLOCKER） |

`REPORT_FIX` 不得放行到 Traycer；它的目的就是避免用昂貴終審配額審一份不可信報告。

## Fail Budget

Cross-Review state 存在：

```text
.tmp/cross_review_state/<TICKET_ID>
```

內容包含 `TICKET`、`ROUND`（BLOCKER 輪次）、`REPORT_FIX_COUNT`、`LAST`。`MAX_BLOCKER_BUDGET` 從 `.claude/verify-policy.env` 讀取，預設為 `3`。

- `BLOCKER` 到達 `MAX_BLOCKER_BUDGET - 1` 輪時輸出 checkpoint
- `BLOCKER` 到達 `MAX_BLOCKER_BUDGET` 輪時 exit 2，要求人工裁決
- `REPORT_FIX` 不遞增 `ROUND`（不扣 blocker budget），但遞增 `REPORT_FIX_COUNT`（上限 2）；同票同一 state 週期（PASS／ADVISORY 清 state 前）第 3 次 REPORT_FIX 輸出 `CHECKPOINT_REPORT_FIX_LIMIT`（exit 2），停止自動閉環

## 降級策略

| 狀況 | 行為 |
|------|------|
| `codex` CLI 不可用 | SV Report 標記 ⚠️ SKIPPED；手動 cross-review 或送 Traycer 前需明確承擔風險 |
| `ssot_context.md` 不存在 | 降級為純 SV Report 驗證（Layer 2 only），無架構重審 |
| Codex 輸出格式異常 | 視為 `BLOCKER`，保留完整輸出供人工審查 |
| ChatGPT Plus credit 當日耗盡 | 可改用 Gemini CLI headless fallback，但只作工具級輔助，不取代正式 Cross-Review |

## Gemini CLI Fallback

若 Codex 不可用，可用 Gemini CLI 協助閱讀 payload：

```bash
cat .tmp/cross_review_payload.md | gemini -p - --output-format json
```

此 fallback 僅能輔助 Layer 2 報告驗證；缺少正式 L1/L3 審查保證，結果必須標記降級。

## 結果日誌

每次 cross-review 的完整結果保存在：

```text
.tmp/cross_review_result_YYYYMMDD_HHMMSS.md
```

此日誌可作為 KAIROS 回顧輸入。

## 與 Traycer 的分工

Cross-Review 不取代 Traycer 終審。兩者審查面向不同：

- **Codex Cross-Review**：文本級 + 結構級（SSOT drift、scope、依賴方向、orphan、anti-mock）
- **Traycer strict_verification**：行為級 + 系統級（thread safety、lifecycle、效能回歸、商業邏輯）

Cross-Review `PASS` / `ADVISORY` 後仍必須送 Traycer 終審。

## Guard Input Reachability（INFRA-REACH-01 固定項）

此為 Cross-Review 的強制固定提問，不依賴 diff 是否含新 Guard——每次都必須輸出明確回答。

**檢查目標**：本 Ticket diff 是否新增 guard / gate / early-return / reducer branch / fallback，且依賴 diff 外部 CameraState 欄位或 runtime state？

**Producer 判定三選一**：

| 判定 | 說明 |
|------|------|
| (A) diff 內可見 | producer 存在於本次 diff 範圍內 |
| (B) diff 外但 production 可達 | 任何 repo／runtime 路徑上有 non-idle / non-reset 賦值點（附 `檔案:行號`） |
| (C) missing-wiring | producer 不存在或不可證明 → 標記風險，對應 AC 不得標 PASS |

檢查範圍只限判定實際讀取且影響結果的輸入；每個輸入舉證一個 writer 與判定處 consumer，不列全 schema、不向上游無限追溯（一跳為止）。

**強制輸出格式**（每次 Cross-Review 必須輸出此行）：

```
Guard Input Reachability: [無新 Guard] / [有新 Guard — State: X — Producer: (A)/(B)/(C)] / [missing-wiring: State X 無 producer]
```

此規則由 `cross_review.sh §3.5` 實作（prompt wiring），由 `sv/SKILL.md §B-7` 補充語意審查層，由 `static_blind_scan.sh §3.5.5` 提供 Dead-State 偵測工具。

## REACHABILITY 機械 Token（INFRA-REACH-02 AI-4）

Guard Input Reachability 的散文回答（上一節）僅供人類/Codex 語意判讀，不可機械解析。本節定義供 `structural_verify.sh` 消費的機械 token，兩者為同一次審查必須**同時**輸出的獨立行，不可互相替代。

**格式**（Codex 每次 Cross-Review 必須輸出，見 `cross_review.sh §3.5` 機械 token 段落）：

```
REACHABILITY: NONE
```

或（至少一個新 Guard 判定為 (C) missing-wiring 時）：

```
REACHABILITY: <guard1>:<producer位置>,<guard2>:MISSING
```

| 欄位 | 說明 |
|------|------|
| `NONE` | 無新 Guard，或所有 producer 判定為 (A)/(B) |
| `<guard>:<producer位置>` | producer 判定 (A)/(B)，位置格式 `檔案:行號` |
| `<guard>:MISSING` | producer 判定 (C)，對應 AC 不得標 PASS |

**消費鏈**：`cross_review.sh` 從 Codex 輸出解析 `REACHABILITY:` 行 → 寫入 `.tmp/cross_review_pass/<TICKET_ID>` receipt 的 `REACHABILITY=` 欄位 → `structural_verify.sh` Check G（`--post` mode，獨立於 Check E receipt 語義之外）驗證該欄位存在且非 `UNSET`，缺失或未設定 → **BLOCKER**（Codex 未實際輸出機械 token，即使散文回答存在也視為未落地）。

## 維護

- `ssot_context.md`：每 Sprint 更新一次，或 SSOT 版本變更時同步
- `cross_review.sh`：prompt 結構、verdict 或 fail budget 行為變更時同步更新
- `structural_verify.sh`：receipt 欄位或 Check E / Check G 邏輯變更時同步更新
- 本文件：工作流變更時同步更新
