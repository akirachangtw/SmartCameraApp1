# P0-2 / P0-3 測試指引文件

**版本**: v2.6.0
**更新日期**: 2025-12-27
**前置任務**: ✅ P0-1 模擬器測試已完成（提交 `cb8ccfc`）

---

## 📊 當前專案狀態

### ✅ 已完成功能（v2.6.0）

1. **Lighting Analysis Enhancement**
   - 5 種光線狀態檢測（Backlight, HighContrast, Overexposed, Underexposed, Normal）
   - 優先級排序：Backlight > Overexposed > HighContrast > Underexposed > Normal
   - Histogram 分析（高光比、陰影比、動態範圍）

2. **HDR Warning UI**
   - 4 種顏色警告（深橙 0xFFFF6F00 / 橙 0xFFFFA726 / 紅 0xFFE53935 / 黃 0xFFFFB300）
   - 動態警告訊息（Traditional Chinese）
   - 自動 HDR 建議邏輯

3. **Fill-the-Frame Composition**
   - 5 級主體大小評估（太小 <30% / 稍小 30-40% / 理想 40-70% / 稍大 70-85% / 太大 >85%）
   - 與 Edge Distance 加權整合（Edge 60% + Fill 40% = 25% 總權重）

4. **Test Image Automation (v2.7.0)**
   - Gemini API 自動生成測試圖片
   - 動態圖片發現機制（零維護）
   - Gradle 任務自動化（checkPythonEnv, scanTestImagePrompts, generateTestImages, updateTestImages）

### ✅ 測試覆蓋率

- **單元測試**: 73/73 通過 (100%)
- **模擬器測試**: 5/5 光線狀態驗證 ✅
- **測試圖片**: 15 張（10 HDR + 5 v1.0）

### 📦 最新提交

```
cb8ccfc [Chore] Update .gitignore for v2.7.0 automation
5c89697 [Docs] Add comprehensive test reports for v2.6.0
46e0649 [Infrastructure] Add v2.7.0 test image generation automation
e59ec71 [Fix] Correct Overexposed state priority + Improve test prompts
9b0abdc [Test] Add HDRWarningOverlay UI instrumented tests
```

---

## 🎯 P0-2: 實機 HDR 自動啟用驗證

### 目標
驗證 Lighting Analysis 在真實 Android 裝置上正確觸發 HDR 警告並建議啟用 HDR。

### 前置條件

1. **硬體需求**
   - Android 實機（建議 Android 10+ 以上）
   - 支援 CameraX API
   - 支援 HDR 模式的相機硬體

2. **環境準備**
   ```bash
   # 連接實機
   adb devices

   # 安裝 APK
   .\gradlew.bat assembleDebug
   adb install -r app\build\outputs\apk\debug\app-debug.apk
   ```

3. **測試圖片**
   - 使用模擬器測試中的 15 張圖片
   - 或準備真實場景照片（見 P0-3）

### 測試場景

#### 場景 1: Backlight Detection（最高優先級）

**測試圖片**:
- `hdr_backlight_window_severe.jpg`
- `hdr_backlight_sunset_extreme.jpg`

**檢測標準**:
```kotlin
// ExposureDetector.kt
subject.luminance < 76 (30%)
background.luminance > 178 (70%)
luminanceDiff > 80
```

**預期結果**:
- ✅ 光線狀態: `Backlight`
- ✅ 警告顏色: 深橙色 (0xFFFF6F00)
- ✅ 訊息: "逆光場景 - 強烈建議使用 HDR 模式以平衡主體與背景曝光"
- ✅ HDR 建議: `shouldUseHDR = true`

**Logcat 驗證**:
```bash
adb logcat -d | grep -E "(Backlight|isBacklit|shouldUseHDR)"
```

**預期輸出**:
```
ExposureDetector: Backlight check: subject=25.7, bg=122.0, backlit=true
AnalyzeLightingUseCase: Lighting with subject: ..., backlit=true, HDR=true
CameraViewModel: Lighting state updated: Backlight
```

---

#### 場景 2: HighContrast Detection

**測試圖片**:
- `hdr_high_contrast_window_interior.jpg`
- `hdr_high_contrast_directional_spotlight.jpg`

**檢測標準**:
```kotlin
// ExposureDetector.kt
(highlightRatio > 0.10 AND shadowRatio > 0.10) OR dynamicRange > 180
```

**預期結果**:
- ✅ 光線狀態: `HighContrast`
- ✅ 警告顏色: 橙色 (0xFFFFA726)
- ✅ 訊息: "高對比場景 - 建議使用 HDR 模式以保留亮部與暗部細節"
- ✅ HDR 建議: `shouldUseHDR = true`

**Logcat 驗證**:
```
ExposureDetector: Exposure: ..., HL=0.21, SH=0.39, DR=252.0
AnalyzeLightingUseCase: ..., isHighContrast=true
CameraViewModel: Lighting state updated: HighContrast
```

---

#### 場景 3: Overexposed Detection（修復後）

**測試圖片**:
- `hdr_overexposed_snow_glare.jpg` (高光比 75.59%)
- `hdr_overexposed_sunny_outdoor.jpg` (高光比 50.43%)

**檢測標準**:
```kotlin
// ExposureDetector.kt
highlightRatio > 0.15  // 15% of pixels in 240-255 range
```

**預期結果**:
- ✅ 光線狀態: `Overexposed`
- ✅ 警告顏色: 紅色 (0xFFE53935)
- ✅ 訊息: "過曝風險 - 建議降低曝光補償或使用 HDR 模式減少高光溢出"
- ✅ HDR 建議: `shouldUseHDR = depends on scene`

**Logcat 驗證**:
```
ExposureDetector: Exposure: avg=213.3, HL=0.756, SH=0.079, DR=255.0
AnalyzeLightingUseCase: ..., overexp=true, lowlight=false
CameraViewModel: Lighting state updated: Overexposed
```

**關鍵修復** (提交 `e59ec71`):
- Overexposed 優先級提升至 Priority 2（在 HighContrast 之前）
- 測試圖片高光比從 7.39% 提升至 75.59%

---

#### 場景 4: Underexposed Detection

**測試場景**:
- 低光環境照片
- 室內無補光照片

**檢測標準**:
```kotlin
// ExposureDetector.kt
avgLuminance < 51 (20% of 255)
```

**預期結果**:
- ✅ 光線狀態: `Underexposed`
- ✅ 警告顏色: 黃色 (0xFFFFB300)
- ✅ 訊息: "曝光不足 - 建議提高曝光補償或使用補光"
- ✅ HDR 建議: `shouldUseHDR = false`

---

#### 場景 5: Normal Lighting

**測試圖片**:
- `hdr_normal_soft_natural.jpg`
- `hdr_normal_outdoor_shade.jpg`

**檢測標準**:
```kotlin
// 無異常條件
!isBacklit && !isOverexposed && !isLowLight && !isHighContrast
```

**預期結果**:
- ✅ 光線狀態: `Normal`
- ✅ 無警告顯示
- ✅ HDR 建議: `shouldUseHDR = false`

---

### 測試流程

#### 步驟 1: APK 安裝與啟動
```bash
# 編譯
.\gradlew.bat assembleDebug

# 安裝
adb install -r app\build\outputs\apk\debug\app-debug.apk

# 啟動應用
adb shell am start -n com.akirachang.ai.photographer/.MainActivity

# 清除 logcat
adb logcat -c
```

#### 步驟 2: 測試每個場景

1. 在應用中載入測試圖片
2. 觀察螢幕上的 HDR Warning UI
3. 記錄顯示的警告顏色和訊息
4. 擷取 logcat 輸出

```bash
# 監控 logcat（實時）
adb logcat | grep -E "(ExposureDetector|AnalyzeLightingUseCase|CameraViewModel|HDRWarning)"

# 或擷取完整日誌
adb logcat -d > p0-2_real_device_test.log
```

#### 步驟 3: 驗證 HDR 自動啟用

**關鍵檢查點**:
1. ✅ Backlight/HighContrast 場景 → `shouldUseHDR = true`
2. ✅ Overexposed/Underexposed 場景 → 依據場景動態判斷
3. ✅ Normal 場景 → `shouldUseHDR = false`
4. ✅ HDR 警告 UI 正確顯示對應顏色和訊息

---

### 測試報告範本

```markdown
# P0-2 實機測試報告

**日期**: YYYY-MM-DD
**裝置**: [品牌 型號 Android 版本]
**APK**: app-debug.apk (Git: cb8ccfc)

## 測試結果

### Backlight
- 測試圖片: hdr_backlight_window_severe.jpg
- 預期狀態: Backlight
- 實際狀態: [填寫]
- HDR 建議: [true/false]
- 警告顏色: [填寫]
- 警告訊息: [填寫]
- ✅/❌

### HighContrast
- 測試圖片: hdr_high_contrast_window_interior.jpg
- 預期狀態: HighContrast
- 實際狀態: [填寫]
- ✅/❌

### Overexposed
- 測試圖片: hdr_overexposed_snow_glare.jpg
- 預期狀態: Overexposed
- 實際狀態: [填寫]
- ✅/❌

### Underexposed
- 測試場景: [描述]
- 實際狀態: [填寫]
- ✅/❌

### Normal
- 測試圖片: hdr_normal_soft_natural.jpg
- 預期狀態: Normal
- 實際狀態: [填寫]
- ✅/❌

## 問題記錄

[記錄任何異常行為或 bug]

## Logcat 附件

[附上相關 logcat 輸出]
```

---

## 🎯 P0-3: 真實極端光線場景測試圖片收集

### 目標
收集真實世界極端光線場景照片，補充 AI 生成測試圖片的不足。

### 為何需要真實照片？

**AI 生成限制** (從 P0-1 發現):
- Gemini API 傾向生成「視覺平衡」的圖片
- 極端高光（240-255）被視為「瑕疵」而避免
- 初始生成的 `hdr_overexposed_snow_glare.jpg` 僅 7.39% 高光比（需 15%）
- 需要極其明確的提示詞才能達到標準

**真實照片優勢**:
- 自然的極端光線條件
- 真實的相機傳感器響應
- 實際的雜訊和色彩偏移
- 邊緣案例場景（AI 難以想像）

---

### 收集場景列表

#### 場景 1: 極端逆光（Backlight）

**拍攝時機**:
- 日出/日落時段（太陽低角度）
- 室內拍攝窗戶人物

**拍攝要點**:
- 主體（人臉）應在陰影中
- 背景極度明亮（天空/窗戶）
- 相機對焦於主體（不要對焦背景）
- 關閉 HDR 模式

**目標數值**:
- 主體平均亮度 < 76 (30% of 255)
- 背景平均亮度 > 178 (70% of 255)
- 亮度差 > 80

**範例場景**:
1. 室內人物站在窗前（逆光剪影）
2. 戶外日落時拍攝人像（太陽在背後）
3. 明亮店面玻璃前的人物（夜間）

**檔名規範**: `real_backlight_[描述]_[日期].jpg`
- 例: `real_backlight_window_indoor_20251227.jpg`

---

#### 場景 2: 高對比（HighContrast）

**拍攝時機**:
- 強烈方向性光源（聚光燈、落地窗側光）
- 明暗交界清晰的場景

**拍攝要點**:
- 畫面同時包含極亮和極暗區域
- 不是逆光（主體本身有亮暗對比）
- 動態範圍 > 180

**目標數值**:
- 高光比 > 10%
- 陰影比 > 10%
- 動態範圍 > 180

**範例場景**:
1. 側窗光拍攝人像（一半臉亮、一半臉暗）
2. 舞台聚光燈下的表演者
3. 夜間街燈下的場景

**檔名規範**: `real_high_contrast_[描述]_[日期].jpg`

---

#### 場景 3: 極度過曝（Overexposed）

**拍攝時機**:
- 正午強烈陽光
- 雪地/沙灘反射
- 白色背景 + 強光

**拍攝要點**:
- **手動增加曝光補償 +2 to +3 EV**
- 讓天空完全失去細節（純白）
- 主體細節也應部分流失

**目標數值**:
- **高光比 > 25%** (像素在 240-255 範圍)
- 平均亮度 > 200
- 天空應為純白 (255,255,255)

**範例場景**:
1. 雪地人像（正午陽光 + 曝光補償 +3）
2. 海灘/沙灘（強烈反射 + 過曝）
3. 白牆前人像（強光 + 曝光補償 +2）

**檔名規範**: `real_overexposed_[描述]_[日期].jpg`

**關鍵提示**:
- ⚠️ 這是最難拍攝的場景（需要故意拍「錯」）
- 必須手動調整曝光補償，不能依賴自動曝光
- 參考模擬器測試中的成功案例：
  - `hdr_overexposed_snow_glare.jpg`: HL=75.59%
  - `hdr_overexposed_sunny_outdoor.jpg`: HL=50.43%

---

#### 場景 4: 嚴重欠曝（Underexposed）

**拍攝時機**:
- 室內低光環境
- 夜間無補光
- 陰天樹蔭下

**拍攝要點**:
- 關閉閃光燈
- 可手動降低曝光補償 -2 EV
- 整體畫面應非常暗

**目標數值**:
- 平均亮度 < 51 (20% of 255)
- 陰影比 > 40%

**範例場景**:
1. 室內無燈光環境
2. 夜間街道（路燈稀少）
3. 陰天樹林深處

**檔名規範**: `real_underexposed_[描述]_[日期].jpg`

---

#### 場景 5: 邊緣案例（Edge Cases）

**特殊場景**:
1. **混合光源**
   - 室內外交界（門口/窗邊）
   - 同時有日光和人工光源
   - 檔名: `real_mixed_lighting_[描述]_[日期].jpg`

2. **部分逆光**
   - 側逆光（非完全逆光）
   - 主體有部分亮度但背景更亮
   - 檔名: `real_partial_backlight_[描述]_[日期].jpg`

3. **過曝 + 高對比**
   - 同時滿足過曝和高對比條件
   - 測試優先級排序（應歸類為 Overexposed）
   - 檔名: `real_overexp_highcontrast_[描述]_[日期].jpg`

---

### 圖片收集規範

#### 技術要求

1. **解析度**
   - 最小: 1280x960 (1.2MP)
   - 建議: 1920x1440 或更高
   - 寬高比: 3:4 (人像) 或 4:3 (風景)

2. **格式**
   - JPEG (.jpg)
   - 無後製處理（RAW 轉 JPEG 可接受）
   - 保留 EXIF 資料

3. **拍攝設置**
   - **關閉 HDR 模式**（重要！）
   - 關閉美顏/濾鏡
   - 自動對焦或手動對焦主體
   - 記錄拍攝時的曝光補償設定

#### 檔案組織

```
app/src/debug/assets/test_images_real/
├── backlight/
│   ├── real_backlight_window_indoor_20251227.jpg
│   └── real_backlight_sunset_outdoor_20251227.jpg
├── high_contrast/
│   ├── real_high_contrast_side_light_20251227.jpg
│   └── real_high_contrast_spotlight_20251227.jpg
├── overexposed/
│   ├── real_overexposed_snow_20251227.jpg
│   ├── real_overexposed_beach_20251227.jpg
│   └── real_overexposed_white_wall_20251227.jpg
├── underexposed/
│   ├── real_underexposed_indoor_night_20251227.jpg
│   └── real_underexposed_shade_20251227.jpg
└── edge_cases/
    ├── real_mixed_lighting_doorway_20251227.jpg
    └── real_partial_backlight_side_20251227.jpg
```

#### 元數據記錄

為每張照片創建配對的 `.txt` 文件記錄拍攝資訊：

**範例**: `real_overexposed_snow_20251227.txt`
```
場景: 雪地人像
日期: 2025-12-27
時間: 12:30 (正午)
地點: 戶外公園
天氣: 晴天
相機: Google Pixel 7
曝光補償: +3 EV
ISO: 100
快門: 1/500s
光圈: f/2.0
HDR: 關閉
預期狀態: Overexposed
備註: 雪地反射非常強烈，天空完全過曝
```

---

### 驗證流程

#### 步驟 1: 圖片預檢

使用 Python 腳本快速驗證圖片是否符合標準：

```python
# tools/validate_real_images.py
from PIL import Image
import numpy as np
import os

def validate_overexposed(image_path, min_highlight_ratio=0.25):
    """驗證過曝圖片是否達標"""
    img = Image.open(image_path).convert('L')
    arr = np.array(img)

    # 計算高光比
    highlight_ratio = (arr > 240).sum() / arr.size
    avg_luminance = arr.mean()

    print(f"\n{os.path.basename(image_path)}:")
    print(f"  Highlight ratio: {highlight_ratio*100:.2f}% (target: >{min_highlight_ratio*100}%)")
    print(f"  Avg luminance: {avg_luminance:.2f}/255 (target: >200)")
    print(f"  Status: {'✅ PASS' if highlight_ratio > min_highlight_ratio else '❌ FAIL'}")

    return highlight_ratio > min_highlight_ratio

# 使用
validate_overexposed('app/src/debug/assets/test_images_real/overexposed/real_overexposed_snow_20251227.jpg')
```

#### 步驟 2: 應用內測試

1. 複製圖片到 `test_images/` 目錄
2. 更新 `TestImageProvider` 以包含真實圖片
3. 在模擬器/實機上運行應用
4. 驗證光線狀態檢測結果

#### 步驟 3: 建立測試報告

記錄每張圖片的檢測結果：

| 檔名 | 預期狀態 | 實際狀態 | 高光比 | 平均亮度 | 備註 |
|------|---------|---------|--------|---------|------|
| real_overexposed_snow_20251227.jpg | Overexposed | Overexposed ✅ | 32.5% | 215 | 符合預期 |
| real_backlight_window_20251227.jpg | Backlight | Backlight ✅ | 28.1% | 125 | 逆光效果良好 |

---

### 收集目標

**最低要求**:
- Backlight: 2 張
- HighContrast: 2 張
- **Overexposed: 3 張** (最重要，AI 生成困難)
- Underexposed: 2 張
- Edge Cases: 2 張

**總計**: 最少 11 張真實照片

**理想目標**:
- 每個場景 5 張以上
- 涵蓋不同天氣、時段、地點
- 總計 30+ 張

---

### 後續整合

收集完成後：

1. **更新測試套件**
   ```kotlin
   // TestImageProvider.kt
   private val realWorldImages = listOf(
       "real_backlight_window_indoor_20251227.jpg",
       "real_overexposed_snow_20251227.jpg",
       // ...
   )
   ```

2. **新增真實場景測試**
   ```kotlin
   @Test
   fun `real world overexposed snow scene should trigger Overexposed state`() {
       val result = analyzeLightingUseCase(realImage_overexposed_snow)
       assertTrue(result is LightingState.Overexposed)
   }
   ```

3. **建立真實場景測試報告**
   - 比較 AI 生成 vs 真實照片的檢測準確度
   - 識別演算法改進點
   - 調整檢測閾值（如需要）

---

## 📋 測試檢查清單

### P0-2 實機測試
- [ ] 準備實機並安裝 ADB 驅動
- [ ] 編譯並安裝 APK (`app-debug.apk`)
- [ ] 測試 Backlight 場景（2 張圖片）
- [ ] 測試 HighContrast 場景（2 張圖片）
- [ ] 測試 Overexposed 場景（2 張圖片）
- [ ] 測試 Underexposed 場景
- [ ] 測試 Normal 場景（2 張圖片）
- [ ] 驗證 HDR 自動建議邏輯
- [ ] 擷取並分析 logcat 輸出
- [ ] 撰寫測試報告 (`P0-2_REAL_DEVICE_TEST_REPORT.md`)

### P0-3 真實場景收集
- [ ] 拍攝 Backlight 場景（至少 2 張）
- [ ] 拍攝 HighContrast 場景（至少 2 張）
- [ ] 拍攝 Overexposed 場景（至少 3 張，**重點**）
- [ ] 拍攝 Underexposed 場景（至少 2 張）
- [ ] 拍攝 Edge Cases（至少 2 張）
- [ ] 為每張照片記錄元數據（.txt 文件）
- [ ] 使用 Python 腳本驗證圖片數值
- [ ] 整合到測試套件
- [ ] 在模擬器/實機驗證檢測結果
- [ ] 撰寫真實場景測試報告 (`P0-3_REAL_WORLD_TEST_REPORT.md`)

---

## 🎓 技術參考

### 光線檢測閾值速查表

| 狀態 | 檢測條件 | 程式碼位置 |
|-----|---------|-----------|
| **Backlight** | subject < 76 AND bg > 178 AND diff > 80 | `ExposureDetector.kt:146-163` |
| **HighContrast** | (HL > 10% AND SH > 10%) OR DR > 180 | `ExposureDetector.kt:76-78` |
| **Overexposed** | HL > 15% (240-255 range) | `ExposureDetector.kt:74` |
| **Underexposed** | avgLum < 51 (20% of 255) | `ExposureDetector.kt:75` |
| **Normal** | 無異常條件 | `AnalyzeLightingUseCase.kt:196-213` |

### 優先級排序邏輯

```kotlin
// AnalyzeLightingUseCase.kt:147-214
when {
    isBacklit       -> Backlight       // Priority 1 (最高)
    isOverexposed   -> Overexposed     // Priority 2
    isHighContrast  -> HighContrast    // Priority 3
    isLowLight      -> Underexposed    // Priority 4
    else            -> Normal          // Priority 5 (最低)
}
```

**重要修復** (提交 `e59ec71`):
- Overexposed 從 Priority 3 提升至 Priority 2
- 理由：過曝是具體問題，高對比是場景特性

### HDR 建議邏輯

```kotlin
// ExposureAnalysis.kt
val shouldUseHDR = when {
    isBacklit      -> true   // 逆光必定建議 HDR
    isHighContrast -> true   // 高對比建議 HDR
    isOverexposed  -> depends // 依據 dynamicRange 判斷
    isLowLight     -> false  // 低光不建議 HDR
    else           -> false  // 正常光線不需要
}
```

---

## 📚 相關文件

- [OVEREXPOSED_FIX_REPORT_v2.6.0_20251227.md](OVEREXPOSED_FIX_REPORT_v2.6.0_20251227.md) - Overexposed Bug 完整分析
- [TEST_REPORT_P0-1_EMULATOR_v2.6.0_20251226.md](TEST_REPORT_P0-1_EMULATOR_v2.6.0_20251226.md) - P0-1 模擬器測試報告
- [V2.7.0_AUTOMATION_IMPLEMENTATION_SUMMARY.md](V2.7.0_AUTOMATION_IMPLEMENTATION_SUMMARY.md) - v2.7.0 自動化架構

---

## 🚀 開始測試

### 快速啟動指令

```bash
# P0-2: 實機測試
.\gradlew.bat assembleDebug
adb install -r app\build\outputs\apk\debug\app-debug.apk
adb shell am start -n com.akirachang.ai.photographer/.MainActivity
adb logcat -c && adb logcat | grep -E "(ExposureDetector|AnalyzeLightingUseCase)"

# P0-3: 驗證真實圖片
python tools/validate_real_images.py app/src/debug/assets/test_images_real/overexposed/*.jpg
```

---

**文件版本**: 1.0
**作者**: Claude Sonnet 4.5
**最後更新**: 2025-12-27
