# v2.4.6 模擬器測試 - 快速指南

**版本**: v2.4.6
**測試環境**: 模擬器（Emulator）
**最後更新**: 2025-12-14

---

## 🚀 快速開始（5 分鐘）

### 方法 1: 使用批次腳本（推薦）

```batch
# 執行完整測試
test-v246-emulator.bat

# 僅執行單元測試
test-v246-emulator.bat unit

# 快速測試（僅構圖分析）
test-v246-emulator.bat quick
```

### 方法 2: VSCode 任務

1. 按 `Ctrl+Shift+P`
2. 選擇 `Tasks: Run Task`
3. 選擇 `🧪 Run Unit Tests`

### 方法 3: 手動執行

```bash
# 1. 構建
./gradlew.bat assembleEmulatorDebug

# 2. 測試
./gradlew.bat testEmulatorDebugUnitTest

# 3. 查看報告
start app/build/reports/tests/testEmulatorDebugUnitTest/index.html

# 4. 安裝（需先啟動模擬器）
./gradlew.bat installEmulatorDebug
```

---

## 📊 v2.4.6 關鍵測試點

### 1. 權重調整驗證 ✅
**測試**: 眼睛位置權重從 25% 降為 18%

```kotlin
@Test
fun `eye position weight should be reduced to 18 percent in v2_4_6`()
```

**預期結果**: 即使眼睛位置略差，總分仍應合理（> 0.6）

### 2. 眼睛位置分析 ✅
**測試**: 眼睛在理想位置（上三分線）

```kotlin
@Test
fun `eyes at ideal position (upper third) should score high`()
```

**預期結果**: CompositionState.Good

### 3. 頭部空間檢查 ✅
**測試**: 理想頭部空間 5-15%

```kotlin
@Test
fun `ideal headroom (5-15 percent) should score high`()
```

**預期結果**: 高分數或 Good 狀態

### 4. 前鏡頭鏡像 ✅
**測試**: 前鏡頭 X 座標正確鏡像

```kotlin
@Test
fun `front camera should mirror X coordinates`()
```

**預期結果**: 前後鏡頭皆返回有效結果

### 5. 多主體偵測 ✅
**測試**: 偵測多張人臉

```kotlin
@Test
fun `multiple faces should be detected`()
```

**預期結果**: 正確處理多主體

---

## 🎯 測試執行順序

### 階段 1: 單元測試（15 分鐘）
```bash
# 僅測試構圖分析
./gradlew.bat testEmulatorDebugUnitTest --tests "*AnalyzeCompositionUseCaseTest"

# 測試所有 Use Case
./gradlew.bat testEmulatorDebugUnitTest --tests "*.usecase.*"
```

**驗收標準**:
- ✅ 所有測試通過
- ✅ 測試覆蓋率 > 70%
- ✅ 無 flaky tests

### 階段 2: 構建與安裝（5 分鐘）
```bash
# 構建
./gradlew.bat assembleEmulatorDebug

# 檢查模擬器
adb devices

# 安裝
./gradlew.bat installEmulatorDebug
```

**驗收標準**:
- ✅ BUILD SUCCESSFUL
- ✅ APK 大小合理 (< 100MB)
- ✅ 安裝成功

### 階段 3: 手動 UI 測試（20 分鐘）
在模擬器上執行以下測試：

#### 3.1 構圖引導框
- [ ] 引導框顯示在理想位置（上三分線附近）
- [ ] 引導框顏色正確（綠色/黃色）
- [ ] 引導框大小適當

#### 3.2 格線系統
- [ ] 三分法格線正確顯示
- [ ] 格線可開關

#### 3.3 構圖提示
- [ ] 提示文字清晰可讀（中文）
- [ ] 提示內容準確
- [ ] 方向指示器正確

#### 3.4 前後鏡頭切換
- [ ] 前鏡頭顯示正確（鏡像）
- [ ] 後鏡頭顯示正確
- [ ] 切換流暢

### 階段 4: 日誌分析（10 分鐘）
```bash
# 收集日誌
adb logcat | findstr "AnalyzeComposition CameraModule FakeCameraDataSource"
```

**檢查項目**:
- ✅ 使用 FakeCameraDataSource（模擬器模式）
- ✅ 構圖分析正常執行
- ✅ 無異常錯誤
- ✅ 無記憶體警告

---

## 📝 測試報告範本

### 快速測試記錄
```markdown
**測試日期**: 2025-12-14
**測試人員**: [Your Name]
**版本**: v2.4.6-emulator
**環境**: Emulator (Pixel 6, API 34)

### 單元測試
- 執行: ✅ / ❌
- 通過率: ___%
- 失敗案例: [列出]

### 構建
- 構建時間: ___ 秒
- APK 大小: ___ MB
- 安裝: ✅ / ❌

### UI 測試
- 構圖引導框: ✅ / ❌
- 格線系統: ✅ / ❌
- 構圖提示: ✅ / ❌
- 相機切換: ✅ / ❌

### 問題
1. [問題描述]
2. [問題描述]

### 截圖
- [附上截圖]
```

---

## 🐛 常見問題

### Q1: 測試失敗 - "Unresolved reference"
**解決**:
```bash
./gradlew.bat clean
./gradlew.bat build
```

### Q2: 模擬器未連接
**解決**:
```bash
# 列出可用模擬器
emulator -list-avds

# 啟動模擬器
emulator -avd <emulator_name>
```

### Q3: FakeCameraDataSource 無測試圖片
**解決**: 系統會自動生成備用圖片（灰色背景 + 白色矩形）

### Q4: 測試報告無法開啟
**位置**:
```
app/build/reports/tests/testEmulatorDebugUnitTest/index.html
```

---

## 📊 預期結果

### 單元測試（AnalyzeCompositionUseCaseTest）
```
✅ verify v2_4_6 weight allocation sums to 1_0
✅ eye position weight should be reduced to 18 percent in v2_4_6
✅ eyes at ideal position (upper third) should score high
✅ eyes too high should lower score
✅ eyes too low should lower score
✅ ideal headroom (5-15 percent) should score high
✅ insufficient headroom (less than 5 percent) should suggest adjustment
✅ front camera should mirror X coordinates
✅ no faces detected should return NoSubject state
✅ multiple faces should be detected
```

**預期通過率**: 100% (10/10)

### 構建
```
BUILD SUCCESSFUL in 40s
```

### 日誌
```
D/CameraModule: 🎮 Using FakeCameraDataSource (Emulator Mode)
D/FakeCameraDataSource: Starting preview stream
D/FakeCameraDataSource: Emitted test image 1920x1080
D/AnalyzeCompositionUseCase: Analyzing composition...
D/AnalyzeCompositionUseCase: Eye position score: 0.85
D/AnalyzeCompositionUseCase: Overall score: 0.78
D/AnalyzeCompositionUseCase: Result: CompositionState.Good
```

---

## 🎯 成功標準

- ✅ 所有單元測試通過（10/10）
- ✅ 構建成功
- ✅ 應用可在模擬器上運行
- ✅ 構圖分析正常工作
- ✅ UI 顯示正確
- ✅ 無崩潰或 ANR
- ✅ 日誌無嚴重錯誤

---

## 📚 相關文檔

- [完整測試計畫](V2_4_6_EMULATOR_TEST_PLAN.md)
- [模擬器開發指南](EMULATOR_DEVELOPMENT_GUIDE.md)
- [快速開始](QUICK_START.md)

---

**建立者**: Claude Code
**狀態**: ✅ 可開始測試
**預計時間**: 50 分鐘（全流程）
