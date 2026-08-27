---
name: wktree-compact
description: 產出 worktree 交接檔（每次新建時戳快照，不覆寫）。當使用者說「產出交接檔」「compact 這個 worktree」「寫 handoff」或工作階段將結束需要留交接紀錄時觸發。
---

# wktree-compact

將當前 worktree session 收斂為 Compact 交接檔，寫入 `.claude/wktrdata/<topic>/handoffs/<YYYY-MM-DD-HHMM>.md`（非版控）。

## 執行方式

1. topic 取值：`git branch --show-current` 去 `worktree-` 前綴，**MUST** 直取，**MUST NOT** 手動輸入。
2. 每次 compact **MUST 新建**一份時戳檔，**MUST NOT** overwrite 既往 session 快照——Session 層之價值即在保留歷史。
3. **MUST** 固定六節，總量預算 ≤2000 字，各標題恰出現 1 次：

```markdown
## Scope
（目標／非目標）

## State
- branch: <live `git branch --show-current`>
- head: <live `git rev-parse --short HEAD`>
- 未提交變更: <live `git status --porcelain`，有／無>
- Session ID: <本次 session 識別值>

## Changes
（改動檔案＋關鍵決策，逐檔一行）

## Risks
（已知風險＋未驗證項；RAPID_MODE 執行時，狀態一律標 `*_unverified`）

## Resume
（下一 session 第一步）

## Do Not Assume
（下一 session 禁自行假定之事項逐條列舉，例如「禁假定測試已跑過」「禁假定該檔已完成重構」）
```

4. `State` 節 **MUST** 以 live git 指令直取，**MUST NOT** 憑記憶／前次輸出填寫。
5. 寫入路徑：`.claude/wktrdata/<topic>/handoffs/<YYYY-MM-DD-HHMM>.md`，時戳 **MUST** 以 `date -u +"%Y-%m-%d-%H%M"` live 直取（禁憑記憶填寫）；目錄不存在則建立。
6. 同批更新 Topic 層 `.claude/wktrdata/<topic>/PROGRESS.md`（目標／已完成／下一步／未決歧義／指向本次 handoff 檔名）。**MUST NOT** 於 Topic 層 PROGRESS 記載 Status／Head／Path 等機器狀態欄——該三者唯一 SSOT 為 `.claude/wktrdata/PROGRESS.md`，雙載必漂移。
7. 所有路徑 **MUST** 以 `git rev-parse --path-format=absolute --git-common-dir` 錨定 main root，**MUST NOT** 用 CWD 相對路徑。
