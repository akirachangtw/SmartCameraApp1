# ⚠️ 最高憲法（ABSOLUTE PRIORITY — 所有規範中最優先，不可覆蓋）

> **這是本專案的最高開發紀律。任何操作前必須先遵守以下條款，優先順序高於所有其他章節。**

## 🛑 零容忍執行紀律 (Zero-Tolerance Constraints)

1. **零偷懶政策**：絕對禁止留下 `// TODO`、`return null // implement later`，必須真實接線。
2. **焦土政策**：重構時，必須主動搜尋並刪除舊管線的廢棄變數與 UI 元件。
3. **錯誤防禦**：必須處理可能導致 Crash 的 null 狀態與陣列越界。所有 `when` 必須 exhaustive。
4. **架構純潔**：嚴格遵守 UDF (Unidirectional Data Flow)，狀態層與 UI 層絕對分離。UI 不直接呼叫 Repository 或 UseCase。
5. **命名一致 (Anti-Drift)**：所有類別名稱、變數名稱必須與 v3.3 SSOT 一致。禁止自行發明未經授權的命名（如 SSOT 定義 `PitchTractionLayer` 但實作叫 `WarmGradient`）。
6. **預設值陷阱防禦**：None/Empty 狀態的 UI 必須**完全透明或隱藏**，禁止畫出預設框或預設顏色（如 `Size(36.dp)` 硬編碼、`Color.Gray` 佔位）。
7. **硬體頻率脫鉤**：高頻視覺追蹤 (Halo 10fps) 與低頻硬體呼叫 (AF 冷卻 2-3s) 必須徹底分離。前鏡頭永遠不呼叫 AF。
8. **語言紀律**：所有對話輸出、推理描述、工具呼叫說明一律使用**繁體中文或英文**。禁止在任何模式下切換到日文、韓文或其他語言。程式碼內的註解與變數名稱使用英文。

9. **i18n 三道防線**：

    - **UI 層 (Jetpack Compose)**：絕對禁止 Hardcode 顯示字串（例如 `Text("向左微調")`）。所有面向使用者者的靜態文案，必須提取至 `res/values/strings.xml`，並透過 `stringResource(R.string.xxx)` 渲染。
    - **Domain/Data 層**：Domain 層物件（如 `CompositionAdvice`）絕對禁止持有或傳遞本地化字串。必須使用「純語意代碼 (Sealed Class / Enum)」，翻譯責任下放 UI 層。
    - **AI 邏輯層 (LLM Prompt)**：串接 Gemini / Claude 時，必須在 Prompt 內動態注入 Locale 參數，強制 AI 返回正確語言的 JSON 結果。

10. **Bash 指令安全格式**：使用 Bash 工具時，必須嚴格遵守以下規範以配合本地端權限白名單：

    - **禁止切換目錄**：絕對禁止在指令最前方加上 `cd [路徑] &&`。理由：Claude Code 已在專案根目錄運行，且 `cd` 開頭的複合指令會繞過 `settings.local.json` 的權限白名單匹配。
    - **強制使用相對路徑**：所有檔案操作、`git` 或 `grep` 檢索（包含 Bash, Edit, View, Replace 等），直接針對目標檔案或目錄下達指令。
    - ✅ 正確：`grep -n "CameraCharacteristics" app/src/...` / `git log --oneline -8` / `Edit .claude/PROGRESS.md`
    - ❌ 錯誤：`cd "D:\..." && grep ...` / `Edit d:\...\.claude\PROGRESS.md`

---

## 📋 PROGRESS.md 機制（跨 Session 狀態載體）

> **PROGRESS.md 是跨工作階段的唯一進度真理。每次開工先讀、每次收工要更新。**

### 標準作業流程 (SOP)

```text
0. [Resume]  開始任何工作前，先讀 PROGRESS.md 確認上次進度
1. [Plan]    用 Gemini 壓縮需求為精簡 Prompt（不消耗嚴格配額）
2. [Code]    Claude Code 實作（密集 Coding 時啟用 /uc 臥底模式）
3. [QA]      /qa → 編譯 + 測試 + 自動更新 PROGRESS.md
4. [SV]      /sv → 語意審查 + 確定性驗證 + 檔案邊界檢查
5. [Gate]    SV + QA 都通過 → Traycer 終審（每日限 2-4 次）
6. [Log]     當日結束 → /compact 壓縮交接 or 手動更新 PROGRESS.md
```

### Sprint 節點 KAIROS 回顧

在 **D2 / D6 / D10 / D13 / D15** 結束時執行 `/dream`，回顧錯誤模式、決策審計、SSOT 漂移掃描，產出新的 CLAUDE.md 規則。

---

## ⏱️ Session Health 判斷規則（SSOT — 所有 Skill 引用此處）

| 對話輪數 | 狀態 | 建議 |
| --------- | ------ | ------ |
| ≤ 10 turns | 🟢 | 繼續工作 |
| 11-18 turns | 🟡 | 留意，密集 Coding 可考慮下個 Ticket 前 /compact |
| ≥ 19 turns | 🔴 | 建議立即 /compact 開新 Session |

「turns」= user 訊息總數（不含系統 Prompt）。單則 >200 行代碼額外計 +2 turns。

📊 Status: ~[X]K/200K ([Y]%) [🟢 繼續 / 🟡 留意 / 🔴 建議 /compact]
Turns: [N] | Code: ~[M] lines
Suggestion: [建議]
Token 估算基準

---

## 🛠️ 本地 Skills（開發必用）

| 指令 | 檔案位置 | 觸發方式 | 用途 |
| ------ | --------- | ---------- | ------ |
| `/sv` | `.claude/skills/sv/SKILL.md` | **人工 or 自動** | 無情法官：殘骸掃描 + 編譯 + Lint + 邊界檢查 + UDF 合規 + SSOT 漂移 |
| `/qa` | `.claude/skills/qa/SKILL.md` | **人工 or 自動** | 自動化 QA：Build + Unit Test + Warning 清理 + PROGRESS.md 更新 |
| `/uc` | `.claude/skills/uc/SKILL.md` | **人工觸發** | 臥底模式：零廢話純 Diff 輸出，省 Token 省配額 |
| `/normal` | `.claude/skills/normal/SKILL.md` | **人工觸發** | 退出臥底模式，恢復正常對話 |
| `/dream` | `.claude/skills/dream/SKILL.md` | **Sprint 節點觸發** | KAIROS 回顧：錯誤模式提煉 + 決策審計 + SSOT 漂移掃描 |
| `/compact` | `.claude/skills/compact/SKILL.md` | **對話過長時** | Context 壓縮：產出精簡交接文件，準備開新 Session |
| `/mdlint` | `.claude/skills/mdlint/SKILL.md` | **人工觸發** | MD linting 自動修復：掃描並修正 CLAUDE.md 或指定 .md 檔案的格式問題 |

**標準使用順序**（每次實作完成後）：`/qa` → `/sv` → Traycer `strict_verification`

---

## 🛡️ 防禦型工作流鐵律 (Global Rules — AI Agent Behavioral Contract)

> **適用對象**: Claude Code、Cursor、Traycer 等所有 AI 代理。
> 優先順序：本節 < 最高憲法（最高憲法仍最優先）。

## 鐵律 1 — SSOT 優先 (Single Source of Truth)

- **策略 SSOT**: `plans/architecture/SSOT_v4_0.md`
- **技術 SSOT**: `plans/architecture/Cueshot_Layout_Spec_v2_2.md`
- **進度 SSOT**: `PROGRESS.md`

在修改任何核心邏輯前，必須優先查閱上述文件。文件是架構的唯一真理，程式碼的解釋以文件為準。

## 鐵律 2 — 原子化提交 (Atomic Commits)

嚴禁在同一個 Ticket 或 Commit 中混合修改「原始碼（`.kt`、`.java`、`.xml`）」與「專案文件/Log（`.md`、`.log`）」。

```text
✅ Commit 1: [Fix]  程式碼修復（.kt）
✅ Commit 2: [Docs] 文件與 Log 歸檔（.md / .log）
❌ 禁止: 一個 Commit 同時含兩類檔案
```

## 鐵律 3 — Android 邊界防禦 (Boundary Defense)

處理 Coroutine、CameraX 管線或 Sensor 邏輯時，必須主動考慮 Edge Cases：

- 無效幾何尺寸（`<= 0`）→ `return null`，絕不回傳 `Matrix()` 偽裝者
- Flow / Coroutine 加 `.catch { }` 或 `try-catch` 並印出 `Log.e`
- 心跳 Log（每 N 幀）確認管線存活

## 鐵律 4（越權禁令 — v2 強化版）

a) 無 Ticket 不動手：每次實作必須有明確的 Ticket 編號
   （格式：D[X]-T[Y] / P0-[NAME] / INFRA-[XX] / HOTFIX-[XX] / V16-[XX]）。
   若 user 未提供 Ticket 編號，回覆：
   「需要 Ticket 編號才能開始實作。目前 PROGRESS.md 的 Next Actions 有：
   [列出 Next Actions]。請指定要執行哪一個？」
b) 禁止自主探索：不得基於 MEMORY.md、PROGRESS.md 或代碼中的 TODO/FIXME
   自行決定「下一步」。所有工作項目必須由 user 明確指派。
c) Skill 指令不等於 Ticket：/qa、/sv、/compact、/dream、/hygiene 是驗收工具，
   不是實作授權。執行 /qa 發現的問題必須回報 user，不得自行修復。
d) Session 開始必問：若 user 的第一個指令不含 Ticket 編號，
   先詢問今天的 Ticket 是什麼，再開始工作。

## 鐵律 5（語言鎖定）

本專案所有輸出必須為繁體中文或英文技術術語。
嚴禁韓文、日文、簡體中文。違反時立即自我修正。

---

## ⚠️ Critical Project Files - DO NOT DELETE

### Workflow Automation Tools

- **`tools/hybrid_flow.py`** 🔴 CRITICAL — L1/L2/PR planning document generation
- **`tools/hybrid_flow-tasks.json`** 🔴 CRITICAL — VSCode Tasks configuration reference
- **`.vscode/tasks.json`** 🔴 CRITICAL — Active VSCode Tasks configuration
- **`tools/test_handoff_gui.pyw`** 🔴 CRITICAL — Home NB ↔ Work NB test handoff automation GUI

**Deletion Impact**: Breaks automated development workflow!

## Android APP 開發規範

### 語言規範

- **程式碼內容**: 使用英文撰寫（類別名稱、變數名稱、註解、日誌訊息）
- **Claude Code Chat 對話**: 使用繁體中文溝通
- **主要開發語言**: Kotlin

### 版本控制規則

#### 版本號格式: `主版本.次版本.修訂號 (Build號)`

- **主版本號(+1.0.0)**: 重大功能變更或架構重構
- **次版本號(+0.1.0)**: 新增功能或顯著改進
- **修訂號(+0.0.1)**: Bug 修復或小幅調整
- **Build號**: 每次編譯自動遞增

### 命名規範

#### 類別命名

- 使用 `PascalCase`
- Activity: `XxxActivity` / Fragment: `XxxFragment` / ViewModel: `XxxViewModel`

#### 變數與函數命名

- 變數使用 `camelCase`，常數使用 `UPPER_SNAKE_CASE`
- 布林值以 `is`, `has`, `should` 開頭
- 函數名稱使用動詞開頭
- 變數名稱**至少 3 個字元**

### Git Commit 規範

```text
[類型] 簡短說明

詳細變更內容:
- 變更項目 1
- 變更項目 2
```

類型：`[Feature]` / `[Fix]` / `[Refactor]` / `[UI]` / `[Perf]` / `[Docs]` / `[Test]` / `[Build]`

### 程式碼品質規則

- 每個類別不超過 300 行
- 每個函數不超過 50 行
- 避免硬編碼字串（使用 `strings.xml`）
- 避免硬編碼顏色（使用 `colors.xml` 或 Compose Theme）
- 使用依賴注入（Hilt）
- 遵循 MVVM 架構模式

### 對話記錄管理

- **開發功能對話**: `chat_[功能簡稱]_YYYYMMDD_HHMMSS.md`
- **版本更新記錄**: `YY-MM-DD_CHANGELOG_v[版本號].md`

## 多 AI 分工策略（gstack v1.5）

| AI | 角色 | 配額策略 |
| ---- | ------ | --------- |
| **Gemini Advanced** | 架構師 / 防波堤 | 配額最寬鬆：報錯、Log 分析、千行 Code 追蹤全丟 Gemini |
| **Claude Pro (Opus/Sonnet)** | CSO + 苦力實作 | 嚴格限制 Context：只餵當前檔案 + CLAUDE.md |
| **Traycer** | 最終法官 | 每日限 2-4 次：架構拆票 + 終審蓋章 |
| **ChatGPT Plus** | 降級選用 | 純數學/演算法問題，拿完 Function 就走 |

---

## ⚙️ KAIROS 產出的架構守則 (Auto-populated)

> 以下規則由 `/dream` 自動回顧產出，經確認後追加。

### KAIROS #1 產出 (D2, 2026-04-10)

- **Rule 11 — 骨架 Ticket 未實作強制聲明**：D1/骨架 Ticket 中的未實作函數，必須以 `error("Not implemented — will be wired in DX-TY")` 顯式拋出，嚴禁回傳合法型別空值（如 `null`、`emptyList()`）偽裝完成。觸發來源：SV-D2-T4-FIX-01（D1 `observeCompositionAdvice` 暫代殘留導致 D2 需補接線）。

- **Rule 12 — Rule 負向測試強制覆蓋**：每個 `CompositionRule` 實作的測試套件，必須包含 ≥1 組負向測試（空 faces 清單 + 座標超界輸入 `[0,1]` 以外），驗證結果為 `score=0` / `isAligned=false`（非拋出例外）。觸發來源：SV-D2-T1-FIX-01~03（ThirdsRule 座標漂移 bug，修復需三張連環補修票）。

---

### KAIROS #2 產出 (D6, 2026-04-15)

- **Rule 13 — Android SDK 型別 JVM 測試環境規則**：使用 `android.graphics.PointF`/`RectF` 的類別，其 JVM 單元測試必須加上 `@RunWith(RobolectricTestRunner::class)` + `@Config(sdk = [28])`（與 `EyeLineRuleTest` 一致），或將欄位型別改為純 Kotlin data class 以徹底消除 Android stub 依賴。理由：stub 環境下 `PointF`/`RectF` public fields 全回傳 `0`，導致座標計算測試假過或假失敗，且難以察覺。觸發來源：D6-B2-T1（`CoordinateTransformer` stub 問題）、D6-B3（`EyeAfControllerTest` 7 條測試失敗）。

### KAIROS #3 產出 (FIX-CROSSREVIEW-01, 2026-05-10)

- **Rule 17 — Cross-Review Model Escalation**：`cross_review.sh` 預設組合為 gpt-5.5 + high + summary auto。

    | 條件 | 覆蓋值 |
    | ---- | ------ |
    | diff 跨 domain + data + presentation 三層 | `CODEX_REASONING_EFFORT=xhigh` |
    | diff 涉及 `CameraViewModel.kt` | `CODEX_REASONING_EFFORT=xhigh` |
    | 純 domain 新建檔案 | 維持 high |
    | 快速 smoke check | `CODEX_MODEL=gpt-5.4-mini CODEX_REASONING_EFFORT=low` |

- **Rule 18 — Edit 操作前強制 Re-view 規則**：對任何超過 200 行的 `.kt` / `.xml` 目標檔案，執行 `Edit`（str_replace）前必須先用 `Read` 工具讀取操作區段（含 ±10 行上下文）。`/compact` 後視為強制觸發條件（無論檔案大小）。根因：顯式知識（Read 結果）比 session 記憶可靠，避免 attention degradation 導致 str_replace 精確度下降。觸發來源：K3-7 KAIROS #3 Session Boundary Rule 修訂（2026-05-10）。

**完整規範請參考**: `devspec/.clinerules`
**SSOT 策略文件**: `plans/architecture/SSOT_v4_0.md`
**SSOT 技術文件**: `plans/architecture/Cueshot_Layout_Spec_v2_2.md`

## Path-Scoped Rules 索引

- `.claude/rules/phase0-lessons.md` — Phase 0 盲區規則 (Rule 19-23)
- `.claude/rules/camera-constraints.md` — 相機硬體約束
- `.claude/rules/compose-ui.md` — Compose UI + Ambient UI 規則
- `.claude/rules/ptf-rules.md` — PTF 場景規則

（以上規則由 Claude Code 根據 path matching 自動按需載入，無需手動引用）
