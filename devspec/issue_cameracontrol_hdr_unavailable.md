# Bug Report — Camera / Preview / Pipeline

> **工作流級別**: Level 1 (規劃檢視) → Level 2 (可行性檢視) → Level 3 (實作與驗證)
>
> 本 Issue 已完成 Level 1 & Level 2，準備進入 Level 3 實作

---

## 問題描述（What happened）

實機與模擬器環境下，Logcat 持續出現 CameraControl 不可用警告:

```
W CameraRepositoryImpl: Cannot enable HDR: CameraControl not available
W CameraRepositoryImpl: Cannot disable HDR: CameraControl not available
```

**實際行為**:
- HDR 手動控制 UI (AUTO/ON/OFF 按鈕) 正常顯示與切換
- 狀態管理邏輯正常運作 (ViewModel 層)
- 警告 UI 顯示/隱藏邏輯正常
- 但硬體層 HDR (透過曝光補償模擬) **無法確定是否生效**

---

## 預期行為（What expected）

1. **Logcat**: 不應出現 "CameraControl not available" 警告
2. **預期日誌**:
   ```
   I LiveCameraSource: HDR enabled: exposure compensation set to +1
   I LiveCameraSource: HDR disabled: exposure compensation reset to 0
   ```
3. **實際拍照**: 在 ON 模式下拍照應有 +1 EV 曝光補償效果

---

## 發生環境（Environment）

- **裝置**:
  - ✅ 模擬器 (Medium_Phone_API_36.1)
  - ✅ 實機 (Sony XQ-BT52, Android 11)
- **Android 版本**: API 30+
- **App 版本**: v2.6.1
- **Commit**: e9b89ae
- **模式**:
  - ✅ Live Camera (實機)
  - ✅ Static Image (模擬器測試圖片)

---

## 重現步驟（Steps to reproduce）

1. 啟動 App (模擬器或實機皆可)
2. 點擊 HDR 按鈕切換到 ON 模式
3. 檢查 Logcat
4. **觀察**: 出現 `W CameraRepositoryImpl: Cannot enable HDR: CameraControl not available`
5. 點擊 HDR 按鈕切換到 OFF 模式
6. **觀察**: 出現 `W CameraRepositoryImpl: Cannot disable HDR: CameraControl not available`

---

## 根本原因分析（Root Cause）

### 架構衝突: 雙 Camera Provider

v2.6.1 引入 `LiveCameraSource` 後，實機環境下存在兩套 Camera Provider:

```
實機架構:
├── LiveCameraSource (實際管理相機)
│   └── ProcessCameraProvider
│         └── camera.cameraControl ← ✅ 可用
│
└── LifecycleCameraController (舊架構遺留)
      └── cameraControl ← ❌ 未綁定，返回 null
            ↑
            └── CameraRepositoryImpl 嘗試使用此處設定 HDR ❌
```

### 問題發生位置

**[CameraRepositoryImpl.kt:287-320](app/src/main/java/com/akirachang/ai/photographer/data/repository/CameraRepositoryImpl.kt#L287-L320)**:

```kotlin
override fun enableHDRMode() {
    try {
        val cameraControl = _cameraController.cameraControl  // ❌ 實機環境下為 null
        if (cameraControl != null) {
            cameraControl.setExposureCompensationIndex(1)
            Log.i(TAG, "HDR mode enabled (exposure compensation +1)")
        } else {
            Log.w(TAG, "Cannot enable HDR: CameraControl not available")  // ⚠️ 警告觸發
        }
    } catch (e: Exception) {
        Log.e(TAG, "Failed to enable HDR mode", e)
    }
}
```

### 為何發生

**[CameraRepositoryImpl.kt:445-456](app/src/main/java/com/akirachang/ai/photographer/data/repository/CameraRepositoryImpl.kt#L445-L456)**:

```kotlin
override fun bindToLifecycle(lifecycleOwner: LifecycleOwner) {
    val liveCameraSource = cameraInputSource as? LiveCameraSource
    if (liveCameraSource != null) {
        // 實機：只綁定 LiveCameraSource
        liveCameraSource.bindToLifecycle(lifecycleOwner)
        // ❌ _cameraController 未綁定 → cameraControl = null
    } else {
        // 模擬器：綁定 _cameraController
        _cameraController.bindToLifecycle(lifecycleOwner)
    }
}
```

### 為何 LiveCameraSource 的 CameraControl 可用

**[LiveCameraSource.kt:248-256](app/src/main/java/com/akirachang/ai/photographer/data/source/camera/LiveCameraSource.kt#L248-L256)**:

```kotlin
override suspend fun setZoomRatio(ratio: Float): Result<Unit> {
    if (!::camera.isInitialized) {
        return Result.failure(Exception("Camera not initialized"))
    }

    val cameraControl = camera.cameraControl  // ✅ 這個可用
    cameraControl.setZoomRatio(clampedRatio)  // ✅ Zoom 功能正常
    Result.success(Unit)
}
```

---

## 初步風險判斷（Checklist）

### 現象分類
- ☐ Preview 無畫面 / 黑畫面
- ☐ ImageAnalysis 無資料
- ☐ 模式切換後異常
- ☐ 僅模擬器正常，實機異常
- ✅ 其他：**CameraControl API 不可用，硬體 HDR 效果不確定**

### ⚠️ Level 2 升級條件檢查

**已勾選項目** (必須先完成 Level 2 可行性評估):
- ✅ 涉及 CameraX / Preview / ImageAnalysis (CameraControl API)
- ✅ 涉及模擬器 vs 實體裝置差異 (兩者皆有問題)
- ☐ 涉及 FPS / Memory / CPU / GPU
- ☐ 涉及模式切換 (Live / Static / Debug)
- ✅ 修改兩個以上檔案 (4 個檔案)
- ✅ 非純 UI / 文案調整 (Camera API 層級修改)

**Level 2 結論** (可行性評估已完成):
- ✅ **可行** → 已完成可行性評估，可進入 Level 3 實作
- ☐ 需調整
- ☐ 不可行

**可行性評估文件**:
- [CAMERACONTROL_ISSUE_ANALYSIS.md](../CAMERACONTROL_ISSUE_ANALYSIS.md)
- [CAMERACONTROL_FIX_FEASIBILITY.md](../CAMERACONTROL_FIX_FEASIBILITY.md)

---

## 修正方案（Solution）

### 採用方案: A (Camera Input 抽象層)

**設計原則**:
1. 在 `CameraInputSource` 介面新增 HDR 控制方法
2. `LiveCameraSource` 實作使用可用的 `camera.cameraControl`
3. `StaticImageSource` 實作為 no-op
4. `CameraRepositoryImpl` 委派給 `cameraInputSource`
5. 採用**排隊機制**覆蓋 camera 未初始化與 rebind 場景

### 關鍵設計: 排隊機制 (Critical)

**與 v2.6.1 analyzer 持久化模式一致**:

```
v2.6.1 相機切換修復:
  pendingAnalyzer (一次性)
    → previewStreamAnalyzer (持久化) ✅ 已修復

v2.6.2 HDR 設定:
  直接設定 (無排隊)
    → pendingExposureIndex (持久化) ✅ 必須採用
```

### 實作概要

#### 1. CameraInputSource.kt
```kotlin
fun enableHDR(): Result<Unit>
fun disableHDR(): Result<Unit>
```

#### 2. LiveCameraSource.kt
```kotlin
private var pendingExposureIndex: Int? = null

override fun enableHDR(): Result<Unit> {
    if (::camera.isInitialized) {
        val range = camera.cameraInfo.exposureState.exposureCompensationRange
        val targetValue = 1.coerceIn(range.lower, range.upper)
        camera.cameraControl.setExposureCompensationIndex(targetValue)
        pendingExposureIndex = targetValue  // 持久化
    } else {
        pendingExposureIndex = 1  // 排隊
    }
    return Result.success(Unit)
}

// 在 bindToLifecycle() 後補套用
pendingExposureIndex?.let { exposureValue ->
    camera.cameraControl.setExposureCompensationIndex(exposureValue)
    Log.i(TAG, "✅ Pending HDR reapplied: $exposureValue")
}
```

#### 3. StaticImageSource.kt
```kotlin
override fun enableHDR(): Result<Unit> {
    Log.i(TAG, "HDR enabled (no-op for static images)")
    return Result.success(Unit)
}
```

#### 4. CameraRepositoryImpl.kt
```kotlin
override fun enableHDRMode() {
    Log.d(TAG, "Delegating HDR enable to CameraInputSource")
    cameraInputSource.enableHDR()
}

// 移除 287-320 行對 _cameraController.cameraControl 的直接存取
```

---

## 影響範圍（Impact）

### 修改檔案
1. `app/src/main/java/com/akirachang/ai/photographer/data/source/camera/CameraInputSource.kt`
   - 新增 2 個介面方法 (~15 行)
2. `app/src/main/java/com/akirachang/ai/photographer/data/source/camera/LiveCameraSource.kt`
   - 新增 HDR 實作 + rebind 補套用 (~50 行)
3. `app/src/main/java/com/akirachang/ai/photographer/data/source/camera/StaticImageSource.kt`
   - 新增 no-op 實作 (~15 行)
4. `app/src/main/java/com/akirachang/ai/photographer/data/repository/CameraRepositoryImpl.kt`
   - 委派實作 (~10 行新增, ~35 行刪除)

**總計**: ~90 行新增, ~15 行修改, ~35 行刪除

### 風險評估
- **技術風險**: 🟡 中低 (複製已驗證模式)
- **回歸風險**: 🟢 低 (介面新增，不修改既有方法)
- **測試範圍**: 🟡 中 (需實機拍照驗證 HDR 效果)

### 回滾方案
- **回滾點**: Git commit before changes
- **回滾指令**: `git revert <commit-hash>`
- **影響**: 回到 v2.6.1 狀態 (HDR UI 正常，硬體效果不確定)

---

## 驗證計畫（Verification）

### Phase 1: 編譯驗證
- ✅ Gradle build 成功
- ✅ 無編譯錯誤
- ✅ 介面實作完整性檢查

### Phase 2: 模擬器測試
- ✅ HDR 按鈕切換正常
- ✅ 無 "CameraControl not available" 警告
- ✅ Logcat 顯示 "HDR enabled/disabled (no-op for static images)"

### Phase 3: 實機基本測試
- ✅ HDR 按鈕切換正常
- ✅ 無 "CameraControl not available" 警告
- ✅ Logcat 顯示 "HDR enabled: exposure compensation set to X"

### Phase 4: 實機 rebind 測試
- ✅ 設定 HDR 為 ON
- ✅ 切換鏡頭
- ✅ Logcat 顯示 "Pending HDR reapplied: X"
- ✅ HDR 設定保持

### Phase 5: 實機拍照驗證 (最終驗證)
1. 固定場景拍照測試:
   - ON 模式拍照 → EXIF `ExposureCompensation = +1.0 EV`
   - OFF 模式拍照 → EXIF `ExposureCompensation = 0.0 EV`
2. AUTO 模式自動切換:
   - Backlight 場景 → 自動啟用 HDR
   - Normal 場景 → 自動停用 HDR

---

## 補充資料

### Logcat 範例 (v2.6.1 問題)

```
12-29 18:11:25.438 W CameraRepositoryImpl: Cannot enable HDR: CameraControl not available
12-29 18:11:41.452 W CameraRepositoryImpl: Cannot disable HDR: CameraControl not available
```

### 預期 Logcat (v2.6.2 修復後)

```
01-01 12:00:00.000 I LiveCameraSource: HDR enabled: exposure compensation set to 1
01-01 12:00:05.000 I LiveCameraSource: ✅ Pending HDR reapplied: 1
01-01 12:00:10.000 I LiveCameraSource: HDR disabled: exposure compensation reset to 0
```

### 參考文檔
- [HDR_MANUAL_CONTROL_REAL_DEVICE_TEST_v2.6.1.md](../chat_logs/HDR_MANUAL_CONTROL_REAL_DEVICE_TEST_v2.6.1.md)
- [CAMERACONTROL_ISSUE_ANALYSIS.md](../CAMERACONTROL_ISSUE_ANALYSIS.md)
- [CAMERACONTROL_FIX_FEASIBILITY.md](../CAMERACONTROL_FIX_FEASIBILITY.md)
- [CameraControl-not-available.md](../chat_logs/CameraControl-not-available.md)

---

## 實作時程

- **預計時間**: 半天
  - 實作: 1-2 小時
  - 測試: 2-3 小時
- **目標版本**: v2.6.2
- **狀態**: ✅ **準備開始實作** (Level 2 可行性評估已完成)

---

**Issue 建立日期**: 2026-01-01
**建立者**: Claude Sonnet 4.5
**優先級**: P1 (高優先級 - 影響 HDR 功能實際效果)
**類型**: Bug - Camera Pipeline
