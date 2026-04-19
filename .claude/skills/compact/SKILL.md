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

## 下一步待辦 (Next Action)
1. [明確的目標檔案與函數名稱]
2. [預期的邏輯變更]

## 🎯 Context 指標針 (Context Pointers)
（新 Session Agent 請主動執行以下指令以獲取最新狀態，**嚴禁依賴歷史記憶**）

1. **全局變更概覽**：
   ```bash
   git diff HEAD~[N] --stat
   ```
   （註：[N] 替換為本次對話產生的 commit 數量）

2. **核心檔案最新狀態檢索**：
   請優先使用 Read 工具讀取上述「下一步待辦」中指定的檔案。若需查看具體修改脈絡，請執行：
   ```bash
   git diff HEAD~[N] -p [具體檔案路徑]
   ```

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
- 「下一步待辦 (Next Action)」必須具體到檔案名稱與函數，禁止寫「繼續開發」這種空話。
- 「Context 指標針」的 `[N]` 必須填入本次對話實際產生的 commit 數量（查 `git log --oneline -10` 確認）。

產出前，**必須先執行以下指令取得實際系統時間**：
```bash
powershell -command "Get-Date -Format 'yyyyMMdd_HHmm'"
```
使用該指令回傳的值作為檔名時間戳，儲存至：
```
docs/session_handoff/SESSION_HANDOFF_<實際時間戳>.md
```
例如指令回傳 `20260409_1425`，則檔名為 `SESSION_HANDOFF_20260409_1425.md`。

⚠️ 禁止估算或硬編碼時間——時間戳必須來自系統指令。

提醒：「交接文件已儲存至 docs/session_handoff/。建議：1) 更新 PROGRESS.md 2) 開新對話 3) 貼入此交接文件」
