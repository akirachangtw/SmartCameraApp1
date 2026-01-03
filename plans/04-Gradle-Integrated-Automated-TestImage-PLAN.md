# Gradle-Integrated Automated Test Image Generation Implementation Plan

## 目標
建立完全自動化的測試圖片生成系統，整合至 Gradle 構建流程，支援依據開發階段逐步生成相關測試場景。

## 使用者需求總結
1. **整合方式**: 完全自動 - 整合到 Gradle 構建流程
2. **生成範圍**: 依據開發階段逐步生成相關場景
3. **鏡頭類型**: 統一圖片 - 前後鏡頭共用測試圖片

## 實施策略概覽

### 階段化生成策略
- **Phase 1**: 構圖基礎測試場景 (10 張圖片, ~6 分鐘)
- **Phase 2**: 光線分析測試場景 (8 張圖片, ~5 分鐘)
- **Phase 3**: 進階測試場景 (12 張圖片, ~7 分鐘)
- **總計**: 30 張測試圖片，涵蓋所有核心測試案例

### 自動化整合點
- Gradle 自訂任務 `generateTestImages`
- 可選的 pre-build hook (建議用於 CI/CD)
- 增量生成機制 (跳過已存在的圖片)

---

## 核心實施步驟

### Step 1: 建立 Prompt 檔案結構

**建立目錄**: `tools/test_image_prompts/`

**檔案清單**:
```
tools/test_image_prompts/
├── README.md (使用說明文件)
├── prompts_phase1_composition_basics.txt (Phase 1 提示詞)
├── prompts_phase2_lighting_basics.txt (Phase 2 提示詞)
├── prompts_phase3_advanced.txt (Phase 3 提示詞)
└── test_coverage_matrix.md (測試覆蓋率對照表)
```

#### Phase 1 提示詞範例 (10 張圖片)

**構圖測試場景**:
1. `comp_thirds_top_left.jpg` - 主體位於左上三分點
2. `comp_thirds_top_right.jpg` - 主體位於右上三分點
3. `comp_thirds_center_poor.jpg` - 主體居中 (不良構圖)
4. `comp_headroom_ideal.jpg` - 理想頭頂空間 (10%)
5. `comp_headroom_too_little.jpg` - 頭頂空間不足 (2%)
6. `comp_headroom_too_much.jpg` - 頭頂空間過多 (30%)
7. `comp_edge_good_margins.jpg` - 良好邊緣距離
8. `comp_edge_too_close_left.jpg` - 左側邊緣過近
9. `comp_single_subject.jpg` - 單一主體
10. `comp_two_subjects.jpg` - 雙主體

**提示詞格式**:
```
# Phase 1: Composition Basics
# Rule of Thirds - Top-Left Intersection
"comp_thirds_top_left.jpg", "Professional portrait photo, young Asian woman, natural smile, positioned at top-left third intersection, headroom 10%, minimal background, soft lighting, 3:4 portrait aspect ratio, photorealistic"
```

#### Phase 2 提示詞範例 (8 張圖片)

**光線測試場景**:
1. `light_normal.jpg` - 正常光線
2. `light_backlight_severe.jpg` - 嚴重逆光
3. `light_backlight_moderate.jpg` - 中度逆光
4. `light_high_contrast_window.jpg` - 高對比 (窗戶)
5. `light_high_contrast_sunset.jpg` - 高對比 (夕陽)
6. `light_overexposed.jpg` - 過曝
7. `light_underexposed_indoor.jpg` - 室內欠曝
8. `light_underexposed_twilight.jpg` - 暮光欠曝

#### Phase 3 提示詞範例 (12 張圖片)

**進階測試場景**:
- Fill-the-Frame: 5 張 (30%, 40%, 55%, 70%, 85%)
- Eye Position: 3 張 (上三分線、中間、下方)
- Multi-Subject: 4 張 (1, 2, 3, 4+ 人)

---

### Step 2: 修改 Python 腳本

**檔案**: `tools/generate_assets.py`

**新增功能**: 支援 `--force` 參數

```python
# 在 argparse 區塊新增
parser.add_argument(
    '--force',
    action='store_true',
    help='Force regenerate existing images (ignore SKIP_EXISTING_FILES)'
)

# 在 main() 函數中處理
args = parser.parse_args()
if args.force:
    GeneratorConfig.SKIP_EXISTING_FILES = False
```

**目的**: 支援 Gradle 任務的強制重新生成選項

---

### Step 3: 建立 Gradle 自訂任務

**檔案**: `app/build.gradle.kts`

**插入位置**: 在 `dependencies` 區塊之後

**任務功能**:
1. 驗證 Python 環境 (python3/python)
2. 檢查 GEMINI_API_KEY 環境變數
3. 根據 phase 參數選擇提示詞檔案
4. 執行 Python 腳本生成圖片
5. 驗證生成的圖片 (檔案大小、格式)
6. 報告執行結果

**任務實作**:

```kotlin
tasks.register("generateTestImages") {
    group = "image generation"
    description = "Generate test images using Gemini Imagen 3 API"

    doLast {
        // 1. 讀取專案屬性
        val phase = project.findProperty("phase") as String? ?: "1"
        val forceRegenerate = (project.findProperty("forceRegenerate") as String?)?.toBoolean() ?: false
        val skipImageGen = (project.findProperty("skipImageGen") as String?)?.toBoolean() ?: false

        if (skipImageGen) {
            logger.lifecycle("Skipping test image generation (-PskipImageGen=true)")
            return@doLast
        }

        // 2. 驗證 GEMINI_API_KEY
        val geminiApiKey = System.getenv("GEMINI_API_KEY")
            ?: project.findProperty("gemini.api.key") as String?

        if (geminiApiKey.isNullOrBlank()) {
            logger.warn("GEMINI_API_KEY not found. Skipping image generation.")
            logger.warn("Set via: export GEMINI_API_KEY='your_key' or gradle.properties")
            return@doLast
        }

        // 3. 偵測 Python 執行檔
        val pythonExecutable = findPythonExecutable()
        if (pythonExecutable == null) {
            logger.warn("Python not found. Skipping image generation.")
            return@doLast
        }

        // 4. 解析提示詞檔案
        val promptFileName = when (phase) {
            "1" -> "prompts_phase1_composition_basics.txt"
            "2" -> "prompts_phase2_lighting_basics.txt"
            "3" -> "prompts_phase3_advanced.txt"
            else -> {
                logger.error("Invalid phase: $phase. Use 1, 2, or 3.")
                throw GradleException("Invalid phase parameter")
            }
        }

        val promptFile = file("../tools/test_image_prompts/$promptFileName")
        if (!promptFile.exists()) {
            logger.error("Prompt file not found: ${promptFile.absolutePath}")
            throw GradleException("Missing prompt file")
        }

        // 5. 確認輸出目錄
        val outputDir = file("src/debug/assets/test_images")
        if (!outputDir.exists()) {
            logger.lifecycle("Creating output directory: ${outputDir.absolutePath}")
            outputDir.mkdirs()
        }

        // 6. 執行 Python 腳本
        logger.lifecycle("Generating Phase $phase test images...")
        logger.lifecycle("Prompt file: ${promptFile.name}")
        logger.lifecycle("Output directory: ${outputDir.absolutePath}")

        val commandArgs = mutableListOf(
            pythonExecutable,
            "tools/generate_assets.py",
            promptFile.absolutePath
        )
        if (forceRegenerate) {
            commandArgs.add("--force")
        }

        val result = project.exec {
            workingDir = projectDir.parentFile
            commandLine = commandArgs
            environment("GEMINI_API_KEY", geminiApiKey)
            isIgnoreExitValue = true
        }

        // 7. 報告結果
        if (result.exitValue == 0) {
            logger.lifecycle("✓ Test image generation completed successfully")
            validateGeneratedImages(outputDir)
        } else {
            logger.error("✗ Test image generation failed (exit code: ${result.exitValue})")
            logger.error("Check Python script logs for details")
        }
    }
}

fun findPythonExecutable(): String? {
    val candidates = listOf("python3", "python")
    for (candidate in candidates) {
        try {
            val result = project.exec {
                commandLine = listOf(candidate, "--version")
                isIgnoreExitValue = true
                standardOutput = java.io.ByteArrayOutputStream()
            }
            if (result.exitValue == 0) {
                logger.info("Found Python executable: $candidate")
                return candidate
            }
        } catch (e: Exception) {
            // Continue to next candidate
        }
    }
    return null
}

fun validateGeneratedImages(outputDir: File) {
    val images = outputDir.listFiles { file -> file.extension.lowercase() == "jpg" }
    logger.lifecycle("Found ${images?.size ?: 0} JPEG images in output directory")

    images?.forEach { image ->
        val sizeKB = image.length() / 1024
        when {
            sizeKB < 50 -> logger.warn("Warning: ${image.name} is suspiciously small ($sizeKB KB)")
            sizeKB > 5000 -> logger.warn("Warning: ${image.name} is very large ($sizeKB KB)")
            else -> logger.info("✓ ${image.name} ($sizeKB KB)")
        }
    }
}
```

**使用方式**:

```bash
# 生成 Phase 1 圖片
./gradlew generateTestImages -Pphase=1

# 生成 Phase 2 圖片
./gradlew generateTestImages -Pphase=2

# 強制重新生成 Phase 1
./gradlew generateTestImages -Pphase=1 -PforceRegenerate=true

# 跳過圖片生成 (離線開發)
./gradlew assembleDebug -PskipImageGen=true
```

---

### Step 4: 更新 TestImageProvider.kt

**檔案**: `app/src/main/java/com/akirachang/ai/photographer/data/local/TestImageProvider.kt`

**修改位置**: Line 26-32

**更新策略**: 手動更新 (建議用於 v1.0)

```kotlin
// Before (5 images)
private val testImages = listOf(
    "portrait_good_composition.jpg",
    "portrait_bad_headroom.jpg",
    "portrait_off_center.jpg",
    "landscape_symmetry.jpg",
    "landscape_rule_of_thirds.jpg"
)

// After Phase 1 (10 images)
private val testImages = listOf(
    // Phase 1: Composition Basics
    "comp_thirds_top_left.jpg",
    "comp_thirds_top_right.jpg",
    "comp_thirds_center_poor.jpg",
    "comp_headroom_ideal.jpg",
    "comp_headroom_too_little.jpg",
    "comp_headroom_too_much.jpg",
    "comp_edge_good_margins.jpg",
    "comp_edge_too_close_left.jpg",
    "comp_single_subject.jpg",
    "comp_two_subjects.jpg"
)

// After Phase 1+2 (18 images)
private val testImages = listOf(
    // Phase 1: Composition Basics
    "comp_thirds_top_left.jpg",
    "comp_thirds_top_right.jpg",
    "comp_thirds_center_poor.jpg",
    "comp_headroom_ideal.jpg",
    "comp_headroom_too_little.jpg",
    "comp_headroom_too_much.jpg",
    "comp_edge_good_margins.jpg",
    "comp_edge_too_close_left.jpg",
    "comp_single_subject.jpg",
    "comp_two_subjects.jpg",

    // Phase 2: Lighting Basics
    "light_normal.jpg",
    "light_backlight_severe.jpg",
    "light_backlight_moderate.jpg",
    "light_high_contrast_window.jpg",
    "light_high_contrast_sunset.jpg",
    "light_overexposed.jpg",
    "light_underexposed_indoor.jpg",
    "light_underexposed_twilight.jpg"
)

// After Phase 1+2+3 (30 images - complete)
// Add Phase 3 images to the list...
```

**同步更新 `logExpectedScores()`**: 為新圖片添加預期分數註解

---

### Step 5: 建立文件

#### 5.1 README.md

**檔案**: `tools/test_image_prompts/README.md`

**內容要點**:
- 快速開始指南
- 提示詞檔案格式說明
- Gradle 任務使用方式
- 提示詞撰寫指南
- 疑難排解

#### 5.2 測試覆蓋率對照表

**檔案**: `tools/test_image_prompts/test_coverage_matrix.md`

**內容**: 圖片檔案 ↔ 測試案例對照表

```markdown
| Image File | Test File | Test Method | Expected Behavior |
|------------|-----------|-------------|-------------------|
| comp_thirds_top_left.jpg | AnalyzeCompositionUseCaseTest.kt | face at top-left intersection should return Good | Thirds score ≥ 0.8 |
```

---

### Step 6: 可選 - Pre-Build Integration

**檔案**: `app/build.gradle.kts`

**整合點**: 僅用於 emulator 構建變體

```kotlin
// 在 android {} 區塊外部添加
android.applicationVariants.all {
    if (name.contains("emulatorDebug")) {
        // Only auto-generate for emulator debug builds
        preBuildProvider.configure {
            dependsOn("generateTestImages")
        }
    }
}
```

**優點**: 自動確保測試圖片存在
**缺點**: 每次構建都會檢查 (但有增量機制，已存在圖片會跳過)

---

## 實施時間表

### Week 1: Phase 1 實施
**時間**: 2-3 小時

1. 建立 `tools/test_image_prompts/` 目錄 (5 分鐘)
2. 撰寫 Phase 1 提示詞檔案 (30 分鐘)
3. 修改 `generate_assets.py` 添加 `--force` 參數 (10 分鐘)
4. 建立 Gradle `generateTestImages` 任務 (45 分鐘)
5. 執行 `./gradlew generateTestImages -Pphase=1` (6 分鐘)
6. 更新 `TestImageProvider.kt` (10 分鐘)
7. 撰寫 `README.md` (30 分鐘)
8. 測試驗證 (20 分鐘)

### Week 2: Phase 2 實施
**時間**: 1-2 小時

1. 撰寫 Phase 2 提示詞檔案 (20 分鐘)
2. 執行 `./gradlew generateTestImages -Pphase=2` (5 分鐘)
3. 更新 `TestImageProvider.kt` (5 分鐘)
4. 建立測試覆蓋率對照表 (30 分鐘)
5. 測試驗證 (20 分鐘)

### Week 3: Phase 3 實施
**時間**: 1-2 小時

1. 撰寫 Phase 3 提示詞檔案 (20 分鐘)
2. 執行 `./gradlew generateTestImages -Pphase=3` (7 分鐘)
3. 更新 `TestImageProvider.kt` (5 分鐘)
4. 添加 Pre-Build Integration (可選, 10 分鐘)
5. 完整測試驗證 (30 分鐘)
6. 撰寫專案文件更新 (20 分鐘)

---

## 關鍵檔案清單

### 需要建立的檔案
1. `tools/test_image_prompts/README.md`
2. `tools/test_image_prompts/prompts_phase1_composition_basics.txt`
3. `tools/test_image_prompts/prompts_phase2_lighting_basics.txt`
4. `tools/test_image_prompts/prompts_phase3_advanced.txt`
5. `tools/test_image_prompts/test_coverage_matrix.md`

### 需要修改的檔案
1. `tools/generate_assets.py` - 添加 `--force` 參數
2. `app/build.gradle.kts` - 添加 `generateTestImages` 任務
3. `app/src/main/java/com/akirachang/ai/photographer/data/local/TestImageProvider.kt` - 更新圖片清單

### 輸出檔案位置
- `app/src/debug/assets/test_images/*.jpg` (30 張測試圖片)

---

## 錯誤處理策略

### 優雅降級 (預設)
- 缺少 API Key → 跳過生成，記錄警告，繼續構建
- Python 未安裝 → 跳過生成，記錄警告
- 生成失敗 → 記錄錯誤，使用現有圖片，繼續構建

### 嚴格模式 (CI/CD)
```bash
./gradlew generateTestImages -Pphase=1 -PstrictImageGen=true
```
- 任何錯誤都會中斷構建

---

## 成功驗證標準

1. **功能驗證**:
   - ✅ 執行 `./gradlew generateTestImages -Pphase=1` 成功生成 10 張圖片
   - ✅ 所有圖片檔案大小在 50KB - 5MB 範圍內
   - ✅ 圖片格式為 JPEG
   - ✅ 增量生成正常運作 (已存在圖片被跳過)

2. **測試驗證**:
   - ✅ `TestImageProvider.kt` 能成功載入所有新圖片
   - ✅ `AnalyzeCompositionUseCaseTest.kt` 測試通過率 ≥ 90%
   - ✅ `AnalyzeLightingUseCaseTest.kt` 測試通過率 ≥ 90%

3. **文件驗證**:
   - ✅ README.md 清晰說明使用方式
   - ✅ 測試覆蓋率對照表完整
   - ✅ 團隊成員能在 10 分鐘內完成環境設定

---

## 風險緩解

| 風險 | 可能性 | 影響 | 緩解措施 |
|------|--------|------|----------|
| API 配額超限 | 中 | 中 | 增量生成、快取圖片於 Git |
| 生成圖片不符提示詞 | 中 | 高 | 迭代優化提示詞、人工審查 |
| Python 環境問題 | 低 | 中 | 詳細設定文件、環境偵測 |
| CI/CD 速率限制 | 高 | 低 | 快取圖片、跳過重複生成 |

---

## 後續改進方向

### v2.7.0 - v2.8.0
- Prompt 驗證工具 (語法檢查、重複檔名偵測)
- 圖片品質指標 (亮度、對比度分析)
- 批次生成優化 (進度報告)

### v2.9.0 - v3.0.0
- 動態 TestImageProvider (執行時自動發現圖片)
- CI/CD GitHub Actions 整合
- 視覺回歸測試

### v3.1.0+
- 多模型支援 (Stable Diffusion, DALL-E)
- ML 驗證 (預測分數 vs 實際分數比對)
- 自訂圖片編輯器整合
