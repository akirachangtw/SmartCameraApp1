# SmartCameraApp1 - AI Photographer Assistant

## Project Overview

This is an Android application that uses AI to assist with photography composition and settings.

### Key Technologies

- **Language**: Kotlin (Primary), Java (Legacy)
- **Architecture**: MVVM with Jetpack Compose
- **Camera**: CameraX API
- **AI/ML**: ML Kit, TensorFlow Lite
- **Dependency Injection**: Hilt
- **Testing**: JUnit, Mockito, Espresso

### Current Branch

Feature branch: `feature/camera-input-abstraction`

---

## ⚠️ Critical Project Files - DO NOT DELETE

### Workflow Automation Tools
- **`tools/hybrid_flow.py`** 🔴 CRITICAL
  - Purpose: L1/L2/PR planning document generation
  - Integration: Claude Code (`.claude/settings.local.json`), VSCode Tasks
  - Documentation: `tools/HYBRID_FLOW_CLAUDE_USAGE.md`

- **`tools/hybrid_flow-tasks.json`** 🔴 CRITICAL
  - Purpose: VSCode Tasks configuration reference
  - Used by: VSCode Task Runner (Ctrl+Shift+P → "Run Task")

- **`.vscode/tasks.json`** 🔴 CRITICAL
  - Purpose: Active VSCode Tasks configuration
  - Protected Tasks: 5 Hybrid Tasks + 1 Test Handoff Task
  - Protected Inputs: 4 Hybrid Input parameters
  - Backup: Forced into Git with `git add -f` (normally in .gitignore)
  - Reference: `tools/hybrid_flow-tasks.json`
  - Protection Guide: `.vscode/TASKS_PROTECTION.md`
  - **⚠️ DO NOT DELETE**: Hybrid Tasks (L1/L2/PR) or Test Handoff Manager Task

- **`tools/test_handoff_gui.pyw`** 🔴 CRITICAL
  - Purpose: Home NB ↔ Work NB test handoff automation GUI
  - Documentation: `docs/guides/PYTHON_GUI_GUIDE.md`

**Deletion Impact**: Breaks automated development workflow!
**Protection Details**: See `tools/.gitkeep_critical_files` and `.vscode/TASKS_PROTECTION.md`

---

## 📂 Project Directory Structure

### Critical Directories (Versioned)
- **`app/`** - Android application source code
- **`tools/`** - Development automation scripts
- **`docs/`** - Project documentation
- **`.claude/`** - Claude Code configuration

### Temporary Directories (Not Versioned)
- **`tmp/`** 📁 Temporary files directory
  - Purpose: Test outputs, logcat dumps, debug sessions, downloads
  - Status: Fully ignored by git (`.gitignore`)
  - Safe to delete anytime
  - Documentation: `tmp/README.md`

- **`output/`** - Test session archives
- **`others/`** - Historical testing data (some versions ignored)

### Storage Guidelines
| What | Where | Versioned |
|------|-------|-----------|
| **Work NB 測試產出** | `docs/test_results/<version>/` | ✅ **Yes** |
| Test outputs (temp) | `tmp/test_outputs/` | ❌ No |
| Logcat dumps (temp) | `tmp/logcat_dumps/` | ❌ No |
| Debug screenshots (temp) | `tmp/screenshots_temp/` | ❌ No |
| Historical test data | `others/vX.X.X-Testing/` | ❌ No (ignored) |
| Test images (committed) | `app/src/debug/assets/test_images/` | ✅ Yes |
| Documentation | `docs/` | ✅ Yes |
| Tools/Scripts | `tools/` | ✅ Yes |

> ⚠️ **重要**: Work NB 測試產出（測試報告、照片、Logcat）必須存放至 `docs/test_results/<version>/` 目錄，才能被 Git 追蹤並提交。`tmp/` 目錄已被 `.gitignore` 忽略。

### 📖 目錄結構規範

**完整規範文檔**: [`devspec/DIRECTORY_STRUCTURE.md`](devspec/DIRECTORY_STRUCTURE.md)

**快速參考** - 文檔應存放在哪裡？

| 文檔類型 | 存放位置 | 範例 |
|---------|---------|------|
| 整體專案計畫/架構設計 | `plans/` | 產品路線圖、架構藍圖 |
| 功能 L1/L2/Traycer 計劃 | `docs/features/<版本>/` | v2.10.0_aspect_ratio_L1.md |
| 版本發布記錄 | `docs/releases/<版本>/` | RELEASE_NOTES_v2.8.0.md |
| 測試報告 | `docs/test_results/<版本>/` | PIXEL9A_TEST_REPORT.md |
| Bug 追蹤/Issue 分析 | `docs/issues/` | front_camera_mlkit_mirror.md |
| 技術分析報告 | `docs/reports/` | 16kb-compatibility-report.md |
| Git 管理文檔 | `docs/git/` | GIT_ANALYSIS_GUIDE.md |
| 操作指南/工具使用 | `docs/guides/` | MANUAL_TEST_HANDOFF.md |
| 開發規範/流程/模板 | `devspec/` | MERGE_SOP.md, BRANCH_RELEASES.md |

**⚠️ 重要規則**:
- L1/L2 計劃必須存放在 `docs/features/<版本>/`，**不要**放在 `plans/`
- 測試報告必須存放在 `docs/test_results/<版本>/`，**不要**放在 `docs/features/`
- 撰寫新文檔前，請查閱 `devspec/DIRECTORY_STRUCTURE.md` 的「快速決策樹」

---

## ⚠️ ADB 使用指南 - 重要注意事項

### 截圖指令 - 避免 Claude Code API Crash

**❌ 絕對不要使用以下指令**：
```bash
# 這些指令會造成 Claude Code Chat API 異常崩潰
adb shell screencap -p > file.png
adb shell screencap -p | <任何重導向操作>
```

**✅ 正確的截圖方法**（先存手機再 pull）：
```bash
# Step 1: 在手機上截圖並存檔
adb -s DEVICE_ID shell "screencap -p > /sdcard/Download/temp_screen.png"

# Step 2: 拉取圖片到電腦
adb -s DEVICE_ID pull //sdcard//Download//temp_screen.png tmp/screenshot.png

# Step 3: 刪除手機暫存檔
adb -s DEVICE_ID shell "rm /sdcard/Download/temp_screen.png"
```

**問題原因**：
1. **換行符號轉換**：`adb shell` 會將 Unix (`0x0A`) 轉換為 Windows (`0x0D 0x0A`)，破壞 PNG 二進位結構
2. **PowerShell 編碼**：`>` 重導向會嘗試以文字編碼處理二進位資料（如 UTF-16），導致完全損壞
3. **Claude Code API**：損壞的圖片資料會造成 API 請求異常

**詳細說明**：參考 [`plans/adb_screencap_guide.md`](plans/adb_screencap_guide.md)

---

## ⚠️ Git Merge 規範 - 分支合併前必讀

### 🔴 強制性檢查（合併前必須執行）

**IMPORTANT**: 在執行任何 `git merge` 操作前，**必須**先執行以下檢查：

```bash
# Step 1: 執行智能檢查工具（必須）
python tools/check_merge_readiness.py

# 預期輸出: ✅ READY TO MERGE
# 如果輸出 ⚠️  NOT READY，必須先修正問題再合併
```

**檢查內容**:
- ✅ 版本號偵測成功
- ✅ `docs/features/v<版本>/` 存在功能文檔
- ✅ `docs/test_results/v<版本>/` 存在測試報告
- ✅ `devspec/BRANCH_RELEASES.md` 包含當前版本
- ✅ `devspec/feature_record_Backlog.md` 已更新

**如果檢查失敗**:
1. 執行 `devspec/MERGE_SOP.md` Phase 2 文檔同步
2. 修正所有警告項目
3. 重新執行檢查，直到輸出 `✅ READY TO MERGE`

**快速參考**: `devspec/MERGE_SOP_QUICK_REFERENCE.md`

---

### 合併前檢查清單

**❌ 絕對不要使用以下方式合併**：
```bash
# 這些指令可能合併錯誤的版本（本地分支可能過時）
git merge feature/branch-name          # ❌ 使用本地分支
git merge feature-branch               # ❌ 使用本地分支
```

**✅ 正確的合併方法**（使用遠端分支或 tag）：
```bash
# 合併前必須執行（更新遠端信息）
git fetch origin --tags

# 合併前必須執行（智能檢查）
python tools/check_merge_readiness.py

# 方法 1: 使用遠端分支（推薦）
git merge origin/feature/branch-name   # ✅ 使用遠端最新版本

# 方法 2: 使用 tag（最終修正版）
git merge v2.10.2-final                # ✅ 使用已標記的穩定版本
```

### 必讀文檔

**合併前必須檢查**：
1. **`tools/check_merge_readiness.py`** 🔴 強制執行
   - 自動檢查文檔同步狀態
   - 驗證 MERGE_SOP Phase 2 是否完成
   - 輸出必須為 `✅ READY TO MERGE`

2. **`devspec/MERGE_SOP_QUICK_REFERENCE.md`** - 快速參考（1-2 頁）
   - ⚡ 5 步驟快速流程
   - 🔍 執行前檢查清單
   - ⚠️ 常見錯誤與預防

3. **`devspec/BRANCH_RELEASES.md`** - 查看分支版本記錄
   - 確認使用 `-final` tag（最終修正版）
   - 檢查「包含功能」和「已知問題」
   - 確認分支狀態為「✅ 穩定，可安全合併」

4. **`devspec/MERGE_SOP.md`** - 完整標準作業程序
   - Phase 1: 準備階段（確認來源分支、閱讀決議文檔）
   - Phase 2: 文檔同步（devspec/, docs/features/, docs/test_results/）
   - Phase 3: 代碼合併（解決衝突、保留關鍵修復）
   - Phase 4: 驗證（Build + 16KB + 實機測試）
   - Phase 5: 記錄（更新 BRANCH_RELEASES.md）

5. **`devspec/DOCS_SYNC_CHECKLIST.md`** - 文檔同步檢查清單
   - 同步決議文檔（devspec/feature_record_Backlog.md）
   - 同步計劃文檔（docs/features/）
   - 同步測試報告（docs/test_results/）

### 常見錯誤與預防

**錯誤案例**：合併了本地分支而非遠端分支
- **問題**: 本地分支 `feature/aspect-ratio-usecasegroup-v2.10.0` 停留在 commit `0440242`
- **缺少**: v2.10.2 的關鍵修復（commit `6501ac2`）
- **影響**: 重新引入已修復的 Bug（Permission UX、Aspect Ratio、Toast Regression）
- **解決**: 使用 `git merge origin/feature/aspect-ratio-usecasegroup-v2.10.0` 或 `git merge v2.10.2-final`

**預防措施**：
1. 合併前執行 `git fetch origin --tags`
2. 使用 `git log origin/branch-name --oneline -10` 確認遠端分支包含所有關鍵提交
3. 查看 `devspec/BRANCH_RELEASES.md` 確認使用正確的 tag
4. 閱讀 `devspec/feature_record_Backlog.md` 的 Critical Warnings

**關鍵提醒**：
> 每次合併都可能影響過去已完成的功能。務必遵循完整的 MERGE_SOP 流程，確保不會遺失任何修復或功能。

**詳細說明**：參考 [`devspec/MERGE_SOP.md`](devspec/MERGE_SOP.md)

---

# Android APP 開發規範

## 語言規範

- **程式碼內容**: 使用英文撰寫(類別名稱、變數名稱、註解、日誌訊息等)
- **Claude Code Chat對話**: 使用繁體中文進行溝通
- **主要開發語言**: Kotlin(優先)或 Java

## 版本控制規則

### 版本號格式: `主版本.次版本.修訂號 (Build號)`

- **主版本號(+1.0.0)**: 重大功能變更或架構重構時遞增
- **次版本號(+0.1.0)**: 新增功能或顯著改進時遞增
- **修訂號(+0.0.1)**: Bug修復或小幅調整時遞增
- **Build號**: 每次編譯自動遞增(用於內部測試)

## 命名規範

### 類別命名

- 使用 `PascalCase`(大駝峰命名法)
- Activity: 以 `Activity` 結尾
- Fragment: 以 `Fragment` 結尾
- ViewModel: 以 `ViewModel` 結尾
- Adapter: 以 `Adapter` 結尾

### 變數與函數命名

- 變數使用 `camelCase`(小駝峰命名法)
- 常數使用 `UPPER_SNAKE_CASE`
- 布林值建議以 `is`, `has`, `should` 開頭
- 函數名稱使用動詞開頭
- 變數名稱**至少3個字元**

## Git Commit 規範

### Commit 類型

- **[Feature]**: 新增功能
- **[Fix]**: Bug 修復
- **[Refactor]**: 代碼重構
- **[UI]**: UI/UX 調整
- **[Perf]**: 效能優化
- **[Docs]**: 文件更新
- **[Test]**: 測試相關
- **[Build]**: 建置相關

### Commit 訊息格式

```
[類型] 簡短說明

詳細變更內容:
- 變更項目 1
- 變更項目 2

相關 Issue: #123
```

## 混合工作流程 (Claude Code + ChatGPT)

### 三級評估機制

本專案採用分級風險評估機制,根據變更複雜度自動判斷所需流程深度:

#### Level 1: 規劃檢視 (Always-on)
**觸發條件** - 任一成立:
- 新功能構想 / Bug 修正討論
- 行為異常但尚未定位
- 架構、流程或資料流調整構想

**執行事項**:
- 彙整問題背景 (What / Why / Scope)
- 拆解變更影響範圍
- 列出潛在風險(至少 1-3 項)

#### Level 2: 可行性檢視 (Conditional)
**自動升級條件** - 任一成立:
- ⚠️ 涉及 CameraX / Preview / ImageAnalysis
- ⚠️ 涉及模擬器 vs 實體裝置差異
- ⚠️ 涉及 FPS / Memory / CPU / GPU
- ⚠️ 涉及模式切換 (Live / Static / Debug)
- ⚠️ 修改兩個以上檔案
- ⚠️ 非純 UI / 文案調整

**執行事項**:
- 評估 API、依賴與衝突
- 給出可行性結論 (可行 / 不可行 / 需調整)

#### Level 3: 實作與驗證 (Strict)
**啟動條件** - 需全部符合:
- Level 2 結論為可行
- 變更範圍明確、可最小 diff
- 回滾方案清楚
- 驗證方式已定義

**執行事項**:
- 小步提交 (atomic commits)
- Build / 測試 / 實機驗證
- 紀錄結果與差異

### 快速判斷
- 想改東西 → Level 1
- 碰相機 / 效能 / 模式 → Level 2
- 可行 + 有驗證 → Level 3

### 📋 模板與追蹤

**Issue 模板** (Level 1 規劃 → SSOT 單一真實來源):
- `devspec/issue_bug_camera_pipeline.md` - 相機/預覽/模式切換專用
- `devspec/issue_feature_composition.md` - 構圖/Overlay/提示文字專用

**PR 模板** (Level 3 實作關卡):
- `devspec/pull_request_template.md` - 確保實機驗證與回滾方案

**完整檢查清單**:
- `devspec/hybrid_workflow_checklist.md`

---

## 開發完成流程

### 功能開發完成後的檢查流程

1. **功能驗證**
   - 執行應用並測試新功能
   - 驗證所有相關功能點
   - 檢查性能和記憶體使用

2. **測試決策** ⭐ **重要**
   > **詢問用戶**: 「此功能開發已完成。是否要為此功能編寫單元測試?」

   **若選擇 YES**: 進行第3步(編寫單元測試)
   **若選擇 NO**: 記錄決策理由,跳至第4步

3. **編寫單元測試**(如選擇是)
   - 建立相應的 Test 類別
   - 編寫至少 3-5 個測試案例
   - 遵循 Given-When-Then 結構
   - 確保測試覆蓋率 > 80%
   - 執行測試確保通過

4. **記錄變更**
   - 儲存本次對話記錄到 chat_logs/
   - 更新 CHANGELOG.md
   - 編寫清晰的 Commit 訊息

5. **提交變更**
   - 執行 Git add
   - 執行 Git commit
   - 推送到遠端倉庫(如適用)

## 測試規範

### 單元測試規範

- 測試方法使用 backtick 命名(Kotlin)
- 格式: `` `fun `should do something when condition`()` ``
- 遵循 Given-When-Then 結構
- 測試名稱應清楚描述測試意圖

### 測試覆蓋率要求

- **目標覆蓋率**: > 80%(Core Business Logic)
- **最低覆蓋率**: > 60%(整體代碼)
- **優先順序**:
  1. ViewModel 和 UseCase(必須)
  2. Repository(推薦)
  3. Model 和 Util(可選)

---

## 跨機器測試工作流 (Home NB ↔ Work NB)

### ⭐ 標準方法：Test Handoff Task (Git 遠端同步)

**❌ 不使用 USB 隨身碟**
- 避免手動複製 APK 和文檔
- 避免版本不一致風險
- 避免大檔案傳輸問題

**✅ 使用 Test Handoff GUI**
- **工具**: `python tools/test_handoff_gui.pyw`
- **方法**: Git 遠端同步（GitHub/GitLab）
- **文檔**: `docs/guides/PYTHON_GUI_GUIDE.md`

### 工作流程

#### 1. Home NB 推送測試文檔
```bash
# 啟動 GUI
python tools/test_handoff_gui.pyw

# 步驟：
# - 填寫任務資訊（Topic, Device, Objective）
# - 切換到「1. 📤 Home (Push)」分頁
# - 點擊「🚀 執行推送 (Commit & Push)」
```

**推送內容**（通過 Git）:
- 測試計劃文檔（如 `PIXEL9A_TEST_GUIDE.md`）
- 模擬器測試報告（如 `EMULATOR_TEST_REPORT_FINAL.md`）
- 交接摘要（如 `HANDOVER_SUMMARY.md`）
- 版本號更新（`app/build.gradle.kts`）

**不推送內容**（在 `.gitignore`）:
- ❌ APK 檔案（應在目標機器重新編譯）
- ❌ `tmp/` 暫存目錄內容

#### 2. Work NB 拉取並執行測試
```bash
# 拉取測試文檔
git pull origin main

# 編譯 Device 版本 APK（不從 Home NB 傳輸）
./gradlew assembleDeviceDebug

# 安裝並測試
adb install app/build/outputs/apk/device/debug/app-device-debug.apk
```

#### 3. Work NB 推送測試結果
```bash
# 存放測試產出至 docs/test_results/<version>/
# - 測試報告（如 PIXEL9A_TEST_REPORT.md）
# - Logcat 日誌（*.log）
# - 截圖（*.png）
# - 測試照片（如有需要）

# 提交並推送
git add docs/test_results/<version>/
git commit -m "[Test] <version>: Pixel 9a 實機測試結果

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>"
git push
```

#### 4. Home NB 拉取測試結果
```bash
# 使用 Test Handoff GUI 或 Git pull
git pull origin main
```

### APK 編譯策略

**原則**: 在目標機器上編譯，不傳輸 APK

**理由**:
- APK 檔案通常很大（200-300 MB）
- Git 不適合大型二進位檔案
- 確保編譯環境一致性
- 避免 USB 隨身碟傳輸問題

**實作**:
```bash
# Home NB - 編譯模擬器版本
./gradlew assembleEmulatorDebug

# Work NB - 編譯實機版本
./gradlew assembleDeviceDebug
```

### 測試文檔結構範例

```
docs/test_results/<version>/
├── HANDOVER_SUMMARY.md          # 交接摘要（SSOT 單一真實來源）
├── EMULATOR_TEST_REPORT_FINAL.md  # 模擬器測試報告
├── PIXEL9A_TEST_GUIDE.md        # 實機測試指南
├── PIXEL9A_TEST_REPORT.md       # 實機測試報告（Work NB 產出）
├── *.log                        # Logcat 日誌
└── *.png                        # 截圖
```

### 重要提醒

1. **HANDOVER_SUMMARY.md 為單一真實來源（SSOT）**
   - 包含完整的交接資訊和工作流說明
   - 其他文檔應參考連結此檔案，避免重複內容

2. **測試產出必須存放至 `docs/test_results/<version>/`**
   - ✅ 可被 Git 追蹤並提交
   - ❌ `tmp/` 目錄已被 `.gitignore` 忽略

3. **使用 Test Handoff GUI 自動化流程**
   - 自動偵測 Git remote
   - 自動生成 commit 訊息
   - 4 步工作流（Home Push → Work Pull → Work Push → Home Pull）

**詳細說明**: 參考 `docs/guides/PYTHON_GUI_GUIDE.md`

---

## 程式碼品質規則

- 使用 Kotlin Coding Conventions
- 每個類別不超過 300 行
- 每個函數不超過 50 行
- 避免使用硬編碼字串(使用 strings.xml)
- 避免使用硬編碼顏色(使用 colors.xml)
- 使用依賴注入(推薦 Hilt)
- 遵循 MVVM 或 MVI 架構模式

## 對話記錄管理

### 檔案命名規範

1. **開發功能對話**: `chat_[功能簡稱]_YYYYMMDD_HHMMSS.md`
   - 例如: `chat_auth_20251117_143025.md`
   - 功能簡稱: 1-4字母,如 auth, ui, ml, api, db

2. **版本更新記錄**: `YY-MM-DD_CHANGELOG_v[版本號].md`
   - 例如: `25-11-17_CHANGELOG_v1.1.0.md`

3. **其他對話記錄**: `chat_log_YYYYMMDD_HHMMSS.txt`

---

## 快速參考

### 日常開發檢查清單

- [ ] 程式碼遵循命名規範
- [ ] 變數名稱至少 3 個字元
- [ ] 添加必要的註解
- [ ] 使用適當的 Log 級別
- [ ] 處理可能的異常
- [ ] 儲存對話記錄(如使用 Claude)
- [ ] 編寫 Commit 訊息
- [ ] Code Review(如團隊開發)

---

**完整規範請參考**: `devspec/.clinerules`
