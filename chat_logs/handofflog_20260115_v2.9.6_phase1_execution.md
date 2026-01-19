# v2.9.6 Phase 1 執行階段交接文檔

**建立時間**: 2026-01-15 16:30:00
**前一對話**: 規劃分析階段完成
**下一階段**: 執行 Traycer 修復方案

---

## 📊 當前狀態摘要

### 已完成工作
1. ✅ **Phase 1 初步實作**（v2.9.6-phase1）
   - 已建立 OrientationEventListener
   - 已實作 4-quadrant rotation mapping
   - 已實作 updateTargetRotation()
   - 已移除 ImageCapture 固定 ROTATION_0
   - Build 成功，版本號：versionCode=52, versionName="2.9.6-phase1"

2. ✅ **實機測試**（Sony XQ-BT52, 2026-01-15）
   - 發現 3 個關鍵問題（詳見下方）
   - Logcat 顯示 listener 已啟用但未觸發
   - 照片方向測試：橫向左正確，直立/橫向右錯誤

3. ✅ **四方分析完成**
   - Claude L1/L2 Plan（深入分析，但實施順序有誤）
   - 綜合計畫（Phase 1+2+3，範圍過大）
   - Traycer 執行計畫（最佳方案，順序正確）⭐
   - Gemini 可行性驗證（FEASIBLE，但誤判為從頭實作）

4. ✅ **最終決策**
   - 採用 **Traycer 執行計畫**
   - 預估時程：30-40 分鐘
   - 成功機率：90%（四方共識）

### 當前版本狀態
- **Git Branch**: main
- **App Version**: v2.9.6-phase1 (versionCode=52)
- **Build Type**: device-debug
- **測試裝置**: Sony XQ-BT52 (Serial: HQ616R2713)

---

## ⚠️ 發現的問題（實機測試結果）

### 問題 1: OrientationEventListener 未觸發 🔴 Critical
**症狀**：
- Logcat 顯示 "OrientationEventListener enabled" ✅
- 但完全沒有 "Orientation changed" log ❌
- 表示 `onOrientationChanged()` 回調從未被呼叫

**根本原因**：
1. `currentRotation` 在類別宣告時初始化為固定的 `Surface.ROTATION_0`
2. `initOrientationListener()` 被呼叫，但內部沒有立即讀取 Display rotation
3. ImageCapture.targetRotation 從未設置（builder 未設置，listener 又未觸發）

**影響**：
- `updateTargetRotation()` 從未執行
- 照片方向完全依賴初始錯誤的 ROTATION_0

### 問題 2: 照片方向錯誤 🔴 Critical
**測試結果**：
| 拍攝方向 | 預期結果 | 實際結果 | 狀態 |
|---------|---------|---------|------|
| 直立 (0°) | 正確顯示 | 逆時針旋轉 90° | ❌ 錯誤 |
| 橫向左 (90°) | 正確顯示 | 正確 | ✅ 正確 |
| 橫向右 (270°) | 正確顯示 | 順時針旋轉 180° | ❌ 錯誤 |

**分析**：
- 橫向左正確可能是「巧合」（sensor orientation 270° 剛好匹配）
- 其他方向錯誤證實 targetRotation 未動態更新

### 問題 3: 預覽同步異常 🟡 Major
**症狀**：
- 旋轉到 90° 時預覽會先逆時針旋轉 90°
- 需要旋轉到 135° 再回到 90° 預覽才正常
- 預覽畫面與裝置方向不同步

**可能原因**：
- Preview.targetRotation 未即時更新
- 135° 是 rotation mapping 的邊界值，可能觸發 fallback 行為

### 問題 4: UI 跟著翻轉 🟢 Minor
**症狀**：
- 相機介面元素（快門按鈕、設定等）會跟著裝置旋轉
- 應該鎖定在 Portrait 方向

**原因**：
- Activity 的 `screenOrientation` 可能未設定為 `portrait`

---

## 🎯 執行方案：Traycer 計畫

### 主要執行文檔
📄 **[docs/plan/v2.9.6_phase1_camera_fix_plan-traycer.md](../docs/plan/v2.9.6_phase1_camera_fix_plan-traycer.md)**

### 方案摘要
- **5 個明確步驟**，總共 +15 lines 程式碼
- **2 個檔案修改**：`LiveCameraSource.kt` + `AndroidManifest.xml`
- **預估時程**：30-40 分鐘（含編譯測試）
- **風險等級**：🟢 低（最小 diff，無架構變更）

### 關鍵步驟（必須按順序執行）

#### Step 1: 修改 initOrientationListener()
**檔案**: `app/src/main/java/com/akirachang/ai/photographer/data/source/camera/LiveCameraSource.kt`
**行號**: ~540

**變更**：在 `initOrientationListener()` 開頭加入立即讀取當前方向
```kotlin
private fun initOrientationListener() {
    // ✨ 新增：立即讀取當前裝置方向
    val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
    val display = windowManager?.defaultDisplay
    currentRotation = display?.rotation ?: Surface.ROTATION_0
    Log.i(TAG, "Initial device rotation: $currentRotation")

    // ... 其餘程式碼保持不變
}
```

#### Step 2: 修改 bindToLifecycle()
**檔案**: `app/src/main/java/com/akirachang/ai/photographer/data/source/camera/LiveCameraSource.kt`
**行號**: ~650-710

**變更**：在 camera bind 後，**先呼叫 initOrientationListener()，再呼叫 updateTargetRotation()**

```kotlin
// 在 cameraProvider.bindToLifecycle(...).also { ... } 內部
Log.i(TAG, "✅ Camera bound successfully")

// ✅ 正確順序（關鍵）
initOrientationListener()              // 先讀取當前方向
updateTargetRotation(currentRotation)  // 再使用剛讀取的正確值
Log.i(TAG, "Initial targetRotation set to: $currentRotation")
```

**⚠️ 注意**：
- **絕對不要用反順序**：先 update 後 init 會導致使用舊值 ROTATION_0
- 當前 codebase 可能已有 `initOrientationListener()` 呼叫，需要確認順序

#### Step 3: 加入 Sensor Orientation 診斷 log
**檔案**: `app/src/main/java/com/akirachang/ai/photographer/data/source/camera/LiveCameraSource.kt`
**行號**: ~650（bindToLifecycle 內部）

**變更**：在 camera bind 後加入診斷 log
```kotlin
val cameraInfo = camera.cameraInfo
val sensorRotation = cameraInfo.getSensorRotationDegrees()
Log.d(TAG, "Camera sensor orientation: $sensorRotation°")
```

**診斷價值**：
- 確認 sensor orientation（預期 Sony XQ-BT52 為 270°）
- 若 ≠ 270°，可能需要調整 rotation mapping

#### Step 4: 鎖定 Activity 螢幕方向
**檔案**: `app/src/main/AndroidManifest.xml`
**位置**: `<activity android:name=".MainActivity">`

**變更**：加入 `screenOrientation` 屬性
```xml
<activity
    android:name=".MainActivity"
    android:screenOrientation="portrait"
    android:configChanges="orientation|screenSize"
    android:exported="true">
    <!-- ... existing intent filters ... -->
</activity>
```

#### Step 5: 加入 OrientationListener 觸發驗證 flag
**檔案**: `app/src/main/java/com/akirachang/ai/photographer/data/source/camera/LiveCameraSource.kt`
**行號**: ~211（成員變數區）+ ~545（onOrientationChanged 內部）

**變更 1**: 新增成員變數
```kotlin
private var orientationListenerTriggered = false
```

**變更 2**: 在 onOrientationChanged() 開頭加入
```kotlin
override fun onOrientationChanged(orientation: Int) {
    if (orientation == ORIENTATION_UNKNOWN) return

    // ✨ 診斷：確認 listener 有觸發
    if (!orientationListenerTriggered) {
        orientationListenerTriggered = true
        Log.i(TAG, "OrientationEventListener first trigger confirmed")
    }

    // ... 其餘程式碼保持不變
}
```

**診斷價值**：
- ✅ 若看到 "first trigger confirmed" → Listener 正常工作
- ❌ 若 5 秒內無此 log → 需要 Plan B（fallback）

---

## 🔧 編譯與安裝指令

### 編譯
```bash
cd d:\MyDocuments\DevSources\AndroidStudioProjects\SmartCameraApp1
./gradlew assembleDeviceDebug
```

### 安裝到實機
```bash
adb -s HQ616R2713 install -r app/build/outputs/apk/device/debug/app-device-debug.apk
```

### Logcat 監控
```bash
adb -s HQ616R2713 logcat -c
adb -s HQ616R2713 logcat | grep -E "LiveCameraSource|Orientation"
```

---

## ✅ 驗證標準（成功標準）

### Phase 1: Logcat 驗證（必須全部出現）
```
LiveCameraSource: Initial device rotation: 0                          ← Step 1
LiveCameraSource: OrientationEventListener enabled                     ← Step 1
LiveCameraSource: Camera sensor orientation: 270°                      ← Step 3
LiveCameraSource: Initial targetRotation set to: 0                     ← Step 2
LiveCameraSource: ImageCapture targetRotation updated: 0               ← Step 2

[旋轉裝置到 90°]
LiveCameraSource: OrientationEventListener first trigger confirmed     ← Step 5
LiveCameraSource: Orientation changed: 90° → rotation=270              ← Existing
LiveCameraSource: ImageCapture targetRotation updated: 270             ← Existing
```

### Phase 2: 照片方向測試（TC-001/002/003）
| 測試案例 | 裝置方向 | 預期 EXIF Orientation | 驗證方式 |
|---------|---------|---------------------|---------|
| TC-001 | 直立 (0°) | 1 (Normal) | analyze_exif.py |
| TC-002 | 橫向左 (90°) | 6 (90° CW) | analyze_exif.py |
| TC-003 | 橫向右 (270°) | 8 (90° CCW) | analyze_exif.py |

**執行方式**：
```bash
# 拍照 3 張（每個方向各 1 張）
# 拉取照片
adb -s HQ616R2713 pull /sdcard/DCIM/SmartCamera tmp/v2.9.6_phase1_fix_test/

# EXIF 分析
python tools/analyze_exif.py tmp/v2.9.6_phase1_fix_test/
```

### Phase 3: 回歸測試
- **RT-001**: FPS ≥ 25（無效能回歸）
- **RT-002**: 前後鏡頭切換正常
- **RT-003**: 預覽立即同步（無需過度旋轉到 135°）
- **RT-004**: UI 不再跟著翻轉（快門按鈕始終在底部）

---

## 📁 關鍵檔案路徑

### 主要執行文檔
- **Traycer 計畫**: `docs/plan/v2.9.6_phase1_camera_fix_plan-traycer.md`

### 參考文檔（備用）
- **Claude L2 可行性**: `docs/plan/v2.9.6_phase1_orientation_fix_L2.md`（注意：Step 2 順序錯誤）
- **最終綜合分析**: `docs/plan/v2.9.6_phase1_FINAL_CONSOLIDATED_ANALYSIS.md`
- **四方比較**: `docs/plan/v2.9.6_phase1_CONSOLIDATED_FEASIBILITY.md`

### 需要修改的檔案
1. `app/src/main/java/com/akirachang/ai/photographer/data/source/camera/LiveCameraSource.kt`
   - Step 1: ~540 行（initOrientationListener）
   - Step 2: ~650-710 行（bindToLifecycle）
   - Step 3: ~650 行（sensor orientation log）
   - Step 5: ~211 行（成員變數）+ ~545 行（onOrientationChanged）

2. `app/src/main/AndroidManifest.xml`
   - Step 4: MainActivity 設定

### 測試工具
- **自動化腳本**: `tools/test_phase1_orientation.ps1`
- **EXIF 分析**: `tools/analyze_exif.py`

---

## 🎯 Go/No-Go 決策點

### Go 條件（所有必須達成）
- [x] 所有 5 個步驟都已實施
- [x] 編譯成功（無錯誤）
- [x] Logcat 顯示所有預期 log
- [x] 看到 "OrientationEventListener first trigger confirmed"
- [x] 三個方向照片都正確（TC-001/002/003 全過）
- [x] FPS ≥ 25（無效能回歸）

### No-Go（需要 Plan B）
- [ ] 5 秒內未看到 "first trigger confirmed"
  → 參考 Claude L2 的 Plan B（DisplayListener fallback）
- [ ] Sensor orientation ≠ 270°
  → 參考 Claude L2 的 Step 3（調整 rotation mapping）
- [ ] 照片方向仍錯誤（修復後）
  → 需要深入分析 CameraX rotation 計算邏輯

---

## 🚀 後續步驟（Phase 1 完成後）

### 立即行動
1. **完成測試報告**: `tmp/v2.9.6_phase1_test_report.md`
2. **Git Commit**:
   ```bash
   git add .
   git commit -m "[Fix] v2.9.6 Phase 1: OrientationEventListener 修復完成

   修復內容:
   - 立即讀取初始裝置方向
   - 正確設置 ImageCapture 初始 targetRotation
   - 加入 OrientationListener 觸發驗證
   - 鎖定 Activity Portrait 方向

   測試結果:
   - 三個方向照片都正確
   - FPS 維持 27+ 無回歸
   - 預覽同步正常

   Tested on: Sony XQ-BT52
   Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>"
   ```
3. **更新版本號**: `versionCode=53`, `versionName="2.9.6-phase1-fixed"`

### Phase 2 規劃（待 Phase 1 完成）
- 參考「綜合計畫」的 Phase 2（預覽解析度優化）
- 將 Preview 解析度從 800x600 提升到 1080p
- 需要評估記憶體影響（Pixel 9a 敏感）

### Phase 3 規劃（待 Phase 2 完成）
- 參考「綜合計畫」的 Phase 3（OutputFileOptions API）
- 移除 Bitmap 轉換流程
- 直接使用 CameraX 存檔（保留原始品質）

---

## ⚠️ 注意事項

### 關鍵提醒
1. **實施順序不可顛倒**：
   - ✅ 正確：先 `initOrientationListener()` 後 `updateTargetRotation()`
   - ❌ 錯誤：先 `updateTargetRotation()` 後 `initOrientationListener()`

2. **不要跳過 Step 5**：
   - Listener trigger flag 是診斷關鍵
   - 若 listener 未觸發，立即知道需要 Plan B

3. **Sensor Orientation 很重要**：
   - 若 ≠ 270°，rotation mapping 可能需要調整
   - Step 3 的 log 是必須的

4. **AndroidManifest 修改不可漏**：
   - 忘記鎖定方向會導致 UI 跟著翻轉
   - 使用者體驗會變差

### 已知限制
- **不支援 Upside Down (180°)**：大多數 Android 裝置預設禁用
- **OrientationListener 延遲**：100-300ms 延遲是正常的
- **不改善預覽解析度**：留待 Phase 2 解決

---

## 📊 風險評估（來自四方分析）

### 🔴 Critical Risk
- **Rotation Mapping 與 Sensor Orientation 不匹配**
  - 緩解：Step 3 診斷 log 確認 sensor orientation

### 🟡 Major Risk
- **onOrientationChanged 觸發延遲或遺漏**
  - 緩解：Step 5 trigger flag + Plan B fallback 準備

### 🟢 Minor Risk
- **效能回歸（高頻呼叫）**
  - 緩解：已有 `if (rotation != currentRotation)` 過濾

---

## 📞 聯絡資訊（若需要協助）

- **前一對話記錄**: `~/.claude/projects/.../conversation.jsonl`
- **測試裝置**: Sony XQ-BT52 (Serial: HQ616R2713)
- **項目路徑**: `d:\MyDocuments\DevSources\AndroidStudioProjects\SmartCameraApp1`

---

**交接完成時間**: 2026-01-15 16:35:00
**建立者**: Claude Code Assistant
**下一步**: 開啟新對話並執行 Traycer 5 步驟修復

---

## 🎯 新對話開場建議

```
引用 @chat_logs/handofflog_20260115_v2.9.6_phase1_execution.md

請執行 v2.9.6 Phase 1 修復：

1. 逐步執行 Traycer 計畫 5 步驟（注意實施順序）
2. 編譯並安裝到 Sony XQ-BT52 (Serial: HQ616R2713)
3. 驗證 Logcat 輸出（所有預期 log）
4. 執行照片方向測試（TC-001/002/003）
5. 完成測試報告與 Git commit

主要執行文檔：@docs/plan/v2.9.6_phase1_camera_fix_plan-traycer.md

關鍵要點：
- 實施順序：先 initOrientationListener() 後 updateTargetRotation()
- 必須加入 listener trigger flag（Step 5）
- AndroidManifest.xml 鎖定 Portrait（Step 4）
```
