# Git 分支分析報告 - Home NB

**日期**: 2026-02-13
**分析人員**: Claude Sonnet 4.5 (Home NB)
**目的**: 分析 v2.11.0 和 v2.10.2-final 的合併策略

---

## 📊 分支關係分析結果

### 共同祖先

```bash
git merge-base feature/camera-core-v2.11.0 origin/feature/aspect-ratio-usecasegroup-v2.10.0
# 輸出: 01441aa [Docs] v2.x.x Phase 7: Work NB → Home NB 工作交接
```

✅ **確認**: 兩個分支從 `01441aa` 分岔，屬於**平行開發**

### 分支分岔圖

```
共同祖先: 01441aa (v2.x.x Phase 7)
├─ feature/camera-core-v2.11.0 (左側)
│  ├─ e45a309 - v2.11.0: Initialize development branch
│  ├─ ...文檔還原...
│  ├─ 3aac584 - [Feature] v2.11.0: Eye-AF + Smart Subject Focus + Rectangle UI
│  ├─ d371f2f - [Docs] Add branch version management system
│  └─ 882c64a - [Test] v2.11.0: Add captured test photos (當前)
│
└─ feature/aspect-ratio-usecasegroup-v2.10.0 (右側)
   ├─ f7f7d37 - Phase 7: Focus indicator fade-out animation
   ├─ 8a57d97 - Phase 8: Multi-point LK optical flow tracking
   ├─ 5bd66e9 - Phase 9A: Remove isJitterFrame feedback loop
   ├─ ...v2.10.0 功能開發...
   ├─ 0440242 - [Docs] Test handoff: v2.10.0-aspect-ratio-exif-settings
   ├─ 6501ac2 - [Fix] v2.10.2: Bug fixes (Permission UX + Aspect Ratio + Toast)
   └─ 189cc85 - [Docs] Cleanup: Archive old test results (v2.10.2-final)
```

---

## 🔍 v2.10.2-final 功能分析

### 修改的檔案 (commit 6501ac2)

| 檔案 | 變更 | 內容 |
|------|------|------|
| `MainActivity.kt` | +38 行 | ✅ 啟動時一次性請求所有權限 (Camera + Location + Files) |
| `CameraScreen.kt` | -19 行 | ✅ 移除 Snackbar「照片已儲存」 + 照片資訊按鈕 |
| `CameraViewModel.kt` | +8 行 | ✅ 改用 `.collect()` 持續監聽 aspectRatioMode |
| `feature_record_Backlog.md` | +37 行 | ✅ 添加 Critical Warning |

### v2.10.0 新增的檔案

- ✅ `ExifInfo.kt` - EXIF 資料模型
- ✅ `PhotoInfoBottomSheet.kt` - 照片資訊面板
- ✅ `AspectRatioMode.kt` - 長寬比例模式
- ✅ `LocationPermissionHandler.kt` - GPS 權限處理
- ✅ `ExpandableSettingsCard.kt` - Settings UI 元件

### v2.10.0/v2.10.2 關鍵功能

1. ✅ **EXIF 資訊顯示** (GPS、曝光參數、時間戳等)
2. ✅ **Aspect Ratio 切換** (4:3 / 16:9 / 1:1)
3. ✅ **權限請求 UX 改善**
4. ✅ **Toast Regression 修正**
5. ✅ **Settings UI 現代化**

---

## 🔍 v2.11.0 功能分析

### 修改的檔案 (commit 3aac584)

| 檔案 | 變更 | 內容 |
|------|------|------|
| `MLKitDetector.kt` | +69 行 | ✅ Eye position 提取 (ML Kit landmarks) |
| `DetectionResult.kt` | +23 行 | ✅ EyePosition 資料模型 |
| `CameraScreen.kt` | +151 行淨增 | ✅ Rectangle + Corner Brackets UI (完全重寫 FocusIndicator) |
| `CameraViewModel.kt` | +187 行 | ✅ Smart Subject Focus 邏輯 (Saliency-based) |
| `feature_record_Backlog.md` | +3 行 | ✅ 功能記錄更新 |

### v2.11.0 關鍵功能

1. ✅ **Eye-AF (Eye-Priority Auto Focus)**
   - ML Kit 眼睛 landmark 提取
   - 優先對焦於最近的眼睛（而非臉部中心）
   - 僅前置相機

2. ✅ **Smart Subject Focus (AI Auto-Focus)**
   - Saliency-based 顯著性計算
   - 自動對焦於最重要的主體
   - 人臉優先 > 大物體 > 居中物體

3. ✅ **Rectangle + Corner Brackets 視覺設計**
   - 新的焦點指示器設計（取代 v2.10.2 的圓弧設計）
   - Cyan 青色 (#00E5FF) neon glow 效果
   - 3 狀態：Searching → Success → Dwell
   - Overshoot 動畫（縮小 10% → 彈回）

---

## ⚠️ 衝突檔案分析

### 模擬合併結果

```bash
git merge origin/feature/aspect-ratio-usecasegroup-v2.10.0 --no-commit --no-ff
```

**衝突檔案** (3 個):
1. ❌ `app/build.gradle.kts` - 版本號衝突
2. ❌ `app/src/main/java/com/akirachang/ai/photographer/presentation/camera/CameraScreen.kt` - **巨大衝突**
3. ❌ `devspec/feature_record_Backlog.md` - 功能記錄衝突

**自動合併成功** (關鍵):
1. ✅ `app/src/main/java/com/akirachang/ai/photographer/presentation/camera/CameraViewModel.kt` - **無衝突！**

---

## 🔥 關鍵發現 #1: CameraViewModel.kt 自動合併成功

**重要**: Git 成功自動合併了 `CameraViewModel.kt`，保留了兩邊的功能：

### v2.10.2 的內容（已保留）:
```kotlin
// v2.10.2: Monitor aspect ratio mode changes from Settings (continuous listening)
viewModelScope.launch {
    overlayPreferences.aspectRatioMode.collect { savedMode ->
        // ...持續監聽 aspectRatioMode 變更
        cameraRepository.setAspectRatioMode(mode)
    }
}
```

### v2.11.0 的內容（已保留）:
```kotlin
// Smart Subject Focus (v2.11.0: AI Auto-Focus)
private var lastManualTapTime: Long = 0L
private val MANUAL_TAP_OVERRIDE_DURATION = 5000L

// Eye-AF tracking 邏輯
val eyePosition = detectionResult.eyePositions.firstOrNull { it.faceId == trackedId }
if (eyePosition?.nearestEye != null) {
    // Eye-AF: Focus on nearest eye for professional portrait
    centerX = eyePosition.nearestEye.x
    centerY = eyePosition.nearestEye.y
}
```

**結論**: `CameraViewModel.kt` 不需要手動合併！ ✅

---

## 🔥 關鍵發現 #2: CameraScreen.kt 視覺設計完全不同

### v2.10.2 的 FocusIndicator 設計

**視覺風格**: Dashed 3-segment arc (圓弧設計)

```kotlin
// v2.9.9 Day 1-2: Plan B color scheme (white/cyan/yellow)
// Dashed 3-segment arc design (3 × 120°)
val color = when {
    isLocked -> Color(0xFFFFC107)  // Yellow - AE/AF Lock
    isTracking -> Color.White  // White - Tracking
    focusSuccess == false -> Color(0xFF00E5FF)  // Cyan - Focus failed
    isFocusing -> Color(0xFF00E5FF)  // Cyan - Focusing
    else -> Color.White  // White - Focus success
}
```

**動畫系統**:
- `StiffnessHigh` (900f) - Phase 9B-fix 優化
- Alpha 優先順序: `isLocked → isFading → isTracking`

### v2.11.0 的 FocusIndicator 設計

**視覺風格**: Rectangle + Corner Brackets (矩形 + 角落括號)

```kotlin
// Rectangle Focus Indicator with Corner Brackets (v2.11.0)
// - Searching: Rectangle only, Cyan neon glow
// - Success: Rectangle + Corner brackets, Cyan, shrink 10% → bounce animation
// - Dwell: Rectangle + Corner brackets, Cyan Alpha 0.4, stroke 1.5dp
// - Locked: Rectangle + Corner brackets, Cyber Yellow neon glow

val color = when {
    isLocked -> Color(0xFFFFD700)  // Cyber Yellow (#FFD700)
    isTracking -> Color(0xFF00E5FF)  // Cyan (#00E5FF)
    else -> Color(0xFF00E5FF)  // Cyan (#00E5FF)
}
```

**動畫系統**:
- `StiffnessMedium` + Overshoot 動畫 (Animatable)
- Stroke width 動態調整: 2.5dp → 1.5dp (Dwell)
- Alpha 優先順序: `isTracking || isLocked → isFading → focusSuccess`

### 差異統計

```bash
git diff 6501ac2:...CameraScreen.kt 3aac584:...CameraScreen.kt --stat
# 輸出: 286 lines changed, 164 insertions(+), 122 deletions(-)
```

**結論**: CameraScreen.kt 是**完全不同的視覺設計**，無法自動合併 ❌

---

## 🎯 合併策略建議

### 策略概覽

**目標**: 創建 v2.11.0-final，同時包含兩個分支的所有功能

### 階段 1: 自動合併的檔案（無需處理）✅

以下檔案 Git 已自動合併，**無需手動干預**：

- ✅ `CameraViewModel.kt` - Eye-AF + Smart Subject Focus + Aspect Ratio monitoring
- ✅ `MainActivity.kt` - v2.10.2 權限請求改善
- ✅ `ExifInfo.kt`, `PhotoInfoBottomSheet.kt`, `AspectRatioMode.kt` - v2.10.2 新檔案
- ✅ `SettingsScreen.kt` - v2.10.2 Aspect Ratio toggle
- ✅ 其他 100+ 檔案

### 階段 2: 衝突檔案處理策略

#### 2.1 app/build.gradle.kts（簡單）

**衝突原因**: 版本號不同
- v2.10.2: versionCode 72, versionName "2.10.2"
- v2.11.0: versionCode 73, versionName "2.11.0"

**解決方案**:
```kotlin
versionCode = 74  // 新版本號（遞增）
versionName = "2.11.0-final"  // 或 "2.11.1"
```

**執行**: 手動編輯，保留 v2.11.0 的版本號基礎

---

#### 2.2 CameraScreen.kt（複雜）⭐ **關鍵決策**

**問題**: v2.10.2 和 v2.11.0 的 FocusIndicator 設計完全不同

**選項 A: 保留 v2.11.0 的 Rectangle 設計 + 應用 v2.10.2 的優化**（推薦）

**理由**:
1. v2.11.0 的 Rectangle UI 是最新的視覺設計
2. v2.10.2 的 `StiffnessHigh` 優化可以保留
3. v2.10.2 的 PhotoInfoBottomSheet 可以恢復

**合併步驟**:
1. 使用 v2.11.0 的 `FocusIndicator` composable（完整保留 Rectangle UI）
2. 應用 v2.10.2 的動畫優化：
   ```kotlin
   // 從 v2.10.2 應用 StiffnessHigh
   spring(
       dampingRatio = Spring.DampingRatioNoBouncy,
       stiffness = Spring.StiffnessHigh  // Phase 9B-fix 優化
   )
   ```
3. 恢復 v2.10.2 的 PhotoInfoBottomSheet：
   ```kotlin
   // v2.11.0 缺少此功能，從 v2.10.2 復原
   val exifInfo = state.currentPhotoExifInfo
   if (state.showPhotoInfo && exifInfo != null) {
       PhotoInfoBottomSheet(
           exifInfo = exifInfo,
           onDismiss = { viewModel.onEvent(CameraUiEvent.DismissPhotoInfo) }
       )
   }
   ```

**優點**:
- ✅ 保留最新的視覺設計（Rectangle）
- ✅ 應用性能優化（StiffnessHigh）
- ✅ 恢復 EXIF 資訊顯示功能
- ✅ 用戶體驗最佳

**缺點**:
- ⚠️ 需要手動編輯約 50-100 行代碼
- ⚠️ 需要測試動畫效果

---

**選項 B: 保留 v2.10.2 的 Arc 設計 + 移植 v2.11.0 的功能**（不推薦）

**理由**:
- v2.10.2 的圓弧設計已穩定
- v2.11.0 的 Eye-AF 和 Smart Subject Focus 只需要 CameraViewModel，不依賴 UI

**合併步驟**:
1. 使用 v2.10.2 的 `FocusIndicator` composable
2. 保留 v2.10.2 的 PhotoInfoBottomSheet
3. v2.11.0 的 Eye-AF 和 Smart Subject Focus 自動生效（CameraViewModel 已自動合併）

**優點**:
- ✅ 保留穩定的圓弧設計
- ✅ 保留 EXIF 資訊顯示
- ✅ 手動編輯量最小

**缺點**:
- ❌ 放棄最新的視覺設計（Rectangle）
- ❌ 用戶看不到新的 UI 改進
- ❌ 不符合 v2.11.0 的設計目標

---

**選項 C: 逐行比對，手動合併所有差異**（最複雜）

**不推薦理由**:
- 286 行差異，工作量巨大
- 容易引入錯誤
- 測試成本高

---

#### 2.3 devspec/feature_record_Backlog.md（中等）

**衝突原因**: 兩邊都添加了功能記錄

**解決方案**:
1. 手動合併兩邊的內容
2. 保留 v2.10.2 的 Critical Warning
3. 添加 v2.11.0 的功能記錄
4. 確保時間順序正確

**執行**: 手動編輯，合併兩邊內容

---

## 📋 最終推薦方案

### ⭐ 推薦：選項 A（Rectangle UI + v2.10.2 優化）

**合併流程**:

1. **執行合併**:
   ```bash
   git merge origin/feature/aspect-ratio-usecasegroup-v2.10.0
   ```

2. **解決 build.gradle.kts 衝突**:
   ```kotlin
   versionCode = 74
   versionName = "2.11.1"  // 或 "2.11.0-final"
   ```

3. **解決 CameraScreen.kt 衝突**:
   - 使用 v2.11.0 的 `FocusIndicator`（Rectangle 設計）
   - 應用 v2.10.2 的 `StiffnessHigh` 優化
   - 恢復 v2.10.2 的 `PhotoInfoBottomSheet`

4. **解決 feature_record_Backlog.md 衝突**:
   - 手動合併兩邊內容

5. **測試**:
   - ✅ Eye-AF 功能測試（前置相機）
   - ✅ Smart Subject Focus 測試（後置相機）
   - ✅ Rectangle + Corner Brackets 視覺測試
   - ✅ EXIF 資訊顯示測試
   - ✅ Aspect Ratio 切換測試
   - ✅ 權限請求測試

---

## 📊 功能對照表

### 最終 v2.11.1 將包含的功能

| 功能 | 來源 | 狀態 |
|------|------|------|
| Eye-AF (眼睛優先對焦) | v2.11.0 | ✅ 自動合併 (CameraViewModel) |
| Smart Subject Focus | v2.11.0 | ✅ 自動合併 (CameraViewModel) |
| Rectangle + Corner Brackets UI | v2.11.0 | ✅ 手動保留 (CameraScreen) |
| EXIF 資訊顯示 (GPS/曝光) | v2.10.2 | ✅ 手動恢復 (CameraScreen) |
| Aspect Ratio 切換 | v2.10.2 | ✅ 自動合併 (Settings) |
| 權限請求 UX 改善 | v2.10.2 | ✅ 自動合併 (MainActivity) |
| StiffnessHigh 動畫優化 | v2.10.2 | ✅ 手動應用 (CameraScreen) |
| Toast Regression 修正 | v2.10.2 | ✅ 自動合併 (CameraScreen) |

---

## 🚀 下一步行動

### Work NB 執行步驟

1. **閱讀本報告**
2. **確認選項 A 或 B**
3. **執行合併** (按照 `devspec/MERGE_SOP.md`)
4. **解決衝突**
5. **測試**
6. **提交**

### Home NB 支援

- ✅ 分析報告已完成
- ✅ 合併策略已明確
- ⏸️ 等待 Work NB 確認執行

---

**報告生成日期**: 2026-02-13
**分析完成**: Home NB (Claude Sonnet 4.5)
**下一步**: 等待 Work NB 確認合併策略並執行
