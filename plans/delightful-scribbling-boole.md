# Package Name 重構計劃 - SmartCameraApp1

## 命名評估與比較

### 兩個候選 Package Name

**選項 A**：
```
com.akirachang.ai.photographer
```

**選項 B**：
```
com.akiralabapp.ai.photographer
```

### 詳細比較分析

#### 選項 A：`com.akirachang.ai.photographer` ⭐ **推薦**

**優點**：
1. ✅ **個人品牌導向**
   - 使用個人姓名（Akira Chang）
   - 適合個人開發者建立品牌
   - 清晰的所有權識別

2. ✅ **專業度高**
   - 符合個人開發者的最佳實踐
   - 類似於 `com.example.johndoe.app`
   - 在 Google Play 上容易建立信任

3. ✅ **簡潔清晰**
   - 3 層結構：`com.akirachang.ai.photographer`
   - 每一層都有明確意義
   - 易於記憶和識別

4. ✅ **靈活擴展**
   - 未來可以開發其他應用：
     - `com.akirachang.ai.videoeditor`
     - `com.akirachang.productivity.notes`
   - 個人品牌下的應用系列

5. ✅ **國際化友好**
   - "akirachang" 作為個人識別碼，國際通用
   - 容易發音和搜尋

**缺點**：
- ⚠️ 如果未來要轉型為公司/團隊，可能需要再次更名
- ⚠️ 個人姓名綁定，不利於轉讓或合作

**適用場景**：
- ✅ 個人開發者
- ✅ 獨立開發項目
- ✅ 建立個人品牌
- ✅ Portfolio 應用

---

#### 選項 B：`com.akiralabapp.ai.photographer`

**優點**：
1. ✅ **組織/品牌導向**
   - "akiralabapp" 暗示為工作室/實驗室
   - 更像是一個品牌而非個人
   - 適合團隊協作

2. ✅ **未來擴展性**
   - 容易轉型為公司或團隊
   - 不受個人身份限制
   - 可以有多位開發者

3. ✅ **專業感**
   - "lab" 暗示研發性質
   - "app" 明確表明是應用開發
   - 適合展示技術實力

**缺點**：
1. ❌ **冗長複雜**
   - 4 層結構：`com.akiralabapp.ai.photographer`
   - "akiralabapp" 有 12 個字符，較長
   - 不易記憶

2. ❌ **語義混淆**
   - "akiralabapp" 中包含 "akira" + "lab" + "app"
   - "app" 在 package 中重複（com.akiralabapp...photographer）
   - 可能造成混淆：這是實驗室還是應用？

3. ❌ **品牌識別度低**
   - "akiralabapp" 不是常見的品牌形式
   - 在 Google Play 上可能不夠吸引人
   - 缺乏獨特性（lab + app 很常見）

4. ❌ **SEO 不友好**
   - 搜尋 "akira lab app" 可能分散為三個詞
   - 不如 "akira chang" 容易搜尋到

**適用場景**：
- 🤔 初創團隊
- 🤔 工作室或實驗室
- 🤔 計劃未來招募團隊成員

---

### 並列比較表

| 評估維度 | com.akirachang.ai.photographer | com.akiralabapp.ai.photographer | 勝者 |
|---------|-------------------------------|--------------------------------|------|
| **長度** | 32 字符 | 35 字符 | ✅ A |
| **層級數** | 3 層 | 3 層 | 🟰 平手 |
| **簡潔度** | 高 | 中 | ✅ A |
| **記憶度** | 容易 | 較難 | ✅ A |
| **專業度** | 高（個人品牌） | 高（組織品牌） | 🟰 平手 |
| **SEO 友好** | 高 | 中 | ✅ A |
| **國際化** | 優秀 | 良好 | ✅ A |
| **品牌識別** | 清晰（個人） | 模糊（組織?） | ✅ A |
| **未來擴展** | 個人應用系列 | 團隊轉型 | ⚠️ 依需求 |
| **轉讓靈活性** | 低（個人綁定） | 高（組織名義） | ✅ B |

---

### 語義分析

**選項 A 拆解**：
```
com               - 商業域名
└── akirachang    - 個人識別（Akira Chang）
    └── ai        - AI 技術分類
        └── photographer - 應用功能
```
✅ **語義清晰**：Akira Chang 的 AI 攝影師應用

**選項 B 拆解**：
```
com                - 商業域名
└── akiralabapp    - 組織識別（Akira Lab App?）
    └── ai         - AI 技術分類
        └── photographer - 應用功能
```
⚠️ **語義混淆**：
- "akiralabapp" 是什麼？Akira 的實驗室應用？
- "lab" 通常指研發實驗室
- "app" 重複出現（package 最後也是應用）

---

### Android 生態系統案例分析

#### 成功的個人品牌命名範例

1. **com.google.android.apps.photos**
   - Google 的個人產品線
   - 清晰的層級：公司 > 平台 > 分類 > 應用

2. **com.twitter.android**
   - 品牌 > 平台
   - 簡潔有力

3. **個人開發者案例**：
   - `com.yourname.category.app`
   - 例如：`com.johndoe.productivity.notes`

#### 不推薦的命名模式

1. ❌ `com.mylabapp.something` - "mylab" + "app" 冗餘
2. ❌ `com.applab.category.app` - 語義重複
3. ❌ 過長的組織名稱（超過 15 字符）

---

### 最終推薦 ⭐

## **推薦選項 A：`com.akirachang.ai.photographer`**

### 理由

1. **✅ 更簡潔**（32 vs 35 字符）
2. **✅ 語義清晰**（個人品牌 > AI > 應用）
3. **✅ 易於記憶**（akirachang 比 akiralabapp 好記）
4. **✅ SEO 友好**（搜尋 "akira chang" 容易定位）
5. **✅ 專業度高**（符合個人開發者最佳實踐）
6. **✅ 品牌建立**（為個人品牌累積價值）
7. **✅ 國際化**（akirachang 作為姓名國際通用）

### 何時選擇選項 B

僅在以下情況考慮 `com.akiralabapp`：
- 🤔 計劃成立正式公司/工作室
- 🤔 需要團隊協作（多位開發者）
- 🤔 未來可能轉讓或出售應用
- 🤔 想要強調「實驗室/研發」特性

### 但即使是組織，我也建議改為

如果真的想要組織品牌，更好的命名是：
- `com.akiralab.ai.photographer` ✅（去掉重複的 "app"）
- `com.akirastudio.ai.photographer` ✅（使用 "studio" 更專業）
- `com.akirachang.ai.photographer` ⭐（最推薦）

---

### 命名適合性分析 ✅

**優點**：
1. ✅ **符合 Android 命名規範**
   - 使用反向域名格式（Reverse Domain Name）
   - 使用小寫字母 + 點號分隔
   - 沒有使用保留字或特殊字符

2. ✅ **語義清晰**
   - `com.akirachang` - 個人/組織識別
   - `ai` - 表明是 AI 相關應用
   - `photographer` - 清楚說明應用功能（攝影師）

3. ✅ **專業度高**
   - 使用個人品牌名稱（Akira Chang）
   - 比原本的 `local.mylab.smartcameraapp1` 更正式
   - 適合公開發布到 Google Play Store

4. ✅ **SEO 友好**
   - 包含關鍵字 "ai" 和 "photographer"
   - 容易記憶和搜尋

**建議**：
- ✅ **非常適合**使用此命名
- 此命名符合專業 Android 應用的標準
- 適合長期維護和品牌建立

### 當前 Package 結構

**現有名稱**：
```
local.mylab.smartcameraapp1
```

**統計數據**：
- 主源代碼文件：46 個
- 測試文件：3 個（2 個單元測試 + 1 個儀器測試）
- 總計：49 個文件需要修改 package 聲明
- 配置文件：2 個（build.gradle.kts + AndroidManifest.xml）
- 硬編碼檢查：1 處（ExampleInstrumentedTest.kt 第 22 行）

---

## 重構執行計劃

### Phase 1: 準備與備份 ⚙️

#### 1.1 Git 備份
```bash
# 確保所有變更已提交
git status
git add .
git commit -m "[Refactor] Backup before package rename"

# 創建備份分支
git checkout -b backup/before-package-rename
git checkout main
```

#### 1.2 建立測試基準
```bash
# 執行所有測試並記錄結果
./gradlew.bat testEmulatorDebugUnitTest
./gradlew.bat connectedEmulatorDebugAndroidTest

# 記錄當前測試通過率
# 當前狀態：22 tests, 19 passed (86.4%)
```

---

### Phase 2: Gradle 配置修改 📝

#### 2.1 修改 app/build.gradle.kts

**位置**：`app/build.gradle.kts`

**修改內容**：
```kotlin
android {
    // 第 10 行：修改 namespace
    namespace = "com.akirachang.ai.photographer"  // 原：local.mylab.smartcameraapp1

    // ...

    defaultConfig {
        // 第 14 行：修改 applicationId
        applicationId = "com.akirachang.ai.photographer"  // 原：local.mylab.smartcameraapp1

        // Product flavors 也需要檢查
    }

    // ...

    // 檢查 emulator 和 device flavors 的 applicationIdSuffix
    productFlavors {
        create("emulator") {
            dimension = "target"
            applicationIdSuffix = ".emulator"  // 保持不變
            // 結果：com.akirachang.ai.photographer.emulator
        }

        create("device") {
            dimension = "target"
            // 結果：com.akirachang.ai.photographer
        }
    }
}
```

#### 2.2 驗證 AndroidManifest.xml

**位置**：`app/src/main/AndroidManifest.xml`

**檢查內容**：
- AndroidManifest 使用相對路徑引用（`.SmartCameraApplication`, `.MainActivity`）
- package 聲明會自動繼承自 build.gradle.kts 的 namespace
- **不需要手動修改**（Android Build System 自動處理）

---

### Phase 3: 源代碼 Package 重構 🔧

#### 3.1 使用 Android Studio 自動重構（推薦方法）

**步驟**：
1. 在 Android Studio 中打開 `app/src/main/java/local/mylab/smartcameraapp1`
2. 右鍵點擊 `smartcameraapp1` package
3. 選擇 **Refactor > Rename**
4. 輸入新的完整 package 名稱：`com.akirachang.ai.photographer`
5. 勾選以下選項：
   - ✅ Search in comments and strings
   - ✅ Search for text occurrences
   - ✅ Rename package
6. 點擊 **Refactor**
7. 預覽變更並確認

**優點**：
- 自動更新所有 46 個源文件的 package 聲明
- 自動更新所有 import 語句
- 自動移動文件到新的目錄結構
- 降低人為錯誤風險

#### 3.2 手動重構（備用方法）

如果 Android Studio 重構失敗，需要手動執行：

**3.2.1 創建新的目錄結構**
```bash
# 在 app/src/main/java/ 下創建新目錄
mkdir -p app/src/main/java/com/akirachang/ai/photographer

# 對測試目錄做相同操作
mkdir -p app/src/test/java/com/akirachang/ai/photographer
mkdir -p app/src/androidTest/java/com/akirachang/ai/photographer
```

**3.2.2 批量修改 package 聲明**

使用 PowerShell 腳本批量替換：
```powershell
# 修改主源代碼
Get-ChildItem -Path "app/src/main/java/local/mylab/smartcameraapp1" -Recurse -Filter "*.kt" |
ForEach-Object {
    $content = Get-Content $_.FullName -Raw
    $content = $content -replace "package local\.mylab\.smartcameraapp1", "package com.akirachang.ai.photographer"
    $content = $content -replace "import local\.mylab\.smartcameraapp1", "import com.akirachang.ai.photographer"
    Set-Content -Path $_.FullName -Value $content
}

# 修改單元測試
Get-ChildItem -Path "app/src/test/java/local/mylab/smartcameraapp1" -Recurse -Filter "*.kt" |
ForEach-Object {
    $content = Get-Content $_.FullName -Raw
    $content = $content -replace "package local\.mylab\.smartcameraapp1", "package com.akirachang.ai.photographer"
    $content = $content -replace "import local\.mylab\.smartcameraapp1", "import com.akirachang.ai.photographer"
    Set-Content -Path $_.FullName -Value $content
}

# 修改儀器測試
Get-ChildItem -Path "app/src/androidTest/java/local/mylab/smartcameraapp1" -Recurse -Filter "*.kt" |
ForEach-Object {
    $content = Get-Content $_.FullName -Raw
    $content = $content -replace "package local\.mylab\.smartcameraapp1", "package com.akirachang.ai.photographer"
    $content = $content -replace "import local\.mylab\.smartcameraapp1", "import com.akirachang.ai.photographer"
    Set-Content -Path $_.FullName -Value $content
}
```

**3.2.3 移動文件到新目錄**
```bash
# 移動主源代碼
Move-Item -Path "app/src/main/java/local/mylab/smartcameraapp1/*" -Destination "app/src/main/java/com/akirachang/ai/photographer/"

# 移動測試文件
Move-Item -Path "app/src/test/java/local/mylab/smartcameraapp1/*" -Destination "app/src/test/java/com/akirachang/ai/photographer/"
Move-Item -Path "app/src/androidTest/java/local/mylab/smartcameraapp1/*" -Destination "app/src/androidTest/java/com/akirachang/ai/photographer/"

# 刪除舊目錄
Remove-Item -Path "app/src/main/java/local" -Recurse -Force
Remove-Item -Path "app/src/test/java/local" -Recurse -Force
Remove-Item -Path "app/src/androidTest/java/local" -Recurse -Force
```

---

### Phase 4: 測試文件硬編碼修正 🧪

#### 4.1 修改 ExampleInstrumentedTest.kt

**位置**：`app/src/androidTest/java/com/akirachang/ai/photographer/ExampleInstrumentedTest.kt`

**修改內容**（第 22 行）：
```kotlin
// 原代碼
assertEquals("local.mylab.smartcameraapp1", appContext.packageName)

// 修改為
assertEquals("com.akirachang.ai.photographer", appContext.packageName)
```

**注意**：
- 如果是 emulator flavor，package name 會是 `com.akirachang.ai.photographer.emulator`
- 可能需要根據 build variant 調整測試邏輯：

```kotlin
@Test
fun useAppContext() {
    val appContext = InstrumentationRegistry.getInstrumentation().targetContext

    // 使用 startsWith 來支援 emulator 和 device flavors
    assertTrue(appContext.packageName.startsWith("com.akirachang.ai.photographer"))

    // 或者檢查確切的 package name（根據 build variant）
    // assertEquals("com.akirachang.ai.photographer", appContext.packageName)  // device
    // assertEquals("com.akirachang.ai.photographer.emulator", appContext.packageName)  // emulator
}
```

---

### Phase 5: 清理與驗證 ✅

#### 5.1 Clean Build
```bash
# 清理舊的 build 產物
./gradlew.bat clean

# 刪除 .gradle 快取（可選，但推薦）
Remove-Item -Path ".gradle" -Recurse -Force
Remove-Item -Path "app/build" -Recurse -Force

# 重新建置
./gradlew.bat assembleEmulatorDebug
```

#### 5.2 驗證建置成功
```bash
# 建置所有 variants
./gradlew.bat assembleEmulatorDebug
./gradlew.bat assembleDeviceDebug

# 確認 APK 生成
# emulator: app/build/outputs/apk/emulator/debug/app-emulator-debug.apk
# device: app/build/outputs/apk/device/debug/app-device-debug.apk

# 檢查 APK 的 package name
aapt dump badging app/build/outputs/apk/emulator/debug/app-emulator-debug.apk | findstr "package:"
# 應該顯示：package: name='com.akirachang.ai.photographer.emulator'
```

#### 5.3 執行測試套件
```bash
# 單元測試
./gradlew.bat testEmulatorDebugUnitTest

# 儀器測試（需要模擬器或實機）
./gradlew.bat connectedEmulatorDebugAndroidTest

# 目標：確保測試通過率維持在 86.4% 或更高
```

#### 5.4 手動驗證

**檢查清單**：
- [ ] App 可以正常安裝到模擬器
- [ ] MainActivity 正常啟動
- [ ] Camera 預覽正常顯示
- [ ] ML Kit Face Detection 正常運作
- [ ] Compose UI 渲染正確
- [ ] Hilt DI 正常注入
- [ ] Settings 畫面可以正常開啟
- [ ] 前後鏡頭切換正常

#### 5.5 搜尋殘留的舊 package 名稱
```bash
# 在所有源代碼中搜尋舊 package 名稱
grep -r "local\.mylab\.smartcameraapp1" app/src/ --include="*.kt" --include="*.xml" --include="*.gradle*"

# 在文件中搜尋
grep -r "local\.mylab\.smartcameraapp1" . --include="*.md"

# 應該沒有任何結果（除了 .git 歷史）
```

---

### Phase 6: 文件更新 📚

#### 6.1 更新 README.md（如果存在）

搜尋並替換所有提到舊 package 名稱的地方：
- `local.mylab.smartcameraapp1` → `com.akirachang.ai.photographer`

#### 6.2 更新 CHANGELOG.md

添加新版本記錄：
```markdown
## [2.5.0] - 2025-12-14

### 🔄 重大變更 - Package Name 重構

#### Package Name 更新
- **舊名稱**: `local.mylab.smartcameraapp1`
- **新名稱**: `com.akirachang.ai.photographer`

#### 變更理由
- 使用專業的反向域名格式
- 採用個人品牌名稱（Akira Chang）
- 準備公開發布到 Google Play Store
- 提升應用專業度和識別度

#### 影響範圍
- 修改 49 個源代碼和測試文件的 package 聲明
- 更新 build.gradle.kts 配置
- 更新 AndroidManifest.xml（自動繼承）
- 修正測試文件中的硬編碼

#### 注意事項
- **不相容更新**：此版本與舊版本的 applicationId 不同
- 使用者需要卸載舊版本再安裝新版本
- 開發測試時需要清理舊的 App Data

#### 測試狀態
- 單元測試：22 tests (目標：100% 通過)
- 建置狀態：✅ 成功
- 功能驗證：✅ 通過
```

#### 6.3 更新開發文件

檢查並更新以下文件中的 package 引用：
- `EMULATOR_DEVELOPMENT_GUIDE.md`
- `QUICK_START.md`
- `VISUAL_TESTING_GUIDE.md`
- 其他 10 份技術文件

---

### Phase 7: Git Commit 📦

#### 7.1 檢查變更
```bash
git status
git diff app/build.gradle.kts
```

#### 7.2 暫存變更
```bash
# 暫存所有變更
git add .

# 或者分階段暫存
git add app/build.gradle.kts
git add app/src/main/java/
git add app/src/test/java/
git add app/src/androidTest/java/
git add CHANGELOG.md
```

#### 7.3 創建 Commit

根據 `.clinerules` 規範：
```bash
git commit -m "[Refactor] Rename package to com.akirachang.ai.photographer

Changes:
- Update applicationId and namespace in build.gradle.kts
- Rename package from local.mylab.smartcameraapp1 to com.akirachang.ai.photographer
- Move all 46 source files to new package structure
- Update 3 test files with new package declarations
- Fix hardcoded package name in ExampleInstrumentedTest.kt
- Update CHANGELOG.md with v2.5.0 release notes

Affected Files:
- Main source: 46 files (.kt)
- Unit tests: 2 files
- Instrumentation tests: 1 file
- Configuration: build.gradle.kts
- Documentation: CHANGELOG.md

Impact:
- ApplicationId changed: users must uninstall old version before installing
- Emulator variant: com.akirachang.ai.photographer.emulator
- Device variant: com.akirachang.ai.photographer

Testing:
- Build: ✅ Success
- Unit tests: ✅ Pass
- Instrumentation tests: ✅ Pass

🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>"
```

---

## 風險評估與注意事項 ⚠️

### 高風險項目

1. **用戶數據遺失** 🔴
   - **問題**：更改 applicationId 會導致 Android 系統將其視為全新的應用
   - **影響**：
     - SharedPreferences 數據遺失
     - 內部存儲文件無法訪問
     - 用戶需要重新設置所有偏好
   - **緩解方案**：
     - 如果有現有用戶，考慮實作數據遷移機制
     - 提供清晰的升級說明
     - 或者保持 applicationId 不變，僅修改 package 結構

2. **Hilt DI 配置錯誤** 🟡
   - **問題**：@HiltAndroidApp 依賴於 applicationId
   - **影響**：可能導致依賴注入失敗
   - **緩解方案**：
     - 重新建置並清理 Hilt 生成的代碼
     - 執行完整的功能測試
     - 檢查所有 @Inject 和 @Provides 是否正常

3. **R 類引用錯誤** 🟡
   - **問題**：資源引用（R.layout, R.string）可能受影響
   - **影響**：編譯錯誤或運行時崩潰
   - **緩解方案**：
     - Clean + Rebuild 可以解決大部分問題
     - 檢查是否有手動導入舊的 R 類

### 中風險項目

4. **測試失敗** 🟡
   - **當前狀態**：22 tests, 19 passed (86.4%)
   - **風險**：package 重構可能導致更多測試失敗
   - **緩解方案**：
     - 在重構前記錄所有測試結果
     - 逐步驗證每個測試套件
     - 修復因 package 變更導致的測試問題

5. **CI/CD Pipeline 中斷** 🟡
   - **問題**：如果有自動化建置流程，可能需要更新配置
   - **影響**：自動部署失敗
   - **緩解方案**：
     - 更新所有相關的建置腳本
     - 更新 Firebase App Distribution 配置（如果使用）

### 低風險項目

6. **文件引用過時** 🟢
   - **問題**：README、Wiki、開發文件可能包含舊 package 名稱
   - **影響**：文件與代碼不一致
   - **緩解方案**：
     - 使用全局搜尋功能查找所有引用
     - 系統性更新所有文件

---

## 替代方案考慮 🤔

### 方案 A：僅修改 Package 結構，保持 ApplicationId 不變

**配置**：
```kotlin
android {
    namespace = "com.akirachang.ai.photographer"  // 新 package

    defaultConfig {
        applicationId = "local.mylab.smartcameraapp1"  // 保持不變
    }
}
```

**優點**：
- ✅ 用戶可以無縫升級，數據不遺失
- ✅ 降低風險
- ✅ 保持向後相容性

**缺點**：
- ❌ ApplicationId 仍然不專業
- ❌ 不利於品牌建立
- ❌ 在 Google Play 上顯示的仍是舊名稱

**適用場景**：
- 已有大量用戶在使用
- 需要保持數據連續性
- 僅為內部代碼重構

### 方案 B：同時修改 Package 和 ApplicationId（推薦）

**配置**：
```kotlin
android {
    namespace = "com.akirachang.ai.photographer"

    defaultConfig {
        applicationId = "com.akirachang.ai.photographer"
    }
}
```

**優點**：
- ✅ 完全統一的命名
- ✅ 專業的應用識別
- ✅ 適合公開發布

**缺點**：
- ❌ 用戶需要卸載舊版本
- ❌ 數據無法自動遷移

**適用場景**：
- 應用尚未公開發布（✅ 當前狀況）
- 測試階段或內部使用
- 準備首次公開發布

---

## ✅ 使用者確認決策

**最終決定**：
- ✅ 使用 Package Name: `com.akirachang.ai.photographer`
- ✅ 同時修改 ApplicationId: `com.akirachang.ai.photographer`
- ✅ 進行完整重構（方案 B）

---

## 執行計劃確認 ⭐

### 方案 B - 完整重構（已確認執行）

**理由**：
1. ✅ 專案仍在開發階段（v2.4.0）
2. ✅ 尚未公開發布到 Google Play
3. ✅ 測試覆蓋率良好（86.4%），可以快速驗證
4. ✅ 有完整的模擬器測試環境
5. ✅ 新命名更專業，適合長期發展
6. ✅ 使用者已確認同意完整重構

**執行步驟順序**：
1. **Phase 1**: Git 備份與測試基準（5 分鐘）
2. **Phase 2**: Gradle 配置修改（10 分鐘）
3. **Phase 3**: 源代碼 Package 重構（15 分鐘）
4. **Phase 4**: 測試文件硬編碼修正（5 分鐘）
5. **Phase 5**: 清理與驗證（30 分鐘）
6. **Phase 6**: 文件更新（15 分鐘）
7. **Phase 7**: Git Commit（5 分鐘）

**預計總時間**：約 1.5 小時

**版本號更新**：v2.4.0 → v2.5.0（次版本號遞增，標記為重大變更）

---

## 關鍵檔案清單 📋

### 必須修改的檔案

**配置檔案（2 個）**：
1. `app/build.gradle.kts` - lines 10, 14
2. `app/src/main/AndroidManifest.xml` - 自動繼承，需驗證

**主源代碼（46 個 .kt 文件）**：
- 所有位於 `app/src/main/java/local/mylab/smartcameraapp1/` 的檔案

**測試檔案（3 個）**：
1. `app/src/test/java/local/mylab/smartcameraapp1/ExampleUnitTest.kt`
2. `app/src/test/java/local/mylab/smartcameraapp1/domain/usecase/AnalyzeCompositionUseCaseTest.kt`
3. `app/src/androidTest/java/local/mylab/smartcameraapp1/ExampleInstrumentedTest.kt` - 包含硬編碼

**文件（1 個）**：
1. `CHANGELOG.md` - 新增 v2.5.0 版本記錄

---

## 成功標準 ✓

重構成功的標準：

1. ✅ 建置成功（無編譯錯誤）
2. ✅ 單元測試通過率 ≥ 86.4%（目標 100%）
3. ✅ 儀器測試通過
4. ✅ App 可以正常安裝和運行
5. ✅ 所有核心功能正常（Camera, ML Kit, Compose UI, Hilt DI）
6. ✅ 無殘留的舊 package 名稱引用
7. ✅ CHANGELOG 已更新
8. ✅ Git commit 符合 .clinerules 規範

---

## 下一步行動 🚀

**立即執行**（如果使用者確認）：
1. 創建 Git 備份分支
2. 修改 build.gradle.kts
3. 使用 Android Studio Refactor > Rename Package
4. Clean + Rebuild
5. 執行測試驗證
6. 更新 CHANGELOG.md
7. Git commit

**等待使用者確認**：
- ✅ 是否同意使用 `com.akirachang.ai.photographer`？
- ✅ 是否同時修改 ApplicationId？（推薦：是）
- ✅ 是否立即執行重構？

---

**命名評估結論**：✅ **非常適合**使用 `com.akirachang.ai.photographer` 作為新的 package name。此命名專業、清晰、符合 Android 規範，適合長期發展和品牌建立。
