# VSCode CLI 工具設定指南（Windows 11）

## 📋 目錄

- [什麼是 VSCode CLI](#什麼是-vscode-cli)
- [安裝 Visual Studio Code](#安裝-visual-studio-code)
- [啟用 code 命令](#啟用-code-命令)
- [驗證安裝](#驗證安裝)
- [故障排除](#故障排除)

---

## 🤔 什麼是 VSCode CLI

**VSCode CLI (Command Line Interface)** 是 Visual Studio Code 的命令行工具，允許您：

- 從終端打開文件和文件夾
- 安裝和管理外掛
- 比較文件
- 執行各種 VSCode 操作

**命令名稱**: `code`

**常用命令**:
```bash
code .                                  # 在當前目錄打開 VSCode
code file.txt                           # 打開文件
code --install-extension <ext-id>       # 安裝外掛
code --list-extensions                  # 列出已安裝外掛
code --diff file1.txt file2.txt        # 比較兩個文件
```

---

## 🔽 安裝 Visual Studio Code

### 步驟 1: 下載 VSCode

**官方網站**: https://code.visualstudio.com/

**下載選項**:
- ✅ **User Installer (推薦)**: 安裝到用戶目錄，不需要管理員權限
- System Installer: 安裝到系統目錄，需要管理員權限
- Portable: 綠色版本，不寫入註冊表

**Windows 11 推薦**: User Installer 64 位元版本

---

### 步驟 2: 安裝 VSCode

#### 重要！安裝時的選項設定

運行安裝程式後，在「選擇額外任務」畫面中，**務必勾選**：

```
┌─────────────────────────────────────────────────────────┐
│ 選擇額外任務                                              │
│                                                          │
│ 選擇您希望安裝程式執行的額外任務:                           │
│                                                          │
│ ☐ 建立桌面圖示                                            │
│                                                          │
│ 其他:                                                     │
│ ✅ 新增「以 Code 開啟」動作至 Windows 檔案總管的           │
│    檔案內容功能表                                          │
│ ✅ 新增「以 Code 開啟」動作至 Windows 檔案總管的           │
│    目錄內容功能表                                          │
│ ✅ 將 Code 註冊為支援的檔案類型的編輯器                     │
│ ✅ 新增至 PATH (重新啟動後生效)  ← 最重要！                │
│                                                          │
└─────────────────────────────────────────────────────────┘
```

**關鍵選項**: `✅ 新增至 PATH (重新啟動後生效)`

這個選項會自動將 `code` 命令添加到系統環境變數！

---

### 步驟 3: 重新啟動

**重要**: 安裝完成後，必須重新啟動終端（或重新登入 Windows）才能使 PATH 變更生效。

---

## ⚙️ 啟用 code 命令

### 情況 1: 安裝時已勾選「新增至 PATH」

如果安裝時已勾選該選項：

1. **關閉所有終端視窗**
2. **重新打開終端**（PowerShell、CMD 或 Git Bash）
3. **測試命令**:
   ```powershell
   code --version
   ```

如果顯示版本號，表示成功！

---

### 情況 2: 安裝時未勾選「新增至 PATH」

#### 方法 1: 在 VSCode 中手動啟用（推薦）⭐

1. **打開 VSCode**
   - 從開始功能表啟動
   - 或搜索「Visual Studio Code」

2. **打開命令面板**
   - 按 `Ctrl+Shift+P`
   - 或點擊 `檢視` → `命令選擇區`

3. **輸入並選擇**:
   ```
   Shell Command: Install 'code' command in PATH
   ```
   或中文版：
   ```
   殼層命令: 在 PATH 中安裝 'code' 命令
   ```

4. **等待提示**
   - 會顯示「已成功安裝」的訊息

5. **重新啟動終端**
   - 關閉所有終端視窗
   - 重新打開

6. **驗證**:
   ```powershell
   code --version
   ```

---

#### 方法 2: 手動添加到 PATH（進階）

**User Installer 路徑**:
```
C:\Users\<YourUsername>\AppData\Local\Programs\Microsoft VS Code\bin
```

**System Installer 路徑**:
```
C:\Program Files\Microsoft VS Code\bin
```

**步驟**:

1. **找到 VSCode 安裝路徑**:
   ```powershell
   # 在 PowerShell 中執行
   Get-ChildItem -Path "$env:LOCALAPPDATA\Programs\Microsoft VS Code\bin\code.cmd" -ErrorAction SilentlyContinue
   ```

2. **打開系統環境變數設定**:
   - 按 `Win + X`
   - 選擇「系統」
   - 點擊「進階系統設定」
   - 點擊「環境變數」

3. **編輯 PATH 變數**:
   - 在「使用者變數」區域找到 `Path`
   - 點擊「編輯」
   - 點擊「新增」
   - 輸入 VSCode 的 bin 目錄路徑：
     ```
     C:\Users\<YourUsername>\AppData\Local\Programs\Microsoft VS Code\bin
     ```
   - 點擊「確定」保存

4. **重新啟動終端**

5. **驗證**:
   ```powershell
   code --version
   ```

---

#### 方法 3: 使用 PowerShell 腳本自動添加

創建並執行以下 PowerShell 腳本：

```powershell
# add-vscode-to-path.ps1

Write-Host "正在搜索 VSCode 安裝路徑..." -ForegroundColor Yellow

# 可能的安裝路徑
$possiblePaths = @(
    "$env:LOCALAPPDATA\Programs\Microsoft VS Code\bin",
    "$env:ProgramFiles\Microsoft VS Code\bin",
    "${env:ProgramFiles(x86)}\Microsoft VS Code\bin"
)

$vscodePath = $null

foreach ($path in $possiblePaths) {
    if (Test-Path "$path\code.cmd") {
        $vscodePath = $path
        Write-Host "找到 VSCode: $path" -ForegroundColor Green
        break
    }
}

if (-not $vscodePath) {
    Write-Host "錯誤: 找不到 VSCode 安裝路徑！" -ForegroundColor Red
    Write-Host "請確認 VSCode 已正確安裝。" -ForegroundColor Yellow
    exit 1
}

# 檢查是否已在 PATH 中
$currentPath = [Environment]::GetEnvironmentVariable("Path", "User")

if ($currentPath -like "*$vscodePath*") {
    Write-Host "VSCode 已在 PATH 中！" -ForegroundColor Green
} else {
    Write-Host "正在添加 VSCode 到 PATH..." -ForegroundColor Yellow

    # 添加到用戶 PATH
    $newPath = "$currentPath;$vscodePath"
    [Environment]::SetEnvironmentVariable("Path", $newPath, "User")

    Write-Host "成功添加到 PATH！" -ForegroundColor Green
    Write-Host "請重新啟動終端以使變更生效。" -ForegroundColor Yellow
}

# 驗證
Write-Host ""
Write-Host "驗證安裝..." -ForegroundColor Yellow
$env:Path = [Environment]::GetEnvironmentVariable("Path", "User") + ";" + [Environment]::GetEnvironmentVariable("Path", "Machine")

try {
    $version = & "$vscodePath\code.cmd" --version 2>&1
    Write-Host "VSCode 版本: $($version[0])" -ForegroundColor Green
    Write-Host "安裝成功！" -ForegroundColor Green
} catch {
    Write-Host "警告: 需要重新啟動終端才能使用 code 命令" -ForegroundColor Yellow
}
```

**執行方式**:
```powershell
.\add-vscode-to-path.ps1
```

---

## ✅ 驗證安裝

### 測試 1: 檢查版本

```powershell
code --version
```

**預期輸出**:
```
1.95.3
f1a4fb101478ce6ec82fe9627c43efbf9e98c813
x64
```

---

### 測試 2: 檢查命令位置

```powershell
# PowerShell
Get-Command code

# CMD
where code
```

**預期輸出**:
```
CommandType     Name      Version    Source
-----------     ----      -------    ------
Application     code.cmd  0.0.0.0    C:\Users\...\Microsoft VS Code\bin\code.cmd
```

---

### 測試 3: 打開 VSCode

```powershell
# 在當前目錄打開 VSCode
code .
```

應該會啟動 VSCode 並打開當前文件夾。

---

### 測試 4: 列出已安裝的外掛

```powershell
code --list-extensions
```

應該顯示已安裝的外掛列表（如果有的話）。

---

## 🔧 故障排除

### 問題 1: `code: command not found` 或 `code 不是內部或外部命令`

**原因**: `code` 命令未添加到 PATH

**解決方案**:
1. 使用「方法 1: 在 VSCode 中手動啟用」（最簡單）
2. 或使用「方法 2: 手動添加到 PATH」
3. 重新啟動終端

---

### 問題 2: 重啟終端後仍然無法使用

**檢查步驟**:

1. **確認 PATH 是否添加成功**:
   ```powershell
   $env:Path -split ';' | Select-String "VS Code"
   ```

   應該顯示包含 VSCode 的路徑。

2. **檢查 code.cmd 是否存在**:
   ```powershell
   Test-Path "$env:LOCALAPPDATA\Programs\Microsoft VS Code\bin\code.cmd"
   ```

   應該返回 `True`。

3. **嘗試使用完整路徑**:
   ```powershell
   & "$env:LOCALAPPDATA\Programs\Microsoft VS Code\bin\code.cmd" --version
   ```

   如果這個可以工作，說明問題在於 PATH 設定。

---

### 問題 3: PowerShell 執行策略限制

**錯誤訊息**:
```
無法載入檔案 *.ps1，因為這個系統上已停用指令碼執行。
```

**解決方案**:
```powershell
# 以管理員身分運行 PowerShell
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

---

### 問題 4: VSCode 安裝在非標準位置

如果 VSCode 安裝在自定義路徑：

1. **找到安裝路徑**:
   - 右鍵桌面上的 VSCode 捷徑
   - 選擇「開啟檔案位置」
   - 向上一層目錄，找到 `bin` 文件夾

2. **手動添加該路徑到 PATH**（參考方法 2）

---

### 問題 5: 使用 Windows Terminal 或其他終端

某些終端可能需要額外配置：

**Windows Terminal**:
1. 打開設定（`Ctrl+,`）
2. 選擇預設設定檔（PowerShell 或 CMD）
3. 重新啟動 Windows Terminal

**Git Bash**:
```bash
# 在 ~/.bashrc 中添加
export PATH="$PATH:/c/Users/<YourUsername>/AppData/Local/Programs/Microsoft VS Code/bin"
```

---

## 📚 常用 code 命令

### 基本操作

```powershell
# 在當前目錄打開 VSCode
code .

# 打開特定文件
code file.txt

# 打開特定文件夾
code D:\Projects\MyProject

# 在新視窗中打開
code -n .

# 等待文件關閉後才返回（用於 Git 編輯器）
code --wait file.txt
```

---

### 外掛管理

```powershell
# 安裝外掛
code --install-extension <extension-id>

# 列出已安裝的外掛
code --list-extensions

# 列出已安裝的外掛（包含版本）
code --list-extensions --show-versions

# 卸載外掛
code --uninstall-extension <extension-id>

# 禁用所有外掛啟動 VSCode
code --disable-extensions
```

---

### 文件比較

```powershell
# 比較兩個文件
code --diff file1.txt file2.txt
```

---

### 其他實用命令

```powershell
# 查看版本
code --version

# 查看幫助
code --help

# 以特定語言模式打開文件
code --language javascript file.txt

# 打開特定文件並跳到指定行和列
code --goto file.txt:10:5
```

---

## 🎯 設定完成檢查清單

- [ ] VSCode 已安裝
- [ ] `code --version` 可以運行
- [ ] `code .` 可以打開 VSCode
- [ ] `code --list-extensions` 可以運行
- [ ] 重啟終端後命令仍然可用
- [ ] 可以使用 `code --install-extension` 安裝外掛

---

## 📖 下一步

設定完成後，您可以：

1. **安裝外掛**:
   - 運行 `install-vscode-extensions.ps1` 或 `install-vscode-extensions.bat`
   - 或使用手動安裝指令

2. **配置 VSCode**:
   - 參考 `VSCODE_EXTENSIONS_HYBRID_MODE.md`
   - 設定 `settings.json`

3. **開始使用混合工作流**:
   - 參考 `HYBRID_WORKFLOW_GUIDE.md`

---

## 🔗 相關資源

- [VSCode 官方網站](https://code.visualstudio.com/)
- [VSCode 命令行文檔](https://code.visualstudio.com/docs/editor/command-line)
- [VSCode 外掛市場](https://marketplace.visualstudio.com/)

---

*最後更新: 2025-11-09*
*作業系統: Windows 11*
*VSCode 版本: 1.95+*
