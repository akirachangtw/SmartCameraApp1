# CameraControl not available - 對話摘要（另存）

## 背景
- 問題現象：Logcat 出現
  - `W CameraRepositoryImpl: Cannot enable HDR: CameraControl not available`
  - `W CameraRepositoryImpl: Cannot disable HDR: CameraControl not available`
- 影響：HDR 手動控制（AUTO/ON/OFF）的 UI/狀態/邏輯正常，但硬體層 HDR（以曝光補償模擬）可能未生效。

## 依據文件
- `devspec/DAILY_DEVELOPMENT_SOP.md`
- `chat_logs/HDR_MANUAL_CONTROL_REAL_DEVICE_TEST_v2.6.1.md`
- `chat_logs/TEST_REPORT_v2.6.1_20260101.md`
- `CAMERACONTROL_ISSUE_ANALYSIS.md`

## 進度結論（v2.6.1）
- HDR 手動控制：模擬器與實機測試均為 ✅ 通過（UI、狀態循環切換、自動 HDR 邏輯、警告 UI 顯示/隱藏、TestImageProvider、5 種光線狀態觸發）。
- 非阻斷問題：`CameraControl not available` 警告在模擬器/實機皆存在；需補做「真實相機預覽/拍照」來驗證 HDR 實際效果。

## 根因（來自 `CAMERACONTROL_ISSUE_ANALYSIS.md`）
- v2.6.1 架構同時存在：
  - `LifecycleCameraController`（舊路徑，模擬器拍照仍可能用到）
  - `LiveCameraSource` / `ProcessCameraProvider`（實機真正管理相機 UseCases）
- `CameraRepositoryImpl.enableHDRMode()/disableHDRMode()` 仍嘗試使用 `_cameraController.cameraControl`，但在實機流程中 `_cameraController` 未 bind 到 lifecycle，因此 `cameraControl` 會是 `null`，導致警告與 HDR 無法套用。

## 修正方案（採用方案 A：低風險）
目標：讓 HDR/曝光補償控制走到「實機可用的 camera.cameraControl」。

### 變更設計
1. 在 `CameraInputSource` 抽象層新增 HDR 控制 API
   - `enableHDR(): Result<Unit>`
   - `disableHDR(): Result<Unit>`
2. `LiveCameraSource` 實作 HDR 控制
   - 使用 `camera.cameraControl.setExposureCompensationIndex(...)`
   - 夾住 `camera.cameraInfo.exposureState.exposureCompensationRange`（避免超出範圍）
   - 若 `camera` 尚未初始化：用 `pendingExposureIndex` 排隊，並在 `bindToLifecycle()` 完成後補套用（也覆蓋切鏡頭 rebind）
3. `StaticImageSource` 實作 no-op
   - 回傳 `Result.success(Unit)` 並 log 註記 no-op
4. `CameraRepositoryImpl.enableHDRMode()/disableHDRMode()` 改為委派
   - `cameraInputSource.enableHDR()/disableHDR()`
   - 移除對 `_cameraController.cameraControl` 的直接存取，從根本消除 `Cannot enable/disable HDR: CameraControl not available`

### 預期影響檔案
- `app/src/main/java/com/akirachang/ai/photographer/data/source/camera/CameraInputSource.kt`
- `app/src/main/java/com/akirachang/ai/photographer/data/source/camera/LiveCameraSource.kt`
- `app/src/main/java/com/akirachang/ai/photographer/data/source/camera/StaticImageSource.kt`
- `app/src/main/java/com/akirachang/ai/photographer/data/repository/CameraRepositoryImpl.kt`

## 驗證計畫
- Logcat：不再出現 `CameraRepositoryImpl: Cannot enable/disable HDR: CameraControl not available`；改看到 `LiveCameraSource` 的 exposure 設定成功日誌。
- 實機拍照：在 ON/OFF/AUTO 各拍同場景（固定測光/構圖），比對亮部/暗部細節與曝光差異；切鏡頭後再切換 HDR，確認仍可套用。

## 後續
- 建議以 Issue（使用 `devspec/issue_bug_camera_pipeline.md`）落盤：主題「CameraControl not available 導致 HDR 套用不確定」，附 log、復現步驟、驗證方式與回滾點。
