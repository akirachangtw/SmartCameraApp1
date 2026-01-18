# ADB 裝置管理工具 - Python 版本

## 🎯 功能特色

✅ **無主控台視窗** - 使用 `.pyw` 副檔名，只顯示 GUI 介面
✅ **裝置管理** - 自動偵測並列出所有連線的 ADB 裝置
✅ **螢幕常亮控制** - USB 充電時保持螢幕常亮
✅ **應用程式權限** - 授予 TeamViewer 媒體投影權限
✅ **即時日誌** - 顯示所有操作的執行結果
✅ **美化介面** - 使用現代化配色方案

---

## 📋 系統需求

- **Python 3.8+** (已安裝 Python 3.14 ✅)
- **ADB 工具** (需在系統 PATH 中或與腳本同目錄)
- **作業系統**: Windows / macOS / Linux

---

## 🚀 使用方式

### 方法 1: 直接執行（推薦）

雙擊檔案：
```
UsbDevTool_Enhanced.pyw
```

✅ **不會顯示黑色主控台視窗**
✅ **直接啟動 GUI 介面**

### 方法 2: 從命令列執行

```bash
# 使用 pythonw (無視窗)
pythonw UsbDevTool_Enhanced.pyw

# 或使用 python (會有主控台，適合除錯)
python UsbDevTool_Enhanced.pyw
```

---

## 🔧 功能說明

### 1️⃣ 裝置連線
- **重新整理**: 掃描並更新已連線的 ADB 裝置
- **重啟 ADB**: 重新啟動 ADB server（解決連線問題）
- **裝置資訊**: 顯示型號、Android 版本、序號

### 2️⃣ 螢幕控制
- **啟用常亮**: USB 充電時保持螢幕開啟
- **停用常亮**: 恢復預設螢幕逾時設定
- **螢幕休眠**: 立即關閉螢幕（模擬電源鍵）

**對應指令**:
```bash
adb shell svc power stayon usb      # 啟用常亮
adb shell svc power stayon false    # 停用常亮
adb shell input keyevent 26         # 螢幕休眠（KEYCODE_POWER）
```

### 3️⃣ 應用程式權限控制
- **授予 TeamViewer 媒體權限**: 允許螢幕投影功能

**對應指令**:
```bash
adb shell appops set com.teamviewer.host.market PROJECT_MEDIA allow
```

---

## 📦 依賴套件

**內建套件**（無需額外安裝）:
- `tkinter` - GUI 框架
- `subprocess` - 執行 ADB 命令
- `threading` - 非同步執行

---

## 🎨 與 PowerShell 版本比較

| 項目 | PowerShell 版本 | Python 版本 |
|------|----------------|-------------|
| 執行方式 | 需 VBS 啟動器 | 直接執行 `.pyw` |
| 跨平台 | ❌ 僅 Windows | ✅ Win/Mac/Linux |
| 依賴項 | PowerShell 5.1+ | Python 3.8+ |
| GUI 框架 | WinForms | tkinter |
| 程式碼行數 | ~520 行 | ~550 行 |
| 主控台視窗 | 需 VBS 隱藏 | `.pyw` 自動隱藏 |

---

## 🛠️ 進階功能

### 自訂 ADB 路徑

如果 ADB 不在系統 PATH 中，可修改腳本第 35 行：

```python
def __init__(self, adb_path: str = "adb"):
```

改為：

```python
def __init__(self, adb_path: str = "C:/Android/platform-tools/adb.exe"):
```

### 打包成 .exe

如果需要分發給沒有安裝 Python 的用戶：

```bash
# 安裝 PyInstaller
pip install pyinstaller

# 打包（無主控台）
pyinstaller --noconsole --onefile --name "ADB裝置管理工具" UsbDevTool_Enhanced.pyw

# 產生的 .exe 位於 dist/ 資料夾
```

---

## 🐛 疑難排解

### 問題 1: 雙擊無反應

**解決方法**:
1. 確認 `.pyw` 檔案關聯到 Python
2. 右鍵 → 開啟檔案 → 選擇 `pythonw.exe`
3. 或從命令列執行：`pythonw UsbDevTool_Enhanced.pyw`

### 問題 2: 找不到 ADB

**解決方法**:
1. 確認 ADB 已安裝
2. 執行 `adb version` 測試
3. 將 ADB 加入系統 PATH
4. 或修改腳本指定完整路徑

### 問題 3: 權限不足

**解決方法**:
1. 確認手機已啟用 USB 偵錯
2. 手機上授權電腦（RSA 金鑰指紋）
3. 執行 `adb devices` 檢查連線狀態

---

## 📝 版本歷史

### v1.1 (2026-01-15)
- ✨ 新增「螢幕休眠」功能（模擬電源鍵）
- 🎨 更新區域標題：「螢幕常亮控制」→「螢幕控制」
- 📐 調整按鈕寬度（3 按鈕並排佈局）

### v1.0 (2026-01-15)
- 🎉 首次發布 Python 版本
- ✅ 完整移植 PowerShell 版本所有功能
- ✅ 使用 `.pyw` 副檔名隱藏主控台
- ✅ 非同步執行，避免 GUI 凍結
- ✅ 現代化配色與介面設計

---

## 📄 授權

本工具為開源專案，可自由使用與修改。

---

## 🔗 相關檔案

- `UsbDevTool_Enhanced.pyw` - Python 版本主程式
- `UsbDevTool_Enhanced.ps1` - PowerShell 版本
- `UsbDevTool_Enhanced_Launcher.vbs` - PowerShell 啟動器

---

**開發者**: AkiraChang
**最後更新**: 2026-01-15
