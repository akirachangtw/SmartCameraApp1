# Pixel 9a 實機測試指南 — v2.9.9 Phase 9B-fix (前置鏡頭對焦指標跳動修正)

## 📱 測試環境

- **測試設備**: Google Pixel 9a
- **Device ID**: `59131JEBF17244`
- **Android 版本**: Android 15 (16KB Page Size)
- **測試分支**: `feature/optical-flow-phase5-6`
- **基礎版本**: v2.9.9 (versionCode: 60)

---

## 🔧 測試前準備

### 1. 拉取代碼並編譯

```bash
cd /path/to/SmartCameraApp1

# 拉取功能分支最新版本
git fetch origin
git checkout feature/optical-flow-phase5-6
git pull origin feature/optical-flow-phase5-6

# 編譯 Device Debug APK
./gradlew assembleDeviceDebug

# 確認 APK 生成
ls -lh app/build/outputs/apk/device/debug/app-device-debug.apk
```

### 2. 安裝到 Pixel 9a

```bash
# 確認設備連接
adb devices
# 預期輸出: 59131JEBF17244    device

# 安裝 APK（強制覆蓋）
adb -s 59131JEBF17244 install -r app/build/outputs/apk/device/debug/app-device-debug.apk

# 啟動應用
adb -s 59131JEBF17244 shell am start -n com.akirachang.ai.photographer/.MainActivity
```

> **注意**: device flavor 的 package name 為 `com.akirachang.ai.photographer`（無 suffix）

### 3. 啟用 Logcat 監控

```bash
# 清除舊日誌
adb -s 59131JEBF17244 logcat -c

# 開始監控（儲存至本目錄）
adb -s 59131JEBF17244 logcat -s "CameraViewModel:* OpticalFlowTracker:* SmartCameraAnalyzer:* CoordinateMapper:*" > docs/test_results/v2.9.9-Phase9B-fix/logcat_phase9bfix.log &
```

### 4. 確認 Feature Flag 啟用

開啟應用後，Logcat 中應出現：
```
CameraViewModel: Optical flow tracking callback registered
```

若未出現，請確認 `BuildConfig.ENABLE_OPTICAL_FLOW_TRACKING` 為 `true`。

---

## ✅ 測試場景

---

### 場景 1: 前置鏡頭點擊人臉 ⭐ CRITICAL

**目標**: 驗證前置鏡頭對人臉的追蹤圓圈穩定、無跳動（Issue A 修正效果）

**操作步驟**:
1. 開啟應用，切換至**前置鏡頭**
2. 將手機對著自己的臉（確認 ML Kit 偵測到人臉，Logcat 出現 face detection）
3. **點擊人臉位置**
4. 觀察圓圈是否穩定跟隨人臉移動
5. 緩慢移動手持角度，觀察圓圈追蹤平滑度

**預期結果**:
- ✅ 白色虛線圓圈出現在點擊位置
- ✅ 圓圈穩定跟隨人臉，**無可見跳動或追趕效果**
- ✅ 移動時圓圈平滑跟隨（無lag + 突然補齊的跳躍）
- ✅ 追蹤期間 Alpha 保持 1.0（不會突然淡化）

**Logcat 驗證**:
```bash
grep -E "Track:|Velocity clamped|focusIndicatorFading" docs/test_results/v2.9.9-Phase9B-fix/logcat_phase9bfix.log
```
- 無 `focusIndicatorFading` 在追蹤期間出現（Issue B 驗證）
- `Velocity clamped` 出現次數應極少（正常追蹤不需要夾緊）

**通過標準**:
- [ ] 圓圈穩定跟隨，無可見跳動
- [ ] 追蹤期間 Alpha 未觸發淡出
- [ ] Logcat 無追蹤期間的 `focusIndicatorFading` 記錄

---

### 場景 2: 前置鏡頭點擊牆壁（無紋理）⭐ HIGH

**目標**: 驗證無紋理區域時 Optical Flow 正確報告 tracking lost，不崩潰

**操作步驟**:
1. 前置鏡頭對著牆壁或均勻色彩背景
2. **點擊無紋理的均勻色彩區域**
3. 觀察圓圈出現後的行為

**預期結果**:
- ✅ 圓圈先出現在點擊位置
- ✅ Optical Flow 啟動後因無紋理而失敗 → tracking lost
- ✅ 圓圈在 1-2 秒內自動消失
- ✅ **無崩潰、無白屏、無 ANR**
- ✅ 可立即再次點擊開啟新追蹤

**Logcat 驗證**:
```bash
grep -E "Optical flow failed|tracking lost" docs/test_results/v2.9.9-Phase9B-fix/logcat_phase9bfix.log
```

**通過標準**:
- [ ] 無崩潰或異常
- [ ] 圓圈在 1-2 秒內自動消失
- [ ] 可再次點擊開啟追蹤

---

### 場景 3: 快速移動鏡頭（前置）⭐ CRITICAL

**目標**: 驗證快速移動時圓圈平滑跟隨，無明顯延遲或追趕跳躍效果（Issue A 核心驗證）

**操作步驟**:
1. 前置鏡頭點擊有紋理的物件（如衣服、眼睛周邊等）
2. 確認追蹤正常進行（圓圈穩定）
3. **快速旋轉/平移手持角度**（約 30-45 度移動）
4. 觀察圓圈在快速移動過程中的行為

**預期結果**:
- ✅ 圓圈在快速移動中**平滑跟隨**視觉特徵
- ✅ **無明顯的「延遲後突然追趕」效果**（修正前的symptoms）
- ✅ 移動停止後圓圈能在目標位置穩定（不繼續飄動）
- ✅ 若移動太快導致追蹤失敗，圓圈應干凈消失（非跳躍後消失）

**Root Cause Verification**:
- Issue A 修正前：`maxDistancePerFrame=15f` + `StiffnessMedium` → 鏡頭快速移動時圓圈落後，停止後「跳躍追趕」
- Issue A 修正後：`maxDistancePerFrame=25f` + `StiffnessHigh` → 允許每幀更大位移 + 更快動畫響應

**通過標準**:
- [ ] 快速移動時無明顯追趕跳躍
- [ ] 移動停止後圓圈位置穩定
- [ ] 整體視覺流暢度優於修正前

---

### 場景 4: 對焦成功後超過 1 秒（無追蹤）⭐ HIGH

**目標**: 驗證靜態對焦的正確 Fade-out 行為（Alpha 1.0 → 0.4 持續）

**操作步驟**:
1. 將相機（前或後鏡頭均可）對著有紋理的物件
2. **輕點一次**開啟對焦（不是長按 AE/AF Lock）
3. **不再移動鏡頭**，等待觀察圓圈隨時間的變化

**預期結果**:
- ✅ 點擊後圓圈出現，Alpha 為 1.0（完全不透明）
- ✅ 約 **1 秒後**開始淡化（500ms 動畫）
- ✅ 淡化後圓圈以 **Alpha 0.4** 持續顯示（半透明但可見）
- ✅ 圓圈**不會完全消失**（保持 Alpha 0.4 直到下次點擊）
- ✅ 再次點擊時圓圈 Alpha 重置為 1.0

**關鍵時序驗證**:
```
t=0ms      點擊 → Alpha 1.0
t=1000ms   開始淡化
t=1500ms   淡化完成 → Alpha 0.4 (持續)
```

**Logcat 驗證**:
```bash
grep "focusIndicatorFading" docs/test_results/v2.9.9-Phase9B-fix/logcat_phase9bfix.log
```
- 應在約 1 秒後出現 `focusIndicatorFading = true`

**通過標準**:
- [ ] 1 秒後開始淡化
- [ ] 淡化至 Alpha 0.4（可見半透明圓圈）
- [ ] 圓圈持續顯示，不完全消失
- [ ] 下次點擊後重置為 Alpha 1.0

> **注意**: 若前一步驟啟動了 Optical Flow 追蹤（`trackedPointActive=true`），此場景的淡化不會觸發。請確認**靜態對焦**（點擊後追蹤迅速失敗或未啟動 Optical Flow）。

---

### 場景 5: 後置鏡頭測試（回歸驗證）⭐ MEDIUM

**目標**: 確認 Phase 9B-fix 的修改未影響後置鏡頭的既有行為

**操作步驟**:
1. 切換至**後置鏡頭**
2. 對向物件豐富的場景
3. 點擊有紋理的物件，觀察追蹤行為
4. 重複場景 4 的步驟（靜態對焦 Fade-out）
5. 確認追蹤和淡化行為均正常

**預期結果**:
- ✅ 後置鏡頭追蹤行為與 Phase 9A 測試時一致
- ✅ Fade-out 行為符合場景 4 的規範
- ✅ **無任何回歸**

**通過標準**:
- [ ] 後置鏡頭追蹤正常
- [ ] Fade-out 行為正確
- [ ] 無新增異常

---

## 📸 截圖要求

請截圖並儲存至本目錄 `docs/test_results/v2.9.9-Phase9B-fix/`：

| 截圖 | 說明 |
|------|------|
| `screenshot_frontcam_tracking.png` | 前置鏡頭圓圈穩定追蹤（場景 1） |
| `screenshot_tracking_lost.png` | 無紋理區域追蹤失敗後（場景 2） |
| `screenshot_fade_alpha04.png` | Alpha 0.4 持續顯示狀態（場景 4） |

截圖方法（避免 Claude Code API crash）：
```bash
# Step 1: 在手機截圖
adb -s 59131JEBF17244 shell "screencap -p > /sdcard/Download/temp_screen.png"

# Step 2: 拉取到電腦
adb -s 59131JEBF17244 pull //sdcard//Download//temp_screen.png docs/test_results/v2.9.9-Phase9B-fix/screenshot_frontcam_tracking.png

# Step 3: 刪除手機暫存
adb -s 59131JEBF17244 shell "rm /sdcard/Download/temp_screen.png"
```

> ⚠️ **絕對不要使用 `screencap -p > file.png` 直接重導向到電腦** — 會破壞 PNG 二進位結構

---

## 📝 測試報告模板

測試完成後，請在本目錫建立 `PIXEL9A_TEST_REPORT.md`：

```markdown
# Pixel 9a 測試報告 — v2.9.9 Phase 9B-fix

## 測試日期: YYYY-MM-DD
## 測試人員:
## 設備: Pixel 9a (59131JEBF17244)

## 通過/失敗 清單
- [ ] 場景 1: 前置鏡頭點擊人臉 — 圓圈穩定跟隨，無跳動
- [ ] 場景 2: 前置鏡頭點擊牆壁 — tracking lost 正常處理
- [ ] 場景 3: 快速移動鏡頭 — 平滑跟隨，無追趕跳躍
- [ ] 場景 4: 對焦成功後 1 秒淡化至 Alpha 0.4
- [ ] 場景 5: 後置鏡頭回歸驗證

## 效能數據
- FPS:
- 記憶體 RSS:
- LK 延遲 (P50/P95):
- Thermal:

## Issue A 驗證 (圓圈跳動)
- 前置鏡頭追蹤時是否觀察到跳動: (是/否)
- 快速移動後是否觀察到追趕效果: (是/否)

## Issue B 驗證 (Alpha 錯誤淡出)
- 追蹤期間是否觀察到不當淡化: (是/否)
- 靜態對焦淡化行為: (正確/異常)

## 異常記錄
（空白表示無異常）

## 截圖
（列出截圖檔名）
```
