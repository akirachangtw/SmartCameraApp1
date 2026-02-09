# v2.10.0 Test Handoff Summary

**Date**: 2026-02-09
**Version**: v2.10.0
**Branch**: `feature/aspect-ratio-usecasegroup-v2.10.0`
**Status**: ⏳ Pending Device Testing
**Test Device**: Pixel 9a (Device ID: `59131JEBF17244`)

---

## 📋 Executive Summary

v2.10.0 包含以下重要功能更新：

1. **Aspect Ratio WYSIWYG 修正** - 解決預覽與實際拍攝比例不一致問題
2. **UseCaseGroup + ViewPort 整合** - 統一 Preview/Capture 比例
3. **EXIF 資訊顯示** - 新增照片資訊顯示功能（GPS、曝光參數、裝置資訊）
4. **Settings UI 現代化** - 可摺疊設定卡片、位置權限管理

**⚠️ 關鍵變更**：
- 影響相機預覽比例計算（CameraX ViewPort）
- 新增 GPS 位置權限處理
- Settings UI 重構（ExpandableSettingsCard）

---

## 🎯 Test Objectives

### Primary Goals
- [ ] **Aspect Ratio 驗證** - 確認預覽與拍攝比例一致（WYSIWYG）
- [ ] **EXIF 資訊驗證** - 確認照片資訊正確顯示（GPS、曝光、裝置）
- [ ] **Settings UI 驗證** - 確認可摺疊卡片正常運作
- [ ] **位置權限驗證** - 確認 GPS 權限請求與處理正常

### Secondary Goals
- [ ] 跨裝置比較（模擬器 vs Pixel 9a）
- [ ] 效能檢查（FPS、Memory）
- [ ] 穩定性測試（旋轉、模式切換）

---

## 📦 Commits Since Last Release

```
d7db1e5 [Feature] v2.10.0: Add EXIF info + Photo info sheet + Location permission
7948186 [Feature] v2.10.0: Aspect ratio WYSIWYG fix + cross-device validation
9c412ca [Feature] v2.10.0: UseCaseGroup + ViewPort aspect ratio implementation
998395f [Feature] v2.10.0: Add AspectRatioMode enum + CameraState support
2090934 [Fix] Preview zoom: Use 16:9 AspectRatioStrategy to fix 1.6x FOV + WYSIWYG capture
```

**Total**: 5 commits

---

## 🔍 Key Changes

### 1. Aspect Ratio System (Commits: 998395f, 9c412ca, 7948186, 2090934)

**Files Modified**:
- `CameraState.kt` - 新增 `AspectRatioMode` enum (RATIO_16_9, RATIO_4_3, RATIO_1_1)
- `CameraViewModel.kt` - 整合 UseCaseGroup + ViewPort
- `CameraRepositoryImpl.kt` - 統一 Preview/Capture 比例計算

**Impact**:
- 解決預覽與拍攝比例不一致問題
- 支援 16:9, 4:3, 1:1 比例切換
- WYSIWYG（所見即所得）保證

**Testing Focus**:
- 切換不同比例（16:9 ↔ 4:3 ↔ 1:1）
- 確認預覽框與實際拍攝結果一致
- 跨裝置驗證（模擬器 vs Pixel 9a）

---

### 2. EXIF Info Display (Commit: d7db1e5)

**Files Added**:
- `ExifInfo.kt` - EXIF 資料模型（GPS、曝光、裝置資訊）
- `PhotoInfoBottomSheet.kt` - 照片資訊底部彈出視窗

**Files Modified**:
- `CameraRepository.kt` / `CameraRepositoryImpl.kt` - EXIF 資料收集
- `CameraScreen.kt` / `CameraState.kt` / `CameraViewModel.kt` - 整合 UI 顯示
- `OverlayPreferences.kt` - 新增顯示設定

**Features**:
- GPS 座標顯示（經緯度）
- 曝光參數（ISO、快門速度、光圈）
- 裝置資訊（型號、Android 版本）

**Testing Focus**:
- 拍攝後點擊照片資訊按鈕
- 確認 GPS 座標正確（需授予位置權限）
- 確認曝光參數顯示
- 確認裝置資訊正確

---

### 3. Settings UI Modernization (Commit: d7db1e5)

**Files Added**:
- `ExpandableSettingsCard.kt` - 可摺疊設定卡片元件
- `LocationPermissionHandler.kt` - 位置權限管理

**Files Modified**:
- `SettingsScreen.kt` / `SettingsViewModel.kt` - Settings UI 重構

**Features**:
- 可摺疊的設定分組（Camera、Overlay、Advanced）
- 位置權限請求與處理
- 現代化 Material Design 3 風格

**Testing Focus**:
- 點擊設定卡片展開/收合
- 確認動畫流暢
- 測試位置權限請求流程

---

## 🧪 Test Plan for Pixel 9a

### Phase 1: Pre-Build Check
- [x] Git status clean
- [x] Files changed: 12 files
- [x] Commit count: 5 commits

### Phase 2: Build & Install
```bash
# 在 Work NB 執行
./gradlew assembleDeviceDebug
adb -s 59131JEBF17244 install -r app/build/outputs/apk/device/debug/app-device-debug.apk
```

### Phase 3: Functional Testing

#### 測試 1: Aspect Ratio WYSIWYG
1. 啟動 APP → 進入 Live 模式
2. 切換比例：16:9 → 4:3 → 1:1
3. 每個比例下拍攝 1 張照片
4. 使用系統相簿檢視照片比例
5. **Expected**: 預覽框與實際照片比例一致

#### 測試 2: EXIF Info Display
1. 進入 Settings → 啟用「位置權限」
2. 授予 GPS 位置權限
3. 拍攝 1 張照片
4. 點擊照片資訊按鈕（ℹ️ 圖示）
5. **Expected**: 顯示 GPS 座標、曝光參數、裝置資訊

#### 測試 3: Settings UI
1. 進入 Settings 頁面
2. 點擊各個設定卡片（Camera、Overlay、Advanced）
3. **Expected**: 卡片展開/收合動畫流暢

#### 測試 4: Location Permission
1. 清除 APP 權限（Settings → Apps → SmartCameraApp → Permissions）
2. 重新啟動 APP
3. 進入 Settings → 點擊「啟用位置服務」
4. **Expected**: 彈出位置權限請求對話框

### Phase 4: Stability Testing
- [ ] 旋轉螢幕 5 次（Portrait ↔ Landscape）
- [ ] 模式切換 10 次（Live ↔ Static）
- [ ] 長時間運行 5 分鐘
- [ ] Memory leak 檢查（Logcat）

### Phase 5: Performance Testing
- [ ] FPS 監控（預覽流暢度）
- [ ] Memory 監控（啟動/拍攝/長時間運行）
- [ ] 啟動時間測量

---

## 📊 Expected Results

### Success Criteria
- ✅ Aspect ratio WYSIWYG 一致性 = 100%
- ✅ EXIF 資訊正確顯示
- ✅ Settings UI 動畫流暢（無卡頓）
- ✅ GPS 權限請求正常
- ✅ Crash 率 = 0%
- ✅ Memory 增長 < 20% (5 分鐘運行)

### Known Issues (Before Testing)
- 無已知問題

---

## 📁 Artifacts

### Home NB (Emulator Testing)
- **模擬器**: Medium_Phone_API_36.1
- **測試狀態**: ⏭️ SKIPPED (直接實機測試)
- **APK**: `app/build/outputs/apk/emulator/debug/app-emulator-debug.apk`

### Work NB (Device Testing)
- **裝置**: Pixel 9a (Device ID: `59131JEBF17244`)
- **測試狀態**: ⏳ Pending
- **APK**: 需在 Work NB 重新編譯
  ```bash
  ./gradlew assembleDeviceDebug
  ```

### Test Results (To be filled by Work NB)
- **Logcat**: `docs/test_results/v2.10.0/pixel9a_test_logcat.log`
- **Screenshots**: `docs/test_results/v2.10.0/screenshots/`
- **Test Report**: `docs/test_results/v2.10.0/PIXEL9A_TEST_REPORT.md`

---

## 🔗 Related Documents

- **Feature Record**: `devspec/feature_record_Backlog.md` (待更新)
- **PR Template**: `devspec/pull_request_template.md`
- **Hybrid Workflow**: `devspec/hybrid_workflow_checklist.md`

---

## 📝 Work NB Action Items

### Step 1: Pull Changes
```bash
git fetch origin
git checkout feature/aspect-ratio-usecasegroup-v2.10.0
git pull origin feature/aspect-ratio-usecasegroup-v2.10.0
```

### Step 2: Build Device APK
```bash
./gradlew assembleDeviceDebug
```

### Step 3: Install to Pixel 9a
```bash
adb -s 59131JEBF17244 install -r app/build/outputs/apk/device/debug/app-device-debug.apk
```

### Step 4: Start Logcat Monitoring
```bash
adb -s 59131JEBF17244 logcat -c
adb -s 59131JEBF17244 logcat -v time > docs/test_results/v2.10.0/pixel9a_test_logcat.log
```

### Step 5: Execute Test Plan
參考「Phase 3: Functional Testing」區塊

### Step 6: Collect Results
```bash
# 截圖
mkdir -p docs/test_results/v2.10.0/screenshots
adb -s 59131JEBF17244 shell "screencap -p > /sdcard/Download/test_aspect_ratio.png"
adb -s 59131JEBF17244 pull //sdcard//Download//test_aspect_ratio.png docs/test_results/v2.10.0/screenshots/
adb -s 59131JEBF17244 shell "rm /sdcard/Download/test_aspect_ratio.png"

# 拍攝的照片
adb -s 59131JEBF17244 pull //sdcard//DCIM//SmartCameraApp// docs/test_results/v2.10.0/captured_photos/
```

### Step 7: Create Test Report
使用模板創建測試報告：`docs/test_results/v2.10.0/PIXEL9A_TEST_REPORT.md`

### Step 8: Push Results
```bash
git add docs/test_results/v2.10.0/
git commit -m "[Test] v2.10.0: Pixel 9a 實機測試結果

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>"
git push origin feature/aspect-ratio-usecasegroup-v2.10.0
```

---

## ⚠️ Notes

1. **Aspect Ratio 驗證最重要** - 這是本版本核心功能
2. **GPS 權限必須測試** - 新增功能，需確認權限請求流程
3. **跨裝置比較** - 如果可能，與模擬器結果比較
4. **效能監控** - 注意 Memory 使用量（EXIF 資料收集可能增加記憶體）

---

**SSOT (Single Source of Truth)**: 此文檔為 v2.10.0 測試交接的唯一真實來源。所有測試相關資訊以此文檔為準。
