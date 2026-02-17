# P0 Verification Test Guide - v2.11.1

## Test Environment

- **Device**: Emulator (Medium_Phone_API_36.1)
- **Device ID**: emulator-5554
- **Screen Size**: 1080x2400 (PreviewViewportContainer screenSize: 1080x2274 after padding)
- **APK**: app-emulator-debug.apk (Phase 1+2+3 混合方案)
- **Test Date**: 2026-02-17 (Phase 1~3 修復完成後重新驗證)

## P0 Fixes to Verify

### ✅ Issue #1: 預覽畫面外無法點擊對焦
**修復方式**: LiveCameraSource 使用 PreviewView.meteringPointFactory（自動處理 FIT_CENTER letterbox）

**驗證方法**:
1. 切換到會產生 letterbox 的長寬比（如 4:3 或 1:1）
2. 點擊黑邊（letterbox）區域
3. **預期結果**: 不應該觸發對焦指示器
4. 點擊預覽內容區域
5. **預期結果**: 正常顯示對焦指示器（綠色圓圈）

### ✅ Issue #2: Grid 線限制在預覽邊界
**修復方式**: GridOverlay 接受 contentBounds 參數，限制繪製區域

**驗證方法**:
1. 啟用 Rule of Thirds Grid（在設定中）
2. 測試三種長寬比：
   - 16:9（可能填滿螢幕，無 letterbox）
   - 4:3（可能有左右黑邊）
   - 1:1（可能有上下黑邊）
3. **預期結果**: Grid 線只顯示在預覽內容區域，不延伸到黑邊

---

## Test Procedure

### Phase 1: 基本功能驗證

#### 1.1 App 啟動檢查
- [x] App 啟動成功（無 crash）
- [x] 相機預覽正常顯示
- [x] UI 元件正常顯示（拍照按鈕、FPS 指示器）
- [x] Logcat 無 ERROR/FATAL（只有模擬器常見警告）

**狀態**: ✅ PASS

---

### Phase 2: Grid 邊界測試（Issue #2）

#### 2.1 啟用 Grid
**手動操作步驟**:
1. 點擊設定按鈕（右上角齒輪圖示）
2. 找到 "Grid" 或"構圖輔助" 選項
3. 啟用 Rule of Thirds Grid

#### 2.2 測試 16:9 長寬比
**截圖命令**:
```bash
# 確認當前長寬比（檢查 UI 或 Logcat）
adb -s emulator-5554 shell "screencap -p > /sdcard/Download/p0_grid_16_9.png"
adb -s emulator-5554 pull //sdcard//Download//p0_grid_16_9.png docs/test_results/v2.11.1/
adb -s emulator-5554 shell "rm /sdcard/Download/p0_grid_16_9.png"
```

**檢查項目**:
- [ ] Grid 線顯示
- [ ] Grid 線沒有超出預覽邊界
- [ ] 如果有 letterbox，Grid 不延伸到黑邊區域

#### 2.3 測試 4:3 長寬比
**手動操作**:
1. 點擊長寬比切換按鈕（或在設定中切換）
2. 選擇 4:3

**截圖命令**:
```bash
adb -s emulator-5554 shell "screencap -p > /sdcard/Download/p0_grid_4_3.png"
adb -s emulator-5554 pull //sdcard//Download//p0_grid_4_3.png docs/test_results/v2.11.1/
adb -s emulator-5554 shell "rm /sdcard/Download/p0_grid_4_3.png"
```

**檢查項目**:
- [ ] Grid 線顯示
- [ ] Grid 線限制在內容區域（如果有左右黑邊，Grid 不延伸）
- [ ] Grid 線與預覽內容對齊

#### 2.4 測試 1:1 長寬比
**手動操作**:
1. 切換到 1:1 長寬比

**截圖命令**:
```bash
adb -s emulator-5554 shell "screencap -p > /sdcard/Download/p0_grid_1_1.png"
adb -s emulator-5554 pull //sdcard//Download//p0_grid_1_1.png docs/test_results/v2.11.1/
adb -s emulator-5554 shell "rm /sdcard/Download/p0_grid_1_1.png"
```

**檢查項目**:
- [ ] Grid 線顯示
- [ ] Grid 線限制在正方形內容區域
- [ ] 如果有上下黑邊，Grid 不延伸

---

### Phase 3: Tap-to-Focus 邊界測試（Issue #1）

#### 3.1 測試預覽內點擊（正常對焦）
**自動化命令**（點擊螢幕中心）:
```bash
# 點擊中心點 (540, 1200)
adb -s emulator-5554 shell input tap 540 1200
sleep 1

# 截圖對焦指示器
adb -s emulator-5554 shell "screencap -p > /sdcard/Download/p0_focus_center.png"
adb -s emulator-5554 pull //sdcard//Download//p0_focus_center.png docs/test_results/v2.11.1/
adb -s emulator-5554 shell "rm /sdcard/Download/p0_focus_center.png"
```

**檢查項目**:
- [ ] 對焦指示器出現（綠色圓圈）
- [ ] 指示器位置在點擊位置附近
- [ ] Logcat 顯示對焦成功

#### 3.2 測試 Letterbox 區域點擊（不應對焦）
**前置條件**: 切換到會產生 letterbox 的長寬比（4:3 或 1:1）

**4:3 模式 - 測試左側黑邊**:
```bash
# 計算 letterbox 區域座標（需要根據實際 contentBounds）
# 假設 4:3 有左右黑邊，點擊左側黑邊 (50, 1200)
adb -s emulator-5554 shell input tap 50 1200
sleep 1

# 截圖
adb -s emulator-5554 shell "screencap -p > /sdcard/Download/p0_focus_letterbox_left.png"
adb -s emulator-5554 pull //sdcard//Download//p0_focus_letterbox_left.png docs/test_results/v2.11.1/
adb -s emulator-5554 shell "rm /sdcard/Download/p0_focus_letterbox_left.png"
```

**1:1 模式 - 測試上方黑邊**:
```bash
# 點擊上方黑邊 (540, 50)
adb -s emulator-5554 shell input tap 540 50
sleep 1

# 截圖
adb -s emulator-5554 shell "screencap -p > /sdcard/Download/p0_focus_letterbox_top.png"
adb -s emulator-5554 pull //sdcard//Download//p0_focus_letterbox_top.png docs/test_results/v2.11.1/
adb -s emulator-5554 shell "rm /sdcard/Download/p0_focus_letterbox_top.png"
```

**檢查項目**:
- [ ] **不應出現對焦指示器**（這是關鍵！）
- [ ] Logcat 無錯誤訊息
- [ ] UI 無異常反應

---

## Actual Test Results (2026-02-17 Phase 1~3 Re-Verification)

### Issue #1 Fix Verification ✅ PASS
- ✅ 16:9 中心點擊 (540, 1200) → 對焦成功 `Focus successful at (540.0, 1137.0)`
- ✅ 16:9 letterbox 頂部點擊 (y=50, y=150) → **無 Log，正確過濾** ✅
- ✅ 4:3 letterbox 頂部點擊 (y=200，contentBounds.top=417) → **無 Log，正確過濾** ✅
- ✅ 4:3 中心點擊 (540, 1137) → 對焦成功 `Focus successful at (540.0, 1074.0)`

修復方式：UI 層 pointerInput 邊界過濾（Phase 2），使用 state.contentBounds.contains() 過濾 letterbox 點擊

### Issue #2 Fix Verification ✅ PASS
- ✅ 16:9: contentBounds=(0,177,1080,2097) h=1920, 比例 9:16 ✓
- ✅ 4:3: contentBounds=(0,417,1080,1857) h=1440, 比例 3:4 ✓（頂部黑邊可見，Grid 不延伸）
- ✅ 1:1: contentBounds=(0,597,1080,1677) h=1080, 比例 1:1 ✓（上下黑邊均等，Grid 只在正方形區域）

修復方式：PreviewViewportContainer 整合（Phase 1）+ Portrait 比例反轉修正 + Grid 初始隱藏（Phase 3）

---

## Expected Results Summary

### Issue #1 Fix Verification
- ✅ 預覽內容區域點擊 → 正常對焦
- ✅ Letterbox 黑邊區域點擊 → **不觸發對焦**（UI 層 pointerInput 邊界過濾）

### Issue #2 Fix Verification
- ✅ Grid 線在 16:9 → 限制在預覽範圍（height=1920）
- ✅ Grid 線在 4:3 → 不延伸到上下黑邊（height=1440）
- ✅ Grid 線在 1:1 → 不延伸到上下黑邊（height=1080，正方形）

---

## Logcat Monitoring

**查看實時 Logcat**:
```bash
adb -s emulator-5554 logcat -v time | grep -E "CameraViewModel|LiveCameraSource|GridOverlay|PreviewViewportContainer"
```

**查看對焦相關 Log**:
```bash
grep -E "focus|Focus|metering" docs/test_results/v2.11.1/emulator_test_logcat.log | tail -20
```

**查看座標轉換 Log**:
```bash
grep -E "v2.11.1 P0|CoordinateTransformer|contentBounds" docs/test_results/v2.11.1/emulator_test_logcat.log | tail -20
```

---

## Test Result Template

### Grid 邊界測試結果

| 長寬比 | Grid 顯示 | 限制在內容區域 | Letterbox 處理 | 結果 |
|--------|-----------|----------------|----------------|------|
| 16:9   | [ ]       | [ ]            | N/A            |      |
| 4:3    | [ ]       | [ ]            | [ ]            |      |
| 1:1    | [ ]       | [ ]            | [ ]            |      |

### Tap-to-Focus 邊界測試結果

| 測試點位置 | 對焦指示器 | 座標正確 | 結果 |
|------------|------------|----------|------|
| 中心區域   | [ ]        | [ ]      |      |
| Letterbox（左） | [ ] 不應出現 | N/A |      |
| Letterbox（上） | [ ] 不應出現 | N/A |      |

---

## Notes

- 模擬器可能無法完全模擬實機對焦行為（無真實相機 sensor）
- 主要驗證座標系統是否正確，不需驗證實際對焦效果
- Grid 邊界驗證是視覺檢查，需要截圖比對
- 建議最終在實機上進行完整驗證（Pixel 9a）
