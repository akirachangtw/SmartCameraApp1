---
name: compact
description: 壓縮當前對話為精簡交接摘要。Dashboard-first，細節折疊。對話過長時使用。
disable-model-invocation: false
---

> RFC 2119 Keywords: **MUST** = 違反即 FAIL; **SHOULD** = 強烈建議，可附理由跳過; **MAY** = 純可選。

# Prompt
執行 Context 壓縮。先取得系統時間：
`powershell -command "Get-Date -Format 'yyyyMMdd_HHmm'"`

**SHOULD** 產出以下格式，儲存至 `docs/session_handoff/SESSION_HANDOFF_<時間戳>.md`：

---

## 輸出格式

```markdown
# Cueshot Handoff — [日期]

## Dashboard（新 session 先讀這段）
- **完成**: [1-2 句話概括本次成果]
- **阻塞**: [當前 blocker，若無寫「無」]
- **下一步**: [精確到檔案 + 函數名]
- **帶入**: CLAUDE.md · PROGRESS.md · [本次工作檔案]

<details>
<summary>📋 變更明細（按需展開）</summary>

| 檔案 | 函數/區塊 | 變更內容 |
|------|----------|---------|
| [path] | [function] | [what] |

</details>

<details>
<summary>🎯 關鍵決策（按需展開）</summary>

| 決策 | 理由 | 影響範圍 |
|------|------|---------|
| [decision] | [why] | [files] |

</details>

<details>
<summary>🔍 Context 指標針（按需展開）</summary>

全局變更：`git diff HEAD~[N] --stat`
核心檔案：`git diff HEAD~[N] -p [檔案]`
（[N] = 本次 commit 數，用 `git log --oneline -10` 確認）

</details>
```

---

同步更新 PROGRESS.md Dashboard 段落（Sprint Health + Active Blockers + Next Actions）。

**SHOULD** 完成後輸出以下確認訊息（必須包含實際檔案路徑）：
「交接文件已儲存至 docs/session_handoff/SESSION_HANDOFF_<實際時間戳>.md，PROGRESS.md 已同步更新。建議：開新對話 → 貼入 Dashboard 段 → 按需展開細節。」
