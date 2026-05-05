# Compact Workflow

When the user invokes `/compact`, summarize the current conversation to create a compact handoff document, saving tokens and allowing a fresh start.

## Step 1: Get Current Time
// turbo
Run the following command to get the current system time to generate a filename.
```powershell
Get-Date -Format 'yyyyMMdd_HHmm'
```

## Step 2: Generate Content and Save Handoff Document
Create a new file at `docs/session_handoff/SESSION_HANDOFF_<Timestamp>.md` with the following template. Fill in the placeholders based on the work done in the current session.

```markdown
# Cueshot Handoff — [Date]

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
```

## Step 3: Gather Context Metrics
If any commits were made during this session, use git to gather context metrics. Let `[N]` be the number of commits made during this session.
// turbo
```powershell
git log --oneline -10
```
Then (if applicable):
// turbo
```powershell
git diff HEAD~[N] --stat
git diff HEAD~[N] -p [Core Files Modified]
```
Append these outputs inside a `<details><summary>🔍 Context 指標針（按需展開）</summary>` section at the end of the handoff document.

## Step 4: Update PROGRESS.md
Update the `PROGRESS.md` Dashboard section (Sprint Health, Active Blockers, and Next Actions) to reflect the new state.

## Step 5: Notify User
Conclude your response with the following exact message:
**交接文件已儲存。建議：開新對話 → 貼入 Dashboard 段 → 按需展開細節。**
