# Python GUI 測試交接工具使用指南

**文檔版本**: 2.0
**建立日期**: 2026-01-18
**工具檔案**: `tools/test_handoff_gui.py`
**適用專案**: SmartCameraApp1 v2.9.6+

---

## 📋 概述

Python GUI 測試交接工具是一個圖形化介面，用於管理 Home NB ↔ Work NB 之間的測試交接流程。

### 為什麼使用 Python GUI？

與舊版 VSCode Tasks 方案相比：

| 特性 | VSCode Tasks (舊) | Python GUI (新) |
|-----|-----------------|----------------|
| PowerShell 引號轉義問題 | ❌ 難以維護 | ✅ 完全避免 |
| 錯誤處理 | ❌ 基本 | ✅ 完善且友好 |
| 配置管理 | ❌ 硬編碼 | ✅ 可配置檔案 |
| Git Remote 適配 | ❌ 固定 | ✅ 自動檢測 |
| 日誌管理 | ❌ 無時間戳記 | ✅ 時間戳記 + 可儲存 |
| 跨平台支援 | ❌ 僅 Windows | ✅ 跨平台 |

---

## 🚀 快速開始

### 啟動方式

#### 方式 1: VSCode Task (推薦)

1. 按下 `Ctrl+Shift+P`
2. 輸入 "Run Task"
3. 選擇 `🚀 Test Handoff Manager (Python GUI)`

#### 方式 2: 命令列

```bash
cd d:\MyDocuments\DevSources\AndroidStudioProjects\SmartCameraApp1
python tools/test_handoff_gui.py
```

#### 方式 3: Python 執行

```bash
python -m tools.test_handoff_gui
```

---

## 🎯 核心功能

### 主要分頁

1. **1. 📤 Home NB (Push)** - 開發機推送測試計畫
2. **2. 📥 Work NB (Pull)** - 測試機拉取測試計畫
3. **3. 📤 Work NB (Push)** - 測試機推送測試結果
4. **4. 📥 Home NB (Pull)** - 開發機查看測試報告
5. **⚙️ Settings** - 配置管理

---

## 📖 詳細使用指南

### Step 1: Home NB (Push) - 推送測試計畫

**執行時機**: 在 Home NB 完成測試文檔撰寫後

**操作步驟**:

1. 在 GUI 頂部輸入欄位填寫:
   - **任務主題 (Topic)**: 例如 `v2.9.6_phase1`
   - **測試裝置 (Device)**: 例如 `Pixel 9a`
   - **測試目標 (Objective)**: 簡述測試目的

2. 切換到 **1. 📤 Home NB (Push)** 分頁

3. (可選) 點擊 `🔍 檢查 Git 狀態` 確認變更

4. 點擊 `🚀 執行推送 (Commit & Push)`

5. 確認提示後，等待推送完成

**預期結果**:
```
[16:24:05] --- 🚀 Starting Task 1: Home NB (Push) ---
Step 1: Adding files...
Step 2: Committing...
Step 3: Pushing to github (main)...
✅ 成功！請切換到測試機執行步驟 2。
```

**錯誤處理**:
- **No changes to commit**: 請先修改測試文檔
- **Authentication failed**: 執行 `gh auth login` 或檢查 Personal Access Token
- **Connection refused**: 檢查網路連線

---

### Step 2: Work NB (Pull) - 拉取測試計畫

**執行時機**: 在 Work NB 準備開始測試時

**操作步驟**:

1. 啟動 GUI (在 Work NB 上)

2. 切換到 **2. 📥 Work NB (Pull)** 分頁

3. 點擊 `⬇️ 拉取更新 (Git Pull)`

4. 查看列出的測試文檔

5. 在 Claude Code Chat 中執行:
   ```
   引用 @docs/test_handoff/v2.9.6_phase1_work_nb_execution.md

   請協助我執行測試並生成報告。
   ```

**預期結果**:
```
[16:25:12] --- 📥 Starting Task 2: Work NB (Pull) ---
Step 1: Pulling from origin (main)...
✅ 同步成功！可用測試文檔：
Step 2: Listing test handoff documents...
  quick_commands_reference.md
  test_report_template.md
  v2.9.6_phase1_work_nb_execution.md
```

---

### Step 3: Work NB (Push) - 推送測試結果

**執行時機**: 在 Work NB 完成測試並生成報告後

**操作步驟**:

1. 確保測試報告已儲存於 `docs/test_results/` 目錄

2. 切換到 **3. 📤 Work NB (Push)** 分頁

3. 選擇測試結果: `PASS`, `FAIL`, 或 `PARTIAL`

4. (可選) 點擊 `🔍 檢查 Git 狀態` 確認測試報告已建立

5. 點擊 `🚀 推送報告 (Commit & Push)`

**預期結果**:
```
[17:30:45] --- 🚀 Starting Task 3: Work NB (Push) ---
Step 1: Adding test results...
Step 2: Committing...
Step 3: Pushing to origin (main)...
✅ 報告推送成功！
```

---

### Step 4: Home NB (Pull) - 查看測試報告

**執行時機**: Work NB 完成測試後，回到 Home NB 查看結果

**操作步驟**:

1. 切換回 Home NB

2. 啟動 GUI

3. 切換到 **4. 📥 Home NB (Pull)** 分頁

4. 點擊 `⬇️ 拉取並開啟報告`

5. 工具會自動:
   - 拉取最新變更
   - 列出最近 5 個測試報告
   - 在 VSCode 中開啟最新報告

**預期結果**:
```
[18:00:12] --- 📥 Starting Task 4: Home NB (Pull) ---
Step 1: Pulling from origin (main)...
✅ 下載完成。正在尋找最新報告...
Step 2: Opening latest test report...
Opening: D:\...\docs\test_results\v2.9.6_phase1_pixel9a_20260118_173045.md
✅ 測試交接循環完成！
```

---

## ⚙️ 配置管理

### Settings 分頁功能

1. **Git Remote 資訊**:
   - 顯示自動檢測到的所有 git remotes
   - 顯示 URL 位址

2. **預設值設定**:
   - **Topic**: 預設任務主題
   - **Device**: 預設測試裝置
   - **Objective**: 預設測試目標

3. **操作按鈕**:
   - `💾 儲存配置`: 將當前設定保存到配置檔
   - `🔄 恢復預設值`: 重置為原始預設值

### 配置檔案

**位置**: `.vscode/test_handoff_config.json`

**範例內容**:
```json
{
  "git_remotes": {
    "home_push": "github",
    "work_pull": "origin",
    "work_push": "origin",
    "home_pull": "origin"
  },
  "defaults": {
    "topic": "v2.9.6_phase1",
    "device": "Pixel 9a",
    "objective": "Verify OrientationEventListener fix and photo EXIF orientation"
  },
  "paths": {
    "test_handoff": "docs/test_handoff/",
    "test_results": "docs/test_results/"
  }
}
```

**使用方式**:
1. 在 Settings 分頁修改預設值
2. 點擊 `💾 儲存配置`
3. 下次啟動時將自動載入

**注意事項**:
- 配置檔會被 `.gitignore` 忽略（避免多機器間衝突）
- 如需同步配置，可手動複製配置檔到其他機器

---

## 🛠️ 進階功能

### Git Remote 智能檢測

工具會自動檢測所有可用的 git remotes，並根據操作類型選擇最佳 remote：

- **Home NB Push**: 優先使用 `github`，fallback 為 `origin`
- **Work NB Pull**: 優先使用 `origin`，fallback 為 `github`
- **Work NB Push**: 優先使用 `origin`，fallback 為 `github`
- **Home NB Pull**: 優先使用 `origin`，fallback 為 `github`

**檢測結果範例**:
```
📁 Config file: .vscode/test_handoff_config.json
📡 Detected Git Remotes:
  github: https://github.com/akirachangtw/SmartCameraApp1.git
  origin: D:\MyDocuments\DevSources\GitRepos\SmartCameraApp1.git
```

### 錯誤處理

工具提供友好的錯誤訊息和恢復建議：

| 錯誤類型 | 訊息 | 建議 |
|---------|------|------|
| No changes to commit | ❌ No changes to commit | Please modify files before committing. |
| Authentication failed | ❌ Git authentication failed | Run: gh auth login |
| Connection refused | ❌ Network connection failed | Check your internet connection. |
| Merge conflict | ❌ Merge conflict detected | Resolve conflicts manually. |

### 日誌管理

**功能**:
- ✅ 自動添加時間戳記於關鍵操作
- ✅ 清除日誌: `🗑️ 清除日誌` 按鈕
- ✅ 儲存日誌: `💾 儲存日誌` 按鈕

**日誌檔案命名**: `test_handoff_log_YYYYMMDD_HHMMSS.txt`

---

## 🔧 故障排除

### Issue 1: GUI 無法啟動

**症狀**: 執行 Python 腳本後無反應

**可能原因**:
- Python 版本過舊
- Tkinter 未安裝

**解決方案**:
```bash
# 檢查 Python 版本 (需 3.7+)
python --version

# Windows: Tkinter 通常已包含
# Linux/Mac: 可能需要額外安裝
# Ubuntu/Debian
sudo apt-get install python3-tk

# macOS
brew install python-tk
```

---

### Issue 2: Git 操作失敗

**症狀**: 所有 git 操作顯示錯誤

**可能原因**:
- Git 未安裝或未加入 PATH
- 不在 git repository 中執行

**解決方案**:
```bash
# 檢查 git 是否安裝
git --version

# 確認在專案根目錄執行
cd d:\MyDocuments\DevSources\AndroidStudioProjects\SmartCameraApp1
```

---

### Issue 3: Remote 檢測失敗

**症狀**: 啟動時顯示 "⚠️ Warning: No git remotes detected"

**可能原因**:
- Git remotes 未配置

**解決方案**:
```bash
# 檢查現有 remotes
git remote -v

# 如需添加 remotes
git remote add github https://github.com/akirachangtw/SmartCameraApp1.git
git remote add origin D:\MyDocuments\DevSources\GitRepos\SmartCameraApp1.git
```

---

### Issue 4: 配置檔無法儲存

**症狀**: 點擊 `💾 儲存配置` 後顯示錯誤

**可能原因**:
- `.vscode` 目錄不存在
- 檔案權限不足

**解決方案**:
```bash
# 建立 .vscode 目錄
mkdir .vscode

# Windows: 檢查檔案權限
icacls .vscode
```

---

## 📚 參考資料

### 相關文檔

- [v2.9.6_phase1_work_nb_execution.md](v2.9.6_phase1_work_nb_execution.md) - Work NB 測試執行指南
- [test_report_template.md](test_report_template.md) - 測試報告模板
- [quick_commands_reference.md](quick_commands_reference.md) - ADB 指令快速參考
- [VSCODE_TASKS_LEGACY.md](VSCODE_TASKS_LEGACY.md) - 舊版 VSCode Tasks 方案 (已棄用)

### 工具檔案

- **主程式**: `tools/test_handoff_gui.py`
- **備份版本**: `tools/test_handoff_gui_legacy.py` (優化前的原始版本)
- **配置檔**: `.vscode/test_handoff_config.json` (選用)

---

## 💡 最佳實踐

### 1. 保持配置一致性

在整個測試循環中使用相同的 Topic/Device 參數，確保 commit 訊息一致。

### 2. 定期儲存日誌

對於重要的測試流程，使用 `💾 儲存日誌` 保存操作記錄以供後續參考。

### 3. 使用配置檔

為常用設定建立配置檔，減少重複輸入：
1. 填寫常用的 Topic/Device/Objective
2. 點擊 Settings 分頁
3. 更新預設值並儲存

### 4. 測試前檢查 Git 狀態

在執行 Push 操作前，先點擊 `🔍 檢查 Git 狀態` 確認變更正確。

### 5. 錯誤處理

遇到錯誤時：
1. 閱讀錯誤訊息和建議
2. 查看日誌區域的詳細輸出
3. 必要時儲存日誌以供分析

---

## 🎯 完整工作流程範例

### 場景: v2.9.6 Phase 1 測試交接

#### Home NB (開發機)

1. **準備測試文檔**:
   - 編輯 `docs/test_handoff/v2.9.6_phase1_work_nb_execution.md`
   - 更新測試步驟、預期結果

2. **執行 Push** (Steps 1-2):
   ```
   - 啟動 GUI
   - 填寫: Topic=v2.9.6_phase1, Device=Pixel 9a
   - 切換到 Step 1
   - 點擊 "執行推送"
   - 確認成功訊息
   ```

#### Work NB (測試機 + Pixel 9a)

3. **拉取測試計畫** (Step 2):
   ```
   - 啟動 GUI
   - 切換到 Step 2
   - 點擊 "拉取更新"
   - 確認文檔已同步
   ```

4. **執行測試**:
   ```
   - 打開 Claude Code Chat
   - 引用測試文檔
   - 執行測試並生成報告
   ```

5. **推送測試結果** (Step 3):
   ```
   - 回到 GUI
   - 切換到 Step 3
   - 選擇結果: PASS
   - 點擊 "推送報告"
   ```

#### Home NB (開發機)

6. **查看測試報告** (Step 4):
   ```
   - 回到 Home NB
   - 啟動 GUI
   - 切換到 Step 4
   - 點擊 "拉取並開啟報告"
   - 檢視測試結果
   ```

---

**文檔版本**: 2.0
**最後更新**: 2026-01-18
**維護者**: Claude Code Assistant
**適用範圍**: SmartCameraApp1 所有測試交接場景
