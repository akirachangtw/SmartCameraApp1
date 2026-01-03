# HDR 手動控制功能實作報告

**版本**: v2.6.1
**日期**: 2025-12-28
**作者**: Claude Sonnet 4.5
**狀態**: ✅ 實作完成，待實機測試

---

## 📋 功能概述

SmartCameraApp v2.6.1 新增 **HDR 手動控制功能**，允許用戶手動控制 HDR 模式，取代原本僅依賴光線分析的自動 HDR 邏輯。

### 功能特點

- 三種 HDR 模式: AUTO（自動）/ ON（強制開啟）/ OFF（強制關閉）
- 一鍵循環切換模式
- 視覺化圖示顯示當前模式
- AUTO 模式保留原有光線警告 UI
- ON/OFF 模式隱藏光線警告（用戶已手動控制）

---

## 🎯 使用場景

### AUTO 模式（預設）
**行為**: 根據光線分析自動建議 HDR
**適用場景**:
- 一般拍攝（讓系統智能判斷）
- 初學者
- 不確定是否需要 HDR 時

**視覺回饋**:
- 圖示: ⭐ AutoAwesome（星星）
- 顏色: 白色
- 顯示光線警告 UI

### ON 模式（強制開啟）
**行為**: 永久啟用 HDR，忽略光線分析
**適用場景**:
- 高對比場景（建築、風景）
- 逆光人像
- 用戶明確知道需要 HDR

**視覺回饋**:
- 圖示: ☀️ WbSunny（太陽）
- 顏色: 橙色 (#FFA726)
- 隱藏光線警告 UI

### OFF 模式（強制關閉）
**行為**: 永久停用 HDR，忽略光線分析
**適用場景**:
- 追求拍攝速度（HDR 會增加處理時間）
- 低光環境（HDR 可能造成鬼影）
- 需要自然曝光效果

**視覺回饋**:
- 圖示: 🔆 BrightnessHigh（亮度）
- 顏色: 灰色 (#757575)
- 隱藏光線警告 UI

---

## 🏗️ 技術架構

### 1. 資料模型層

#### HDRMode.kt (新增)
**位置**: `app/src/main/java/com/akirachang/ai/photographer/presentation/camera/model/HDRMode.kt`

```kotlin
enum class HDRMode {
    AUTO,   // 自動模式
    ON,     // 強制開啟
    OFF;    // 強制關閉

    val isAuto: Boolean
        get() = this == AUTO

    val isEnabled: Boolean
        get() = this == ON

    fun next(): HDRMode = when (this) {
        AUTO -> ON
        ON -> OFF
        OFF -> AUTO
    }

    val displayName: String
        get() = when (this) {
            AUTO -> "自動"
            ON -> "開啟"
            OFF -> "關閉"
        }
}
```

**設計要點**:
- 提供 `next()` 方法實現循環切換
- `isAuto` 屬性用於判斷是否在自動模式
- `displayName` 提供 Traditional Chinese 顯示名稱

---

### 2. UI 狀態層

#### CameraState.kt (修改)
**變更**:
```kotlin
data class CameraState(
    // ... 原有欄位
    val hdrMode: HDRMode = HDRMode.AUTO  // v2.6.1: 新增 HDR 模式狀態
)
```

#### CameraUiEvent.kt (修改)
**變更**:
```kotlin
sealed class CameraUiEvent {
    // ... 原有事件
    object ToggleHDR : CameraUiEvent()  // v2.6.1: 新增 HDR 切換事件
}
```

---

### 3. ViewModel 層

#### CameraViewModel.kt (修改)
**新增方法**:

```kotlin
private fun toggleHDR() {
    val newMode = _state.value.hdrMode.next()
    _state.update { it.copy(hdrMode = newMode) }
    applyHDRMode(newMode)
    Log.i(TAG, "HDR mode changed: ${newMode.displayName}")
}

private fun applyHDRMode(mode: HDRMode) {
    when (mode) {
        HDRMode.AUTO -> {
            // 重新啟用自動 HDR 邏輯
            val currentLighting = _lightingState.value
            handleAutoHDR(repository, currentLighting)
        }
        HDRMode.ON -> {
            // 強制啟用 HDR
            repository.enableHDRMode()
            _isAutoHDRActive.value = true
        }
        HDRMode.OFF -> {
            // 強制停用 HDR
            repository.disableHDRMode()
            _isAutoHDRActive.value = false
        }
    }
}
```

**修改邏輯**:
```kotlin
private fun handleAutoHDR(...) {
    // v2.6.1: 跳過非 AUTO 模式
    val currentHDRMode = _state.value.hdrMode
    if (!currentHDRMode.isAuto) {
        Log.d(TAG, "Auto HDR skipped - manual mode active: ${currentHDRMode.displayName}")
        return
    }
    // ... 原有自動 HDR 邏輯
}
```

**設計要點**:
- AUTO 模式下保留原有自動 HDR 邏輯
- ON/OFF 模式下繞過自動邏輯，由用戶完全控制
- 切換模式時立即應用新設定

---

### 4. UI 組件層

#### CameraScreen.kt (修改)
**HDR 控制按鈕**:
```kotlin
// HDR 按鈕 (v2.6.1)
IconButton(
    onClick = { viewModel.onEvent(CameraUiEvent.ToggleHDR) }
) {
    Icon(
        imageVector = when (state.hdrMode) {
            HDRMode.ON -> Icons.Default.WbSunny       // 太陽
            HDRMode.OFF -> Icons.Default.BrightnessHigh // 亮度
            HDRMode.AUTO -> Icons.Default.AutoAwesome // 星星
        },
        contentDescription = "HDR 模式: ${state.hdrMode.displayName}",
        tint = when (state.hdrMode) {
            HDRMode.ON -> Color(0xFFFFA726)   // 橙色
            HDRMode.OFF -> Color(0xFF757575)  // 灰色
            HDRMode.AUTO -> Color.White       // 白色
        },
        modifier = Modifier.size(32.dp)
    )
}
```

**按鈕位置**: 頂部控制列，閃光燈按鈕旁邊

**HDRWarningOverlay 調用**:
```kotlin
HDRWarningOverlay(
    lightingState = lightingState,
    hdrMode = state.hdrMode,  // v2.6.1: 傳遞 HDR 模式
    modifier = Modifier.fillMaxSize()
)
```

---

#### HDRWarningOverlay.kt (修改)
**新增參數**:
```kotlin
@Composable
fun HDRWarningOverlay(
    lightingState: LightingState,
    hdrMode: HDRMode = HDRMode.AUTO,  // v2.6.1: 新增
    modifier: Modifier = Modifier
) {
    // v2.6.1: 只在 AUTO 模式下顯示警告
    val shouldShowWarning = hdrMode.isAuto && lightingState.requiresWarning()

    AnimatedVisibility(
        visible = shouldShowWarning,
        // ...
    ) {
        // ... 原有警告 UI
    }
}
```

**設計要點**:
- 只有 AUTO 模式才顯示光線警告
- ON/OFF 模式下隱藏警告（用戶已做決定）
- 向後兼容（預設值為 AUTO）

---

## 📊 功能流程圖

```
用戶點擊 HDR 按鈕
    ↓
ToggleHDR Event
    ↓
toggleHDR()
    ↓
計算下一個模式: AUTO → ON → OFF → AUTO
    ↓
更新 UI 狀態: hdrMode
    ↓
applyHDRMode()
    ├─ AUTO: 調用 handleAutoHDR() → 根據光線分析
    ├─ ON: repository.enableHDRMode() → 強制開啟
    └─ OFF: repository.disableHDRMode() → 強制關閉
    ↓
UI 更新
    ├─ 按鈕圖示/顏色變化
    └─ HDRWarningOverlay 顯示/隱藏
```

---

## 🔧 實作細節

### 關鍵設計決策

#### 1. 為何選擇循環切換而非三選一按鈕？
- **優點**: UI 簡潔，單一按鈕即可完成所有操作
- **參考**: 類似閃光燈按鈕設計（OFF → AUTO → ON → OFF）
- **用戶體驗**: 快速切換，無需打開選單

#### 2. 為何在 ON/OFF 模式下隱藏光線警告？
- **邏輯**: 用戶已手動控制 HDR，不需要系統建議
- **避免混淆**: 防止 UI 顯示與用戶選擇衝突
- **清晰性**: 明確表示手動模式已接管控制

#### 3. 圖示選擇理由
- **AUTO (AutoAwesome)**: 星星圖示象徵「智能/自動」
- **ON (WbSunny)**: 太陽圖示象徵「高光/HDR」
- **OFF (BrightnessHigh)**: 亮度圖示，灰色表示「停用」

**注意**: 原計劃使用 `Hdr`, `HdrOn`, `HdrOff` 圖示，但 Material Icons 未提供，改用上述替代圖示。

---

## 🧪 測試計劃

### 單元測試

測試項目（待實作）:
1. `toggleHDR()` 正確循環切換模式
2. `applyHDRMode()` 正確應用各模式
3. `handleAutoHDR()` 在非 AUTO 模式下跳過
4. HDRWarningOverlay 僅在 AUTO 模式下顯示

### 整合測試

測試場景:
1. **AUTO → ON 切換**
   - 驗證 HDR 立即啟用
   - 驗證光線警告消失

2. **ON → OFF 切換**
   - 驗證 HDR 立即停用
   - 驗證光線警告仍隱藏

3. **OFF → AUTO 切換**
   - 驗證重新啟用自動邏輯
   - 驗證光線警告在符合條件時顯示

4. **AUTO 模式下光線變化**
   - 驗證 Backlight → HDR 自動啟用
   - 驗證 Normal → HDR 自動停用

### UI 測試

檢查項目:
1. HDR 按鈕正確顯示三種圖示
2. 按鈕顏色正確反映當前模式
3. 點擊按鈕平滑切換模式
4. HDRWarningOverlay 正確顯示/隱藏

---

## 📱 實機測試指南

### 前置條件

```bash
# 連接實機
adb devices

# 編譯最新版本
.\gradlew.bat assembleDebug

# 安裝 APK
adb install -r app\build\outputs\apk\device\debug\app-device-debug.apk

# 啟動應用
adb shell am start -n com.akirachang.ai.photographer/.MainActivity

# 清除 logcat
adb logcat -c
```

### 測試步驟

#### 測試 1: 模式切換功能
1. 觀察 HDR 按鈕初始狀態（應為 AUTO - 白色星星）
2. 點擊 HDR 按鈕 → 確認變為 ON（橙色太陽）
3. 點擊 HDR 按鈕 → 確認變為 OFF（灰色亮度）
4. 點擊 HDR 按鈕 → 確認變為 AUTO（白色星星）

**預期結果**: 循環切換正常，圖示/顏色正確

#### 測試 2: AUTO 模式光線警告
1. 設定為 AUTO 模式
2. 載入 `hdr_backlight_window_severe.jpg` 測試圖片
3. 確認顯示 Backlight 警告（深橙色）

**預期結果**: 光線警告正常顯示

#### 測試 3: ON 模式警告隱藏
1. 設定為 ON 模式
2. 載入同樣的逆光測試圖片
3. 確認光線警告 **不顯示**

**預期結果**: 警告隱藏，用戶完全控制

#### 測試 4: OFF 模式警告隱藏
1. 設定為 OFF 模式
2. 載入任何需要 HDR 的測試圖片
3. 確認光線警告 **不顯示**

**預期結果**: 警告隱藏，HDR 強制停用

#### 測試 5: AUTO 模式下 HDR 自動啟用
1. 設定為 AUTO 模式
2. 載入逆光/高對比圖片
3. 檢查 logcat 確認 "Auto HDR activated"

**Logcat 驗證**:
```bash
adb logcat -d | grep -E "(HDR mode|Auto HDR|toggleHDR)"
```

**預期輸出**:
```
CameraViewModel: HDR mode changed: 開啟
CameraViewModel: HDR mode: ON - Force enabled
AnalyzeLightingUseCase: Lighting state updated: Backlight
CameraViewModel: Auto HDR skipped - manual mode active: 開啟
```

---

## 📝 Logcat 監控

### 關鍵日誌

模式切換:
```
CameraViewModel: HDR mode changed: 自動/開啟/關閉
CameraViewModel: HDR mode: AUTO - Re-evaluating based on lighting state
CameraViewModel: HDR mode: ON - Force enabled
CameraViewModel: HDR mode: OFF - Force disabled
```

自動 HDR 跳過:
```
CameraViewModel: Auto HDR skipped - manual mode active: 開啟/關閉
```

自動 HDR 觸發:
```
CameraViewModel: Auto HDR activated
CameraViewModel: Auto HDR deactivated
```

---

## 🐛 已知問題與限制

### 圖示限制
- 原計劃使用 `Hdr`, `HdrOn`, `HdrOff` 圖示
- Material Icons (Compose) 未提供這些圖示
- **解決方案**: 使用 `AutoAwesome`, `WbSunny`, `BrightnessHigh` 替代
- **未來改進**: 考慮自定義 SVG 圖示

### 相機控制延遲
- CameraControl 可能在相機初始化期間不可用
- **表現**: 偶爾出現 "Cannot enable/disable HDR: CameraControl not available" 警告
- **影響**: 輕微（不影響功能，僅延遲 HDR 應用）
- **未來改進**: 新增 CameraControl 就緒檢查

### 狀態持久化
- 當前 HDR 模式未持久化
- **表現**: 應用重啟後重置為 AUTO
- **未來改進**: 使用 DataStore 保存用戶偏好

---

## 📊 程式碼統計

### 新增檔案
- `HDRMode.kt`: 79 行（資料模型）

### 修改檔案
| 檔案 | 新增行數 | 修改行數 | 主要變更 |
|------|---------|---------|---------|
| CameraState.kt | 3 | 2 | 新增 `hdrMode` 欄位和 `ToggleHDR` 事件 |
| CameraViewModel.kt | 67 | 12 | 新增 `toggleHDR()`, `applyHDRMode()`, 修改 `handleAutoHDR()` |
| CameraScreen.kt | 25 | 5 | 新增 HDR 控制按鈕，更新 imports |
| HDRWarningOverlay.kt | 8 | 5 | 新增 `hdrMode` 參數，更新顯示邏輯 |

**總計**: ~182 行新增/修改

---

## 🎯 後續建議

### 短期 (v2.6.2)
1. **單元測試**
   - 為 `HDRMode`, `toggleHDR()`, `applyHDRMode()` 編寫測試
   - 驗證自動 HDR 跳過邏輯

2. **UI 改進**
   - 新增 Toast 提示當前模式（可選）
   - 考慮使用自定義 HDR 圖示

3. **狀態持久化**
   - 使用 DataStore 保存 HDR 模式偏好
   - 應用重啟後恢復上次設定

### 中期 (v2.7.0)
1. **進階控制**
   - 新增長按 HDR 按鈕顯示詳細說明
   - 新增 HDR 強度控制（如果相機 API 支援）

2. **智能建議**
   - 在 AUTO 模式下，針對特定場景顯示「建議使用 ON/OFF」

3. **使用統計**
   - 記錄用戶最常使用的 HDR 模式
   - 根據使用習慣調整預設值

### 長期 (v3.0.0)
1. **場景識別整合**
   - 結合物體檢測自動判斷最佳 HDR 模式
   - 例如：檢測到建築 → 建議 ON，檢測到人像 → 建議 AUTO

2. **自定義預設值**
   - 允許用戶設定特定場景的預設 HDR 模式
   - 例如：前鏡頭永遠 OFF，後鏡頭 AUTO

---

## ✅ 實作檢查清單

- [x] 建立 HDRMode 資料模型
- [x] 更新 CameraState 新增 hdrMode 欄位
- [x] 新增 ToggleHDR UI 事件
- [x] 實作 toggleHDR() 方法
- [x] 實作 applyHDRMode() 方法
- [x] 修改 handleAutoHDR() 跳過邏輯
- [x] 新增 HDR 控制按鈕 UI
- [x] 更新 HDRWarningOverlay 顯示邏輯
- [x] 編譯測試通過
- [x] 撰寫實作文件
- [ ] 實機測試驗證
- [ ] 撰寫單元測試
- [ ] 更新用戶文件

---

## 📚 相關文件

- [P0-2 實機測試報告](P0-2_REAL_DEVICE_TEST_REPORT.md)
- [Overexposed 修復報告](OVEREXPOSED_FIX_REPORT_v2.6.0_20251227.md)
- [P0-2/P0-3 測試指引](P0-2_P0-3_TESTING_GUIDE.md)

---

**報告版本**: 1.0
**建立日期**: 2025-12-28
**最後更新**: 2025-12-28
**作者**: Claude Sonnet 4.5
**專案**: SmartCameraApp v2.6.1
