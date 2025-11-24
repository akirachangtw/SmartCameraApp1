# VSCode + Android Studio 混合開發工作流指南

## 📋 目錄

- [工作流概述](#工作流概述)
- [工具職責分工](#工具職責分工)
- [開發流程](#開發流程)
- [VSCode 配置優化](#vscode-配置優化)
- [Android Studio 配置優化](#android-studio-配置優化)
- [文件同步注意事項](#文件同步注意事項)
- [實戰案例](#實戰案例)
- [常見問題](#常見問題)
- [最佳實踐](#最佳實踐)

---

## 🎯 工作流概述

### 核心理念

**VSCode**: 快速、輕量、專注編碼
**Android Studio**: 專業、完整、深度測試

```
┌─────────────────────────────────────────────────────────┐
│                    開發循環                              │
│                                                          │
│  VSCode (編碼)  →  Git Commit  →  Android Studio (測試)  │
│       ↑                                    ↓             │
│       └──────────── 修復問題 ←──────────────┘             │
└─────────────────────────────────────────────────────────┘
```

### 為什麼這樣做？

✅ **優勢**
- VSCode 啟動快（2-3秒 vs 30-60秒）
- 記憶體占用小（200MB vs 2-3GB）
- 編輯響應快速
- 多專案切換方便
- Git 操作更直觀（GitLens）
- 終端操作更靈活

✅ **保留 Android Studio 的強項**
- 完整的設備管理
- 專業調試工具
- 性能分析器
- Layout Inspector
- Network Inspector
- APK Analyzer

---

## 🔧 工具職責分工

### VSCode 負責的任務（80% 時間）

| 任務類型 | 具體內容 | 快捷鍵 |
|---------|---------|--------|
| **代碼編寫** | Kotlin/Java 代碼編輯 | - |
| **代碼重構** | 重命名、格式化、查找引用 | `F2`, `Shift+Alt+F` |
| **Git 操作** | Commit, Push, Pull, Merge | `Ctrl+Shift+G` |
| **快速構建** | Gradle 構建、清理 | `Ctrl+Shift+B` |
| **代碼審查** | Diff 檢視、Blame 查看 | GitLens 面板 |
| **終端操作** | ADB 命令、Gradle 任務 | `Ctrl+`` |
| **文檔編輯** | README, 設計文檔 | - |
| **快速測試** | 單元測試運行 | `./gradlew test` |

### Android Studio 負責的任務（20% 時間）

| 任務類型 | 具體內容 | 何時使用 |
|---------|---------|---------|
| **深度調試** | 斷點調試、變量檢視 | 遇到複雜 Bug 時 |
| **性能分析** | CPU、記憶體、網絡分析 | 性能優化階段 |
| **UI 檢視** | Layout Inspector | UI 佈局問題 |
| **設備管理** | AVD 創建、配置 | 需要新測試環境 |
| **APK 分析** | APK 大小、資源分析 | 發布前優化 |
| **Compose 預覽** | 實時 UI 預覽 | UI 開發階段 |
| **Logcat 過濾** | 高級日誌過濾 | 複雜日誌分析 |
| **Lint 檢查** | 代碼質量檢查 | 代碼審查時 |
| **資源管理** | 圖片、字串資源管理 | 添加新資源時 |

---

## 🚀 開發流程

### 典型的一天工作流

#### 早上 9:00 - 開始工作（VSCode）

```bash
# 1. 打開專案
cd d:\MyDocuments\DevSources\AndroidStudioProjects\SmartCameraApp1
code .

# 2. 同步代碼
git pull origin master

# 3. 檢查狀態
git status

# 4. 查看今天的任務
# 打開 TODO.md 或使用 TODO Tree 外掛
```

#### 9:15 - 12:00 - 功能開發（VSCode）

**步驟 1: 創建功能分支**
```bash
git checkout -b feature/add-zoom-control
```

**步驟 2: 編寫代碼**
- 打開相關文件（例如：`CameraViewModel.kt`）
- 使用 Kotlin 外掛的智能補全
- 使用 Error Lens 即時查看錯誤
- 定期保存和格式化

**步驟 3: 快速驗證**
```bash
# 構建檢查語法錯誤
./gradlew build

# 運行單元測試
./gradlew test
```

**步驟 4: 提交代碼**
```bash
git add .
git commit -m "feat: add zoom control to camera"
```

#### 12:00 - 13:00 - 午休

#### 13:00 - 測試驗證（Android Studio）

**步驟 1: 打開 Android Studio**
- 啟動 Android Studio
- 等待 Gradle 同步完成（自動檢測 VSCode 的更改）

**步驟 2: 運行應用**
```
點擊 Run 按鈕 (Shift+F10)
或
點擊 Debug 按鈕 (Shift+F9)
```

**步驟 3: 深度測試**
1. **功能測試**
   - 測試縮放功能是否正常
   - 測試邊界情況（最大/最小縮放）
   - 測試與其他功能的交互

2. **性能測試**
   - 打開 Profiler（View → Tool Windows → Profiler）
   - 監控 CPU 和記憶體使用
   - 檢查是否有記憶體洩漏

3. **UI 檢查**
   - 使用 Layout Inspector 檢查 UI 層級
   - 驗證不同屏幕尺寸的顯示效果
   - 檢查深色/淺色模式

4. **日誌分析**
   - 打開 Logcat
   - 過濾應用日誌：`package:mine`
   - 檢查是否有警告或錯誤

**步驟 4: 記錄問題**
- 如果發現問題，在 VSCode 中創建 TODO 或 Issue
- 截圖保存到 `docs/screenshots/` 目錄

#### 14:00 - 修復問題（返回 VSCode）

```bash
# 回到 VSCode
# 根據測試發現的問題進行修復
# 再次 commit
git commit -m "fix: resolve zoom boundary issue"
```

#### 14:30 - 再次測試（Android Studio）

- 驗證修復是否有效
- 進行回歸測試
- 確保沒有引入新問題

#### 15:00 - 功能完成

```bash
# 在 VSCode 中推送代碼
git push origin feature/add-zoom-control

# 創建 Pull Request（如果使用 GitHub/GitLab）
gh pr create --title "Add zoom control" --body "..."
```

---

## ⚙️ VSCode 配置優化

### 專為混合工作流優化的配置

創建或更新 `.vscode/settings.json`：

```json
{
  // ===== Java/Kotlin 開發 =====
  "java.configuration.updateBuildConfiguration": "automatic",
  "java.home": "C:\\Program Files\\Java\\jdk-11",
  "java.jdt.ls.vmargs": "-Xmx2G",

  // ===== Gradle 設定 =====
  "gradle.nestedProjects": true,
  "gradle.autoDetect": "on",

  // ===== 文件監控（重要！確保 Android Studio 的更改能被檢測到） =====
  "files.watcherExclude": {
    "**/.gradle/**": true,
    "**/build/**": true,
    "**/.idea/**": false  // 監控 .idea 確保能檢測 Android Studio 的更改
  },

  // ===== 自動保存（避免兩邊編輯衝突） =====
  "files.autoSave": "afterDelay",
  "files.autoSaveDelay": 1000,

  // ===== Git 設定 =====
  "git.autofetch": true,
  "git.confirmSync": false,
  "git.enableSmartCommit": true,
  "gitlens.codeLens.enabled": true,
  "gitlens.currentLine.enabled": true,

  // ===== 編輯器設定 =====
  "editor.formatOnSave": true,
  "editor.formatOnPaste": false,
  "editor.codeActionsOnSave": {
    "source.organizeImports": "explicit"
  },

  // ===== Kotlin 特定設定 =====
  "[kotlin]": {
    "editor.tabSize": 4,
    "editor.insertSpaces": true,
    "editor.rulers": [100, 120],
    "editor.wordWrap": "off"
  },

  // ===== 終端設定 =====
  "terminal.integrated.defaultProfile.windows": "PowerShell",
  "terminal.integrated.fontSize": 13,

  // ===== 任務自動檢測 =====
  "task.autoDetect": "on",

  // ===== 排除不必要的文件 =====
  "files.exclude": {
    "**/.gradle": true,
    "**/build": true,
    "**/.DS_Store": true,
    "**/local.properties": true
  },

  // ===== 搜索排除 =====
  "search.exclude": {
    "**/build": true,
    "**/.gradle": true,
    "**/node_modules": true
  },

  // ===== Error Lens 設定 =====
  "errorLens.enabledDiagnosticLevels": [
    "error",
    "warning"
  ],
  "errorLens.excludeBySource": [
    "cSpell"
  ],

  // ===== TODO 高亮 =====
  "todo-tree.general.tags": [
    "TODO",
    "FIXME",
    "BUG",
    "HACK",
    "XXX",
    "[ ]",
    "[x]"
  ],
  "todo-tree.highlights.defaultHighlight": {
    "foreground": "white",
    "background": "orange",
    "icon": "check",
    "type": "text"
  }
}
```

### 創建自定義任務（.vscode/tasks.json）

```json
{
  "version": "2.0.0",
  "tasks": [
    {
      "label": "🔨 Quick Build",
      "type": "shell",
      "command": "./gradlew assembleDebug",
      "group": {
        "kind": "build",
        "isDefault": true
      },
      "presentation": {
        "reveal": "always",
        "panel": "dedicated"
      },
      "problemMatcher": []
    },
    {
      "label": "🧪 Run Unit Tests",
      "type": "shell",
      "command": "./gradlew test",
      "group": "test",
      "presentation": {
        "reveal": "always"
      }
    },
    {
      "label": "🧹 Clean Build",
      "type": "shell",
      "command": "./gradlew clean build",
      "group": "build"
    },
    {
      "label": "📱 Install to Device",
      "type": "shell",
      "command": "./gradlew installDebug",
      "group": "build",
      "dependsOn": ["🔨 Quick Build"]
    },
    {
      "label": "🔍 Lint Check",
      "type": "shell",
      "command": "./gradlew lint",
      "group": "test"
    },
    {
      "label": "📊 Dependency Report",
      "type": "shell",
      "command": "./gradlew dependencies > dependencies.txt",
      "group": "none"
    },
    {
      "label": "📝 Show Logcat",
      "type": "shell",
      "command": "adb logcat | findstr SmartCameraApp1",
      "isBackground": true,
      "presentation": {
        "reveal": "always",
        "panel": "dedicated"
      }
    },
    {
      "label": "🚀 Build + Install + Log",
      "dependsOrder": "sequence",
      "dependsOn": [
        "🔨 Quick Build",
        "📱 Install to Device",
        "📝 Show Logcat"
      ],
      "group": "build"
    }
  ]
}
```

### 創建代碼片段（.vscode/kotlin.code-snippets）

```json
{
  "Composable Function": {
    "prefix": "comp",
    "body": [
      "@Composable",
      "fun ${1:FunctionName}(",
      "    ${2:// parameters}",
      ") {",
      "    ${0:// body}",
      "}"
    ],
    "description": "Create a Composable function"
  },
  "ViewModel with State": {
    "prefix": "viewmodel",
    "body": [
      "@HiltViewModel",
      "class ${1:Name}ViewModel @Inject constructor(",
      "    ${2:// dependencies}",
      ") : ViewModel() {",
      "",
      "    private val _state = MutableStateFlow(${1:Name}State())",
      "    val state: StateFlow<${1:Name}State> = _state.asStateFlow()",
      "",
      "    ${0:// functions}",
      "}"
    ],
    "description": "Create a ViewModel with StateFlow"
  },
  "Repository Implementation": {
    "prefix": "repo",
    "body": [
      "class ${1:Name}RepositoryImpl @Inject constructor(",
      "    ${2:// dependencies}",
      ") : ${1:Name}Repository {",
      "",
      "    override suspend fun ${3:methodName}(): Result<${4:Type}> {",
      "        return try {",
      "            ${0:// implementation}",
      "            Result.success(value)",
      "        } catch (e: Exception) {",
      "            Result.failure(e)",
      "        }",
      "    }",
      "}"
    ],
    "description": "Create a Repository implementation"
  },
  "Hilt Module": {
    "prefix": "hiltmodule",
    "body": [
      "@Module",
      "@InstallIn(${1:SingletonComponent}::class)",
      "object ${2:Name}Module {",
      "",
      "    @Provides",
      "    @Singleton",
      "    fun provide${3:Dependency}(",
      "        ${4:// parameters}",
      "    ): ${3:Dependency} {",
      "        ${0:// implementation}",
      "    }",
      "}"
    ],
    "description": "Create a Hilt module"
  }
}
```

---

## 🎨 Android Studio 配置優化

### 專為測試優化的配置

#### 1. 啟用自動導入

```
Settings → Editor → General → Auto Import
✅ Add unambiguous imports on the fly
✅ Optimize imports on the fly
```

#### 2. 配置 Logcat 過濾器

創建自定義過濾器：

```
Name: App Only
Expression: package:mine level:debug
```

```
Name: Errors Only
Expression: package:mine level:error
```

```
Name: Camera Logs
Expression: tag:Camera* level:debug
```

#### 3. 設置快捷鍵

```
File → Settings → Keymap

推薦設置:
- Run: Shift+F10
- Debug: Shift+F9
- Stop: Ctrl+F2
- Logcat: Alt+6
- Profiler: Alt+7
- Layout Inspector: Alt+8
```

#### 4. 配置 Run Configuration

```
Run → Edit Configurations → Android App → app

General:
✅ Launch: Default Activity
✅ Deploy: APK from app bundle
✅ Installation Options: Install and launch activity

Debugger:
Debug type: Auto
✅ Show static fields
✅ Enable alternative view
```

#### 5. 設置性能分析器

```
File → Settings → Build, Execution, Deployment → Profiler

✅ Enable advanced profiling
✅ Enable live allocation tracking
Memory Heap Dump Directory: ./profiler-dumps
```

---

## 🔄 文件同步注意事項

### 避免衝突的關鍵規則

#### ✅ 安全做法

1. **單一編輯器原則**
   - 同一時間只在一個 IDE 中編輯代碼
   - 編輯完畢後保存並關閉文件

2. **切換前的檢查清單**
   ```bash
   # 從 VSCode 切換到 Android Studio 前：
   1. 保存所有文件（Ctrl+K S）
   2. 關閉所有編輯器標籤
   3. 提交或暫存更改（可選）
   4. 檢查 Git 狀態
   ```

3. **使用 Git 作為同步工具**
   ```bash
   # VSCode 開發完成後
   git add .
   git commit -m "feat: complete feature X"

   # 切換到 Android Studio
   # Android Studio 會自動檢測並提示重新載入
   ```

4. **避免同時打開**
   - 不要同時打開兩個 IDE 編輯同一個文件
   - 如果需要參考，使用只讀模式

#### ❌ 危險操作

1. ❌ 同時在兩個 IDE 中編輯同一文件
2. ❌ 不保存就切換 IDE
3. ❌ 忽略 IDE 的「文件已更改」警告
4. ❌ 在一個 IDE 中進行重構，在另一個中繼續編輯

### Gradle 同步管理

#### VSCode 中的 Gradle 操作

```bash
# 清理構建
./gradlew clean

# 同步依賴（相當於 Android Studio 的 Sync）
./gradlew --refresh-dependencies

# 構建專案
./gradlew build

# 查看依賴樹
./gradlew :app:dependencies
```

#### Android Studio 中的 Gradle 同步

```
Tools → Android → Sync Project with Gradle Files
快捷鍵: Ctrl+Shift+O (自定義)
```

**重要提示**：
- 在 VSCode 中修改 `build.gradle.kts` 後
- 切換到 Android Studio 時會自動提示同步
- 點擊「Sync Now」即可

---

## 💡 實戰案例

### 案例 1: 新增縮放控制功能

#### 階段 1: 規劃（VSCode）

```bash
# 1. 創建功能分支
git checkout -b feature/camera-zoom-control

# 2. 創建任務清單
# 在 TODO.md 中添加：
# - [ ] 在 CameraViewModel 中添加縮放狀態
# - [ ] 實現縮放邏輯
# - [ ] 添加 UI 滑桿組件
# - [ ] 測試縮放功能
```

#### 階段 2: 實現（VSCode）

**2.1 修改 ViewModel**

打開 `CameraViewModel.kt`：

```kotlin
// 添加縮放狀態
private val _zoomRatio = MutableStateFlow(1f)
val zoomRatio: StateFlow<Float> = _zoomRatio.asStateFlow()

// 添加縮放函數
fun setZoomRatio(ratio: Float) {
    viewModelScope.launch {
        _zoomRatio.value = ratio.coerceIn(1f, maxZoomRatio)
        // 更新相機縮放
        cameraController?.setZoomRatio(ratio)
    }
}
```

**2.2 快速構建測試**

```bash
# 在 VSCode 終端中
./gradlew assembleDebug

# 檢查編譯錯誤
# 修復後再次構建
```

**2.3 提交代碼**

```bash
git add .
git commit -m "feat: add zoom state and logic to ViewModel"
```

#### 階段 3: UI 實現（VSCode）

打開 `CameraScreen.kt`：

```kotlin
@Composable
fun ZoomControl(
    currentZoom: Float,
    onZoomChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text("縮放: ${String.format("%.1fx", currentZoom)}")
        Slider(
            value = currentZoom,
            onValueChange = onZoomChange,
            valueRange = 1f..10f,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
```

```bash
git commit -m "feat: add zoom control UI component"
```

#### 階段 4: 深度測試（Android Studio）

**4.1 啟動 Android Studio**

```
1. File → Sync Project with Gradle Files（等待同步）
2. Run → Debug 'app' (Shift+F9)
3. 等待應用安裝並啟動
```

**4.2 功能測試**

```
測試項目：
✅ 滑桿是否正常顯示
✅ 滑動時數值是否更新
✅ 縮放是否實際生效
✅ 邊界值測試（1x, 10x）
✅ 快速滑動是否流暢
```

**4.3 性能測試**

```
1. 打開 Profiler (Alt+7)
2. 開始錄製
3. 操作縮放滑桿
4. 停止錄製
5. 檢查：
   - CPU 使用率
   - 記憶體分配
   - 幀率（是否穩定 60fps）
```

**4.4 UI 檢查**

```
1. Tools → Layout Inspector
2. 查看 ZoomControl 的層級結構
3. 檢查 padding/margin 是否正確
4. 測試不同屏幕方向（旋轉設備）
```

**4.5 日誌分析**

```
1. 打開 Logcat (Alt+6)
2. 應用過濾器：package:mine
3. 查找警告或錯誤
4. 確認沒有 "WARN" 或 "ERROR" 級別的日誌
```

#### 階段 5: 問題修復（返回 VSCode）

發現問題：快速滑動時有輕微卡頓

```kotlin
// 在 VSCode 中修改
fun setZoomRatio(ratio: Float) {
    viewModelScope.launch {
        // 添加防抖邏輯
        delay(16) // 約 60fps 的一幀
        _zoomRatio.value = ratio.coerceIn(1f, maxZoomRatio)
        cameraController?.setZoomRatio(ratio)
    }
}
```

```bash
git commit -m "fix: add debounce to zoom control for smoother performance"
```

#### 階段 6: 回歸測試（Android Studio）

```
1. 再次運行應用
2. 驗證卡頓問題是否解決
3. 進行完整的回歸測試
4. 確認沒有引入新問題
```

#### 階段 7: 完成（VSCode）

```bash
# 推送到遠程
git push origin feature/camera-zoom-control

# 創建 Pull Request
gh pr create --title "Add camera zoom control" --body "..."
```

---

### 案例 2: 修復權限問題 Bug

#### 問題報告

用戶反饋：在 Android 14 上，選擇「選擇照片」後，應用仍然顯示需要權限。

#### 調試流程（Android Studio 為主）

**1. 重現問題**

```
1. 啟動 Android Studio
2. 選擇 Android 14 (API 34) 模擬器
3. Run → Debug 'app'
4. 在權限對話框中選擇「選擇照片」
5. 觀察應用行為
```

**2. 設置斷點調試**

在 Android Studio 中：

```kotlin
// 在 PermissionUtils.kt 的 hasPartialMediaAccess 函數設置斷點
fun hasPartialMediaAccess(
    grantedPermissions: Map<String, Boolean>
): Boolean {
    // 斷點在這裡 ←
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        return false
    }
    // ...
}
```

**3. 查看變量值**

在 Debug 面板中檢查：
```
grantedPermissions = {
  READ_MEDIA_IMAGES = false,
  READ_MEDIA_VIDEO = false,
  READ_MEDIA_VISUAL_USER_SELECTED = true
}

hasVisualUserSelected = true  // ✅
hasFullImageAccess = false    // ✅
hasFullVideoAccess = false    // ✅

返回值 = true  // ✅ 正確
```

**4. 繼續追蹤**

在 `CameraViewModel.kt` 中設置斷點：

```kotlin
// 發現問題：邏輯判斷錯誤
if (hasFullMediaAccess(permissions)) {
    // 只檢查了完整權限，沒有處理部分權限
    _state.update { it.copy(hasPermission = true) }
}
```

**5. 記錄問題**

在 Android Studio 中記下問題位置，然後切換到 VSCode 修復。

#### 修復階段（VSCode）

```bash
# 創建 bugfix 分支
git checkout -b bugfix/partial-permission-handling
```

在 VSCode 中修改 `CameraViewModel.kt`：

```kotlin
// 修改前
if (hasFullMediaAccess(permissions)) {
    _state.update { it.copy(hasPermission = true) }
}

// 修改後
if (hasFullMediaAccess(permissions) || hasPartialMediaAccess(permissions)) {
    _state.update { it.copy(hasPermission = true) }
}
```

```bash
git commit -m "fix: handle partial media access permission on Android 14"
```

#### 驗證階段（Android Studio）

```
1. 重新運行應用
2. 再次選擇「選擇照片」
3. 驗證應用正常工作
4. 測試邊界情況：
   - 拒絕所有權限
   - 只選擇 1 張照片
   - 選擇多張照片
   - 後續修改選擇的照片
```

---

## 🎓 最佳實踐

### 1. 代碼編寫時（VSCode）

✅ **DO**
- 頻繁保存（Ctrl+S）
- 使用 Git 頻繁提交（小步快跑）
- 運行快速構建驗證語法
- 使用代碼片段提高效率
- 保持終端開啟（快速執行命令）
- 使用 GitLens 查看代碼歷史

❌ **DON'T**
- 不要一次性改動太多文件
- 不要跳過編譯檢查就切換到測試
- 不要忽略 Error Lens 的警告
- 不要在 VSCode 中做複雜的重構

### 2. 測試驗證時（Android Studio）

✅ **DO**
- 使用專業工具（Profiler, Layout Inspector）
- 設置有意義的斷點
- 保存 Logcat 日誌到文件
- 使用多種設備/API 級別測試
- 記錄性能基準數據
- 截圖保存問題現場

❌ **DON'T**
- 不要在 Android Studio 中編輯大量代碼
- 不要跳過性能測試
- 不要忽略 Lint 警告
- 不要只在一個設備上測試

### 3. 工具切換時

✅ **DO**
- 保存所有文件
- 檢查 Git 狀態
- 等待 Gradle 同步完成
- 確認文件自動重新載入
- 關閉不必要的標籤頁

❌ **DON'T**
- 不要在未保存的情況下切換
- 不要同時在兩個 IDE 中編輯
- 不要忽略「文件已更改」提示
- 不要強制覆蓋他人更改

### 4. Git 工作流

✅ **DO**
```bash
# 開發新功能
git checkout -b feature/feature-name

# 頻繁提交（在 VSCode 中）
git add .
git commit -m "feat: add feature X"

# 測試通過後推送
git push origin feature/feature-name

# 創建 PR
gh pr create
```

❌ **DON'T**
- 不要直接在 master 上開發
- 不要積累大量更改才提交
- 不要忘記寫有意義的 commit message
- 不要跳過 code review

---

## 📊 效率對比

### 任務執行時間對比

| 任務 | VSCode | Android Studio | 節省時間 |
|-----|--------|----------------|---------|
| 啟動 IDE | 2-3 秒 | 30-60 秒 | 92% |
| 打開大型文件 | <1 秒 | 2-5 秒 | 60% |
| 代碼補全響應 | <100ms | <200ms | 50% |
| Git Commit | 5 秒 | 10 秒 | 50% |
| Gradle 構建 | 30 秒 | 30 秒 | 0% |
| 切換分支 | 1 秒 | 3-5 秒 | 67% |

### 記憶體使用對比

| 工具 | 閒置 | 活躍開發 | 高峰 |
|-----|------|---------|------|
| VSCode | 200MB | 400MB | 600MB |
| Android Studio | 1.5GB | 2.5GB | 4GB |

### 混合工作流效率

假設一天 8 小時工作：

```
傳統方式（僅 Android Studio）:
- 啟動/重啟時間: 5 分鐘
- IDE 卡頓等待: 20 分鐘
- 記憶體不足導致的效率損失: 15 分鐘
總浪費時間: ~40 分鐘/天

混合方式（VSCode + Android Studio）:
- VSCode 編碼: 6 小時（快速流暢）
- Android Studio 測試: 2 小時（專注測試）
- 切換損耗: 5 分鐘
總浪費時間: ~5 分鐘/天

每天節省: 35 分鐘
每月節省: ~12 小時
每年節省: ~145 小時（約 18 個工作日）
```

---

## 🔧 故障排除

### 問題 1: Android Studio 沒有檢測到 VSCode 的更改

**症狀**: 在 VSCode 中保存文件後，Android Studio 沒有提示重新載入

**解決方案**:
```
1. 在 Android Studio 中手動重新載入：
   File → Reload All from Disk (Ctrl+Alt+Y)

2. 檢查 Android Studio 設置：
   Settings → Appearance & Behavior → System Settings
   ✅ Synchronize files on frame or editor tab activation
   ✅ Save files on frame deactivation

3. 重新同步 Gradle：
   File → Sync Project with Gradle Files
```

### 問題 2: VSCode 無法識別新添加的依賴

**症狀**: 在 `build.gradle.kts` 中添加依賴後，VSCode 提示類找不到

**解決方案**:
```bash
# 1. 刷新 Gradle 依賴
./gradlew --refresh-dependencies

# 2. 重新構建專案
./gradlew clean build

# 3. 重啟 Java Language Server
Ctrl+Shift+P → Java: Clean Java Language Server Workspace
```

### 問題 3: Git 衝突（兩個 IDE 同時編輯）

**症狀**: Git 提示文件有衝突

**解決方案**:
```bash
# 1. 檢查狀態
git status

# 2. 使用 VSCode 的衝突解決工具
# VSCode 會自動高亮衝突區域
# 點擊「Accept Current Change」或「Accept Incoming Change」

# 3. 如果衝突複雜，使用外部工具
git mergetool

# 4. 完成後提交
git add .
git commit -m "resolve merge conflict"
```

### 問題 4: Gradle 同步失敗

**症狀**: 在任一 IDE 中 Gradle 同步失敗

**解決方案**:
```bash
# 1. 清理 Gradle 緩存
./gradlew clean
./gradlew --stop

# 2. 刪除緩存目錄
rm -rf .gradle
rm -rf build

# 3. 重新下載依賴
./gradlew --refresh-dependencies

# 4. 重新構建
./gradlew build

# 5. 如果還是失敗，檢查網絡連接和代理設置
```

### 問題 5: 模擬器無法連接

**症狀**: VSCode 中執行 `adb devices` 看不到設備

**解決方案**:
```bash
# 1. 重啟 ADB 服務
adb kill-server
adb start-server

# 2. 檢查模擬器狀態
adb devices -l

# 3. 如果模擬器在 Android Studio 中啟動
# 確保 ADB 端口正確（通常是 5037）

# 4. 檢查 ANDROID_SDK_ROOT 環境變數
echo $env:ANDROID_SDK_ROOT  # PowerShell
```

---

## 📝 檢查清單

### 每日開始檢查清單

- [ ] 同步代碼：`git pull`
- [ ] 檢查分支：`git branch`
- [ ] 查看今日任務
- [ ] 啟動 VSCode
- [ ] 測試 Gradle 構建：`./gradlew build`

### VSCode → Android Studio 切換清單

- [ ] 保存所有文件（Ctrl+K S）
- [ ] 提交或暫存更改
- [ ] 檢查 Git 狀態：`git status`
- [ ] 關閉所有編輯器標籤
- [ ] 啟動 Android Studio
- [ ] 等待 Gradle 同步完成
- [ ] 點擊「Reload All from Disk」

### Android Studio → VSCode 切換清單

- [ ] 停止運行的應用
- [ ] 關閉 Profiler/Layout Inspector
- [ ] 保存所有文件
- [ ] 記錄發現的問題
- [ ] 關閉 Android Studio（可選）
- [ ] 返回 VSCode
- [ ] Git pull（如果有團隊成員提交）

### 提交代碼檢查清單

- [ ] 運行測試：`./gradlew test`
- [ ] 運行 Lint：`./gradlew lint`
- [ ] 檢查代碼格式
- [ ] 更新 CHANGELOG（如果需要）
- [ ] 寫清晰的 commit message
- [ ] 在 Android Studio 中驗證
- [ ] 推送到遠程：`git push`

### 每日結束檢查清單

- [ ] 提交所有更改
- [ ] 推送到遠程
- [ ] 更新任務狀態
- [ ] 記錄明天的計劃
- [ ] 關閉所有 IDE

---

## 🎯 總結

### 關鍵優勢

1. **效率提升**
   - VSCode 快速啟動和編輯
   - Android Studio 專業測試
   - 各取所長，效率最大化

2. **資源優化**
   - 降低記憶體占用
   - 減少系統卡頓
   - 更好的多任務處理

3. **專注度提升**
   - 編碼時：輕量環境，專注代碼
   - 測試時：完整工具，深度分析

4. **靈活性**
   - 可以根據任務選擇工具
   - 不被單一工具限制
   - 學習兩種工具的最佳實踐

### 成功要訣

1. **嚴格遵守單一編輯器原則**
2. **善用 Git 進行同步**
3. **定期在兩個 IDE 之間驗證**
4. **保持文件和環境的一致性**
5. **建立清晰的工作流程**

### 不適合混合工作流的情況

- 需要頻繁使用 Compose 預覽
- 正在進行大規模重構
- 需要持續性能監控
- UI 設計為主的任務
- 團隊不熟悉 VSCode

在這些情況下，建議完全使用 Android Studio。

---

## 📚 延伸閱讀

### 官方文檔
- [Android Studio User Guide](https://developer.android.com/studio/intro)
- [VSCode Java Documentation](https://code.visualstudio.com/docs/languages/java)
- [Gradle Build Tool](https://docs.gradle.org/)

### 社群資源
- [r/androiddev - Tools Discussion](https://www.reddit.com/r/androiddev/)
- [Android Development Tools](https://developer.android.com/tools)

### 相關文檔
- [VSCODE_EXTENSIONS_GUIDE.md](./VSCODE_EXTENSIONS_GUIDE.md) - VSCode 外掛指南
- [QUICK_START.md](./QUICK_START.md) - 專案快速入門
- [PHASE1_IMPLEMENTATION_SUMMARY.md](./PHASE1_IMPLEMENTATION_SUMMARY.md) - Phase 1 實現總結

---

*最後更新: 2025-11-09*
*專案: SmartCameraApp1*
*工作流版本: 1.0*
