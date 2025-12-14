# v2.4.6 模擬器測試計畫

**版本**: v2.4.6
**建立日期**: 2025-12-14
**測試目標**: 使用模擬器驗證 v2.4.6 的核心功能

---

## 📋 v2.4.6 核心功能清單

根據程式碼分析，v2.4.6 包含以下核心功能：

### 1. **構圖分析系統** (AnalyzeCompositionUseCase)
- ✅ 三分法分析 (Rule of Thirds) - 25% 權重
- ✅ 頭部空間檢查 (Headroom) - 22% 權重
- ✅ 邊緣距離驗證 (Edge Distance) - 25% 權重
- ✅ 眼睛位置分析 (Eye Position) - 18% 權重 (v2.4.6 降低權重)
- ✅ 多主體支援 (Multi-Subject) - 10% 權重
- ✅ 視覺構圖引導框 (Composition Guide Frame) - v2.4.0

### 2. **光影分析系統** (AnalyzeLightingUseCase - v2.2.0)
- ✅ 逆光偵測
- ✅ 高對比偵測
- ✅ 低光偵測
- ✅ Auto HDR 建議 (v2.3.0)

### 3. **設備感測器整合** (v2.1.0)
- ✅ 方向感測器 (OrientationData)
- ✅ 水平儀指示器

### 4. **Overlay 系統**
- ✅ 格線顯示 (Grid)
- ✅ 構圖提示 (Composition Hint)
- ✅ 偵測覆蓋層 (Detection Overlay)
- ✅ 水平儀指示器 (Level Indicator)
- ✅ 方向指示器 (Directional Indicator - v2.1.0)
- ✅ 構圖引導框 (Composition Guide - v2.4.0)

### 5. **相機功能**
- ✅ 前後鏡頭切換
- ✅ 前鏡頭鏡像座標處理 (v2.3.1)
- ✅ 人臉偵測穩定化 (v2.2.1) - 防止閃爍

---

## 🎯 模擬器測試策略

### 適合模擬器測試 (80%)
| 功能 | 適用度 | 測試方法 |
|------|--------|---------|
| 構圖算法 | ✅ 100% | 單元測試 + 靜態圖片 |
| 光影分析 | ✅ 100% | 單元測試 + 靜態圖片 |
| UI/Overlay | ✅ 100% | UI 測試 + 視覺驗證 |
| 座標轉換 | ✅ 100% | 單元測試 |
| ViewModel 邏輯 | ✅ 100% | 單元測試 |
| ML Kit 架構 | ✅ 90% | 靜態圖片驗測 |

### 需實機驗證 (20%)
| 功能 | 限制 | 原因 |
|------|------|------|
| 實時人臉偵測 | ⚠️ 部分 | FPS 和延遲需實機 |
| 相機預覽流 | ⚠️ 部分 | 效果和性能需實機 |
| 感測器精度 | ⚠️ 部分 | 模擬器感測器不準確 |
| 光影實時分析 | ⚠️ 部分 | 真實光線條件需實機 |

---

## 🧪 測試階段規劃

### 階段 1: 整合現有架構 (1 小時)
**目標**: 將現有 CameraViewModel 整合新的 CameraDataSource

**任務**:
1. 更新 CameraRepository 注入 CameraDataSource
2. 移除直接的 CameraX 依賴
3. 使用 FakeCameraDataSource 提供測試圖片流
4. 驗證構建成功

**驗收標準**:
- ✅ 構建無錯誤
- ✅ ViewModel 正確注入 CameraDataSource
- ✅ 模擬器模式使用 FakeCameraDataSource

### 階段 2: 準備測試資源 (30 分鐘)
**目標**: 使用 v2.4.6-Testing 的截圖作為測試圖片

**任務**:
1. 複製 v2.4.6-Testing/optimized 的圖片到 test_images
2. 重新命名為有意義的測試檔名
3. 建立測試圖片索引

**測試圖片清單**:
```
app/src/debug/assets/test_images/
├── portrait_front_good.jpg        # 前鏡頭良好構圖
├── portrait_front_centered.jpg    # 前鏡頭置中
├── portrait_back_good.jpg         # 後鏡頭良好構圖
├── portrait_back_rule_thirds.jpg  # 後鏡頭三分法
├── portrait_back_headroom.jpg     # 後鏡頭頭部空間
└── portrait_multi_subject.jpg     # 多人像
```

### 階段 3: 單元測試 - 構圖分析 (2 小時)
**目標**: 驗證構圖分析算法的正確性

**測試案例**:

#### 3.1 眼睛位置分析測試
```kotlin
@Test
fun `眼睛在理想位置時應得高分`() {
    // Given: 眼睛在上三分線
    val eyeY = imageHeight / 3f

    // When: 分析構圖
    val result = analyzeCompositionUseCase(...)

    // Then: 眼睛位置分數應 > 0.8
    assertThat(result.eyePositionScore).isGreaterThan(0.8f)
}

@Test
fun `眼睛過高或過低應降低分數`() {
    // Given: 眼睛在畫面最上方
    val eyeY = imageHeight * 0.1f

    // When: 分析構圖
    val result = analyzeCompositionUseCase(...)

    // Then: 眼睛位置分數應 < 0.5
    assertThat(result.eyePositionScore).isLessThan(0.5f)
}
```

#### 3.2 頭部空間測試
```kotlin
@Test
fun `理想頭部空間應得高分`() {
    // Given: 頭部空間 5-15%
    val headroom = 0.10f  // 10%

    // When: 分析構圖
    val result = analyzeCompositionUseCase(...)

    // Then: 頭部空間分數應 > 0.8
    assertThat(result.headroomScore).isGreaterThan(0.8f)
}

@Test
fun `頭部空間過少應提示`() {
    // Given: 頭部空間 < 5%
    val headroom = 0.03f

    // When: 分析構圖
    val result = analyzeCompositionUseCase(...)

    // Then: 應有警告訊息
    assertThat(result.compositionState)
        .isInstanceOf(CompositionState.NeedsAdjustment::class.java)
}
```

#### 3.3 權重調整測試 (v2.4.6)
```kotlin
@Test
fun `驗證 v2_4_6 權重分配`() {
    // Then: 權重總和應為 1.0
    val totalWeight =
        WEIGHT_EYE_POSITION +           // 0.18
        WEIGHT_RULE_OF_THIRDS +         // 0.25
        WEIGHT_HEADROOM +               // 0.22
        WEIGHT_EDGE_DISTANCE +          // 0.25
        WEIGHT_SUBJECT_DETECTION        // 0.10

    assertThat(totalWeight).isWithin(0.001f).of(1.0f)
}

@Test
fun `眼睛位置權重降低不應過度影響總分`() {
    // Given: 眼睛位置略差，但其他指標良好
    val detection = createDetectionWithPoorEyes()

    // When: 分析構圖
    val result = analyzeCompositionUseCase(detection, ...)

    // Then: 總分仍應可達到良好標準
    assertThat(result.overallScore).isGreaterThan(0.7f)
}
```

#### 3.4 前鏡頭鏡像測試 (v2.3.1)
```kotlin
@Test
fun `前鏡頭座標應正確鏡像`() {
    // Given: 前鏡頭模式
    val isFrontCamera = true
    val originalX = 100f  // 左側

    // When: 轉換座標
    val transformed = transformCoordinates(originalX, isFrontCamera)

    // Then: X 座標應鏡像到右側
    assertThat(transformed.x).isGreaterThan(imageWidth - originalX)
}
```

### 階段 4: 單元測試 - 光影分析 (1 小時)
**目標**: 驗證光影分析系統

**測試案例**:

```kotlin
@Test
fun `逆光場景應觸發 HDR 建議`() {
    // Given: 逆光圖片（暗部多、亮部多）
    val bitmap = loadTestImage("backlight_severe.jpg")

    // When: 分析光影
    val result = analyzeLightingUseCase(bitmap)

    // Then: 應建議 HDR
    assertThat(result.lightingState).isInstanceOf(LightingState.Backlit::class.java)
    assertThat(result.recommendHDR).isTrue()
}

@Test
fun `低光場景應提示`() {
    // Given: 低光圖片
    val bitmap = loadTestImage("low_light.jpg")

    // When: 分析光影
    val result = analyzeLightingUseCase(bitmap)

    // Then: 應標示低光
    assertThat(result.lightingState).isInstanceOf(LightingState.LowLight::class.java)
}
```

### 階段 5: ViewModel 整合測試 (1.5 小時)
**目標**: 驗證 ViewModel 整體流程

**測試案例**:

```kotlin
@Test
fun `預覽流應觸發構圖分析`() = runTest {
    // Given: ViewModel 已初始化
    val viewModel = createViewModel()

    // When: 接收預覽幀
    viewModel.startAnalysis()
    advanceUntilIdle()

    // Then: compositionState 應更新
    viewModel.compositionState.test {
        val state = awaitItem()
        assertThat(state).isNotEqualTo(CompositionState.NoSubject)
    }
}

@Test
fun `偵測結果應穩定化避免閃爍`() = runTest {
    // Given: ViewModel 配置穩定化參數
    val viewModel = createViewModel()

    // When: 發送單次人臉偵測結果
    emitSingleFaceDetection()

    // Then: 應等待 MIN_DETECTIONS_TO_SHOW 次才顯示
    // (MIN_DETECTIONS_TO_SHOW = 3)
    assertThat(viewModel.state.value.detectionResult).isNull()

    // When: 發送第 3 次
    repeat(2) { emitSingleFaceDetection() }

    // Then: 應顯示
    assertThat(viewModel.state.value.detectionResult).isNotNull()
}
```

### 階段 6: UI/Overlay 視覺測試 (2 小時)
**目標**: 在模擬器上驗證 UI 顯示正確性

**測試方法**: 手動 + 截圖對比

**測試清單**:

#### 6.1 構圖引導框 (v2.4.0)
- [ ] 引導框正確顯示在理想位置
- [ ] 引導框顏色正確（綠色 = 良好，黃色 = 需調整）
- [ ] 引導框大小適當
- [ ] 引導框不遮擋主體

#### 6.2 格線系統
- [ ] 三分法格線位置準確 (33%, 67%)
- [ ] 格線顏色可見但不突兀
- [ ] 格線可開關

#### 6.3 構圖提示
- [ ] 提示文字清晰可讀
- [ ] 提示位置不遮擋主體
- [ ] 提示內容準確（中文專業建議）
- [ ] 方向指示器正確指示移動方向

#### 6.4 前後鏡頭切換
- [ ] 前鏡頭顯示正確（鏡像）
- [ ] 後鏡頭顯示正確
- [ ] 切換流暢無閃爍
- [ ] Overlay 座標正確轉換

### 階段 7: 整合測試腳本 (1 小時)
**目標**: 建立自動化測試腳本

**腳本內容**:

```bash
#!/bin/bash
# v2.4.6 自動化測試腳本

echo "🧪 v2.4.6 模擬器測試開始..."

# 1. 構建
echo "📦 構建模擬器版本..."
./gradlew.bat assembleEmulatorDebug

# 2. 單元測試
echo "🧪 執行單元測試..."
./gradlew.bat testEmulatorDebugUnitTest

# 3. 生成測試報告
echo "📊 生成測試報告..."
./gradlew.bat testEmulatorDebugUnitTest --tests "*CompositionTest"
./gradlew.bat testEmulatorDebugUnitTest --tests "*LightingTest"

# 4. 安裝到模擬器
echo "📱 安裝到模擬器..."
./gradlew.bat installEmulatorDebug

# 5. 啟動應用並收集日誌
echo "📝 啟動應用並收集日誌..."
adb shell am start -n local.mylab.smartcameraapp1.emulator/.MainActivity
adb logcat -c
adb logcat | grep -E "(AnalyzeComposition|CameraModule|FakeCameraDataSource)" > v2.4.6_emulator_test.log &

echo "✅ 測試環境準備完成！"
echo "📊 測試報告: app/build/reports/tests/testEmulatorDebugUnitTest/index.html"
echo "📝 日誌檔案: v2.4.6_emulator_test.log"
```

---

## 📊 測試驗收標準

### 功能驗收
- [ ] 所有單元測試通過率 > 90%
- [ ] 構圖分析準確率 > 85%
- [ ] 光影分析準確率 > 80%
- [ ] UI/Overlay 顯示正確無誤
- [ ] 前後鏡頭切換正常
- [ ] 無記憶體洩漏
- [ ] 無 ANR (Application Not Responding)

### 性能驗收（模擬器）
- [ ] 預覽流暢（即使是假資料也應流暢）
- [ ] 構圖分析延遲 < 100ms（單元測試）
- [ ] UI 重繪 < 16ms（60 FPS）
- [ ] 記憶體使用 < 200MB

### 程式碼品質
- [ ] 測試覆蓋率 > 70%
- [ ] Lint 無 error
- [ ] 無 Kotlin warnings
- [ ] 文檔完整

---

## 🐛 已知問題追蹤

### v2.4.6 已知問題
- [ ] 眼睛位置權重過高導致總分偏低 ✅ 已修正（降為 18%）
- [ ] 前鏡頭鏡像座標 ✅ 已修正 (v2.3.1)
- [ ] 人臉偵測閃爍 ✅ 已修正 (v2.2.1)

---

## 📝 測試記錄範本

### 測試執行記錄
```markdown
**測試日期**: 2025-12-14
**測試人員**: [Your Name]
**版本**: v2.4.6
**環境**: Emulator (Pixel 6, API 34)

### 測試結果
- 單元測試: ✅ PASS (125/130)
- UI 測試: ✅ PASS
- 整合測試: ✅ PASS

### 發現問題
1. [問題描述]
   - 嚴重性: High/Medium/Low
   - 重現步驟: ...
   - 預期結果: ...
   - 實際結果: ...

### 截圖
- [附上相關截圖]

### 日誌
- [附上相關日誌]
```

---

## 🎯 下一步行動

### 立即執行
1. **整合 CameraDataSource** - 將現有 ViewModel 整合新架構
2. **準備測試圖片** - 從 v2.4.6-Testing 複製優化後的圖片
3. **執行階段 3** - 構圖分析單元測試

### 短期目標 (1-2 天)
1. 完成所有單元測試
2. 在模擬器上驗證 UI
3. 生成測試報告

### 中期目標 (3-5 天)
1. 建立自動化測試腳本
2. 整合 CI/CD（如果需要）
3. 準備實機驗證清單

---

**建立者**: Claude Code
**文檔版本**: 1.0
**最後更新**: 2025-12-14
