# Cross-Review 指南（Layer 3 指令）

**版本**: v1.6
**所屬 Skill**: /sv（本地 Reviewer）
**觸發時機**: /sv 階段 A-C 全部通過並產出 `sv_report.md` 後，自動進入階段 D

---

## 概念

Cross-Review 是 gstack Step 4.5 防線。它使用 Codex CLI 對 Claude Code 的 SV Report 與 git diff 進行獨立跨模型二審，一次呼叫完成三層驗證：

- **Layer 1 — Architectural Review（架構重審）**：SSOT drift、Scope Ban、Clean Architecture 依賴方向、硬體約束
- **Layer 2 — SV Report Cross-Review（報告驗證）**：SV Report 宣稱 vs diff 事實比對、Orphan Code、Anti-Mock
- **Layer 3 — Blind Scan（盲區掃描）**：物理邊界、全鏈路退場、狀態機時序、治理證據鏈

`REPORT_FIX` 是本專案的正式 verdict 名稱。原始 fail-budget plugin 的 `REPORTFIX` 只作為相容輸入；任何 state、報告、文件輸出都必須使用 `REPORT_FIX`。

## 執行方式

### 自動觸發（SKILL.md 階段 D）

/sv 通過後，Claude Code 自動檢查環境並執行：

```bash
bash .claude/skills/sv/scripts/cross_review.sh ./sv_report.md [diff_scope] [extra_exclude] [ssot_context]
```

參數說明：

| 參數 | 預設值 | 說明 |
|------|--------|------|
| `sv_report.md` | 必填 | /sv 產出的 SV Report 檔案路徑 |
| `diff_scope` | `tmp/verify_cmd.conf` 的 `DIFF_REF..DIFF_END`，無 conf 時才 fallback `HEAD~1..HEAD` | Git diff 範圍，可指定如 `HEAD~3..HEAD` |
| `extra_exclude` | 空 | 逗號分隔 pathspec；亦會合併 `sv_report.md ## Excluded Payload` |
| `ssot_context` | `.claude/skills/sv/ssot_context.md` | SSOT 精簡上下文路徑 |

### 手動執行

```bash
# 基本用法
bash .claude/skills/sv/scripts/cross_review.sh ./sv_report.md

# 指定 diff 範圍
bash .claude/skills/sv/scripts/cross_review.sh ./sv_report.md HEAD~3..HEAD

# 指定 diff 範圍 + 授權排除
bash .claude/skills/sv/scripts/cross_review.sh ./sv_report.md "a9558fc..c65482f" "*/CameraScreen.kt"

# 完整四參數
bash .claude/skills/sv/scripts/cross_review.sh ./sv_report.md "a9558fc..c65482f" "*/CameraScreen.kt" .claude/skills/sv/ssot_context.md
```

## Verdict 與退出碼

第一行 verdict 必須是以下四種之一：

| Verdict | Exit | 意義 | 下一步 |
|---------|------|------|--------|
| `PASS` | 0 | Cross-Review 通過 | 進入 Traycer 終審 |
| `ADVISORY` | 0 | 有非阻斷建議 | 記錄後可進入 Traycer 終審 |
| `BLOCKER` | 1 | 真實功能、架構、scope 或 anti-mock 缺陷 | 修程式碼後重新 /qa → /sv |
| `REPORT_FIX` | 3 | `sv_report.md` 或 payload disclosure 不可信 | 修報告後重跑 cross-review，不需重跑 /qa |
| ENV / FAIL BUDGET | 2 | codex、Git、輸入、或 fail budget 錯誤 | 人工介入 |

`REPORT_FIX` 不得放行到 Traycer；它的目的就是避免用昂貴終審配額審一份不可信報告。

## Fail Budget

Cross-Review state 存在：

```text
.tmp/cross_review_state/<TICKET_ID>
```

內容包含 `TICKET`、`ROUND`、`LAST`。`MAX_BLOCKER_BUDGET` 從 `.claude/verify-policy.env` 讀取，預設為 `3`。

- `BLOCKER` 到達 `MAX_BLOCKER_BUDGET - 1` 輪時輸出 checkpoint
- `BLOCKER` 到達 `MAX_BLOCKER_BUDGET` 輪時 exit 2，要求人工裁決
- `REPORT_FIX` 不扣 blocker budget，但會阻擋流程直到報告修正

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

## 維護

- `ssot_context.md`：每 Sprint 更新一次，或 SSOT 版本變更時同步
- `cross_review.sh`：prompt 結構、verdict 或 fail budget 行為變更時同步更新
- 本文件：工作流變更時同步更新
