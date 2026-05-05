---
name: compact
description: Compress the current conversation into a concise dashboard-first handoff for a fresh Codex/GPT session. Use when the user invokes /compact, asks to reduce context, prepare a restart package, save a session handoff, or when token usage is high enough that continuing risks context loss.
---

# Compact

## Workflow

Create a restart handoff that a new Codex/GPT session can read quickly before continuing work.

1. Get the current timestamp with PowerShell:

```powershell
Get-Date -Format 'yyyyMMdd_HHmm'
```

2. Create `docs/session_handoff/SESSION_HANDOFF_<timestamp>.md`.
3. Make the top section dashboard-first: include only the information required to resume.
4. Fold detailed context into `<details>` sections.
5. If this repository has `PROGRESS.md`, update only the Dashboard-style status fields that are directly supported by the current work. Do not invent progress.

## Handoff Template

```markdown
# Cueshot Handoff - [YYYY-MM-DD HH:mm]

## Dashboard（新 session 先讀這段）
- **完成**: [1-2 句話概括本次成果]
- **阻塞**: [當前 blocker，若無寫「無」]
- **下一步**: [精確到檔案 + 函數名；若未知，寫需先讀取的文件]
- **帶入**: AGENTS.md · PROGRESS.md · [本次工作檔案]

<details>
<summary>變更明細（按需展開）</summary>

| 檔案 | 函數/區塊 | 變更內容 |
|------|----------|---------|
| [path] | [function/block] | [what changed] |

</details>

<details>
<summary>關鍵決策（按需展開）</summary>

| 決策 | 理由 | 影響範圍 |
|------|------|---------|
| [decision] | [why] | [files/modules] |

</details>

<details>
<summary>Context 指標針（按需展開）</summary>

- 全局變更: `git diff --stat`
- 核心檔案: `git diff -p [path]`
- 近期提交: `git log --oneline -10`

</details>
```

## Rules

- Preserve SSOT names, ticket IDs, file paths, and unresolved blockers exactly.
- Separate facts from assumptions.
- Do not include long logs or broad code excerpts; point to files and commands instead.
- Do not claim tests passed unless they were actually run in the current session.
- After writing the file, reply with the handoff path and the top Dashboard section.
