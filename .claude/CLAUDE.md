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
  - Purpose: VSCode Tasks configuration
  - Used by: VSCode Task Runner (Ctrl+Shift+P → "Run Task")

**Deletion Impact**: Breaks automated development workflow!
**Protection Details**: See `tools/.gitkeep_critical_files`

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
