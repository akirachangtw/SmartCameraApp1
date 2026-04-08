# Description
壓縮當前對話為精簡交接摘要，準備開新對話。Context Firewall。

# Prompt
你現在執行「Context 壓縮」。請掃描本次對話的所有內容，產出以下格式的交接文件：

```markdown
# Cueshot 工作交接 — [日期]

## 本次完成的變更
| 檔案 | 函數/區塊 | 變更內容 |
|------|----------|---------|
| [path] | [function] | [what changed] |

## 當前阻塞點
- [ ] [blocking issue 1]
- [ ] [blocking issue 2]
（若無阻塞，寫「無」）

## 下一步待辦
1. [next action — 精確到檔案和函數]
2. [next action]

## 關鍵決策 (本次對話中做出的)
| 決策 | 理由 | 影響範圍 |
|------|------|---------|
| [decision] | [why] | [files affected] |
（若無新決策，寫「無」）

## 需帶入新對話的 Context
[列出新對話需要讀取的最小檔案集合，例如：]
- CLAUDE.md
- PROGRESS.md
- [specific file being worked on]

## 不需帶入的 Context
[明確列出本次對話中已解決、不需帶入的議題]
```

**重要規則**：
- 交接文件必須**自足 (self-contained)**——新對話只讀這份文件 + CLAUDE.md + PROGRESS.md 就能繼續工作。
- 不要複製代碼，只引用檔案路徑和函數名。
- 「下一步待辦」必須具體到可以直接執行的粒度，禁止寫「繼續開發」這種空話。

產出後，將此交接文件儲存至：
```
docs/session_handoff/SESSION_HANDOFF_YYYYMMDD_HHMM.md
```
（自動帶入當下日期時間，例如：`SESSION_HANDOFF_20260408_1430.md`）

提醒：「交接文件已儲存至 docs/session_handoff/。建議：1) 更新 PROGRESS.md 2) 開新對話 3) 貼入此交接文件」
