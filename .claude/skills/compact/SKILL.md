---
name: compact
description: 同專案 session 管理下的 durable progress 同步與交接規格。原生 /compact、/clear、/resume 優先；本技能負責 .claude/PROGRESS.md 與必要 handoff 檔。
disable-model-invocation: false
---

> RFC 2119 Keywords: **MUST** = 違反即 FAIL；**SHOULD** = 強烈建議，可附理由跳過；**MAY** = 可選。

# Prompt

規格來源：`Skill-Advisor/deliver/compact-skill/SKILL_RevB.md` ＋ 部署票 `INFRA-COMPACT-REVB-APP-01`

# Compact Skill — Rev B

來源說明：本規格以目前附件版 `SKILL.md` 的既有 handoff、`PROGRESS.md` 同步、Archive 與 Git 治理意圖為基礎重寫，同時對齊 Claude Code 目前官方 session 管理原則：同一議題延續優先使用 `/compact`，切換不相關工作使用 `/clear`，回到既有工作優先使用 `/resume`。本技能不取代原生 session 指令；本技能的責任是把**跨 session 必須存活的專案進度與交接事實**回存到 durable files，並確保 `.claude/PROGRESS.md` 成為同專案新 session 的第一入口。

## 1. 目的與定位

### 1.1 目的

本技能用於同一專案內的 session checkpoint、跨 session 交接與 durable progress 同步。它的主要目標如下：

- 將本次工作中**不可只依賴 transcript 保存**的狀態，落盤到 `.claude/PROGRESS.md`。
- 在需要跨 session、跨人或跨 agent 交接時，產出 `docs/session_handoff/SESSION_HANDOFF_<timestamp>.md`。
- 讓新 session 或恢復中的 session 能先讀 `.claude/PROGRESS.md`，必要時再讀 handoff 檔，而不是把 handoff 當作原生 `/resume` 的替代品。
- 保留原附件對 Dashboard-first、交接摘要、Archive 與治理可追溯性的要求，但將其與原生 `/compact` 語義解耦。

### 1.2 非目標

本技能**不是**以下功能的替代品：

- **不是** Claude Code `/compact` 的替代品；session context 壓縮仍由原生命令處理。
- **不是** Claude Code `/resume` 的替代品；既有 session 的恢復仍以原生 session 機制為優先。
- **不是** project memory 的唯一儲存位置；長期規則、架構不變量與 compact 保留偏好應放在 `CLAUDE.md`。
- **不是**預設自動提交工具；Git stage / commit / ticket lifecycle 改動屬 delivery governance，必須顯式觸發。

## 2. 原生 session 原則

本技能 MUST 遵守下列原則：

1. **同一議題延續且 context 過長**：優先使用原生 `/compact`。
2. **切換到不相關議題**：優先在 checkpoint 後使用原生 `/clear`。
3. **返回既有議題**：優先使用原生 `/resume` 或既有 session 恢復能力。
4. `.claude/PROGRESS.md` 是 durable project progress 的主要入口；`docs/session_handoff/*.md` 是補充性交接證據，不是 session transcript 的替代品。

## 3. 產物與資料權威

### 3.1 資料層級

本技能涉及的資料來源依權威性分層如下：

1. **原生 session transcript / `/resume`**：當前與既有 session 的完整對話脈絡。
2. **`.claude/PROGRESS.md`**：同專案 durable progress SSOT 之一；新 session 第一優先讀取。
3. **`docs/session_handoff/SESSION_HANDOFF_<timestamp>.md`**：顯式 checkpoint 或交接時的補充證據檔。
4. **`Archive/` 歷史檔**：歷史回顧用途，不作當前狀態唯一依據。

### 3.2 不變量

- `.claude/PROGRESS.md` **MUST** 反映當前 active workstream 的最新狀態。
- handoff 檔案 **MUST NOT** 取代 `.claude/PROGRESS.md` 作為新 session 的第一入口。
- Archive 只保存歷史，不得覆蓋當前活躍狀態。
- 本技能 **MUST NOT** 宣稱其輸出可完整替代 Claude Code session transcript。

## 4. 執行模式

本技能分為三種模式，避免將所有副作用綁定在單一 `compact` 語義下。

呼叫語法：`/compact`（無參數，**MUST** 等同 `/compact checkpoint`）／`/compact brief`／`/compact checkpoint`／`/compact closeout`。**無參數時之預設模式 MUST 為 `checkpoint`**。

### 4.1 `brief` 模式

用途：新 session 啟動、`/resume` 後快速理解目前專案狀態。

規則：

- **MUST** 先讀 `.claude/PROGRESS.md`。
- **MAY** 讀取最新一份 `docs/session_handoff/SESSION_HANDOFF_<timestamp>.md` 作補充。
- **MUST NOT** 寫入任何檔案。
- **MUST NOT** 執行任何 Git stage / commit。
- **MUST** 在完成上述動作後直接結束，**MUST NOT** 到達本檔「冊面收束一致性閘」與「Auto Commit: Governance Artifacts」二節之邏輯。

### 4.2 `checkpoint` 模式

用途：暫停目前 workstream、切換議題前、需要 durable progress sync 時。

規則：

- **MUST** 更新 `.claude/PROGRESS.md`。
- **SHOULD** 在下列條件成立時產出 handoff 檔：
  - 即將跨 session 暫停；
  - 可能由另一位人員或 agent 接手；
  - 已累積非 trivial 的決策、blocker、驗證證據；
  - 即將 `/clear` 切換到不相關議題。
- **MUST NOT** 預設進行 Git commit。
- **MAY** 執行非破壞性的 housekeeping，例如 Archive 歷史 handoff（見 §7），但不得影響 `.claude/PROGRESS.md` 的現況可讀性。
- **MUST** 在完成上述動作後直接結束，**MUST NOT** 到達本檔「冊面收束一致性閘」與「Auto Commit: Governance Artifacts」二節之邏輯。

### 4.3 `closeout` 模式

用途：工作已達完成／收束條件，準備交付或關閉 workstream。

規則：

- **MUST** 更新 `.claude/PROGRESS.md` 為最新完成狀態。
- **SHOULD** 產出 handoff 或 closeout 文件，記錄完成內容、驗收證據與 reopen 條件。
- **MAY** 提出 Git stage / commit 建議，但必須等待顯式確認。
- **MUST NOT** 因 closeout 而自動取代 ticket lifecycle 的治理決策。
- **MUST** 依序執行本檔「冊面收束一致性閘」節、再執行「Auto Commit: Governance Artifacts」節——此二節之呼叫路徑僅限於 `closeout` 模式。

## 5. `.claude/PROGRESS.md` 規格

`.claude/PROGRESS.md` 是本技能的核心產物。每次 `checkpoint` 或 `closeout` 模式，均 **MUST** 更新。

建議結構如下：

```markdown
# PROGRESS

## Active Workstream
- Ticket / Workstream:
- Objective:
- Current Status:
- Current Branch:
- Source Session:
- Last Verified:

## Sprint Health
- Overall:
- Confidence:
- Context Mode: continue / compacted / cleared / resumed

## Active Blockers
- ...

## Next Actions
- [ ] path + function + expected outcome

## Carry-forward Facts
- Confirmed decisions
- Constraints
- Relevant files
- Tests run / pending
```

### 5.1 更新規則

- **MUST** 寫明目前 active workstream。
- **MUST** 將「下一步」精確到檔案、函數或可執行單位。
- **MUST** 將 blocker、已確認決策與約束落盤。
- **Source Session 欄 MUST** 直接取自 `tmp/claude_status_cache.json` 頂層 `session_id`（producer：`.claude/skills/status/scripts/statusline.py` 之 `write_cache()`）；該快取檔或該欄位不存在時，**MUST** 標記 `unavailable`，**MUST NOT** 以檔名、時間戳等弱指紋推定。
- **Context Mode 欄值 MUST** 由呼叫時明示參數決定（`resumed`／`compacted`／`cleared`）；無參數呼叫時固定為 `continue`，**MUST 禁自行語義推定**實際模式。
- **MUST NOT** 在 `.claude/PROGRESS.md` 保存冗長原始 log。
- `.claude/PROGRESS.md` **MUST** 偏向「當前態」，歷史長文應放入 handoff 或 archive。

## 6. Handoff 檔規格

### 6.1 產出路徑

當需要建立 handoff 檔時，**SHOULD** 使用：

`docs/session_handoff/SESSION_HANDOFF_<yyyyMMdd_HHmm>.md`

### 6.2 建議內容

```markdown
# Session Handoff — <日期時間>

## Dashboard（新 session 先讀這段）
- 完成：
- 阻塞：
- 下一步：
- 帶入：CLAUDE.md · .claude/PROGRESS.md · <relevant files>

## 關鍵決策
| 決策 | 理由 | 影響範圍 |
|------|------|---------|

## 變更明細
| 檔案 | 函數/區塊 | 變更內容 |
|------|----------|---------|

## 驗證與證據
- Commands run:
- Tests passed / failed:
- Outstanding risks:

## 未識別檔處置
- path:
- classification:
- decision:

## Excluded History
- 已證偽路徑
- 不再需要的原始 log
- 過期假設
```

### 6.3 使用規則

- handoff 檔 **SHOULD** 在需要 durable handoff 時建立，而不是每次原生 `/compact` 都建立。
- handoff 檔 **MUST** 聚焦於跨 session 必須保留的事實。
- handoff 檔 **MUST NOT** 嘗試完整重建 session transcript。
- handoff 檔 **SHOULD** 引用 `.claude/PROGRESS.md`，不得與其形成雙重真相競爭。

### 6.4 雙 handoff 載體邊界

本 skill 之 `docs/session_handoff/` 為 **repo-local、Claude Code 端**交接載體；Desktop 治理端載體（`Skill-Advisor/Handoff/`）職責互斥，**MUST NOT** 交叉寫入。

## 7. Archive 規則

若執行 housekeeping，`docs/session_handoff/Archive/` **MAY** 保存歷史 handoff。

規則如下：

1. `docs/session_handoff/Archive/` 不存在時可建立。
2. 依 mtime 排序，`docs/session_handoff/` 根目錄 **MUST** 僅保留最新 **2** 份 handoff 檔；其餘 **MUST** 移入 `Archive/`。
3. **MUST NOT** 覆寫 Archive 內既有同名檔案；同名衝突時跳過並於輸出載明。
4. **MUST NOT** 因 Archive 操作影響 `.claude/PROGRESS.md` 或保留中 handoff 的可讀性。

建議 PowerShell：

```powershell
$root = 'docs/session_handoff'
$archive = Join-Path $root 'Archive'
New-Item -ItemType Directory -Force -Path $archive | Out-Null

Get-ChildItem -Path $root -File -Filter 'SESSION_HANDOFF_*.md' |
  Sort-Object LastWriteTime -Descending |
  Select-Object -Skip 2 |
  ForEach-Object {
    $dest = Join-Path $archive $_.Name
    if (-not (Test-Path -LiteralPath $dest)) {
      Move-Item -LiteralPath $_.FullName -Destination $dest
    }
  }
```

## 8. Git 與治理規則

### 8.1 預設原則

- `brief` 與 `checkpoint` 模式 **MUST NOT** 自動執行 `git add -A`、`git add .`、`git commit`。
- 任何 Git stage / commit 都 **MUST** 屬於顯式的 `closeout` 或 delivery 流程。
- `.claude/PROGRESS.md` 更新不等於自動同意提交。

### 8.2 允許的 closeout 行為

在顯式 `closeout` 模式下：

- **MAY** 顯示 `git status --short` 作為預覽。
- **MAY** 依使用者確認逐檔 `git add`。
- **MUST NOT** 使用 `git add -A` 或 `git add .`。
- **MUST** 將無法分類或未獲裁決的檔案降為未決，不得靜默提交。
- **SHOULD** 將未決項目寫入 handoff 的「未識別檔處置」段。

## 9. 與 CLAUDE.md 的關係

`CLAUDE.md` 的用途如下：

- 保存長期規則、架構不變量、程式風格與 compact instructions。
- 保存「原生 `/compact` 時應保留什麼」的全域規則。
- **MUST NOT** 用來取代 `.claude/PROGRESS.md` 的當前進度狀態。

建議在 `CLAUDE.md` 保留類似規則：

```markdown
## Compact Instructions
When compacting, preserve:
- active workstream and acceptance criteria
- confirmed decisions and rationale
- modified files and current git state
- blocker, tests run, and next actions
Do not preserve:
- raw verbose logs
- repeated exploration
- superseded assumptions
```

## 10. 操作流程

### 10.1 同一議題、同一 session

1. 使用原生 `/compact` 管理 context。
2. 若決策、blocker、下一步或驗證狀態已改變，執行 `checkpoint` 模式更新 `.claude/PROGRESS.md`。
3. 除非需要 durable handoff，否則不必產生 handoff 檔。

### 10.2 同專案、切換到不相關議題

1. 執行 `checkpoint` 模式。
2. **MUST** 更新 `.claude/PROGRESS.md`。
3. 視需要建立 handoff。
4. 再使用原生 `/clear` 開始新議題。

### 10.3 回到既有議題

1. 優先使用原生 `/resume`。
2. 先讀 `.claude/PROGRESS.md`。
3. 必要時再讀最新 handoff 檔作補充。

## 11. 驗收標準

本 Rev B 規格至少須滿足下列條件：

- 同專案 session 管理遵循原生 `/compact`、`/clear`、`/resume` 優先原則。
- `.claude/PROGRESS.md` 成為新 session 的第一入口。
- 每次 `checkpoint` 與 `closeout` 都確實回存執行進度到 `.claude/PROGRESS.md`。
- handoff 檔只在需要 durable handoff 時建立，不再與原生 `/compact` 綁死。
- 預設流程不自動進行 Git commit。
- 歷史 handoff 可 Archive，但不得破壞目前 workstream 的可讀性。

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
