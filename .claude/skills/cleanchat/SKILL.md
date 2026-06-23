---
name: cleanchat
description: 清理 tmp/*.md 與 plans/cso_briefing/**/*.md 中的 AI 產生標註標籤（[file:N], [^N], [cite:N] 等）、統計列與末尾空白。使用 /cleanchat 觸發。
---

# /cleanchat — Markdown 清理工具

清理以下目標路徑的 Markdown 檔案：
- `tmp/*.md`
- `plans/cso_briefing/**/*.md`

Driver: `.claude/skills/cleanchat/scripts/md-formatting.sh`

## 執行（Agent 路徑）

```bash
bash .claude/skills/cleanchat/scripts/md-formatting.sh
```

可選傳入 REPO_ROOT（預設自動從 `git rev-parse --show-toplevel` 取得）：

```bash
bash .claude/skills/cleanchat/scripts/md-formatting.sh /path/to/repo
```

輸出範例：

```
Scanned : 78 file(s)
Cleaned : 31 file(s)
  - tmp/PHASE-B-01-R2.md
  - plans/cso_briefing/archive/CSO_Handoff_20260511_W1.5R_Authorization.md
  ...
```

## 清理規則

| 規則 | 說明 |
|------|------|
| AI 腳注定義 | 刪除 `[^1]: ...` 整行 |
| 行內標籤 | 刪除 `[file:1]`, `[^1]`, `[cite:1]`, `[web:1]` 等（數字型） |
| 統計列 | 刪除 `Turns: ... \| Code: ...` 整行 |
| Suggestion 列 | 刪除 `Suggestion: ...` 整行 |
| 末尾空白 | 移除尾端多餘空行，保留單一 `\n` |

注意：純文字說明性的 `[file:N]`（N 為英文字母）**不會**被刪除。

## 排除

`Clean_Chat_Canvas_Skill.md` 自動略過。

## Gotchas

- PowerShell 的 `Write-Output` 在 Git Bash 環境下對 emoji 字元解析有問題，driver 改用 bash 實作。
- `find -print0` + `read -d ''` 處理含空格的路徑名。
- 重複執行冪等：已乾淨的檔案不會被重寫（內容比對後略過）。
