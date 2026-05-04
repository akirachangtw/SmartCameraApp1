# Cross-Review 指南（Layer 3 指令）

**版本**: v1.5
**所屬 Skill**: /sv（本地 Reviewer）
**觸發時機**: /sv 階段 A-C 全部通過後，自動進入階段 D

---

## 概念

Cross-Review 是 gstack v1.4 新增的 Step 4.5 防線。它使用 Codex CLI（GPT-5.5）對 Claude Code 的 SV Report 進行獨立的跨模型二審，一次呼叫完成兩層驗證：

- **Layer 1 — Architectural Review（架構重審）**：SSOT drift、Scope Ban、Clean Architecture 依賴方向、硬體約束
- **Layer 2 — SV Report Cross-Review（報告驗證）**：SV Report 宣稱 vs diff 事實比對、Orphan Code、Anti-Mock

## 執行方式

### 自動觸發（SKILL.md 階段 D）

/sv 通過後，Claude Code 自動檢查環境並執行：

```bash
bash .claude/skills/sv/scripts/cross_review.sh ./sv_report.md [diff_scope] [ssot_context_path]
```

參數說明：

| 參數 | 預設值 | 說明 |
|------|--------|------|
| `sv_report.md` | （必填） | /sv 產出的 SV Report 檔案路徑 |
| `diff_scope` | `HEAD~1..HEAD` | Git diff 範圍，可指定如 `HEAD~3..HEAD` |
| `ssot_context_path` | `.claude/skills/sv/ssot_context.md` | SSOT 精簡上下文路徑 |

### 手動執行

若 Claude Code 環境無法呼叫 codex，開發者可在終端機手動執行：

```bash
# 基本用法
bash .claude/skills/sv/scripts/cross_review.sh ./sv_report.md

# 指定 diff 範圍
bash .claude/skills/sv/scripts/cross_review.sh ./sv_report.md HEAD~3..HEAD

# 完整三參數
bash .claude/skills/sv/scripts/cross_review.sh ./sv_report.md HEAD~1..HEAD .claude/skills/sv/ssot_context.md
```

## 退出碼

| 碼 | 意義 | 下一步 |
|----|------|--------|
| `0` | PASS — Cross-review 通過 | 進入 Traycer 終審 |
| `1` | FAIL — Cross-review 發現問題 | 回 Step 2 修正，附 Codex 問題清單 |
| `2` | ENV ERROR — 環境或輸入錯誤 | 檢查 codex 安裝、Git repo、SV Report 路徑 |

## 降級策略

| 狀況 | 行為 |
|------|------|
| `codex` CLI 不可用 | SV Report 標記 ⚠️ SKIPPED，可直接送 Traycer 或改用 Gemini CLI fallback |
| `ssot_context.md` 不存在 | 降級為純 SV Report 驗證（Layer 2 only），無架構重審 |
| Codex 輸出格式異常（首行非 PASS/FAIL） | 視為 FAIL，保留完整輸出供人工審查 |
| ChatGPT Plus credit 當日耗盡 | 改用 Gemini CLI headless fallback |

## Gemini CLI Fallback

若 Codex 不可用，可使用 Gemini CLI 替代：

```bash
# 組裝 payload 後 pipe 給 Gemini
cat .tmp/cross_review_payload.md | gemini -p - --output-format json
```

此 fallback 僅做 Layer 2（SV Report 驗證），不做 Layer 1（架構重審），因為 Gemini 沒有移轉的 Traycer Agent Skills。

## 結果日誌

每次 cross-review 的完整結果保存在：

```
.tmp/cross_review_result_YYYYMMDD_HHMMSS.md
```

此日誌可作為 KAIROS 回顧的輸入材料。

## 與 Traycer 的分工

Cross-Review 不取代 Traycer 終審。兩者的審查面向不同：

- **Codex Cross-Review**：文本級 + 結構級（SSOT drift, scope, 依賴方向, orphan, anti-mock）
- **Traycer strict_verification**：行為級 + 系統級（thread safety, lifecycle, 效能回歸, 商業邏輯）

Cross-Review PASS 後仍必須送 Traycer 終審。

## 維護

- `ssot_context.md`：每 Sprint 更新一次，或 SSOT 版本變更時同步
- `cross_review.sh`：prompt 結構變更時更新
- 本文件（CROSS_REVIEW.md）：工作流變更時更新
