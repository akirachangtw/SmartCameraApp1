# Pixel 9a 實機測試指南 - v2.x.x Optical Flow Point Tracking

## 📱 測試環境

- **測試設備**: Google Pixel 9a
- **Android 版本**: Android 15 (16KB Page Size)
- **測試分支**: `feature/optical-flow-phase5-6`
- **基礎版本**: v2.9.9-Day1-2 (versionCode: 60)
- **測試日期**: 2026-02-03

## 🔧 測試前準備

### 1. 拉取代碼並編譯

```bash
cd /path/to/SmartCameraApp1

# 拉取功能分支
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
# 查看設備 ID
adb devices

# 安裝 APK
adb -s <DEVICE_ID> install -r app/build/outputs/apk/device/debug/app-device-debug.apk

# 啟動應用
adb -s <DEVICE_ID> shell am start -n com.akirachang.ai.photographer/.MainActivity
```

> **注意**: device flavor 的 package name 為 `com.akirachang.ai.photographer`（無 suffix）

### 3. 啟用 Logcat 監控

```bash
# 清除舊日誌
adb -s <DEVICE_ID> logcat -c

# 開始監控 (保存到檔案)
adb -s <DEVICE_ID> logcat -s "CameraViewModel:* OpticalFlowTracker:* SmartCameraAnalyzer:* LiveCameraSource:* CoordinateMapper:*" > docs/test_results/v2.x.x-OpticalFlow/logcat_optical_flow.log &
```

### 4. 確認 Feature Flag 啟用

Logcat 中應出現：
```
CameraViewModel: Optical flow tracking callback registered
```

---

## ✅ 測試場景

---

### 場景 1: 基本光學流動追蹤 ⭐ CRITICAL

**目標**: 驗證點擊紋理豐富物件後，對焦指標跟隨視觉特徵移動

**操作步驟**:
1. 開啟應用，將相機對向**物件豐富的場景**（桌面、書架、人群等）
2. 確認預覽畫面正常（無黑屏）
3. **點擊畫面中一個有明顯紋理的物件**（例如：衣服、物件邊緣、書本）
4. 緩慢移動手持相機（平移動作）
5. 觀察對焦指標是否隨視觉特徵移動

**預期結果**:
- ✅ 點擊後白色虛線圓圈出現在點擊位置
- ✅ 緩慢移動時圓圈跟隨該物件移動
- ✅ 追蹤持續至少 5 秒以上（在穩定紋理區域）
- ✅ Logcat 顯示: `Track: quality=0.9x, distance=Xpx`

**Logcat 驗證**:
```bash
grep "Track:" docs/test_results/v2.x.x-OpticalFlow/logcat_optical_flow.log
# 預期: quality >= 0.8, 持續多個 frame
```

**通過標準**:
- [ ] 對焦圓圈隨視觉特徵移動
- [ ] 追蹤 quality >= 0.8 持續 5+ 秒
- [ ] LK 延遲 < 20ms (P95)

---

### 場景 2: Re-tap 快速目標切換 ⭐ CRITICAL

**目標**: 驗證在追蹤活躍中，點擊新位置可無狀態污染地切換目標

**操作步驟**:
1. 按場景 1 啟動追蹤（確認追蹤正常進行）
2. 在圓圈跟隨移動中，**迅速點擊另一個物件**（距離當前追蹤點至少半屏）
3. 觀察對焦指標是否移動到新點並開始追蹤新目標

**預期結果**:
- ✅ 圓圈**立即移動**到新點位置
- ✅ 新追蹤開始後 quality >= 0.8（如新點有紋理）
- ✅ **無 status=0 即失蹤** 的閃變（修復前的 bug）
- ✅ Logcat 顯示兩次 `Started tracking at` 以及新追蹤的 `Track:` 日誌

**通過標準**:
- [ ] 切換無閃變（不會瞬間消失再重新出現）
- [ ] 新目標追蹤正常啟動
- [ ] Logcat 無 `status=0` 在新追蹤開始後的第一個 frame

---

### 場景 3: 無紋理區域處理 ⭐ HIGH

**目標**: 驗證點擊無紋理區域時正確報告追蹤失敗（非崩潰）

**操作步驟**:
1. 將相機對向有均勻大面積色彩的區域（白色牆面、天空、桌面中央）
2. **點擊該均勻色彩區域**

**預期結果**:
- ✅ 對焦圓圈出現（白色/藍色閃動後）
- ✅ 圓圈短暫後消失（tracking lost 後自動清除）
- ✅ **無崩潰、無白屏、無 ANR**
- ✅ Logcat 顯示: `Optical flow failed: status=0` 後 `Optical flow tracking lost`

**通過標準**:
- [ ] 無崩潰或異常
- [ ] 圓圈在 1-2 秒內自動消失
- [ ] 可再次點擊開啟新追蹤

---

### 場景 4: ML Kit 追蹤優先於 Optical Flow ⭐ HIGH

**目標**: 驗證點擊已偵測到的人臉/物件時，使用 ML Kit 追蹤而非 Optical Flow

**操作步驟**:
1. 將相機對向**人臉**（可使用前鏡頭自拍，或對向他人）
2. 等待 ML Kit 偵測到人臉（Logcat 出現 face detection 日誌）
3. **點擊人臉位置**

**預期結果**:
- ✅ 使用 ML Kit 追蹤模式（Logcat: `ML Kit tracking started`）
- ✅ **不會**觸發 Optical Flow 追蹤（無 `Optical flow tracking started`）
- ✅ 對焦圓圈跟隨人臉

**通過標準**:
- [ ] Logcat 確認為 ML Kit 模式而非 Optical Flow
- [ ] 追蹤行為與 v2.9.9 Traycer 一致

---

### 場景 5: AE/AF Lock 後再點擊啟動 Optical Flow ⭐ MEDIUM

**目標**: 驗證 AE/AF 鎖定狀態下點擊可正確解鎖並開啟新追蹤

**操作步驟**:
1. **長按**畫面啟動 AE/AF Lock（黃色方框出現）
2. 確認鎖定正常（曝光不變）
3. **單點擊**畫面中有紋理的物件

**預期結果**:
- ✅ AE/AF Lock 解除（黃色方框消失）
- ✅ 啟動新的追蹤（Optical Flow 或 ML Kit，取決於點擊位置）
- ✅ 追蹤正常進行

**通過標準**:
- [ ] Lock 正確解除
- [ ] 新追蹤正常啟動
- [ ] 無狀態冲突（鎖定和追蹤不同時激活）

---

### 場景 6: 效能監測 ⭐ MEDIUM

**目標**: 確認 Pixel 9a 上 Optical Flow 不影響整體效能

**操作步驟**:
1. 開啟追蹤並維持約 30 秒
2. 觀察 FPS 計數器（右下角）
3. 觀察 Logcat 中的效能日誌

**預期結果**:
- ✅ FPS >= 20（Pixel 9a 目標）
- ✅ 記憶體無明顯增長趨勢
- ✅ Thermal 狀態為 NONE
- ✅ LK 延遲 P95 < 20ms

**Logcat 驗證**:
```bash
grep "Performance:" docs/test_results/v2.x.x-OpticalFlow/logcat_optical_flow.log | tail -10
# 預期: FPS >= 20, Dropped = 0%, Thermal = NONE
```

**通過標準**:
- [ ] FPS 穩定 >= 20
- [ ] 記憶體 RSS 增長 < 10MB（compared to app startup）
- [ ] Thermal = NONE (30 秒內)

---

### 場景 7: 相機切換後追蹤行為 ⭐ LOW

**目標**: 切換前後/後鏡頭時追蹤狀態正確清除

**操作步驟**:
1. 啟動光學流動追蹤（後鏡頭）
2. **切換到前鏡頭**
3. 觀察追蹤狀態

**預期結果**:
- ✅ 切換時追蹤自動停止
- ✅ 圓圈消失
- ✅ 切回後鏡頭可重新開啟追蹤

**通過標準**:
- [ ] 切換時無崩潰
- [ ] 追蹤狀態正確清除

---

## 📸 截圖要求

請截圖並儲存至 `docs/test_results/v2.x.x-OpticalFlow/`:

| 截圖 | 說明 |
|------|------|
| `screenshot_tracking_active.png` | 對焦圓圈跟隨物件 |
| `screenshot_retap_switch.png` | Re-tap 後圓圈移動到新位置 |
| `screenshot_tracking_lost.png` | 無紋理區域追蹤失敗後的畫面 |
| `screenshot_mlkit_priority.png` | 點擊人臉使用 ML Kit 追蹤 |

截圖方法：
```bash
adb -s <DEVICE_ID> shell 'screencap -p /sdcard/Download/screenshot.png'
adb -s <DEVICE_ID> pull //sdcard//Download//screenshot.png docs/test_results/v2.x.x-OpticalFlow/screenshot_tracking_active.png
adb -s <DEVICE_ID> shell 'rm /sdcard/Download/screenshot.png'
```

---

## 📝 測試報告模板

測試完成後，請建立 `PIXEL9A_TEST_REPORT.md`，填寫以下內容：

```markdown
# Pixel 9a 測試報告 - v2.x.x Optical Flow

## 測試日期: YYYY-MM-DD
## 測試人員:

## 通過/失敗 清單
- [ ] 場景 1: 基本光學流動追蹤
- [ ] 場景 2: Re-tap 快速目標切換
- [ ] 場景 3: 無紋理區域處理
- [ ] 場景 4: ML Kit 追蹤優先
- [ ] 場景 5: AE/AF Lock 後再點擊
- [ ] 場景 6: 效能監測
- [ ] 場景 7: 相機切換後行為

## 效能數據
- FPS:
- 記憶體 RSS:
- LK 延遲 (P50/P95):
- Thermal:

## 異常記錄
（空白表示無異常）

## 截圖
（列出截圖檔名）
```
