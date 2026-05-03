# Gemini 架構師提交單 — Progressive Disclosure 重構

**提交單編號**: REFACTOR-PD-01
**提交日期**: 2026-04-26
**來源**: CSO 戰略分析 — Claude-Mem Progressive Disclosure 模式借鑑
**優先級**: 高（每 session 節省 ~6,500 tokens = 等效 +3-4 輪完整架構對話空間）

---

## 一、問題診斷摘要

### 1.1 PROGRESS.md — 573 行歷史博物館

| 區段 | 行數 | 佔比 | 每 session 必要性 |
|------|------|------|-----------------|
| D0~D3 已關閉 Tickets | ~220 | 38% | ❌ 永遠不需要 |
| D5~D7 已關閉 Tickets | ~120 | 21% | ❌ 永遠不需要 |
| KAIROS #1 + #2 歸檔 | ~90 | 16% | ❌ 除非回查特定規則來源 |
| Debt / Standby | ~40 | 7% | 🟡 偶爾需要 |
| D8 當前狀態 + Blockers | ~60 | 10% | ✅ 每次都需要 |
| Sprint Health + Quick Links | ~43 | 8% | ✅ 每次都需要 |
| **合計** | **573** | 100% | **僅 18% 是每次必要** |

**問題**：每個 Claude Code session 載入 ~7,000 tokens 的 PROGRESS.md，其中 75% 是已關閉的歷史。

### 1.2 Session Health 表格 ×3 重複

同一張判斷表（≤10 🟢 / 11-18 🟡 / ≥19 🔴）分別出現在：
- `.claude/skills/sv/SKILL.md` 第 85-92 行
- `.claude/skills/qa/SKILL.md` 第 55-62 行
- `.claude/skills/token-monitor/SKILL.md`（若存在）

維護風險：調整閾值需改三處。

### 1.3 /compact 保母式語句

以下語句屬於「說教」而非「規格」，增加 SKILL.md 的 token 消耗但不增加行為精度：
- 「禁止寫繼續開發這種空話」
- 「交接文件必須自足 (self-contained)——新對話只讀這份文件...」
- 「不要複製代碼，只引用檔案路徑和函數名」

---

## 二、改進方案

### 2.1 PROGRESS.md 拆為兩層

**Layer 1: `PROGRESS.md`（Dashboard，≤ 50 行）** — 每 session 必載

```markdown
# Cueshot MVP Sprint Dashboard

**Sprint**: D0-D15 | **Branch**: `feature/cueshot-mvp`
**Current Day**: D8-VERIFY ✅ | **Next KAIROS**: D10 (04-21)
**Last Updated**: 2026-04-24

---

## Sprint Health

| Metric | Status |
|--------|--------|
| Schedule | D8 ✅ — on track |
| Compile | 0 errors ✅ |
| Tests | 224t / 0 failed ✅ |
| Lint | 0 new ✅ |

## Active Blockers

| ID | Issue | Severity | Root Cause |
|----|-------|----------|------------|
| I8 | IFTR 歸零 — isAllAligned 永遠 false | 🔴 Critical | HeadroomRule × ThirdsRule 幾何互斥 |
| I7 | 對焦圓環永動循環 | 🟡 Medium | EyeAfController 每 2s 無條件重觸發 |

## Next Actions

1. I8 修復：`isAllAligned` 改為加權平均分 ≥ 70 → `AnalyzeCompositionUseCase.kt`
2. I7 修復：EyeAfController 加入 `lens=Focused && 漂移 < 10px → skip` → `EyeAfController.kt`
3. Gemini Prototyper → State 1/2/3 靜態原型 → Claude Tech Audit

## Active Debt（僅列未解決項）

- 🟡 SSOT-ORPHAN-RULE-01：PtfRuleAggregator 雙份邏輯（D10 KAIROS #3）
- 📝 Candidate Rule 17：有狀態平滑器 Lifecycle Gate（D10 歸入）

## Quick Links

- SSOT: `SmartCameraApp_Strategy_v3_3_SSOT_20260404.md`
- Tech Spec: `SmartCameraApp_TechDeepDive_v3_20260405.md`
- Roadmap: `Cueshot_MVP_Phase1_Roadmap_v1.md`
- Full History: `PROGRESS_ARCHIVE.md`
```

**Layer 2: `PROGRESS_ARCHIVE.md`（歷史歸檔）** — 僅按需讀取

將以下內容從 PROGRESS.md **剪下** 移入：
- 所有已 CLOSED 的 Day sections（D0、D0.1、D0.x、D1、D2、D3、D5、D6、D7、D8 已完成的 tickets）
- KAIROS #1 和 #2 的完整歸檔（Error Patterns、Decision Audit、SSOT Drift）
- Standby Mechanisms 段落
- 已解決的 Debt 項目

**歸檔檔案結構**：
```markdown
# Cueshot MVP Sprint Archive

## Archived Days
### D0 — 絞殺者手術 ✅ CLOSED (2026-04-08)
[原 D0 全部內容]

### D1 — Rule Engine 骨架 ✅ CLOSED (2026-04-09)
[原 D1 全部內容]
...

## KAIROS Retrospectives
### KAIROS #1 — D2 (2026-04-11) ✅ EXECUTED
[原 KAIROS #1 全部內容]

### KAIROS #2 — D6 (2026-04-15) ✅ EXECUTED
[原 KAIROS #2 全部內容]

## Resolved Debt
[已解決的技術債]

## Standby Mechanisms
[Phase 2 啟用項目]
```

---

### 2.2 /compact SKILL.md 重構

**目標**：輸出改為 Dashboard-first + `<details>` 折疊式細節

**新版 `/compact` SKILL.md**:

```markdown
# Description
壓縮當前對話為精簡交接摘要。Dashboard-first，細節折疊。

# Prompt
執行 Context 壓縮。先取得系統時間：
`powershell -command "Get-Date -Format 'yyyyMMdd_HHmm'"`

產出以下格式，儲存至 `docs/session_handoff/SESSION_HANDOFF_<時間戳>.md`：

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

提醒：「交接文件已儲存。建議：開新對話 → 貼入 Dashboard 段 → 按需展開細節。」
```

---

### 2.3 Session Health 表格去重

**行動**：從 `/sv` 和 `/qa` 的 SKILL.md 中**刪除** Session Health 表格（各 ~8 行），改為引用。

**在 `/sv` SKILL.md 末尾改為**：
```
Session 健康度：參照 CLAUDE.md §Session Health 規則。在 Report 末尾附上：
📊 Session: ~[N] turns | [狀態 emoji]
```

**在 `/qa` SKILL.md 末尾改為**：
```
Session 健康度：參照 CLAUDE.md §Session Health 規則。在 Report 末尾附上：
📊 Session: ~[N] turns | [狀態 emoji]
```

**在 CLAUDE.md 新增一個段落**（成為 SSOT）：
```markdown
## ⏱️ Session Health 判斷規則（SSOT — 所有 Skill 引用此處）

| 對話輪數 | 狀態 | 建議 |
|---------|------|------|
| ≤ 10 turns | 🟢 | 繼續工作 |
| 11-18 turns | 🟡 | 留意，密集 Coding 可考慮下個 Ticket 前 /compact |
| ≥ 19 turns | 🔴 | 建議立即 /compact 開新 Session |

「turns」= user 訊息總數（不含系統 Prompt）。單則 >200 行代碼額外計 +2 turns。
```

---

### 2.4 /compact 保母語句精簡

**刪除以下語句**（理由：Agent 已從 CLAUDE.md 學會這些行為，重複寫入不增加行為精度，只增加 token 消耗）：

| 刪除語句 | 理由 |
|---------|------|
| 「交接文件必須自足 (self-contained)——新對話只讀這份文件 + CLAUDE.md + PROGRESS.md 就能繼續工作」 | Dashboard 段的「帶入」欄位已隱含此意 |
| 「不要複製代碼，只引用檔案路徑和函數名」 | 範例格式本身已示範此模式 |
| 「下一步待辦 (Next Action) 必須具體到檔案名稱與函數，禁止寫繼續開發這種空話」 | 範例中的 `[精確到檔案 + 函數名]` 已足夠 |

---

## 三、實作範圍與禁區

### 3.1 需修改的檔案（4 + 1 新增）

| 檔案 | 動作 | 變更摘要 |
|------|------|---------|
| `PROGRESS.md` | **重寫** | 573 行 → ~50 行 Dashboard |
| `PROGRESS_ARCHIVE.md` | **新增** | 接收 PROGRESS.md 剪下的歷史內容 |
| `.claude/skills/compact/SKILL.md` | **重寫** | Dashboard-first + `<details>` 折疊 + 刪保母語句 |
| `.claude/skills/sv/SKILL.md` | **微改** | 刪除 Session Health 表格（~8 行）→ 引用 CLAUDE.md |
| `.claude/skills/qa/SKILL.md` | **微改** | 刪除 Session Health 表格（~8 行）→ 引用 CLAUDE.md |
| `CLAUDE.md` | **追加** | 新增 §Session Health 段落（~8 行） |

### 3.2 🚫 絕對禁區

- **不改** `/sv` 和 `/qa` 的驗收邏輯（階段 A/B/C、Step 1-4）
- **不改** CLAUDE.md 的既有規則（Rules 1-16）
- **不改** `/uc`、`/normal`、`/dream` 的 SKILL.md
- **不改** SSOT v3.3 或 Tech Deep Dive v3

### 3.3 依賴檢查

`/qa` Step 4 寫道：「讀取 PROGRESS.md，更新以下欄位：Today's Tickets 中已完成項目的 Status → ✅ DONE」。

Dashboard 版 PROGRESS.md 不再有 Today's Tickets 表格。`/qa` Step 4 需同步改為：
```
Step 4: 更新 PROGRESS.md Dashboard
- Sprint Health 指標（Compile/Tests/Lint）
- Active Blockers（新增或移除阻塞點）
- Next Actions（更新下一步）
- 若 Ticket 完成，在 PROGRESS_ARCHIVE.md 對應 Day 段落標記 ✅ DONE
```

---

## 四、驗收標準

| 項目 | 驗收條件 |
|------|---------|
| PROGRESS.md | ≤ 50 行，包含 Sprint Health + Blockers + Next Actions + Active Debt + Quick Links |
| PROGRESS_ARCHIVE.md | 包含所有 D0~D8 已完成 Tickets + KAIROS #1/#2 完整歸檔 |
| 資訊零遺失 | PROGRESS.md + PROGRESS_ARCHIVE.md 合併後 = 原 PROGRESS.md 全部內容 |
| /compact 輸出 | Dashboard ≤ 6 行 + 3 個 `<details>` 折疊區塊 |
| Session Health SSOT | CLAUDE.md 有且僅有一份定義；/sv 和 /qa 引用而非複製 |
| /qa Step 4 | 更新目標改為 Dashboard 欄位 + Archive 標記 |
| grep 驗證 | `grep -r "≤ 10 turns" .claude/skills/` 結果 = 0（表格已從 skills 移除）|

---

## 五、預估效益

| 指標 | 改進前 | 改進後 | 效益 |
|------|--------|--------|------|
| PROGRESS.md 每 session token 消耗 | ~7,000 | ~500 | **-93%** |
| /compact 輸出 token | ~800 | ~200（Dashboard）+ 按需展開 | **-75% 預設** |
| Session Health 維護點 | 3 處 | 1 處（CLAUDE.md） | **-67%** |
| /compact SKILL.md token | ~650 | ~350 | **-46%** |
| 每 session 可用 context 空間增量 | — | +6,500 tokens | ≈ +3 輪完整架構對話 |

---

## 六、執行順序建議

```
Step 1: 建立 PROGRESS_ARCHIVE.md（純搬移，不改內容）
Step 2: 重寫 PROGRESS.md 為 Dashboard
Step 3: CLAUDE.md 追加 §Session Health
Step 4: /sv + /qa 刪除 Session Health 表格 → 改引用
Step 5: /compact SKILL.md 重寫
Step 6: /qa Step 4 更新
Step 7: 驗收 — grep 驗證 + 行數檢查 + 內容完整性比對
```

**注意**：Step 1-2 必須一起執行（原子操作），否則中間狀態會丟失歷史資料。

---

## 文件元資訊

- **提交單編號**: REFACTOR-PD-01
- **建立日期**: 2026-04-26
- **來源**: CSO × Claude-Mem Progressive Disclosure 模式分析
- **適用工具**: Gemini 架構師執行（大量文本搬移適合 1M context）
- **回饋路徑**: 完成後截圖帶回 Claude CSO 確認
