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

## 冊面收束一致性閘

**MUST** 在 `## Auto Commit: Governance Artifacts` 全部步驟之前執行，含以下三條機械判準（禁語義化裁量）：

1. **觸發偵測**：`git status --short` 輸出中存在 `plans/cso_briefing/project/deliver/<票號>*.md`（票檔進交付區），或 `PROGRESS.md` diff 含該票號之 CLOSED 字面 → 該票號列入待驗清單。

2. **一致性驗證（目錄—狀態明確集合，兩級判定）**：對待驗清單每一票號，以 `registry_list(project="SmartCameraApp1", ticket_id=<票號>)` 讀取冊面現值，依觸發訊號分級檢核：
   - **級 A**（票檔進 `deliver/`）→ `lifecycle` ∈ {`REVIEW`, `CLOSED`}（`REVIEW` 為合法交付態，PLT 五段④；`VOID` 不屬 `deliver/`，其歸位為 `void/`）
   - **級 B**（`PROGRESS.md` diff 標該票 CLOSED）→ `lifecycle` = `CLOSED`
   - **共同**：`file` 欄指向現存路徑（非 `Pending/` 死指標）
   - **目錄—狀態明確集合**（判定之唯一依據，禁語義裁量），逐集合分別指認來源：
     - `Pending/` ↔ {`PENDING`,`TICKET_RIGOR`,`IN_PROGRESS`,`EXECUTED`,`BLOCKED`}：四值來源「Pending/ 淨空原則」§1（明文「**MUST NOT** 窄寫為『僅 PENDING』」）；`BLOCKED` 來源「Ticket 生命週期流程（五段）」旁支條（「`BLOCKED` 可自任意活態進出」）＋淨空原則 §3（暫時態，禁無限期停留）。
     - `deliver/` ↔ {`REVIEW`,`CLOSED`}：來源「物理歸位邊界」（Akira 2026-09-07）——`REVIEW` 即搬 `deliver/`、`CLOSED` 保持於 `deliver/`。
     - `void/` ↔ {`VOID`}：來源同旁支條（`VOID` 為終態）＋「VOID 歸位紀律」（票檔移 `void/`）。
     - 「五段觸發與寫入者」節之性質為「誰在何時寫入」，**MUST NOT** 被引為目錄歸屬之來源；該節僅列 `TICKET_RIGOR`／`IN_PROGRESS`／`EXECUTED`／`REVIEW`／`CLOSED` 五值，不含 `PENDING`／`BLOCKED`／`VOID`。
     - 平台 `registry_service.py` `VALID_LIFECYCLE` 為 lifecycle 合法值之上游檢核來源，**不取代** APP 端之目錄歸屬定義。
     - **未知值 fail-closed**：`DRAFT_NEEDED` 不屬本表。`registry_list` 回傳任何未列入本表之 lifecycle 值（含 `DRAFT_NEEDED`）→ 回報 `⛔ Closure Gate UNKNOWN_LIFECYCLE_MAPPING @<票號>: lifecycle=<實值> / file=<實值>` 並停止全部後續 Auto Commit；**MUST NOT** 推定映射至任一目錄。
   - **MUST NOT** 使用 `get_ticket`——該工具查的是執行審批票 SQLite（另一套資料），對治理冊面票恆回 `NOT_FOUND`，誤用即把「查錯資料庫」讀成「該票未入冊」。

3. **不一致處置**：任一項不符 → **MUST** 回報 `⛔ Closure Gate FAILED @<票號>: lifecycle=<實值> / file=<實值>` 並**停止全部後續 Auto Commit 步驟**（比照既有 `⛔ Auto Commit BLOCKED` 之停止語義）；**MUST NOT** 代呼叫任何 registry 寫入原語；由 host session 以正規原語補齊後重跑 `/compact`。待驗清單含多票號時，逐票獨立判定，任一票 FAILED 即整批阻擋，回報行 MUST 列出全部 FAILED 票號（非僅首個）。

待驗清單為空 → 該節 SKIPPED，**MUST NOT** 阻斷。

**分類固定，禁個別 lifecycle 特例**：目錄歸屬分類恰有三類（`Pending/`／`deliver/`／`void/`），每一類只以其對應集合之成員關係判定。級 A、級 B、`PROGRESS.md` 觸發、`file` 欄檢核、`SKIPPED`／`FAILED`／`UNKNOWN` 處置皆為驗證規則而非目錄分類，不計入分類分支數。**MUST NOT** 因任一單一 lifecycle 值（如 `TICKET_RIGOR`／`IN_PROGRESS`／`EXECUTED`／`BLOCKED`／`DRAFT_NEEDED`）新增專屬判定分支、例外、轉向或例外處置；未列值一律走 `UNKNOWN_LIFECYCLE_MAPPING` 停止處置，不得回退為個別值例外。

## Auto Commit: Governance Artifacts

**MUST** 在 Housekeeping 完成、確認訊息輸出**之前**執行。Governance Artifact 清單（檔案路徑 + commit prefix）**MUST** 以 `.claude/rules/ssot-governance.md` 「Governance Artifact 清單」表為單一真相來源，**MUST NOT** 在本檔重複硬編碼清單內容——新增/異動 Governance Artifact 一律改該表，本步驟只讀表逐檔迴圈。

1. **Scope 汙染確認**：`git status --short`，若有 tracked dirty 檔案**不在**上述表列中 → **MUST** 停止，回報 `⛔ Auto Commit BLOCKED: Scope 汙染 [檔案清單]`，**不執行任何 commit**（含表列檔案）。
2. **逐檔獨立提交**（表列每一行各自執行以下判定，互不阻擋）：
   - **零 diff 判定**：`git status --short -- <檔案>` 輸出為空 → 判定 SKIPPED，回報 `⚪ Auto Commit SKIPPED（<檔案> 無異動）`，**不執行 commit**，**MUST NOT** 使用 `git commit --allow-empty`。
   - **Commit**：`git add <檔案>` → `git commit -m "<Prefix> <檔案> Dashboard/Registry 同步：/compact 交接"`（**SHOULD** 加 `Co-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>` trailer）。同 Prefix 之多檔（如 PROGRESS.md + PROGRESS_ARCHIVE.md 同時異動）**MAY** 合併為單一 commit。Commit 失敗 → **MUST** 回報 `⛔ Auto Commit FAILED: [檔案]`，不得靜默吞錯。

**SHOULD** 完成後輸出以下確認訊息（必須包含實際檔案路徑），並於末尾附加逐檔 Auto Commit 狀態行：
「交接文件已儲存至 docs/session_handoff/SESSION_HANDOFF_<實際時間戳>.md，PROGRESS.md 已同步更新。Housekeeping 已完成：docs/session_handoff/ 僅保留當天 handoff，過期檔已移至 docs/session_handoff/Archive/。建議：開新對話 → 貼入 Dashboard 段 → 按需展開細節。」
逐檔 Auto Commit 狀態行**之前**先輸出一行：`📌 收束閘: [PASS|SKIPPED|⛔FAILED@票號]`
每檔各輸出一行：`📌 Auto Commit: [hash] [Prefix] <檔案>` / `📌 Auto Commit: SKIPPED（<檔案> 無異動）` / `⛔ Auto Commit BLOCKED/FAILED`（依上述判定擇一）
