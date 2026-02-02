# v2.9.9 Day 1-2 - Traycer v2 專業對焦系統 交接摘要

## 📋 版本資訊

- **版本號**: v2.9.9-Day1-2
- **Version Code**: 60
- **開發日期**: 2026-02-02
- **交接類型**: Home NB → Work NB (實機測試)
- **測試設備**: Pixel 9a

## 🎯 核心功能

本版本實作完整的 **Traycer v2 專業對焦系統**，包含以下核心功能：

### 1. **AE/AF Lock（自動曝光與對焦鎖定）**
- **觸發方式**: 長按預覽畫面
- **鎖定行為**:
  - 鎖定當前點的自動曝光 (AE) 和自動對焦 (AF)
  - 使用 CameraX `FocusMeteringAction` with `FLAG_AF | FLAG_AE`
  - **持久鎖定**（無 auto-cancel），直到手動解鎖
- **視覺反饋**:
  - 黃色虛線方框（80dp）標示鎖定點
  - 黃色虛線3等分弧線顯示對焦圈
- **解鎖方式**: 點擊螢幕任意位置

### 2. **虛線3等分弧線視覺設計**
- **設計規格**:
  - 3 段 120° 弧線（使用 `drawArc`）
  - 虛線效果：PathEffect.dashPathEffect (dash: 8dp, gap: 6dp)
  - 半徑: 40dp
  - 線寬: 3dp
- **方案 B 配色系統**:
  - **白色 (#FFFFFF)**: 追蹤模式、靜態對焦成功
  - **螢光藍 (#00E5FF)**: 對焦進行中
  - **黃色 (#FFEB3B)**: AE/AF 鎖定模式
  - **紅色 (#FF5252)**: 對焦失敗

### 3. **三模式並存**
- **追蹤模式** (Tracking Mode):
  - 點擊人臉/物件開啟
  - 白色虛線圓圈連續跟隨目標
  - 使用 Spring 彈性動畫平滑過渡
- **靜態模式** (Static Mode):
  - 點擊空白處觸發
  - 螢光藍 → 白色（成功）或紅色（失敗）
  - 1 秒後自動消失
- **鎖定模式** (Lock Mode):
  - 長按觸發
  - 黃色虛線方框 + 圓圈
  - 持久顯示，點擊解鎖

## 🔧 實作細節

### 修改檔案清單 (共 9 個檔案, +500 行代碼)

1. **app/build.gradle.kts**
   - 版本號更新: versionCode 60, versionName "2.9.9-Day1-2"

2. **CameraState.kt** (+40 行)
   - 新增 `isAEAFLocked: Boolean`
   - 新增 `lockPoint: Pair<Float, Float>?`
   - 新增 `LockAEAF`, `UnlockAEAF` 事件

3. **CameraViewModel.kt** (+150 行)
   - 新增 `lockAEAF()` - 長按鎖定邏輯
   - 新增 `unlockAEAF()` - 解除鎖定
   - 修改 `focusOnPoint()` - 點擊自動解鎖
   - 修改 `switchCamera()` - 清除鎖定狀態

4. **CameraRepository.kt** (+30 行)
   - 新增 `lockAEAF()` 介面方法
   - 新增 `cancelFocusAndMetering()` 介面方法

5. **CameraRepositoryImpl.kt** (+20 行)
   - 委託實作 AE/AF Lock 方法

6. **CameraInputSource.kt** (+30 行)
   - 新增 `lockAEAF()` 預設實作（StaticImageSource no-op）
   - 新增 `cancelFocusAndMetering()` 預設實作

7. **LiveCameraSource.kt** (+120 行)
   - 實作 `lockAEAF()` - FLAG_AF | FLAG_AE，無 auto-cancel
   - 實作 `cancelFocusAndMetering()` - CameraX API
   - 使用 suspendCancellableCoroutine 非阻塞協程化

8. **CameraScreen.kt** (+100 行)
   - 更新 `FocusIndicator` - 虛線3等分弧線設計
   - 新增 `LockIndicator` - 黃色虛線方框
   - 新增長按手勢處理 (onLongPress)
   - 新增必要的 import: Size, PathEffect

9. **FeatureFlags.kt** (+15 行)
   - 新增 `enableAEAFLock: Boolean` (預設 true)

## ✅ 編譯狀態

- **編譯結果**: ✅ **BUILD SUCCESSFUL**
- **編譯時間**: 52 秒
- **Target APK**: `app/build/outputs/apk/device/debug/app-device-debug.apk`
- **編譯警告**: 僅有已知的 deprecation warnings (RenderScript, Timber)

## 📱 待測試場景 (7 項)

Work NB 需在 Pixel 9a 實機上驗證以下場景：

### 測試場景 1: 人臉追蹤模式
- **操作**: 點擊畫面中的人臉
- **預期結果**:
  - 白色虛線3等分弧線出現在人臉位置
  - 圓圈連續跟隨人臉移動
  - 使用 Spring 彈性動畫平滑過渡

### 測試場景 2: 靜態對焦模式
- **操作**: 點擊空白處（無人臉/物件）
- **預期結果**:
  - 螢光藍虛線圓圈出現在點擊位置
  - 對焦完成後變為白色
  - 1 秒後自動消失

### 測試場景 3: AE/AF 鎖定
- **操作**: 長按預覽畫面
- **預期結果**:
  - 黃色虛線方框（80dp）出現在長按位置
  - 黃色虛線3等分弧線同時顯示
  - 曝光與對焦鎖定（移動相機，曝光不變）

### 測試場景 4: 解除鎖定
- **操作**: 鎖定狀態下點擊螢幕
- **預期結果**:
  - 黃色方框和圓圈消失
  - 恢復自動曝光與對焦模式

### 測試場景 5: 追蹤目標離開畫面
- **操作**: 追蹤人臉時，讓人臉移出畫面
- **預期結果**:
  - 追蹤圓圈在 10 幀後自動消失

### 測試場景 6: 切換相機清除狀態
- **操作**: 在追蹤/鎖定狀態下切換前後鏡頭
- **預期結果**:
  - 追蹤狀態自動清除
  - 鎖定狀態自動解除
  - 所有指示器消失

### 測試場景 7: 效能測試
- **操作**: 在追蹤模式下觀察 FPS
- **預期結果**:
  - Pixel 9a FPS ≥ 25（追蹤模式）
  - 無明顯延遲或卡頓

## 📂 Test Handoff 工作流

### Home NB 推送 (當前步驟)

1. **創建 Git Commit**:
   ```bash
   git add .
   git commit -m "[Feature] v2.9.9 Day 1-2: Traycer v2 專業對焦系統

   實作內容:
   - AE/AF Lock (長按鎖定曝光與對焦)
   - 虛線3等分弧線視覺 (120°×3 segments)
   - 方案 B 配色 (白色/螢光藍/黃色)
   - 三模式並存 (追蹤/靜態/鎖定)
   - 長按手勢處理 (detectTapGestures onLongPress)

   修改檔案:
   - CameraState.kt (+40 行): AE/AF Lock 狀態
   - CameraViewModel.kt (+150 行): Lock 邏輯實作
   - LiveCameraSource.kt (+120 行): CameraX FLAG_AF|FLAG_AE
   - CameraScreen.kt (+100 行): 虛線弧線 + 黃色方框
   - FeatureFlags.kt (+15 行): enableAEAFLock

   驗證:
   - 編譯成功 (BUILD SUCCESSFUL in 52s)
   - versionCode: 60, versionName: 2.9.9-Day1-2

   待 Work NB Pixel 9a 實機測試 (7 項場景)

   Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>"
   ```

2. **推送到遠端**:
   ```bash
   git push github main
   ```

### Work NB 拉取與測試

1. **拉取代碼**:
   ```bash
   git pull github main
   ```

2. **編譯實機版本**:
   ```bash
   ./gradlew assembleDeviceDebug
   ```

3. **安裝到 Pixel 9a**:
   ```bash
   adb -s <DEVICE_ID> install app/build/outputs/apk/device/debug/app-device-debug.apk
   ```

4. **執行測試場景** (參考下方測試指南)

5. **記錄測試結果**:
   - 保存至 `docs/test_results/v2.9.9-Day1-2/PIXEL9A_TEST_REPORT.md`
   - 包含截圖、Logcat、測試照片

6. **推送測試結果**:
   ```bash
   git add docs/test_results/v2.9.9-Day1-2/
   git commit -m "[Test] v2.9.9 Day 1-2: Pixel 9a 實機測試結果"
   git push github main
   ```

## 🔗 相關文檔

- **測試計劃**: [`PIXEL9A_TEST_GUIDE.md`](./PIXEL9A_TEST_GUIDE.md)
- **路線圖**: [`plans/02.4-smart_camera_app_roadmap.md`](../../plans/02.4-smart_camera_app_roadmap.md) (Line 759-869)
- **設計文檔**: [`docs/plan/v2.9.7_tap-to-focus-v2-Traycer.md`](../plan/v2.9.7_tap-to-focus-v2-Traycer.md)

## 📝 備註

- APK 不推送到 Git（在 Work NB 重新編譯）
- 測試產出必須存放至 `docs/test_results/v2.9.9-Day1-2/`
- `tmp/` 目錄內容已被 `.gitignore` 忽略

---

**準備完成，等待 Work NB 測試驗證！** 🚀
