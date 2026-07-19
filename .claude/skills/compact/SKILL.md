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

## 最後 Housekeeping

**MUST** 在交接文件寫入且 PROGRESS.md 更新完成後，清理 `docs/session_handoff/`：

1. 使用同一個系統日期 `yyyyMMdd` 作為「當天」判斷基準。
2. 確保 `docs/session_handoff/Archive/` 存在。
3. `docs/session_handoff/` 根目錄只保留當天檔案：`SESSION_HANDOFF_<yyyyMMdd>_*.md`。
4. 將非當天的 `SESSION_HANDOFF_*.md` 搬移至 `docs/session_handoff/Archive/`。
5. 不處理 `Archive/` 內既有檔案；若 Archive 已存在同名檔案，不覆寫，改為跳過並在最終訊息註明。

建議 PowerShell：

```powershell
$today = Get-Date -Format 'yyyyMMdd'
$root = 'docs/session_handoff'
$archive = Join-Path $root 'Archive'
New-Item -ItemType Directory -Force -Path $archive | Out-Null

Get-ChildItem -Path $root -File -Filter 'SESSION_HANDOFF_*.md' |
  Where-Object { $_.Name -notlike "SESSION_HANDOFF_${today}_*.md" } |
  ForEach-Object {
    $dest = Join-Path $archive $_.Name
    if (-not (Test-Path -LiteralPath $dest)) {
      Move-Item -LiteralPath $_.FullName -Destination $dest
    }
  }
```

## Auto Commit: PROGRESS.md

**MUST** 在 Housekeeping 完成、確認訊息輸出**之前**執行：

1. **Scope 汙染確認**：`git status --short`，除 `.claude/PROGRESS.md` 外若有其他 **tracked** dirty 檔案 → **MUST** 停止，回報 `⛔ Auto Commit BLOCKED: Scope 汙染 [檔案清單]`，**不執行 commit**。
2. **零 diff 判定**：`git status --short -- .claude/PROGRESS.md` 輸出為空 → 判定 SKIPPED，回報 `⚪ Auto Commit SKIPPED（PROGRESS.md 無異動）`，**不執行 commit**，**MUST NOT** 使用 `git commit --allow-empty`。
3. **動態編號**：
   ```bash
   _LAST_N=$(git log --oneline --all 2>/dev/null | grep -oE "INFRA-COMPACT-SYNC-[0-9]+" | grep -oE "[0-9]+" | sort -n | tail -1)
   _NEXT_N=$(( ${_LAST_N:-0} + 1 ))
   ```
4. **Commit**：`git add .claude/PROGRESS.md` → `git commit -m "[INFRA-COMPACT-SYNC-${_NEXT_N}] PROGRESS.md Dashboard 同步：/compact 交接"`（**SHOULD** 加 `Co-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>` trailer）。Commit 失敗 → **MUST** 回報 `⛔ Auto Commit FAILED`，不得靜默吞錯。

**SHOULD** 完成後輸出以下確認訊息（必須包含實際檔案路徑），並於末尾附加 Auto Commit 狀態行：
「交接文件已儲存至 docs/session_handoff/SESSION_HANDOFF_<實際時間戳>.md，PROGRESS.md 已同步更新。Housekeeping 已完成：docs/session_handoff/ 僅保留當天 handoff，過期檔已移至 docs/session_handoff/Archive/。建議：開新對話 → 貼入 Dashboard 段 → 按需展開細節。」
`📌 Auto Commit: [hash] [INFRA-COMPACT-SYNC-${_NEXT_N}]` / `📌 Auto Commit: SKIPPED（PROGRESS.md 無異動）` / `⛔ Auto Commit BLOCKED/FAILED`（依上述判定擇一）
