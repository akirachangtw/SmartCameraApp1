# VSCode 混合工作流模式 - 精簡外掛指南

## 📋 目錄

- [混合模式 vs 純 VSCode 開發](#混合模式-vs-純-vscode-開發)
- [必裝外掛（最小化配置）](#必裝外掛最小化配置)
- [強烈推薦外掛](#強烈推薦外掛)
- [可選外掛](#可選外掛)
- [不需要的外掛](#不需要的外掛)
- [快速安裝指令](#快速安裝指令)
- [配置優化](#配置優化)

---

## 🤔 混合模式 vs 純 VSCode 開發

### 關鍵差異

| 功能需求 | 純 VSCode 開發 | 混合工作流（VSCode + AS） |
|---------|---------------|------------------------|
| **Kotlin 語法高亮** | ✅ 必須 | ✅ 必須 |
| **代碼智能補全** | ✅ 必須（進階） | ⚠️ 基本即可 |
| **調試工具** | ✅ 必須配置 | ❌ 不需要（用 AS） |
| **模擬器管理** | ✅ 必須 | ⚠️ 簡化版（主要用 AS） |
| **性能分析** | ⚠️ 有限支持 | ❌ 不需要（用 AS） |
| **UI 預覽** | ❌ 不支持 | ❌ 不需要（用 AS） |
| **Git 工具** | ✅ 必須（增強版） | ✅ 必須（增強版） |
| **終端工具** | ✅ 必須 | ✅ 必須 |

### 混合模式的外掛策略

**核心理念**: **輕量化、快速啟動、專注編碼**

- ✅ 保留：編碼必需的基礎工具
- ✅ 加強：Git 和終端操作
- ❌ 移除：調試和測試工具（交給 Android Studio）
- ❌ 移除：重量級的智能補全（基本補全就夠）

---

## 🔴 必裝外掛（最小化配置）

### 1. Kotlin Language Support
- **外掛 ID**: `mathiasfrohlich.Kotlin`
- **用途**:
  - Kotlin 語法高亮
  - 基本代碼片段
  - 括號匹配
- **為什麼必須**:
  - VSCode 不原生支持 Kotlin
  - 沒有它連基本的顏色高亮都沒有
- **安裝**:
  ```bash
  code --install-extension mathiasfrohlich.Kotlin
  ```

### 2. Extension Pack for Java
- **外掛 ID**: `vscjava.vscode-java-pack`
- **包含外掛**:
  - Language Support for Java
  - Debugger for Java
  - Test Runner for Java
  - Maven for Java
  - Project Manager for Java
  - IntelliCode
- **用途**:
  - Java/Kotlin 基礎語言支持
  - Gradle 項目識別
  - 基本智能補全
- **為什麼必須**:
  - Android 專案底層是 Java 生態
  - 提供基本的代碼跳轉和補全
  - 不裝這個 VSCode 連專案結構都識別不了
- **安裝**:
  ```bash
  code --install-extension vscjava.vscode-java-pack
  ```

### 3. Gradle for Java
- **外掛 ID**: `vscjava.vscode-gradle`
- **用途**:
  - Gradle 任務管理
  - 依賴視圖
  - 構建腳本語法支持
- **為什麼必須**:
  - 查看和執行 Gradle 任務
  - 同步依賴（相當於 AS 的 Sync）
  - 沒有它無法執行構建命令
- **安裝**:
  ```bash
  code --install-extension vscjava.vscode-gradle
  ```

---

## 🟡 強烈推薦外掛

### 4. GitLens — Git supercharged ⭐⭐⭐⭐⭐
- **外掛 ID**: `eamodio.gitlens`
- **用途**:
  - 查看代碼歷史
  - Blame 註解（誰寫的代碼）
  - 分支管理
  - 提交歷史可視化
  - 文件歷史追蹤
- **為什麼推薦**:
  - 混合模式下 Git 操作主要在 VSCode
  - 比 Android Studio 的 Git 工具更強大
  - 極大提升 Git 工作效率
- **混合模式特別功能**:
  - 查看 Android Studio 提交的歷史
  - 對比不同 IDE 產生的更改
- **安裝**:
  ```bash
  code --install-extension eamodio.gitlens
  ```

### 5. Error Lens ⭐⭐⭐⭐⭐
- **外掛 ID**: `usernamehw.errorlens`
- **用途**:
  - 即時顯示錯誤和警告
  - 行內錯誤提示
  - 不用鼠標懸停就能看到問題
- **為什麼推薦**:
  - 快速發現語法錯誤
  - 避免切換到 AS 才發現基本錯誤
  - 提高編碼效率
- **安裝**:
  ```bash
  code --install-extension usernamehw.errorlens
  ```

### 6. TODO Tree ⭐⭐⭐⭐
- **外掛 ID**: `Gruntfuggly.todo-tree`
- **用途**:
  - 掃描所有 TODO/FIXME 註釋
  - 側邊欄統一顯示
  - 快速跳轉到待辦項目
- **為什麼推薦**:
  - 在 AS 測試時發現問題，在 VSCode 中標記 TODO
  - 統一管理所有待修復項目
  - 防止遺漏問題
- **混合模式特別用途**:
  ```kotlin
  // 在 Android Studio 測試時發現問題
  // 回到 VSCode 添加註釋
  // TODO: 修復快速滑動時的卡頓問題（發現於 AS Profiler）
  // FIXME: 記憶體洩漏（AS Memory Profiler 檢測到）
  ```
- **安裝**:
  ```bash
  code --install-extension Gruntfuggly.todo-tree
  ```

### 7. Material Icon Theme ⭐⭐⭐
- **外掛 ID**: `PKief.material-icon-theme`
- **用途**:
  - 文件和文件夾圖標美化
  - Kotlin/Gradle 文件識別
  - 提升視覺體驗
- **為什麼推薦**:
  - 快速識別文件類型
  - 專案結構一目了然
  - 降低視覺疲勞
- **安裝**:
  ```bash
  code --install-extension PKief.material-icon-theme
  ```

### 8. Code Spell Checker ⭐⭐⭐
- **外掛 ID**: `streetsidesoftware.code-spell-checker`
- **用途**:
  - 拼寫檢查
  - 避免變量名拼寫錯誤
- **為什麼推薦**:
  - 防止因拼寫錯誤導致的低級 Bug
  - 提高代碼專業度
- **安裝**:
  ```bash
  code --install-extension streetsidesoftware.code-spell-checker
  ```

---

## 🟢 可選外掛（按需安裝）

### 9. Markdown All in One
- **外掛 ID**: `yzhang.markdown-all-in-one`
- **用途**: 編輯 README、文檔
- **安裝時機**: 如果經常寫文檔
- **安裝**:
  ```bash
  code --install-extension yzhang.markdown-all-in-one
  ```

### 10. Bracket Pair Colorizer 2（或內建功能）
- **外掛 ID**: `CoenraadS.bracket-pair-colorizer-2`
- **用途**: 括號配對顏色標記
- **注意**: VSCode 1.60+ 已內建此功能
- **啟用內建功能**:
  ```json
  {
    "editor.bracketPairColorization.enabled": true,
    "editor.guides.bracketPairs": true
  }
  ```

### 11. indent-rainbow
- **外掛 ID**: `oderwat.indent-rainbow`
- **用途**: 縮進顏色標記
- **安裝時機**: Kotlin 嵌套較深時
- **安裝**:
  ```bash
  code --install-extension oderwat.indent-rainbow
  ```

### 12. Better Comments
- **外掛 ID**: `aaron-bond.better-comments`
- **用途**: 彩色註釋（TODO、FIXME、NOTE 等）
- **安裝時機**: 配合 TODO Tree 使用
- **安裝**:
  ```bash
  code --install-extension aaron-bond.better-comments
  ```

### 13. Git Graph
- **外掛 ID**: `mhutchie.git-graph`
- **用途**: Git 提交歷史圖形化
- **安裝時機**: 如果需要可視化分支歷史
- **注意**: GitLens 已提供類似功能
- **安裝**:
  ```bash
  code --install-extension mhutchie.git-graph
  ```

### 14. File Utils
- **外掛 ID**: `sleistner.vscode-fileutils`
- **用途**: 快速複製/移動/重命名文件
- **安裝時機**: 如果經常重組文件結構
- **安裝**:
  ```bash
  code --install-extension sleistner.vscode-fileutils
  ```

### 15. Path Intellisense
- **外掛 ID**: `christian-kohler.path-intellisense`
- **用途**: 文件路徑自動補全
- **安裝時機**: 如果經常手寫 import 路徑
- **安裝**:
  ```bash
  code --install-extension christian-kohler.path-intellisense
  ```

---

## ❌ 不需要的外掛（混合模式下）

以下外掛在純 VSCode 開發中有用，但在混合模式下**不建議安裝**：

### 調試相關（用 Android Studio 代替）
- ❌ `Android iOS Emulator` - 模擬器管理（AS 更專業）
- ❌ `Android Studio Emulator Manager` - AVD 管理（AS 更專業）
- ❌ `ADB Interface for VSCode` - ADB 工具（終端直接用更快）
- ❌ `Debugger for Java` - Java 調試器（AS 更強大）

### UI/預覽相關（VSCode 不支持）
- ❌ `Compose Multiplatform IDE Support` - Compose 預覽（VSCode 支持有限）
- ❌ `Flutter/React Native` 相關外掛（本專案不需要）

### 重量級智能補全（會拖慢啟動速度）
- ❌ `Tabnine` - AI 補全（會占用記憶體）
- ❌ `GitHub Copilot` - AI 輔助編程（可選，但會拖慢速度）
- ❌ `Kite` - AI 補全（已停止維護）

### 測試相關（用 Android Studio 代替）
- ❌ `Test Explorer UI` - 測試視圖（AS 的測試工具更完善）
- ❌ `Coverage Gutters` - 代碼覆蓋率（AS 更準確）

### 為什麼不裝這些？

1. **保持輕量**: 外掛越多，啟動越慢
2. **避免重複**: Android Studio 已經提供更好的工具
3. **減少衝突**: 某些外掛可能與 AS 產生的文件衝突
4. **專注編碼**: VSCode 只需要編碼工具

---

## ⚡ 快速安裝指令

### 最小化安裝（僅核心功能）

**僅 3 個外掛，2 秒安裝完成**

```bash
code --install-extension mathiasfrohlich.Kotlin && \
code --install-extension vscjava.vscode-java-pack && \
code --install-extension vscjava.vscode-gradle
```

### 推薦安裝（最佳體驗）

**8 個外掛，10 秒安裝完成**

```bash
code --install-extension mathiasfrohlich.Kotlin && \
code --install-extension vscjava.vscode-java-pack && \
code --install-extension vscjava.vscode-gradle && \
code --install-extension eamodio.gitlens && \
code --install-extension usernamehw.errorlens && \
code --install-extension Gruntfuggly.todo-tree && \
code --install-extension PKief.material-icon-theme && \
code --install-extension streetsidesoftware.code-spell-checker
```

### 完整安裝（包含可選外掛）

```bash
code --install-extension mathiasfrohlich.Kotlin && \
code --install-extension vscjava.vscode-java-pack && \
code --install-extension vscjava.vscode-gradle && \
code --install-extension eamodio.gitlens && \
code --install-extension usernamehw.errorlens && \
code --install-extension Gruntfuggly.todo-tree && \
code --install-extension PKief.material-icon-theme && \
code --install-extension streetsidesoftware.code-spell-checker && \
code --install-extension yzhang.markdown-all-in-one && \
code --install-extension oderwat.indent-rainbow && \
code --install-extension aaron-bond.better-comments && \
code --install-extension mhutchie.git-graph && \
code --install-extension sleistner.vscode-fileutils && \
code --install-extension christian-kohler.path-intellisense
```

### Windows PowerShell 版本（推薦安裝）

```powershell
# 核心外掛（必裝）
$coreExtensions = @(
    "mathiasfrohlich.Kotlin",
    "vscjava.vscode-java-pack",
    "vscjava.vscode-gradle"
)

# 推薦外掛
$recommendedExtensions = @(
    "eamodio.gitlens",
    "usernamehw.errorlens",
    "Gruntfuggly.todo-tree",
    "PKief.material-icon-theme",
    "streetsidesoftware.code-spell-checker"
)

# 可選外掛
$optionalExtensions = @(
    "yzhang.markdown-all-in-one",
    "oderwat.indent-rainbow",
    "aaron-bond.better-comments",
    "mhutchie.git-graph",
    "sleistner.vscode-fileutils",
    "christian-kohler.path-intellisense"
)

# 安裝核心 + 推薦
($coreExtensions + $recommendedExtensions) | ForEach-Object {
    Write-Host "Installing $_..." -ForegroundColor Green
    code --install-extension $_
}

# 詢問是否安裝可選外掛
$installOptional = Read-Host "是否安裝可選外掛? (Y/N)"
if ($installOptional -eq "Y" -or $installOptional -eq "y") {
    $optionalExtensions | ForEach-Object {
        Write-Host "Installing $_..." -ForegroundColor Cyan
        code --install-extension $_
    }
}

Write-Host "安裝完成！" -ForegroundColor Yellow
```

---

## ⚙️ 配置優化

### 精簡版 settings.json（混合模式專用）

創建 `.vscode/settings.json`：

```json
{
  // ============================================
  // 核心配置（必須）
  // ============================================

  // Java/Kotlin 設定
  "java.configuration.updateBuildConfiguration": "automatic",
  "java.home": "C:\\Program Files\\Java\\jdk-11",
  "java.jdt.ls.vmargs": "-Xmx1G",  // 降低記憶體占用（AS 會用更多）

  // Gradle 設定
  "gradle.nestedProjects": true,
  "gradle.autoDetect": "on",

  // ============================================
  // 文件同步（重要！）
  // ============================================

  // 自動保存（避免 AS 和 VSCode 衝突）
  "files.autoSave": "afterDelay",
  "files.autoSaveDelay": 1000,

  // 監控文件變化（檢測 AS 的更改）
  "files.watcherExclude": {
    "**/.gradle/**": true,
    "**/build/**": true,
    "**/.idea/**": false  // 不排除 .idea，確保能檢測 AS 更改
  },

  // ============================================
  // Git 配置（混合模式重點）
  // ============================================

  "git.autofetch": true,
  "git.confirmSync": false,
  "git.enableSmartCommit": true,
  "git.suggestSmartCommit": true,

  // GitLens 設定
  "gitlens.codeLens.enabled": true,
  "gitlens.currentLine.enabled": true,
  "gitlens.hovers.currentLine.over": "line",
  "gitlens.blame.highlight.enabled": true,

  // ============================================
  // 編輯器設定
  // ============================================

  "editor.formatOnSave": true,
  "editor.formatOnPaste": false,  // 避免自動格式化干擾
  "editor.tabSize": 4,
  "editor.insertSpaces": true,
  "editor.rulers": [100, 120],

  // 括號配對（內建功能，不需要外掛）
  "editor.bracketPairColorization.enabled": true,
  "editor.guides.bracketPairs": true,

  // ============================================
  // Kotlin 特定設定
  // ============================================

  "[kotlin]": {
    "editor.tabSize": 4,
    "editor.insertSpaces": true,
    "editor.wordWrap": "off",
    "editor.defaultFormatter": "mathiasfrohlich.Kotlin"
  },

  "[java]": {
    "editor.tabSize": 4,
    "editor.insertSpaces": true
  },

  // ============================================
  // Error Lens 設定
  // ============================================

  "errorLens.enabledDiagnosticLevels": [
    "error",
    "warning"
  ],
  "errorLens.excludeBySource": [
    "cSpell"  // 排除拼寫檢查的錯誤顯示
  ],
  "errorLens.fontSize": "0.9em",
  "errorLens.fontWeight": "normal",

  // ============================================
  // TODO Tree 設定
  // ============================================

  "todo-tree.general.tags": [
    "TODO",
    "FIXME",
    "BUG",
    "HACK",
    "NOTE",
    "XXX",
    "[ ]",
    "[x]"
  ],
  "todo-tree.highlights.defaultHighlight": {
    "foreground": "black",
    "type": "text"
  },
  "todo-tree.highlights.customHighlight": {
    "TODO": {
      "icon": "check",
      "background": "#ffcc00"
    },
    "FIXME": {
      "icon": "alert",
      "background": "#ff6666"
    },
    "NOTE": {
      "icon": "note",
      "background": "#66ccff"
    }
  },

  // ============================================
  // 終端設定
  // ============================================

  "terminal.integrated.defaultProfile.windows": "PowerShell",
  "terminal.integrated.fontSize": 13,
  "terminal.integrated.scrollback": 10000,

  // ============================================
  // 文件排除（加快搜索速度）
  // ============================================

  "files.exclude": {
    "**/.gradle": true,
    "**/build": true,
    "**/.DS_Store": true,
    "**/local.properties": true
  },

  "search.exclude": {
    "**/build": true,
    "**/.gradle": true,
    "**/node_modules": true,
    "**/.idea": false  // 不排除，以便搜索 AS 配置
  },

  // ============================================
  // 性能優化（重要！）
  // ============================================

  // 禁用不必要的功能以加快速度
  "java.maxConcurrentBuilds": 1,  // 降低並發構建數
  "java.autobuild.enabled": true,
  "java.completion.enabled": true,
  "java.completion.maxResults": 50,  // 限制補全結果數量

  // ============================================
  // UI 設定
  // ============================================

  "workbench.iconTheme": "material-icon-theme",
  "workbench.colorTheme": "Default Dark+",  // 或您喜歡的主題
  "workbench.startupEditor": "none",  // 快速啟動

  // ============================================
  // 其他設定
  // ============================================

  "telemetry.telemetryLevel": "off",  // 關閉遙測提升速度
  "extensions.autoUpdate": false,  // 手動更新外掛
  "update.mode": "manual"  // 手動更新 VSCode
}
```

### 性能對比

| 配置 | 啟動時間 | 記憶體占用 | 外掛數量 |
|-----|---------|----------|---------|
| **最小化安裝** | 2-3 秒 | 150-200 MB | 3 個 |
| **推薦安裝** | 3-4 秒 | 200-300 MB | 8 個 |
| **完整安裝** | 4-5 秒 | 300-400 MB | 15 個 |
| **過度安裝（不推薦）** | 10-15 秒 | 600-800 MB | 30+ 個 |

**建議**: 選擇「推薦安裝」配置，兼顧功能和性能。

---

## 📊 外掛對比表

### 核心外掛對比（必裝 3 個）

| 外掛 | 大小 | 啟動影響 | 必要性 | 替代方案 |
|-----|------|---------|-------|---------|
| Kotlin Language | 小 | 極小 | ⭐⭐⭐⭐⭐ | 無 |
| Extension Pack for Java | 中 | 小 | ⭐⭐⭐⭐⭐ | 無 |
| Gradle for Java | 小 | 極小 | ⭐⭐⭐⭐⭐ | 終端手動執行 |

### 推薦外掛對比

| 外掛 | 大小 | 啟動影響 | 功能價值 | 是否裝 |
|-----|------|---------|---------|-------|
| GitLens | 中 | 小 | ⭐⭐⭐⭐⭐ | ✅ 強烈推薦 |
| Error Lens | 小 | 極小 | ⭐⭐⭐⭐⭐ | ✅ 強烈推薦 |
| TODO Tree | 小 | 極小 | ⭐⭐⭐⭐ | ✅ 推薦 |
| Material Icon | 小 | 極小 | ⭐⭐⭐ | ✅ 推薦 |
| Code Spell Checker | 中 | 小 | ⭐⭐⭐ | ✅ 推薦 |

### 不推薦外掛對比（混合模式下）

| 外掛 | 為何不裝 | Android Studio 替代 |
|-----|---------|-------------------|
| Android Emulator | AS 模擬器管理更專業 | AVD Manager |
| ADB Interface | 終端直接用更快 | Terminal 內建 ADB |
| Compose Preview | VSCode 支持有限 | AS Compose Preview |
| Java Debugger | AS 調試器更強大 | AS Debug 工具 |
| Test Explorer | AS 測試工具更完善 | AS Test Runner |

---

## 🎯 不同角色的推薦配置

### 初學者（剛接觸 Android 開發）

**推薦**: 最小化安裝 + GitLens

```bash
code --install-extension mathiasfrohlich.Kotlin && \
code --install-extension vscjava.vscode-java-pack && \
code --install-extension vscjava.vscode-gradle && \
code --install-extension eamodio.gitlens
```

**原因**:
- 保持簡單，專注學習
- Android Studio 處理複雜任務
- GitLens 幫助理解代碼歷史

---

### 中級開發者（1-2 年經驗）

**推薦**: 推薦安裝（8 個外掛）

```bash
# 使用上方的「推薦安裝」指令
```

**原因**:
- 平衡功能和性能
- Error Lens 提高效率
- TODO Tree 管理待辦事項

---

### 高級開發者（3+ 年經驗）

**推薦**: 推薦安裝 + 部分可選外掛

```bash
# 推薦安裝 + Git Graph + File Utils + Path Intellisense
```

**原因**:
- 需要更多工具支持複雜任務
- Git Graph 管理複雜分支
- File Utils 快速重構文件結構

---

### 團隊協作（多人項目）

**推薦**: 推薦安裝 + Better Comments

```bash
# 推薦安裝 + Better Comments
code --install-extension aaron-bond.better-comments
```

**原因**:
- GitLens 追蹤團隊成員貢獻
- Better Comments 統一註釋風格
- TODO Tree 共享任務清單

---

## 🔍 外掛使用技巧

### GitLens 高效使用

**1. 查看代碼作者**
```
將鼠標懸停在任意行上
→ 顯示最後修改者和時間
→ 點擊查看完整提交
```

**2. 對比文件歷史**
```
右鍵文件 → GitLens → Open File History
→ 查看所有修改記錄
→ 對比任意兩個版本
```

**3. 查看行歷史**
```
選中一行 → 右鍵 → GitLens → Open Line History
→ 查看這一行的所有修改
```

**4. 與 Android Studio 協同**
```
在 AS 測試時發現問題
→ 在 VSCode 中用 GitLens 查看歷史
→ 找到引入問題的提交
→ 快速定位錯誤源頭
```

---

### Error Lens 高效使用

**1. 即時發現錯誤**
```kotlin
// 錯誤會立即在行末顯示，無需鼠標懸停
val result = someFunction()  // ← Error: Unresolved reference: someFunction
```

**2. 配置錯誤級別**
```json
{
  "errorLens.enabledDiagnosticLevels": [
    "error",    // 顯示錯誤
    "warning"   // 顯示警告
    // "info" 不顯示提示，避免干擾
  ]
}
```

---

### TODO Tree 高效使用

**1. 添加結構化 TODO**
```kotlin
// TODO: [功能] 實現縮放控制
// FIXME: [性能] 修復記憶體洩漏（AS Profiler 檢測到）
// BUG: [UI] 深色模式下按鈕顏色不對
// NOTE: [重構] 這段代碼需要優化
```

**2. 在側邊欄查看**
```
點擊側邊欄的 TODO Tree 圖標
→ 按類型分組（TODO/FIXME/BUG）
→ 點擊跳轉到對應位置
```

**3. 導出任務清單**
```
右鍵 TODO Tree → Export to Markdown
→ 生成 TODO.md 文件
→ 與團隊共享
```

---

## 📝 檢查清單

### 初次安裝檢查清單

- [ ] 安裝 JDK 11+
- [ ] 設定 JAVA_HOME 環境變數
- [ ] 安裝核心外掛（3 個）
- [ ] 安裝推薦外掛（5 個）
- [ ] 創建 `.vscode/settings.json`
- [ ] 測試 Gradle 構建：`./gradlew build`
- [ ] 驗證 Git 功能：`git status`
- [ ] 重啟 VSCode 使配置生效

### 定期維護檢查清單

- [ ] 每月檢查外掛更新（不要自動更新）
- [ ] 每季度清理無用外掛
- [ ] 檢查 `.vscode/settings.json` 是否需要調整
- [ ] 驗證外掛是否影響啟動速度
- [ ] 清理 VSCode 緩存（如果變慢）

### 清理緩存指令

```bash
# Windows
%APPDATA%\Code\Cache
%APPDATA%\Code\CachedData

# 手動刪除這些文件夾，然後重啟 VSCode
```

---

## 🚀 性能優化建議

### 1. 控制外掛數量

```
✅ 好: 5-10 個精選外掛
❌ 壞: 20+ 個外掛
```

### 2. 定期禁用不用的外掛

```
Ctrl+Shift+X → 右鍵外掛 → Disable
（而不是卸載，以後可能還會用）
```

### 3. 使用外掛推薦文件

在專案根目錄創建 `.vscode/extensions.json`：

```json
{
  "recommendations": [
    "mathiasfrohlich.Kotlin",
    "vscjava.vscode-java-pack",
    "vscjava.vscode-gradle",
    "eamodio.gitlens",
    "usernamehw.errorlens",
    "Gruntfuggly.todo-tree",
    "PKief.material-icon-theme",
    "streetsidesoftware.code-spell-checker"
  ],
  "unwantedRecommendations": [
    "DiemasMichiels.emulate",
    "toroxx.android-emulator-manager",
    "vinicioslc.adb-interface-vscode"
  ]
}
```

**好處**:
- 團隊成員安裝統一外掛
- 新成員快速上手
- 避免安裝不需要的外掛

### 4. 監控啟動時間

```
Ctrl+Shift+P → Developer: Startup Performance
→ 查看哪些外掛拖慢啟動
→ 禁用耗時外掛
```

---

## 🎓 總結

### 核心原則

1. **少即是多**: 只裝真正需要的外掛
2. **輕量優先**: 避免重量級外掛
3. **職責分離**: VSCode 編碼，AS 測試
4. **保持同步**: Git 是兩個 IDE 的橋樑

### 推薦配置總結

**最佳配置（8 個外掛）**:
1. Kotlin Language ⭐⭐⭐⭐⭐
2. Extension Pack for Java ⭐⭐⭐⭐⭐
3. Gradle for Java ⭐⭐⭐⭐⭐
4. GitLens ⭐⭐⭐⭐⭐
5. Error Lens ⭐⭐⭐⭐⭐
6. TODO Tree ⭐⭐⭐⭐
7. Material Icon Theme ⭐⭐⭐
8. Code Spell Checker ⭐⭐⭐

**預期效果**:
- 啟動時間: 3-4 秒
- 記憶體占用: 200-300 MB
- 功能完整度: 90%（編碼需求）
- 與 AS 協同: 完美

### 下一步

1. **安裝外掛**: 使用「推薦安裝」指令
2. **配置設定**: 複製 `settings.json`
3. **測試功能**: 編輯代碼、提交 Git
4. **閱讀文檔**: [HYBRID_WORKFLOW_GUIDE.md](./HYBRID_WORKFLOW_GUIDE.md)
5. **開始開發**: 享受高效的混合工作流！

---

## 📚 相關文檔

- [HYBRID_WORKFLOW_GUIDE.md](./HYBRID_WORKFLOW_GUIDE.md) - 混合工作流完整指南
- [VSCODE_EXTENSIONS_GUIDE.md](./VSCODE_EXTENSIONS_GUIDE.md) - 完整外掛指南（純 VSCode 開發）
- [QUICK_START.md](./QUICK_START.md) - 專案快速入門

---

*最後更新: 2025-11-09*
*專案: SmartCameraApp1*
*模式: VSCode + Android Studio 混合工作流*
*推薦外掛數: 8 個*