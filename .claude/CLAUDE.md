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

```
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
|---------|------|------|
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
|------|---------|----------|------|
| `/sv` | `.claude/skills/sv/SKILL.md` | **人工觸發** | 無情法官：殘骸掃描 + 編譯 + Lint + 邊界檢查 + UDF 合規 + SSOT 漂移 |
| `/qa` | `.claude/skills/qa/SKILL.md` | **人工 or 自動** | 自動化 QA：Build + Unit Test + Warning 清理 + PROGRESS.md 更新 |
| `/uc` | `.claude/skills/uc/SKILL.md` | **人工觸發** | 臥底模式：零廢話純 Diff 輸出，省 Token 省配額 |
| `/normal` | `.claude/skills/normal/SKILL.md` | **人工觸發** | 退出臥底模式，恢復正常對話 |
| `/dream` | `.claude/skills/dream/SKILL.md` | **Sprint 節點觸發** | KAIROS 回顧：錯誤模式提煉 + 決策審計 + SSOT 漂移掃描 |
| `/compact` | `.claude/skills/compact/SKILL.md` | **對話過長時** | Context 壓縮：產出精簡交接文件，準備開新 Session |

**標準使用順序**（每次實作完成後）：`/qa` → `/sv` → Traycer `strict_verification`

---

# 🛡️ 防禦型工作流鐵律 (Global Rules — AI Agent Behavioral Contract)

> **適用對象**: Claude Code、Cursor、Traycer 等所有 AI 代理。
> 優先順序：本節 < 最高憲法（最高憲法仍最優先）。

## 鐵律 1 — SSOT 優先 (Single Source of Truth)

- **策略 SSOT**: `plans/architecture/SSOT_v4_0.md`
- **技術 SSOT**: `plans/architecture/Cueshot_Layout_Spec_v2_2.md`
- **進度 SSOT**: `PROGRESS.md`

在修改任何核心邏輯前，必須優先查閱上述文件。文件是架構的唯一真理，程式碼的解釋以文件為準。

## 鐵律 2 — 原子化提交 (Atomic Commits)

嚴禁在同一個 Ticket 或 Commit 中混合修改「原始碼（`.kt`、`.java`、`.xml`）」與「專案文件/Log（`.md`、`.log`）」。

```
✅ Commit 1: [Fix]  程式碼修復（.kt）
✅ Commit 2: [Docs] 文件與 Log 歸檔（.md / .log）
❌ 禁止: 一個 Commit 同時含兩類檔案
```

## 鐵律 3 — Android 邊界防禦 (Boundary Defense)

處理 Coroutine、CameraX 管線或 Sensor 邏輯時，必須主動考慮 Edge Cases：

- 無效幾何尺寸（`<= 0`）→ `return null`，絕不回傳 `Matrix()` 偽裝者
- Flow / Coroutine 加 `.catch { }` 或 `try-catch` 並印出 `Log.e`
- 心跳 Log（每 N 幀）確認管線存活

## 鐵律 4 — 禁止越權 (No Scope Drift)

嚴格遵守 Ticket / Prompt 指定的修改範圍，絕對禁止「順手」修改未授權的檔案或重構無關邏輯。

- 發現超出授權範圍：立即暫停並回報，等待指令
- `/sv` 偵測到越權修改（Git diff 比對）：強制退回

### 禁區檔案 (No-Go Zones)

除非 `/dream` 審計明確要求，以下檔案不可在日常 Ticket 中修改：
- `CLAUDE.md`（本檔案）
- `PROGRESS.md`（僅 `/qa` 和 `/dream` 可程式化更新）
- 已封板的 SSOT 文件

---

# Cueshot — AI Coaching Camera (MVP)

## Product Overview

**App 名稱**: Cueshot
**定位**: AI 離線導拍引擎 — 不只告訴你怎麼拍對，還懂得什麼時候不該干預。
**開發者**: Akira（獨立開發者，台灣）

### Key Technologies

- **Language**: Kotlin (Primary)
- **Architecture**: Clean Architecture (domain/data/presentation) + MVVM
- **UI**: Jetpack Compose (單一 Activity)
- **Camera**: CameraX 1.4+
- **AI/ML**: ML Kit Face Detection + Object Detection（互斥，不可同時開啟）
- **Sensor**: IMU (gyroscope) + ImuCompensator
- **Image Processing**: ML Kit Selfie Segmentation + StackBlur (Phase 1.5 DOF)
- **DI**: Hilt
- **Testing**: JUnit, Mockito

### Scene Modes (4 modes, no LowAngle)

```
PTF (user_selected) → PassToFriendMode [最高優先]
pitch > 35°         → FoodMode [IMU 自動]
pitch ∈ [-10°, 35°] AND face ≥ 1 → PortraitMode [ML Kit]
pitch ∈ [-10°, 35°] AND face == 0 → GeneralMode [fallback]
```

ML Kit Face Detection 與 Object Detection 互斥，切換需 500ms hysteresis。

### Ambient UI 色彩語言

| 顏色 | 用途 |
|------|------|
| `#D4A030` 琥珀金 | Pitch Traction 引導中、Halo 未對齊、Normal 模式 |
| `#50C864` 綠色 | 對齊完成、PTF 模式、呼吸韻律 |
| 所有 Halo 統一圓形 | 手動對焦 / 構圖引導 / PTF / 減法留白確認 |

### North Star Metric

**IFTR (Instruction Follow-Through Rate) > 70%** — 引導後 3 秒內手機姿態朝正確方向改變 ≥ 3°。

---

## ⚠️ Critical Project Files - DO NOT DELETE

### Workflow Automation Tools
- **`tools/hybrid_flow.py`** 🔴 CRITICAL — L1/L2/PR planning document generation
- **`tools/hybrid_flow-tasks.json`** 🔴 CRITICAL — VSCode Tasks configuration reference
- **`.vscode/tasks.json`** 🔴 CRITICAL — Active VSCode Tasks configuration
- **`tools/test_handoff_gui.pyw`** 🔴 CRITICAL — Home NB ↔ Work NB test handoff automation GUI

**Deletion Impact**: Breaks automated development workflow!

---

## ⚠️ ADB 使用指南

**❌ 絕對不要使用**：
```bash
adb shell screencap -p > file.png          # 會造成 Claude Code Chat API 崩潰
adb shell screencap -p | <任何重導向操作>
```

**✅ 正確截圖方法**（先存手機再 pull）：
```bash
adb -s DEVICE_ID shell "screencap -p > /sdcard/Download/temp_screen.png"
adb -s DEVICE_ID pull //sdcard//Download//temp_screen.png tmp/screenshot.png
adb -s DEVICE_ID shell "rm /sdcard/Download/temp_screen.png"
```

---

## ⚡ 效能預算與併發防禦 (Performance & Concurrency Strategy)

### 三層非同步管道 (Three-Tier Async Pipeline)

* **偵測層 (ML Pipe)**：物體偵測與語義解析 (10fps)，強制綁定 `Dispatchers.Default`。
* **補償層 (IMU Pipe)**：陀螺儀融合 (100-200 Hz)，座標預測插值。
* **渲染層 (UI Pipe)**：Compose 繪製 (60fps)，僅接收預測後的輕量化狀態。

### 三級裝置降級策略

| 等級 | 核心引導 | 輔助功能 | 關閉功能 |
|------|---------|---------|---------|
| FULL | Pitch Traction 60fps + Halo 10fps + 所有規則 | 鏡頭髒汙 2fps + 雜亂框 + 減法留白 | 無 |
| LIMITED | Pitch Traction 60fps + Halo 8fps + 核心 4 規則 | 鏡頭髒汙 1fps + 減法留白 | 雜亂框 |
| LEGACY | Pitch Traction 60fps + Halo 5fps + 2 規則 | 無 | 鏡頭髒汙 + 減法留白 + 雜亂框 + 光線 |

**核心保護**：無論哪個等級，Pitch Traction (IMU 驅動) 永遠 60fps。

### 分幀計算

```
Frame 1: ML Kit + Rules + Priority → 65ms
Frame 2: Histogram + Minimalist + Clutter → 13ms
Frame 5: Lens Smudge (每 5 幀) → 12ms
```

端到端峰值：< 100ms（實際 ~65ms）。記憶體：< 150MB。電池增耗：< 15%。

---

# Android APP 開發規範

## 語言規範

- **程式碼內容**: 使用英文撰寫（類別名稱、變數名稱、註解、日誌訊息）
- **Claude Code Chat 對話**: 使用繁體中文溝通
- **主要開發語言**: Kotlin

## 版本控制規則

### 版本號格式: `主版本.次版本.修訂號 (Build號)`
- **主版本號(+1.0.0)**: 重大功能變更或架構重構
- **次版本號(+0.1.0)**: 新增功能或顯著改進
- **修訂號(+0.0.1)**: Bug 修復或小幅調整
- **Build號**: 每次編譯自動遞增

## 命名規範

### 類別命名
- 使用 `PascalCase`
- Activity: `XxxActivity` / Fragment: `XxxFragment` / ViewModel: `XxxViewModel`

### 變數與函數命名
- 變數使用 `camelCase`，常數使用 `UPPER_SNAKE_CASE`
- 布林值以 `is`, `has`, `should` 開頭
- 函數名稱使用動詞開頭
- 變數名稱**至少 3 個字元**

## Git Commit 規範

```
[類型] 簡短說明

詳細變更內容:
- 變更項目 1
- 變更項目 2
```

類型：`[Feature]` / `[Fix]` / `[Refactor]` / `[UI]` / `[Perf]` / `[Docs]` / `[Test]` / `[Build]`

## 程式碼品質規則

- 每個類別不超過 300 行
- 每個函數不超過 50 行
- 避免硬編碼字串（使用 `strings.xml`）
- 避免硬編碼顏色（使用 `colors.xml` 或 Compose Theme）
- 使用依賴注入（Hilt）
- 遵循 MVVM 架構模式

## 對話記錄管理

- **開發功能對話**: `chat_[功能簡稱]_YYYYMMDD_HHMMSS.md`
- **版本更新記錄**: `YY-MM-DD_CHANGELOG_v[版本號].md`

---

# 實機測試日誌格式

```markdown
# device_test_YYYYMMDD.md

## 測試裝置
- 型號: [e.g. Pixel 7a]
- Android: [version]
- 效能等級: [FULL/LIMITED/LEGACY]

## 測試項目
| 功能 | 預期行為 | 實際行為 | Pass/Fail | 備註 |
|------|---------|---------|-----------|------|
| Pitch Traction 60fps | 邊緣引導光流暢 | | | |
| Pitch >35° 切 FoodMode | 自動切換 <500ms | | | |
| Face Detection Halo | 圓形 Halo 跟蹤人臉 | | | |
| PTF Tab toggle | 300ms 切換，零 CameraX rebind | | | |
| 快門冷卻 | PTF 進入後 1s 內不可按 | | | |

## 發現的問題
| Issue | Severity | Reproduce Steps | Workaround |
|-------|----------|-----------------|------------|
```

---

# 多 AI 分工策略（gstack v1.5）

| AI | 角色 | 配額策略 |
|----|------|---------|
| **Gemini Advanced** | 架構師 / 防波堤 | 配額最寬鬆：報錯、Log 分析、千行 Code 追蹤全丟 Gemini |
| **Claude Pro (Opus/Sonnet)** | CSO + 苦力實作 | 嚴格限制 Context：只餵當前檔案 + CLAUDE.md |
| **Traycer** | 最終法官 | 每日限 2-4 次：架構拆票 + 終審蓋章 |
| **ChatGPT Plus** | 降級選用 | 純數學/演算法問題，拿完 Function 就走 |

---

## ⚙️ KAIROS 產出的架構守則 (Auto-populated)

> 以下規則由 `/dream` 自動回顧產出，經確認後追加。

**KAIROS #1 產出 (D2, 2026-04-10)**

11. **骨架 Ticket 未實作強制聲明**：D1/骨架 Ticket 中的未實作函數，必須以 `error("Not implemented — will be wired in DX-TY")` 顯式拋出，嚴禁回傳合法型別空值（如 `null`、`emptyList()`）偽裝完成。觸發來源：SV-D2-T4-FIX-01（D1 `observeCompositionAdvice` 暫代殘留導致 D2 需補接線）。

12. **Rule 負向測試強制覆蓋**：每個 `CompositionRule` 實作的測試套件，必須包含 ≥1 組負向測試（空 faces 清單 + 座標超界輸入 `[0,1]` 以外），驗證結果為 `score=0` / `isAligned=false`（非拋出例外）。觸發來源：SV-D2-T1-FIX-01~03（ThirdsRule 座標漂移 bug，修復需三張連環補修票）。

---

**KAIROS #2 產出 (D6, 2026-04-15)**

13. **Android SDK 型別 JVM 測試環境規則**：使用 `android.graphics.PointF`/`RectF` 的類別，其 JVM 單元測試必須加上 `@RunWith(RobolectricTestRunner::class)` + `@Config(sdk = [28])`（與 `EyeLineRuleTest` 一致），或將欄位型別改為純 Kotlin data class 以徹底消除 Android stub 依賴。理由：stub 環境下 `PointF`/`RectF` public fields 全回傳 `0`，導致座標計算測試假過或假失敗，且難以察覺。觸發來源：D6-B2-T1（`CoordinateTransformer` stub 問題）、D6-B3（`EyeAfControllerTest` 7 條測試失敗）。

14. **Composable 狀態記憶置頂規則**：所有 Composable 函數中的 `remember`/`LaunchedEffect`/`rememberCoroutineScope` 呼叫，必須置於函數體的最頂部（任何 `if`/`when`/`return` 語句之前），無論哪些路徑邏輯上需要使用它們。違反此規則會導致 Compose Slot-Table 不一致，在條件狀態切換時觸發 IllegalStateException 或首幀遺失（視 Compose 版本而定）。觸發來源：D6-B2-T2.1（`HaloGuidanceLayer` `LaunchedEffect` 首次錯置於 `isAllAligned` early return之後，Traycer 要求修復）。

15. **防護機制 KDoc 引用真實呼叫鏈規則**：當 KDoc 描述多道防線（double-gating）或安全機制時，必須引用**實際參與呼叫鏈的具體類別名稱與方法**（如 `LiveCameraSource.focusOnPoint()` 而非泛稱 `the framework`），並標注觸發判斷點（如「as detected by `FixedFocusDetector` at camera bind time」）。禁止引用未在此呼叫路徑上的類別。觸發來源：D6-B3 KDoc 校準（`EyeAfController` 第二道防線誤引用 `CameraXFocusController`，實際路徑為 `LiveCameraSource`）。

16. **硬體呼叫雙重門控規則**：任何觸發硬體資源（對焦、閃光、音效）的 Controller 或 UseCase，必須實作**兩道獨立 Guard**：①鏡頭方向 Guard（前鏡頭永遠 early-return，不分情境）；②節流 Guard（時間戳 cooldown，初始哨兵值必須為 `Long.MIN_VALUE / 2` 而非 `0L`，確保首次呼叫必定通過）。兩道 Guard 缺一不可，且必須有對應的負向單元測試（前鏡頭抑制 + 節流攔截各 ≥1 條）。觸發來源：D6-B3 `EyeAfController` 設計審計；哨兵值 bug（`0L` 導致首次呼叫被誤攔截）。

---

**KAIROS #3 產出 (FIX-CROSSREVIEW-01, 2026-05-10)**

17. **Cross-Review Model Escalation**：`cross_review.sh` 預設組合為 gpt-5.5 + high + summary auto。

    | 條件 | 覆蓋值 |
    | ---- | ------ |
    | diff 跨 domain + data + presentation 三層 | `CODEX_REASONING_EFFORT=xhigh` |
    | diff 涉及 `CameraViewModel.kt` | `CODEX_REASONING_EFFORT=xhigh` |
    | 純 domain 新建檔案 | 維持 high |
    | 快速 smoke check | `CODEX_MODEL=gpt-5.4-mini CODEX_REASONING_EFFORT=low` |

18. **Edit 操作前強制 Re-view 規則**：對任何超過 200 行的 `.kt` / `.xml` 目標檔案，執行 `Edit`（str_replace）前必須先用 `Read` 工具讀取操作區段（含 ±10 行上下文）。`/compact` 後視為強制觸發條件（無論檔案大小）。根因：顯式知識（Read 結果）比 session 記憶可靠，避免 attention degradation 導致 str_replace 精確度下降。觸發來源：K3-7 KAIROS #3 Session Boundary Rule 修訂（2026-05-10）。

---

**KAIROS #4 產出 (FIX-P0-SESSION-A, 2026-05-13)**

19. **Decommission 全鏈路紀律**：任何 decommission / debug-overlay 類票據在關閉前，必須同時清空 4 層殘留：①UI Composable 呼叫點、②Settings 開關 UI、③持久化 key（DataStore/SharedPreferences）、④ViewModel/Repository 暴露面。只停用呼叫點而不清除其餘三層，不算完成；`/sv` 必須對這 4 層逐一 grep 驗證。觸發來源：FIX-P0-SESSION-A（DetectionOverlay 殘留 4 層，Traycer 退回）。

---

### KAIROS #5 候選規則 (FIX-P0-SESSION-A-FINAL-SYNC, 2026-05-13)

[CANDIDATE RULE 20] **PROGRESS.md 三處狀態一致性規則**：PROGRESS.md 對同一驗收 scope 不得同時出現 "SV done" 與 "待 /sv"；Header / Sprint Health / Next Actions 必須三者一致。任何 Ticket 完成後，必須同步更新三個區塊，禁止留下互相矛盾的狀態描述。觸發來源：FIX-P0-SESSION-A-FINAL-SYNC（Traycer 指出 Header 標 DONE 但 Sprint Health 仍標「待 /sv」）。

---

**完整規範請參考**: `devspec/.clinerules`
**SSOT 策略文件**: `plans/architecture/SSOT_v4_0.md`
**SSOT 技術文件**: `plans/architecture/Cueshot_Layout_Spec_v2_2.md`
