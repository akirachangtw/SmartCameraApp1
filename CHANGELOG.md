# Changelog

All notable changes to SmartCameraApp will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [v2.10.2] - 2026-02-09

### Fixed
- 🐛 **權限請求 UX 改善** - 啟動時一次性請求所有必要權限（Camera + Location + Files and Media）
  - 修正用戶反饋：「手機APP的位置/相機和影片的存取權限設定應該是初次執行時就會顯示提示是否允許存取」
  - 實作：在 MainActivity 中使用 rememberMultiplePermissionsState
  - 根據 Android 版本動態請求適當的檔案權限（API 33+ / API 29-32 / API 28-）

- 🐛 **長寬比例設定生效問題** - 修正 Settings 中設定無法套用到預覽和拍攝
  - 修正用戶反饋：「無法依照長寬比例套用於預覽畫面尺寸及拍照圖片尺寸」
  - 根源：CameraViewModel 使用 `.first()` 只讀取一次設定
  - 實作：改用 `.collect()` 持續監聽 overlayPreferences.aspectRatioMode
  - 效果：Settings 切換長寬比例（4:3/16:9/1:1）立即觸發 camera rebind

- 🐛 **Toast 訊息 Regression** - 移除 v2.10.1 重複顯示的 Snackbar「照片已儲存」
  - 修正用戶反饋：「又再出現離開Settings介面及拍照顯示文字: "拍照已儲存"」
  - 歷史：v2.9.8 已移除此 Snackbar，v2.10.1 (commit d7db1e5) 重新加入導致 regression
  - 實作：恢復 v2.9.8 方案（按鈕禁用 + 進度指示器，不使用 Snackbar）
  - 用戶期望：「拍照中/完成皆以快門按鈕動畫為主, 皆以視覺為主」

### Changed
- ♻️ **UI 簡潔性改善** - 移除照片資訊按鈕（ℹ️ 圖示）
  - 用戶反饋：「刪除照片資訊按鈕->非用戶使用習慣」
  - 保持 UI 簡潔，只保留快門按鈕和視覺反饋

### Documentation
- 📝 **Regression 預防機制** - 更新 feature_record_Backlog.md
  - 添加 Critical Warning：「DO NOT re-add Snackbar for "照片已儲存"」
  - 添加 Critical Warning：「DO NOT add photo info button」
  - 記錄 Regression 歷史：v2.9.8 → v2.10.1 → v2.10.2
  - 提供替代方案建議（長按快門按鈕或 Settings 中查看 EXIF）

### Tested
- ✅ Pixel 9a 實機測試（100% PASSED）
  - 權限請求驗證
  - 長寬比例切換驗證（1:1 → 4:3 → 16:9 → 1:1）
  - Toast 訊息驗證（無重複顯示）
  - UI 簡潔性驗證
  - 穩定性測試（旋轉、長時間運行）

---

## [v2.10.1] - 2026-02-09

### Added
- ✨ **EXIF 資訊顯示** - PhotoInfoBottomSheet
  - GPS 座標顯示（經緯度）
  - 曝光參數（ISO、快門速度、光圈）
  - 裝置資訊（型號、Android 版本）

- ✨ **Settings UI 現代化**
  - 可摺疊的設定分組（Camera、Overlay、Advanced）
  - 位置權限請求與處理（LocationPermissionHandler）
  - Material Design 3 風格

### Fixed
- 🐛 **Aspect Ratio WYSIWYG** - 修正預覽與拍攝比例不一致
  - UseCaseGroup + ViewPort 整合
  - 支援 16:9, 4:3, 1:1 三種比例

### Known Issues
- ⚠️ Toast 訊息重複顯示（已在 v2.10.2 修正）

---

## [v2.10.0-fix] - 2026-02-08

### Fixed
- 🐛 **Aspect Ratio WYSIWYG 修正** - ViewPort.FIT + PreviewView.FIT_CENTER
  - 修正 1.6x FOV 放大問題
  - 統一 Preview/Capture 比例計算
  - 跨裝置驗證（Sony XQ-BT52 + Pixel 9a）

---

## [v2.9.8] - 2026-01-30

### Fixed
- 🐛 **照片保存反饋機制** - 移除 Snackbar「照片已儲存」
  - 問題：Snackbar 重複顯示（每次進入 CameraScreen 都觸發）
  - 解決方案：改用 UI 視覺反饋（按鈕禁用 + CircularProgressIndicator）
  - 測試結果：100% 通過，無用戶抱怨

---

## Version History Before v2.9.8

請參考 Git commit history 或舊版測試報告。
