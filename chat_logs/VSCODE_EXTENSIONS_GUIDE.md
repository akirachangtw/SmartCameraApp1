# VSCode 開發 Android 智慧相機應用 - 外掛安裝指南

## 📋 目錄

- [必裝外掛](#必裝外掛核心開發)
- [強烈建議外掛](#強烈建議提升效率)
- [推薦外掛](#推薦外掛增強體驗)
- [快速安裝指令](#快速安裝指令)
- [外掛配置](#外掛配置)
- [環境需求](#環境需求)
- [常見問題](#常見問題)

---

## 🔴 必裝外掛（核心開發）

### 1. Kotlin Language
- **外掛 ID**: `mathiasfrohlich.Kotlin`
- **功能**:
  - Kotlin 語法高亮
  - 代碼片段（snippets）
  - 基礎智能感知
- **安裝**:
  ```bash
  code --install-extension mathiasfrohlich.Kotlin
  ```

### 2. Android iOS Emulator
- **外掛 ID**: `DiemasMichiels.emulate`
- **功能**:
  - 在 VSCode 中啟動 Android 模擬器
  - 快速切換不同 AVD
  - 查看已連接設備
- **安裝**:
  ```bash
  code --install-extension DiemasMichiels.emulate
  ```

### 3. Gradle for Java
- **外掛 ID**: `vscjava.vscode-gradle`
- **功能**:
  - Gradle 任務管理
  - 依賴視圖
  - 構建腳本支持
- **安裝**:
  ```bash
  code --install-extension vscjava.vscode-gradle
  ```

### 4. Extension Pack for Java
- **外掛 ID**: `vscjava.vscode-java-pack`
- **功能**:
  - Java/Kotlin 語言支持
  - 調試器
  - Maven/Gradle 支持
  - 測試執行器
- **安裝**:
  ```bash
  code --install-extension vscjava.vscode-java-pack
  ```

---

## 🟡 強烈建議（提升效率）

### 5. Compose Multiplatform IDE Support
- **外掛 ID**: `JetBrains.compose-multiplatform-ide-support`
- **功能**:
  - Jetpack Compose 代碼補全
  - Composable 函數識別
  - 預覽支持（有限）
- **安裝**:
  ```bash
  code --install-extension JetBrains.compose-multiplatform-ide-support
  ```

### 6. Android Studio Emulator Manager
- **外掛 ID**: `toroxx.android-emulator-manager`
- **功能**:
  - AVD 創建和管理
  - 快速啟動模擬器
  - 設備狀態監控
- **安裝**:
  ```bash
  code --install-extension toroxx.android-emulator-manager
  ```

### 7. ADB Interface for VSCode
- **外掛 ID**: `vinicioslc.adb-interface-vscode`
- **功能**:
  - ADB 命令快捷執行
  - 設備文件瀏覽
  - 日誌查看（logcat）
  - 應用安裝/卸載
- **安裝**:
  ```bash
  code --install-extension vinicioslc.adb-interface-vscode
  ```

### 8. Error Lens
- **外掛 ID**: `usernamehw.errorlens`
- **功能**:
  - 即時顯示錯誤和警告
  - 行內錯誤提示
  - 自定義錯誤樣式
- **安裝**:
  ```bash
  code --install-extension usernamehw.errorlens
  ```

---

## 🟢 推薦外掛（增強體驗）

### 9. GitLens
- **外掛 ID**: `eamodio.gitlens`
- **功能**:
  - Git 增強功能
  - 代碼作者追蹤
  - 提交歷史可視化
- **安裝**:
  ```bash
  code --install-extension eamodio.gitlens
  ```

### 10. Code Spell Checker
- **外掛 ID**: `streetsidesoftware.code-spell-checker`
- **功能**:
  - 拼寫檢查
  - 自定義詞典
  - 多語言支持
- **安裝**:
  ```bash
  code --install-extension streetsidesoftware.code-spell-checker
  ```

### 11. Markdown All in One
- **外掛 ID**: `yzhang.markdown-all-in-one`
- **功能**:
  - Markdown 編輯增強
  - 目錄自動生成
  - 預覽功能
- **安裝**:
  ```bash
  code --install-extension yzhang.markdown-all-in-one
  ```

### 12. Material Icon Theme
- **外掛 ID**: `PKief.material-icon-theme`
- **功能**:
  - 文件和文件夾圖標美化
  - Kotlin/Android 文件識別
  - 視覺增強
- **安裝**:
  ```bash
  code --install-extension PKief.material-icon-theme
  ```

---

## 📦 快速安裝指令

### 一鍵安裝所有必裝外掛

```bash
code --install-extension mathiasfrohlich.Kotlin && \
code --install-extension DiemasMichiels.emulate && \
code --install-extension vscjava.vscode-gradle && \
code --install-extension vscjava.vscode-java-pack
```

### 一鍵安裝所有推薦外掛

```bash
code --install-extension mathiasfrohlich.Kotlin && \
code --install-extension DiemasMichiels.emulate && \
code --install-extension vscjava.vscode-gradle && \
code --install-extension vscjava.vscode-java-pack && \
code --install-extension JetBrains.compose-multiplatform-ide-support && \
code --install-extension toroxx.android-emulator-manager && \
code --install-extension vinicioslc.adb-interface-vscode && \
code --install-extension usernamehw.errorlens && \
code --install-extension eamodio.gitlens && \
code --install-extension streetsidesoftware.code-spell-checker && \
code --install-extension yzhang.markdown-all-in-one && \
code --install-extension PKief.material-icon-theme
```

### Windows PowerShell 版本

```powershell
@(
    "mathiasfrohlich.Kotlin",
    "DiemasMichiels.emulate",
    "vscjava.vscode-gradle",
    "vscjava.vscode-java-pack",
    "JetBrains.compose-multiplatform-ide-support",
    "toroxx.android-emulator-manager",
    "vinicioslc.adb-interface-vscode",
    "usernamehw.errorlens",
    "eamodio.gitlens",
    "streetsidesoftware.code-spell-checker",
    "yzhang.markdown-all-in-one",
    "PKief.material-icon-theme"
) | ForEach-Object { code --install-extension $_ }
```

---

## ⚙️ 外掛配置

### VSCode 設定（settings.json）

在專案根目錄創建 `.vscode/settings.json`：

```json
{
  // Java/Kotlin 設定
  "java.configuration.updateBuildConfiguration": "automatic",
  "java.home": "C:\\Program Files\\Java\\jdk-11",

  // Gradle 設定
  "gradle.nestedProjects": true,
  "gradle.autoDetect": "on",

  // Android SDK 路徑（請根據實際路徑修改）
  "android.sdk.path": "C:\\Users\\YourName\\AppData\\Local\\Android\\Sdk",

  // 文件關聯
  "files.associations": {
    "*.kt": "kotlin",
    "*.kts": "kotlin",
    "build.gradle": "groovy",
    "*.gradle": "groovy"
  },

  // Error Lens 設定
  "errorLens.enabledDiagnosticLevels": [
    "error",
    "warning",
    "info"
  ],

  // 代碼格式化
  "editor.formatOnSave": true,
  "editor.formatOnPaste": true,

  // Kotlin 特定設定
  "[kotlin]": {
    "editor.tabSize": 4,
    "editor.insertSpaces": true,
    "editor.defaultFormatter": "mathiasfrohlich.Kotlin"
  },

  // Git 設定
  "git.autofetch": true,
  "gitlens.codeLens.enabled": true,

  // 終端設定
  "terminal.integrated.defaultProfile.windows": "PowerShell",

  // 圖標主題
  "workbench.iconTheme": "material-icon-theme"
}
```

### 任務配置（tasks.json）

在 `.vscode/tasks.json` 中配置 Gradle 任務：

```json
{
  "version": "2.0.0",
  "tasks": [
    {
      "label": "Android: Build Debug APK",
      "type": "shell",
      "command": "./gradlew assembleDebug",
      "group": {
        "kind": "build",
        "isDefault": true
      },
      "problemMatcher": []
    },
    {
      "label": "Android: Install Debug APK",
      "type": "shell",
      "command": "./gradlew installDebug",
      "group": "build",
      "problemMatcher": []
    },
    {
      "label": "Android: Run Tests",
      "type": "shell",
      "command": "./gradlew test",
      "group": "test",
      "problemMatcher": []
    },
    {
      "label": "Android: Clean Build",
      "type": "shell",
      "command": "./gradlew clean",
      "group": "build",
      "problemMatcher": []
    },
    {
      "label": "Android: Lint Check",
      "type": "shell",
      "command": "./gradlew lint",
      "group": "test",
      "problemMatcher": []
    }
  ]
}
```

### 啟動配置（launch.json）

在 `.vscode/launch.json` 中配置調試：

```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "java",
      "name": "Attach to Android",
      "request": "attach",
      "hostName": "localhost",
      "port": 5005
    }
  ]
}
```

---

## 🔧 環境需求

### 必須安裝的軟件

1. **Java Development Kit (JDK)**
   - 版本: JDK 11 或更高
   - 下載: [Oracle JDK](https://www.oracle.com/java/technologies/downloads/) 或 [OpenJDK](https://adoptium.net/)
   - 環境變數: 設定 `JAVA_HOME`

2. **Android SDK**
   - 通過 Android Studio 安裝
   - 或使用 [Command Line Tools](https://developer.android.com/studio#command-tools)
   - 環境變數: 設定 `ANDROID_HOME` 或 `ANDROID_SDK_ROOT`

3. **Gradle**
   - 專案已包含 Gradle Wrapper（gradlew）
   - 無需單獨安裝

4. **ADB (Android Debug Bridge)**
   - 包含在 Android SDK Platform-Tools 中
   - 確保已添加到 PATH

### 環境變數設定（Windows）

```powershell
# 設定 JAVA_HOME
[System.Environment]::SetEnvironmentVariable('JAVA_HOME', 'C:\Program Files\Java\jdk-11', 'Machine')

# 設定 ANDROID_HOME
[System.Environment]::SetEnvironmentVariable('ANDROID_HOME', 'C:\Users\YourName\AppData\Local\Android\Sdk', 'Machine')

# 添加到 PATH
$path = [System.Environment]::GetEnvironmentVariable('Path', 'Machine')
$newPath = "$path;$env:JAVA_HOME\bin;$env:ANDROID_HOME\platform-tools;$env:ANDROID_HOME\tools"
[System.Environment]::SetEnvironmentVariable('Path', $newPath, 'Machine')
```

### 驗證環境

```bash
# 檢查 Java 版本
java -version

# 檢查 Gradle 版本
./gradlew --version

# 檢查 ADB
adb version

# 列出已連接設備
adb devices
```

---

## 🚀 快速開始流程

### 1. 安裝外掛
使用上方的一鍵安裝指令安裝所有外掛。

### 2. 配置 VSCode
創建 `.vscode` 目錄並添加配置文件：
```bash
mkdir .vscode
# 然後將上方的 settings.json、tasks.json、launch.json 複製進去
```

### 3. 開啟專案
```bash
cd d:\MyDocuments\DevSources\AndroidStudioProjects\SmartCameraApp1
code .
```

### 4. 同步 Gradle
- 打開命令面板（`Ctrl+Shift+P`）
- 輸入 `Gradle: Refresh Gradle Project`
- 等待依賴下載完成

### 5. 啟動模擬器
- 打開命令面板（`Ctrl+Shift+P`）
- 輸入 `Emulate: Run Emulator`
- 選擇 AVD

### 6. 構建並運行
```bash
# 構建 Debug APK
./gradlew assembleDebug

# 安裝到設備
./gradlew installDebug
```

或使用快捷鍵：
- `Ctrl+Shift+B` - 構建專案
- `F5` - 啟動調試

---

## ❓ 常見問題

### Q1: VSCode 能完全替代 Android Studio 嗎？

**不能完全替代**，但可以處理大部分開發任務：

**VSCode 可以做：**
- ✅ 編寫和編輯 Kotlin 代碼
- ✅ Gradle 構建和依賴管理
- ✅ Git 版本控制
- ✅ 運行模擬器和真機調試
- ✅ 查看日誌（logcat）
- ✅ 文件和資源編輯

**VSCode 無法做（需要 Android Studio）：**
- ❌ 視覺化佈局編輯器（Layout Editor）
- ❌ Jetpack Compose 預覽（Preview）
- ❌ APK 分析器（APK Analyzer）
- ❌ 網絡檢查器（Network Inspector）
- ❌ 性能分析器（Profiler）
- ❌ 設備文件瀏覽器（Device File Explorer）
- ❌ 資源管理器（Resource Manager）

**建議做法：**
- 日常編碼使用 VSCode（輕量、快速）
- 複雜調試和性能分析使用 Android Studio

---

### Q2: Kotlin 代碼補全不準確怎麼辦？

**解決方案：**

1. 安裝 IntelliJ IDEA Community Edition（免費）
2. 使用 IntelliJ IDEA 的 Kotlin Language Server
3. 在 VSCode 中配置：
   ```json
   {
     "kotlin.languageServer.enabled": true,
     "kotlin.languageServer.path": "C:\\Program Files\\JetBrains\\IntelliJ IDEA Community Edition\\bin\\kotlin-language-server"
   }
   ```

---

### Q3: Gradle 構建很慢怎麼辦？

**優化建議：**

1. 啟用 Gradle Daemon：
   在 `gradle.properties` 中添加：
   ```properties
   org.gradle.daemon=true
   org.gradle.parallel=true
   org.gradle.caching=true
   org.gradle.configureondemand=true
   ```

2. 增加 Gradle 記憶體：
   ```properties
   org.gradle.jvmargs=-Xmx4096m -XX:MaxMetaspaceSize=512m
   ```

3. 使用本地 Maven 倉庫快取

---

### Q4: 如何查看 Android 日誌（logcat）？

**方法 1: 使用 ADB Interface 外掛**
- 打開命令面板（`Ctrl+Shift+P`）
- 輸入 `ADB: Logcat`

**方法 2: 使用終端**
```bash
# 查看所有日誌
adb logcat

# 過濾應用日誌
adb logcat | findstr "SmartCameraApp1"

# 只顯示錯誤
adb logcat *:E
```

**方法 3: 創建任務**
在 `tasks.json` 中添加：
```json
{
  "label": "Android: Show Logcat",
  "type": "shell",
  "command": "adb logcat | findstr 'SmartCameraApp1'",
  "isBackground": true
}
```

---

### Q5: 如何在 VSCode 中運行測試？

**單元測試：**
```bash
./gradlew test
```

**Android 測試（需要模擬器或真機）：**
```bash
./gradlew connectedAndroidTest
```

**使用 VSCode Test Explorer：**
1. 安裝 `Java Test Runner` 外掛（已包含在 Extension Pack for Java 中）
2. 在側邊欄點擊 Testing 圖標
3. 點擊測試文件旁的 Run 按鈕

---

### Q6: 如何在 VSCode 中重構代碼？

Kotlin 外掛的重構功能有限，建議：

**基本重構（VSCode 可做）：**
- 重命名（`F2`）
- 提取變數
- 查找引用（`Shift+F12`）
- 格式化代碼（`Shift+Alt+F`）

**複雜重構（需要 IntelliJ IDEA 或 Android Studio）：**
- 提取函數
- 提取介面
- 內聯變數
- 改變簽名

---

### Q7: 如何配置代碼格式化？

**使用 ktlint（推薦）：**

1. 在 `app/build.gradle.kts` 中添加：
   ```kotlin
   plugins {
       id("org.jlleitschuh.gradle.ktlint") version "11.6.1"
   }
   ```

2. 運行格式化：
   ```bash
   ./gradlew ktlintFormat
   ```

3. 配置 VSCode 自動格式化：
   ```json
   {
     "editor.formatOnSave": true,
     "[kotlin]": {
       "editor.defaultFormatter": "mathiasfrohlich.Kotlin"
     }
   }
   ```

---

### Q8: 如何使用斷點調試？

**方法 1: 使用 Java 調試器**

1. 在代碼中設置斷點（點擊行號左側）
2. 在終端運行：
   ```bash
   ./gradlew installDebug
   adb shell am start -D -n local.mylab.smartcameraapp1/.MainActivity
   ```
3. 在 VSCode 中按 `F5` 啟動調試器
4. 在終端運行：
   ```bash
   jdb -attach localhost:5005
   ```

**方法 2: 使用 Android Studio 調試**
（VSCode 的 Android 調試支持有限，建議使用 Android Studio）

---

## 📚 其他推薦外掛（可選）

### 未來功能擴展

如果計劃實現以下功能，可安裝對應外掛：

**API 開發：**
```bash
code --install-extension humao.rest-client  # REST Client
code --install-extension rangav.vscode-thunder-client  # Thunder Client
```

**資料庫：**
```bash
code --install-extension alexcvzz.vscode-sqlite  # SQLite Viewer
code --install-extension cweijan.vscode-database-client2  # Database Client
```

**Docker 容器化：**
```bash
code --install-extension ms-azuretools.vscode-docker  # Docker
```

**JSON 處理：**
```bash
code --install-extension ZainChen.json  # JSON Tools
```

**XML 編輯（Android 資源文件）：**
```bash
code --install-extension redhat.vscode-xml  # XML Tools
```

**TODO 追蹤：**
```bash
code --install-extension Gruntfuggly.todo-tree  # TODO Tree
```

**代碼複雜度分析：**
```bash
code --install-extension kisstkondoros.vscode-codemetrics  # CodeMetrics
```

---

## 🎯 開發工作流建議

### 日常開發流程

1. **早上開始工作**
   - 打開 VSCode：`code .`
   - 拉取最新代碼：`git pull`
   - 啟動模擬器：`Ctrl+Shift+P` → `Emulate: Run Emulator`

2. **編寫代碼**
   - 使用 Kotlin 外掛的智能補全
   - 使用 Error Lens 即時查看錯誤
   - 定期保存和格式化（`Ctrl+S` + `Shift+Alt+F`）

3. **構建和測試**
   - 構建：`Ctrl+Shift+B`
   - 運行測試：`./gradlew test`
   - 安裝到設備：`./gradlew installDebug`

4. **調試問題**
   - 查看 logcat：`adb logcat`
   - 使用 Android Studio Profiler（如需）

5. **提交代碼**
   - 查看變更：`Git: Changes`（側邊欄）
   - 提交：`Git: Commit`
   - 推送：`Git: Push`

### VSCode vs Android Studio 分工

| 任務類型 | 推薦工具 | 原因 |
|---------|---------|------|
| 編寫 Kotlin 代碼 | VSCode | 快速、輕量 |
| 編輯 XML 佈局 | Android Studio | 視覺化編輯器 |
| Gradle 構建 | VSCode | 終端操作方便 |
| 調試崩潰 | Android Studio | 強大的調試器 |
| 性能分析 | Android Studio | 專業分析工具 |
| Git 操作 | VSCode | GitLens 增強 |
| 查看日誌 | VSCode | 終端方便 |
| 資源管理 | Android Studio | 專用工具 |

---

## 🔄 定期更新

### 檢查外掛更新

```bash
# 列出已安裝外掛
code --list-extensions

# 更新所有外掛
code --update-extensions
```

### 推薦更新頻率

- **外掛**: 每月檢查一次
- **Android SDK**: 每季度更新
- **Gradle**: 跟隨專案需求更新
- **Kotlin**: 跟隨專案 Kotlin 版本

---

## 📞 獲取幫助

### 官方資源

- [Kotlin 官方文檔](https://kotlinlang.org/docs/home.html)
- [Android 開發者文檔](https://developer.android.com/)
- [Jetpack Compose 文檔](https://developer.android.com/jetpack/compose)
- [VSCode Android 開發指南](https://code.visualstudio.com/docs/languages/java)

### 社群資源

- [Stack Overflow - Android](https://stackoverflow.com/questions/tagged/android)
- [r/androiddev](https://www.reddit.com/r/androiddev/)
- [Kotlin Slack](https://kotlinlang.slack.com/)

---

## ✅ 總結

### 最小化安裝（僅核心功能）

```bash
code --install-extension mathiasfrohlich.Kotlin && \
code --install-extension vscjava.vscode-java-pack && \
code --install-extension vscjava.vscode-gradle
```

### 推薦安裝（最佳體驗）

使用本文檔中的「一鍵安裝所有推薦外掛」指令。

### 記住

- ✅ VSCode 適合日常編碼和輕量級任務
- ✅ Android Studio 適合複雜調試和專業工具
- ✅ 兩者結合使用效率最高
- ✅ 定期更新外掛和工具

---

*最後更新: 2025-11-09*
*專案: SmartCameraApp1*
*版本: Phase 1*